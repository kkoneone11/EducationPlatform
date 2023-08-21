package com.education.media.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.base.exception.EducationException;
import com.education.base.model.RestResponse;
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
import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
@Slf4j
public class MediaFileServiceImpl extends ServiceImpl<MediaFilesMapper,MediaFiles> implements MediaFileService {

   @Autowired
   private MediaFilesMapper mediaFilesMapper;

   @Autowired
   private MinioClient minioClient;

   @Autowired
   private MediaFileService mediaFileService;

    //普通文件桶
    @Value("${minio.bucket.files}")
    private String bucket_Files;

    //视频文件桶
    @Value("${minio.bucket.videofiles}")
    private String bucket_video;

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
     //获取minio中文件的默认目录
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
        if(extension==null)
            extension = "";
        //根据扩展名取出mimeType
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
        //通用mimeType字节流 返回未知的扩展名
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        if(extensionMatch!=null){
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
     * 根据当前时间在minio中bucket创建一个目录
     * @return
     */
   private String getDefaultFolderPath(){
       SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
       return sdf.format(new Date()).replace("-", "/") + "/";
   }

    /**
     * @description 将文件/视频等类型写入minIO
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


    /**
     * @description 检查文件是否存在
     * @param fileMd5 文件的md5
     * @return com.education.base.model.RestResponse<java.lang.Boolean> false不存在，true存在
     * @authorkkoneone11
     * @date 2022/9/13 15:38
     */
    @Override
    public RestResponse<Boolean> checkfile(String fileMd5) {
        //先用mapper查询一下是否入库
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if(mediaFiles!=null){
            //根据入库信息再去minio中查询是否入库
            String bucket = mediaFiles.getBucket();
            String filePath = mediaFiles.getFilePath();

            InputStream stream = null;
            try {
                stream = minioClient.getObject(
                        GetObjectArgs.builder()
                                .bucket(bucket)
                                .object(filePath)
                                .build()
                );
                if(stream != null){
                    //文件存在
                    return RestResponse.success(true);
                }
            } catch (Exception e){
                e.printStackTrace();
                return RestResponse.success(false);
            }
        }
        //文件不存在
        return RestResponse.success(false);
    }

    /**
     * @description 判断minio对应的目录下是否存在分块文件
     * @param fileMd5  文件的md5
     * @param chunkIndex  分块序号
     * @return com.education.base.model.RestResponse<java.lang.Boolean> false不存在，true存在
     * @author kkoneone11
     * @date 2022/9/13 15:39
     */
    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {
        //先根据fileMd5获取文件的目录
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        //再根据chunkIndex拼接分块文件的路径
        String chunkFilePath = chunkFileFolderPath + chunkIndex;
        //根据文件流查看分块是否存在
        InputStream stream = null;
        try {
            stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket_video)
                    .object(chunkFilePath)
                    .build());
            //文件存在
            if(stream != null){
                return RestResponse.success(true);
            }
            //文件不存在
        }catch (Exception e){
            e.printStackTrace();
            return RestResponse.success(false);
        }
        return RestResponse.success(false);
    }

    /**
     * 根据fileMd5获取文件的目录
     * @param fileMd5
     * @return
     */
    private String getChunkFileFolderPath(String fileMd5){
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + "chunk" + "/";
    }

    /**
     * @param fileMd5 文件md5
     * @param chunk   分块序号
     * @param localChunkFilePath  分块文件本地路径
     * @return com.education.base.model.RestResponse
     * @description 上传分块
     */
    @Override
    public RestResponse uploadChunk(String fileMd5, int chunk, String localChunkFilePath) {
        //先获取分块的文件的目录
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        //再组装分块在minio应该上传的位置
        String chunkFilePath = chunkFileFolderPath + chunk;
        //分块没有扩展名
        String mimeType = getMimeType(null);

        //从本地上传到minio
        boolean b = addMediaFilesToMinIO(localChunkFilePath, mimeType, bucket_video, chunkFilePath);

        if(!b){
            log.debug("上传分块文件失败:{}", chunkFilePath);
            return RestResponse.validfail(false, "上传分块失败");
        }
        log.debug("上传分块文件成功:{}",chunkFilePath);
        return RestResponse.success(true);
    }

    @Override
    public RestResponse mergechunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {
        //1.获取分块
        //先根据md5获取分块上传的位置
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        //将分块以List的形式取出  根据chunkTotal一个个取出分块 然后利用ComposeSource将每个分块从minio取出
        List<ComposeSource> sourceObjectList = Stream.iterate(0, i -> ++i)
                .limit(chunkTotal)
                .map(i -> ComposeSource.builder()
                        .bucket(bucket_video)
                        .object(chunkFileFolderPath.concat(Integer.toString(i)))
                        .build())
                .collect(Collectors.toList());
        //2.合并分块
        //文件名称
        String filename = uploadFileParamsDto.getFilename();
        //文件拓展名
        String extName = filename.substring(filename.lastIndexOf("."));
        //合并文件的路径
        String mergeFilePath = getFilePathByMd5(fileMd5, extName);
        //合并
        try{
            ObjectWriteResponse response = minioClient.composeObject(ComposeObjectArgs.builder()
                    .bucket(bucket_video)
                    .object(mergeFilePath)
                    .sources(sourceObjectList)
                    .build());
            log.debug("合并文件成功{}:",mergeFilePath);
        }catch (Exception e){
            e.printStackTrace();
            log.debug("合并文件失败，,fileMd5:{},异常",fileMd5,e.getMessage(),e);
            return RestResponse.validfail(false, "合并文件失败。");
        }

        //3.验证md5判断文件是否相同 因为有可能网络丢包导致文件合并有误
        //先从minio下载合并后的文件
        File minioFile = downloadFileFromMinIO(bucket_video, mergeFilePath);
        if(minioFile == null){
            log.debug("下载合并后文件失败,mergeFilePath:{}",mergeFilePath);
            return RestResponse.validfail(false,"下载合并后文件失败。");
        }
        //计算从minio下载的合并文件的md5
        try(InputStream newFileInputStream = new FileInputStream(minioFile)){
            String md5Hex = DigestUtils.md5Hex(newFileInputStream);
            //比较md5
            if(!fileMd5.equals(md5Hex)){
                return RestResponse.validfail(false,"文件合并校验失败，上传失败");
            }
            //为入库做准备
            uploadFileParamsDto.setFileSize(minioFile.length());
        }catch (Exception e){
            e.printStackTrace();
            return RestResponse.validfail(false,"文件合并校验失败，上传失败");
        }finally {
            //验证完后删除 方便临时文件下次使用
            if(minioFile != null){
                minioFile.delete();
            }
        }
        //4.文件入库
        mediaFileService.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_video, mergeFilePath);
        //5.删除文件
        clearChunkFiles(chunkFileFolderPath,chunkTotal);
        return RestResponse.success(true);
    }

    /**
     * 得到合并后的文件的地址
     * @param fileMd5 文件id即md5值
     * @param fileExt 文件扩展名
     * @return
     */
    private String getFilePathByMd5(String fileMd5,String fileExt){
        return   fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/" +fileMd5 +fileExt;
    }


    /**
     * 从minio下载文件
     * @param bucket 桶
     * @param objectName 对象名称
     * @return 下载后的文件
     */
    private File downloadFileFromMinIO(String bucket,String objectName){

        //先创建一个临时文件和输出流
        File minioFile = null;
        FileOutputStream outputStream = null;
        //然后将下载的文件用输入流处理
        try{
            InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
            //创建临时文件
            minioFile = File.createTempFile("minio",".merge");
            //根据临时文件创建一个输出流
            outputStream = new FileOutputStream(minioFile);
            //写入到输出流中
            IOUtils.copy(stream,outputStream);
            return minioFile;
        }catch (Exception e){
            e.printStackTrace();
            log.debug("文件写入失败");
        }finally {
            if(outputStream!=null){
                try {
                    outputStream.close();
                }catch (Exception e){
                    e.printStackTrace();
                    log.debug("文件流关闭失败");
                }
            }
        }
        return null;
    }

    /**
     * 清除分块
     * @param chunkFileFolderPath
     * @param chunkTotal
     */
    private void clearChunkFiles(String chunkFileFolderPath , int chunkTotal){
        //先将目标分块一个个组合起来
        List<DeleteObject> deleteObjects = Stream.iterate(0, i -> ++i)
                .limit(chunkTotal)
                .map(i -> new DeleteObject(chunkFileFolderPath.concat(Integer.toString(i))))
                .collect(Collectors.toList());
        //利用minio的RemoveObjectArgs进行删除目标，并用Iterable收集每个结果
        Iterable<Result<DeleteError>> results = minioClient.removeObjects(RemoveObjectsArgs.builder()
                .bucket("video")
                .objects(deleteObjects)
                .build());
        //遍历结果集看是否有删除错误
        results.forEach(r -> {
            DeleteError deleteError = null;
            try{
                deleteError = r.get();
            }catch (Exception e){
                e.printStackTrace();
                log.error("清楚分块文件失败,objectname:{}",deleteError.objectName(),e);
            }
        });
    }
}
