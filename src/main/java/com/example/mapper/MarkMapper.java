package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.dto.Mark;
import com.example.entity.dto.Student;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MarkMapper extends BaseMapper<Mark> {
    @Select("SELECT m.* FROM test1.mark m " +
            "INNER JOIN test1.student_homework sh ON m.sh_id = sh.sh_id " +
            "WHERE sh.th_id = #{thId} AND m.commenter_id = #{sid}")
    List<Mark> selectMarksByThId(@Param("thId") int thId, @Param("sid") String sid);
}
