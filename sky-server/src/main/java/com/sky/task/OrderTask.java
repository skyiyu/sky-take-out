package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * 自定义定时任务类  定时处理订单状态
 */
@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 定时任务 处理超时订单的方法
     */
    @Scheduled(cron = "0 * * * * ?")
    public void processTimeoutOrder() {
        log.info("定时处理超时订单：{}", LocalDateTime.now());
        LocalDateTime localDateTime = LocalDateTime.now().plusMinutes(-15);
        List<Orders> ordersList = orderMapper.getBystatusAndOrderTimeLT(Orders.PENDING_PAYMENT, localDateTime);
        if (ordersList != null && ordersList.size() > 0) {
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("订单超时，自动取消");
                orders.setCancelTime(localDateTime.now());
                orderMapper.update(orders);
            }
        }
    }

    /**
     * 处理一直处于派送中状态的订单
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void processDeliveryOrder() {
        log.info("定时处理处于派送中的订单", LocalDateTime.now());
        LocalDateTime localDateTime = LocalDateTime.now().plusHours(1);
        List<Orders> ordersList = orderMapper.getBystatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, localDateTime);
        if (ordersList != null && ordersList.size() > 0) {
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);
            }
        }

    }
}
