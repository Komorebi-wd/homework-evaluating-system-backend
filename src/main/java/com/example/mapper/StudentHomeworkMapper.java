package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.dto.Course;
import com.example.entity.dto.StudentHomework;
import com.example.entity.dto.TeacherHomework;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StudentHomeworkMapper extends BaseMapper<StudentHomework> {
    @Select("SELECT * FROM test1.student_homework WHERE sh_id = #{shId}")
    StudentHomework selectByShId(@Param("shId") int shId);

    //获得指定sid学生在指定thid课程作业下提交的全部作业shids
    @Select("SELECT sh_id FROM test1.student_homework WHERE sid = #{sid} AND th_id = #{thId}")
    List<Integer> getShIdsBySidAndThId(@Param("sid") String sid, @Param("thId") Integer thId);

    //指定课程作业下，不同于sid的其他学生提交的全部作业shids
    @Select("SELECT sh_id FROM test1.student_homework WHERE sid != #{sid} AND th_id = #{thId}")
    List<Integer> getShIdsByDifferentSidAndThId(@Param("sid") String sid, @Param("thId") Integer thId);

    @Select("SELECT sid FROM test1.student_homework WHERE sh_id = #{shId}")
    String getSidByShId(@Param("shId") Integer shId);

    // 通过sid和thIds列表获取shIds列表
    @Select("<script>" +
            "SELECT sh_id FROM student_homework " +
            "WHERE sid = #{sid} " +
            "AND th_id IN " +
            "<foreach item='thId' collection='thIds' open='(' separator=',' close=')'>" +
            "#{thId}" +
            "</foreach>" +
            "</script>")
    List<Integer> getShIdsBySidAndThIds(@Param("sid") String sid, @Param("thIds") List<Integer> thIds);

    // 通过sids列表和thIds列表获取shIds列表
    @Select("<script>" +
            "SELECT sh_id FROM student_homework " +
            "WHERE sid IN " +
            "<foreach item='sid' collection='sids' open='(' separator=',' close=')'>" +
            "#{sid}" +
            "</foreach> " +
            "AND th_id IN " +
            "<foreach item='thId' collection='thIds' open='(' separator=',' close=')'>" +
            "#{thId}" +
            "</foreach>" +
            "</script>")
    List<Integer> getShIdsBySidsAndThIds(@Param("sids") List<String> sids, @Param("thIds") List<Integer> thIds);



//    @Select("SELECT sh_id FROM student_homework WHERE sid = (SELECT sid FROM student_homework WHERE sh_id = #{shId}) AND sh_id != #{shId}")
//    List<Integer> getShIdsBySidAndExcludeGivenShId(@Param("shId") int shId);
//
//    //获得指定shId同课程作业下，其他人交的全部shIds
//    @Select("SELECT sh_id FROM student_homework WHERE th_id = (SELECT th_id FROM student_homework WHERE sh_id = #{shId}) AND sid != (SELECT sid FROM student_homework WHERE sh_id = #{shId})")
//    List<Integer> getShIdsByThIdAndDifferentSid(@Param("shId") int shId);
}

