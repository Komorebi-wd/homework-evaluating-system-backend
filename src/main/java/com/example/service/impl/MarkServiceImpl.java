package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.RestBean;
import com.example.entity.dto.Mark;
import com.example.entity.dto.StudentHomework;
import com.example.entity.vo.TotalScoreVO;
import com.example.mapper.MarkMapper;
import com.example.mapper.StudentMapper;
import com.example.service.MarkService;
import com.example.util.NewFileUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class MarkServiceImpl extends ServiceImpl<MarkMapper, Mark> implements MarkService {
    @Resource
    NewFileUtil newFileUtil;
    @Resource
    MarkMapper markMapper;
    @Resource
    StudentMapper studentMapper;

    // 对于给定 List<int> thIds 和 List<String> sids，计算每个 sid 的 avgTotalScore
    //返回TotalScoreVO
    //score为0则返回null
    public List<TotalScoreVO> calculateAvgTotalScores(List<Integer> thIds, List<String> sids) {
        // List<Double> avgTotalScores = new ArrayList<>();
        List<TotalScoreVO> totalScoreVOS = new ArrayList<>();

        for (String sid : sids) {
            double sumAvgScores = 0.0;
            TotalScoreVO totalScoreVO = new TotalScoreVO();

            for (Integer thId : thIds) {
                // 获取某个 sid 下某个 thId 的平均分
                List<Mark> marks = getAllMarksByThId(thId, sid);
                Double avgScore = calculateAverageScore(marks);

                // 累加每个 thId 的平均分
                sumAvgScores += avgScore;
            }

            // 计算每个 sid 下全部 thId 的平均分
            double avgTotalScore = sumAvgScores / thIds.size();
            totalScoreVO.setSid(sid)
                    .setSname(studentMapper.getUsernameBySid(sid))
                    .setScore(avgTotalScore != 0 ? avgTotalScore : null);
            //System.out.println(sid + ": "+sumAvgScores+ ", "+thIds.size()+", "+avgTotalScore);

            // 将每个 sid 的 avgTotalScore 添加到列表中
            //avgTotalScores.add(avgTotalScore);
            totalScoreVOS.add(totalScoreVO);
        }

        return  totalScoreVOS;
        //return avgTotalScores;
    }

    //计算marks的平均分，空则返回0
    public Double calculateAverageScore(List<Mark> marks) {
        if (marks == null || marks.isEmpty()) {
            return 0.0;
        }

        double sum = 0.0;

        for (Mark mark : marks) {
            sum += mark.getScore();
        }

        return sum / marks.size();
    }

    //指定thId作业, sid学生所提交作业所被批改的全部记录mark
    public List<Mark> getAllMarksByThId(Integer thId, String sid) {
        // 获取所有对应的 shId 列表
        List<Integer> shIds = markMapper.getShIdsByThIdSid(thId, sid);

        // 存放所有的 Mark 记录
        List<Mark> allMarks = new ArrayList<>();

        // 循环调用 getMarksByShId 方法，将结果合并到 allMarks 中
        for (Integer shId : shIds) {
            List<Mark> marksForShId = markMapper.getMarksByShId(shId);
            allMarks.addAll(marksForShId);
        }

        return allMarks;
    }


    @Transactional
    public String addMark(MultipartFile multipartFile, int shId, String comment, String commenterId, double score) throws IOException {
        Mark mark = new Mark()
                .setShId(shId)
                .setFileData(multipartFile.getBytes())
                .setFileName(Objects.requireNonNull(multipartFile.getOriginalFilename()).substring(0,multipartFile.getOriginalFilename().lastIndexOf(".")))
                .setFileType(newFileUtil.getExtension(multipartFile))
                .setFileSize(String.valueOf(multipartFile.getSize()))
                .setSubmitTime(new Date())
                .setComment(comment)
                .setScore(score)
                .setCommenterId(commenterId);

        if (this.saveOrUpdate(mark)){
            return RestBean.success(mark, "评论成功，当前shId: "+shId+", commenterId: "+commenterId).asJsonString();
        } else return RestBean.failure(999, "评论失败，当前shId: "+shId+", commenterId: "+commenterId).asJsonString();
    }
}
