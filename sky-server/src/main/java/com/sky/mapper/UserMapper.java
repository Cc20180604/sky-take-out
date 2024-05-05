package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {
    /**
     * 新增用户
     * @param user
     */
    public void insert(User user);

    /**
     * 根据openid查询user
     * @param openid
     * @return
     */
    @Select("select * from user where openid = #{openid}")
    public User selectByOpenid(String openid);
}
