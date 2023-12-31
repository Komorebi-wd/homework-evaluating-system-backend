package com.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.entity.RestBean;
import com.example.entity.dto.*;
import com.example.entity.vo.response.DistributionVO;
import com.example.entity.vo.response.StudentScoreVO;
import com.example.mapper.*;
import com.example.service.impl.*;
import com.example.util.MarkUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/student")
public class StudentController {
    /*课程、作业相关
    * 1. getAllCourses 获得全部课程信息
    * 2. addScWithSidCid 学生选课(sid取自UserDetail）, 后续再加：（课程已满or选课时间外不能选)
    * 3. getAllCoursesBySid 当前学生课程列表（sid取自UserDetail）
    * 4. getAllThsWith
    * Cid 查看指定cid下所有教师作业Th
    * 5. downloadThWithCidThId 下载指定cid下指定thId教师作业（thId指第x次作业）
    * 6. submitShWithSidCidThIdComment 上传指定cid、thId的学生作业Sh（sid取自UserDetail)
    * 6-5. submitShsWithSidCidThIdComment 上传指定cid、thId的多个学生作业Shs（sid取自UserDetail)
    * 7. getAllShsWithSidCidThId   查看指定cid、thId、sid（取自UserDetail）的学生作业Shs
    * 8. downloadAllShsWithSidCidThId 下载指定cid、thId、sid（取自UserDetail）的学生作业Shs（若已提交多个Sh，打包成zip）
    * 9. getAllUnSubmitThsWithSid 查看指定sid（取自UserDetail）的全部为提交作业
    * */
    /*批改作业相关
    * 1. distributeShToClassMates(将本次作业分发给同学）（根据算法发给提交次数最少的同学）(后续考虑多次提交情况）
    * 2. getDistributions 获得全部被分配的学生作业信息（主要是ShId、thId、课程名等)(未批改的）
    * 3. downloadShWithShId 下载指定shId学生作业（检验shId已分配给本人）
    * 4. downloadAllDistributions 下载全部已分配作业，打包zip
    * 5. addMarkWithShIdCommentCommentIdScore 添加作业批改, 提交次数+1（文件、评论、分数）（CommentId取自UserDetail）（应该只能提交一版，后续只能修改）
    * 6. getMyMarksWithCidThId 查询我的所有提交记录(ThId为第x次作业）
    * 7. getAllMarksWithShId 查询指定ShId的被批改记录
    * 8. getMarkWithMid 查询指定mid的单条批改记录
    * */
    /*成绩相关
    * 1. getSubmittedAvgScoresWithSidCid 获得本学生指定cid下全部已提交作业的成绩*/
    /*申诉相关
    * 1. addSuggestionWithCidQ 向指定cid课堂提交申诉question
    * 2. getNSuggestionWithCid 查询本人在cid课堂中未被回复的申诉
    * 3. getNSuggestionWithCid 查询本人在cid课堂中已被回复的申诉*/
    @Resource
    CourseServiceImpl courseService;
    @Resource
    CourseMapper courseMapper;
    @Resource
    AccountServiceImpl accountService;
    @Resource
    StudentCourseServiceImpl studentCourseService;
    @Resource
    StudentCourseMapper studentCourseMapper;
    @Resource
    TeacherHomeworkServiceImpl teacherHomeworkService;
    @Resource
    StudentHomeworkServiceImpl studentHomeworkService;
    @Resource
    StudentHomeworkMapper studentHomeworkMapper;
    @Resource
    MarkServiceImpl markService;
    @Resource
    MarkMapper markMapper;
    @Resource
    TeacherHomeworkMapper teacherHomeworkMapper;
    @Resource
    MarkUtils markUtils;
    @Resource
    SuggestionServiceImpl suggestionService;
    @Resource
    SuggestionMapper suggestionMapper;

    @GetMapping("/course/{cid}/suggestion/getY")
    public String getYSuggestionWithCid(@PathVariable int cid){
        UserDetails userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Account account = accountService.findAccountByNameOrEmail(userDetails.getUsername());
        String sid = account.getUid();
        return RestBean.success(suggestionMapper.getSuggestionsBySidAndCidAndStatusY(sid, cid), "成功查询全部已回复申诉").asJsonString();
    }

