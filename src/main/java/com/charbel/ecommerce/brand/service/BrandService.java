package com.charbel.ecommerce.brand.service;

import com.charbel.ecommerce.brand.dto.BrandResponse;
import com.charbel.ecommerce.brand.dto.CreateBrandRequest;
import com.charbel.ecommerce.brand.entity.Brand;
import com.charbel.ecommerce.brand.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BrandService {

	private final BrandRepository brandRepository;

	public List<BrandResponse> getAllBrands() {
		return brandRepository.findAll().stream().map(BrandResponse::fromEntity).collect(Collectors.toList());
	}

	public List<BrandResponse> getActiveBrands() {
		return brandRepository.findAllActiveBrands().stream().map(BrandResponse::fromEntity)
				.collect(Collectors.toList());
	}

	public BrandResponse getBrandById(UUID id) {
		Brand brand = brandRepository.findByIdAndNotDeleted(id)
				.orElseThrow(() -> new RuntimeException("Brand not found with id: " + id));
		return BrandResponse.fromEntity(brand);
	}

	public BrandResponse getBrandBySlug(String slug) {
		Brand brand = brandRepository.findBySlug(slug)
				.orElseThrow(() -> new RuntimeException("Brand not found with slug: " + slug));
		return BrandResponse.fromEntity(brand);
	}

	@Transactional
	public BrandResponse createBrand(CreateBrandRequest request) {
		log.info("Creating new brand: {}", request.getName());

		if (brandRepository.existsByName(request.getName())) {
			throw new RuntimeException("Brand already exists with name: " + request.getName());
		}

		if (brandRepository.existsBySlug(request.getSlug())) {
			throw new RuntimeException("Brand already exists with slug: " + request.getSlug());
		}

		Brand brand = Brand.builder().name(request.getName()).slug(request.getSlug())
				.description(request.getDescription()).logoUrl(request.getLogoUrl()).websiteUrl(request.getWebsiteUrl())
				.build();

		brand = brandRepository.save(brand);
		log.info("Brand created successfully with id: {}", brand.getId());

		return BrandResponse.fromEntity(brand);
	}

	@Transactional
	public BrandResponse updateBrand(UUID id, CreateBrandRequest request) {
		log.info("Updating brand with id: {}", id);

		Brand brand = brandRepository.findByIdAndNotDeleted(id)
				.orElseThrow(() -> new RuntimeException("Brand not found with id: " + id));

		if (!brand.getName().equals(request.getName()) && brandRepository.existsByName(request.getName())) {
			throw new RuntimeException("Brand already exists with name: " + request.getName());
		}

		if (!brand.getSlug().equals(request.getSlug()) && brandRepository.existsBySlug(request.getSlug())) {
			throw new RuntimeException("Brand already exists with slug: " + request.getSlug());
		}

		brand.setName(request.getName());
		brand.setSlug(request.getSlug());
		brand.setDescription(request.getDescription());
		brand.setLogoUrl(request.getLogoUrl());
		brand.setWebsiteUrl(request.getWebsiteUrl());

		brand = brandRepository.save(brand);
		log.info("Brand updated successfully");

		return BrandResponse.fromEntity(brand);
	}

	@Transactional
	public void deleteBrand(UUID id) {
		log.info("Soft deleting brand with id: {}", id);

		Brand brand = brandRepository.findByIdAndNotDeleted(id)
				.orElseThrow(() -> new RuntimeException("Brand not found with id: " + id));

		brand.softDelete();
		brandRepository.save(brand);
		log.info("Brand soft deleted successfully");
	}
}
