package com.split.ledger.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 账本实体类，映射数据库 ledgers 表
 */
@Data
@TableName("ledgers")
public class Ledger {
    /**
     * 账本 ID，使用数据库自增主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 账本名称
     */
    private String name;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
