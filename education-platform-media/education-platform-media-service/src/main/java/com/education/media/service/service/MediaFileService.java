package com.education.media.service.service;


import com.education.base.model.RestResponse;
import com.education.base.model.page.PageParams;
import com.education.base.model.page.PageResult;
import com.education.media.model.dto.QueryMediaParamsDto;
import com.education.media.model.dto.UploadFileParamsDto;
import com.education.media.model.dto.UploadFileResultDto;
import com.education.media.model.po.MediaFiles;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @description 媒资文件管理业务类
 * @author Mr.M
 * @date 2022/9/10 8:55
 * @version 1.0
 */
public interface MediaFileService {

 /**
  * @description 媒资文件查询方法
  * @param pageParams 分页参数
  * @param queryMediaParamsDto 查询条件
  * @return com.xuecheng.base.model.PageResult<com.xuecheng.media.model.po.MediaFiles>
  * @author Mr.M
  * @date 2022/9/10 8:57
 */
 PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);

 UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath);

 MediaFiles addMediaFilesToDb(Long companyId,String fileMd5,UploadFileParamsDto uploadFileParamsDto,String bucket,String objectName);

 RestResponse<Boolean> checkfile(String fileMd5);

 RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex);

 RestResponse uploadChunk(String fileMd5,int chunk,String localChunkFilePath);

 RestResponse mergechunks(Long companyId,String fileMd5,int chunkTotal,UploadFileParamsDto uploadFileParamsDto);
}
