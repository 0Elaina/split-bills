package com.split.expense.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.split.expense.entity.ExpenseParticipant;

@Mapper
public interface ExpenseParticipantMapper extends BaseMapper<ExpenseParticipant> {
    
}
