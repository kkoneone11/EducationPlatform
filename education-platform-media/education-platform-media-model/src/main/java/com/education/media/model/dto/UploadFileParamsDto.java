package com.education.media.model.dto;

import lombok.Data;

/**
 * @Author：kkoneone11
 * @name：UploadFileParamsDto
 * @Date：2023/8/16 14:36
 * Service方法需要提供一个更加通用的保存文件的方法
 */
@Data
public class UploadFileParamsDto {
    /**
     * 文件名称
     */
    private String filename;


    /**
     * 文件类型（文档，音频，视频）
     */
    private String fileType;
    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 标签
     */
    private String tags;

    /**
     * 上传人
     */
    private String username;

    /**
     * 备注
     */
    private String remark;
    /**
     * minio的存储名
     */
    private String objectName;

}
