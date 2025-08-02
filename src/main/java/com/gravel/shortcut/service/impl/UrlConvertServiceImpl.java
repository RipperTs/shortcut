package com.gravel.shortcut.service.impl;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.gravel.shortcut.configuration.AsyncJob;
import com.gravel.shortcut.configuration.CacheProperties;
import com.gravel.shortcut.configuration.bloom.BloomFilter;
import com.gravel.shortcut.service.UrlConvertService;
import com.gravel.shortcut.utils.NumericConvertUtils;
import com.gravel.shortcut.utils.SnowFlake;
import com.gravel.shortcut.utils.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Base64;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName UrlConvertServiceImpl
 * @Description: 短地址处理service
 * @Author gravel
 * @Date 2020/1/29
 * @Version V1.0
 **/
@Slf4j
@Service
public class UrlConvertServiceImpl implements UrlConvertService {

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private SnowFlake idGenerator;

    @Resource
    private BloomFilter bloomFilter;

    @Resource
    private AsyncJob asyncJob;

    @Resource
    private CacheProperties cacheProperties;

    /**
     * 生成带前缀的Redis key
     *
     * @param key 原始key
     * @return 带前缀的key
     */
    private String buildCacheKey(String key) {
        return cacheProperties.getPrefix() + key;
    }

    /**
     * 得到短地址URL
     *
     * @param encodedUrl base64编码的URL
     * @return
     */
    @Override
    public String convertUrl(String encodedUrl) {
        // 校验和解码base64
        String url = decodeBase64Url(encodedUrl);
        
        Preconditions.checkArgument(Validator.checkUrl(url), "[url]格式不合法！url={}", url);
        log.info("转换开始----->[url]={}", url);
        String shortCut;
        String encodedUrlKey = buildCacheKey(encodedUrl);
        // 如果布隆过滤器能命中，则检查Redis中是否还存在对应的value
        if (bloomFilter.includeByBloomFilter(encodedUrl)) {
            shortCut = redisTemplate.opsForValue().get(encodedUrlKey);
            if (!Strings.isNullOrEmpty(shortCut)) {
                log.info("布隆过滤器命中，短链接仍有效----->[shortCut]={}", shortCut);
                return shortCut;
            } else {
                log.info("布隆过滤器命中但Redis中key已过期，重新生成短链接");
            }
        }
        // 直接生成一个新的短地址，并存入缓存
        long nextId = idGenerator.nextId();
        // 转换为62进制
        shortCut = NumericConvertUtils.convertTo(nextId, 62);
        log.info("转换成功----->[shortCut]={}", shortCut);
        // 将短网址和短域名异步添加到布隆过滤器中，提升响应速度
        // 注意：这里存储的是base64编码的URL
        asyncJob.add2RedisAndBloomFilter(shortCut, encodedUrl, cacheProperties.getPrefix());
        // 存在的话直接返回

        return shortCut;
    }

    /**
     * 得到短地址URL（带过期时间）
     *
     * @param encodedUrl base64编码的URL
     * @param expireTime 过期时间（秒）
     * @return
     */
    @Override
    public String convertUrl(String encodedUrl, Long expireTime) {
        // 校验和解码base64
        String url = decodeBase64Url(encodedUrl);
        
        Preconditions.checkArgument(Validator.checkUrl(url), "[url]格式不合法！url={}", url);
        log.info("转换开始----->[url]={}, expireTime={}", url, expireTime);
        String shortCut;
        String encodedUrlKey = buildCacheKey(encodedUrl);
        
        // 如果没有设置过期时间，使用原有逻辑
        if (expireTime == null || expireTime <= 0) {
            return convertUrl(encodedUrl);
        }
        
        // 如果布隆过滤器能命中，则检查Redis中是否还存在对应的value
        if (bloomFilter.includeByBloomFilter(encodedUrl)) {
            shortCut = redisTemplate.opsForValue().get(encodedUrlKey);
            if (!Strings.isNullOrEmpty(shortCut)) {
                log.info("布隆过滤器命中，短链接仍有效----->[shortCut]={}", shortCut);
                // 重新设置过期时间
                String shortcutKey = buildCacheKey(shortCut);
                redisTemplate.expire(shortcutKey, expireTime, TimeUnit.SECONDS);
                redisTemplate.expire(encodedUrlKey, expireTime, TimeUnit.SECONDS);
                return shortCut;
            } else {
                log.info("布隆过滤器命中但Redis中key已过期，重新生成短链接");
            }
        }
        
        // 直接生成一个新的短地址，并存入缓存
        long nextId = idGenerator.nextId();
        // 转换为62进制
        shortCut = NumericConvertUtils.convertTo(nextId, 62);
        log.info("转换成功----->[shortCut]={}", shortCut);
        
        // 存储映射关系，设置过期时间
        String shortcutKey = buildCacheKey(shortCut);
        redisTemplate.opsForValue().set(shortcutKey, encodedUrl, expireTime, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(encodedUrlKey, shortCut, expireTime, TimeUnit.SECONDS);
        
        // 将短网址和短域名异步添加到布隆过滤器中，提升响应速度
        asyncJob.addToBloomFilter(encodedUrl);
        
        return shortCut;
    }

    /**
     * 解码base64编码的URL
     *
     * @param encodedUrl base64编码的URL
     * @return 解码后的URL
     */
    private String decodeBase64Url(String encodedUrl) {
        try {
            // 校验是否为有效的base64字符串
            if (!isValidBase64(encodedUrl)) {
                throw new IllegalArgumentException("URL参数必须为base64编码格式");
            }
            
            // base64解码
            byte[] decodedBytes = Base64.getDecoder().decode(encodedUrl);
            String decodedStr = new String(decodedBytes, StandardCharsets.UTF_8);
            
            // URL解码
            return URLDecoder.decode(decodedStr, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            throw new IllegalArgumentException("URL参数base64解码失败：" + e.getMessage());
        }
    }

    /**
     * 校验字符串是否为有效的base64格式
     *
     * @param str 待校验字符串
     * @return 是否为有效base64
     */
    private boolean isValidBase64(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Base64.getDecoder().decode(str);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 将短地址URL 转换为正常的地址
     *
     * @param shortUrl
     * @return
     */
    @Override
    public String revertUrl(String shortUrl) {
        log.info("还原开始----->[shortUrl]={}", shortUrl);
        String shortcut = shortUrl.substring(shortUrl.lastIndexOf("/") + 1);
        String shortcutKey = buildCacheKey(shortcut);
        String encodedUrl = redisTemplate.opsForValue().get(shortcutKey);
        if (Strings.isNullOrEmpty(encodedUrl)) {
            log.warn("未找到对应的短地址----->[shortcut]={}", shortcut);
            return null;
        }
        
        // 解码base64编码的URL
        String decodedUrl = decodeBase64Url(encodedUrl);
        log.info("还原成功----->[真实Url]={}", decodedUrl);
        return decodedUrl;
    }

}
