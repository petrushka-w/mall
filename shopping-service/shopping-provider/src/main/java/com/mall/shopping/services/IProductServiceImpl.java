package com.mall.shopping.services;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mall.shopping.IProductService;
import com.mall.shopping.constant.GlobalConstants;
import com.mall.shopping.constants.ShoppingRetCode;
import com.mall.shopping.converter.ContentConverter;
import com.mall.shopping.converter.ProductConverter;
import com.mall.shopping.dal.entitys.*;
import com.mall.shopping.dal.persistence.ItemDescMapper;
import com.mall.shopping.dal.persistence.ItemMapper;
import com.mall.shopping.dal.persistence.PanelContentMapper;
import com.mall.shopping.dal.persistence.PanelMapper;
import com.mall.shopping.dto.*;
import com.mall.shopping.services.cache.CacheManager;
import com.mall.shopping.utils.ExceptionProcessorUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
@Service
public class IProductServiceImpl implements IProductService {
    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private ItemDescMapper itemDescMapper;

    @Autowired
    private ProductConverter productConverter;

    @Autowired
    private PanelMapper panelMapper;

    @Autowired
    private PanelContentMapper panelContentMapper;

    @Autowired
    private ContentConverter contentConverter;

    @Override
    public ProductDetailResponse getProductDetail(ProductDetailRequest request) {
        request.requestCheck();
        ProductDetailResponse response = new ProductDetailResponse();
        response.setCode(ShoppingRetCode.SUCCESS.getCode());
        response.setMsg(ShoppingRetCode.SUCCESS.getMessage());
        //查询缓存

        try {
            String checkCache = cacheManager.checkCache(GlobalConstants.PRODUCT_ITEM_CACHE_KEY);
            //缓存非空
            if(StringUtils.isNoneEmpty(checkCache)){
                ProductDetailDto productDetailDto = JSON.parseObject(checkCache, ProductDetailDto.class);
                response.setProductDetailDto(productDetailDto);
                return response;
            }
            //缓存里没有，去数据库查，并添加到缓存。
            Long id = request.getId().longValue();
            Item item = itemMapper.selectByPrimaryKey(id);
            ItemDesc itemDesc = itemDescMapper.selectByPrimaryKey(id);
            ProductDetailDto productDetailDto = new ProductDetailDto();
            productDetailDto.setProductId(item.getId().longValue());
            productDetailDto.setDetail(itemDesc.getItemDesc());
            productDetailDto.setLimitNum(item.getLimitNum()==null?item.getNum().longValue():item.getLimitNum().longValue());
            productDetailDto.setProductName(item.getTitle());
            productDetailDto.setSalePrice(item.getPrice());
            productDetailDto.setSubTitle(item.getSellPoint());

            response.setProductDetailDto(productDetailDto);
            cacheManager.setCache(GlobalConstants.PRODUCT_ITEM_CACHE_KEY,
                    JSON.toJSONString(productDetailDto),
                    GlobalConstants.PRODUCT_ITEM_EXPIRE_TIME);

        }catch (Exception e){
            ExceptionProcessorUtils.wrapperHandlerException(response,e);
        }
        return response;


    }

    @Override
    public AllProductResponse getAllProduct(AllProductRequest request) {
        AllProductResponse response = new AllProductResponse();
        response.setCode(ShoppingRetCode.SUCCESS.getCode());
        response.setMsg(ShoppingRetCode.SUCCESS.getMessage());
        try {
            String orderCol="price";
            String orderDir="asc";
            if(request.getSort().equals("1")){
                orderDir="asc";
            }else{
                orderDir="desc";
            }
            PageHelper.startPage(request.getPage(),request.getSize());
            List<Item> items = itemMapper.selectItemFront(request.getCid(), orderCol,
                    orderDir, request.getPriceGt(), request.getPriceLte());
            List<ProductDto> productDtos = productConverter.items2Dto(items);
            response.setProductDtoList(productDtos);
            PageInfo<Item> itemPageInfo = new PageInfo<Item>(items);
            response.setTotal(itemPageInfo.getTotal());
        }catch (Exception e){
            ExceptionProcessorUtils.wrapperHandlerException(response,e);
        }
        return response;
    }

    @Override
    public RecommendResponse getRecommendGoods() {
        RecommendResponse response = new RecommendResponse();
        response.setCode(ShoppingRetCode.SUCCESS.getCode());
        response.setMsg(ShoppingRetCode.SUCCESS.getMessage());

       try {
           //查看缓存
           String checkCache = cacheManager.checkCache(GlobalConstants.RECOMMEND_PANEL_CACHE_KEY);
           //缓存里有
           if(StringUtils.isNoneEmpty(checkCache)) {
               List<PanelDto> panelDtos = JSON.parseArray(GlobalConstants.RECOMMEND_PANEL_CACHE_KEY, PanelDto.class);
               Set<PanelDto> set = new HashSet<>(panelDtos);
               response.setPanelContentItemDtos(set);
               return response;
           }
           List<Panel> panels = panelMapper.selectPanelContentById(GlobalConstants.RECOMMEND_PANEL_ID);
           if(panels==null||panels.isEmpty()){
               return response;
           }
           Set<PanelDto> res=new HashSet<>();
           for (Panel panel : panels) {
               List<PanelContentItem> panelContentItems = panelContentMapper.selectPanelContentAndProductWithPanelId(panel.getId());
               PanelDto panelDto = contentConverter.panen2Dto(panel);
               panelDto.setPanelContentItems(contentConverter.panelContentItem2Dto(panelContentItems));
               res.add(panelDto);
           }
           response.setPanelContentItemDtos(res);

       }catch (Exception e){
           ExceptionProcessorUtils.wrapperHandlerException(response,e);
       }
       return response;
    }
}
