package com.split.ledger.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.split.ledger.entity.Ledger;


/**
 * 账本数据库访问接口
 */
@Mapper
public interface LedgerMapper extends BaseMapper<Ledger> {
    
}
