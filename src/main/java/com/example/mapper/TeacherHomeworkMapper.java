package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.dto.TeacherHomework;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TeacherHomeworkMapper extends BaseMapper<TeacherHomework> {

    // 根据给定的 tid 和 cid，在 teacher_homework 表中查询所有对应的 thId 列表
    @Select("SELECT th_id FROM test1.teacher_homework WHERE tid = #{tid} AND cid = #{cid}")
    List<Integer> getThIdsByTidAndCid(@Param("tid") String tid, @Param("cid") int cid);

    @Select("SELECT th.* FROM test1.teacher_homework th WHERE tid = #{tid} AND cid = #{cid}")
    List<TeacherHomework> getThsByTidAndCid(@Param("tid") String tid, @Param("cid") int cid);

    @Select("SELECT th.* FROM test1.teacher_homework th "+
            "INNER JOIN test1.student_homework sh on th.th_id = sh.th_id "+
            "WHERE sh.sh_id = #{shId}")
    TeacherHomework getThByShId(int shId);

    @Select("SELECT th.cid FROM test1.teacher_homework th "+
            "WHERE th.th_id = #{thId}")
    int getCidByThId(int thId);
}
