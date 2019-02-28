package com.alibaba.csp.sentinel.dashboard.datasource.entity;

import lombok.Data;
import org.elasticsearch.common.UUIDs;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;

@Data
@Document(indexName = "sentinel-metric", type = "sentinel")
public class EsMetricEntity implements Serializable {
    @Id
    @Field
    private String id= UUIDs.randomBase64UUID();
    @Field(type = FieldType.Long)
    private Long gmtCreate;
    @Field(type = FieldType.Long)
    private Long gmtModified;
    @Field(type = FieldType.Text)
    private String app;
    @Field(type = FieldType.Long)
    private Long timestamp;
    @Field(type = FieldType.Text)
    private String resource;
    @Field(index = false, type = FieldType.Long)
    private Long passQps;
    @Field(index = false, type = FieldType.Long)
    private Long successQps;
    @Field(index = false, type = FieldType.Long)
    private Long blockQps;
    /**
     * 发生异常的次数
     */
    @Field(index = false, type = FieldType.Long)
    private Long exceptionQps;

    /**
     * 所有successQps的Rt的和。
     */
    @Field(index = false, type = FieldType.Double)
    private double rt;

    /**
     * 本次聚合的总条数
     */
    @Field(index = false, type = FieldType.Integer)
    private int count;
    @Field(index = false, type = FieldType.Integer)
    private int resourceCode;
}
