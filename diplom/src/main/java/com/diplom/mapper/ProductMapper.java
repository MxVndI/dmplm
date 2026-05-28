package com.diplom.mapper;

import com.diplom.domain.model.Product;
import com.diplom.persistance.entity.ProductEntity;
import com.diplom.rest.dto.ProductDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    // ── DTO ↔ Entity (direct) ─────────────────────────────────────────────
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "photoKey", ignore = true)
    @Mapping(target = "photoUrl", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    ProductEntity toEntity(ProductDto dto);

    ProductDto toDto(ProductEntity entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "photoKey", ignore = true)
    @Mapping(target = "photoUrl", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntityFromDto(ProductDto dto, @MappingTarget ProductEntity entity);

    // ── Legacy domain-layer mappings (kept for backwards compatibility) ───
    Product restToDomain(ProductDto dto);
    ProductDto domainToRest(Product domain);
    Product persistenceToDomain(ProductEntity entity);
    ProductEntity domainToPersistence(Product domain);
}
