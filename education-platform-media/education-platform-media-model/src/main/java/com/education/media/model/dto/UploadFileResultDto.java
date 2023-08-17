package com.education.media.model.dto;

import com.education.media.model.po.MediaFiles;
import lombok.Data;

/**
 * @Author：kkoneone11
 * @name：UploadFileResultDto
 * @Date：2023/8/16 12:32
 * 上传普通文件成功响应结果
 */

/**
 * 虽然响应结果与MediaFiles表中的字段完全一致，但最好不要直接用MediaFiles类。
 * 因为该类属于PO类，如果后期我们要对响应结果进行修改，那么模型类也需要进行修改，但是MediaFiles是PO类，我们不能动。
 * 所以可以直接用一个类继承MediaFiles，里面什么属性都不用加
 */
@Data
public class UploadFileResultDto extends MediaFiles {
}
