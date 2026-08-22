package com.split.member.vo;

import java.time.LocalDateTime;

import org.springframework.beans.BeanUtils;

import com.split.member.entity.Member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberVO {
    private String id;
    private String ledgerId;
    private String name;
    private LocalDateTime createdAt;

    public static MemberVO fromEntity(Member member) {
        if (member == null) return null;
        MemberVO vo = new MemberVO();
        BeanUtils.copyProperties(member, vo);
        vo.setId(String.valueOf(member.getId()));
        vo.setLedgerId(String.valueOf(member.getLedgerId()));
        return vo;
    }
}
