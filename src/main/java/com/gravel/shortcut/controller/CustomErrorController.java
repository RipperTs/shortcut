package com.gravel.shortcut.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @ClassName ErrorController
 * @Description: 错误页面控制器
 * @Author gravel
 * @Date 2025/8/1
 * @Version V1.0
 **/
@Controller
public class CustomErrorController implements ErrorController {

    private static final String ERROR_PATH = "/error";

    @RequestMapping(value = ERROR_PATH)
    public String error(HttpServletRequest request) {
        return "error";
    }

    @Override
    public String getErrorPath() {
        return ERROR_PATH;
    }
}