package com.alibaba.csp.sentinel.dashboard.repository.metric;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.EsMetricEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.MetricEntity;
import com.alibaba.csp.sentinel.dashboard.repository.ElasticRepository;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Repository("elasticMetricsRepository")
public class ElasticMetricsRepository implements MetricsRepository<MetricEntity> {

    @Autowired
    private ElasticRepository repository;

    @Override
    public void save(MetricEntity metric) {
        EsMetricEntity esMetricEntity = new EsMetricEntity();
        BeanUtils.copyProperties(metric, esMetricEntity,"id");
        esMetricEntity.setGmtCreate(metric.getGmtCreate().getTime());
        esMetricEntity.setGmtModified(metric.getGmtModified().getTime());
        esMetricEntity.setTimestamp(metric.getTimestamp().getTime());
        repository.save(esMetricEntity);
    }

    @Override
    public void saveAll(Iterable<MetricEntity> metrics) {
        if (metrics == null) {
            return;
        }
        metrics.forEach(this::save);
    }

    @Override
    public List<MetricEntity> queryByAppAndResourceBetween(String app, String resource, long startTime, long endTime) {
        app = QueryParser.escape(app);
        resource = QueryParser.escape(resource);
        List<EsMetricEntity> metrics = repository
                .getByAppAndResourceAndTimestampBetween(app, resource, startTime,
                        endTime);
        if (metrics == null || metrics.isEmpty()) {
            return Collections.emptyList();
        }
        List<MetricEntity> entities = new ArrayList<>();
        metrics.forEach((metric) -> {
            MetricEntity entity = new MetricEntity();
            BeanUtils.copyProperties(metric, entity);
            entity.setGmtCreate(new Date(metric.getGmtCreate()));
            entity.setGmtModified(new Date(metric.getGmtModified()));
            entity.setTimestamp(new Date(metric.getTimestamp()));
            entities.add(entity);
        });
        return entities;
    }

    public List<String> listResourcesOfApp(String app) {
        if (StringUtil.isBlank(app)) {
            return Collections.emptyList();
        }
        app = QueryParser.escape(app).trim().replace(" ","");
        //查询30分钟以内的资源
        LocalDateTime dateTime = LocalDateTime.now().minusMinutes(10);
        Date start = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
        List<EsMetricEntity> metrics = repository
                .getResourceByAppAndTimestampGreaterThan(app, start.getTime());
        if (metrics == null || metrics.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, MetricEntity> resources = new HashMap<>(32);
        metrics.forEach((metric) -> {
            MetricEntity entity = new MetricEntity();
            BeanUtils.copyProperties(metric, entity);
            String resource = entity.getResource();
            if (resources.containsKey(resource)) {
                MetricEntity exist = resources.get(resource);
                exist.addBlockQps(entity.getBlockQps());
                exist.addPassQps(entity.getPassQps());
                exist.addExceptionQps(entity.getExceptionQps());
                exist.addCount(entity.getCount());
                exist.addRtAndSuccessQps(entity.getRt(), entity.getSuccessQps());
            } else {
                resources.put(resource, MetricEntity.copyOf(entity));
            }
        });

        return resources.entrySet().stream().sorted((o1, o2) -> {
            MetricEntity e1 = o1.getValue();
            MetricEntity e2 = o2.getValue();
            int t = e2.getPassQps().compareTo(e1.getPassQps());
            if (t != 0) {
                return t;
            }
            return e2.getExceptionQps().compareTo(e1.getExceptionQps());
        }).map(Map.Entry::getKey).collect(Collectors.toList());
    }
}
