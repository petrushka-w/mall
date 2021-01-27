package com.mall.shopping.services;

import com.alibaba.fastjson.JSON;
import com.mall.shopping.IHomeService;
import com.mall.shopping.constant.GlobalConstants;
import com.mall.shopping.constants.ShoppingRetCode;
import com.mall.shopping.converter.ContentConverter;
import com.mall.shopping.dal.entitys.Panel;
import com.mall.shopping.dal.entitys.PanelContentItem;
import com.mall.shopping.dal.persistence.PanelContentMapper;
import com.mall.shopping.dal.persistence.PanelMapper;
import com.mall.shopping.dto.HomePageResponse;
import com.mall.shopping.dto.PanelContentItemDto;
import com.mall.shopping.dto.PanelDto;
import com.mall.shopping.services.cache.CacheManager;
import com.mall.shopping.utils.ExceptionProcessorUtils;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IHomeServiceImpl implements IHomeService {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    PanelMapper panelMapper;

    @Autowired
    PanelContentMapper panelContentMapper;

    @Autowired
    ContentConverter contentConverter;


    @Override
    public HomePageResponse homepage() {
        HomePageResponse response = new HomePageResponse();
        response.setMsg(ShoppingRetCode.SUCCESS.getMessage());
        response.setCode(ShoppingRetCode.SUCCESS.getCode());

        //检查缓存
        try {
            String checkCache = cacheManager.checkCache(GlobalConstants.HOMEPAGE_CACHE_KEY);
            //如果有缓存,直接返回
            if(StringUtils.isNoneEmpty(checkCache)){
                List<PanelDto> panelDtos = JSON.parseArray(checkCache, PanelDto.class);
                Set<PanelDto> set=new HashSet<>(panelDtos);
                response.setPanelContentItemDtos(set);
                return response;
            }
            //没有缓存，去数据库里拿，并写入缓存
            Example panelexample = new Example(Panel.class);
            Example.Criteria criteria = panelexample.createCriteria();
            criteria.andEqualTo("position",0);
            criteria.andEqualTo("status",1);
            panelexample.setOrderByClause("sort_order");
            List<Panel> panels = panelMapper.selectByExample(panelexample);
            Set<PanelDto> res=new HashSet<>();
            for (Panel panel : panels) {
                PanelDto panelDto = contentConverter.panen2Dto(panel);
                List<PanelContentItem> panelContentItems = panelContentMapper.selectPanelContentAndProductWithPanelId(panel.getId());
                List<PanelContentItemDto> panelContentItemDtos = contentConverter.panelContentItem2Dto(panelContentItems);
                panelDto.setPanelContentItems(panelContentItemDtos);
                res.add(panelDto);
            }
            //加入缓存
            cacheManager.setCache(GlobalConstants.HOMEPAGE_CACHE_KEY,JSON.toJSONString(res),GlobalConstants.HOMEPAGE_EXPIRE_TIME);
            response.setPanelContentItemDtos(res);
        }catch (Exception e){
            ExceptionProcessorUtils.wrapperHandlerException(response,e);
        }
        return response;
    }
}
