package com.cjl.self_schedule.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cjl.self_schedule.message.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {

    @Select("SELECT COUNT(*) FROM user_message WHERE recipient_id = #{userId} AND is_read = 0")
    Long getUnreadCount(@Param("userId") Long userId);
}
