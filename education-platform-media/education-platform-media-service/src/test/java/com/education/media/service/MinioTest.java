package com.education.media.service;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.UploadObjectArgs;
import org.apache.commons.compress.utils.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterInputStream;

/**
 * @Author：kkoneone11
 * @name：MinioTest
 * @Date：2023/8/16 9:36
 */

@SpringBootTest
@ContextConfiguration(classes = MinioTest.class)
public class MinioTest {
    static MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://192.168.101.65:9000")
                    .credentials("minioadmin", "minioadmin")
                    .build();

    //上传文件
    @Test
    public void upload() {
        try {
            UploadObjectArgs testbucket = UploadObjectArgs.builder()
                    .bucket("testbucket")
//                    .object("test001.mp4")
                    .object("001/test1.jpg")//添加子目录 同一个桶内对象名不能重复
                    .filename("D:\\develop\\upload\\test1.jpg")
//                    .contentType("video/mp4")//默认根据扩展名确定文件内容类型，也可以指定
                    .build();
            minioClient.uploadObject(testbucket);
            System.out.println("上传成功");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("上传失败");
        }
    }

    @Test
    public void delete(){
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder().bucket("testbucket").object("001/test1.jpg").build());
            System.out.println("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("删除失败");
        }
    }

    //查询文件
    @Test
    public void getFile() {
        GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket("testbucket").object("001/test1.jpg").build();
        try(
                FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
                FileOutputStream outputStream = new FileOutputStream(new File("D:\\develop\\upload\\test2.jpg"));
        ) {
            IOUtils.copy(inputStream,outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    //将分块文件上传至minio
    @Test
    public void uploadChunk(){
        String chunkFolderPath = "D:\\develop\\upload\\chunk\\";
        File chunkFolder = new File(chunkFolderPath);
        //分块文件
        File[] files = chunkFolder.listFiles();
        //将分块文件上传至minio
        for (int i = 0; i < files.length; i++) {
            try {
                UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder().bucket("testbucket").object("chunk/" + i).filename(files[i].getAbsolutePath()).build();
                minioClient.uploadObject(uploadObjectArgs);
                System.out.println("上传分块成功"+i);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
