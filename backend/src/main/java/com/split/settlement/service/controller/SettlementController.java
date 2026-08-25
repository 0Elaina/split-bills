package com.split.settlement.service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.split.common.web.Result;
import com.split.settlement.service.SettlementService;
import com.split.settlement.vo.SettlementVO;

import lombok.RequiredArgsConstructor;

/**
 * 结算控制器
 */
@RestController
@RequestMapping("/api/v1/ledgers")
@RequiredArgsConstructor
public class SettlementController {
    private final SettlementService settlementService;


    /**
     * 获取指定账本的结算与转账建议
     * 
     * @param ledgerId 账本ID
     * @return 结算与转账建议VO
     */
    @GetMapping("/{ledgerId}/settlement")
    public Result<SettlementVO> getSettlement(@PathVariable Long ledgerId) {
        return Result.success(settlementService.calculateSettlement(ledgerId));
    }
}
