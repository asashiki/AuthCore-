package com.example.entity.vo.request;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * @Author: ASASHIKI
 * @CreateTime: 2025-04-17  05:37
 * @Description:
 */

@Data
@AllArgsConstructor
public class EmailResetVO {
    @Email
    String email;
    @Length(min = 6, max = 6)
    String code;
    @Length(min = 4, max = 20)
    String password;
}
