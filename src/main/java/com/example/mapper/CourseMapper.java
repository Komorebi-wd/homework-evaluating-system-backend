package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.dto.Course;
import com.example.entity.dto.Student;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {
    @Select("SELECT c.* FROM test1.course c " +
            "INNER JOIN test1.student_course sc ON c.cid = sc.cid " +
            "WHERE sc.sid = #{sid}")
    List<Course> selectCoursesBySid(String sid);

    @Select("SELECT c.cname FROM test1.course c " +
            "WHERE c.cid = #{cid}")
    String getCnameByCid(int cid);
}
