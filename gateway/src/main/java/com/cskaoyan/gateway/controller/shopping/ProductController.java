package com.cskaoyan.gateway.controller.shopping;

import com.cskaoyan.gateway.form.shopping.PageInfo;
import com.cskaoyan.gateway.form.shopping.PageResponse;
import com.mall.commons.result.ResponseData;
import com.mall.commons.result.ResponseUtil;
import com.mall.shopping.IProductService;
import com.mall.shopping.constants.ShoppingRetCode;
import com.mall.shopping.dto.*;
import com.mall.user.annotation.Anoymous;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/shopping")
public class ProductController {
    @Reference
    private IProductService iProductService;

    @GetMapping("/product/{id}")
    @Anoymous
    /**
     * 查看商品详情
     */
    //@PathVariable是spring3.0的一个新功能：接收请求路径中占位符的值
    public ResponseData product(@PathVariable long id){
        ProductDetailRequest productDetailRequest = new ProductDetailRequest();
        productDetailRequest.setId(id);
        ProductDetailResponse response = iProductService.getProductDetail(productDetailRequest);
        if(response.getCode().equals(ShoppingRetCode.SUCCESS.getCode())){
            return new ResponseUtil<>().setData(response.getProductDetailDto());
        }
        return new ResponseUtil<>().setErrorMsg(response.getMsg());
    }

    /**
     * 分页查询商品
     */
    @GetMapping("/goods")
    @Anoymous
    public ResponseData goods(PageInfo pageInfo){
        AllProductRequest request = new AllProductRequest();
        request.setCid(pageInfo.getCid());
        request.setPage(pageInfo.getPage());
        request.setPriceGt(pageInfo.getPriceGt());
        request.setPriceLte(pageInfo.getPriceLte());
        request.setSize(pageInfo.getSize());
        request.setSort(pageInfo.getSort());
        AllProductResponse response = iProductService.getAllProduct(request);
        if(response.getCode().equals(ShoppingRetCode.SUCCESS.getCode())){
            ResponseUtil<Object> responseUtil = new ResponseUtil<>();
            PageResponse pageResponse = new PageResponse();
            pageResponse.setData(response.getProductDtoList());
            pageResponse.setTotal(response.getTotal());
            return new ResponseUtil<>().setData(pageResponse);

        }
        return new ResponseUtil<>().setErrorMsg(response.getMsg());
    }

    /**
     * 查看推荐商品
     */
    @GetMapping("/recommend")
    public ResponseData recommond(){
        RecommendResponse response = iProductService.getRecommendGoods();
        if(response.getCode().equals(ShoppingRetCode.SUCCESS.getCode())){
            return new ResponseUtil<>().setData(response.getPanelContentItemDtos());
        }
        return new ResponseUtil<>().setData(response.getMsg());
    }
}
