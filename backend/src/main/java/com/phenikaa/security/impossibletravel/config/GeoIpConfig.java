package com.phenikaa.security.impossibletravel.config;

import com.maxmind.geoip2.DatabaseReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class GeoIpConfig {

    private static final Logger log = LoggerFactory.getLogger(GeoIpConfig.class);

    @Bean
    public DatabaseReader databaseReader() throws IOException {
        ClassPathResource resource = new ClassPathResource("geodb/GeoLite2-City.mmdb");
        if (!resource.exists()) {
            throw new IOException("Không tìm thấy file CSDL MaxMind GeoLite2 tại classpath: geodb/GeoLite2-City.mmdb");
        }

        log.info("Đang nạp cơ sở dữ liệu MaxMind GeoLite2 City từ classpath...");
        try (InputStream inputStream = resource.getInputStream()) {
            DatabaseReader reader = new DatabaseReader.Builder(inputStream).build();
            log.info("Cơ sở dữ liệu MaxMind GeoLite2 City đã được nạp thành công.");
            return reader;
        }
    }
}
