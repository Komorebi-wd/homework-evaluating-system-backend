package com.example.sb_demo1;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.entity.dto.Account;
import com.example.entity.dto.StudentCourse;
import com.example.mapper.StudentCourseMapper;
import com.example.mapper.TeacherHomeworkMapper;
import com.example.util.MarkUtils;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;

@SpringBootTest
class SbDemo1ApplicationTests {

    @Test
    void contextLoads() {

    }

}
