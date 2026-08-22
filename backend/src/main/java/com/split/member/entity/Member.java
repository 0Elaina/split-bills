package com.split.member.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@TableName("member")
@NoArgsConstructor
public class Member {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long ledgerId;
    
    private String name;
    private LocalDateTime createdAt;

    public Member(Long ledgerId, String name) {
        this.ledgerId = ledgerId;
        this.name = name;
        this.createdAt = LocalDateTime.now();
    }
}
