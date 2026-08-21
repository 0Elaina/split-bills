package com.split.ledger.vo;

import java.time.LocalDateTime;

import org.springframework.beans.BeanUtils;

import com.split.ledger.entity.Ledger;

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
    private String id;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static LedgerVO fromEntity(Ledger ledger) {
        LedgerVO vo = new LedgerVO();
        BeanUtils.copyProperties(ledger, vo);
        vo.setId(String.valueOf(ledger.getId()));
        return vo;
    }
}
