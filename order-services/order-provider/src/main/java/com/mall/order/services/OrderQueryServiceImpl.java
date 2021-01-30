package com.mall.order.services;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mall.order.OrderQueryService;
import com.mall.order.constant.OrderRetCode;
import com.mall.order.converter.OrderConverter;
import com.mall.order.dal.entitys.*;
import com.mall.order.dal.persistence.OrderItemMapper;
import com.mall.order.dal.persistence.OrderMapper;
import com.mall.order.dal.persistence.OrderShippingMapper;
import com.mall.order.dto.*;
import com.mall.order.utils.ExceptionProcessorUtils;
import com.mall.shopping.constants.ShoppingRetCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;

/**
 *  ciggar
 * create-date: 2019/7/30-上午10:04
 */
@Slf4j
@Component
@Service
public class OrderQueryServiceImpl implements OrderQueryService {
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private OrderConverter orderConverter;

    @Autowired
    private OrderShippingMapper orderShippingMapper;

    @Override
    public OrderCountResponse orderCount(OrderCountRequest request) {
        return null;
    }

    @Override
    public OrderListResponse orderList(OrderListRequest request) {
        OrderListResponse response = new OrderListResponse();
        try{
            request.requestCheck();
            response.setCode(ShoppingRetCode.SUCCESS.getCode());
            response.setMsg(ShoppingRetCode.SUCCESS.getMessage());
            PageHelper.startPage(request.getPage(),request.getSize());
            Example example = new Example(Order.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("userId",request.getUserId());

            //PageHelper紧跟的第一个方法会被分页
            List<Order> orders = orderMapper.selectByExample(example);
            if(CollectionUtils.isEmpty(orders)){
                response.setDetailInfoList(new ArrayList<>());
                response.setTotal((long) 0);
                return response;
            }
            List<OrderDetailInfo> infos=new ArrayList<>();
            for(Order order:orders){
                OrderDetailInfo orderDetailInfo = orderConverter.order2detail(order);
                List<OrderItem> orderItems = orderItemMapper.queryByOrderId(orderDetailInfo.getOrderId());
                OrderShipping orderShipping = orderShippingMapper.selectByPrimaryKey(order.getOrderId());
                OrderShippingDto orderShippingDto = orderConverter.shipping2dto(orderShipping);
                orderDetailInfo.setOrderShippingDto(orderShippingDto);
                infos.add(orderDetailInfo);
            }
            response.setDetailInfoList(infos);
            PageInfo<Order> pageInfo=new PageInfo<>();
            response.setTotal(pageInfo.getTotal());
        }catch (Exception e){
            ExceptionProcessorUtils.wrapperHandlerException(response,e);
        }
        return response;
    }

    @Override
    public OrderDetailResponse orderDetail(OrderDetailRequest request) {
        OrderDetailResponse response = new OrderDetailResponse();
        try {
            request.requestCheck();
            response.setCode(OrderRetCode.SUCCESS.getCode());
            response.setMsg(OrderRetCode.SUCCESS.getMessage());
            Order order = orderMapper.selectByPrimaryKey(request.getOrderId());
            List<OrderItem> orderItems = orderItemMapper.queryByOrderId(request.getOrderId());
            OrderShipping orderShipping = orderShippingMapper.selectByPrimaryKey(request.getOrderId());
            response = orderConverter.order2res(order);
            response.setOrderItemDto(orderConverter.item2dto(orderItems));
            response.setOrderShippingDto(orderConverter.shipping2dto(orderShipping));
        }catch (Exception e){
            ExceptionProcessorUtils.wrapperHandlerException(response,e);
        }
        return response;
    }

    @Override
    public OrderItemResponse orderItem(OrderItemRequest request) {
        return null;
    }
}
