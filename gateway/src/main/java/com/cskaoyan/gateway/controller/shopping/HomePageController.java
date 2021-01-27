package com.cskaoyan.gateway.controller.shopping;

import com.mall.commons.result.ResponseData;
import com.mall.commons.result.ResponseUtil;
import com.mall.shopping.IHomeService;
import com.mall.shopping.constants.ShoppingRetCode;
import com.mall.shopping.dto.HomePageResponse;
import com.mall.user.annotation.Anoymous;
import com.mall.user.constants.SysRetCodeConstants;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 查看主页
 */
@RestController
@RequestMapping("/shopping")
public class HomePageController {

    @Reference
    private IHomeService iHomeService;

    @GetMapping("/homepage")
    @Anoymous
    public ResponseData homepage(HttpServletRequest request){

        HomePageResponse homepage = iHomeService.homepage();
        if(homepage.getCode().equals(ShoppingRetCode.SUCCESS.getCode())){
            return new ResponseUtil<>().setData(homepage.getPanelContentItemDtos());
        }
        return new ResponseUtil<>().setErrorMsg(homepage.getMsg());
    }
}
