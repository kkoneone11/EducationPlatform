package com.education.base.model.page;

/**
 * @Author：kkoneone11
 * @name：PageParams
 * @Date：2023/8/4 15:59
 */

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

/**
 * 要用到的分页查询接口比较多因此单独分离出来放在base工程
 */
@Data
@ToString
public class PageParams {

    //当前页码
    @ApiModelProperty("当前页码")
    private Long pageNo = 1L;
    //当前页的加载的内容条数
    @ApiModelProperty("每页记录数默认值")
    private Long pageSize = 10L;

    public PageParams(){

    }
    public PageParams(Long pageNo , Long pageSize){
        this.pageNo = pageNo;
        this.pageSize = pageNo;
    }
}
