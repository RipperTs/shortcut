package com.gravel.shortcut.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @ClassName SecurityProperties
 * @Description: 安全配置属性
 * @Author gravel
 * @Date 2020/1/29
 * @Version V1.0
 **/
@Component
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

    /**
     * 转换密码列表，如果配置了密码则前端转换时需要提供密码
     */
    private List<String> passwords;

    public List<String> getPasswords() {
        return passwords;
    }

    public void setPasswords(List<String> passwords) {
        this.passwords = passwords;
    }

    /**
     * 检查密码是否有效
     *
     * @param password 待验证的密码
     * @return 是否有效
     */
    public boolean isValidPassword(String password) {
        if (passwords == null || passwords.isEmpty()) {
            // 如果没有配置密码，则不需要验证
            return true;
        }
        return passwords.contains(password);
    }

    /**
     * 是否启用了密码验证
     *
     * @return 是否启用
     */
    public boolean isPasswordEnabled() {
        return passwords != null && !passwords.isEmpty();
    }
}