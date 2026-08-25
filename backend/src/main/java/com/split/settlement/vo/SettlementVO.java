package com.split.settlement.vo;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 账单结算结果（包含成员余额与转账建议）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementVO {
    // 全体成员的金额情况
    private List<MemberBalanceVO> balances;

    // 生成的最优转账建议
    private List<TransferVO> transfers;
}
