package com.diplom.mapper;

import com.diplom.domain.model.Product;
import com.diplom.persistance.entity.ProductEntity;
import com.diplom.rest.dto.ProductDto;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-11T17:25:21+0300",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class ProductMapperImpl implements ProductMapper {

    @Override
    public Product restToDomain(ProductDto dto) {
        if ( dto == null ) {
            return null;
        }

        Product product = new Product();

        product.setAvailableQuantity( dto.getAvailableQuantity() );
        product.setDescription( dto.getDescription() );
        product.setName( dto.getName() );
        product.setPrice( dto.getPrice() );

        return product;
    }

    @Override
    public ProductDto domainToRest(Product domain) {
        if ( domain == null ) {
            return null;
        }

        ProductDto productDto = new ProductDto();

        productDto.setAvailableQuantity( domain.getAvailableQuantity() );
        productDto.setDescription( domain.getDescription() );
        productDto.setName( domain.getName() );
        productDto.setPrice( domain.getPrice() );

        return productDto;
    }

    @Override
    public Product persistenceToDomain(ProductEntity entity) {
        if ( entity == null ) {
            return null;
        }

        Product product = new Product();

        product.setAvailableQuantity( entity.getAvailableQuantity() );
        product.setCreatedAt( entity.getCreatedAt() );
        product.setDescription( entity.getDescription() );
        product.setId( entity.getId() );
        product.setName( entity.getName() );
        product.setPhotoKey( entity.getPhotoKey() );
        product.setPhotoUrl( entity.getPhotoUrl() );
        product.setPrice( entity.getPrice() );

        return product;
    }

    @Override
    public ProductEntity domainToPersistence(Product domain) {
        if ( domain == null ) {
            return null;
        }

        ProductEntity productEntity = new ProductEntity();

        productEntity.setAvailableQuantity( domain.getAvailableQuantity() );
        productEntity.setCreatedAt( domain.getCreatedAt() );
        productEntity.setDescription( domain.getDescription() );
        productEntity.setId( domain.getId() );
        productEntity.setName( domain.getName() );
        productEntity.setPhotoKey( domain.getPhotoKey() );
        productEntity.setPhotoUrl( domain.getPhotoUrl() );
        productEntity.setPrice( domain.getPrice() );

        return productEntity;
    }
}
