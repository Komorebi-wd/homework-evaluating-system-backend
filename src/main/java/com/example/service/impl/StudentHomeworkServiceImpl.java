package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.RestBean;
import com.example.entity.dto.StudentHomework;
import com.example.entity.dto.TeacherHomework;
import com.example.entity.vo.response.SimilarHomework;
import com.example.mapper.MarkMapper;
import com.example.mapper.StudentCourseMapper;
import com.example.mapper.StudentHomeworkMapper;
import com.example.mapper.TeacherHomeworkMapper;
import com.example.service.StudentHomeworkService;
import com.example.util.CheckUtil;
import com.example.util.NewFileUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.converter.json.GsonBuilderUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class StudentHomeworkServiceImpl extends ServiceImpl<StudentHomeworkMapper, StudentHomework> implements StudentHomeworkService {
    @Resource
    NewFileUtil newFileUtil;
    @Resource
    StudentHomeworkMapper studentHomeworkMapper;
    @Resource
    TeacherHomeworkMapper teacherHomeworkMapper;
    @Resource
    StudentCourseMapper studentCourseMapper;

    @Resource
    MarkMapper markMapper;

    //为指定课程下的一批指定学生添加成绩
    public String addScoreWithTidCidSidsAddScore(String tid, int cid, List<String> sids, double addScore){
        List<Integer> thIds = teacherHomeworkMapper.getThIdsByTidAndCid(tid, cid);
        System.out.println(thIds);
        List<Integer> shIds = studentHomeworkMapper.getShIdsBySidsAndThIds(sids, thIds);
        System.out.println(shIds);

        if (shIds != null && !shIds.isEmpty()) {
            int items = markMapper.addScoreBatch(shIds, addScore);
            return "共"+items+"项记录被成功更改";
        }
        return "更新失败";
    }

    public List<SimilarHomework> calculateSimilarHomeworksWithThId(int thId) {
        int cid = teacherHomeworkMapper.getCidByThId(thId);
        System.out.println(cid);
        List<String> sids = studentCourseMapper.getSidsByCid(cid);
        System.out.println("1");

        List<SimilarHomework> similarHomeworkList = new ArrayList<>();

        for (String sid : sids) {//遍历每个同学
            List<Integer> shIds = studentHomeworkMapper.getShIdsBySidAndThId(sid, thId);
            double maxSimilarity = Double.MIN_VALUE;
            String mostSimilarSid = null;

            List<Integer> otherShIds = studentHomeworkMapper.getShIdsByDifferentSidAndThId(sid, thId);
            for (int shId : shIds) {
                for (int otherShId : otherShIds) {
                    double similarity = compareFiles(shId, otherShId);
                        if (similarity > maxSimilarity) {
                            maxSimilarity = similarity;//所有其他同学中的最大值
                            mostSimilarSid = studentHomeworkMapper.getSidByShId(otherShId);//该同学sid
                        }
                }
            }
            if (mostSimilarSid != null) {//异常情况：比如不存在其他同学
                similarHomeworkList.add(new SimilarHomework(sid, mostSimilarSid, maxSimilarity));
            }
        }
        return similarHomeworkList;
    }


    //比较两个文件相似度(simHash)
    public double compareFiles(int shId1, int shId2) {
        StudentHomework studentHomework1 = this.getById(shId1);
        StudentHomework studentHomework2 = this.getById(shId2);
        byte[] fileData1 = studentHomework1.getFileData();
        String fileType1 = studentHomework1.getFileType();
        byte[] fileData2 = studentHomework2.getFileData();
        String fileType2 = studentHomework2.getFileType();
        return CheckUtil.calculateFileSimilarity(fileData1, fileType1, fileData2, fileType2);
    }

    //获得Th下某sid的Sh(有可能是多个）
    public List<StudentHomework> getStudentHomeworksByThIdSid(int thId, String sid) {
        QueryWrapper<StudentHomework> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("th_id", thId)
                .eq("sid", sid);

        return studentHomeworkMapper.selectList(queryWrapper);
    }

    //下载指定shId学生作业
    public boolean downloadStudentHomework(int shId, HttpServletResponse httpServletResponse) throws UnsupportedEncodingException {
        StudentHomework studentHomework = this.getById(shId);
        return newFileUtil.downloadFile(studentHomework.getFileData(), studentHomework.getFileName(), studentHomework.getFileType(),httpServletResponse);
    }


public boolean downloadStudentHomeworks(List<StudentHomework> shList, HttpServletResponse response) throws UnsupportedEncodingException {
    // 设置响应头，告诉浏览器返回的是一个 ZIP 文件
    response.setContentType("application/zip;charset=UTF-8");
    response.setHeader("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode("files.zip", "UTF-8") + "\"");

    try (ServletOutputStream outputStream = response.getOutputStream();
         ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {

        for (int i = 0; i < shList.size(); i++) {
            StudentHomework entity = shList.get(i);
            byte[] data = entity.getFileData();
            String fileName = entity.getFileName() + "." + entity.getFileType();

            // 检查文件名是否已存在，如果存在就添加序号
            String newFileName = checkAndAppendNumberIfNecessary(shList, fileName, i);

            // 添加 ZIP 文件中的条目
            ZipEntry zipEntry = new ZipEntry(newFileName);
            zipOutputStream.putNextEntry(zipEntry);

            // 将文件数据写入 ZIP 文件
            zipOutputStream.write(data);

            // 关闭 ZIP 条目
            zipOutputStream.closeEntry();
        }

        // 刷新并关闭 ZIP 输出流
        zipOutputStream.finish();
        zipOutputStream.flush();

        return true;
    } catch (IOException e) {
        e.printStackTrace();
        return false;
    }
}

    private String checkAndAppendNumberIfNecessary(List<StudentHomework> shList, String fileName, int currentIndex) {
        String newFileName = fileName;
        int count = 1;

        // 检查文件名是否已存在
        for (int i = 0; i < currentIndex; i++) {
            if (fileName.equals(shList.get(i).getFileName() + "." + shList.get(i).getFileType())) {
                // 文件名已存在，添加序号
                newFileName = fileName.substring(0, fileName.lastIndexOf('.')) + "(" + count + ")." + shList.get(currentIndex).getFileType();
                count++;
            }
        }

        return newFileName;
    }



    //获得thId下全部sh
    // 此thId为真正thId
    public List<StudentHomework> getStudentHomeworksByThId(int thId){
        QueryWrapper<StudentHomework> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("th_id", thId);

        return studentHomeworkMapper.selectList(queryWrapper);
    }

    //thId为真正thId
    @Transactional
    public StudentHomework submitStudentHomework(MultipartFile multipartFile,String sid, int thId, String comment) throws IOException, SQLException {
        //thId = cid*10 + thId;
        StudentHomework studentHomework = new StudentHomework()
                .setSid(sid)
                .setThId(thId)
                .setFileData(multipartFile.getBytes())
                .setFileName(multipartFile.getOriginalFilename().substring(0,multipartFile.getOriginalFilename().lastIndexOf(".")))
                .setFileType(newFileUtil.getExtension(multipartFile))
                .setFileSize(String.valueOf(multipartFile.getSize()))
                .setSubmitTime(new Date())
                .setComment(comment);

        if (this.saveOrUpdate(studentHomework)){
            //return RestBean.success(studentHomework).asJsonString();
            return studentHomework;
        } else return null;
            //return RestBean.failure(999, "上传学生错误").asJsonString();
    }
}
