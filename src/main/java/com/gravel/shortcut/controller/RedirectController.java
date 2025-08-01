package com.gravel.shortcut.controller;

import com.gravel.shortcut.service.UrlConvertService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @ClassName RedirectController
 * @Description: 重定向控制器
 * @Author gravel
 * @Date 2020/1/29
 * @Version V1.0
 **/

@Controller
public class RedirectController {

    @Resource
    private UrlConvertService urlConvertService;

    /**
     * 302 重定向到新的地址
     *
     * @param request
     * @param response
     * @return
     */
    @GetMapping("/*")
    public Object redirect(HttpServletRequest request, HttpServletResponse response) {
        String shortcut = request.getServletPath().substring(1);
        
        // 排除静态资源请求
        if (shortcut.contains(".")) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "error";
        }
        
        try {
            String url = urlConvertService.revertUrl(shortcut);
            
            // 如果找不到对应的URL，返回错误页面
            if (url == null || url.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return "error";
            }
            
            return new RedirectView(url);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "error";
        }
    }

}
