package com.example.entity.vo.response;

import lombok.Data;
import org.springframework.context.annotation.Bean;

import java.util.Date;

/**
 * @Author: ASASHIKI
 * @CreateTime: 2025-03-27  23:21
 * @Description:
 */
@Data
public class AuthorizeVO {
    String username;
    String role;
    String token;
    Date expire;
}
