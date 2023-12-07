package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.dto.Student;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StudentMapper extends BaseMapper<Student> {
    // 根据给定的 sid，在 student 表中查询对应的 username
    @Select("SELECT username FROM test1.student WHERE sid = #{sid}")
    String getUsernameBySid(@Param("sid") String sid);

    @Select("SELECT s.sid, s.username, s.email FROM test1.student s " +
            "INNER JOIN test1.student_course sc ON s.sid = sc.sid " +
            "WHERE sc.cid = #{cid}")
    List<Student> selectStudentsByCid(int cid);
}
