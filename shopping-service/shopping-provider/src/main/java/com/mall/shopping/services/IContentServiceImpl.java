package com.mall.shopping.services;

import com.mall.shopping.IContentService;
import com.mall.shopping.constant.GlobalConstants;
import com.mall.shopping.constants.ShoppingRetCode;
import com.mall.shopping.converter.ContentConverter;
import com.mall.shopping.dal.entitys.PanelContent;
import com.mall.shopping.dal.persistence.PanelContentMapper;
import com.mall.shopping.dto.NavListResponse;
import com.mall.shopping.dto.PanelContentDto;
import com.mall.shopping.services.cache.CacheManager;
import com.mall.shopping.utils.ExceptionProcessorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

public class IContentServiceImpl implements IContentService {
    @Autowired
    private PanelContentMapper panelContentMapper;

    @Autowired
    private ContentConverter contentConverter;


    @Override
    public NavListResponse queryNavList() {
        NavListResponse response = new NavListResponse();
        response.setCode(ShoppingRetCode.SUCCESS.getCode());
        response.setMsg(ShoppingRetCode.SUCCESS.getMessage());
        try {
            Example example = new Example(PanelContent.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("panelId",GlobalConstants.HEADER_PANEL_CACHE_KEY);
            example.setOrderByClause("sort_order");
            List<PanelContent> panelContents = panelContentMapper.selectByExample(example);
            List<PanelContentDto> panelContentDtos = contentConverter.panelContents2Dto(panelContents);
            response.setPannelContentDtos(panelContentDtos);
        }catch (Exception e){
            ExceptionProcessorUtils.wrapperHandlerException(response,e);
        }
        return response;


    }
}
