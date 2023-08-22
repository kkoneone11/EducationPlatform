package com.education.media.service.service;

import com.education.media.model.po.MediaProcess;

import java.util.List;

/**
 * @Author：kkoneone11
 * @name：MediaFileProcessService
 * @Date：2023/8/22 23:58
 */
public interface MediaFileProcessService {

    public List<MediaProcess> getMediaProcessList(int shardIndex,int shardTotal,int count);
}
