package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.RestBean;
import com.example.entity.Email;
import com.example.entity.dto.Account;
import com.example.entity.dto.Admin;
import com.example.entity.dto.Student;
import com.example.entity.dto.Teacher;
import com.example.mapper.AccountMapper;
import com.example.service.AccountService;
import com.example.util.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {
    @Resource
    JwtUtils jwtUtils;

    @Resource
    AdminServiceImpl adminService;
    @Resource
    TeacherServiceImpl teacherService;
    @Resource
    StudentServiceImpl studentService;



    @Override
    public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
        //这里传进来的name可能是email
        Account account = this.findAccountByNameOrEmail(name);
        if (account == null) throw new UsernameNotFoundException("用户名或密码错误");
        return User
                .withUsername(name)//这里传进来什么传出去什么
                .password(account.getPassword())
                .roles(account.getRole())
                .build();
    }

    //没找到返回null
    public Account findAccountByNameOrEmail(String name) {
        Admin admin = adminService.findAdminByNameOrEmail(name);
        Teacher teacher = teacherService.findTeacherByNameOrEmail(name);
        Student student = studentService.findStudentByNameOrEmail(name);
        //上面的name可能使email，这里username是username
        String id, username, email, role, password;
        Date registerTime;
        //判断是什么身份，并设好值给account
        if (admin != null) {
            id = admin.getAid();
            username = admin.getUsername();
            email = admin.getEmail();
            role = "admin";
            password = admin.getPassword();
            registerTime = admin.getRegisterTime();
        } else if (teacher != null) {
            id = teacher.getTid();
            username = teacher.getUsername();
            email = teacher.getEmail();
            role = "teacher";
            password = teacher.getPassword();
            registerTime = teacher.getRegisterTime();
        } else if (student != null) {
            id = student.getSid();
            username = student.getUsername();
            email = student.getEmail();
            role = "student";
            password = student.getPassword();
            registerTime = student.getRegisterTime();
        } else {
            return null;
        }

        return new Account()
                .setUid(id)
                .setUsername(username)
                .setPassword(password)
                .setEmail(email)
                .setRole(role)
                .setRegisterTime(registerTime);
    }

    //以默认密码“000000”添加用户
    //邮箱为uid加bjtu邮箱后缀
    //admin权限
    @Transactional
    public String addAccountWithDefaultPassword(String id, String username, String role) {
        switch (role) {
            case "admin" -> {
                return adminService.addAdminWithDefaultPassword(id, username);
            }
            case "teacher" -> {
                return teacherService.addTeacherWithDefaultPassword(id, username);
            }
            case "student" -> {
                return studentService.addStudentWithDefaultPassword(id, username);
            }
            default -> {
                return RestBean.failure(999, "invalid role").asJsonString();
            }
        }
    }

    public String changeName(UserDetails userDetails,
                             String newName) {
        //修改用户名
        //根据role, 更新用户名
        Account account = this.findAccountByNameOrEmail(userDetails.getUsername());
        switch (account.getRole()) {
            case "admin" -> {
                Admin admin = new Admin()
                        .setAid(account.getUid())
                        .setUsername(newName)
                        .setPassword(account.getPassword())
                        .setEmail(account.getEmail())
                        .setRegisterTime(account.getRegisterTime())
                        .setPassword(account.getPassword());
                if (!adminService.updateById(admin)) return RestBean.failure(999, "admin更新失败").asJsonString();
            }
            case "teacher" -> {
                Teacher teacher = new Teacher()
                        .setTid(account.getUid())
                        .setUsername(newName)
                        .setPassword(account.getPassword())
                        .setEmail(account.getEmail())
                        .setRegisterTime(account.getRegisterTime())
                        .setPassword(account.getPassword());
                if (!teacherService.updateById(teacher)) return RestBean.failure(999, "teacher更新失败").asJsonString();
            }
            case "student" -> {
                Student student = new Student()
                        .setSid(account.getUid())
                        .setUsername(newName)
                        .setPassword(account.getPassword())
                        .setEmail(account.getEmail())
                        .setRegisterTime(account.getRegisterTime())
                        .setPassword(account.getPassword());
                if (!studentService.updateById(student)) return RestBean.failure(999, "student更新失败").asJsonString();
            }
            default -> {
                return RestBean.failure(999, "invalid role occurs in changeName").asJsonString();
            }
        }

        //创建新令牌
        UserDetails newUserDetails = this.loadUserByUsername(newName);
        Account newAccount = this.findAccountByNameOrEmail(newName);
        String newJwtToken = jwtUtils.createJwt(newUserDetails, newAccount.getUid(), newAccount.getUsername());
        return RestBean.success(newJwtToken, "username更改成功").asJsonString();
    }

    //修改密码（非忘记密码，需登录状态，需旧密码）
    @Transactional//事务
    public String changePassword(UserDetails userDetails,
                                 HttpServletRequest httpServletRequest,
                                 String oldPassword, String newPassword) {
        //修改密码
        //验证旧密码
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        Account account = this.findAccountByNameOrEmail(userDetails.getUsername());
        if (!bCryptPasswordEncoder.matches(oldPassword, account.getPassword()))
            return RestBean.failure(999, "密码错误").asJsonString();
        //根据role, 更新密码
        String role = account.getRole();
        switch (role) {
            case "admin" -> {
                Admin admin = new Admin()
                        .setAid(account.getUid())
                        .setUsername(account.getUsername())
                        .setPassword(account.getPassword())
                        .setEmail(account.getEmail())
                        .setRegisterTime(account.getRegisterTime());
                admin.setPassword(bCryptPasswordEncoder.encode(newPassword));
                if (!adminService.updateById(admin)) return RestBean.failure(999, "admin更新失败").asJsonString();
            }
            case "teacher" -> {
                Teacher teacher = new Teacher()
                        .setTid(account.getUid())
                        .setUsername(account.getUsername())
                        .setPassword(account.getPassword())
                        .setEmail(account.getEmail())
                        .setRegisterTime(account.getRegisterTime());
                teacher.setPassword(bCryptPasswordEncoder.encode(newPassword));
                if (!teacherService.updateById(teacher)) return RestBean.failure(999, "teacher更新失败").asJsonString();
            }
            case "student" -> {
                Student student = new Student()
                        .setSid(account.getUid())
                        .setUsername(account.getUsername())
                        .setPassword(account.getPassword())
                        .setEmail(account.getEmail())
                        .setRegisterTime(account.getRegisterTime());
                student.setPassword(bCryptPasswordEncoder.encode(newPassword));
                if (!studentService.updateById(student)) return RestBean.failure(999, "student更新失败").asJsonString();
            }
            default -> {
                return RestBean.failure(999, "invalid role occurs in changepassword").asJsonString();
            }
        }

        //使当前令牌失效
        String authorization = httpServletRequest.getHeader("Authorization");//获得header中jwt
        if (!jwtUtils.invalidateJwt(authorization)) return RestBean.failure(999, "更新jwt失败").asJsonString();

        //创建新令牌
        UserDetails newUserDetails = this.loadUserByUsername(userDetails.getUsername());
        String newJwtToken = jwtUtils.createJwt(newUserDetails, account.getUid(), account.getUsername());
        return RestBean.success(newJwtToken).asJsonString();
    }


}