package com.education.media.service.service.jobhandler;

import com.education.media.service.config.XxlJobConfig;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @Author：kkoneone11
 * @name：SampleJob
 * @Date：2023/8/22 9:26
 */
@Component
@Slf4j
public class SampleJob {

    /**
     * 1、简单任务示例（Bean模式）
     */
    @XxlJob("testJob")
    public void testJob() throws Exception {
        log.info("开始执行.....");

    }

    @XxlJob("shardingJobHandler")
    public void shardingJobHandler() throws Exception{
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        log.info("分片参数:当前分片序号 = {},总分片数 = {}",shardIndex , shardTotal);
        log.info("开始执行第"+shardIndex+"批任务");
    }
}
