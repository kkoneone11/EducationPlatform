package com.education.media.api;

import com.education.base.exception.EducationException;
import com.education.base.model.RestResponse;
import com.education.media.model.po.MediaFiles;
import com.education.media.service.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author：kkoneone11
 * @name：MediaOpenController
 * @Date：2023/9/11 18:56
 */
@Api(value = "媒资文件管理接口" , tags = "媒资文件管理接口")
@RestController
@RequestMapping("/open")
public class MediaOpenController {

    @Autowired
    private MediaFileService mediaFileService;

    /**
     * 媒资管理
     * @param mediaId
     * @return 视频地址的url
     */
    @ApiOperation("预览文件")
    @GetMapping("/preview/{mediaId}")
    public RestResponse<String> getPlayUriByMediaId(@PathVariable("mediaId") String mediaId){
        MediaFiles mediaFiles = mediaFileService.getFileById(mediaId);
        if(mediaFiles == null || StringUtils.isEmpty(mediaFiles.getUrl())){
            EducationException.cast("视频还没有转码处理");
        }
        return RestResponse.success(mediaFiles.getUrl());
    }
}
