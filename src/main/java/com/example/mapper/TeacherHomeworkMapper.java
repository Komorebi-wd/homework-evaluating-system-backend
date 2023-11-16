package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.dto.TeacherHomework;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TeacherHomeworkMapper extends BaseMapper<TeacherHomework> {
    @Select("SELECT th.* FROM test1.teacher_homework th "+
            "INNER JOIN test1.student_homework sh on th.th_id = sh.th_id "+
            "WHERE sh.sh_id = #{shId}")
    TeacherHomework getThByShId(int shId);

    @Select("SELECT th.cid FROM test1.teacher_homework th "+
            "WHERE th.th_id = #{thId}")
    int getCidByThId(int thId);
}
