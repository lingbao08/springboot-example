package com.example.demo.config;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Configuration
public class ElasticConfig {

    @Bean
    public TransportClient esClient() throws UnknownHostException {
        Settings settings = Settings.builder()
                .put("cluster.name", "elasticsearch")
                .put("client.transport.sniff", true).build();

        InetSocketTransportAddress address = new InetSocketTransportAddress(
                InetAddress.getByName("localhost"),9300
        );
        //有多个地址，就仿照上面一直写下去

        TransportClient client = new PreBuiltTransportClient(settings)
                .addTransportAddress(address);
        //多个地址，就继续add，或者addTransportAddresses来添加一个数组
        return client;
    }
}
