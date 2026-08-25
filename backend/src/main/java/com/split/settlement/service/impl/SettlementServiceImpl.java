package com.split.settlement.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.split.expense.entity.Expense;
import com.split.expense.entity.ExpenseParticipant;
import com.split.expense.mapper.ExpenseMapper;
import com.split.expense.mapper.ExpenseParticipantMapper;
import com.split.member.entity.Member;
import com.split.member.mapper.MemberMapper;
import com.split.member.vo.MemberVO;
import com.split.settlement.service.SettlementService;
import com.split.settlement.vo.MemberBalanceVO;
import com.split.settlement.vo.SettlementVO;
import com.split.settlement.vo.TransferVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SettlementServiceImpl implements SettlementService {

    private final MemberMapper memberMapper;
    private final ExpenseMapper expenseMapper;
    private final ExpenseParticipantMapper participantMapper;

    /**
     * 计算并生成指定账本的实时结算结果
     * 
     * @param ledgerId 账本 ID
     * @return 包含所有人余额及转账建议的汇总 VO
     */
    @Override
    public SettlementVO calculateSettlement(Long ledgerId) {
        // 获取账本所有成员。如果没有成员，直接返回空结果
        List<Member> members = memberMapper.selectList(new LambdaQueryWrapper<Member>()
                .eq(Member::getLedgerId, ledgerId));
        if (members.isEmpty()) {
            return SettlementVO.builder()
                    .balances(Collections.emptyList())
                    .transfers(Collections.emptyList())
                    .build();
        }

        // 获取账本所有消费记录
        List<Expense> expenses = expenseMapper.selectList(
                new LambdaQueryWrapper<Expense>()
                        .eq(Expense::getLedgerId, ledgerId));

        // 获取账本所有的参与人关联关系
        List<ExpenseParticipant> participants = participantMapper.selectList(
                new LambdaQueryWrapper<ExpenseParticipant>()
                        .eq(ExpenseParticipant::getLedgerId, ledgerId));

        // 调用核心算法，算清所有实付与应付（单位：分）
        Map<Long, Long> paidMap = new HashMap<>();
        Map<Long, Long> owedMap = new HashMap<>();
        calculateRawBalance(members, expenses, participants, paidMap, owedMap);

        // 组装 MemberBalanceVO 列表，按成员 ID 升序排列保证结果稳定
        List<MemberBalanceVO> balances = members.stream()
                .map(m -> {
                    long paid = paidMap.get(m.getId());
                    long owed = owedMap.get(m.getId());
                    long net = paid - owed;
                    return MemberBalanceVO.builder()
                            .member(MemberVO.fromEntity(m))
                            .paidAmount(formatAmount(paid))
                            .owedAmount(formatAmount(owed))
                            .netBalance(formatAmount(net))
                            .build();
                })
                .sorted(Comparator.comparing(b -> Long.valueOf(b.getMember().getId())))
                .collect(Collectors.toList());
        
        // 贪心算法生成转账建议
        List<TransferVO> transfers = generateTransfers(members, paidMap, owedMap);

        return SettlementVO.builder()
                .balances(balances)
                .transfers(transfers)
                .build();
    }

    /**
     * 计算每个人的实付和应付（单位：分），并抹平尾差
     * 
     * @param members      账本所有成员
     * @param expenses     账本所有消费记录
     * @param participants 账本所有参与人关联关系
     * @param paidMap      每个人的实付金额（单位：分）映射表
     * @param owedMap      每个人的应付金额（单位：分）映射表
     */
    private void calculateRawBalance(
            List<Member> members,
            List<Expense> expenses,
            List<ExpenseParticipant> allParticipants,
            Map<Long, Long> paidMap,
            Map<Long, Long> owedMap) {
        // 初始化每个人的金额字典为 0 分
        for (Member m : members) {
            paidMap.put(m.getId(), 0L);
            owedMap.put(m.getId(), 0L);
        }

        // 将所有参与人按消费 ID 进行分组，方便快速查找 O(1)
        Map<Long, List<ExpenseParticipant>> participantGroup = allParticipants.stream()
                .collect(Collectors.groupingBy(ExpenseParticipant::getExpenseId));

        for (Expense expense : expenses) {
            // 累加付款人的实付金额
            Long payerId = expense.getPayerMemberId();
            if (paidMap.containsKey(payerId)) {
                paidMap.put(payerId, paidMap.get(payerId) + expense.getAmountCents());
            }

            // 计算参与人的应付金额（含尾差分配）
            List<ExpenseParticipant> eps = participantGroup.getOrDefault(expense.getId(), Collections.emptyList());

            int count = eps.size();
            if (count == 0)
                continue;

            // 基础分摊金额
            long baseSplit = expense.getAmountCents() / count;
            // 尾差分配金额
            long remainder = expense.getAmountCents() % count;

            // 规则核心：按 Member ID 升序排列
            eps.sort(Comparator.comparing(ExpenseParticipant::getMemberId));

            for (int i = 0; i < count; i++) {
                Long memberId = eps.get(i).getMemberId();
                if (!owedMap.containsKey(memberId)) {
                    continue;
                }

                // 队伍前面的人（索引 < remainder），每人强行多分摊 1 分钱
                long actualSplit = baseSplit + (i < remainder ? 1 : 0);
                owedMap.put(memberId, owedMap.get(memberId) + actualSplit);
            }
        }
    }

    /**
     * 将长整型的“分”转换为保留两位小数的字符串“元”
     */
    private String formatAmount(long cents) {
        return new java.math.BigDecimal(cents)
                .divide(java.math.BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP)
                .toPlainString();
    }

    /**
     * 用于在贪心算法中保存成员及其当前剩余净余额的内部类
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    private static class BalanceNode {
        private Member member;
        private long balance; // 净余额，正数为应收，负数为应付
    }

    /**
     * 基于所有人的净余额，生成贪心转账建议
     */
    private List<TransferVO> generateTransfers(List<Member> members, Map<Long, Long> paidMap, Map<Long, Long> owedMap) {
        List<BalanceNode> receivers = new ArrayList<>();
        List<BalanceNode> payers = new ArrayList<>();

        // 1. 分别挑选出收款人 (net > 0) 和付款人 (net < 0)
        for (Member m : members) {
            long net = paidMap.get(m.getId()) - owedMap.get(m.getId());
            if (net > 0) {
                receivers.add(new BalanceNode(m, net));
            } else if (net < 0) {
                payers.add(new BalanceNode(m, net));
            }
        }

        List<TransferVO> transfers = new ArrayList<>();

        // 2. 贪心消除，直到两者都为空
        while (!receivers.isEmpty() && !payers.isEmpty()) {
            // 每次循环重新排序：按绝对值降序，相同时按 ID 升序
            receivers.sort((a, b) -> {
                int cmp = Long.compare(b.getBalance(), a.getBalance()); // 降序
                return cmp != 0 ? cmp : Long.compare(a.getMember().getId(), b.getMember().getId());
            });
            payers.sort((a, b) -> {
                int cmp = Long.compare(Math.abs(b.getBalance()), Math.abs(a.getBalance())); // 降序
                return cmp != 0 ? cmp : Long.compare(a.getMember().getId(), b.getMember().getId());
            });

            // 取出池子中最靠前（金额最大）的收款人和付款人
            BalanceNode receiver = receivers.get(0);
            BalanceNode payer = payers.get(0);

            // 本次转账金额为两者的最小值
            long transferAmount = Math.min(receiver.getBalance(), Math.abs(payer.getBalance()));

            // 记录转账建议
            transfers.add(TransferVO.builder()
                    .fromMember(com.split.member.vo.MemberVO.fromEntity(payer.getMember()))
                    .toMember(com.split.member.vo.MemberVO.fromEntity(receiver.getMember()))
                    .amount(formatAmount(transferAmount))
                    .build());

            // 扣减余额
            receiver.setBalance(receiver.getBalance() - transferAmount);
            payer.setBalance(payer.getBalance() + transferAmount); // 负数加上正数，向 0 逼近

            // 如果已经结平，从池子中移除
            if (receiver.getBalance() == 0)
                receivers.remove(0);
            if (payer.getBalance() == 0)
                payers.remove(0);
        }

        return transfers;
    }
}
