package com.cskaoyan.gateway.controller.user;

import com.mall.commons.result.ResponseData;
import com.mall.commons.result.ResponseUtil;
import com.mall.user.intercepter.TokenIntercepter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//用户退出
@RestController
@RequestMapping("/user")
public class LoginOutController {
    @GetMapping("/loginOut")
    public ResponseData loginOut(HttpServletRequest request, HttpServletResponse response){
        Cookie[] cookies = request.getCookies();
        if(cookies!=null){
            for (Cookie cookie : cookies) {
                if(cookie.getName().equals(TokenIntercepter.ACCESS_TOKEN)){
                    cookie.setValue(null);
                    cookie.setMaxAge(0);  //让该cookie立即过期
                    cookie.setPath("/");
                    response.addCookie(cookie);//覆盖原来的cookie
                }
            }
        }
        return new ResponseUtil<>().setData(null);
    }
}
