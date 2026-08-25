package com.split.settlement.service;

import com.split.settlement.vo.SettlementVO;

public interface SettlementService {
    /**
     * 计算并生成指定账本的实时结算结果
     * 
     * @param ledgerId 账本 ID
     * @return 包含所有人余额及转账建议的汇总 VO
     */
    SettlementVO calculateSettlement(Long ledgerId);
}
