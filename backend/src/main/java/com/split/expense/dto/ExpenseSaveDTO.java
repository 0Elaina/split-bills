package com.split.expense.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 新增消费请求体
 */
@Data
@NoArgsConstructor
public class ExpenseSaveDTO {
    @NotBlank(message = "消费名称不能为空")
    private String title;

    // 使用正则约束金额：大于 0，最多允许两位小数（如 "100", "100.5", "100.50", "0.5"）
    @NotBlank(message = "消费金额不能为空")
    @Pattern(regexp = "^[1-9]\\d*(\\.\\d{1,2})?$|^0\\.\\d{1,2}$", message = "金额格式不正确, 必须大于 0")
    private String amount;

    @NotNull(message = "消费日期不能为空")
    private LocalDate expenseDate;

    @NotNull(message = "付款人不能为空")
    private Long payerMemberId;

    @NotEmpty(message = "参与人不能为空")
    private List<Long> participantMemberIds;
}
