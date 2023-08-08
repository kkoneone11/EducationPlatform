package com.education.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.system.mapper.DictionaryMapper;
import com.education.system.model.po.Dictionary;
import com.education.system.service.DictionaryService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author：kkoneone11
 * @name：DictionaryServiceImpl
 * @Date：2023/8/8 10:42
 */
@Service
public class DictionaryServiceImpl extends ServiceImpl<DictionaryMapper, Dictionary> implements DictionaryService {
    @Override
    public List<Dictionary> queryAll() {

        List<Dictionary> list = this.list();


        return list;
    }

    @Override
    public Dictionary getByCode(String code) {


        LambdaQueryWrapper<Dictionary> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Dictionary::getCode, code);

        Dictionary dictionary = this.getOne(queryWrapper);


        return dictionary;
    }

}
