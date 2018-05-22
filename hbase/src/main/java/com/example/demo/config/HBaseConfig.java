package com.example.demo.config;

/**
 * @author lingbao08
 * @DESCRIPTION Hbase配置类
 * @create 2018/5/22 18:14
 **/

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.hadoop.hbase.HbaseTemplate;


@Configuration
public class HBaseConfig {

    //HBase Zookeeper主机IP和IP
    @Value("${spring.datasource.third.zkQuorum}")
    private String zkQuorum;

    private static final Logger logger = LoggerFactory.getLogger(HBaseConfig.class);

    /**
     * @Description: 获取hbaseTemplate
     * @Param:
     * @Reutrn: org.springframework.data.hadoop.hbase.HbaseTemplate
     * @Date: 2018/5/22 17:15
     */

    @Bean(name = "hbaseTemplate")
    @Qualifier("hbaseTemplate")
    public HbaseTemplate hbaseTemplate() {
        org.apache.hadoop.conf.Configuration configuration = HBaseConfiguration.create();
        configuration.set("hbase.zookeeper.quorum", zkQuorum);
        logger.debug("HBase HbaseTemplate");
        return new HbaseTemplate(configuration);
    }
}
