package com.example.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.course.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
    List<Message> selectByUserId(@Param("userId") String userId);
    
    List<String> selectAllUserIds();
    
    List<String> selectUserIdsByRole(@Param("role") Integer role);
    
    List<Message> searchMessages(@Param("userId") String userId, 
                                 @Param("keyword") String keyword, 
                                 @Param("offset") int offset, 
                                 @Param("size") int size);
}