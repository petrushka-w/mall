package com.cskaoyan.gateway.controller.user;

import com.alibaba.fastjson.JSON;
import com.mall.commons.result.ResponseData;
import com.mall.commons.result.ResponseUtil;
import com.mall.commons.tool.utils.CookieUtil;
import com.mall.user.IKaptchaService;
import com.mall.user.ILoginService;
import com.mall.user.annotation.Anoymous;
import com.mall.user.constants.SysRetCodeConstants;
import com.mall.user.dto.KaptchaCodeRequest;
import com.mall.user.dto.KaptchaCodeResponse;
import com.mall.user.dto.UserLoginRequest;
import com.mall.user.dto.UserLoginResponse;
import com.mall.user.intercepter.TokenIntercepter;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 用户登录
 */
@RestController
@RequestMapping("/user")
public class LoginController {
    @Reference
    private IKaptchaService iKaptchaService;

    @Reference
    private ILoginService iLoginService;

    @PostMapping("/login")
    @Anoymous
    public ResponseData login(@RequestBody Map<String,String> map, HttpServletRequest request , HttpServletResponse response){
        String userName = map.get("userName");
        String userPwd = map.get("userPwd");
        String captcha = map.get("captcha");


        //验证验证码

        KaptchaCodeRequest kaptchaCodeRequest = new KaptchaCodeRequest();
        String uuid = CookieUtil.getCookieValue(request, "kaptcha_uuid");
        kaptchaCodeRequest.setUuid(uuid);
        kaptchaCodeRequest.setCode(captcha);
        KaptchaCodeResponse kaptchaCodeResponse = iKaptchaService.validateKaptchaCode(kaptchaCodeRequest);
        //验证码错误
        if(!kaptchaCodeResponse.getImageCode().equals(SysRetCodeConstants.SUCCESS.getCode())){
            return new ResponseUtil<>().setErrorMsg(kaptchaCodeResponse.getMsg());
        }

        //验证用户名和密码
        UserLoginRequest userLoginRequest = new UserLoginRequest();
        userLoginRequest.setUserName(userName);
        userLoginRequest.setPassword(userPwd);
        UserLoginResponse loginResponse = iLoginService.login(userLoginRequest);
        //验证成功
        if(loginResponse.getCode().equals(SysRetCodeConstants.SUCCESS.getCode())){
            Cookie cookie = CookieUtil.genCookie(TokenIntercepter.ACCESS_TOKEN, loginResponse.getToken(), "/", 24 * 60 * 60);
            cookie.setHttpOnly(true);
            response.addCookie(cookie);
            return new ResponseUtil<>().setData(loginResponse);
        }
        //验证失败
        return new ResponseUtil<>().setErrorMsg(loginResponse.getMsg());
    }



    //验证用户登录
    @GetMapping("/login")
    public ResponseData loginVerify(HttpServletRequest request,HttpServletResponse response){
        String attribute = (String) request.getAttribute(TokenIntercepter.USER_INFO_KEY);
        Object parse = JSON.parse(attribute);
        return new ResponseUtil<>().setData(parse);
    }

}
