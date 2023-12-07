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

    // 根据给定的 thId 和 sid，在 student_homework 表中查询所有对应的 shId
    @Select("SELECT sh_id FROM test1.student_homework WHERE th_id = #{thId} AND sid = #{sid}")
    List<Integer> getShIdsByThIdSid(@Param("thId") Integer thId, @Param("sid") String sid);


    // 根据给定的 shId 在 mark 表中查询全部对应的记录
    @Select("SELECT * FROM test1.mark WHERE sh_id = #{shId}")
    List<Mark> getMarksByShId(@Param("shId") Integer shId);

    @Select("SELECT m.* FROM test1.mark m " +
            "INNER JOIN test1.student_homework sh ON m.sh_id = sh.sh_id " +
            "WHERE sh.th_id = #{thId} AND m.commenter_id = #{sid}")
    List<Mark> selectMarksByThId(@Param("thId") int thId, @Param("sid") String sid);
}
