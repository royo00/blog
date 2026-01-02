package com.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.entity.AccessLog;
import com.blog.entity.SiteStatistics;
import com.blog.entity.User;
import com.blog.mapper.AccessLogMapper;
import com.blog.mapper.SiteStatisticsMapper;
import com.blog.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 统计服务类
 * 负责网站访问量和用户数据的统计聚合
 */
@Slf4j
@Service
public class StatisticsService {

    @Autowired
    private AccessLogMapper accessLogMapper;

    @Autowired
    private SiteStatisticsMapper siteStatisticsMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 聚合昨天的访问数据到统计表
     * 每天凌晨执行，统计前一天的数据
     */
    @Transactional
    public void aggregateDailyStatistics() {
        // 获取昨天的日期
        LocalDate yesterday = LocalDate.now().minusDays(1);

        log.info("开始聚合日期 {} 的统计数据", yesterday);

        try {
            // 计算PV（页面浏览量）
            int pv = calculatePV(yesterday);
            log.info("PV: {}", pv);

            // 计算UV（独立访客数）
            int uv = calculateUV(yesterday);
            log.info("UV: {}", uv);

            // 计算新注册用户数
            int newUsers = calculateNewUsers(yesterday);
            log.info("新注册用户: {}", newUsers);

            // 检查是否已存在该日期的统计记录
            LambdaQueryWrapper<SiteStatistics> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SiteStatistics::getStatisticDate, yesterday);
            SiteStatistics existing = siteStatisticsMapper.selectOne(queryWrapper);

            if (existing != null) {
                // 更新已存在的记录
                existing.setPv(pv);
                existing.setUv(uv);
                existing.setNewUsers(newUsers);
                siteStatisticsMapper.updateById(existing);
                log.info("更新日期 {} 的统计记录成功", yesterday);
            } else {
                // 创建新的统计记录
                SiteStatistics statistics = new SiteStatistics();
                statistics.setStatisticDate(yesterday);
                statistics.setPv(pv);
                statistics.setUv(uv);
                statistics.setNewUsers(newUsers);
                siteStatisticsMapper.insert(statistics);
                log.info("创建日期 {} 的统计记录成功", yesterday);
            }

        } catch (Exception e) {
            log.error("聚合日期 {} 的统计数据失败", yesterday, e);
            throw e;
        }
    }

    /**
     * 计算指定日期的PV（页面浏览量）
     *
     * @param date 日期
     * @return PV值
     */
    private int calculatePV(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        LambdaQueryWrapper<AccessLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(AccessLog::getCreatedAt, startOfDay)
                .lt(AccessLog::getCreatedAt, endOfDay);

        return Math.toIntExact(accessLogMapper.selectCount(queryWrapper));
    }

    /**
     * 计算指定日期的UV（独立访客数）
     * 注意：为便于本地测试，暂不对IP地址去重，UV等于PV
     *
     * @param date 日期
     * @return UV值
     */
    private int calculateUV(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        LambdaQueryWrapper<AccessLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(AccessLog::getCreatedAt, startOfDay)
                .lt(AccessLog::getCreatedAt, endOfDay);

        // 直接返回访问记录总数，不进行IP去重（便于本地测试）
        return Math.toIntExact(accessLogMapper.selectCount(queryWrapper));
    }

    /**
     * 计算指定日期的新注册用户数
     *
     * @param date 日期
     * @return 新用户数
     */
    private int calculateNewUsers(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(User::getCreatedAt, startOfDay)
                .lt(User::getCreatedAt, endOfDay);

        return Math.toIntExact(userMapper.selectCount(queryWrapper));
    }

    /**
     * 获取最近N天的统计数据（包含今天）
     *
     * @param days 天数
     * @return 统计数据列表
     */
    public List<SiteStatistics> getRecentStatistics(int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        LambdaQueryWrapper<SiteStatistics> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(SiteStatistics::getStatisticDate, startDate)
                .le(SiteStatistics::getStatisticDate, endDate)
                .orderByAsc(SiteStatistics::getStatisticDate);

        return siteStatisticsMapper.selectList(queryWrapper);
    }

    /**
     * 聚合最近N天的统计数据（包含今天）
     * 用于管理员手动刷新或进入面板时自动更新
     *
     * @param days 天数
     */
    @Transactional
    public void aggregateRecentStatistics(int days) {
        LocalDate today = LocalDate.now();

        // 从今天开始统计（i=0），向前推 days 天
        for (int i = 0; i < days; i++) {
            LocalDate date = today.minusDays(i);
            try {
                aggregateStatisticsForDate(date);
            } catch (Exception e) {
                log.error("聚合日期 {} 的统计数据失败", date, e);
            }
        }
    }

    /**
     * 聚合指定日期的统计数据
     *
     * @param date 日期
     */
    private void aggregateStatisticsForDate(LocalDate date) {
        // 计算PV（页面浏览量）
        int pv = calculatePV(date);

        // 计算UV（独立访客数）
        int uv = calculateUV(date);

        // 计算新注册用户数
        int newUsers = calculateNewUsers(date);

        // 检查是否已存在该日期的统计记录
        LambdaQueryWrapper<SiteStatistics> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SiteStatistics::getStatisticDate, date);
        SiteStatistics existing = siteStatisticsMapper.selectOne(queryWrapper);

        if (existing != null) {
            // 更新已存在的记录
            existing.setPv(pv);
            existing.setUv(uv);
            existing.setNewUsers(newUsers);
            siteStatisticsMapper.updateById(existing);
        } else {
            // 创建新的统计记录
            SiteStatistics statistics = new SiteStatistics();
            statistics.setStatisticDate(date);
            statistics.setPv(pv);
            statistics.setUv(uv);
            statistics.setNewUsers(newUsers);
            siteStatisticsMapper.insert(statistics);
        }

        log.info("聚合日期 {} 的统计数据成功: PV={}, UV={}, NewUsers={}", date, pv, uv, newUsers);
    }
}
