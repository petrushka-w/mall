package com.mall.shopping.services;

import com.alibaba.fastjson.JSON;
import com.mall.shopping.IProductCateService;
import com.mall.shopping.constant.GlobalConstants;
import com.mall.shopping.constants.ShoppingRetCode;
import com.mall.shopping.converter.ProductCateConverter;
import com.mall.shopping.dal.entitys.ItemCat;
import com.mall.shopping.dal.persistence.ItemCatMapper;
import com.mall.shopping.dto.AllProductCateRequest;
import com.mall.shopping.dto.AllProductCateResponse;
import com.mall.shopping.dto.ProductCateDto;
import com.mall.shopping.services.cache.CacheManager;
import com.mall.shopping.utils.ExceptionProcessorUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
@Service
public class IProductCateServiceImpl implements IProductCateService {
    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private ProductCateConverter productCateConverter;

    @Autowired
    private ItemCatMapper itemCatMapper;




    @Override
    public AllProductCateResponse getAllProductCate(AllProductCateRequest request) {
        AllProductCateResponse response = new AllProductCateResponse();
        response.setMsg(ShoppingRetCode.SUCCESS.getMessage());
        response.setCode(ShoppingRetCode.SUCCESS.getCode());
        try {
            String checkCache = cacheManager.checkCache(GlobalConstants.PRODUCT_CATE_CACHE_KEY);
            //缓存非空
            if(StringUtils.isNoneEmpty(checkCache)){
                List<ProductCateDto> productCateDtos = JSON.parseArray(checkCache, ProductCateDto.class);
                response.setProductCateDtoList(productCateDtos);
                return response;
            }
            List<ItemCat> itemCats = itemCatMapper.selectAll();
            List<ProductCateDto> productCateDtos = productCateConverter.items2Dto(itemCats);
            response.setProductCateDtoList(productCateDtos);
            //添加缓存
            cacheManager.setCache(GlobalConstants.PRODUCT_CATE_CACHE_KEY,JSON.toJSONString(productCateDtos),GlobalConstants.PRODUCT_CATE_EXPIRE_TIME);
        }catch (Exception e){
            ExceptionProcessorUtils.wrapperHandlerException(response,e);
        }
        return response;
    }
}
