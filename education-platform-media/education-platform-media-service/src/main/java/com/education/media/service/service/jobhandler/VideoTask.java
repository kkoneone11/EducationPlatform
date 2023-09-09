package com.education.media.service.service.jobhandler;

import com.education.base.utils.Mp4VideoUtil;
import com.education.media.model.po.MediaProcess;
import com.education.media.service.mapper.MediaProcessMapper;
import com.education.media.service.service.MediaFileProcessService;
import com.education.media.service.service.MediaFileService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @Author：kkoneone11
 * @name：VideoTask
 * @Date：2023/8/23 18:57
 */

@Component
@Slf4j
public class VideoTask {

    @Autowired
    private MediaFileProcessService mediaFileProcessService;

    @Autowired
    private MediaFileService mediaFileService;

    @Value("${videoprocess.ffmpegpath}")
    String ffmpegpath;

    //定义一个任务 执行器那边会绑定任务
    @XxlJob("videoJobHandler")
    public void videoJobHandler() throws Exception{
        //分片序号
        int shardIndex = XxlJobHelper.getShardIndex();
        //分片总数
        int shardTotal = XxlJobHelper.getShardTotal();
        //查询待处理任务
        List<MediaProcess> mediaProcessList = null;
        int size = 0;
        try {
            //count传入的是当前cpu可执行的最大线程数
            int processors = Runtime.getRuntime().availableProcessors();
            mediaProcessList = mediaFileProcessService.getMediaProcessList(shardIndex,shardTotal,processors);
            size = mediaProcessList.size();
            log.debug("取出待处理视频任务{}条",size);
            if(size < 0 ){
                return;
            }
        }catch (Exception e){
            e.printStackTrace();
            return;
        }
        //开启任务 开启size个线程的线程池
        ExecutorService threadPool = Executors.newFixedThreadPool(size);
        //计数器
        CountDownLatch countDownLatch = new CountDownLatch(size);
        //将处理任务加入到线程池
        mediaProcessList.forEach(mediaProcess -> {
            threadPool.execute(() -> {
                String status = mediaProcess.getStatus();
                // 避免重复执行任务
                if ("2".equals(status)) {
                    log.debug("该视频已经被处理，无需再次处理。视频信息：{}", mediaProcess);
                    countDownLatch.countDown();
                    return;
                }
                Long taskId = mediaProcess.getId();
                //根据taskId抢占任务
                boolean b = mediaFileProcessService.startTask(taskId);
                if(!b){
                    return;
                }
                log.debug("开始执行任务:{}",mediaProcess);
                //抢占成功后进行文件处理
                String bucket = mediaProcess.getBucket();
                String filename = mediaProcess.getFilename();
                //原视频的md5值
                String fileId = mediaProcess.getFileId();
                //先从minio获取待处理文件
                File originalFile = mediaFileService.downloadFileFromMinIO(bucket,filename);
                if(originalFile == null){
                    log.debug("下载待处理文件失败,originalFile:{}",bucket.concat(filename));
                    //更新待处理的状态
                    mediaFileProcessService.saveProcessFinishStatus(taskId,"3",fileId,null,"下载待处理文件失败");
                    return;
                }
                //处理结束的视频文件
                File mp4File = null;
                try{
                    mp4File = File.createTempFile("mp4",".mp4");
                }catch (Exception e){
                    log.error("创建mp4临时文件失败");
                    mediaFileProcessService.saveProcessFinishStatus(taskId,"3",fileId,null,"创建mp4临时文件失败");
                    e.printStackTrace();
                }
                //处理视频
                String result = "";
                try{
                    Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpegpath, originalFile.getAbsolutePath(), mp4File.getName(), mp4File.getAbsolutePath());
                    // 执行视频转码
                    result = videoUtil.generateMp4();
                }catch (Exception e){
                    e.printStackTrace();
                    log.error("处理视频文件:{},出错:{}", mediaProcess.getFilePath(), e.getMessage());
                }
                if(!result.equals("success")){
                    //记录错误信息
                    log.error("处理视频失败,视频地址:{},错误信息:{}", bucket + mediaProcess.getFilePath(), result);
                    mediaFileProcessService.saveProcessFinishStatus(mediaProcess.getId(), "3", fileId, null, result);
                    return;
                }

                //上传到minio
                //文件名其实就是由md5的前两位和后缀名组成的目录路径
                String objectName = getFilePath(fileId,".mp4");
                //访问url
                String url = "/" + bucket + "/" + objectName;
                //mp4在minio的路径
                try{
                    mediaFileService.addMediaFilesToMinIO(mp4File.getPath(),"video/mp4",bucket,objectName);
                    //将url存储到待处理任务表中并更新状态
                    mediaFileProcessService.saveProcessFinishStatus(taskId,"2",fileId,url,null);
                }catch (Exception e){
                    e.printStackTrace();
                    log.error("上传视频失败或入库失败,视频地址:{},错误信息:{}", bucket + objectName, e.getMessage());
                    //最终还是失败了
                    mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "处理后视频上传或入库失败");
                }finally {
                    countDownLatch.countDown();
                }
            });
        });
        //等待,给一个充裕的超时时间,防止无限等待，到达超时时间还没有处理完成则结束任务
        countDownLatch.await(30, TimeUnit.MINUTES);
    }

    private String getFilePath(String fileMd5,String fileExt){
        return   fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/" +fileMd5 +fileExt;
    }


}
