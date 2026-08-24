package com.split.expense.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 消费记录参与实体
 */
@Data
@NoArgsConstructor
@TableName("expense_participants")
public class ExpenseParticipant {
    private Long ledgerId;
    private Long expenseId;
    private Long memberId;
}