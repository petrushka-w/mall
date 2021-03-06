package com.mall.order.services;

import com.mall.order.OrderCoreService;
import com.mall.order.biz.TransOutboundInvoker;
import com.mall.order.biz.context.AbsTransHandlerContext;
import com.mall.order.biz.factory.OrderProcessPipelineFactory;
import com.mall.order.constant.OrderRetCode;
import com.mall.order.constants.OrderConstants;
import com.mall.order.dal.entitys.Order;
import com.mall.order.dal.entitys.OrderItem;
import com.mall.order.dal.persistence.OrderItemMapper;
import com.mall.order.dal.persistence.OrderMapper;
import com.mall.order.dal.persistence.OrderShippingMapper;
import com.mall.order.dto.*;
import com.mall.order.utils.ExceptionProcessorUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;

/**
 *  ciggar
 * create-date: 2019/7/30-上午10:05
 */
@Slf4j
@Component
@Service(cluster = "failfast")
public class OrderCoreServiceImpl implements OrderCoreService {

	@Autowired
	OrderMapper orderMapper;

	@Autowired
	OrderItemMapper orderItemMapper;

	@Autowired
	OrderShippingMapper orderShippingMapper;

	@Autowired
    OrderProcessPipelineFactory orderProcessPipelineFactory;


	/**
	 * 创建订单的处理流程
	 *
	 * @param request
	 * @return
	 */
	@Override
	public CreateOrderResponse createOrder(CreateOrderRequest request) {
		CreateOrderResponse response = new CreateOrderResponse();
		try {
			//创建pipeline对象
			TransOutboundInvoker invoker = orderProcessPipelineFactory.build(request);

			//启动pipeline
			invoker.start(); //启动流程（pipeline来处理）

			//获取处理结果
			AbsTransHandlerContext context = invoker.getContext();

			//把处理结果转换为response
			response = (CreateOrderResponse) context.getConvert().convertCtx2Respond(context);
		} catch (Exception e) {
			log.error("OrderCoreServiceImpl.createOrder Occur Exception :" + e);
			ExceptionProcessorUtils.wrapperHandlerException(response, e);
		}
		return response;
	}

	/**
	 * 取消订单
	 * @param request
	 * @return
	 */
	@Override
	public CancelOrderResponse cancelOrder(CancelOrderRequest request) {
		CancelOrderResponse response = new CancelOrderResponse();
		try {
			request.requestCheck();
			response.setMsg(OrderRetCode.SUCCESS.getMessage());
			response.setCode(OrderRetCode.SUCCESS.getCode());
			updateOrder(OrderConstants.ORDER_STATUS_TRANSACTION_CANCEL,request.getOrderId());
		}catch (Exception e){
			ExceptionProcessorUtils.wrapperHandlerException(response,e);
		}
		return response;
	}

	/**
	 * 删除订单
	 * @param request
	 * @return
	 */
	@Override
	public DeleteOrderResponse deleteOrder(DeleteOrderRequest request) {
		DeleteOrderResponse response = new DeleteOrderResponse();
		try {
			request.requestCheck();
			response.setCode(OrderRetCode.SUCCESS.getCode());
			response.setMsg(OrderRetCode.SUCCESS.getCode());
			deleteOrderWithTransaction(request);
		}catch (Exception e){
			ExceptionProcessorUtils.wrapperHandlerException(response,e);
		}
		return response;
	}

	@Override
	public void updateOrder(Integer status, String orderId) {
		Order order = new Order();
		order.setOrderId(orderId);
		order.setStatus(status);
		order.setCloseTime(new Date());
		orderMapper.updateByPrimaryKeySelective(order);
	}

	/**、
	 * 删除订单的事务处理流程
	 * @param request
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteOrderWithTransaction(DeleteOrderRequest request) {
		String orderId = request.getOrderId();
		orderMapper.deleteByPrimaryKey(orderId);
		Example example = new Example(OrderItem.class);
		example.createCriteria().andEqualTo("orderId",orderId);
		orderItemMapper.deleteByExample(example);
		orderShippingMapper.deleteByPrimaryKey(orderId);
	}

}
