package com.blog.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 数据面板DTO
 */
@Data
public class DashboardDTO {

    /**
     * 总用户数
     */
    private Integer totalUsers;

    /**
     * 总文章数
     */
    private Integer totalArticles;

    /**
     * 总评论数
     */
    private Integer totalComments;

    /**
     * 每日统计数据
     */
    private List<DailyStatDTO> dailyStats;

    /**
     * 每日统计数据
     */
    @Data
    public static class DailyStatDTO {
        /**
         * 日期
         */
        private LocalDate date;

        /**
         * 页面浏览量
         */
        private Integer pv;

        /**
         * 独立访客数
         */
        private Integer uv;

        /**
         * 新注册用户数
         */
        private Integer newUsers;
    }
}
