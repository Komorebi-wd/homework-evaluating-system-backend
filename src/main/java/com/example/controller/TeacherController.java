package com.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.entity.RestBean;
import com.example.entity.dto.Account;
import com.example.entity.dto.Course;
import com.example.entity.dto.Student;
import com.example.entity.dto.StudentHomework;
import com.example.mapper.CourseMapper;
import com.example.service.AccountService;
import com.example.service.impl.StudentHomeworkServiceImpl;
import com.example.service.impl.StudentServiceImpl;
import com.example.service.impl.TeacherHomeworkServiceImpl;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/teacher")
public class TeacherController {
    /*
    * 1: getAllCoursesByTid 获得当前tid(UserDetail)下全部course
    * 2: getAllStudentsByCid 获得指定Cid下全部学生
    * 3: putThWithCidEndDateComment 为指定Cid添加Th作业, 同时指定截止日期
    * 4: getAllShWithCidThId 获得指定Cid、ThId下全部学生作业Sh(简略信息，而非文件本身）
    * 5: downloadShsWithCidSidThId 获得指定Cid、ThId下指定sid学生的作业Sh（下载文件, 多个文件打包为zip）
    * */
    @Resource
    CourseMapper courseMapper;
    @Resource
    AccountService accountService;
    @Resource
    StudentServiceImpl studentService;
    @Resource
    TeacherHomeworkServiceImpl teacherHomeworkService;
    @Resource
    StudentHomeworkServiceImpl studentHomeworkService;

    @GetMapping("/course/{cid}/tHomework/{thId}/sHomework/student/{sid}/download")//thId是第x次作业
    public String downloadShsWithCidSidThId(@PathVariable int cid, @PathVariable String sid, @PathVariable int thId, HttpServletResponse response) throws UnsupportedEncodingException {
        thId = cid*10 + thId;//真正的thId
        List<StudentHomework> studentHomeworks = studentHomeworkService.getStudentHomeworksByThIdSid(thId, sid);
        if (studentHomeworkService.downloadStudentHomeworks(studentHomeworks, response)){
            return RestBean.success("下载全部作业成功，当前sid："+sid+", thId: "+thId).asJsonString();
        } else return RestBean.failure(999,"下载失败").asJsonString();
    }

    @GetMapping("/course/{cid}/tHomework/{thId}/sHomework/getAll")
    @PreAuthorize("hasRole('teacher')")//thId表示第x次作业
    public String getAllShWithCidThId(@PathVariable int cid, @PathVariable int thId){
        thId = cid*10 + thId;//真正的thId
        List<StudentHomework> studentHomeworks = studentHomeworkService.getStudentHomeworksByThId(thId);
        return RestBean.success(studentHomeworks, "成功查询该课程下全部学生作业，当前thId: " + thId).asJsonString();
    }

    @PostMapping("/tHomework/upload")//thId是第x次作业
    @PreAuthorize("hasRole('teacher')")
    public String putThWithCidEndDateComment(int thId, int cid, Date endDate, String comment, MultipartFile multipartFile) throws SQLException, IOException {
        if(teacherHomeworkService.uploadTeacherHomework(multipartFile, cid, thId, endDate, comment)){
            return RestBean.success("cid: " + cid + "thId: " + (thId+cid*10)).asJsonString();
        } else return RestBean.failure(999, "添加失败：'cid: " + cid + "thId: " + (thId+cid*10)+"'").asJsonString();
    }

    @GetMapping("/course/student/getAll/{cid}")
    @PreAuthorize("hasRole('teacher')")
    public String getAllStudentsByCid(@PathVariable int cid){
        List<Student> students = studentService.getStudentsByCid(cid);
        return RestBean.success(students, "查询课程下全部学生成功，当前课程cid：" + cid).asJsonString();
    }

    @GetMapping("/course/getAll")
    @PreAuthorize("hasRole('teacher')")
    public String getAllCoursesByTid(){
        UserDetails userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Account account = accountService.findAccountByNameOrEmail(userDetails.getUsername());
        String tid = account.getUid();

        QueryWrapper<Course> courseQueryWrapper = new QueryWrapper<>();
        courseQueryWrapper.eq("tid", tid);
        List<Course> courseList = courseMapper.selectList(courseQueryWrapper);

        return RestBean.success(courseList, "课程列表查询成功，当前教师："+tid).asJsonString();
    }
}
