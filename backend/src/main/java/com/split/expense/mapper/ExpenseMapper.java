package com.split.expense.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.split.expense.entity.Expense;

@Mapper
public interface ExpenseMapper extends BaseMapper<Expense> {
    
}
