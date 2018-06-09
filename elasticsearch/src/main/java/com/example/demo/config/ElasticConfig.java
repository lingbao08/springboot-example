package com.example.demo.config;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class ElasticConfig {

    @Value("${spring.data.elasticsearch.addresses}")
    private String[] addresses;

    @Value("${spring.data.elasticsearch.cluster-name}")
    private String clusterName;

    public static final int ES_PORT_DEFAULT = 9300;

    @Bean
    public TransportClient esClient() throws Exception {
        Settings settings = Settings.builder()
                .put("cluster.name", clusterName)
                .put("client.transport.sniff", true).build();

//        InetSocketTransportAddress address = new InetSocketTransportAddress(
//                InetAddress.getByName("localhost"), 9300
//        );
        //有多个地址，就仿照上面一直写下去

        TransportClient client = new PreBuiltTransportClient(settings)
                .addTransportAddresses(getAddress());
        //多个地址，就继续add，或者addTransportAddresses来添加一个数组
        return client;
    }

    public InetSocketTransportAddress[] getAddress() throws Exception {

        if (null == addresses || addresses.length == 0)
            throw new Exception("elasticsearch地址错误！");
        InetSocketTransportAddress[] addressArr = new InetSocketTransportAddress[addresses.length];
        for (int i = 0; i < addresses.length; i++) {
            String address = addresses[i];
            String[] split = address.split(":");
            int port = split.length > 1 ? Integer.parseInt(split[1]) : ES_PORT_DEFAULT;
            addressArr[i] =
                    new InetSocketTransportAddress(InetAddress.getByName(split[0]), port);
        }
        return addressArr;
    }
}
