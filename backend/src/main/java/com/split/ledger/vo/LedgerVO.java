package com.split.ledger.vo;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 返回给前端的账本视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LedgerVO {
    private Long id;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
