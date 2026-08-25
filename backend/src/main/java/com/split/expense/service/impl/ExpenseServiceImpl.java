package com.split.expense.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.split.expense.dto.ExpenseSaveDTO;
import com.split.expense.entity.Expense;
import com.split.expense.entity.ExpenseParticipant;
import com.split.expense.mapper.ExpenseMapper;
import com.split.expense.mapper.ExpenseParticipantMapper;
import com.split.expense.service.ExpenseService;
import com.split.expense.vo.ExpenseListItemVO;
import com.split.member.entity.Member;
import com.split.member.mapper.MemberMapper;
import com.split.member.vo.MemberVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {
    private final ExpenseMapper expenseMapper;
    private final ExpenseParticipantMapper participantMapper;
    private final MemberMapper memberMapper;

    /**
     * 分页查询指定账本下的消费明细列表
     * 
     * @param ledgerId 账本 ID
     * @param current  页码 (从 1 开始)
     * @param size     每页大小
     * @return 包含付款人和参与人信息的组装后分页结果
     */
    @Override
    public Page<ExpenseListItemVO> getExpensesByLedgerId(Long ledgerId, long current, long size) {
        // 分页查询消费记录并转换为 List 列表
        Page<Expense> expensePage = fetchExpensePage(ledgerId, current, size);
        List<Expense> expenses = expensePage.getRecords();

        // 快速失败语义：如果当前页没有消费记录，直接返回空分页结果，阻断后续无意义查询
        if (expenses.isEmpty()) {
            return new Page<ExpenseListItemVO>(current, size, expensePage.getTotal());
        }

        // 批量查询关联的参与人
        List<ExpenseParticipant> allParticipants = fetchParticipants(expenses);
        // 转换为 Map<Long, MemberVO>
        // 用于内存中快速 O(1) 查找
        Map<Long, MemberVO> memberVoMap = fetchMemberVoMap(expenses, allParticipants);

        // 组装 List 集合
        List<ExpenseListItemVO> voList = expenses.stream()
                .map(e -> convertToVO(e, allParticipants, memberVoMap))
                .collect(Collectors.toList());

        Page<ExpenseListItemVO> resultPage = new Page<>(current, size, expensePage.getTotal());
        resultPage.setRecords(voList);
        return resultPage;
    }

    /**
     * 新增记一笔
     * 
     * @param ledgerId 账本 ID
     * @param dto      请求参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createExpense(Long ledgerId, ExpenseSaveDTO dto) {
        // 将 String 类型的金额转换成 分（Long）
        BigDecimal amountDecimal = new BigDecimal(dto.getAmount());
        long amountCents = amountDecimal.multiply(BigDecimal.valueOf(100)).longValue();

        // 组装并保存主表 Expense
        Expense expense = new Expense();
        BeanUtils.copyProperties(dto, expense);
        expense.setLedgerId(ledgerId);
        expense.setAmountCents(amountCents);
        expenseMapper.insert(expense);

        // 组装并批量保存关联表 ExpenseParticipant
        List<ExpenseParticipant> participants = dto.getParticipantMemberIds().stream()
                .map(memberId -> {
                    ExpenseParticipant ep = new ExpenseParticipant();
                    ep.setLedgerId(ledgerId); // 修复：补充外键 ledger_id
                    ep.setExpenseId(expense.getId());
                    ep.setMemberId(memberId);
                    return ep;
                })
                .collect(Collectors.toList());
        participantMapper.insert(participants);
    }

    /**
     * 分页查询当前账本下的消费记录
     * 
     * @param ledgerId 账本 ID
     * @param current  页码 (从 1 开始)
     * @param size     每页大小
     * @return 消费记录分页结果
     */
    private Page<Expense> fetchExpensePage(Long ledgerId, long current, long size) {
        // 构建查询条件
        LambdaQueryWrapper<Expense> condition = new LambdaQueryWrapper<Expense>()
                .eq(Expense::getLedgerId, ledgerId)
                .orderByDesc(Expense::getExpenseDate)
                .orderByDesc(Expense::getId);

        // 执行分页查询
        Page<Expense> result = expenseMapper.selectPage(new Page<Expense>(current, size), condition);
        return result;
    }

    /**
     * 批量查询关联的参与人
     * 
     * @param expenses 本页的消费记录列表
     * @return 所有参与人关系的列表
     */
    private List<ExpenseParticipant> fetchParticipants(List<Expense> expenses) {
        // 收集本页所有的消费 ID
        List<Long> expenseIds = expenses.stream()
                .map(Expense::getId)
                .collect(Collectors.toList());

        // 批量查询关联的参与人
        List<ExpenseParticipant> participants = participantMapper.selectList(
                new LambdaQueryWrapper<ExpenseParticipant>()
                        .in(ExpenseParticipant::getExpenseId, expenseIds));
        return participants;
    }

    /**
     * 转化为 Map<Long, MemberVO>，供内存中快速 O(1) 查找
     * 
     * @param expenses        本页的消费记录列表
     * @param allParticipants 所有参与人关系的列表
     * @return 成员视图对象的 Map
     */
    private Map<Long, MemberVO> fetchMemberVoMap(List<Expense> expenses,
            List<ExpenseParticipant> allExpenseParticipants) {
        // 收集所有涉及的 memberId (包含 payer 和 participants) 去重
        Set<Long> memberIds = new HashSet<>();
        expenses.forEach(e -> memberIds.add(e.getPayerMemberId()));
        allExpenseParticipants.forEach(p -> memberIds.add(p.getMemberId()));

        // 根据 id 批量查询成员
        List<Member> members = memberMapper.selectByIds(memberIds);
        // 转换为 Map<Long, MemberVO>
        return members.stream()
                .collect(Collectors.toMap(Member::getId, MemberVO::fromEntity));
    }

    /**
     * 组装参与人（过滤出当前消费的参与人，并按 API 要求根据 memberId 升序）
     * 
     * @param expense         当前消费记录
     * @param allParticipants 所有参与人关系的列表
     * @param memberVoMap     成员视图对象的 Map
     * @return 组装后的参与人列表
     */
    private ExpenseListItemVO convertToVO(Expense expense, List<ExpenseParticipant> allParticipants,
            Map<Long, MemberVO> memberVoMap) {
        ExpenseListItemVO vo = ExpenseListItemVO.builder()
                .id(String.valueOf(expense.getId()))
                .title(expense.getTitle())
                .expenseDate(expense.getExpenseDate())
                .createdAt(expense.getCreatedAt())
                .updatedAt(expense.getUpdatedAt())
                .build();

        // 金额换算
        BigDecimal amount = new BigDecimal(expense.getAmountCents())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        vo.setAmount(amount.toPlainString());

        // 绑定成员
        vo.setPayer(memberVoMap.get(expense.getPayerMemberId()));
        List<MemberVO> participants = allParticipants.stream()
                .filter(p -> p.getExpenseId().equals(expense.getId()))
                .map(p -> memberVoMap.get(p.getMemberId()))
                .sorted(Comparator.comparing(m -> Long.valueOf(m.getId())))
                .collect(Collectors.toList());
        vo.setParticipants(participants);
        return vo;
    }
}
