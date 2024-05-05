package com.sky.service;

import com.sky.entity.User;
import com.sky.vo.UserLoginVO;

public interface UserService {
    /**
     * 微信用户登录
     * @param code
     * @return
     */
    public User wxLogin(String code);
}
