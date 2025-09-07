package com.charbel.ecommerce.config;

import com.charbel.ecommerce.brand.entity.Brand;
import com.charbel.ecommerce.brand.repository.BrandRepository;
import com.charbel.ecommerce.category.entity.Category;
import com.charbel.ecommerce.category.repository.CategoryRepository;
import com.charbel.ecommerce.common.enums.GenderType;
import com.charbel.ecommerce.event.entity.Discount;
import com.charbel.ecommerce.event.entity.Event;
import com.charbel.ecommerce.event.repository.DiscountRepository;
import com.charbel.ecommerce.event.repository.EventRepository;
import com.charbel.ecommerce.product.entity.Product;
import com.charbel.ecommerce.product.entity.ProductVariant;
import com.charbel.ecommerce.product.repository.ProductRepository;
import com.charbel.ecommerce.product.repository.ProductVariantRepository;
import com.charbel.ecommerce.review.entity.Review;
import com.charbel.ecommerce.review.repository.ReviewRepository;
import com.charbel.ecommerce.user.entity.User;
import com.charbel.ecommerce.user.repository.UserRepository;
import com.charbel.ecommerce.address.entity.Address;
import com.charbel.ecommerce.address.repository.AddressRepository;
import com.charbel.ecommerce.orders.entity.Order;
import com.charbel.ecommerce.orders.entity.OrderItem;
import com.charbel.ecommerce.orders.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

	private final UserRepository userRepository;
	private final BrandRepository brandRepository;
	private final CategoryRepository categoryRepository;
	private final ProductRepository productRepository;
	private final ProductVariantRepository productVariantRepository;
	private final EventRepository eventRepository;
	private final DiscountRepository discountRepository;
	private final ReviewRepository reviewRepository;
	private final AddressRepository addressRepository;
	private final OrderRepository orderRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	@Transactional
	public void run(String... args) throws Exception {
		seedAdminUser();
		seedBrands();
		seedCategories();
		seedProducts();
		seedEvents();
		seedEventProductLinks();
		seedReviewUser();
		seedReviews();
		seedAddresses();
		migrateOrderDeliveryFees();
	}

	private void seedAdminUser() {
		String adminEmail = "admin@ecommerce.com";

		if (!userRepository.existsByEmail(adminEmail)) {
			User adminUser = User.builder().email(adminEmail).passwordHash(passwordEncoder.encode("admin123"))
					.firstName("Admin").lastName("User").role(User.UserRole.ADMIN).build();

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
				Brand.builder().name("Nike").slug("nike").description("Just Do It - Athletic wear and sportswear")
						.logoUrl("https://example.com/logos/nike.png").websiteUrl("https://nike.com").build(),

				Brand.builder().name("Adidas").slug("adidas")
						.description("Impossible is Nothing - Sports and lifestyle brand")
						.logoUrl("https://example.com/logos/adidas.png").websiteUrl("https://adidas.com").build(),

				Brand.builder().name("Zara").slug("zara").description("Fast fashion and trendy clothing")
						.logoUrl("https://example.com/logos/zara.png").websiteUrl("https://zara.com").build(),

				Brand.builder().name("H&M").slug("hm").description("Fashion and quality at the best price")
						.logoUrl("https://example.com/logos/hm.png").websiteUrl("https://hm.com").build(),

				Brand.builder().name("Levi's").slug("levis").description("Quality never goes out of style")
						.logoUrl("https://example.com/logos/levis.png").websiteUrl("https://levi.com").build(),

				Brand.builder().name("Uniqlo").slug("uniqlo").description("Made for all - Simple, quality clothing")
						.logoUrl("https://example.com/logos/uniqlo.png").websiteUrl("https://uniqlo.com").build());

		brandRepository.saveAll(brands);
		log.info("Seeded {} brands", brands.size());
	}

	private void seedCategories() {
		if (categoryRepository.count() > 0) {
			log.info("Categories already exist, skipping category seeding");
			return;
		}

		// Root categories (Level 0)
		Category menCategory = Category.builder().name("Men").slug("men").description("Men's fashion and clothing")
				.level(0).sortOrder(1).build();

		Category womenCategory = Category.builder().name("Women").slug("women")
				.description("Women's fashion and clothing").level(0).sortOrder(2).build();

		Category kidsCategory = Category.builder().name("Kids").slug("kids").description("Kids' fashion and clothing")
				.level(0).sortOrder(3).build();

		categoryRepository.saveAll(List.of(menCategory, womenCategory, kidsCategory));

		// Level 1 categories (Men)
		List<Category> menSubCategories = List.of(
				Category.builder().name("Clothing").slug("mens-clothing").parentId(menCategory.getId()).level(1)
						.sortOrder(1).build(),
				Category.builder().name("Shoes").slug("mens-shoes").parentId(menCategory.getId()).level(1).sortOrder(2)
						.build(),
				Category.builder().name("Accessories").slug("mens-accessories").parentId(menCategory.getId()).level(1)
						.sortOrder(3).build());

		// Level 1 categories (Women)
		List<Category> womenSubCategories = List.of(
				Category.builder().name("Clothing").slug("womens-clothing").parentId(womenCategory.getId()).level(1)
						.sortOrder(1).build(),
				Category.builder().name("Shoes").slug("womens-shoes").parentId(womenCategory.getId()).level(1)
						.sortOrder(2).build(),
				Category.builder().name("Accessories").slug("womens-accessories").parentId(womenCategory.getId())
						.level(1).sortOrder(3).build(),
				Category.builder().name("Bags").slug("womens-bags").parentId(womenCategory.getId()).level(1)
						.sortOrder(4).build());

		// Level 1 categories (Kids)
		List<Category> kidsSubCategories = List.of(
				Category.builder().name("Boys").slug("boys").parentId(kidsCategory.getId()).level(1).sortOrder(1)
						.build(),
				Category.builder().name("Girls").slug("girls").parentId(kidsCategory.getId()).level(1).sortOrder(2)
						.build());

		categoryRepository.saveAll(menSubCategories);
		categoryRepository.saveAll(womenSubCategories);
		categoryRepository.saveAll(kidsSubCategories);

		// Level 2 categories (Men's Clothing)
		Category mensClothing = menSubCategories.get(0);
		List<Category> mensClothingSubCategories = List.of(
				Category.builder().name("T-Shirts").slug("mens-t-shirts").parentId(mensClothing.getId()).level(2)
						.sortOrder(1).build(),
				Category.builder().name("Shirts").slug("mens-shirts").parentId(mensClothing.getId()).level(2)
						.sortOrder(2).build(),
				Category.builder().name("Jeans").slug("mens-jeans").parentId(mensClothing.getId()).level(2).sortOrder(3)
						.build(),
				Category.builder().name("Jackets").slug("mens-jackets").parentId(mensClothing.getId()).level(2)
						.sortOrder(4).build());

		// Level 2 categories (Women's Clothing)
		Category womensClothing = womenSubCategories.get(0);
		List<Category> womensClothingSubCategories = List.of(
				Category.builder().name("Dresses").slug("womens-dresses").parentId(womensClothing.getId()).level(2)
						.sortOrder(1).build(),
				Category.builder().name("Tops").slug("womens-tops").parentId(womensClothing.getId()).level(2)
						.sortOrder(2).build(),
				Category.builder().name("Jeans").slug("womens-jeans").parentId(womensClothing.getId()).level(2)
						.sortOrder(3).build(),
				Category.builder().name("Blouses").slug("womens-blouses").parentId(womensClothing.getId()).level(2)
						.sortOrder(4).build());

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
		Product nikeShirt = Product.builder().name("Nike Dri-FIT Classic T-Shirt")
				.description("Comfortable cotton t-shirt with moisture-wicking technology").basePrice(new BigDecimal("29.99"))
				.brandId(nike.getId()).categoryId(mensTshirts.getId()).gender(GenderType.MEN)
				.metadata(createProductMetadata("COTTON", "ALL_SEASON", "CASUAL", "REGULAR")).build();

		productRepository.save(nikeShirt);

		// Create variants for Nike T-Shirt
		List<ProductVariant> nikeShirtVariants = List.of(
				createVariant(nikeShirt, "NIKE-SHIRT-001-S-BLK", "S", "BLACK", new BigDecimal("29.99"), 25),
				createVariant(nikeShirt, "NIKE-SHIRT-001-M-BLK", "M", "BLACK", new BigDecimal("29.99"), 30),
				createVariant(nikeShirt, "NIKE-SHIRT-001-L-BLK", "L", "BLACK", new BigDecimal("29.99"), 20),
				createVariant(nikeShirt, "NIKE-SHIRT-001-S-WHT", "S", "WHITE", new BigDecimal("29.99"), 15),
				createVariant(nikeShirt, "NIKE-SHIRT-001-M-WHT", "M", "WHITE", new BigDecimal("29.99"), 25),
				createVariant(nikeShirt, "NIKE-SHIRT-001-L-WHT", "L", "WHITE", new BigDecimal("29.99"), 18));
		productVariantRepository.saveAll(nikeShirtVariants);

		// Men's Levi's Jeans
		Product levisJeans = Product.builder().name("Levi's 501 Original Fit Jeans")
				.description("The original blue jean. Classic straight fit with button fly").basePrice(new BigDecimal("89.99"))
				.brandId(levis.getId()).categoryId(mensJeans.getId()).gender(GenderType.MEN)
				.metadata(createProductMetadata("DENIM", "ALL_SEASON", "CASUAL", "REGULAR")).build();

		productRepository.save(levisJeans);

		List<ProductVariant> levisJeansVariants = List.of(
				createVariant(levisJeans, "LEVIS-501-30-32-BLU", "30x32", "BLUE", new BigDecimal("89.99"), 12),
				createVariant(levisJeans, "LEVIS-501-32-32-BLU", "32x32", "BLUE", new BigDecimal("89.99"), 15),
				createVariant(levisJeans, "LEVIS-501-34-32-BLU", "34x32", "BLUE", new BigDecimal("89.99"), 10),
				createVariant(levisJeans, "LEVIS-501-30-32-BLK", "30x32", "BLACK", new BigDecimal("94.99"), 8),
				createVariant(levisJeans, "LEVIS-501-32-32-BLK", "32x32", "BLACK", new BigDecimal("94.99"), 12));
		productVariantRepository.saveAll(levisJeansVariants);

		// Women's Zara Dress
		Product zaraDress = Product.builder().name("Zara Floral Print Midi Dress")
				.description("Elegant midi dress with floral print, perfect for any occasion").basePrice(new BigDecimal("59.99"))
				.brandId(zara.getId()).categoryId(womensDresses.getId()).gender(GenderType.WOMEN)
				.metadata(createProductMetadata("POLYESTER", "SPRING", "FORMAL", "REGULAR")).build();

		productRepository.save(zaraDress);

		List<ProductVariant> zaraDressVariants = List.of(
				createVariant(zaraDress, "ZARA-DRESS-001-XS-PNK", "XS", "PINK", new BigDecimal("59.99"), 8),
				createVariant(zaraDress, "ZARA-DRESS-001-S-PNK", "S", "PINK", new BigDecimal("59.99"), 12),
				createVariant(zaraDress, "ZARA-DRESS-001-M-PNK", "M", "PINK", new BigDecimal("59.99"), 15),
				createVariant(zaraDress, "ZARA-DRESS-001-L-PNK", "L", "PINK", new BigDecimal("59.99"), 10),
				createVariant(zaraDress, "ZARA-DRESS-001-S-BLU", "S", "BLUE", new BigDecimal("59.99"), 6),
				createVariant(zaraDress, "ZARA-DRESS-001-M-BLU", "M", "BLUE", new BigDecimal("59.99"), 8));
		productVariantRepository.saveAll(zaraDressVariants);

		// Women's H&M Jeans
		Product hmJeans = Product.builder().name("H&M High Waisted Skinny Jeans")
				.description("Trendy high-waisted skinny fit jeans in stretch denim").basePrice(new BigDecimal("39.99"))
				.brandId(hm.getId()).categoryId(womensJeans.getId()).gender(GenderType.WOMEN)
				.metadata(createProductMetadata("DENIM", "ALL_SEASON", "CASUAL", "SLIM")).build();

		productRepository.save(hmJeans);

		List<ProductVariant> hmJeansVariants = List.of(
				createVariant(hmJeans, "HM-JEANS-001-25-BLU", "25", "BLUE", new BigDecimal("39.99"), 20),
				createVariant(hmJeans, "HM-JEANS-001-26-BLU", "26", "BLUE", new BigDecimal("39.99"), 25),
				createVariant(hmJeans, "HM-JEANS-001-27-BLU", "27", "BLUE", new BigDecimal("39.99"), 18),
				createVariant(hmJeans, "HM-JEANS-001-28-BLU", "28", "BLUE", new BigDecimal("39.99"), 22),
				createVariant(hmJeans, "HM-JEANS-001-25-BLK", "25", "BLACK", new BigDecimal("39.99"), 15),
				createVariant(hmJeans, "HM-JEANS-001-26-BLK", "26", "BLACK", new BigDecimal("39.99"), 18));
		productVariantRepository.saveAll(hmJeansVariants);

		// Unisex Nike Sneakers
		Product nikeShoes = Product.builder().name("Nike Air Max 90")
				.description("Classic sneakers with visible Air cushioning and retro appeal").basePrice(new BigDecimal("119.99"))
				.brandId(nike.getId()).categoryId(womensShoes.getId()) // Using women's shoes category but it's unisex
				.gender(GenderType.UNISEX).metadata(createProductMetadata("LEATHER", "ALL_SEASON", "CASUAL", "REGULAR"))
				.build();

		productRepository.save(nikeShoes);

		List<ProductVariant> nikeShoesVariants = List.of(
				createVariant(nikeShoes, "NIKE-AM90-001-8-WHT", "8", "WHITE", new BigDecimal("119.99"), 12),
				createVariant(nikeShoes, "NIKE-AM90-001-9-WHT", "9", "WHITE", new BigDecimal("119.99"), 15),
				createVariant(nikeShoes, "NIKE-AM90-001-10-WHT", "10", "WHITE", new BigDecimal("119.99"), 18),
				createVariant(nikeShoes, "NIKE-AM90-001-8-BLK", "8", "BLACK", new BigDecimal("119.99"), 10),
				createVariant(nikeShoes, "NIKE-AM90-001-9-BLK", "9", "BLACK", new BigDecimal("119.99"), 14),
				createVariant(nikeShoes, "NIKE-AM90-001-10-BLK", "10", "BLACK", new BigDecimal("119.99"), 16));
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

	private ProductVariant createVariant(Product product, String sku, String size, String color, BigDecimal price,
			Integer stock) {
		Map<String, Object> attributes = new HashMap<>();
		attributes.put("size", size);
		attributes.put("color", color);

		return ProductVariant.builder().product(product).sku(sku).attributes(attributes).price(price).stock(stock)
				.build();
	}

	private void seedEvents() {
		if (eventRepository.count() > 0) {
			log.info("Events already exist, skipping event seeding");
			return;
		}

		LocalDateTime now = LocalDateTime.now();
		LocalDateTime futureDate = now.plusMonths(3);
		LocalDateTime pastDate = now.minusMonths(1);

		// Event 1: Fashion Sale (with discount)
		Event fashionSaleEvent = Event.builder().name("Fashion\nsale")
				.description("Get amazing discounts on our fashion collection")
				.imageUrl(
						"https://images.unsplash.com/photo-1445205170230-053b83016050?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80")
				.startDate(pastDate).endDate(futureDate).status(Event.EventStatus.ACTIVE).build();

		eventRepository.save(fashionSaleEvent);

		// Create discount for Fashion Sale
		Discount fashionSaleDiscount = Discount.builder().eventId(fashionSaleEvent.getId())
				.type(Discount.DiscountType.PERCENTAGE).value(new BigDecimal("25.00")) // 25% off
				.minPurchaseAmount(new BigDecimal("50.00")) // $50 minimum
				.maxDiscountAmount(new BigDecimal("50.00")) // Max $50 discount
				.build();

		discountRepository.save(fashionSaleDiscount);

		// Event 2: Summer Collection
		Event summerCollectionEvent = Event.builder().name("Summer\nCollection")
				.description("Discover our latest summer styles and trending looks")
				.imageUrl(
						"https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80")
				.startDate(now.minusWeeks(2)).endDate(futureDate.plusWeeks(4)).status(Event.EventStatus.ACTIVE).build();

		eventRepository.save(summerCollectionEvent);

		// Event 3: Winter is Coming
		Event winterEvent = Event.builder().name("Winter\nis Coming")
				.description("Prepare for winter with our cozy collection of warm clothing")
				.imageUrl(
						"https://images.unsplash.com/photo-1578662996442-48f60103fc96?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80")
				.startDate(now.plusMonths(2)).endDate(futureDate.plusMonths(2)).status(Event.EventStatus.SCHEDULED)
				.build();

		eventRepository.save(winterEvent);

		log.info("Seeded 3 events with carousel data");
	}

	private void seedEventProductLinks() {
		if (eventRepository.count() == 0 || productRepository.count() == 0) {
			log.info("No events or products found, skipping event-product linking");
			return;
		}

		// Get all events and products
		List<Event> events = eventRepository.findAll();
		List<Product> products = productRepository.findAll();

		// Fashion Sale Event - Link to Zara Dress and H&M Jeans (fashion items)
		Event fashionSaleEvent = events.stream().filter(e -> e.getName().contains("Fashion")).findFirst().orElse(null);

		if (fashionSaleEvent != null) {
			Set<Product> fashionProducts = products.stream()
					.filter(p -> p.getName().contains("Zara") || p.getName().contains("H&M"))
					.collect(Collectors.toSet());

			fashionSaleEvent.setProducts(fashionProducts);
			eventRepository.save(fashionSaleEvent);
			log.info("Linked {} products to Fashion Sale event", fashionProducts.size());
		}

		// Summer Collection Event - Link to Nike T-Shirt and Zara Dress (summer items)
		Event summerEvent = events.stream().filter(e -> e.getName().contains("Summer")).findFirst().orElse(null);

		if (summerEvent != null) {
			Set<Product> summerProducts = products.stream()
					.filter(p -> p.getName().contains("T-Shirt") || p.getName().contains("Dress"))
					.collect(Collectors.toSet());

			summerEvent.setProducts(summerProducts);
			eventRepository.save(summerEvent);
			log.info("Linked {} products to Summer Collection event", summerProducts.size());
		}

		// Winter Event - Link to all Jeans and Sneakers (layering pieces)
		Event winterEvent = events.stream().filter(e -> e.getName().contains("Winter")).findFirst().orElse(null);

		if (winterEvent != null) {
			Set<Product> winterProducts = products.stream()
					.filter(p -> p.getName().contains("Jeans") || p.getName().contains("Air Max"))
					.collect(Collectors.toSet());

			winterEvent.setProducts(winterProducts);
			eventRepository.save(winterEvent);
			log.info("Linked {} products to Winter event", winterProducts.size());
		}

		log.info("Successfully linked products to events");
	}

	private void seedReviewUser() {
		String reviewUserEmail = "charbel_cg@outlook.com";

		if (!userRepository.existsByEmail(reviewUserEmail)) {
			User reviewUser = User.builder()
					.email(reviewUserEmail)
					.passwordHash(passwordEncoder.encode("password123"))
					.firstName("Charbel")
					.lastName("CG")
					.role(User.UserRole.CUSTOMER)
					.build();

			userRepository.save(reviewUser);
			log.info("Review user created successfully:");
			log.info("Email: {}", reviewUserEmail);
			log.info("Password: password123");
		} else {
			log.info("Review user already exists with email: {}", reviewUserEmail);
		}
	}

	private void seedReviews() {
		if (reviewRepository.count() > 0) {
			log.info("Reviews already exist, skipping review seeding");
			return;
		}

		User reviewUser = userRepository.findByEmail("charbel_cg@outlook.com").orElse(null);
		if (reviewUser == null) {
			log.warn("Review user not found, skipping review seeding");
			return;
		}

		List<Product> products = productRepository.findAll();
		if (products.isEmpty()) {
			log.warn("No products found, skipping review seeding");
			return;
		}

		List<Review> reviews = new ArrayList<>();

		// Nike Dri-FIT T-Shirt
		Product nikeShirt = products.stream()
				.filter(p -> p.getName().contains("Nike Dri-FIT"))
				.findFirst()
				.orElse(null);
		if (nikeShirt != null) {
			reviews.add(Review.builder()
					.product(nikeShirt)
					.productId(nikeShirt.getId())
					.user(reviewUser)
					.userId(reviewUser.getId())
					.rating(5)
					.title("Excellent quality and comfort!")
					.comment("This Nike Dri-FIT t-shirt is absolutely amazing! The fabric is incredibly soft and breathable. Perfect for workouts and casual wear. The moisture-wicking technology really works. Highly recommended!")
					.isVerifiedPurchase(true)
					.helpfulCount(12)
					.build());
		}

		// Levi's 501 Jeans
		Product levisJeans = products.stream()
				.filter(p -> p.getName().contains("Levi's 501"))
				.findFirst()
				.orElse(null);
		if (levisJeans != null) {
			reviews.add(Review.builder()
					.product(levisJeans)
					.productId(levisJeans.getId())
					.user(reviewUser)
					.userId(reviewUser.getId())
					.rating(4)
					.title("Classic and durable")
					.comment("These are the classic Levi's 501s that everyone loves. Great quality denim that will last for years. The fit is perfect and they get better with age. Only giving 4 stars because they're a bit pricey, but worth the investment.")
					.isVerifiedPurchase(true)
					.helpfulCount(8)
					.build());
		}

		// Zara Floral Dress
		Product zaraDress = products.stream()
				.filter(p -> p.getName().contains("Zara Floral"))
				.findFirst()
				.orElse(null);
		if (zaraDress != null) {
			reviews.add(Review.builder()
					.product(zaraDress)
					.productId(zaraDress.getId())
					.user(reviewUser)
					.userId(reviewUser.getId())
					.rating(5)
					.title("Beautiful dress, perfect fit!")
					.comment("This dress is gorgeous! The floral print is so elegant and the quality is excellent. Perfect for both casual and formal occasions. The fit is true to size and very flattering. Love it!")
					.isVerifiedPurchase(true)
					.helpfulCount(15)
					.build());
		}

		// H&M Skinny Jeans
		Product hmJeans = products.stream()
				.filter(p -> p.getName().contains("H&M High Waisted"))
				.findFirst()
				.orElse(null);
		if (hmJeans != null) {
			reviews.add(Review.builder()
					.product(hmJeans)
					.productId(hmJeans.getId())
					.user(reviewUser)
					.userId(reviewUser.getId())
					.rating(4)
					.title("Great value for money")
					.comment("These jeans are a great find! The high-waisted cut is very flattering and the stretch denim is comfortable. Good quality for the price point. Perfect for everyday wear.")
					.isVerifiedPurchase(true)
					.helpfulCount(6)
					.build());
		}

		// Nike Air Max 90
		Product nikeShoes = products.stream()
				.filter(p -> p.getName().contains("Nike Air Max 90"))
				.findFirst()
				.orElse(null);
		if (nikeShoes != null) {
			reviews.add(Review.builder()
					.product(nikeShoes)
					.productId(nikeShoes.getId())
					.user(reviewUser)
					.userId(reviewUser.getId())
					.rating(5)
					.title("Classic sneakers never go out of style")
					.comment("These Air Max 90s are iconic! Super comfortable with excellent cushioning. The retro design looks great with any outfit. True to size and built to last. Worth every penny!")
					.isVerifiedPurchase(true)
					.helpfulCount(20)
					.build());
		}

		reviewRepository.saveAll(reviews);
		log.info("Seeded {} reviews for user: {}", reviews.size(), reviewUser.getEmail());

		// Update product rating statistics after seeding reviews
		updateProductRatingStats();
	}

	private void updateProductRatingStats() {
		log.info("Updating product rating statistics...");
		
		List<Product> products = productRepository.findAll();
		
		for (Product product : products) {
			Long reviewCount = reviewRepository.countByProductId(product.getId());
			BigDecimal averageRating = reviewRepository.findAverageRatingByProductId(product.getId());
			
			if (averageRating != null) {
				averageRating = averageRating.setScale(1, RoundingMode.HALF_UP);
			}
			
			product.setReviewCount(reviewCount);
			product.setAverageRating(averageRating);
			
			log.debug("Product {} - Reviews: {}, Average Rating: {}", 
					product.getName(), reviewCount, averageRating);
		}
		
		productRepository.saveAll(products);
		log.info("Updated rating statistics for {} products", products.size());
	}

	private void seedAddresses() {
		if (addressRepository.count() > 0) {
			log.info("Addresses already exist, skipping address seeding");
			return;
		}

		User charbelUser = userRepository.findByEmail("charbel_cg@outlook.com").orElse(null);
		if (charbelUser == null) {
			log.warn("Charbel user not found, skipping address seeding");
			return;
		}

		List<Address> addresses = List.of(
			// Home address in Beirut (default)
			Address.builder()
				.user(charbelUser)
				.street("123 Hamra Street, Building A, Floor 3")
				.city("Beirut")
				.state("Beirut Governorate")
				.zipCode("1103-2070")
				.country("Lebanon")
				.isDefault(true)
				.build(),

			// Work address in Downtown Beirut
			Address.builder()
				.user(charbelUser)
				.street("456 Martyrs' Square, Tower B, Office 1205")
				.city("Beirut")
				.state("Beirut Governorate") 
				.zipCode("1107-2020")
				.country("Lebanon")
				.isDefault(false)
				.build(),

			// Family address in Jounieh
			Address.builder()
				.user(charbelUser)
				.street("789 Maameltein Highway, Villa 25")
				.city("Jounieh")
				.state("Mount Lebanon Governorate")
				.zipCode("1200-1050")
				.country("Lebanon")
				.isDefault(false)
				.build(),

			// Beach house in Batroun
			Address.builder()
				.user(charbelUser)
				.street("321 Coastal Road, Seaside Resort Complex")
				.city("Batroun")
				.state("North Governorate")
				.zipCode("1400-3080")
				.country("Lebanon")
				.isDefault(false)
				.build(),

			// Mountain cabin in Bcharre
			Address.builder()
				.user(charbelUser)
				.street("654 Cedar Mountains Road, Chalet 12")
				.city("Bcharre")
				.state("North Governorate")
				.zipCode("1401-7020")
				.country("Lebanon")
				.isDefault(false)
				.build()
		);

		addressRepository.saveAll(addresses);
		log.info("Seeded {} addresses for user: {}", addresses.size(), charbelUser.getEmail());
	}

	private void migrateOrderDeliveryFees() {
		log.info("Migrating existing orders to add delivery fees and fix order items...");
		
		List<Order> ordersWithoutDeliveryFee = orderRepository.findAll().stream()
			.filter(order -> order.getDeliveryFee() == null)
			.collect(Collectors.toList());
		
		if (ordersWithoutDeliveryFee.isEmpty()) {
			log.info("All orders already have delivery fees, skipping migration");
			return;
		}
		
		for (Order order : ordersWithoutDeliveryFee) {
			BigDecimal deliveryFee = new BigDecimal("5.00");
			order.setDeliveryFee(deliveryFee);
			
			// Recalculate total amount with delivery fee
			BigDecimal newTotal = order.getOriginalAmount().subtract(order.getDiscountAmount()).add(deliveryFee);
			order.setTotalAmount(newTotal);
			
			// Migrate order items from cents to decimal if needed
			if (order.getOrderItems() != null) {
				for (OrderItem orderItem : order.getOrderItems()) {
					// If the unit price seems to be in cents (e.g., > 1000), convert it
					if (orderItem.getUnitPrice().compareTo(new BigDecimal("1000")) > 0) {
						BigDecimal centPrice = orderItem.getUnitPrice();
						BigDecimal dollarPrice = centPrice.divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
						orderItem.setUnitPrice(dollarPrice);
					}
				}
			}
		}
		
		orderRepository.saveAll(ordersWithoutDeliveryFee);
		log.info("Migrated {} orders with delivery fees and order items", ordersWithoutDeliveryFee.size());
	}
}
