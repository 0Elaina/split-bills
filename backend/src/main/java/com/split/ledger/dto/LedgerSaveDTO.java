package com.split.ledger.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LedgerSaveDTO {
    @NotBlank(message = "账本名称不能为空")
    private String name;
}