    @GetMapping("/course/{cid}/suggestion/getN")
    public String getNSuggestionWithCid(@PathVariable int cid){
        UserDetails userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Account account = accountService.findAccountByNameOrEmail(userDetails.getUsername());
        String sid = account.getUid();
        return RestBean.success(suggestionMapper.getSuggestionsBySidAndCidAndStatusN(sid, cid), "成功查询全部未回复申诉").asJsonString();
    }

    @PostMapping("/course/suggestion/add")
    public String addSuggestionWithCidQ(int cid, String question){
        UserDetails userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Account account = accountService.findAccountByNameOrEmail(userDetails.getUsername());
        String sid = account.getUid();

        Suggestion suggestion = new Suggestion()
                .setSid(sid)
                .setCid(cid)
                .setQuestion(question)
                .setStatus("N");
        if (suggestionService.saveOrUpdate(suggestion)){
            return RestBean.success(suggestion, "提交成功").asJsonString();
        } else return RestBean.failure(999, "提交失败").asJsonString();
    }


    @GetMapping("/course/{cid}/getSubmittedScores")
    public String getSubmittedAvgScoresWithSidCid(@PathVariable int cid){
        UserDetails userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Account account = accountService.findAccountByNameOrEmail(userDetails.getUsername());
        String sid = account.getUid();
        List<Integer> thIds = teacherHomeworkService.findSubmittedThIds(sid, cid);
        List<Double> avgScores = markService.calculateAvgScoresWithThIdsSid(thIds, sid);
        List<StudentScoreVO> studentScores = new ArrayList<>();
        for (int i = 0; i < thIds.size(); i++) {
            StudentScoreVO studentScoreVO = new StudentScoreVO();
            studentScoreVO.setScore(avgScores.get(i));
//            System.out.println(thIds.get(i));
//            System.out.println(cid);
//            System.out.println(thIds.get(i)%cid);
            studentScoreVO.setThId(thIds.get(i)%(10*cid));

            studentScores.add(studentScoreVO);
        }
        return RestBean.success(studentScores, "成功查询指定sid、cid下全部已提交作业平均分，当前sid："+sid+", cid: "+cid).asJsonString();
    }

    @GetMapping("course/tHomework/sHomework/mark/{mid}")
    public String getMarkWithMid(@PathVariable int mid){
        return RestBean.success(markService.getById(mid), "成功查询批改信息，当前mid："+mid).asJsonString();
    }

    @GetMapping("/tHomework/unSubmit/getAll")
    public String getAllUnSubmitThsWithSid(){
        UserDetails userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Account account = accountService.findAccountByNameOrEmail(userDetails.getUsername());
        String sid = account.getUid();

        return RestBean.success(teacherHomeworkService.findUnsubmittedThIds(sid), "成功查询所有未提交作业，当前sid: "+sid).asJsonString();

    }

    //指定shId下，我被批改的全部记录
    @GetMapping("/course/tHomework/sHomework/{shId}/mark/getAll")
    public String getAllMarksWithShId(@PathVariable int shId){
        QueryWrapper<Mark> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sh_id", shId);
        List<Mark> marks = markMapper.selectList(queryWrapper);
        return RestBean.success(marks, "成功查询批改列表，当前shId: " + shId).asJsonString();
    }

    //获得指定thid下，自身所提交的全部mark
    @GetMapping("/course/{cid}/tHomework/{thId}/mark/getAll")
    public String getMyMarksWithCidThId(@PathVariable int cid, @PathVariable int thId){
        thId = cid*10 + thId;
        UserDetails userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Account account = accountService.findAccountByNameOrEmail(userDetails.getUsername());
        String sid = account.getUid();

        List<Mark> marks = markMapper.selectMarksByThId(thId,sid);
        return RestBean.success(marks, "成功查询批改列表，当前thId: "+thId).asJsonString();
    }


