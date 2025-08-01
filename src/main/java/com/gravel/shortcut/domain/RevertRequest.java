package com.gravel.shortcut.domain;

/**
 * @ClassName RevertRequest
 * @Description: URL还原请求参数
 * @Author gravel
 * @Date 2020/1/29
 * @Version V1.0
 **/
public class RevertRequest {
    
    private String shortUrl;
    
    public RevertRequest() {}
    
    public RevertRequest(String shortUrl) {
        this.shortUrl = shortUrl;
    }
    
    public String getShortUrl() {
        return shortUrl;
    }
    
    public void setShortUrl(String shortUrl) {
        this.shortUrl = shortUrl;
    }
}