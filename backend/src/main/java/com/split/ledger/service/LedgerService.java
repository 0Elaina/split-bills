package com.split.ledger.service;

import com.split.common.web.PageVO;
import com.split.ledger.dto.LedgerSaveDTO;
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

    /**
     * 创建新账本
     * 
     * @param dto 包含新建数据的请求体
     * @return 带有生成 ID 的账本视图对象
     */
    LedgerVO createLedger(LedgerSaveDTO dto);

    /**
     * 根据 ID 获取账本详情
     * 
     * @param id 账本主键
     * @return 账本视图对象
     */
    LedgerVO getLedger(Long id);

    /**
     * 根据 ID 删除账本
     * 
     * @param id 账本主键
     */
    void deleteLedger(Long id);

    /**
     * 修改账本信息
     * 
     * @param id  账本主键
     * @param dto 包含新名称的请求体
     * @return 修改后的账本视图对象
     */
    LedgerVO updateLedger(Long id, LedgerSaveDTO dto);

}
