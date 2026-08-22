package com.split.ledger.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.split.common.exception.BadRequestException;
import com.split.common.exception.NotFoundException;
import com.split.common.web.ApiErrorCode;
import com.split.common.web.PageVO;
import com.split.ledger.dto.LedgerSaveDTO;
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
                pager.getPages());
    }

    /**
     * 创建新账本
     * 
     * @param dto 包含新建数据的请求体
     * @return 带有生成 ID 的账本视图对象
     */
    @Override
    public LedgerVO createLedger(LedgerSaveDTO dto) {
        Ledger ledger = new Ledger(dto.getName());
        ledgerMapper.insert(ledger);
        return LedgerVO.fromEntity(ledger);
    }

    /**
     * 根据 ID 获取账本详情
     * 
     * @param id 账本主键
     * @return 账本视图对象
     */
    @Override
    public LedgerVO getLedger(Long id) {
        if (id == null) {
            throw new BadRequestException(ApiErrorCode.VALIDATION_ERROR, "ID 不能为空");
        }
        Ledger ledger = ledgerMapper.selectById(id);
        if (ledger == null) {
            throw new NotFoundException(ApiErrorCode.LEDGER_NOT_FOUND, "账本不存在");
        }

        return LedgerVO.fromEntity(ledger);
    }

    /**
     * 根据 ID 删除账本
     * 
     * @param id 账本主键
     */
    @Override
    public void deleteLedger(Long id) {
        // 如果 ID 无效或账本不存在，getLedger 内部会直接抛出对应的异常，程序中断，绝不会往下走。
        getLedger(id);
        ledgerMapper.deleteById(id);
    }

    /**
     * 修改账本信息
     * 
     * @param id  账本主键
     * @param dto 包含新名称的请求体
     * @return 修改后的账本视图对象
     */
    @Override
    public LedgerVO updateLedger(Long id, LedgerSaveDTO dto) {
        if (id == null) {
            throw new BadRequestException(ApiErrorCode.VALIDATION_ERROR, "ID 不能为空");
        }

        Ledger ledger = ledgerMapper.selectById(id);
        if (ledger == null) {
            throw new NotFoundException(ApiErrorCode.LEDGER_NOT_FOUND, "账本不存在");
        }

        ledger.setName(dto.getName());
        ledger.setUpdatedAt(LocalDateTime.now());

        ledgerMapper.updateById(ledger);
        return LedgerVO.fromEntity(ledger);
    }
}
