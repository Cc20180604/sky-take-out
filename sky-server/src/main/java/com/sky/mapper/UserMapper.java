package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

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

    /**
     * 根据id查询user
     * @param id
     * @return
     */
    @Select("select * from user where id = #{id}")
    public User selectById(Long id);

    /**
     * 统计该时间之前创建的所有用户
     * @return
     */
    @Select("select count(*) from user where create_time < #{createTime}")
    public Integer countUserBefore(LocalDateTime createTime);

    /**
     * 统计间隔时间段的新增用户
     * @param begin
     * @param end
     */
    @Select("select count(*) from user where create_time between #{begin} and #{end}")
    Integer countUser(LocalDateTime begin, LocalDateTime end);
}
