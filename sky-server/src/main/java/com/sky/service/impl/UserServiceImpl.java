package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.sky.constant.MessageConstant;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {
    //微信登录接口
    private static final String WX_LOING = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    private UserMapper userMapper;

    /**
     * 微信用户登录
     * @param code
     * @return
     */
    @Override
    public User wxLogin(String code) {
        //获取openid
        String openid = getOpenid(code);
        //判断是否获取成功
        //失败
        if (openid == null){
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        //成功
        //新用户注册
        User user = userMapper.selectByOpenid(openid);
        if (user == null){
            user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }
        //返回用户数据
        return user;
    }

    /**
     * 获取微信openid
     * @param code
     * @return
     */
    private String getOpenid(String code){
        //根据授权码 获取用户openid
        Map<String, String> loginParams = new HashMap<>();
        loginParams.put("appid", weChatProperties.getAppid());
        loginParams.put("secret", weChatProperties.getSecret());
        loginParams.put("js_code", code);
        loginParams.put("grant_type","authorization_code");
        String openid = HttpClientUtil.doGet(WX_LOING, loginParams);
        openid = (String) JSON.parseObject(openid).get("openid");
        return openid;
    }

}
