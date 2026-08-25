package com.split.expense.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.split.expense.dto.ExpenseSaveDTO;
import com.split.expense.vo.ExpenseListItemVO;

public interface ExpenseService {
    /**
     * 分页查询指定账本下的消费明细列表
     * 
     * @param ledgerId 账本 ID
     * @param current  页码 (从 1 开始)
     * @param size     每页大小
     * @return 包含付款人和参与人信息的组装后分页结果
     */
    Page<ExpenseListItemVO> getExpensesByLedgerId(Long ledgerId, long current, long size);

    /**
     * 新增记一笔
     * 
     * @param ledgerId 账本 ID
     * @param dto      请求参数
     */
    void createExpense(Long ledgerId, ExpenseSaveDTO dto);

    /**
     * 删除消费及参与人关联
     *
     * @param ledgerId  账本 ID
     * @param expenseId 消费 ID
     */
    void deleteExpense(Long ledgerId, Long expenseId);

    /**
     * 修改消费（全量替换）
     *
     * @param ledgerId  账本 ID
     * @param expenseId 消费 ID
     * @param dto       更新参数（复用新增的 DTO）
     */
    void updateExpense(Long ledgerId, Long expenseId, ExpenseSaveDTO dto);
}
