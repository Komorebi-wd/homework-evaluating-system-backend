package com.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.entity.RestBean;
import com.example.entity.dto.*;
import com.example.entity.vo.TotalScoreVO;
import com.example.mapper.*;
import com.example.service.AccountService;
import com.example.service.impl.*;
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
     * 3: putThWithCid 为指定Cid添加Th作业
     * 4: getAllShWithCidThId 获得指定Cid、ThId下全部学生作业Sh(简略信息，而非文件本身）
     * 5: downloadShsWithCidSidThId 获得指定Cid、ThId下指定sid学生的作业Sh（下载文件, 多个文件打包为zip）
     * 6: getThsWithTidCid 获得当前tid老师在cid课程布置的所有作业ths
     * */
    /*成绩相关
     * 1：getAvgScoreMarkWithSidThId 获得指定sid学生、指定thId教师布置作业下所获得的成绩(多次提交/被批改取平均值）
     * 2：getAvgTotalScoresWithCidTid 获得指定cid课程下全部学生的总成绩(总成绩是教师布置全部作业最终平均值)
     * 3. changeScoreWithCidThIdSid 老师更改某学生某次作业分数
     * 4. addScoreForTopNStudentWithCidAddScoreN 为cid课程下批改作业最多的前n个人提高addScore分数（注意若原本为null则无法提高，且未设置最大值100）
     */
    /*相似度检验
     * 1：getSimilarHomeworksWithCidThId 获得指定cid课程第thId次作业下每个学生的相似度情况（封装在SimilarHomework中返回）
    * */
    /*申诉相关
     * 1. getNSuggestionWithCid 查询cid课堂中未回复的申诉
     * 2. getYSuggestionWithCid 查询cid课堂中已回复的申诉
     * 3. answerSuggestionWithSuggestionIdAnswer 回复学生的申诉*/
    @Resource
    CourseMapper courseMapper;
    @Resource
    AccountService accountService;
    @Resource
    StudentServiceImpl studentService;
    @Resource
    TeacherHomeworkServiceImpl teacherHomeworkService;
    @Resource
    TeacherHomeworkMapper teacherHomeworkMapper;
    @Resource
    StudentHomeworkServiceImpl studentHomeworkService;
    @Resource
    MarkServiceImpl markService;
    @Resource
    StudentCourseMapper studentCourseMapper;
    @Resource
    StudentCourseServiceImpl studentCourseService;
    @Resource
    StudentMapper studentMapper;
    @Resource
    SuggestionMapper suggestionMapper;

    @PostMapping("/course/score/addScore")
    public String addScoreForTopNStudentWithCidAddScoreN(int cid, double addScore, int n){
        UserDetails userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Account account = accountService.findAccountByNameOrEmail(userDetails.getUsername());
        String tid = account.getUid();
        System.out.println(n);
        List<String> sids = studentCourseService.getTopSidsByMarkCount(cid, n);
        System.out.println(sids);
        String message = studentHomeworkService.addScoreWithTidCidSidsAddScore(tid, cid, sids, addScore);
        return RestBean.success(message).asJsonString();
    }

    @PostMapping("/course/suggestion/answer")
    public String answerSuggestionWithSuggestionIdAnswer(int suggestionId, String answer) {
        Suggestion suggestion = suggestionMapper.selectById(suggestionId);
        suggestion.setAnswer(answer);
        suggestion.setStatus("Y");
        suggestionMapper.updateById(suggestion);
        return RestBean.success(suggestion, "成功回复").asJsonString();
    }

    @GetMapping("/course/{cid}/suggestion/getY")
    public String getYSuggestionWithCid(@PathVariable int cid){
        return RestBean.success(suggestionMapper.getSuggestionsByCidAndStatusY(cid), "成功查询全部已回复申诉").asJsonString();
    }
    @GetMapping("/course/{cid}/suggestion/getN")
    public String getNSuggestionWithCid(@PathVariable int cid){
        return RestBean.success(suggestionMapper.getSuggestionsByCidAndStatusN(cid), "成功查询全部已回复申诉").asJsonString();
    }

    @PostMapping("/course/{cid}/tHomework/{thId}/student/{sid}/changeScore/{newScore}")
    public String changeScoreWithCidThIdSid(@PathVariable int cid, @PathVariable int thId, @PathVariable String sid, @PathVariable double newScore){
        thId = cid*10 + thId;
        markService.updateScoresForShIds(sid, thId, newScore);
        return RestBean.success("成功修改").asJsonString();
    }

    @GetMapping("/course/{cid}/tHomework/{thId}/compare")
    public String getSimilarHomeworksWithCidThId(@PathVariable int cid, @PathVariable int thId){
        thId = cid*10+thId;
        return RestBean.success(studentHomeworkService.calculateSimilarHomeworksWithThId(thId), "成功查询全部相似作业信息").asJsonString();
    }

//    @GetMapping("/sHomework/{shId1}/{shId2}/compare")
//    public String getTextSilimarityWithShIds(@PathVariable int shId1, @PathVariable int shId2){
//        return RestBean.success(studentHomeworkService.compareFiles(shId1, shId2), "<--成功查询相似度").asJsonString();
//    }

    @GetMapping("/course/{cid}/getAllScore")//thId表次数
    public String getAvgTotalScoresWithCidTid(@PathVariable int cid){
        UserDetails userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Account account = accountService.findAccountByNameOrEmail(userDetails.getUsername());
        String tid = account.getUid();

        List<Integer> thIds = teacherHomeworkMapper.getThIdsByTidAndCid(tid, cid);
        List<String> sids = studentCourseMapper.getSidsByCid(cid);

        return RestBean.success(markService.calculateAvgTotalScores(thIds, sids)).asJsonString();
    }

    //查询指定sid学生的指定thId作业下的成绩
    //还未被批改则返回null
    @GetMapping("/course/{cid}/tHomework/{thId}/student/{sid}/getScore")//thId表次数
    public String getAvgScoreMarkWithSidThId(@PathVariable String sid,@PathVariable int cid, @PathVariable int thId){
        thId = cid*10 + thId;
        List<Mark> marks = markService.getAllMarksByThId(thId, sid);
        Double score = markService.calculateAverageScore(marks);
        if (score == 0)
            score = null;
//        //封装以下再返回
//        TotalScoreVO totalScoreVO = new TotalScoreVO().setSid(sid)
//                .setSname(studentMapper.getUsernameBySid(sid))
//                .setScore(score);
        return RestBean.success(score ,"成功查询本次作业成绩").asJsonString();
    }

    @GetMapping("/course/{cid}/tHomework/getAll")
    public String getThsWithTidCid(@PathVariable int cid){
        UserDetails userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Account account = accountService.findAccountByNameOrEmail(userDetails.getUsername());
        String tid = account.getUid();

        return RestBean.success(teacherHomeworkMapper.getThsByTidAndCid(tid, cid), "成功查询全部教师作业，当前tid："+tid).asJsonString();
    }

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


