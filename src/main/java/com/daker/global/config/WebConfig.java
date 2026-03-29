package com.daker.global.config;

import com.daker.domain.hackathon.domain.HackathonStatus;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToHackathonStatusConverter());
    }

    static class StringToHackathonStatusConverter implements Converter<String, HackathonStatus> {
        @Override
        public HackathonStatus convert(String value) {
            return HackathonStatus.valueOf(value.toUpperCase());
        }
    }
}
