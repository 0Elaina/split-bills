package com.split.expense.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.split.common.web.Result;
import com.split.expense.dto.ExpenseSaveDTO;
import com.split.expense.service.ExpenseService;
import com.split.expense.vo.ExpenseListItemVO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 消费明细控制器
 */
@RestController
@RequestMapping("/api/v1/ledgers/{ledgerId}/expenses")
@RequiredArgsConstructor
public class ExpenseController {
    private final ExpenseService expenseService;

    /**
     * 分页查询账本的消费列表
     * 
     * @param ledgerId 账本主键
     * @param current  当前页码
     * @param size     每页数量
     * @return 消费列表分页结果
     */
    @GetMapping
    public Result<Page<ExpenseListItemVO>> getExpenses(
            @PathVariable Long ledgerId,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        return Result.success(expenseService.getExpensesByLedgerId(ledgerId, current, size));
    }

    /**
     * 新增消费明细
     * 
     * @param ledgerId 账本主键
     * @param dto      请求参数
     */
    @PostMapping
    public Result<Void> createExpense(
        @PathVariable Long ledgerId,
        @Valid @RequestBody  ExpenseSaveDTO dto
    ) {
        expenseService.createExpense(ledgerId, dto);
        return Result.success();
    }
}
