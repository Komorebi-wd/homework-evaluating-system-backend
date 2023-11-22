package com.example.controller;

import com.example.entity.RestBean;
import com.example.entity.dto.Account;
import com.example.service.impl.AccountServiceImpl;
import com.example.service.impl.StudentHomeworkServiceImpl;
import com.example.service.impl.TeacherHomeworkServiceImpl;
import com.example.util.FileUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;

@RestController
public class FileUploaderController {

//    @Resource
//    FileUtils fileUtils;
//    @Resource
//    TeacherHomeworkServiceImpl teacherHomeworkService;
//    @Resource
//    StudentHomeworkServiceImpl studentHomeworkService;
//    @Resource
//    AccountServiceImpl accountService;
//
//    @PostMapping("/student/homework/upload1")
//    public String singleFileUpload(@RequestParam("file") MultipartFile file) throws Exception {
//
//        if (file.isEmpty()) {
//            throw new Exception("文件为空!");
//        }
//        String filename = fileUtils.upload(file);
//        String url = "/upload/" + filename;
//        return RestBean.success("上传成功, url: " +url).asJsonString();
//    }
//
////    @PreAuthorize("hasRole('teacher')")
////    @PostMapping("/teacher/homework/upload")//thId是第x次作业
////    public String upload(int thId, int cid, MultipartFile multipartFile) throws SQLException, IOException {
////       if(teacherHomeworkService.uploadTeacherHomework(multipartFile, cid, thId)){
////           return RestBean.success("cid: " + cid + "thId: " + (thId+cid*10)).asJsonString();
////       } else return RestBean.failure(999, "添加失败：'cid: " + cid + "thId: " + (thId+cid*10)+"'").asJsonString();
////    }
//
//    @PostMapping("/student/homework/download")//thId是第x次作业
//    public String download(int cid, int thId, HttpServletResponse response) throws UnsupportedEncodingException {
//        thId = cid*10 + thId;
//        if (teacherHomeworkService.downloadThHomework(thId, response))
//            return RestBean.success("成功下载").asJsonString();
//        else return RestBean.failure(999,"下载失败").asJsonString();
//    }
//
//    //@PreAuthorize("hasRole('student')")
//    @PostMapping("/student/homework/submit")//thId是第x次作业
//    public String submit(int cid, int thId, MultipartFile multipartFile) throws SQLException, IOException {
//        thId = cid*10+thId;//真正thId
//
//        UserDetails userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        Account account = accountService.findAccountByNameOrEmail(userDetails.getUsername());
//        String sid = account.getUid();
//
//        return studentHomeworkService.submitStudentHomework(multipartFile, sid, thId);
//    }
}
