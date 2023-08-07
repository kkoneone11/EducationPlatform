package com.education.base.model.page;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @Author：kkoneone11
 * @name：PageResult
 * @Date：2023/8/4 16:28
 */

/**
 *因为分页查询结果存在固定的数据和格式，因此在base中对结果的返回值也做一个封装
 */
@Data
@ToString
public class PageResult<T> implements Serializable {

    //数据列表
    private List<T> items;
    //总记录数
    private long counts;
    //当前页码
    private long page;
    //每页记录数
    private long pageSize;

    public PageResult(List<T> items, long counts, long page, long pageSize) {
        this.items = items;
        this.counts = counts;
        this.page = page;
        this.pageSize = pageSize;
    }

}