    //下载本人全部待批改sh，打包zip
    //写的有点丑陋，后面修饰下
    @GetMapping("/distribution/downloadAll")
    public String downloadAllDistributions(HttpServletResponse response) throws UnsupportedEncodingException {
        UserDetails userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Account account = accountService.findAccountByNameOrEmail(userDetails.getUsername());
        String sid = account.getUid();
        //获得所有本人sc
        QueryWrapper<StudentCourse> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sid", sid);
        List<StudentCourse> studentCourses = studentCourseMapper.selectList(queryWrapper);
        //查询本人sc中包含的所有待批改shIds, 获得对应shs
        int[] shIds = markUtils.findShIdsInUnmarkList(studentCourses);
        List<StudentHomework> studentHomeworks = new ArrayList<>();
        for (int shId : shIds) {
            StudentHomework studentHomework = studentHomeworkMapper.selectByShId(shId);
            if (studentHomework != null) {
                studentHomeworks.add(studentHomework);
            }
        }
        //下载
        if (studentHomeworkService.downloadStudentHomeworks(studentHomeworks, response)){
            return RestBean.success("下载全部作业成功，当前sid："+sid+", 共下载"+studentHomeworks.size()+"项").asJsonString();
        } else return RestBean.failure(999,"下载失败").asJsonString();
    }

    //获取全部被分配的待批改作业任务
    //主要是shId\sid\cid\和name信息
    //封装成vo返回
    //写的有点丑陋，后续再改进（动态sql啥的）
    @GetMapping("/distribution/getAll")
    public String getDistributions(){
        UserDetails userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Account account = accountService.findAccountByNameOrEmail(userDetails.getUsername());
        String sid = account.getUid();
        //获得所有本人sc
        QueryWrapper<StudentCourse> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sid", sid);
        List<StudentCourse> studentCourses = studentCourseMapper.selectList(queryWrapper);
        //查询本人sc中包含的所有待批改shIds, 获得对应shs
        int[] shIds = markUtils.findShIdsInUnmarkList(studentCourses);
        List<StudentHomework> studentHomeworks = new ArrayList<>();
        for (int shId : shIds) {
            StudentHomework studentHomework = studentHomeworkMapper.selectByShId(shId);
            if (studentHomework != null) {
                studentHomeworks.add(studentHomework);
            }
        }
        return RestBean.success(studentHomeworks, "成功查询待批改作业列表, 共"+studentHomeworks.size()+"项").asJsonString();

//        //转为vo返回前端
//        List<DistributionVO> distributionVOList = new ArrayList<>();
//        for (StudentHomework studentHomework : studentHomeworks) {
//            DistributionVO distributionVO = new DistributionVO();
//            distributionVO.setShId(studentHomework.getShId());
//            distributionVO.setThId(studentHomework.getThId());
//            distributionVO.setShName(studentHomework.getFileName());
//            distributionVO.setCid(teacherHomeworkMapper.getCidByThId(studentHomework.getThId()));
//            distributionVO.setCname(courseMapper.getCnameByCid(distributionVO.getCid()));
//            distributionVO.setSubmitTime(studentHomework.getSubmitTime());
//            distributionVOList.add(distributionVO);
//        }

        //return RestBean.success(distributionVOList, "成功查询待批改作业列表, 共"+distributionVOList.size()+"项").asJsonString();
    }

    //将指定shId作业分发给同班x个同学（x这里设为3）
    //分发逻辑是value最小的人
    //cid根据shId, sid根据UserDetail
    // Warning：
    //  1.防范分发不属于自己作业情况的发生
    //  2.防范重复分发同一作业 OR 同th下的不同sh

    //@PostMapping("/course/tHomework/sHomework/distribute")

    //这是一个method, 而不是一个url，后续改到service中
    public String distributeShToClassMates(int shId){
        UserDetails userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Account account = accountService.findAccountByNameOrEmail(userDetails.getUsername());
        String sid = account.getUid();
        int cid = teacherHomeworkMapper.getThByShId(shId).getCid();
        //查询班级SCs
        QueryWrapper<StudentCourse> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("cid", cid);
        List<StudentCourse> studentCourses = studentCourseMapper.selectList(queryWrapper);
        //分发作业
        List<StudentCourse> resultList = markUtils.findAndAddToUnmarkList(shId, sid, 3, studentCourses);
        for (StudentCourse sc : resultList) {
            UpdateWrapper<StudentCourse> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("sid", sc.getSid())
                    .eq("cid", cid);
            studentCourseMapper.update(sc, updateWrapper);
        }
        return RestBean.success(resultList, "分发作业成功，共发给"+resultList.size()+"人").asJsonString();
    }

