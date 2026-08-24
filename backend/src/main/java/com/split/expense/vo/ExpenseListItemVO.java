package com.split.expense.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.split.member.vo.MemberVO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 消费明细列表项 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseListItemVO {
    private String id;
    private String title;

    // 格式化为两位小数的人民币元，如 "100.00"
    private String amount;

    private LocalDate expenseDate;

    // 嵌套的完整成员信息
    private MemberVO payer;
    private List<MemberVO> participants;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
