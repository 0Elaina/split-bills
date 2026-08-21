package com.split.ledger.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LedgerSaveDTO {
    @NotBlank(message = "账本名称不能为空")
    @Size(max = 50, message = "账本名称最长不能超过50个字符")
    private String name;
}
