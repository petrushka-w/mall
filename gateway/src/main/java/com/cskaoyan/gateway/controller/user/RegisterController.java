package com.cskaoyan.gateway.controller.user;

import com.mall.commons.result.ResponseData;
import com.mall.commons.result.ResponseUtil;
import com.mall.commons.tool.utils.CookieUtil;
import com.mall.user.IKaptchaService;
import com.mall.user.IRegisterService;
import com.mall.user.annotation.Anoymous;
import com.mall.user.constants.SysRetCodeConstants;
import com.mall.user.dto.KaptchaCodeRequest;
import com.mall.user.dto.KaptchaCodeResponse;
import com.mall.user.dto.UserRegisterRequest;
import com.mall.user.dto.UserRegisterResponse;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 用户注册
 */
@RestController
@RequestMapping("/user")
public class RegisterController {
    @Reference
    private IKaptchaService iKaptchaService;

    @Reference
    private IRegisterService iRegisterService;

    @PostMapping("/register")
    @Anoymous
    public ResponseData register(@RequestBody Map<String,String> map, HttpServletRequest request){
        String username = map.get("userName");
        String password = map.get("userPwd");
        String captcha = map.get("captcha");
        String email = map.get("email");

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

        //向用户表插入记录
        UserRegisterRequest userRegisterRequest = new UserRegisterRequest();
        userRegisterRequest.setEmail(email);
        userRegisterRequest.setUserName(username);
        userRegisterRequest.setUserPwd(password);
        UserRegisterResponse registerResponse = iRegisterService.register(userRegisterRequest);

        if(registerResponse.getCode().equals(SysRetCodeConstants.SUCCESS.getCode())){
            return new ResponseUtil<>().setData(null);
        }

        return new ResponseUtil<>().setErrorMsg(registerResponse.getMsg());
    }
}
