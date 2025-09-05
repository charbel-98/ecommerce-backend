package com.charbel.ecommerce.config;

import com.charbel.ecommerce.brand.entity.Brand;
import com.charbel.ecommerce.brand.repository.BrandRepository;
import com.charbel.ecommerce.category.entity.Category;
import com.charbel.ecommerce.category.repository.CategoryRepository;
import com.charbel.ecommerce.common.enums.GenderType;
import com.charbel.ecommerce.product.entity.Product;
import com.charbel.ecommerce.product.entity.ProductVariant;
import com.charbel.ecommerce.product.repository.ProductRepository;
import com.charbel.ecommerce.product.repository.ProductVariantRepository;
import com.charbel.ecommerce.user.entity.User;
import com.charbel.ecommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

	private final UserRepository userRepository;
	private final BrandRepository brandRepository;
	private final CategoryRepository categoryRepository;
	private final ProductRepository productRepository;
	private final ProductVariantRepository productVariantRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	@Transactional
	public void run(String... args) throws Exception {
		seedAdminUser();
		seedBrands();
		seedCategories();
		seedProducts();
	}

	private void seedAdminUser() {
		String adminEmail = "admin@ecommerce.com";
		
		if (!userRepository.existsByEmail(adminEmail)) {
			User adminUser = User.builder()
					.email(adminEmail)
					.passwordHash(passwordEncoder.encode("admin123"))
					.firstName("Admin")
					.lastName("User")
					.role(User.UserRole.ADMIN)
					.build();

			userRepository.save(adminUser);
			log.info("Admin user created successfully:");
			log.info("Email: {}", adminEmail);
			log.info("Password: admin123");
			log.info("Please change the default password after first login!");
		} else {
			log.info("Admin user already exists with email: {}", adminEmail);
		}
	}

	private void seedBrands() {
		if (brandRepository.count() > 0) {
			log.info("Brands already exist, skipping brand seeding");
			return;
		}

		List<Brand> brands = List.of(
			Brand.builder()
				.name("Nike")
				.slug("nike")
				.description("Just Do It - Athletic wear and sportswear")
				.logoUrl("https://example.com/logos/nike.png")
				.websiteUrl("https://nike.com")
				.build(),
			
			Brand.builder()
				.name("Adidas")
				.slug("adidas")
				.description("Impossible is Nothing - Sports and lifestyle brand")
				.logoUrl("https://example.com/logos/adidas.png")
				.websiteUrl("https://adidas.com")
				.build(),
			
			Brand.builder()
				.name("Zara")
				.slug("zara")
				.description("Fast fashion and trendy clothing")
				.logoUrl("https://example.com/logos/zara.png")
				.websiteUrl("https://zara.com")
				.build(),
			
			Brand.builder()
				.name("H&M")
				.slug("hm")
				.description("Fashion and quality at the best price")
				.logoUrl("https://example.com/logos/hm.png")
				.websiteUrl("https://hm.com")
				.build(),
			
			Brand.builder()
				.name("Levi's")
				.slug("levis")
				.description("Quality never goes out of style")
				.logoUrl("https://example.com/logos/levis.png")
				.websiteUrl("https://levi.com")
				.build(),
			
			Brand.builder()
				.name("Uniqlo")
				.slug("uniqlo")
				.description("Made for all - Simple, quality clothing")
				.logoUrl("https://example.com/logos/uniqlo.png")
				.websiteUrl("https://uniqlo.com")
				.build()
		);

		brandRepository.saveAll(brands);
		log.info("Seeded {} brands", brands.size());
	}

	private void seedCategories() {
		if (categoryRepository.count() > 0) {
			log.info("Categories already exist, skipping category seeding");
			return;
		}

		// Root categories (Level 0)
		Category menCategory = Category.builder()
			.name("Men")
			.slug("men")
			.description("Men's fashion and clothing")
			.level(0)
			.sortOrder(1)
			.build();

		Category womenCategory = Category.builder()
			.name("Women") 
			.slug("women")
			.description("Women's fashion and clothing")
			.level(0)
			.sortOrder(2)
			.build();

		Category kidsCategory = Category.builder()
			.name("Kids")
			.slug("kids")
			.description("Kids' fashion and clothing")
			.level(0)
			.sortOrder(3)
			.build();

		categoryRepository.saveAll(List.of(menCategory, womenCategory, kidsCategory));

		// Level 1 categories (Men)
		List<Category> menSubCategories = List.of(
			Category.builder()
				.name("Clothing")
				.slug("mens-clothing")
				.parentId(menCategory.getId())
				.level(1)
				.sortOrder(1)
				.build(),
			Category.builder()
				.name("Shoes")
				.slug("mens-shoes")
				.parentId(menCategory.getId())
				.level(1)
				.sortOrder(2)
				.build(),
			Category.builder()
				.name("Accessories")
				.slug("mens-accessories")
				.parentId(menCategory.getId())
				.level(1)
				.sortOrder(3)
				.build()
		);

		// Level 1 categories (Women)
		List<Category> womenSubCategories = List.of(
			Category.builder()
				.name("Clothing")
				.slug("womens-clothing")
				.parentId(womenCategory.getId())
				.level(1)
				.sortOrder(1)
				.build(),
			Category.builder()
				.name("Shoes")
				.slug("womens-shoes")
				.parentId(womenCategory.getId())
				.level(1)
				.sortOrder(2)
				.build(),
			Category.builder()
				.name("Accessories")
				.slug("womens-accessories")
				.parentId(womenCategory.getId())
				.level(1)
				.sortOrder(3)
				.build(),
			Category.builder()
				.name("Bags")
				.slug("womens-bags")
				.parentId(womenCategory.getId())
				.level(1)
				.sortOrder(4)
				.build()
		);

		// Level 1 categories (Kids)
		List<Category> kidsSubCategories = List.of(
			Category.builder()
				.name("Boys")
				.slug("boys")
				.parentId(kidsCategory.getId())
				.level(1)
				.sortOrder(1)
				.build(),
			Category.builder()
				.name("Girls")
				.slug("girls")
				.parentId(kidsCategory.getId())
				.level(1)
				.sortOrder(2)
				.build()
		);

		categoryRepository.saveAll(menSubCategories);
		categoryRepository.saveAll(womenSubCategories);
		categoryRepository.saveAll(kidsSubCategories);

		// Level 2 categories (Men's Clothing)
		Category mensClothing = menSubCategories.get(0);
		List<Category> mensClothingSubCategories = List.of(
			Category.builder()
				.name("T-Shirts")
				.slug("mens-t-shirts")
				.parentId(mensClothing.getId())
				.level(2)
				.sortOrder(1)
				.build(),
			Category.builder()
				.name("Shirts")
				.slug("mens-shirts")
				.parentId(mensClothing.getId())
				.level(2)
				.sortOrder(2)
				.build(),
			Category.builder()
				.name("Jeans")
				.slug("mens-jeans")
				.parentId(mensClothing.getId())
				.level(2)
				.sortOrder(3)
				.build(),
			Category.builder()
				.name("Jackets")
				.slug("mens-jackets")
				.parentId(mensClothing.getId())
				.level(2)
				.sortOrder(4)
				.build()
		);

		// Level 2 categories (Women's Clothing)
		Category womensClothing = womenSubCategories.get(0);
		List<Category> womensClothingSubCategories = List.of(
			Category.builder()
				.name("Dresses")
				.slug("womens-dresses")
				.parentId(womensClothing.getId())
				.level(2)
				.sortOrder(1)
				.build(),
			Category.builder()
				.name("Tops")
				.slug("womens-tops")
				.parentId(womensClothing.getId())
				.level(2)
				.sortOrder(2)
				.build(),
			Category.builder()
				.name("Jeans")
				.slug("womens-jeans")
				.parentId(womensClothing.getId())
				.level(2)
				.sortOrder(3)
				.build(),
			Category.builder()
				.name("Blouses")
				.slug("womens-blouses")
				.parentId(womensClothing.getId())
				.level(2)
				.sortOrder(4)
				.build()
		);

		categoryRepository.saveAll(mensClothingSubCategories);
		categoryRepository.saveAll(womensClothingSubCategories);

		log.info("Seeded categories with hierarchical structure");
	}

	private void seedProducts() {
		if (productRepository.count() > 0) {
			log.info("Products already exist, skipping product seeding");
			return;
		}

		// Get brands and categories for referencing
		Brand nike = brandRepository.findBySlug("nike").orElseThrow();
		Brand zara = brandRepository.findBySlug("zara").orElseThrow();
		Brand levis = brandRepository.findBySlug("levis").orElseThrow();
		Brand hm = brandRepository.findBySlug("hm").orElseThrow();

		Category mensTshirts = categoryRepository.findBySlug("mens-t-shirts").orElseThrow();
		Category mensJeans = categoryRepository.findBySlug("mens-jeans").orElseThrow();
		Category womensDresses = categoryRepository.findBySlug("womens-dresses").orElseThrow();
		Category womensJeans = categoryRepository.findBySlug("womens-jeans").orElseThrow();
		Category womensShoes = categoryRepository.findBySlug("womens-shoes").orElseThrow();

		// Men's Nike T-Shirt
		Product nikeShirt = Product.builder()
			.name("Nike Dri-FIT Classic T-Shirt")
			.description("Comfortable cotton t-shirt with moisture-wicking technology")
			.basePrice(2999) // $29.99
			.brandId(nike.getId())
			.categoryId(mensTshirts.getId())
			.gender(GenderType.MEN)
			.metadata(createProductMetadata("COTTON", "ALL_SEASON", "CASUAL", "REGULAR"))
			.build();

		productRepository.save(nikeShirt);

		// Create variants for Nike T-Shirt
		List<ProductVariant> nikeShirtVariants = List.of(
			createVariant(nikeShirt, "NIKE-SHIRT-001-S-BLK", "S", "BLACK", 2999, 25),
			createVariant(nikeShirt, "NIKE-SHIRT-001-M-BLK", "M", "BLACK", 2999, 30),
			createVariant(nikeShirt, "NIKE-SHIRT-001-L-BLK", "L", "BLACK", 2999, 20),
			createVariant(nikeShirt, "NIKE-SHIRT-001-S-WHT", "S", "WHITE", 2999, 15),
			createVariant(nikeShirt, "NIKE-SHIRT-001-M-WHT", "M", "WHITE", 2999, 25),
			createVariant(nikeShirt, "NIKE-SHIRT-001-L-WHT", "L", "WHITE", 2999, 18)
		);
		productVariantRepository.saveAll(nikeShirtVariants);

		// Men's Levi's Jeans
		Product levisJeans = Product.builder()
			.name("Levi's 501 Original Fit Jeans")
			.description("The original blue jean. Classic straight fit with button fly")
			.basePrice(8999) // $89.99
			.brandId(levis.getId())
			.categoryId(mensJeans.getId())
			.gender(GenderType.MEN)
			.metadata(createProductMetadata("DENIM", "ALL_SEASON", "CASUAL", "REGULAR"))
			.build();

		productRepository.save(levisJeans);

		List<ProductVariant> levisJeansVariants = List.of(
			createVariant(levisJeans, "LEVIS-501-30-32-BLU", "30x32", "BLUE", 8999, 12),
			createVariant(levisJeans, "LEVIS-501-32-32-BLU", "32x32", "BLUE", 8999, 15),
			createVariant(levisJeans, "LEVIS-501-34-32-BLU", "34x32", "BLUE", 8999, 10),
			createVariant(levisJeans, "LEVIS-501-30-32-BLK", "30x32", "BLACK", 9499, 8),
			createVariant(levisJeans, "LEVIS-501-32-32-BLK", "32x32", "BLACK", 9499, 12)
		);
		productVariantRepository.saveAll(levisJeansVariants);

		// Women's Zara Dress
		Product zaraDress = Product.builder()
			.name("Zara Floral Print Midi Dress")
			.description("Elegant midi dress with floral print, perfect for any occasion")
			.basePrice(5999) // $59.99
			.brandId(zara.getId())
			.categoryId(womensDresses.getId())
			.gender(GenderType.WOMEN)
			.metadata(createProductMetadata("POLYESTER", "SPRING", "FORMAL", "REGULAR"))
			.build();

		productRepository.save(zaraDress);

		List<ProductVariant> zaraDressVariants = List.of(
			createVariant(zaraDress, "ZARA-DRESS-001-XS-PNK", "XS", "PINK", 5999, 8),
			createVariant(zaraDress, "ZARA-DRESS-001-S-PNK", "S", "PINK", 5999, 12),
			createVariant(zaraDress, "ZARA-DRESS-001-M-PNK", "M", "PINK", 5999, 15),
			createVariant(zaraDress, "ZARA-DRESS-001-L-PNK", "L", "PINK", 5999, 10),
			createVariant(zaraDress, "ZARA-DRESS-001-S-BLU", "S", "BLUE", 5999, 6),
			createVariant(zaraDress, "ZARA-DRESS-001-M-BLU", "M", "BLUE", 5999, 8)
		);
		productVariantRepository.saveAll(zaraDressVariants);

		// Women's H&M Jeans
		Product hmJeans = Product.builder()
			.name("H&M High Waisted Skinny Jeans")
			.description("Trendy high-waisted skinny fit jeans in stretch denim")
			.basePrice(3999) // $39.99
			.brandId(hm.getId())
			.categoryId(womensJeans.getId())
			.gender(GenderType.WOMEN)
			.metadata(createProductMetadata("DENIM", "ALL_SEASON", "CASUAL", "SLIM"))
			.build();

		productRepository.save(hmJeans);

		List<ProductVariant> hmJeansVariants = List.of(
			createVariant(hmJeans, "HM-JEANS-001-25-BLU", "25", "BLUE", 3999, 20),
			createVariant(hmJeans, "HM-JEANS-001-26-BLU", "26", "BLUE", 3999, 25),
			createVariant(hmJeans, "HM-JEANS-001-27-BLU", "27", "BLUE", 3999, 18),
			createVariant(hmJeans, "HM-JEANS-001-28-BLU", "28", "BLUE", 3999, 22),
			createVariant(hmJeans, "HM-JEANS-001-25-BLK", "25", "BLACK", 3999, 15),
			createVariant(hmJeans, "HM-JEANS-001-26-BLK", "26", "BLACK", 3999, 18)
		);
		productVariantRepository.saveAll(hmJeansVariants);

		// Unisex Nike Sneakers
		Product nikeShoes = Product.builder()
			.name("Nike Air Max 90")
			.description("Classic sneakers with visible Air cushioning and retro appeal")
			.basePrice(11999) // $119.99
			.brandId(nike.getId())
			.categoryId(womensShoes.getId()) // Using women's shoes category but it's unisex
			.gender(GenderType.UNISEX)
			.metadata(createProductMetadata("LEATHER", "ALL_SEASON", "CASUAL", "REGULAR"))
			.build();

		productRepository.save(nikeShoes);

		List<ProductVariant> nikeShoesVariants = List.of(
			createVariant(nikeShoes, "NIKE-AM90-001-8-WHT", "8", "WHITE", 11999, 12),
			createVariant(nikeShoes, "NIKE-AM90-001-9-WHT", "9", "WHITE", 11999, 15),
			createVariant(nikeShoes, "NIKE-AM90-001-10-WHT", "10", "WHITE", 11999, 18),
			createVariant(nikeShoes, "NIKE-AM90-001-8-BLK", "8", "BLACK", 11999, 10),
			createVariant(nikeShoes, "NIKE-AM90-001-9-BLK", "9", "BLACK", 11999, 14),
			createVariant(nikeShoes, "NIKE-AM90-001-10-BLK", "10", "BLACK", 11999, 16)
		);
		productVariantRepository.saveAll(nikeShoesVariants);

		log.info("Seeded {} products with variants", 5);
	}

	private Map<String, Object> createProductMetadata(String material, String season, String occasion, String fit) {
		Map<String, Object> metadata = new HashMap<>();
		metadata.put("material", material);
		metadata.put("season", season);
		metadata.put("occasion", occasion);
		metadata.put("fit", fit);
		metadata.put("care_instructions", "Machine wash cold, tumble dry low");
		return metadata;
	}

	private ProductVariant createVariant(Product product, String sku, String size, String color, Integer price, Integer stock) {
		Map<String, Object> attributes = new HashMap<>();
		attributes.put("size", size);
		attributes.put("color", color);

		return ProductVariant.builder()
			.product(product)
			.sku(sku)
			.attributes(attributes)
			.price(price)
			.stock(stock)
			.build();
	}
}