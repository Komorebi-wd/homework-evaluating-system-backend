package com.example.controller;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.entity.RestBean;
import com.example.entity.dto.Account;
import com.example.entity.dto.StudentCourse;
import com.example.mapper.StudentCourseMapper;
import com.example.mapper.TeacherHomeworkMapper;
import com.example.service.impl.*;
import com.example.util.MarkUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;


@RestController
@RequestMapping("/api/test")
public class TestController {
    @Resource
    AccountServiceImpl accountService;
    @Resource
    TeacherHomeworkServiceImpl teacherHomeworkService;
    @Resource
    CourseServiceImpl courseService;
    @Resource
    StudentCourseServiceImpl studentCourseService;

    @GetMapping("/hello")
    public String index(){
        //返回当前用户UserDetail信息
        return JSONObject.toJSONString(SecurityContextHolder.getContext().getAuthentication().getPrincipal(), JSONWriter.Feature.WriteNulls);
    }

    //添加用户，需管理员权限
    @PreAuthorize("hasRole('admin')")
    @PostMapping("/addUser")
    public String addUser(String uid, String username, String role){
        return accountService.addAccountWithDefaultPassword(uid, username, role);
    }

    //更改密码，（非忘记密码，需旧密码）
    @PostMapping("/changePassword")
    public String changePasswotd(HttpServletRequest httpServletRequest,
                                 String oldPassword, String newPassword){
        return accountService.changePassword((User)SecurityContextHolder.getContext().getAuthentication().getPrincipal(),
                httpServletRequest, oldPassword, newPassword);
    }

    //管理员
    //获得全部课程信息
    @GetMapping("/course/getAll")
    public String getAllCourse(){
       return RestBean.success(courseService.getAllCourse(), "课程查询成功").asJsonString();
    }

    //教师
    //获得指定cid课程下作业（教师作业）
    @GetMapping("/course/homework/getAll")
    public String getHomeworkByCid(int cid){
        return RestBean.success(teacherHomeworkService.getThsByCid(cid), "作业查询成功").asJsonString();
    }

//    @PostMapping("/mult/add")
//    public String addStudentCourse(String sid, int cid){
//        StudentCourse studentCourse = new StudentCourse()
//                .setSid(sid)
//                .setCid(cid);
//        studentCourseService.save(studentCourse);
//        return RestBean.success(studentCourse, "添加成功").asJsonString();
//    }
    @Resource
    StudentServiceImpl studentService;


    @PostMapping("/student/getAll")
    public String getStudentsByCid(int cid){
        return RestBean.success(studentService.getStudentsByCid(cid), "查询成功").asJsonString();
    }

    @PostMapping("/student/course/getAll")
    public String getCoursesBySid(String sid){
        return RestBean.success(courseService.getCoursesBySid(sid), "查询成功").asJsonString();
    }

    @Resource
    MarkUtils markUtils;
    @Resource
    TeacherHomeworkMapper teacherHomeworkMapper;
    @Resource
    StudentCourseMapper studentCourseMapper;

    @GetMapping("/mark/test")
    public String testMarkList(){
        UserDetails userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Account account = accountService.findAccountByNameOrEmail(userDetails.getUsername());
        String commenterId = account.getUid();
        int cid = teacherHomeworkMapper.getThByShId(1).getCid();
        //查询本人SC
        QueryWrapper<StudentCourse> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sid", commenterId)
                .eq("cid", cid);
        StudentCourse studentCourse = studentCourseMapper.selectOne(queryWrapper);
        //查询班级SC
        QueryWrapper<StudentCourse> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.eq("cid", cid);
        List<StudentCourse> studentCourses = studentCourseMapper.selectList(queryWrapper1);
        System.out.println(studentCourses);

        markUtils.addToUnmarkList(studentCourse, 1);
        System.out.println(studentCourse);
        markUtils.addToUnmarkList(studentCourse,18);
        System.out.println(studentCourse);

        System.out.println(markUtils.moveShIdToMarkList(studentCourse, 1));
        System.out.println(studentCourse);
//        markUtils.appendShIdToMarkList(studentCourse, 18);
//        markUtils.removeShIdFromUnmarkList(studentCourse, 18);
//        System.out.println(studentCourse);
        List<StudentCourse> result = markUtils.findAndAddToUnmarkList(9, commenterId, 2, studentCourses);
        for (StudentCourse sc : result) {
            System.out.println(sc);
        }

        return RestBean.success().asJsonString();
    }
}
