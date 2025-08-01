package com.gravel.shortcut.domain;

/**
 * @ClassName ConvertRequest
 * @Description: URL转换请求参数
 * @Author gravel  
 * @Date 2020/1/29
 * @Version V1.0
 **/
public class ConvertRequest {
    
    private String url;
    private String password;
    
    public ConvertRequest() {}
    
    public ConvertRequest(String url, String password) {
        this.url = url;
        this.password = password;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}