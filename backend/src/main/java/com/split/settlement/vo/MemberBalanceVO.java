package com.split.settlement.vo;

import com.split.member.vo.MemberVO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 单个成员的余额结算结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberBalanceVO {
    // 对应的成员信息
    private MemberVO member;

    // 支付总金额
    private String paidAmount;

    // 应承担总金额
    private String owedAmount;

    // 净余额 = 实付 - 应付。正数为应收，负数为欠款 (如 "66.66")
    private String netBalance;
}
