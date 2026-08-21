package com.split.ledger.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.split.common.web.PageVO;
import com.split.common.web.Result;
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
}
