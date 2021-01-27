package com.cskaoyan.gateway.controller.shopping;

import com.mall.commons.result.ResponseData;
import com.mall.commons.result.ResponseUtil;
import com.mall.shopping.IProductCateService;
import com.mall.shopping.constants.ShoppingRetCode;
import com.mall.shopping.dto.AllProductCateRequest;
import com.mall.shopping.dto.AllProductCateResponse;
import com.mall.user.annotation.Anoymous;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 查看所有商品分类
 */
@RestController
@RequestMapping("/shopping")
public class CategoriesController {
    @Reference
    private IProductCateService iProductCateService;

    @GetMapping("/categories")
    @Anoymous
    public ResponseData categories(){
        AllProductCateRequest allProductCateRequest = new AllProductCateRequest();
        AllProductCateResponse response = iProductCateService.getAllProductCate(allProductCateRequest);
        if(response.getCode().equals(ShoppingRetCode.SUCCESS.getCode())){
            return new ResponseUtil<>().setData(response.getProductCateDtoList());
        }
        return new ResponseUtil<>().setErrorMsg(response.getMsg());
    }
}
