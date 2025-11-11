package com.lyh.codefather.common;

import lombok.Data;

/**
 * @author <a href=https://github.com/fearlesslyh> 梁懿豪 </a>
 * @version 1.0
 * @since 2025/11/10 23:02
 */
@Data
public class PageRequest {
    /**
     * 当前页
     */
    private int pageNum = 1;
    /**
     * 每页显示条数
     */
    private int pageSize = 10;
    /**
     * 排序字段
     */
    private String sortField;
    /**
     * 排序方式
     */
    private String sortOrder = "descend";
}
