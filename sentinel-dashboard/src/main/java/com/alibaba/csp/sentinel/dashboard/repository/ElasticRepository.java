package com.alibaba.csp.sentinel.dashboard.repository;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.EsMetricEntity;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ElasticRepository extends ElasticsearchRepository<EsMetricEntity, String> {

    List<EsMetricEntity> getResourceByAppAndTimestampGreaterThan(String app, Long start);

    List<EsMetricEntity> getByAppAndResourceAndTimestampBetween(String app, String resource, Long from, Long to);
}
