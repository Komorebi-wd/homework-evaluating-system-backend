package com.example.util;

import com.example.entity.Email;
import com.example.entity.RestBean;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

public class EmailUtils {

    @Resource
    JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    String emailSender;

    public Email createEmail(String receiver){
        String random = String.valueOf((int)((Math.random()*9+1)*100000));
        return new Email()
                .setReciever(receiver)
                .setContent(random)
                .setSubject("学生作业互评系统：请接收您的验证码");
    }
    //邮件发送
    public String sendEmail(String receiver) {
        Email email = createEmail(receiver);

        //创建简单邮件消息
        SimpleMailMessage message = new SimpleMailMessage();
        //谁发的
        message.setFrom(emailSender);
        //谁要接收
        message.setTo(email.getReciever());
        //邮件标题
        message.setSubject(email.getSubject());
        //邮件内容
        message.setText(email.getContent());
        mailSender.send(message);

        return email.getContent();
    }
}
