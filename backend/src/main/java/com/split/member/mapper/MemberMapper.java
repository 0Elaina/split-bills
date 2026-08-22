package com.split.member.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.split.member.entity.Member;

@Mapper
public interface MemberMapper extends BaseMapper<Member> {
    
}
