package com.ecommerce.loadtest.service.impl;

import com.ecommerce.loadtest.dao.TestBatchMapper;
import com.ecommerce.loadtest.entity.TestBatch;
import com.ecommerce.loadtest.service.TestBatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 测试批次服务实现类
 * 
 * @author rakkaus
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class TestBatchServiceImpl implements TestBatchService {
    
    private static final Logger logger = LoggerFactory.getLogger(TestBatchServiceImpl.class);
    
    @Autowired
    private TestBatchMapper testBatchMapper;
    
    @Override
    public int deleteByPrimaryKey(Long batchId) {
        logger.info("删除测试批次 - batchId: {}", batchId);
        try {
            return testBatchMapper.deleteByPrimaryKey(batchId);
        } catch (Exception e) {
            logger.error("删除批次失败 - batchId: {}", batchId, e);
            throw new RuntimeException("删除批次失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Long insertSelective(TestBatch record) {
        logger.info("插入测试批次 - createUser: {}, totalCount: {}", record.getCreateUser(), record.getTotalCount());
        try {
            if (record.getCreateTime() == null) {
                record.setCreateTime(System.currentTimeMillis());
            }
            if (record.getBatchStatus() == null) {
                record.setBatchStatus(0);
            }
            if (record.getRealCount() == null) {
                record.setRealCount(0L);
            }
            testBatchMapper.insertSelective(record);
            return record.getBatchId();
        } catch (Exception e) {
            logger.error("插入批次失败 - createUser: {}", record.getCreateUser(), e);
            throw new RuntimeException("插入批次失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public TestBatch selectByPrimaryKey(Long batchId) {
        logger.debug("查询测试批次 - batchId: {}", batchId);
        try {
            return testBatchMapper.selectByPrimaryKey(batchId);
        } catch (Exception e) {
            logger.error("查询批次失败 - batchId: {}", batchId, e);
            throw new RuntimeException("查询批次失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public int updateByPrimaryKeySelective(TestBatch record) {
        logger.info("更新测试批次 - batchId: {}", record.getBatchId());
        try {
            record.setUpdateTime(System.currentTimeMillis());
            return testBatchMapper.updateByPrimaryKeySelective(record);
        } catch (Exception e) {
            logger.error("更新批次失败 - batchId: {}", record.getBatchId(), e);
            throw new RuntimeException("更新批次失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TestBatch> selectBatchListByStatus(Integer batchStatus) {
        logger.info("按状态查询批次 - batchStatus: {}", batchStatus);
        try {
            return testBatchMapper.selectBatchListByStatus(batchStatus);
        } catch (Exception e) {
            logger.error("按状态查询批次失败 - batchStatus: {}", batchStatus, e);
            throw new RuntimeException("按状态查询批次失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getBatchStatistics() {
        logger.info("获取批次统计信息");
        try {
            return testBatchMapper.selectBatchStatistics("");
        } catch (Exception e) {
            logger.error("获取批次统计信息失败", e);
            throw new RuntimeException("获取批次统计信息失败: " + e.getMessage(), e);
        }
    }
}

