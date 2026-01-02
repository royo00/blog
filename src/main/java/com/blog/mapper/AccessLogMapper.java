package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.AccessLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 访问日志Mapper接口
 */
@Mapper
public interface AccessLogMapper extends BaseMapper<AccessLog> {
}
