package com.gravel.shortcut.controller;

import com.google.zxing.WriterException;
import com.gravel.shortcut.domain.Result;
import com.gravel.shortcut.domain.ResultGenerator;
import com.gravel.shortcut.domain.ConvertRequest;
import com.gravel.shortcut.domain.RevertRequest;
import com.gravel.shortcut.configuration.SecurityProperties;
import com.gravel.shortcut.service.UrlConvertService;
import com.gravel.shortcut.utils.QRcodeUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * @ClassName MainController
 * @Description: 主控制器
 * @Author gravel
 * @Date 2020/1/29
 * @Version V1.0
 **/
@RestController
public class MainController {

    @Resource
    private UrlConvertService urlConvertService;

    @Resource
    private SecurityProperties securityProperties;

    /**
     * 传入url和密码 返回 转换成功的url
     *
     * @param request 转换请求参数，包含url和密码
     * @return
     */
    @PostMapping("/convert")
    public Result<String> convertUrl(@RequestBody ConvertRequest request) {
        // 检查密码验证
        if (securityProperties.isPasswordEnabled()) {
            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return ResultGenerator.genFailResult("需要提供转换密码");
            }
            if (!securityProperties.isValidPassword(request.getPassword().trim())) {
                return ResultGenerator.genFailResult("转换密码错误");
            }
        }
        
        return ResultGenerator.genSuccessResult(urlConvertService.convertUrl(request.getUrl()));
    }

    @GetMapping(value = "/qrcode", produces = MediaType.IMAGE_JPEG_VALUE)
    public BufferedImage getImage(@RequestParam String url) throws IOException, WriterException {
        return QRcodeUtils.QREncode(url);
    }

    @PostMapping("/revert")
    public Result<String> revertUrl(@RequestBody RevertRequest request) {
        return ResultGenerator.genSuccessResult(urlConvertService.revertUrl(request.getShortUrl()));
    }


    /**
     * 检查是否启用了密码验证
     *
     * @return
     */
    @GetMapping("/password-enabled")
    public Result<Boolean> isPasswordEnabled() {
        return ResultGenerator.genSuccessResult(securityProperties.isPasswordEnabled());
    }

}
