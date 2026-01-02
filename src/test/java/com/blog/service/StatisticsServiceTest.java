package com.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.entity.AccessLog;
import com.blog.entity.SiteStatistics;
import com.blog.entity.User;
import com.blog.mapper.AccessLogMapper;
import com.blog.mapper.SiteStatisticsMapper;
import com.blog.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * StatisticsService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    @Mock
    private AccessLogMapper accessLogMapper;

    @Mock
    private SiteStatisticsMapper siteStatisticsMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private StatisticsService statisticsService;

    private List<AccessLog> mockAccessLogs;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        testDate = LocalDate.now().minusDays(1);

        // 准备Mock访问日志
        AccessLog log1 = new AccessLog();
        log1.setId(1L);
        log1.setArticleId(1L);
        log1.setUserId(1L);
        log1.setIpAddress("192.168.1.1");
        log1.setCreatedAt(testDate.atTime(10, 0));

        AccessLog log2 = new AccessLog();
        log2.setId(2L);
        log2.setArticleId(2L);
        log2.setUserId(2L);
        log2.setIpAddress("192.168.1.2");
        log2.setCreatedAt(testDate.atTime(11, 0));

        AccessLog log3 = new AccessLog();
        log3.setId(3L);
        log3.setArticleId(1L);
        log3.setUserId(null);
        log3.setIpAddress("192.168.1.1");  // 重复IP
        log3.setCreatedAt(testDate.atTime(14, 0));

        mockAccessLogs = Arrays.asList(log1, log2, log3);
    }

    @Test
    void testAggregateDailyStatistics_CreateNew() {
        // 模拟不存在统计记录
        when(siteStatisticsMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // 模拟PV计数
        when(accessLogMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(3L);

        // 模拟UV计算（需要返回访问日志列表）
        when(accessLogMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(mockAccessLogs);

        // 模拟新用户计数
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(2L);

        // 模拟插入成功
        when(siteStatisticsMapper.insert(any(SiteStatistics.class))).thenReturn(1);

        // 执行聚合
        assertDoesNotThrow(() -> {
            statisticsService.aggregateDailyStatistics();
        });

        // 验证方法调用
        verify(siteStatisticsMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
        verify(siteStatisticsMapper, times(1)).insert(any(SiteStatistics.class));
        verify(siteStatisticsMapper, never()).updateById(any(SiteStatistics.class));
    }

    @Test
    void testAggregateDailyStatistics_UpdateExisting() {
        // 准备已存在的统计记录
        SiteStatistics existingStats = new SiteStatistics();
        existingStats.setId(1L);
        existingStats.setStatisticDate(testDate);
        existingStats.setPv(100);
        existingStats.setUv(50);
        existingStats.setNewUsers(5);

        // 模拟存在统计记录
        when(siteStatisticsMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existingStats);

        // 模拟PV计数
        when(accessLogMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(3L);

        // 模拟UV计算
        when(accessLogMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(mockAccessLogs);

        // 模拟新用户计数
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(2L);

        // 模拟更新成功
        when(siteStatisticsMapper.updateById(any(SiteStatistics.class))).thenReturn(1);

        // 执行聚合
        assertDoesNotThrow(() -> {
            statisticsService.aggregateDailyStatistics();
        });

        // 验证方法调用
        verify(siteStatisticsMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
        verify(siteStatisticsMapper, times(1)).updateById(any(SiteStatistics.class));
        verify(siteStatisticsMapper, never()).insert(any(SiteStatistics.class));
    }

    @Test
    void testGetRecentStatistics() {
        // 准备Mock统计数据
        SiteStatistics stats1 = new SiteStatistics();
        stats1.setStatisticDate(LocalDate.now().minusDays(2));
        stats1.setPv(100);
        stats1.setUv(50);

        SiteStatistics stats2 = new SiteStatistics();
        stats2.setStatisticDate(LocalDate.now().minusDays(1));
        stats2.setPv(150);
        stats2.setUv(60);

        List<SiteStatistics> mockStats = Arrays.asList(stats1, stats2);

        // 模拟查询返回
        when(siteStatisticsMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(mockStats);

        // 执行查询
        List<SiteStatistics> result = statisticsService.getRecentStatistics(7);

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(100, result.get(0).getPv());
        assertEquals(150, result.get(1).getPv());

        // 验证方法调用
        verify(siteStatisticsMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    void testCalculateUV_WithDuplicateIPs() {
        // 模拟返回包含重复IP的访问日志
        when(accessLogMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(mockAccessLogs);

        // 通过聚合统计来间接测试UV计算
        when(siteStatisticsMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(accessLogMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(3L);
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(2L);
        when(siteStatisticsMapper.insert(any(SiteStatistics.class))).thenAnswer(invocation -> {
            SiteStatistics stats = invocation.getArgument(0);
            // UV应该是2（去重后的IP数量：192.168.1.1 和 192.168.1.2）
            assertEquals(2, stats.getUv());
            return 1;
        });

        // 执行聚合
        statisticsService.aggregateDailyStatistics();

        // 验证insert被调用
        verify(siteStatisticsMapper, times(1)).insert(any(SiteStatistics.class));
    }

    @Test
    void testAggregateDailyStatistics_WithEmptyLogs() {
        // 模拟没有访问日志
        when(siteStatisticsMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(accessLogMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(accessLogMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList());
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(siteStatisticsMapper.insert(any(SiteStatistics.class))).thenReturn(1);

        // 执行聚合
        assertDoesNotThrow(() -> {
            statisticsService.aggregateDailyStatistics();
        });

        // 验证统计记录被创建（即使数据为0）
        verify(siteStatisticsMapper, times(1)).insert(any(SiteStatistics.class));
    }
}
