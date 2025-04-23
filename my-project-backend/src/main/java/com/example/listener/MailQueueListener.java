package com.example.listener;

import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @Author: ASASHIKI
 * @CreateTime: 2025-04-15  08:55
 * @Description:
 */
@Component
@RabbitListener(queues = "mail")
public class MailQueueListener {

    @Resource
    JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    String username;

    @RabbitHandler
    public void sendMailMessage(Map<String, Object> data) {
        String email = (String) data.get("email");
        Integer code = (Integer) data.get("code");
        String type = (String) data.get("type");
        SimpleMailMessage message = switch (type) {
            case "register" -> creatMessage("欢迎注册网站",
                    "您的邮箱验证码为: "+ code + ", 有效时间为3分钟, 为了保障您的安全,请勿向他人泄露验证码信息", email);
            case "reset" -> creatMessage("你的密码重置邮箱",
                    "你好,你正在进行重置密码,验证码: "+ code + ", 有效时间为3分钟,如非本人操作,请无视", email);
            default -> null;
        };
        if (message == null) return;
        mailSender.send(message);
    }

    public SimpleMailMessage creatMessage(String title, String content, String email) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject(title);
        message.setText(content);
        message.setFrom(username);
        message.setTo(email);
        return message;
    }

}
