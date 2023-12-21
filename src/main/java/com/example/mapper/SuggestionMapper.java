package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.dto.Suggestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SuggestionMapper extends BaseMapper<Suggestion> {
    //学生查看本人申诉
    @Select("SELECT * FROM test1.suggestion WHERE sid = #{sid} AND cid = #{cid} AND status = 'N'")
    List<Suggestion> getSuggestionsBySidAndCidAndStatusN(@Param("sid") String sid, @Param("cid") Integer cid);
    @Select("SELECT * FROM test1.suggestion WHERE sid = #{sid} AND cid = #{cid} AND status = 'Y'")
    List<Suggestion> getSuggestionsBySidAndCidAndStatusY(@Param("sid") String sid, @Param("cid") Integer cid);
    //老师查看被申诉
    @Select("SELECT * FROM test1.suggestion WHERE cid = #{cid} AND status = 'N'")
    List<Suggestion> getSuggestionsByCidAndStatusN(@Param("cid") Integer cid);
    @Select("SELECT * FROM test1.suggestion WHERE cid = #{cid} AND status = 'Y'")
    List<Suggestion> getSuggestionsByCidAndStatusY(@Param("cid") Integer cid);
}
