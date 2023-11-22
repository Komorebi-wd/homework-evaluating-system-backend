package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.StudentCourse;
import com.example.entity.dto.StudentHomework;
import com.example.entity.dto.TeacherHomework;
import com.example.mapper.CourseMapper;
import com.example.mapper.StudentCourseMapper;
import com.example.mapper.StudentHomeworkMapper;
import com.example.mapper.TeacherHomeworkMapper;
import com.example.service.TeacherHomeworkService;
import com.example.util.NewFileUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeacherHomeworkServiceImpl extends ServiceImpl<TeacherHomeworkMapper, TeacherHomework> implements TeacherHomeworkService{
    @Resource
    NewFileUtil newFileUtil;
    @Resource
    CourseServiceImpl courseService;
    @Resource
    CourseMapper courseMapper;

    @Resource
    StudentCourseMapper studentCourseMapper;
    @Resource
    TeacherHomeworkMapper teacherHomeworkMapper;
    @Resource
    StudentHomeworkMapper studentHomeworkMapper;

    public List<TeacherHomework> findUnsubmittedThIds(String sid) {
        // sid所选全部cids
        List<Integer> cids = studentCourseMapper.selectList(
                        new QueryWrapper<StudentCourse>().eq("sid", sid))
                .stream()
                .map(StudentCourse::getCid)
                .collect(Collectors.toList());

        // cids所对应全部应提交thIds
        List<Integer> allThIds = teacherHomeworkMapper.selectList(
                        new QueryWrapper<TeacherHomework>().in("cid", cids))
                .stream()
                .map(TeacherHomework::getThId)
                .toList();

        //sid所对应全部已提交thIds
        List<Integer> submittedThIds = studentHomeworkMapper.selectList(
                        new QueryWrapper<StudentHomework>().eq("sid", sid))
                .stream()
                .map(StudentHomework::getThId)
                .toList();

        //做差集，获得应提交但为提交thIds
        List<Integer> thIds  = allThIds.stream()
                                    .filter(thId -> !submittedThIds.contains(thId))
                                    .collect(Collectors.toList());

        List<TeacherHomework> teacherHomeworks = teacherHomeworkMapper.selectBatchIds(thIds);
        for ( TeacherHomework teacherHomework : teacherHomeworks) {
            teacherHomework.setCname(courseMapper.getCnameByCid(teacherHomework.getCid()));
        }
        return teacherHomeworks;
    }

    //默认从现在起一周时间
    //传入thId表示第n次作业
    @Transactional
    public Boolean uploadTeacherHomework(MultipartFile multipartFile, int cid, int thId, Date endDate, String comment) throws IOException, SQLException {
       // Blob blob = new SerialBlob(multipartFile.getBytes());
        // 创建一个 Calendar 对象，并设置为开始时间
        Date startDate = new Date();
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(startDate);
//        // 在开始时间上添加一周的时间（7天）
//        calendar.add(Calendar.DAY_OF_YEAR, 7);

        thId = cid*10 + thId;//作业号设为课程号*10+第n此作业

        TeacherHomework teacherHomework = new TeacherHomework()
                .setThId(thId)
                .setTid(courseService.getTidByCid(cid))
                .setCid(cid)
                .setFileName(multipartFile.getOriginalFilename().substring(0,multipartFile.getOriginalFilename().lastIndexOf(".")))
                .setFileType(newFileUtil.getExtension(multipartFile))
                .setFileSize(String.valueOf(multipartFile.getSize()))
                .setFileData(multipartFile.getBytes())
                .setStartTime(startDate)
                .setEndTime(endDate)
                .setComment(comment);

        return this.saveOrUpdate(teacherHomework);
    }

    //下载teacher布置的作业
    //传入作业号
    public boolean downloadThHomework(int thId, HttpServletResponse httpServletResponse) throws UnsupportedEncodingException {
        TeacherHomework teacherHomework = this.getById(thId);
        return newFileUtil.downloadFile(teacherHomework.getFileData(), teacherHomework.getFileName(), teacherHomework.getFileType(),httpServletResponse);
    }

    //查询该课程下布置的作业
    public List<TeacherHomework> getThsByCid(int cid){
        QueryWrapper<TeacherHomework> queryWrapper = new QueryWrapper<TeacherHomework>();
        queryWrapper.eq("cid", cid);
        return this.list(queryWrapper);
    }

}
