package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.dto.Student;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StudentMapper extends BaseMapper<Student> {
    @Select("SELECT s.sid, s.username, s.email FROM test1.student s " +
            "INNER JOIN test1.student_course sc ON s.sid = sc.sid " +
            "WHERE sc.cid = #{cid}")
    List<Student> selectStudentsByCid(int cid);
}