    //下载指定学生作业
    //还没设置检验是否被分配
    @GetMapping("/course/tHomework/sHomework/{shId}/download")
    public String downloadShWithShId(@PathVariable int shId, HttpServletResponse response) throws UnsupportedEncodingException {
        if (studentHomeworkService.downloadStudentHomework(shId, response)){
            return RestBean.success("下载全部作业成功，当前shId："+shId).asJsonString();
        } else return RestBean.failure(999,"下载失败，当前shId："+shId).asJsonString();
    }

    //再mark的同时更新了总次数，但还没更新list
    @PostMapping("/course/tHomework/sHomework/comment/submit")
    public String addMarkWithShIdCommentCommentIdScore(int shId, String comment, double score, MultipartFile multipartFile) throws IOException {
        UserDetails userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Account account = accountService.findAccountByNameOrEmail(userDetails.getUsername());
        String commenterId = account.getUid();
        int cid = teacherHomeworkMapper.getThByShId(shId).getCid();
        //查询SC
        QueryWrapper<StudentCourse> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sid", commenterId)
                .eq("cid", cid);
        StudentCourse studentCourse = studentCourseMapper.selectOne(queryWrapper);
        //更新SC
        markUtils.moveShIdToMarkList(studentCourse, shId);
        UpdateWrapper<StudentCourse> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("sid", commenterId)
                .eq("cid", cid);
        studentCourseMapper.update(studentCourse, updateWrapper);
        //添加mark
        return markService.addMark(multipartFile, shId, comment, commenterId, score);
    }

    @GetMapping("/course/{cid}/tHomework/{thId}/sHomework/downloadAll")
    @PreAuthorize("hasRole('student')")
    public String downloadAllShsWithSidCidThId(@PathVariable int cid, @PathVariable int thId, HttpServletResponse response) throws UnsupportedEncodingException {
        thId = cid*10 + thId;
        UserDetails userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Account account = accountService.findAccountByNameOrEmail(userDetails.getUsername());
        String sid = account.getUid();

        List<StudentHomework> studentHomeworks = studentHomeworkService.getStudentHomeworksByThIdSid(thId,sid);
        if (studentHomeworkService.downloadStudentHomeworks(studentHomeworks, response)){
            return RestBean.success("下载全部作业成功，当前sid："+sid+", thId: "+thId).asJsonString();
        } else return RestBean.failure(999,"下载失败").asJsonString();
    }

    @GetMapping("/course/{cid}/tHomework/{thId}/sHomework/getAll")
    @PreAuthorize("hasRole('student')")
    public String getAllShsWithSidCidThId(@PathVariable int cid, @PathVariable int thId){
        thId = cid*10+thId;
        UserDetails userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Account account = accountService.findAccountByNameOrEmail(userDetails.getUsername());
        String sid = account.getUid();

        QueryWrapper<StudentHomework> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sid", sid)
                .eq("th_id", thId);
        List<StudentHomework> studentHomeworks = studentHomeworkMapper.selectList(queryWrapper);

        return RestBean.success(studentHomeworks, "本人作业查询成功，当前thId: "+thId+" sid: "+sid).asJsonString();
    }

    @PostMapping("/course/tHomework/sHomework/submit")
    @PreAuthorize("hasRole('student')")
    public String submitShWithSidCidThIdComment(int cid, int thId, String comment, MultipartFile multipartFile) throws SQLException, IOException {//thId指第x次作业
        thId = cid*10 + thId;//真正thId

        UserDetails userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Account account = accountService.findAccountByNameOrEmail(userDetails.getUsername());
        String sid = account.getUid();

        StudentHomework studentHomework = studentHomeworkService.submitStudentHomework(multipartFile, sid, thId, comment);
        if (studentHomework!=null) {
            this.distributeShToClassMates(studentHomework.getShId());
            return RestBean.success(studentHomework, "上传作业成功，当前thId: " + thId).asJsonString();
        }else return RestBean.failure(999, "上传作业失败，当前thId: "+thId).asJsonString();
    }

