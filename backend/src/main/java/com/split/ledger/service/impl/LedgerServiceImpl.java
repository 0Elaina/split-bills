package com.split.ledger.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.split.common.web.PageVO;
import com.split.ledger.entity.Ledger;
import com.split.ledger.mapper.LedgerMapper;
import com.split.ledger.service.LedgerService;
import com.split.ledger.vo.LedgerVO;

import lombok.RequiredArgsConstructor;

/**
 * 账本业务逻辑实现
 */
@Service
@RequiredArgsConstructor
public class LedgerServiceImpl implements LedgerService {
    private final LedgerMapper ledgerMapper;

    @Override
    public PageVO<LedgerVO> getLedgers(int page, int size) {
        // 1. 发起分页查询，按 ID 降序排列
        Page<Ledger> pager = new Page<>(page, size);
        ledgerMapper.selectPage(pager, new LambdaQueryWrapper<Ledger>()
                .orderByDesc(Ledger::getId));

        // 2. 将 Ledger 实体列表转换为 LedgerVO 列表
        List<LedgerVO> voList = pager.getRecords().stream()
                .map(LedgerVO::fromEntity)
                .toList();

        // 3. 组装符合 API 契约的 PageVO
        return new PageVO<>(
            voList,
            pager.getCurrent(),
            pager.getSize(),
            pager.getTotal(),
            pager.getPages()
        );
    }
}
