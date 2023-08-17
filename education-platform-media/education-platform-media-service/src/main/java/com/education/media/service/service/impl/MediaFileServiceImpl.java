package com.education.media.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.education.base.exception.EducationException;
import com.education.base.model.page.PageParams;
import com.education.base.model.page.PageResult;
import com.education.media.model.dto.UploadFileParamsDto;
import com.education.media.model.dto.UploadFileResultDto;
import com.education.media.service.mapper.MediaFilesMapper;
import com.education.media.model.dto.QueryMediaParamsDto;
import com.education.media.model.po.MediaFiles;
import com.education.media.service.service.MediaFileService;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import static jdk.nashorn.internal.runtime.regexp.joni.Config.log;

/**
 * @description TODO
 * @author Mr.M
 * @date 2022/9/10 8:58
 * @version 1.0
 */
@Service
@Slf4j
public class MediaFileServiceImpl implements MediaFileService {

   @Autowired
   private MediaFilesMapper mediaFilesMapper;

   @Autowired
   private MinioClient minioClient;

   @Autowired
   private MediaFileService mediaFileService;

    //普通文件桶
    @Value("${minio.bucket.files}")
    private String bucket_Files;

   @Override
   public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

    //构建查询条件对象
    LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();
  
    //分页对象
    Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
    // 查询数据内容获得结果
    Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
    // 获取数据列表
    List<MediaFiles> list = pageResult.getRecords();
    // 获取数据总数
    long total = pageResult.getTotal();
    // 构建结果集
    PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
    return mediaListResult;

   }

 /**
  *MultipartFile是SpringMVC提供简化上传操作的工具类，不使用框架之前，都是使用原生的HttpServletRequest来接收上传的数据,文件是以二进制流传递到后端的。为了使接口更通用，我们可以用字节数组代替MultpartFile类型
  *
  *
  * 上传文件
  *  * @param companyId 机构id
  *  * @param uploadFileParamsDto 上传文件信息
  *  * @param localFilePath 文件磁盘路径
  * @return com.education.media.model.dto.UploadFileResultDto
  */
 @Override
   public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath) {
      //先根据文件路径看看文件是否存在
      File file = new File(localFilePath);
      if(!file.exists()){
        EducationException.cast("文件不存在");
      }
   //获取文件名称
   String filename = uploadFileParamsDto.getFilename();
   //获取文件扩展名
  String extension = filename.substring(filename.lastIndexOf("."));
  //根据文件扩展名获取mimeType
   String mimeType = getMimeType(extension);
   //根据文件获得文件md5
     String fileMd5 = getFileMd5(file);
     //获取文件的默认目录
     String defaultFolderPath = getDefaultFolderPath();
     //1.存储到minio
     //存储到minio中的对象名(带目录)
     String objectName = defaultFolderPath + fileMd5 + extension;
     boolean b = addMediaFilesToMinIO(localFilePath, mimeType, bucket_Files, objectName);
     uploadFileParamsDto.setFileSize(file.length());
     //2.存储到media_file表
     MediaFiles mediaFiles = mediaFileService.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_Files, objectName);
     //准备返回数据
     UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
     BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);
     return uploadFileResultDto;
   }

    /**
     * 获取mimeType
     * @param extension
     * @return
     */
   private String getMimeType(String extension){
     if(extension == null){
        return "";
     }
     //根据拓展名取出mimeType
    ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
     //通用mimeType字节流
       String mimeType =  MediaType.APPLICATION_OCTET_STREAM_VALUE;
       if(extensionMatch != null){
           mimeType = extensionMatch.getMimeType();
       }
       return mimeType;
   }

    /**
     * 获取md5
     * @param file
     * @return
     */
   private String getFileMd5(File file){
       try(FileInputStream fileInputStream = new FileInputStream(file)){
           String fileMd5 = DigestUtils.md5Hex(fileInputStream);
           return fileMd5;
       }catch (Exception e){
           e.printStackTrace();
           return null;
       }
   }

    /**
     * 获取默认路径
     * @return
     */
   private String getDefaultFolderPath(){
       SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
       return sdf.format(new Date()).replace("-", "/") + "/";
   }

    /**
     * @description 将文件写入minIO
     * @param localFilePath  文件地址
     * @param bucket  桶
     * @param objectName 对象名称
     * @return void
     * @date 2022/10/12 21:22
     */
   private boolean addMediaFilesToMinIO(String localFilePath,String mimeType,String bucket,String objectName){
       try{
           //先创建一个testbucket 然后上传到minio
           UploadObjectArgs testbucket = UploadObjectArgs.builder()
                   .bucket(bucket)
                   .object(objectName)
                   .filename(localFilePath)
                   .contentType(mimeType)
                   .build();
           minioClient.uploadObject(testbucket);
           log.debug("上传文件到minio成功,bucket:{},objectName:{}",bucket,objectName);
           System.out.println("上传成功");
           return true;
       }catch (Exception e){
           e.printStackTrace();
           log.error("上传文件到minio出错,bucket:{},objectName:{},错误原因:{}",bucket,objectName,e.getMessage(),e);
           EducationException.cast("上传文件到文件系统失败");
       }
       return false;
   }

    /**
     * @description 将文件信息添加到文件表
     * @param companyId  机构id
     * @param fileMd5  文件md5值
     * @param uploadFileParamsDto  上传文件的信息
     * @param bucket  桶
     * @param objectName 对象名称
     * @return com.xuecheng.media.model.po.MediaFiles
     * @author Mr.M
     * @date 2022/10/12 21:22
     */
    @Transactional
    public MediaFiles addMediaFilesToDb(Long companyId,String fileMd5,UploadFileParamsDto uploadFileParamsDto,String bucket,String objectName){
        //从数据库查询文件
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles == null) {
            mediaFiles = new MediaFiles();
            //拷贝基本信息
            BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
            mediaFiles.setId(fileMd5);
            mediaFiles.setFileId(fileMd5);
            mediaFiles.setCompanyId(companyId);
            mediaFiles.setUrl("/" + bucket + "/" + objectName);
            mediaFiles.setBucket(bucket);
            mediaFiles.setFilePath(objectName);
            mediaFiles.setCreateDate(LocalDateTime.now());
            mediaFiles.setAuditStatus("002003");
            mediaFiles.setStatus("1");
            //保存文件信息到文件表
            int insert = mediaFilesMapper.insert(mediaFiles);
            if (insert < 0) {
                log.error("保存文件信息到数据库失败,{}",mediaFiles.toString());
                EducationException.cast("保存文件信息失败");
            }
            log.debug("保存文件信息到数据库成功,{}",mediaFiles.toString());

        }
        return mediaFiles;

    }



}
