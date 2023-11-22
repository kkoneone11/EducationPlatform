package com.education.content.service.service.Impl;

import com.alibaba.fastjson.JSON;
import com.education.base.exception.CommonError;
import com.education.base.exception.EducationException;
import com.education.content.model.dto.CourseBaseInfoDto;
import com.education.content.model.dto.CoursePreviewDto;
import com.education.content.model.dto.TeachplanDto;
import com.education.content.model.po.CourseBase;
import com.education.content.model.po.CourseMarket;
import com.education.content.model.po.CoursePublish;
import com.education.content.model.po.CoursePublishPre;
import com.education.content.service.config.MultipartSupportConfig;
import com.education.content.service.feignclient.MediaServiceClient;
import com.education.content.service.feignclient.SearchServiceClient;
import com.education.content.service.mapper.CourseBaseMapper;
import com.education.content.service.mapper.CourseMarketMapper;
import com.education.content.service.mapper.CoursePublishMapper;
import com.education.content.service.mapper.CoursePublishPreMapper;
import com.education.content.service.service.CourseBaseInfoService;
import com.education.content.service.service.CoursePublishPreService;
import com.education.content.service.service.CoursePublishService;
import com.education.content.service.service.TeachplanService;
import com.education.messagesdk.model.po.MqMessage;
import com.education.messagesdk.service.MqMessageService;
import com.education.search.po.CourseIndex;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author：kkoneone11
 * @name：CoursePublishServiceImpl
 * @Date：2023/9/10 22:42
 */

@Service
@Slf4j
public class CoursePublishServiceImpl implements CoursePublishService {

    @Autowired
    CourseBaseInfoService courseBaseInfoService;

    @Autowired
    TeachplanService teachplanService;

    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Autowired
    CourseMarketMapper courseMarketMapper;

    @Autowired
    CoursePublishPreMapper coursePublishPreMapper;

    @Autowired
    CoursePublishMapper coursePublishMapper;

    @Autowired
    MqMessageService mqMessageService;

    @Autowired
    MediaServiceClient mediaServiceClient;

    @Autowired
    SearchServiceClient searchServiceClient;

    @Autowired
    StringRedisTemplate redisTemplate;






    /**
     * 封装成CoursePreviewDto
     * @param courseId 课程id
     * @return
     */
    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {

        //课程基本信息、营销信息
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);

