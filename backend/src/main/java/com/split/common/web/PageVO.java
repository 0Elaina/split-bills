package com.split.common.web;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分页数据响应包装
 * 
 * @param <T> 列表项的数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageVO<T> {
    private List<T> items; // 当前页数据列表
    private long page; // 当前页码
    private long size; // 当前页容量
    private long totalElements; // 总记录数
    private long totalPages; // 总页数
}
