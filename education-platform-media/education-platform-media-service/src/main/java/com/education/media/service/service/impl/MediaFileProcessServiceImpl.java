package com.education.media.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.media.model.po.MediaFiles;
import com.education.media.model.po.MediaProcess;
import com.education.media.model.po.MediaProcessHistory;
import com.education.media.service.mapper.MediaFilesMapper;
import com.education.media.service.mapper.MediaProcessHistoryMapper;
import com.education.media.service.mapper.MediaProcessMapper;
import com.education.media.service.service.MediaFileProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    @Autowired
    private MediaFilesMapper mediaFilesMapper;

    @Autowired
    private MediaProcessHistoryMapper mediaProcessHistoryMapper;

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

    /**
     *  开启一个任务
     * @param id 任务id
     * @return true开启任务成功，false开启任务失败
     */
    @Override
    public boolean startTask(long id) {
        int result = mediaProcessMapper.startTask(id);
        return result <=0 ? false : true;

    }

    /**
     * @description 保存任务结果
     * @param taskId  任务id
     * @param status 任务状态
     * @param fileId  文件id
     * @param url url
     * @param errorMsg 错误信息
     * @return void
     * @author kkoneone11
     */
    @Override
    public void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg) {
        //先根据taskId查看该任务是否存在
        MediaProcess mediaProcess = mediaProcessMapper.selectById(taskId);
        if(mediaProcess == null){
            return;
        }
        //如果status是3的则表示处理失败 进行更新状态
        LambdaQueryWrapper<MediaProcess> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(MediaProcess::getId,taskId);

        if(status.equals("3")){
            MediaProcess mediaProcessNew = new MediaProcess();
            mediaProcessNew.setStatus(status);
            mediaProcessNew.setFailCount(mediaProcess.getFailCount()+1);
            mediaProcessNew.setErrormsg(errorMsg);
            mediaProcessMapper.update(mediaProcess,lambdaQueryWrapper);
            log.debug("更新任务处理状态为失败，任务信息:{}",mediaProcessNew);
            return;
        }

        //任务处理成功 则更新媒资文件的状态和url  并添加到历史记录表 删除在待处理任务表的记录
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
        if(mediaFiles != null){
            //更新媒资文件中的url
            mediaFiles.setUrl(url);
            mediaFilesMapper.updateById(mediaFiles);
        }
        //处理成功则更新待处理表中的url和状态
        mediaProcess.setUrl(url);
        mediaProcess.setStatus("2");
        mediaProcess.setFinishDate(LocalDateTime.now());
        mediaProcessMapper.updateById(mediaProcess);

        MediaProcessHistory mediaProcessHistory = new MediaProcessHistory();
        BeanUtils.copyProperties(mediaProcess,mediaProcessHistory);
        mediaProcessHistoryMapper.insert(mediaProcessHistory);
        //删除mediaProcess的记录
        mediaProcessMapper.deleteById(mediaProcess.getId());

    }
}