        //课程计划信息
        List<TeachplanDto> teachplanTree= teachplanService.findTeachplanTree(courseId);

        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        coursePreviewDto.setCourseBase(courseBaseInfo);
        coursePreviewDto.setTeachplans(teachplanTree);
        return coursePreviewDto;

    }

    /**
     * 提交课程预发布信息
     * @param companyId
     * @param courseId
     */
    @Override
    public void commitAudit(Long companyId, Long courseId) {
        //查询课程基本信息，拿出条件进行约束校验
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        //课程审核状态
        String auditStatus = courseBase.getAuditStatus();
        if("202003".equals(auditStatus)){
            //已经审核过 不允许再提交
            EducationException.cast("当前为等待审核状态，审核完成可以再次提交。");
        }
        //本机构只允许提交本机构课程
        if(!courseBase.getCompanyId().equals(companyId)){
            EducationException.cast("不允许提交其它机构的课程。");
        }
        //课程图片是否填写
        if(StringUtils.isEmpty(courseBase.getPic())){
            EducationException.cast("提交失败，请上传课程图片");
        }
        //添加课程预发布记录
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        //课程基本信息加部分营销信息
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        BeanUtils.copyProperties(courseBaseInfo,coursePublishPre);
        //课程营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        //转为json
        String courseMarketJson = JSON.toJSONString(courseMarket);
        //将课程营销信息json数据放入课程预发布表
        coursePublishPre.setMarket(courseMarketJson);

        //查询课程计划信息
        List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);
        if(teachplanTree.size()<=0){
            EducationException.cast("提交失败，还没有添加课程计划");
        }
        //转json
        String teachplanTreeString = JSON.toJSONString(teachplanTree);
        coursePublishPre.setTeachplan(teachplanTreeString);

        //设置预发布记录状态,已提交
        coursePublishPre.setStatus("202003");
        //教学机构id
        coursePublishPre.setCompanyId(companyId);
        //提交时间
        coursePublishPre.setCreateDate(LocalDateTime.now());
        CoursePublishPre coursePublishPreUpdate = coursePublishPreMapper.selectById(courseId);
        if(coursePublishPreUpdate == null){
            //添加课程预发布记录
            coursePublishPreMapper.insert(coursePublishPre);
        }else{
            coursePublishPreMapper.updateById(coursePublishPre);
        }

        //更新课程基本表的审核状态 已提交。
        courseBase.setAuditStatus("202003");
        courseBaseMapper.updateById(courseBase);

    }

    @Override
    @Transactional
    public void publish(Long companyId, Long courseId) {
        //约束校验
        //先查课程预发布表看是否有记录检验是否是先审核
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if(coursePublishPre == null){
            EducationException.cast("课程还未进行审核操作");
        }
        //本机构只允许提交本机构课程
        if(!coursePublishPre.getCompanyId().equals(companyId)){
            EducationException.cast("不允许发布其他机构的课程");
        }
        //查看课程审核状态 看是否审核通过
        String status = coursePublishPre.getStatus();
        if("202004".equals(status)){
            EducationException.cast("课程审核通过才可发布");
        }

        //保存课程发布信息
        saveCoursePublish(courseId);

        //保存消息表
        saveCoursePublishMessage(courseId);

        //删除课程预发布表对应记录
        coursePublishPreMapper.deleteById(courseId);
    }


    /**
     * 保存课程发布信息
     * @param courseId
     */
    private void saveCoursePublish(Long courseId) {
        //查询预发布表
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if(coursePublishPre == null){
            EducationException.cast("课程预发布数据为空");
        }
        CoursePublish coursePublish = new CoursePublish();
        //预发布表的数据和发布表的数据一致
        BeanUtils.copyProperties(coursePublishPre,coursePublish);
        coursePublish.setStatus("203002");
        //查看课程发布是否存在
        CoursePublish coursePublishUpdate = coursePublishMapper.selectById(courseId);
        if(coursePublishUpdate == null){
            //不存在则插入
            coursePublishMapper.insert(coursePublish);
        }else{
            //存在则更新
            coursePublishMapper.updateById(coursePublish);
        }
        //更新课程基本信息表的发布状态
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setStatus("203002");
        courseBaseMapper.updateById(courseBase);

    }

    /** [{"code":"203001","desc":"未发布"},{"code":"203002","desc":"已发布"},{"code":"203003","desc":"下线"}]
     * 保存消息表记录
     * @param courseId
     */
    private void saveCoursePublishMessage(Long courseId) {
        MqMessage mqMessage = mqMessageService.addMessage("course_publish", String.valueOf(courseId), null, null);
        if(mqMessage==null){
            EducationException.cast(CommonError.UNKOWN_ERROR);
        }
    }

    /**
     * 生成静态文件
     * @param courseId  课程id
     * @return
     */
    @Override
    public File generateCourseHtml(Long courseId) {

        //静态化文件
        File htmlFile  = null;

        try {
            //配置freemarker
            Configuration configuration = new Configuration(Configuration.getVersion());

            //加载模板
            //选指定模板路径,classpath下templates下
            //得到classpath路径
            String classpath = this.getClass().getResource("/").getPath();
            configuration.setDirectoryForTemplateLoading(new File(classpath + "/templates/"));
            //设置字符编码
            configuration.setDefaultEncoding("utf-8");

            //指定模板文件名称
            Template template = configuration.getTemplate("course_template.ftl");

            //准备数据
            CoursePreviewDto coursePreviewInfo = this.getCoursePreviewInfo(courseId);

            Map<String, Object> map = new HashMap<>();
            map.put("model", coursePreviewInfo);

            //静态化
            //参数1：模板，参数2：数据模型
            String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
//            System.out.println(content);
            //将静态化内容输出到文件中
            InputStream inputStream = IOUtils.toInputStream(content);
            //创建静态化文件
            htmlFile = File.createTempFile("course",".html");
            log.debug("课程静态化，生成静态文件:{}",htmlFile.getAbsolutePath());
            //输出流
            FileOutputStream outputStream = new FileOutputStream(htmlFile);
            IOUtils.copy(inputStream, outputStream);
        } catch (Exception e) {
            log.error("课程静态化异常:{}",e.toString());
            EducationException.cast("课程静态化异常");
        }

        return htmlFile;
    }

    @Override
    public void uploadCourseHtml(Long courseId, File file) {
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
        String course = mediaServiceClient.upload(multipartFile , null ,"course/"+courseId+".html");
        if(course==null){
            EducationException.cast("上传静态文件异常");
        }
    }

    @Override
    public Boolean saveCourseIndex(Long courseId) {
        //1.取出课程发布信息
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
        //2.拷贝到courseindex
        CourseIndex courseIndex = new CourseIndex();
        //拷贝内容到index中
        BeanUtils.copyProperties(coursePublish,courseIndex);
        //调用远程api
        Boolean isSuccess = searchServiceClient.add(courseIndex);
        if(!isSuccess){
            EducationException.cast("添加索引失败");
        }
        return true;

    }

    @Override
    public CoursePublish getCoursePublish(Long courseId) {
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
        return coursePublish;
    }

    @Override
    public CoursePublish getCoursePublishCache(Long courseId) {
        //1.先查询redis缓存
        String jsonObj = redisTemplate.opsForValue().get("course:" + courseId);
        if(StringUtils.isNotEmpty(jsonObj)){
            //1.1查询为空值也返回（解决缓存穿透）
            if(jsonObj.equals("null")){
                return null;
            }
            //1.2查询不为空值则String类型转化为coursePublish并返回
            return JSON.parseObject(jsonObj, CoursePublish.class);
        }else { //2.缓存没有则查询数据库
            log.debug("缓存没有 ，开始查询数据库");
            CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
            //2.1如果为空则也存入
            if(coursePublish == null){
                redisTemplate.opsForValue().set("course:"+courseId,"null",30, TimeUnit.SECONDS);
                return null;
            }
            String jsonString = JSON.toJSONString(coursePublish);
            //2.2存入redis
            redisTemplate.opsForValue().set("course:"+courseId,jsonString);
            return coursePublish;
        }





    }
}
