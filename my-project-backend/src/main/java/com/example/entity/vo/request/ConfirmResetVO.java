package com.example.entity.vo.request;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * @Author: ASASHIKI
 * @CreateTime: 2025-04-17  05:34
 * @Description:
 */
@Data
@AllArgsConstructor
public class ConfirmResetVO {
    @Email
    String email;
    @Length(max = 6, min = 6)
    String code;
}
