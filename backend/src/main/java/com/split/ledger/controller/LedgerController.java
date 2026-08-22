package com.split.ledger.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.split.common.web.PageVO;
import com.split.common.web.Result;
import com.split.ledger.dto.LedgerSaveDTO;
import com.split.ledger.service.LedgerService;
import com.split.ledger.vo.LedgerVO;

import lombok.RequiredArgsConstructor;

/**
 * 账本 HTTP 接口
 */
@RestController
@RequestMapping("/api/v1/ledgers")
@RequiredArgsConstructor
public class LedgerController {
    private final LedgerService ledgerService;

    /**
     * 查看账本列表
     * 
     * @param page 页码，默认 1
     * @param size 每页数量，默认 20
     * @return 统一的分页响应体
     */
    @GetMapping
    public Result<PageVO<LedgerVO>> getLedgers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(ledgerService.getLedgers(page, size));
    }

    /**
     * 创建新账本
     *
     * @param dto 前端传入的账本数据
     * @return 包含新生成 ID 的账本信息
     */
    @PostMapping
    public Result<LedgerVO> createLedger(
            @RequestBody @Validated LedgerSaveDTO dto) {
        return Result.success(ledgerService.createLedger(dto));
    }

    /**
     * 获取单个账本详情
     *
     * @param id 路径中的账本 ID
     * @return 账本详细信息
     */
    @GetMapping("/{id}")
    public Result<LedgerVO> getLedger(
            @PathVariable("id") Long id) {
        return Result.success(ledgerService.getLedger(id));
    }

    /**
     * 删除指定账本
     *
     * @param id 路径中的账本 ID
     * @return 空业务数据的成功响应
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteLedger(
            @PathVariable("id") Long id) {
        ledgerService.deleteLedger(id);
        return Result.success();
    }

    /**
     * 修改账本名称
     *
     * @param id  路径中的账本 ID
     * @param dto 前端传入的新账本数据
     * @return 修改后的账本最新信息
     */
    @PatchMapping("/{id}")
    public Result<LedgerVO> updateLedger(
            @PathVariable("id") Long id,
            @RequestBody @Validated LedgerSaveDTO dto) {
        return Result.success(ledgerService.updateLedger(id, dto));
    }
}
