package jpabook.jpashop.api;


import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.domain.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class OrderSimpleApiController {
    private final OrderRepository orderRepository;

    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1(){
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for(Order order : all){
            order.getMember().getName(); //Lazy 강제 초기화
            order.getDelivery().getAddress(); //Lazy 강제 초기화
        }
        return all;
    }

    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2(){
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        List<SimpleOrderDto> result = new ArrayList<>();
        for(Order order : all){
            SimpleOrderDto simpleOrderDto = new SimpleOrderDto(order);
            result.add(simpleOrderDto);
        }
        return result;
    }

    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> ordersV3(){
        List<Order> allWithMemberDelivery = orderRepository.findAllWithMemberDelivery();
        List<SimpleOrderDto> result = new ArrayList<>();
        for(Order order : allWithMemberDelivery){
            SimpleOrderDto simpleOrderDto = new SimpleOrderDto(order);
            result.add(simpleOrderDto);
        }
        return result;
    }

    @Data
    private class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        public SimpleOrderDto(Order order){
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
        }
    }
}
