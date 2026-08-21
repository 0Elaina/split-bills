package com.split.common.web;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分页数据响应包装
 * @param <T> 列表项的数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageVO<T> {
    /**
     * 总记录数
     */
    private long total;

    /**
     * 当前页的数据列表
     */
    private List<T> records;
}
