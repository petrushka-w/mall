package com.cskaoyan.gateway.controller.shopping;

import com.mall.commons.result.ResponseData;
import com.mall.commons.result.ResponseUtil;
import com.mall.shopping.IContentService;
import com.mall.shopping.constants.ShoppingRetCode;
import com.mall.shopping.dto.NavListResponse;
import com.mall.user.annotation.Anoymous;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 查看导航页
 */

@RestController
@RequestMapping("/shopping")
public class NavigationController {
    @Reference
    IContentService iContentService;

    @GetMapping("/navigation")
    @Anoymous
    public ResponseData navigation(HttpServletRequest request){
        NavListResponse navListResponse = iContentService.queryNavList();
        if(navListResponse.getCode().equals(ShoppingRetCode.SUCCESS.getCode())){
            return new ResponseUtil<>().setData(navListResponse.getPannelContentDtos());
        }
        return new ResponseUtil<>().setErrorMsg(navListResponse.getMsg());
    }
}
