package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.RestBean;
import com.example.entity.dto.Admin;
import com.example.mapper.AdminMapper;
import com.example.service.AdminService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {
    public Admin findAdminByNameOrEmail(String username){
        return this.query()
                .eq("username", username).or()
                .eq("email", username)
                .one();
    }

    //以默认密码“000000”添加用户
    //邮箱为id加bjtu邮箱后缀
    //admin权限
    @Transactional
    public String addAdminWithDefaultPassword(String id, String username){
        Admin admin = new Admin()
                .setAid(id)
                .setUsername(username)
                .setEmail(id + "@bjtu.edu.cn")
                .setPassword(new BCryptPasswordEncoder().encode("000000"))
                .setRegisterTime(new Date());
        if(this.saveOrUpdate(admin))
            return RestBean.success(admin).asJsonString();
        else return RestBean.failure(999, "failure to add admin").asJsonString();
    }
}
