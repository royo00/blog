package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.SiteStatistics;
import org.apache.ibatis.annotations.Mapper;

/**
 * 网站统计Mapper接口
 */
@Mapper
public interface SiteStatisticsMapper extends BaseMapper<SiteStatistics> {
}
