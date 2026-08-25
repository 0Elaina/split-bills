package com.split.settlement.vo;

import com.split.member.vo.MemberVO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 两人之间的单笔转账建议
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferVO {
    // 欠款人
    private MemberVO fromMember;

    // 收款人
    private MemberVO toMember;

    // 转账金额
    private String amount;
}
