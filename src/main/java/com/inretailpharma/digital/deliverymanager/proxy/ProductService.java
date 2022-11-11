package com.inretailpharma.digital.deliverymanager.proxy;

import java.util.List;

import com.inretailpharma.digital.deliverymanager.dto.ProductDimensionDto;

import reactor.core.publisher.Flux;

public interface ProductService {
	
	Flux<ProductDimensionDto> getDimensions(List<String> skus);

}
