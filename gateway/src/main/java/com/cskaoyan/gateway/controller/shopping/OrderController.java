package com.cskaoyan.gateway.controller.shopping;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cskaoyan.gateway.form.shopping.CancelOrderForm;
import com.cskaoyan.gateway.form.shopping.OrderDetail;
import com.cskaoyan.gateway.form.shopping.PageInfo;
import com.cskaoyan.gateway.form.shopping.PageResponse;
import com.mall.commons.result.ResponseData;
import com.mall.commons.result.ResponseUtil;
import com.mall.order.OrderCoreService;
import com.mall.order.OrderQueryService;
import com.mall.order.constant.OrderRetCode;
import com.mall.order.dto.*;
import com.mall.user.intercepter.TokenIntercepter;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

@RestController
@RequestMapping("/shopping")
public class OrderController {
    @Reference
    private OrderCoreService orderCoreService;

    @Reference
    private OrderQueryService orderQueryService;

    //创建订单
    @PostMapping("/order")
    public ResponseData createOrder(@RequestBody CreateOrderRequest request, HttpServletRequest httpServletRequest){
        //设置暴露的id
        request.setUniqueKey(UUID.randomUUID().toString());
        CreateOrderResponse response = orderCoreService.createOrder(request);
        if(response.getCode().equals(OrderRetCode.SUCCESS.getCode())){
            return new ResponseUtil<>().setData(response.getOrderId());
        }
        return new ResponseUtil<>().setErrorMsg(response.getMsg());
    }

    //获取用户的所有订单
    @GetMapping("/order")
    public ResponseData getOrder(PageInfo pageInfo,HttpServletRequest request){
        OrderListRequest orderListRequest = new OrderListRequest();
        orderListRequest.setPage(pageInfo.getPage());
        orderListRequest.setSize(pageInfo.getSize());
        orderListRequest.setSort(pageInfo.getSort());
        //从request中获取userid
        String userinfo = (String) request.getAttribute(TokenIntercepter.USER_INFO_KEY);
        JSONObject jsonObject = JSON.parseObject(userinfo);
        long userId=Long.parseLong(jsonObject.get("uid").toString());
        orderListRequest.setUserId(userId);
        OrderListResponse response = orderQueryService.orderList(orderListRequest);
        if(response.getCode().equals(OrderRetCode.SUCCESS.getCode())){
            PageResponse pageResponse = new PageResponse();
            pageResponse.setData(response.getDetailInfoList());
            pageResponse.setTotal(response.getTotal());
            return new ResponseUtil<>().setData(pageResponse);
        }
        return new ResponseUtil<>().setErrorMsg(response.getMsg());
    }

    /**
     * 根据订单id查询订单详情
     * @param id
     * @return
     */
    @GetMapping("/order/{id}")
    public ResponseData getOrderDetail(@PathVariable String id){
        OrderDetailRequest orderDetailRequest = new OrderDetailRequest();
        orderDetailRequest.setOrderId(id);
        OrderDetailResponse response = orderQueryService.orderDetail(orderDetailRequest);
        if(response.getCode().equals(OrderRetCode.SUCCESS.getCode())){
            OrderDetail orderDetail=new OrderDetail();
            orderDetail.setGoodsList(response.getOrderItemDto());
            orderDetail.setUserId(response.getUserId());
            orderDetail.setUserName(response.getBuyerNick());
            orderDetail.setOrderStatus(response.getStatus());
            orderDetail.setOrderTotal(response.getPayment());
            orderDetail.setStreetName(response.getOrderShippingDto().getReceiverAddress());
            orderDetail.setTel(response.getOrderShippingDto().getReceiverPhone());
            return new ResponseUtil<>().setData(orderDetail);
        }
        return new ResponseUtil<>().setErrorMsg(response.getMsg());
    }

    /**
     *取消订单
     * @param cancelOrderForm
     * @return
     */
    @PostMapping("/cancelOrder")
    public ResponseData cancelOrder(@RequestBody CancelOrderForm cancelOrderForm){
        CancelOrderRequest cancelOrderRequest = new CancelOrderRequest();
        cancelOrderRequest.setOrderId(cancelOrderForm.getOrderId());
        CancelOrderResponse response = orderCoreService.cancelOrder(cancelOrderRequest);

        return new ResponseUtil<>().setData(response);


    }

    /**
     * 删除订单
     * @param id
     * @return
     */
    @DeleteMapping("/order/{id}")
    public ResponseData deleteOrder(@PathVariable String id){
        DeleteOrderRequest deleteOrderRequest = new DeleteOrderRequest();
        deleteOrderRequest.setOrderId(id);
        DeleteOrderResponse response = orderCoreService.deleteOrder(deleteOrderRequest);
        return new ResponseUtil<>().setData(response);
    }
}
