package com.example.entity.vo.response;

import lombok.Data;

import java.util.Date;

//登录验证通过时，用于封装返回前端的信息，包括：username、role、jwtToken、expireTime
//其实token中已包含这些信息，这样做只是便于前端使用
@Data
public class AuthorizeVO {
    String username;
    String role;
    String token;
    Date expire;
}
