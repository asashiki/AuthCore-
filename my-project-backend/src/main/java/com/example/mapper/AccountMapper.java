package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.dto.Account;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: ASASHIKI
 * @CreateTime: 2025-04-02  13:25
 * @Description:
 */
@Mapper
public interface AccountMapper extends BaseMapper<Account> {
}
