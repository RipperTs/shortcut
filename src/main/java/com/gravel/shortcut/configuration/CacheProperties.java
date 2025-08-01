package com.gravel.shortcut.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @ClassName CacheProperties
 * @Description: 缓存配置属性
 * @Author gravel
 * @Date 2020/1/29
 * @Version V1.0
 **/
@Component
@ConfigurationProperties(prefix = "cache")
public class CacheProperties {

    /**
     * Redis缓存key前缀，默认为 shortcut_
     */
    private String prefix = "shortcut_";

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}