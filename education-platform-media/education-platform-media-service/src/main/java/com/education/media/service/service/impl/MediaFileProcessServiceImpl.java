package com.education.media.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.media.model.po.MediaProcess;
import com.education.media.service.mapper.MediaProcessMapper;
import com.education.media.service.service.MediaFileProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author：kkoneone11
 * @name：MediaFileProcessServiceImpl
 * @Date：2023/8/23 0:00
 */
@Service
@Slf4j
public class MediaFileProcessServiceImpl extends ServiceImpl<MediaProcessMapper, MediaProcess> implements MediaFileProcessService {

    @Autowired
    private MediaProcessMapper mediaProcessMapper;

    /**
     * 获取待处理任务
     * @param shardIndex
     * @param shardTotal
     * @param count
     * @return
     */
    @Override
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count) {
        return mediaProcessMapper.selectListByShardIndex(shardTotal,shardIndex,count);
    }

    @Override
    public boolean startTask(long id) {
        int result = mediaProcessMapper.startTask(id);
        return result <=0 ? false : true;

    }
}
