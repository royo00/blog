package com.blog.task;

import com.blog.service.StatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 统计数据定时任务
 * 负责定期聚合访问量等统计数据
 */
@Slf4j
@Component
public class StatisticsTask {

    @Autowired
    private StatisticsService statisticsService;

    /**
     * 每天凌晨1点执行统计聚合
     * cron表达式: 秒 分 时 日 月 周
     * 0 0 1 * * ? 表示每天凌晨1点0分0秒执行
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void aggregateDailyStatistics() {
        log.info("========== 开始执行每日统计数据聚合任务 ==========");

        try {
            statisticsService.aggregateDailyStatistics();
            log.info("========== 每日统计数据聚合任务执行成功 ==========");
        } catch (Exception e) {
            log.error("========== 每日统计数据聚合任务执行失败 ==========", e);
        }
    }

    /**
     * 测试用：每5分钟执行一次（开发环境可启用）
     * 生产环境请注释掉此方法
     */
    // @Scheduled(cron = "0 */5 * * * ?")
    public void aggregateDailyStatisticsTest() {
        log.info("========== [测试] 开始执行统计数据聚合任务 ==========");

        try {
            statisticsService.aggregateDailyStatistics();
            log.info("========== [测试] 统计数据聚合任务执行成功 ==========");
        } catch (Exception e) {
            log.error("========== [测试] 统计数据聚合任务执行失败 ==========", e);
        }
    }
}
