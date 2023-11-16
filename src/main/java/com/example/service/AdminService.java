package com.example.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.Admin;
public interface AdminService extends IService<Admin> {
    Admin findAdminByNameOrEmail(String username);
}
