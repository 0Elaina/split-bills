package com.split.expense.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 消费记录实体
 */
@Data
@NoArgsConstructor
@TableName("expenses")
public class Expense {
    @TableId(type = IdType.AUTO)
    private Long id;

    // 所属账本
    private Long ledgerId;

    // 消费名, 去首尾空格后入库
    private String title;

    // 核心约束, 人民币金额, 单位: 分
    private Long amountCents;

    // 业务发生日期
    private LocalDate expenseDate;

    // 付款成员
    private Long payerMemberId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
