package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.Account;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AccountService extends IService<Account>, UserDetailsService {

    //查找account（用户名or邮箱均可）
    Account findAccountByNameOrEmail(String username);

    //以默认密码“000000”添加用户
    //邮箱为uid加bjtu邮箱后缀
    //admin权限
    String addAccountWithDefaultPassword(String uid, String username, String role);

    //修改密码（非忘记密码，需登录状态，需旧密码）
    String changePassword(UserDetails userDetails,
                         HttpServletRequest httpServletRequest,
                         String oldPassword, String newPassword);
}
