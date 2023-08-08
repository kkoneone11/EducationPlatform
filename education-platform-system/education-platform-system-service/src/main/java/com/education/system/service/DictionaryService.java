package com.education.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.education.system.model.po.Dictionary;

import java.util.List;

/**
 * @Author：kkoneone11
 * @name：DictionaryService
 * @Date：2023/8/8 10:36
 */
public interface DictionaryService extends IService<Dictionary> {

    /**
     * 查询所有数据字典内容
     * @return
     */
    List<Dictionary> queryAll();

    /**
     * 根据code查询数据字典
     * @param code -- String 数据字典Code
     * @return
     */
    Dictionary getByCode(String code);
}
