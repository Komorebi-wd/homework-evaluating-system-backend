package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.RestBean;
import com.example.entity.dto.Mark;
import com.example.entity.dto.StudentHomework;
import com.example.mapper.MarkMapper;
import com.example.service.MarkService;
import com.example.util.NewFileUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.Objects;

@Service
public class MarkServiceImpl extends ServiceImpl<MarkMapper, Mark> implements MarkService {
    @Resource
    NewFileUtil newFileUtil;
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
