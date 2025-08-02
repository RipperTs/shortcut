package com.gravel.shortcut.service;

/**
 * @ClassName UrlConvertService
 * @Description: TODO
 * @Author gravel
 * @Date 2020/1/29
 * @Version V1.0
 **/
public interface UrlConvertService {

    /**
     * 得到短地址URL
     *
     * @param url
     * @return
     */
    String convertUrl(String url);

    /**
     * 得到短地址URL（带过期时间）
     *
     * @param url
     * @param expireTime 过期时间（秒）
     * @return
     */
    String convertUrl(String url, Long expireTime);

    /**
     * 将短地址URL 转换为正常的地址
     *
     * @param shortUrl
     * @return
     */
    String revertUrl(String shortUrl);


}