    @PostMapping("/course/tHomework/sHomework/submitAll")
    @PreAuthorize("hasRole('student')")
    public String submitShsWithSidCidThIdComment(int cid, int thId, String comment, MultipartFile[] multipartFiles) throws SQLException, IOException {//thId指第x次作业
        thId = cid*10 + thId;//真正thId

        System.out.println(thId);
        System.out.println(comment);
        System.out.println(multipartFiles);

        UserDetails userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Account account = accountService.findAccountByNameOrEmail(userDetails.getUsername());
        String sid = account.getUid();

        List<StudentHomework> studentHomeworkList = new ArrayList<>();
        for (MultipartFile multipartFile : multipartFiles){
            StudentHomework studentHomework = studentHomeworkService.submitStudentHomework(multipartFile, sid, thId, comment);
            System.out.println(studentHomework);
            if (studentHomework!=null){
                this.distributeShToClassMates(studentHomework.getShId());
                studentHomeworkList.add(studentHomework);}
            else return RestBean.failure(999, "上传作业失败，当前thId: "+thId).asJsonString();
        }
        return RestBean.success(studentHomeworkList, "上传作业成功，当前thId: "+thId).asJsonString();

        //return studentHomeworkService.submitStudentHomework(multipartFile, sid, thId);
    }

    @GetMapping("/course/{cid}/tHomework/{thId}/download")//此处thId指第x次作业
    @PreAuthorize("hasRole('student')")
    public String downloadThWithCidThid(@PathVariable int cid, @PathVariable int thId, HttpServletResponse response) throws IOException {
        thId = cid*10 + thId;//真正thId
        if (teacherHomeworkService.downloadThHomework(thId, response))
            return RestBean.success("教师作业成功下载，thId: "+ thId).asJsonString();
        else return RestBean.failure(999,"教师作业下载失败，thId: "+ thId).asJsonString();
    }

    @GetMapping("/course/{cid}/tHomework/getAll")
    @PreAuthorize("hasRole('student')")
    public String getAllThsWithCid(@PathVariable int cid){
      return RestBean.success(teacherHomeworkService.getThsByCid(cid), "成功查询该课程下教师作业，当前cid："+cid).asJsonString();
    }



    @GetMapping("/course/getMyCourses")
    @PreAuthorize("hasRole('student')")
    public String getAllCoursesBySid(){
        UserDetails userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Account account = accountService.findAccountByNameOrEmail(userDetails.getUsername());
        String sid = account.getUid();

        return RestBean.success(courseMapper.selectCoursesBySid(sid), "查询本人课程成功，当前sid："+sid).asJsonString();
    }

    @PostMapping("/course/addSc")
    @PreAuthorize("hasRole('student')")
    public String addScWithSidCid(int cid){
        UserDetails userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Account account = accountService.findAccountByNameOrEmail(userDetails.getUsername());
        String sid = account.getUid();

        StudentCourse studentCourse = new StudentCourse()
                .setSid(sid)
                .setCid(cid)
                .setMarkCount(0)
                .setUnmarkCount(0);
        if(studentCourseService.save(studentCourse)){
            return RestBean.success(studentCourse, "选课成功，sid："+sid+", cid: "+cid).asJsonString();
        } else return RestBean.failure(999, "选课失败，sid："+sid+", cid: "+cid).asJsonString();
    }

    @GetMapping("/course/getAll")
    @PreAuthorize("hasRole('student')")
    public String getAllCourses(){
        return RestBean.success(courseService.list(), "成功查询全部课程信息").asJsonString();
    }

    @GetMapping("/course/{cid}/tHomework/{thId}")
    @PreAuthorize("hasRole('student')")
    public String getThWithCidThid(@PathVariable int cid, @PathVariable int thId) {
        thId = cid*10 + thId;//真正thId
        return RestBean.success(teacherHomeworkService.getById(thId), "教师作业查询成功，当前thId: " + thId+", cid: "+cid).asJsonString();
    }

    @GetMapping("/course/tHomework/sHomework/{shId}")
    @PreAuthorize("hasRole('student')")
    public String getShWithShId(@PathVariable int shId){
        return RestBean.success(studentHomeworkService.getById(shId), "成功查询学生作业, 当前shId: "+shId).asJsonString();
    }

}


