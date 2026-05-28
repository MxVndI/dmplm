package com.diplom.mapper;

import com.diplom.domain.model.Order;
import com.diplom.persistance.entity.OrderEntity;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-11T17:25:21+0300",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class OrderMapperImpl implements OrderMapper {

    @Override
    public Order persistenceToDomain(OrderEntity entity) {
        if ( entity == null ) {
            return null;
        }

        Order order = new Order();

        order.setCreatedAt( entity.getCreatedAt() );
        order.setId( entity.getId() );
        order.setItems( orderItemListToOrderItemList( entity.getItems() ) );
        order.setStatus( entity.getStatus() );
        order.setTestId( entity.getTestId() );
        order.setTotalPrice( entity.getTotalPrice() );
        order.setUserId( entity.getUserId() );
        order.setVariant( entity.getVariant() );

        return order;
    }

    @Override
    public OrderEntity domainToPersistence(Order domain) {
        if ( domain == null ) {
            return null;
        }

        OrderEntity orderEntity = new OrderEntity();

        orderEntity.setCreatedAt( domain.getCreatedAt() );
        orderEntity.setId( domain.getId() );
        orderEntity.setItems( orderItemListToOrderItemList1( domain.getItems() ) );
        orderEntity.setStatus( domain.getStatus() );
        orderEntity.setTestId( domain.getTestId() );
        orderEntity.setTotalPrice( domain.getTotalPrice() );
        orderEntity.setUserId( domain.getUserId() );
        orderEntity.setVariant( domain.getVariant() );

        return orderEntity;
    }

    protected Order.OrderItem orderItemToOrderItem(OrderEntity.OrderItem orderItem) {
        if ( orderItem == null ) {
            return null;
        }

        Order.OrderItem orderItem1 = new Order.OrderItem();

        orderItem1.setPrice( orderItem.getPrice() );
        orderItem1.setProductId( orderItem.getProductId() );
        orderItem1.setProductName( orderItem.getProductName() );
        orderItem1.setQuantity( orderItem.getQuantity() );

        return orderItem1;
    }

    protected List<Order.OrderItem> orderItemListToOrderItemList(List<OrderEntity.OrderItem> list) {
        if ( list == null ) {
            return null;
        }

        List<Order.OrderItem> list1 = new ArrayList<Order.OrderItem>( list.size() );
        for ( OrderEntity.OrderItem orderItem : list ) {
            list1.add( orderItemToOrderItem( orderItem ) );
        }

        return list1;
    }

    protected OrderEntity.OrderItem orderItemToOrderItem1(Order.OrderItem orderItem) {
        if ( orderItem == null ) {
            return null;
        }

        OrderEntity.OrderItem orderItem1 = new OrderEntity.OrderItem();

        orderItem1.setPrice( orderItem.getPrice() );
        orderItem1.setProductId( orderItem.getProductId() );
        orderItem1.setProductName( orderItem.getProductName() );
        orderItem1.setQuantity( orderItem.getQuantity() );

        return orderItem1;
    }

    protected List<OrderEntity.OrderItem> orderItemListToOrderItemList1(List<Order.OrderItem> list) {
        if ( list == null ) {
            return null;
        }

        List<OrderEntity.OrderItem> list1 = new ArrayList<OrderEntity.OrderItem>( list.size() );
        for ( Order.OrderItem orderItem : list ) {
            list1.add( orderItemToOrderItem1( orderItem ) );
        }

        return list1;
    }
}
