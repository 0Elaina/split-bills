package com.split.ledger.service;

import com.split.common.web.PageVO;
import com.split.ledger.vo.LedgerVO;

/**
 * 账本业务逻辑接口
 */
public interface LedgerService {
    /**
     * 分页查询账本列表
     *
     * @param page 当前页码，从 1 开始
     * @param size 每页数量
     * @return 包含 LedgerVO 的分页包装数据
     */
    PageVO<LedgerVO> getLedgers(int page, int size);

}
