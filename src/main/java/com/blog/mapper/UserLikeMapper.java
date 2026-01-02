package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.UserLike;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户点赞Mapper接口
 */
@Mapper
public interface UserLikeMapper extends BaseMapper<UserLike> {
}
