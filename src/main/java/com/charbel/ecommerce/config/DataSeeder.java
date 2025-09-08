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
import com.charbel.ecommerce.product.entity.ProductImage;
import com.charbel.ecommerce.product.entity.ProductVariant;
import com.charbel.ecommerce.product.repository.ProductImageRepository;
import com.charbel.ecommerce.product.repository.ProductRepository;
import com.charbel.ecommerce.product.repository.ProductVariantRepository;
import com.charbel.ecommerce.review.entity.Review;
import com.charbel.ecommerce.review.entity.ReviewImage;
import com.charbel.ecommerce.review.repository.ReviewImageRepository;
import com.charbel.ecommerce.review.repository.ReviewRepository;
import com.charbel.ecommerce.review.repository.ReviewHelpfulVoteRepository;
import com.charbel.ecommerce.user.entity.User;
import com.charbel.ecommerce.user.repository.UserRepository;
import com.charbel.ecommerce.user.repository.RefreshTokenRepository;
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
import java.util.concurrent.atomic.AtomicInteger;
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
    private final ProductImageRepository productImageRepository;
    private final EventRepository eventRepository;
    private final DiscountRepository discountRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final ReviewHelpfulVoteRepository reviewHelpfulVoteRepository;
    private final AddressRepository addressRepository;
    private final OrderRepository orderRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    
    private final Random random = new Random();
    
    private final List<String> REVIEW_COMMENTS = Arrays.asList(
        "Great quality product! Exactly as described.",
        "Love the material and fit. Will buy again!",
        "Good value for money. Recommended.",
        "Arrived quickly and in perfect condition.",
        "The color is even better in person!",
        "Comfortable and stylish. Perfect for everyday wear.",
        "Excellent quality construction. Very durable.",
        "Size runs true to fit. Very happy with purchase.",
        "Beautiful product! Exceeded my expectations.",
        "Great customer service and fast shipping.",
        "The fabric feels premium and looks amazing.",
        "Perfect for both casual and formal occasions.",
        "Amazing comfort! Been wearing it all day.",
        "The design is elegant and versatile.",
        "Highly recommend this to everyone!"
    );

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Only run seeder if explicitly enabled or if database is empty
        boolean seedEnabled = Arrays.asList(args).contains("--seed") || 
                             System.getProperty("seed.data", "false").equals("true") ||
                             brandRepository.count() == 0;
        
        if (!seedEnabled) {
            log.info("DataSeeder skipped - database already contains data. Use --seed argument or seed.data=true property to force re-seeding.");
            return;
        }
        
        log.info("=== Starting comprehensive database seeding ===");
        
        // Clear existing data
        clearAllData();
        
        // Seed in order
        seedBrandsWithLogos();
        seedCategoriesWithImages();
        seedUsers();
        seedProductsWithImages();
        seedReviews();
        seedOrders();
        seedEvents();
        seedEventProductLinks();
        
        log.info("=== Database seeding completed successfully ===");
    }
    
    private void clearAllData() {
        log.info("Clearing all existing data...");
        
        // Clear in reverse dependency order
        refreshTokenRepository.deleteAll();
        reviewHelpfulVoteRepository.deleteAll();
        reviewImageRepository.deleteAll();
        reviewRepository.deleteAll();
        
        // Clear order related data
        orderRepository.findAll().forEach(order -> {
            order.getOrderItems().clear();
            orderRepository.delete(order);
        });
        
        productImageRepository.deleteAll();
        productVariantRepository.deleteAll();
        productRepository.deleteAll();
        
        discountRepository.deleteAll();
        eventRepository.findAll().forEach(event -> {
            event.getProducts().clear();
            eventRepository.delete(event);
        });
        
        addressRepository.deleteAll();
        userRepository.deleteAll();
        categoryRepository.deleteAll();
        brandRepository.deleteAll();
        
        log.info("All data cleared successfully");
    }

    private void seedBrandsWithLogos() {
        log.info("Seeding brands with logos...");
        
        // Check if brands already exist to avoid duplicates
        if (brandRepository.count() > 0) {
            log.info("Brands already exist, skipping brand seeding");
            return;
        }

        List<Brand> brands = Arrays.asList(
            // Sports & Athletic
            Brand.builder()
                .name("Nike")
                .slug("nike")
                .description("Just Do It - Leading athletic wear and sportswear brand")
                .logoUrl("https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=300&h=300&fit=crop")
                .websiteUrl("https://nike.com")
                .build(),
            
            Brand.builder()
                .name("Adidas")
                .slug("adidas")
                .description("Impossible is Nothing - Sports and lifestyle brand")
                .logoUrl("https://images.unsplash.com/photo-1556906781-9a412961c28c?w=300&h=300&fit=crop")
                .websiteUrl("https://adidas.com")
                .build(),
                
            Brand.builder()
                .name("Puma")
                .slug("puma")
                .description("Forever Faster - Athletic and casual footwear")
                .logoUrl("https://images.unsplash.com/photo-1449824913935-59a10b8d2000?w=300&h=300&fit=crop")
                .websiteUrl("https://puma.com")
                .build(),
                
            Brand.builder()
                .name("Under Armour")
                .slug("under-armour")
                .description("I Will - Performance athletic wear")
                .logoUrl("https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=300&h=300&fit=crop")
                .websiteUrl("https://underarmour.com")
                .build(),
                
            Brand.builder()
                .name("New Balance")
                .slug("new-balance")
                .description("Endorsed by No One - Premium athletic footwear")
                .logoUrl("https://images.unsplash.com/photo-1539185441755-769473a23570?w=300&h=300&fit=crop")
                .websiteUrl("https://newbalance.com")
                .build(),

            // Fast Fashion
            Brand.builder()
                .name("Zara")
                .slug("zara")
                .description("Fashion at the speed of life")
                .logoUrl("https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=300&h=300&fit=crop")
                .websiteUrl("https://zara.com")
                .build(),

            Brand.builder()
                .name("H&M")
                .slug("hm")
                .description("Fashion and quality at the best price")
                .logoUrl("https://images.unsplash.com/photo-1441984904996-e0b6ba687e04?w=300&h=300&fit=crop")
                .websiteUrl("https://hm.com")
                .build(),

            Brand.builder()
                .name("Uniqlo")
                .slug("uniqlo")
                .description("Made for all - Simple, quality clothing")
                .logoUrl("https://images.unsplash.com/photo-1558769132-cb1aea458c5e?w=300&h=300&fit=crop")
                .websiteUrl("https://uniqlo.com")
                .build(),

            Brand.builder()
                .name("Forever 21")
                .slug("forever21")
                .description("Forever young, forever trendy")
                .logoUrl("https://images.unsplash.com/photo-1445205170230-053b83016050?w=300&h=300&fit=crop")
                .websiteUrl("https://forever21.com")
                .build(),

            // Denim
            Brand.builder()
                .name("Levi's")
                .slug("levis")
                .description("Quality never goes out of style")
                .logoUrl("https://images.unsplash.com/photo-1582552938357-32b906df40cb?w=300&h=300&fit=crop")
                .websiteUrl("https://levi.com")
                .build(),

            Brand.builder()
                .name("Wrangler")
                .slug("wrangler")
                .description("Real. Comfortable. Jeans.")
                .logoUrl("https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=300&h=300&fit=crop")
                .websiteUrl("https://wrangler.com")
                .build(),

            // Premium/Luxury
            Brand.builder()
                .name("Calvin Klein")
                .slug("calvin-klein")
                .description("Modern, minimalist luxury fashion")
                .logoUrl("https://images.unsplash.com/photo-1434389677669-e08b4cac3105?w=300&h=300&fit=crop")
                .websiteUrl("https://calvinklein.com")
                .build(),

            Brand.builder()
                .name("Tommy Hilfiger")
                .slug("tommy-hilfiger")
                .description("Classic American cool style")
                .logoUrl("https://images.unsplash.com/photo-1556137370-30988834e76a?w=300&h=300&fit=crop")
                .websiteUrl("https://tommy.com")
                .build(),

            // Accessories
            Brand.builder()
                .name("Ray-Ban")
                .slug("ray-ban")
                .description("Never Hide - Iconic sunglasses since 1937")
                .logoUrl("https://images.unsplash.com/photo-1572635196237-14b3f281503f?w=300&h=300&fit=crop")
                .websiteUrl("https://ray-ban.com")
                .build()
        );

        brandRepository.saveAll(brands);
        log.info("Seeded {} brands with logos", brands.size());
    }

    private void seedCategoriesWithImages() {
        log.info("Seeding categories with images...");
        
        // Check if categories already exist to avoid duplicates
        if (categoryRepository.count() > 0) {
            log.info("Categories already exist, skipping category seeding");
            return;
        }

        // Root categories (Level 0)
        Category menCategory = Category.builder()
            .name("Men")
            .slug("men")
            .description("Men's fashion and clothing")
            .imageUrl("https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=800&h=600&fit=crop")
            .level(0)
            .sortOrder(1)
            .build();

        Category womenCategory = Category.builder()
            .name("Women")
            .slug("women")
            .description("Women's fashion and clothing")
            .imageUrl("https://images.unsplash.com/photo-1494790108755-2616c27d6d07?w=800&h=600&fit=crop")
            .level(0)
            .sortOrder(2)
            .build();

        Category kidsCategory = Category.builder()
            .name("Kids")
            .slug("kids")
            .description("Kids' fashion and clothing")
            .imageUrl("https://images.unsplash.com/photo-1503454537195-1dcabb73ffb9?w=800&h=600&fit=crop")
            .level(0)
            .sortOrder(3)
            .build();

        categoryRepository.saveAll(List.of(menCategory, womenCategory, kidsCategory));

        // Level 1 categories (Men)
        List<Category> menSubCategories = Arrays.asList(
            Category.builder()
                .name("Clothing")
                .slug("mens-clothing")
                .description("Men's clothing collection")
                .imageUrl("https://images.unsplash.com/photo-1516826435551-36a8a09e4526?w=800&h=600&fit=crop")
                .parentId(menCategory.getId())
                .level(1)
                .sortOrder(1)
                .build(),
            Category.builder()
                .name("Shoes")
                .slug("mens-shoes")
                .description("Men's footwear collection")
                .imageUrl("https://images.unsplash.com/photo-1449824913935-59a10b8d2000?w=800&h=600&fit=crop")
                .parentId(menCategory.getId())
                .level(1)
                .sortOrder(2)
                .build(),
            Category.builder()
                .name("Accessories")
                .slug("mens-accessories")
                .description("Men's accessories collection")
                .imageUrl("https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=800&h=600&fit=crop")
                .parentId(menCategory.getId())
                .level(1)
                .sortOrder(3)
                .build()
        );

        // Level 1 categories (Women)
        List<Category> womenSubCategories = Arrays.asList(
            Category.builder()
                .name("Clothing")
                .slug("womens-clothing")
                .description("Women's clothing collection")
                .imageUrl("https://images.unsplash.com/photo-1469334031218-e382a71b716b?w=800&h=600&fit=crop")
                .parentId(womenCategory.getId())
                .level(1)
                .sortOrder(1)
                .build(),
            Category.builder()
                .name("Shoes")
                .slug("womens-shoes")
                .description("Women's footwear collection")
                .imageUrl("https://images.unsplash.com/photo-1543163521-1bf539c55dd2?w=800&h=600&fit=crop")
                .parentId(womenCategory.getId())
                .level(1)
                .sortOrder(2)
                .build(),
            Category.builder()
                .name("Accessories")
                .slug("womens-accessories")
                .description("Women's accessories collection")
                .imageUrl("https://images.unsplash.com/photo-1469334031218-e382a71b716b?w=800&h=600&fit=crop")
                .parentId(womenCategory.getId())
                .level(1)
                .sortOrder(3)
                .build(),
            Category.builder()
                .name("Bags")
                .slug("womens-bags")
                .description("Women's bags and purses")
                .imageUrl("https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=800&h=600&fit=crop")
                .parentId(womenCategory.getId())
                .level(1)
                .sortOrder(4)
                .build()
        );

        // Level 1 categories (Kids)
        List<Category> kidsSubCategories = Arrays.asList(
            Category.builder()
                .name("Boys")
                .slug("boys")
                .description("Boys' clothing and accessories")
                .imageUrl("https://images.unsplash.com/photo-1519689373023-dd07c7988603?w=800&h=600&fit=crop")
                .parentId(kidsCategory.getId())
                .level(1)
                .sortOrder(1)
                .build(),
            Category.builder()
                .name("Girls")
                .slug("girls")
                .description("Girls' clothing and accessories")
                .imageUrl("https://images.unsplash.com/photo-1518623001395-125242310d0c?w=800&h=600&fit=crop")
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
        List<Category> mensClothingSubCategories = Arrays.asList(
            Category.builder()
                .name("T-Shirts")
                .slug("mens-t-shirts")
                .description("Men's t-shirts and casual tops")
                .imageUrl("https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=800&h=600&fit=crop")
                .parentId(mensClothing.getId())
                .level(2)
                .sortOrder(1)
                .build(),
            Category.builder()
                .name("Shirts")
                .slug("mens-shirts")
                .description("Men's dress and casual shirts")
                .imageUrl("https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=800&h=600&fit=crop")
                .parentId(mensClothing.getId())
                .level(2)
                .sortOrder(2)
                .build(),
            Category.builder()
                .name("Jeans")
                .slug("mens-jeans")
                .description("Men's denim jeans")
                .imageUrl("https://images.unsplash.com/photo-1582552938357-32b906df40cb?w=800&h=600&fit=crop")
                .parentId(mensClothing.getId())
                .level(2)
                .sortOrder(3)
                .build(),
            Category.builder()
                .name("Jackets")
                .slug("mens-jackets")
                .description("Men's jackets and outerwear")
                .imageUrl("https://images.unsplash.com/photo-1551698618-1dfe5d97d256?w=800&h=600&fit=crop")
                .parentId(mensClothing.getId())
                .level(2)
                .sortOrder(4)
                .build(),
            Category.builder()
                .name("Hoodies")
                .slug("mens-hoodies")
                .description("Men's hoodies and sweatshirts")
                .imageUrl("https://images.unsplash.com/photo-1556821840-3a63f95609a7?w=800&h=600&fit=crop")
                .parentId(mensClothing.getId())
                .level(2)
                .sortOrder(5)
                .build()
        );

        // Level 2 categories (Women's Clothing)
        Category womensClothing = womenSubCategories.get(0);
        List<Category> womensClothingSubCategories = Arrays.asList(
            Category.builder()
                .name("Dresses")
                .slug("womens-dresses")
                .description("Women's dresses and formal wear")
                .imageUrl("https://images.unsplash.com/photo-1595777457583-95e059d581b8?w=800&h=600&fit=crop")
                .parentId(womensClothing.getId())
                .level(2)
                .sortOrder(1)
                .build(),
            Category.builder()
                .name("Tops")
                .slug("womens-tops")
                .description("Women's tops and t-shirts")
                .imageUrl("https://images.unsplash.com/photo-1564257577-cd7cb00968b3?w=800&h=600&fit=crop")
                .parentId(womensClothing.getId())
                .level(2)
                .sortOrder(2)
                .build(),
            Category.builder()
                .name("Jeans")
                .slug("womens-jeans")
                .description("Women's denim jeans")
                .imageUrl("https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=800&h=600&fit=crop")
                .parentId(womensClothing.getId())
                .level(2)
                .sortOrder(3)
                .build(),
            Category.builder()
                .name("Blouses")
                .slug("womens-blouses")
                .description("Women's blouses and formal tops")
                .imageUrl("https://images.unsplash.com/photo-1551033406-611cf9a28f67?w=800&h=600&fit=crop")
                .parentId(womensClothing.getId())
                .level(2)
                .sortOrder(4)
                .build(),
            Category.builder()
                .name("Skirts")
                .slug("womens-skirts")
                .description("Women's skirts and midi wear")
                .imageUrl("https://images.unsplash.com/photo-1594633312681-425c7b97ccd1?w=800&h=600&fit=crop")
                .parentId(womensClothing.getId())
                .level(2)
                .sortOrder(5)
                .build()
        );

        categoryRepository.saveAll(mensClothingSubCategories);
        categoryRepository.saveAll(womensClothingSubCategories);

        log.info("Seeded comprehensive category structure with images");
    }

    private void seedUsers() {
        log.info("Seeding users...");

        // Admin users
        List<User> admins = Arrays.asList(
            User.builder()
                .email("admin@ecommerce.com")
                .passwordHash(passwordEncoder.encode("admin123"))
                .firstName("Admin")
                .lastName("User")
                .role(User.UserRole.ADMIN)
                .build(),
            User.builder()
                .email("manager@ecommerce.com")
                .passwordHash(passwordEncoder.encode("manager123"))
                .firstName("Store")
                .lastName("Manager")
                .role(User.UserRole.ADMIN)
                .build()
        );
        
        userRepository.saveAll(admins);

        // Regular customers
        String[][] customerData = {
            {"john.doe@email.com", "John", "Doe"},
            {"jane.smith@email.com", "Jane", "Smith"},
            {"mike.johnson@email.com", "Mike", "Johnson"},
            {"sarah.wilson@email.com", "Sarah", "Wilson"},
            {"david.brown@email.com", "David", "Brown"},
            {"lisa.davis@email.com", "Lisa", "Davis"},
            {"chris.miller@email.com", "Chris", "Miller"},
            {"emma.taylor@email.com", "Emma", "Taylor"},
            {"alex.anderson@email.com", "Alex", "Anderson"},
            {"sophia.moore@email.com", "Sophia", "Moore"},
            {"ryan.thomas@email.com", "Ryan", "Thomas"},
            {"olivia.jackson@email.com", "Olivia", "Jackson"},
            {"lucas.white@email.com", "Lucas", "White"},
            {"isabella.harris@email.com", "Isabella", "Harris"},
            {"noah.martin@email.com", "Noah", "Martin"},
            {"ava.thompson@email.com", "Ava", "Thompson"},
            {"ethan.garcia@email.com", "Ethan", "Garcia"},
            {"mia.martinez@email.com", "Mia", "Martinez"},
            {"william.robinson@email.com", "William", "Robinson"},
            {"charlotte.clark@email.com", "Charlotte", "Clark"},
            {"james.rodriguez@email.com", "James", "Rodriguez"},
            {"amelia.lewis@email.com", "Amelia", "Lewis"},
            {"benjamin.lee@email.com", "Benjamin", "Lee"},
            {"harper.walker@email.com", "Harper", "Walker"},
            {"henry.hall@email.com", "Henry", "Hall"},
            {"evelyn.allen@email.com", "Evelyn", "Allen"},
            {"sebastian.young@email.com", "Sebastian", "Young"},
            {"ella.hernandez@email.com", "Ella", "Hernandez"},
            {"jack.king@email.com", "Jack", "King"},
            {"scarlett.wright@email.com", "Scarlett", "Wright"},
            {"owen.lopez@email.com", "Owen", "Lopez"},
            {"grace.hill@email.com", "Grace", "Hill"},
            {"liam.scott@email.com", "Liam", "Scott"},
            {"chloe.green@email.com", "Chloe", "Green"},
            {"mason.adams@email.com", "Mason", "Adams"},
            {"lily.baker@email.com", "Lily", "Baker"},
            {"jacob.gonzalez@email.com", "Jacob", "Gonzalez"},
            {"zoe.nelson@email.com", "Zoe", "Nelson"},
            {"logan.carter@email.com", "Logan", "Carter"},
            {"nora.mitchell@email.com", "Nora", "Mitchell"},
            {"elijah.perez@email.com", "Elijah", "Perez"},
            {"hannah.roberts@email.com", "Hannah", "Roberts"},
            {"oliver.turner@email.com", "Oliver", "Turner"},
            {"madison.phillips@email.com", "Madison", "Phillips"},
            {"gabriel.campbell@email.com", "Gabriel", "Campbell"},
            {"avery.parker@email.com", "Avery", "Parker"},
            {"daniel.evans@email.com", "Daniel", "Evans"},
            {"layla.edwards@email.com", "Layla", "Edwards"},
            {"matthew.collins@email.com", "Matthew", "Collins"},
            {"aria.stewart@email.com", "Aria", "Stewart"}
        };

        List<User> customers = new ArrayList<>();
        for (String[] data : customerData) {
            customers.add(User.builder()
                .email(data[0])
                .passwordHash(passwordEncoder.encode("password123"))
                .firstName(data[1])
                .lastName(data[2])
                .role(User.UserRole.CUSTOMER)
                .build());
        }
        
        userRepository.saveAll(customers);

        // Add addresses for users
        seedAddressesForUsers();

        log.info("Seeded {} users ({} admins, {} customers)", 
                 admins.size() + customers.size(), admins.size(), customers.size());
    }

    private void seedAddressesForUsers() {
        log.info("Seeding addresses for users...");
        
        List<User> users = userRepository.findAll();
        List<Address> addresses = new ArrayList<>();
        
        String[][] addressData = {
            {"123 Main St, Apt 4B", "New York", "NY", "10001", "USA"},
            {"456 Oak Avenue", "Los Angeles", "CA", "90210", "USA"},
            {"789 Pine Street", "Chicago", "IL", "60601", "USA"},
            {"321 Elm Drive", "Houston", "TX", "77001", "USA"},
            {"654 Maple Lane", "Phoenix", "AZ", "85001", "USA"},
            {"987 Cedar Road", "Philadelphia", "PA", "19101", "USA"},
            {"147 Birch Way", "San Antonio", "TX", "78201", "USA"},
            {"258 Willow Court", "San Diego", "CA", "92101", "USA"},
            {"369 Spruce Circle", "Dallas", "TX", "75201", "USA"},
            {"741 Poplar Place", "San Jose", "CA", "95101", "USA"}
        };
        
        for (User user : users) {
            if (user.getRole() == User.UserRole.CUSTOMER) {
                // Each user gets 1-3 addresses
                int addressCount = random.nextInt(3) + 1;
                
                for (int i = 0; i < addressCount; i++) {
                    String[] addressInfo = addressData[random.nextInt(addressData.length)];
                    addresses.add(Address.builder()
                        .user(user)
                        .street(addressInfo[0])
                        .city(addressInfo[1])
                        .state(addressInfo[2])
                        .zipCode(addressInfo[3])
                        .country(addressInfo[4])
                        .isDefault(i == 0) // First address is default
                        .build());
                }
            }
        }
        
        addressRepository.saveAll(addresses);
        log.info("Seeded {} addresses for users", addresses.size());
    }

    @Transactional
    public void seedProductsWithImages() {
        log.info("Seeding products with images...");
        
        // Get all necessary data
        List<Brand> brands = brandRepository.findAll();
        List<Category> categories = categoryRepository.findAll();
        
        Map<String, Brand> brandMap = brands.stream()
            .collect(Collectors.toMap(Brand::getSlug, brand -> brand));
        Map<String, Category> categoryMap = categories.stream()
            .collect(Collectors.toMap(Category::getSlug, category -> category));
        
        List<Product> allProducts = new ArrayList<>();
        
        // Men's T-Shirts
        allProducts.addAll(createMensTShirts(brandMap, categoryMap));
        
        // Men's Shirts  
        allProducts.addAll(createMensShirts(brandMap, categoryMap));
        
        // Men's Jeans
        allProducts.addAll(createMensJeans(brandMap, categoryMap));
        
        // Men's Jackets
        allProducts.addAll(createMensJackets(brandMap, categoryMap));
        
        // Men's Hoodies
        allProducts.addAll(createMensHoodies(brandMap, categoryMap));
        
        // Men's Shoes
        allProducts.addAll(createMensShoes(brandMap, categoryMap));
        
        // Women's Dresses
        allProducts.addAll(createWomensDresses(brandMap, categoryMap));
        
        // Women's Tops
        allProducts.addAll(createWomensTops(brandMap, categoryMap));
        
        // Women's Jeans
        allProducts.addAll(createWomensJeans(brandMap, categoryMap));
        
        // Women's Blouses
        allProducts.addAll(createWomensBlouses(brandMap, categoryMap));
        
        // Women's Shoes
        allProducts.addAll(createWomensShoes(brandMap, categoryMap));
        
        // Women's Accessories
        allProducts.addAll(createWomensAccessories(brandMap, categoryMap));
        
        log.info("Created {} products total", allProducts.size());
    }

    private List<Product> createMensTShirts(Map<String, Brand> brandMap, Map<String, Category> categoryMap) {
        Category category = categoryMap.get("mens-t-shirts");
        List<Product> products = new ArrayList<>();
        
        String[][] tShirtData = {
            {"Nike Dri-FIT Classic T-Shirt", "Comfortable cotton t-shirt with moisture-wicking technology", "29.99", "nike", "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=800&h=600&fit=crop"},
            {"Adidas Essentials 3-Stripes Tee", "Classic athletic tee with iconic stripes", "24.99", "adidas", "https://images.unsplash.com/photo-1556906781-9a412961c28c?w=800&h=600&fit=crop"},
            {"Under Armour Tech 2.0 Shirt", "Lightweight, quick-dry fabric", "27.99", "under-armour", "https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=800&h=600&fit=crop"},
            {"Puma Essential Logo Tee", "Soft cotton blend with signature logo", "22.99", "puma", "https://images.unsplash.com/photo-1449824913935-59a10b8d2000?w=800&h=600&fit=crop"},
            {"Uniqlo Airism Cotton T-Shirt", "Breathable and comfortable for daily wear", "19.99", "uniqlo", "https://images.unsplash.com/photo-1558769132-cb1aea458c5e?w=800&h=600&fit=crop"},
            {"H&M Basic Cotton Tee", "Simple and versatile everyday t-shirt", "12.99", "hm", "https://images.unsplash.com/photo-1441984904996-e0b6ba687e04?w=800&h=600&fit=crop"},
            {"Tommy Hilfiger Classic T-Shirt", "Premium cotton with signature flag logo", "34.99", "tommy-hilfiger", "https://images.unsplash.com/photo-1556137370-30988834e76a?w=800&h=600&fit=crop"},
            {"Calvin Klein Cotton Tee", "Modern minimalist design", "32.99", "calvin-klein", "https://images.unsplash.com/photo-1434389677669-e08b4cac3105?w=800&h=600&fit=crop"},
            {"Zara Basic T-Shirt", "Trendy fit with modern styling", "16.99", "zara", "https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=800&h=600&fit=crop"},
            {"Forever 21 Graphic Tee", "Casual tee with printed graphics", "14.99", "forever21", "https://images.unsplash.com/photo-1445205170230-053b83016050?w=800&h=600&fit=crop"}
        };
        
        for (String[] data : tShirtData) {
            Brand brand = brandMap.get(data[3]);
            if (brand != null && category != null) {
                Product product = createProductWithVariants(
                    data[0], data[1], new BigDecimal(data[2]), 
                    brand.getId(), category.getId(), GenderType.MEN, data[4]);
                products.add(product);
            }
        }
        
        return products;
    }

    private List<Product> createMensShirts(Map<String, Brand> brandMap, Map<String, Category> categoryMap) {
        Category category = categoryMap.get("mens-shirts");
        List<Product> products = new ArrayList<>();
        
        String[][] shirtData = {
            {"Tommy Hilfiger Oxford Shirt", "Classic button-down Oxford shirt", "79.99", "tommy-hilfiger", "https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=800&h=600&fit=crop"},
            {"Calvin Klein Dress Shirt", "Slim fit dress shirt for business", "69.99", "calvin-klein", "https://images.unsplash.com/photo-1434389677669-e08b4cac3105?w=800&h=600&fit=crop"},
            {"Zara Slim Fit Shirt", "Modern slim fit casual shirt", "49.99", "zara", "https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=800&h=600&fit=crop"},
            {"H&M Cotton Shirt", "Comfortable cotton blend shirt", "34.99", "hm", "https://images.unsplash.com/photo-1441984904996-e0b6ba687e04?w=800&h=600&fit=crop"},
            {"Uniqlo Easy Care Shirt", "Wrinkle-resistant dress shirt", "39.99", "uniqlo", "https://images.unsplash.com/photo-1558769132-cb1aea458c5e?w=800&h=600&fit=crop"},
            {"Nike Golf Polo Shirt", "Performance polo for active wear", "54.99", "nike", "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&h=600&fit=crop"},
            {"Adidas Golf Shirt", "Moisture-wicking golf polo", "49.99", "adidas", "https://images.unsplash.com/photo-1556906781-9a412961c28c?w=800&h=600&fit=crop"},
            {"Under Armour Polo", "Flexible fabric polo shirt", "44.99", "under-armour", "https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=800&h=600&fit=crop"},
            {"Puma Golf Polo", "Lightweight performance polo", "42.99", "puma", "https://images.unsplash.com/photo-1449824913935-59a10b8d2000?w=800&h=600&fit=crop"},
            {"Forever 21 Casual Shirt", "Trendy casual button-up", "29.99", "forever21", "https://images.unsplash.com/photo-1445205170230-053b83016050?w=800&h=600&fit=crop"}
        };
        
        for (String[] data : shirtData) {
            Brand brand = brandMap.get(data[3]);
            if (brand != null && category != null) {
                Product product = createProductWithVariants(
                    data[0], data[1], new BigDecimal(data[2]), 
                    brand.getId(), category.getId(), GenderType.MEN, data[4]);
                products.add(product);
            }
        }
        
        return products;
    }

    private List<Product> createMensJeans(Map<String, Brand> brandMap, Map<String, Category> categoryMap) {
        Category category = categoryMap.get("mens-jeans");
        List<Product> products = new ArrayList<>();
        
        String[][] jeansData = {
            {"Levi's 501 Original Fit", "The original blue jean with classic straight fit", "89.99", "levis", "https://images.unsplash.com/photo-1582552938357-32b906df40cb?w=800&h=600&fit=crop"},
            {"Wrangler Cowboy Cut", "Authentic western-style jeans", "64.99", "wrangler", "https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=800&h=600&fit=crop"},
            {"Calvin Klein Skinny Jeans", "Modern skinny fit denim", "79.99", "calvin-klein", "https://images.unsplash.com/photo-1434389677669-e08b4cac3105?w=800&h=600&fit=crop"},
            {"Tommy Hilfiger Relaxed Fit", "Comfortable relaxed fit jeans", "84.99", "tommy-hilfiger", "https://images.unsplash.com/photo-1556137370-30988834e76a?w=800&h=600&fit=crop"},
            {"Zara Slim Fit Jeans", "Contemporary slim fit styling", "59.99", "zara", "https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=800&h=600&fit=crop"},
            {"H&M Slim Jeans", "Affordable slim fit denim", "34.99", "hm", "https://images.unsplash.com/photo-1441984904996-e0b6ba687e04?w=800&h=600&fit=crop"},
            {"Uniqlo Stretch Jeans", "Comfortable stretch denim", "49.99", "uniqlo", "https://images.unsplash.com/photo-1558769132-cb1aea458c5e?w=800&h=600&fit=crop"},
            {"Forever 21 Distressed Jeans", "Trendy distressed styling", "39.99", "forever21", "https://images.unsplash.com/photo-1445205170230-053b83016050?w=800&h=600&fit=crop"},
            {"Levi's 511 Slim Fit", "Modern slim fit with stretch", "79.99", "levis", "https://images.unsplash.com/photo-1582552938357-32b906df40cb?w=800&h=600&fit=crop"},
            {"Wrangler Straight Fit", "Classic straight leg denim", "59.99", "wrangler", "https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=800&h=600&fit=crop"}
        };
        
        for (String[] data : jeansData) {
            Brand brand = brandMap.get(data[3]);
            if (brand != null && category != null) {
                Product product = createJeansWithSizes(
                    data[0], data[1], new BigDecimal(data[2]), 
                    brand.getId(), category.getId(), GenderType.MEN, data[4]);
                products.add(product);
            }
        }
        
        return products;
    }

    private List<Product> createMensJackets(Map<String, Brand> brandMap, Map<String, Category> categoryMap) {
        Category category = categoryMap.get("mens-jackets");
        List<Product> products = new ArrayList<>();
        
        String[][] jacketData = {
            {"Nike Windrunner Jacket", "Lightweight windbreaker for active wear", "89.99", "nike", "https://images.unsplash.com/photo-1551698618-1dfe5d97d256?w=800&h=600&fit=crop"},
            {"Adidas Track Jacket", "Classic 3-stripes track jacket", "74.99", "adidas", "https://images.unsplash.com/photo-1556906781-9a412961c28c?w=800&h=600&fit=crop"},
            {"Under Armour Storm Jacket", "Water-resistant outdoor jacket", "109.99", "under-armour", "https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=800&h=600&fit=crop"},
            {"Puma Essentials Jacket", "Essential track jacket for everyday", "69.99", "puma", "https://images.unsplash.com/photo-1449824913935-59a10b8d2000?w=800&h=600&fit=crop"},
            {"Tommy Hilfiger Bomber", "Classic bomber jacket style", "129.99", "tommy-hilfiger", "https://images.unsplash.com/photo-1556137370-30988834e76a?w=800&h=600&fit=crop"},
            {"Calvin Klein Harrington", "Modern Harrington jacket", "119.99", "calvin-klein", "https://images.unsplash.com/photo-1434389677669-e08b4cac3105?w=800&h=600&fit=crop"},
            {"Zara Biker Jacket", "Trendy faux leather jacket", "79.99", "zara", "https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=800&h=600&fit=crop"},
            {"H&M Denim Jacket", "Classic denim trucker jacket", "44.99", "hm", "https://images.unsplash.com/photo-1441984904996-e0b6ba687e04?w=800&h=600&fit=crop"},
            {"Uniqlo Pocketable Jacket", "Packable lightweight jacket", "59.99", "uniqlo", "https://images.unsplash.com/photo-1558769132-cb1aea458c5e?w=800&h=600&fit=crop"},
            {"Forever 21 Utility Jacket", "Casual utility style jacket", "49.99", "forever21", "https://images.unsplash.com/photo-1445205170230-053b83016050?w=800&h=600&fit=crop"}
        };
        
        for (String[] data : jacketData) {
            Brand brand = brandMap.get(data[3]);
            if (brand != null && category != null) {
                Product product = createProductWithVariants(
                    data[0], data[1], new BigDecimal(data[2]), 
                    brand.getId(), category.getId(), GenderType.MEN, data[4]);
                products.add(product);
            }
        }
        
        return products;
    }

    private List<Product> createMensHoodies(Map<String, Brand> brandMap, Map<String, Category> categoryMap) {
        Category category = categoryMap.get("mens-hoodies");
        List<Product> products = new ArrayList<>();
        
        String[][] hoodieData = {
            {"Nike Club Fleece Hoodie", "Soft fleece hoodie for comfort", "54.99", "nike", "https://images.unsplash.com/photo-1556821840-3a63f95609a7?w=800&h=600&fit=crop"},
            {"Adidas Essentials Hoodie", "Classic pullover hoodie", "49.99", "adidas", "https://images.unsplash.com/photo-1556906781-9a412961c28c?w=800&h=600&fit=crop"},
            {"Under Armour Rival Hoodie", "Comfortable cotton blend hoodie", "44.99", "under-armour", "https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=800&h=600&fit=crop"},
            {"Puma Essential Hoodie", "Essential fleece pullover", "39.99", "puma", "https://images.unsplash.com/photo-1449824913935-59a10b8d2000?w=800&h=600&fit=crop"},
            {"Tommy Hilfiger Logo Hoodie", "Classic hoodie with signature logo", "69.99", "tommy-hilfiger", "https://images.unsplash.com/photo-1556137370-30988834e76a?w=800&h=600&fit=crop"},
            {"Calvin Klein Modern Hoodie", "Contemporary fit hoodie", "64.99", "calvin-klein", "https://images.unsplash.com/photo-1434389677669-e08b4cac3105?w=800&h=600&fit=crop"},
            {"Zara Basic Hoodie", "Simple pullover hoodie", "32.99", "zara", "https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=800&h=600&fit=crop"},
            {"H&M Relaxed Hoodie", "Oversized comfortable hoodie", "24.99", "hm", "https://images.unsplash.com/photo-1441984904996-e0b6ba687e04?w=800&h=600&fit=crop"},
            {"Uniqlo Sweat Pullover", "Quality sweat pullover hoodie", "39.99", "uniqlo", "https://images.unsplash.com/photo-1558769132-cb1aea458c5e?w=800&h=600&fit=crop"},
            {"Forever 21 Graphic Hoodie", "Trendy graphic print hoodie", "29.99", "forever21", "https://images.unsplash.com/photo-1445205170230-053b83016050?w=800&h=600&fit=crop"}
        };
        
        for (String[] data : hoodieData) {
            Brand brand = brandMap.get(data[3]);
            if (brand != null && category != null) {
                Product product = createProductWithVariants(
                    data[0], data[1], new BigDecimal(data[2]), 
                    brand.getId(), category.getId(), GenderType.MEN, data[4]);
                products.add(product);
            }
        }
        
        return products;
    }

    private List<Product> createMensShoes(Map<String, Brand> brandMap, Map<String, Category> categoryMap) {
        Category category = categoryMap.get("mens-shoes");
        List<Product> products = new ArrayList<>();
        
        String[][] shoesData = {
            {"Nike Air Max 90", "Classic sneakers with visible Air cushioning", "119.99", "nike", "https://images.unsplash.com/photo-1549298916-b41d501d3772?w=800&h=600&fit=crop"},
            {"Adidas Stan Smith", "Iconic white leather sneakers", "89.99", "adidas", "https://images.unsplash.com/photo-1549298916-b41d501d3772?w=800&h=600&fit=crop"},
            {"Nike Revolution 5", "Comfortable running shoes", "64.99", "nike", "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&h=600&fit=crop"},
            {"New Balance 990v5", "Premium made in USA sneakers", "174.99", "new-balance", "https://images.unsplash.com/photo-1539185441755-769473a23570?w=800&h=600&fit=crop"},
            {"Puma Suede Classic", "Retro basketball-inspired sneakers", "74.99", "puma", "https://images.unsplash.com/photo-1449824913935-59a10b8d2000?w=800&h=600&fit=crop"},
            {"Under Armour Charged", "Performance running shoes", "89.99", "under-armour", "https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=800&h=600&fit=crop"},
            {"Nike Air Force 1", "Classic basketball shoes", "99.99", "nike", "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&h=600&fit=crop"},
            {"Adidas Ultraboost", "Premium running shoes with boost", "179.99", "adidas", "https://images.unsplash.com/photo-1556906781-9a412961c28c?w=800&h=600&fit=crop"},
            {"New Balance Fresh Foam", "Comfortable everyday sneakers", "79.99", "new-balance", "https://images.unsplash.com/photo-1539185441755-769473a23570?w=800&h=600&fit=crop"},
            {"Puma RS-X", "Bold retro-inspired sneakers", "109.99", "puma", "https://images.unsplash.com/photo-1449824913935-59a10b8d2000?w=800&h=600&fit=crop"}
        };
        
        for (String[] data : shoesData) {
            Brand brand = brandMap.get(data[3]);
            if (brand != null && category != null) {
                Product product = createShoesWithSizes(
                    data[0], data[1], new BigDecimal(data[2]), 
                    brand.getId(), category.getId(), GenderType.MEN, data[4]);
                products.add(product);
            }
        }
        
        return products;
    }

    private List<Product> createWomensDresses(Map<String, Brand> brandMap, Map<String, Category> categoryMap) {
        Category category = categoryMap.get("womens-dresses");
        List<Product> products = new ArrayList<>();
        
        String[][] dressData = {
            {"Zara Floral Midi Dress", "Elegant floral print midi dress", "59.99", "zara", "https://images.unsplash.com/photo-1595777457583-95e059d581b8?w=800&h=600&fit=crop"},
            {"H&M Wrap Dress", "Versatile wrap-style dress", "34.99", "hm", "https://images.unsplash.com/photo-1594633312681-425c7b97ccd1?w=800&h=600&fit=crop"},
            {"Forever 21 Mini Dress", "Trendy mini dress for nights out", "24.99", "forever21", "https://images.unsplash.com/photo-1566479179817-c0b24b7b5f92?w=800&h=600&fit=crop"},
            {"Tommy Hilfiger Shirt Dress", "Classic shirt dress style", "89.99", "tommy-hilfiger", "https://images.unsplash.com/photo-1572804013309-59a88b7e92f1?w=800&h=600&fit=crop"},
            {"Calvin Klein Sheath Dress", "Professional sheath dress", "94.99", "calvin-klein", "https://images.unsplash.com/photo-1566479179817-c0b24b7b5f92?w=800&h=600&fit=crop"},
            {"Zara Printed Dress", "Contemporary printed design", "49.99", "zara", "https://images.unsplash.com/photo-1595777457583-95e059d581b8?w=800&h=600&fit=crop"},
            {"H&M Bodycon Dress", "Figure-flattering bodycon style", "29.99", "hm", "https://images.unsplash.com/photo-1594633312681-425c7b97ccd1?w=800&h=600&fit=crop"},
            {"Uniqlo T-Shirt Dress", "Comfortable casual t-shirt dress", "29.99", "uniqlo", "https://images.unsplash.com/photo-1572804013309-59a88b7e92f1?w=800&h=600&fit=crop"},
            {"Forever 21 Maxi Dress", "Flowy maxi dress for summer", "34.99", "forever21", "https://images.unsplash.com/photo-1566479179817-c0b24b7b5f92?w=800&h=600&fit=crop"},
            {"Zara A-Line Dress", "Classic A-line silhouette", "54.99", "zara", "https://images.unsplash.com/photo-1595777457583-95e059d581b8?w=800&h=600&fit=crop"}
        };
        
        for (String[] data : dressData) {
            Brand brand = brandMap.get(data[3]);
            if (brand != null && category != null) {
                Product product = createProductWithVariants(
                    data[0], data[1], new BigDecimal(data[2]), 
                    brand.getId(), category.getId(), GenderType.WOMEN, data[4]);
                products.add(product);
            }
        }
        
        return products;
    }

    private List<Product> createWomensTops(Map<String, Brand> brandMap, Map<String, Category> categoryMap) {
        Category category = categoryMap.get("womens-tops");
        List<Product> products = new ArrayList<>();
        
        String[][] topData = {
            {"Nike Dri-FIT Women's Top", "Performance athletic top", "32.99", "nike", "https://images.unsplash.com/photo-1564257577-cd7cb00968b3?w=800&h=600&fit=crop"},
            {"Adidas 3-Stripes Top", "Classic athletic top with stripes", "27.99", "adidas", "https://images.unsplash.com/photo-1564257577-cd7cb00968b3?w=800&h=600&fit=crop"},
            {"Under Armour Tech Top", "Moisture-wicking training top", "29.99", "under-armour", "https://images.unsplash.com/photo-1564257577-cd7cb00968b3?w=800&h=600&fit=crop"},
            {"Puma Essential Top", "Comfortable everyday top", "24.99", "puma", "https://images.unsplash.com/photo-1564257577-cd7cb00968b3?w=800&h=600&fit=crop"},
            {"Zara Basic Top", "Versatile basic top", "19.99", "zara", "https://images.unsplash.com/photo-1564257577-cd7cb00968b3?w=800&h=600&fit=crop"},
            {"H&M V-Neck Top", "Classic v-neck style", "14.99", "hm", "https://images.unsplash.com/photo-1564257577-cd7cb00968b3?w=800&h=600&fit=crop"},
            {"Uniqlo Supima Cotton Top", "Premium cotton blend top", "24.99", "uniqlo", "https://images.unsplash.com/photo-1564257577-cd7cb00968b3?w=800&h=600&fit=crop"},
            {"Forever 21 Crop Top", "Trendy cropped style", "12.99", "forever21", "https://images.unsplash.com/photo-1564257577-cd7cb00968b3?w=800&h=600&fit=crop"},
            {"Tommy Hilfiger Logo Top", "Classic top with logo detail", "39.99", "tommy-hilfiger", "https://images.unsplash.com/photo-1564257577-cd7cb00968b3?w=800&h=600&fit=crop"},
            {"Calvin Klein Essential Top", "Minimalist design top", "34.99", "calvin-klein", "https://images.unsplash.com/photo-1564257577-cd7cb00968b3?w=800&h=600&fit=crop"}
        };
        
        for (String[] data : topData) {
            Brand brand = brandMap.get(data[3]);
            if (brand != null && category != null) {
                Product product = createProductWithVariants(
                    data[0], data[1], new BigDecimal(data[2]), 
                    brand.getId(), category.getId(), GenderType.WOMEN, data[4]);
                products.add(product);
            }
        }
        
        return products;
    }

    private List<Product> createWomensJeans(Map<String, Brand> brandMap, Map<String, Category> categoryMap) {
        Category category = categoryMap.get("womens-jeans");
        List<Product> products = new ArrayList<>();
        
        String[][] jeansData = {
            {"Levi's 721 High Rise Skinny", "Modern high-rise skinny jeans", "89.99", "levis", "https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=800&h=600&fit=crop"},
            {"Wrangler Retro Mae", "Mid-rise bootcut jeans", "69.99", "wrangler", "https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=800&h=600&fit=crop"},
            {"Calvin Klein High Rise", "Premium high-rise jeans", "84.99", "calvin-klein", "https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=800&h=600&fit=crop"},
            {"Tommy Hilfiger Straight Leg", "Classic straight leg fit", "79.99", "tommy-hilfiger", "https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=800&h=600&fit=crop"},
            {"Zara High Waist Jeans", "Trendy high-waist styling", "49.99", "zara", "https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=800&h=600&fit=crop"},
            {"H&M Skinny High Jeans", "Affordable skinny fit", "29.99", "hm", "https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=800&h=600&fit=crop"},
            {"Uniqlo High Rise Jeans", "Comfortable high-rise cut", "49.99", "uniqlo", "https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=800&h=600&fit=crop"},
            {"Forever 21 Ripped Jeans", "Distressed style jeans", "32.99", "forever21", "https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=800&h=600&fit=crop"},
            {"Levi's 501 Crop", "Cropped original fit", "79.99", "levis", "https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=800&h=600&fit=crop"},
            {"Wrangler Straight Fit", "Classic straight fit jeans", "64.99", "wrangler", "https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=800&h=600&fit=crop"}
        };
        
        for (String[] data : jeansData) {
            Brand brand = brandMap.get(data[3]);
            if (brand != null && category != null) {
                Product product = createJeansWithSizes(
                    data[0], data[1], new BigDecimal(data[2]), 
                    brand.getId(), category.getId(), GenderType.WOMEN, data[4]);
                products.add(product);
            }
        }
        
        return products;
    }

    private List<Product> createWomensBlouses(Map<String, Brand> brandMap, Map<String, Category> categoryMap) {
        Category category = categoryMap.get("womens-blouses");
        List<Product> products = new ArrayList<>();
        
        String[][] blouseData = {
            {"Tommy Hilfiger Silk Blouse", "Elegant silk blouse for office", "89.99", "tommy-hilfiger", "https://images.unsplash.com/photo-1551033406-611cf9a28f67?w=800&h=600&fit=crop"},
            {"Calvin Klein Wrap Blouse", "Professional wrap-style blouse", "74.99", "calvin-klein", "https://images.unsplash.com/photo-1551033406-611cf9a28f67?w=800&h=600&fit=crop"},
            {"Zara Satin Blouse", "Luxurious satin finish blouse", "49.99", "zara", "https://images.unsplash.com/photo-1551033406-611cf9a28f67?w=800&h=600&fit=crop"},
            {"H&M Chiffon Blouse", "Light and airy chiffon style", "34.99", "hm", "https://images.unsplash.com/photo-1551033406-611cf9a28f67?w=800&h=600&fit=crop"},
            {"Uniqlo Rayon Blouse", "Smooth rayon fabric blouse", "39.99", "uniqlo", "https://images.unsplash.com/photo-1551033406-611cf9a28f67?w=800&h=600&fit=crop"},
            {"Forever 21 Ruffled Blouse", "Trendy ruffled detail blouse", "24.99", "forever21", "https://images.unsplash.com/photo-1551033406-611cf9a28f67?w=800&h=600&fit=crop"},
            {"Tommy Hilfiger Button Down", "Classic button-down blouse", "64.99", "tommy-hilfiger", "https://images.unsplash.com/photo-1551033406-611cf9a28f67?w=800&h=600&fit=crop"},
            {"Calvin Klein Sleeveless Blouse", "Modern sleeveless design", "59.99", "calvin-klein", "https://images.unsplash.com/photo-1551033406-611cf9a28f67?w=800&h=600&fit=crop"},
            {"Zara Printed Blouse", "Contemporary printed blouse", "44.99", "zara", "https://images.unsplash.com/photo-1551033406-611cf9a28f67?w=800&h=600&fit=crop"},
            {"H&M Long Sleeve Blouse", "Versatile long sleeve style", "29.99", "hm", "https://images.unsplash.com/photo-1551033406-611cf9a28f67?w=800&h=600&fit=crop"}
        };
        
        for (String[] data : blouseData) {
            Brand brand = brandMap.get(data[3]);
            if (brand != null && category != null) {
                Product product = createProductWithVariants(
                    data[0], data[1], new BigDecimal(data[2]), 
                    brand.getId(), category.getId(), GenderType.WOMEN, data[4]);
                products.add(product);
            }
        }
        
        return products;
    }

    private List<Product> createWomensShoes(Map<String, Brand> brandMap, Map<String, Category> categoryMap) {
        Category category = categoryMap.get("womens-shoes");
        List<Product> products = new ArrayList<>();
        
        String[][] shoesData = {
            {"Nike Air Max 270", "Modern lifestyle sneakers", "139.99", "nike", "https://images.unsplash.com/photo-1543163521-1bf539c55dd2?w=800&h=600&fit=crop"},
            {"Adidas Ultraboost 22", "Premium running shoes for women", "179.99", "adidas", "https://images.unsplash.com/photo-1543163521-1bf539c55dd2?w=800&h=600&fit=crop"},
            {"Nike Air Force 1", "Classic white sneakers", "99.99", "nike", "https://images.unsplash.com/photo-1543163521-1bf539c55dd2?w=800&h=600&fit=crop"},
            {"New Balance 327", "Retro-inspired lifestyle shoes", "89.99", "new-balance", "https://images.unsplash.com/photo-1543163521-1bf539c55dd2?w=800&h=600&fit=crop"},
            {"Puma Cali Sport", "California-inspired sneakers", "79.99", "puma", "https://images.unsplash.com/photo-1543163521-1bf539c55dd2?w=800&h=600&fit=crop"},
            {"Under Armour HOVR", "Responsive running shoes", "119.99", "under-armour", "https://images.unsplash.com/photo-1543163521-1bf539c55dd2?w=800&h=600&fit=crop"},
            {"Nike React Vision", "Comfortable everyday sneakers", "99.99", "nike", "https://images.unsplash.com/photo-1543163521-1bf539c55dd2?w=800&h=600&fit=crop"},
            {"Adidas Stan Smith", "Timeless white leather sneakers", "89.99", "adidas", "https://images.unsplash.com/photo-1543163521-1bf539c55dd2?w=800&h=600&fit=crop"},
            {"New Balance 574", "Classic lifestyle sneakers", "79.99", "new-balance", "https://images.unsplash.com/photo-1543163521-1bf539c55dd2?w=800&h=600&fit=crop"},
            {"Puma Suede Classic", "Iconic suede sneakers", "69.99", "puma", "https://images.unsplash.com/photo-1543163521-1bf539c55dd2?w=800&h=600&fit=crop"}
        };
        
        for (String[] data : shoesData) {
            Brand brand = brandMap.get(data[3]);
            if (brand != null && category != null) {
                Product product = createShoesWithSizes(
                    data[0], data[1], new BigDecimal(data[2]), 
                    brand.getId(), category.getId(), GenderType.WOMEN, data[4]);
                products.add(product);
            }
        }
        
        return products;
    }

    private List<Product> createWomensAccessories(Map<String, Brand> brandMap, Map<String, Category> categoryMap) {
        Category accessoriesCategory = categoryMap.get("womens-accessories");
        Category bagsCategory = categoryMap.get("womens-bags");
        List<Product> products = new ArrayList<>();
        
        // Accessories
        String[][] accessoryData = {
            {"Ray-Ban Aviator Sunglasses", "Classic aviator sunglasses", "154.99", "ray-ban", "https://images.unsplash.com/photo-1572635196237-14b3f281503f?w=800&h=600&fit=crop", "womens-accessories"},
            {"Tommy Hilfiger Leather Belt", "Classic leather belt with logo", "49.99", "tommy-hilfiger", "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=800&h=600&fit=crop", "womens-accessories"},
            {"Calvin Klein Watch", "Minimalist design watch", "129.99", "calvin-klein", "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=800&h=600&fit=crop", "womens-accessories"},
            {"Zara Chain Necklace", "Trendy layered chain necklace", "24.99", "zara", "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=800&h=600&fit=crop", "womens-accessories"},
            {"H&M Silk Scarf", "Elegant printed silk scarf", "19.99", "hm", "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=800&h=600&fit=crop", "womens-accessories"},
            
            // Bags
            {"Tommy Hilfiger Tote Bag", "Spacious leather tote bag", "149.99", "tommy-hilfiger", "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=800&h=600&fit=crop", "womens-bags"},
            {"Calvin Klein Crossbody", "Compact crossbody bag", "89.99", "calvin-klein", "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=800&h=600&fit=crop", "womens-bags"},
            {"Zara Mini Bag", "Trendy mini shoulder bag", "39.99", "zara", "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=800&h=600&fit=crop", "womens-bags"},
            {"H&M Canvas Tote", "Casual canvas tote bag", "24.99", "hm", "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=800&h=600&fit=crop", "womens-bags"},
            {"Forever 21 Backpack", "Stylish mini backpack", "29.99", "forever21", "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=800&h=600&fit=crop", "womens-bags"}
        };
        
        for (String[] data : accessoryData) {
            Brand brand = brandMap.get(data[3]);
            Category category = categoryMap.get(data[5]);
            if (brand != null && category != null) {
                Product product = createProductWithVariants(
                    data[0], data[1], new BigDecimal(data[2]), 
                    brand.getId(), category.getId(), GenderType.WOMEN, data[4]);
                products.add(product);
            }
        }
        
        return products;
    }

    private Product createProductWithVariants(String name, String description, BigDecimal basePrice, 
                                            UUID brandId, UUID categoryId, GenderType gender, String imageUrl) {
        Product product = Product.builder()
            .name(name)
            .description(description)
            .basePrice(basePrice)
            .brandId(brandId)
            .categoryId(categoryId)
            .gender(gender)
            .metadata(createProductMetadata())
            .build();
        
        productRepository.save(product);
        
        // Create variants
        List<ProductVariant> variants = createStandardVariants(product, basePrice);
        productVariantRepository.saveAll(variants);
        
        // Create product images
        createProductImages(product, imageUrl);
        
        return product;
    }

    private Product createJeansWithSizes(String name, String description, BigDecimal basePrice, 
                                       UUID brandId, UUID categoryId, GenderType gender, String imageUrl) {
        Product product = Product.builder()
            .name(name)
            .description(description)
            .basePrice(basePrice)
            .brandId(brandId)
            .categoryId(categoryId)
            .gender(gender)
            .metadata(createProductMetadata())
            .build();
        
        productRepository.save(product);
        
        // Create jeans variants with waist sizes
        List<ProductVariant> variants = createJeansVariants(product, basePrice, gender);
        productVariantRepository.saveAll(variants);
        
        // Create product images
        createProductImages(product, imageUrl);
        
        return product;
    }

    private Product createShoesWithSizes(String name, String description, BigDecimal basePrice, 
                                       UUID brandId, UUID categoryId, GenderType gender, String imageUrl) {
        Product product = Product.builder()
            .name(name)
            .description(description)
            .basePrice(basePrice)
            .brandId(brandId)
            .categoryId(categoryId)
            .gender(gender)
            .metadata(createProductMetadata())
            .build();
        
        productRepository.save(product);
        
        // Create shoe variants with numeric sizes
        List<ProductVariant> variants = createShoeVariants(product, basePrice, gender);
        productVariantRepository.saveAll(variants);
        
        // Create product images
        createProductImages(product, imageUrl);
        
        return product;
    }

    private List<ProductVariant> createStandardVariants(Product product, BigDecimal basePrice) {
        List<ProductVariant> variants = new ArrayList<>();
        String[] sizes = {"XS", "S", "M", "L", "XL", "XXL"};
        String[] colors = {"BLACK", "WHITE", "NAVY", "GRAY"};
        
        int counter = 1;
        for (String size : sizes) {
            for (String color : colors) {
                if (counter <= 8) { // Limit variants per product
                    String sku = generateSKU(product.getName(), size, color, counter);
                    
                    Map<String, Object> attributes = new HashMap<>();
                    attributes.put("size", size);
                    attributes.put("color", color);
                    
                    variants.add(ProductVariant.builder()
                        .product(product)
                        .sku(sku)
                        .attributes(attributes)
                        .price(basePrice)
                        .stock(random.nextInt(30) + 10) // 10-40 stock
                        .build());
                    counter++;
                }
            }
        }
        
        return variants;
    }

    private List<ProductVariant> createJeansVariants(Product product, BigDecimal basePrice, GenderType gender) {
        List<ProductVariant> variants = new ArrayList<>();
        String[] sizes = gender == GenderType.MEN ? 
            new String[]{"30x30", "30x32", "32x30", "32x32", "34x32", "36x32"} :
            new String[]{"24", "25", "26", "27", "28", "29", "30", "31"};
        String[] colors = {"BLUE", "BLACK", "GRAY", "WHITE"};
        
        int counter = 1;
        for (String size : sizes) {
            for (String color : colors) {
                String sku = generateSKU(product.getName(), size, color, counter);
                
                Map<String, Object> attributes = new HashMap<>();
                attributes.put("size", size);
                attributes.put("color", color);
                
                variants.add(ProductVariant.builder()
                    .product(product)
                    .sku(sku)
                    .attributes(attributes)
                    .price(basePrice)
                    .stock(random.nextInt(25) + 5) // 5-30 stock
                    .build());
                counter++;
            }
        }
        
        return variants;
    }

    private List<ProductVariant> createShoeVariants(Product product, BigDecimal basePrice, GenderType gender) {
        List<ProductVariant> variants = new ArrayList<>();
        String[] sizes = gender == GenderType.MEN ? 
            new String[]{"7", "7.5", "8", "8.5", "9", "9.5", "10", "10.5", "11", "11.5", "12"} :
            new String[]{"5", "5.5", "6", "6.5", "7", "7.5", "8", "8.5", "9", "9.5", "10"};
        String[] colors = {"WHITE", "BLACK", "GRAY", "NAVY", "RED"};
        
        int counter = 1;
        for (String size : sizes) {
            for (String color : colors) {
                if (counter <= 15) { // Limit variants
                    String sku = generateSKU(product.getName(), size, color, counter);
                    
                    Map<String, Object> attributes = new HashMap<>();
                    attributes.put("size", size);
                    attributes.put("color", color);
                    
                    variants.add(ProductVariant.builder()
                        .product(product)
                        .sku(sku)
                        .attributes(attributes)
                        .price(basePrice)
                        .stock(random.nextInt(20) + 5) // 5-25 stock
                        .build());
                    counter++;
                }
            }
        }
        
        return variants;
    }

    private void createProductImages(Product product, String primaryImageUrl) {
        List<ProductImage> images = new ArrayList<>();
        
        // Primary product image
        images.add(ProductImage.builder()
            .productId(product.getId())
            .imageUrl(primaryImageUrl)
            .altText(product.getName() + " - Primary Image")
            .isPrimary(true)
            .sortOrder(0)
            .build());
        
        // Additional product images
        String[] additionalImages = {
            "https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=800&h=600&fit=crop",
            "https://images.unsplash.com/photo-1445205170230-053b83016050?w=800&h=600&fit=crop"
        };
        
        for (int i = 0; i < Math.min(2, additionalImages.length); i++) {
            images.add(ProductImage.builder()
                .productId(product.getId())
                .imageUrl(additionalImages[i])
                .altText(product.getName() + " - Additional Image " + (i + 1))
                .isPrimary(false)
                .sortOrder(i + 1)
                .build());
        }
        
        productImageRepository.saveAll(images);
    }

    private final AtomicInteger globalSkuCounter = new AtomicInteger(1);
    
    private String generateSKU(String productName, String size, String color, int counter) {
        String prefix = productName.replaceAll("[^a-zA-Z]", "").toUpperCase();
        if (prefix.length() > 8) {
            prefix = prefix.substring(0, 8);
        }
        int globalCounter = globalSkuCounter.getAndIncrement();
        return String.format("%s-%s-%s-%05d", prefix, size.replaceAll("[^a-zA-Z0-9]", ""), 
                           color.substring(0, Math.min(3, color.length())), globalCounter);
    }

    private Map<String, Object> createProductMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("material", "COTTON");
        metadata.put("season", "ALL_SEASON");
        metadata.put("occasion", "CASUAL");
        metadata.put("fit", "REGULAR");
        metadata.put("care_instructions", "Machine wash cold, tumble dry low");
        return metadata;
    }

    private void seedReviews() {
        log.info("Seeding reviews...");
        
        List<Product> products = productRepository.findAll();
        List<User> customers = userRepository.findAll().stream()
            .filter(user -> user.getRole() == User.UserRole.CUSTOMER)
            .collect(Collectors.toList());
        
        if (products.isEmpty() || customers.isEmpty()) {
            log.warn("No products or customers found for review seeding");
            return;
        }
        
        List<Review> reviews = new ArrayList<>();
        
        for (Product product : products) {
            // Each product gets 2-8 reviews
            int reviewCount = random.nextInt(7) + 2;
            Set<User> reviewers = new HashSet<>();
            
            for (int i = 0; i < reviewCount && reviewers.size() < reviewCount; i++) {
                User reviewer = customers.get(random.nextInt(customers.size()));
                if (!reviewers.contains(reviewer)) {
                    reviewers.add(reviewer);
                    
                    int rating = generateRealisticRating();
                    String comment = REVIEW_COMMENTS.get(random.nextInt(REVIEW_COMMENTS.size()));
                    
                    Review review = Review.builder()
                        .productId(product.getId())
                        .product(product)
                        .userId(reviewer.getId())
                        .user(reviewer)
                        .rating(rating)
                        .title(generateReviewTitle(rating))
                        .comment(comment)
                        .isVerifiedPurchase(random.nextBoolean())
                        .helpfulCount(random.nextInt(20))
                        .build();
                    
                    reviews.add(review);
                }
            }
        }
        
        reviewRepository.saveAll(reviews);
        
        // Update product rating statistics
        updateAllProductRatingStats();
        
        log.info("Seeded {} reviews across {} products", reviews.size(), products.size());
    }

    private int generateRealisticRating() {
        // Realistic rating distribution: mostly 4-5 stars
        double rand = random.nextDouble();
        if (rand < 0.45) return 5;      // 45% - 5 stars
        else if (rand < 0.75) return 4; // 30% - 4 stars  
        else if (rand < 0.85) return 3; // 10% - 3 stars
        else if (rand < 0.95) return 2; // 10% - 2 stars
        else return 1;                  // 5%  - 1 star
    }

    private String generateReviewTitle(int rating) {
        String[] titles5 = {"Excellent product!", "Love it!", "Perfect!", "Amazing quality", "Highly recommend"};
        String[] titles4 = {"Great product", "Very good", "Happy with purchase", "Good quality", "Satisfied"};
        String[] titles3 = {"It's okay", "Average quality", "Could be better", "Decent", "Fair"};
        String[] titles2 = {"Not impressed", "Below expectations", "Poor quality", "Disappointed", "Not great"};
        String[] titles1 = {"Terrible", "Very poor quality", "Waste of money", "Awful", "Don't buy"};
        
        switch (rating) {
            case 5: return titles5[random.nextInt(titles5.length)];
            case 4: return titles4[random.nextInt(titles4.length)];
            case 3: return titles3[random.nextInt(titles3.length)];
            case 2: return titles2[random.nextInt(titles2.length)];
            case 1: return titles1[random.nextInt(titles1.length)];
            default: return "Review";
        }
    }

    private void updateAllProductRatingStats() {
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
        }
        
        productRepository.saveAll(products);
        log.info("Updated rating statistics for {} products", products.size());
    }

    private void seedOrders() {
        log.info("Seeding orders...");
        
        List<User> customers = userRepository.findAll().stream()
            .filter(user -> user.getRole() == User.UserRole.CUSTOMER)
            .collect(Collectors.toList());
        List<ProductVariant> variants = productVariantRepository.findAll();
        List<Address> addresses = addressRepository.findAll();
        
        if (customers.isEmpty() || variants.isEmpty() || addresses.isEmpty()) {
            log.warn("Missing data for order seeding");
            return;
        }
        
        List<Order> orders = new ArrayList<>();
        
        for (User customer : customers) {
            // Each customer gets 1-6 orders
            int orderCount = random.nextInt(6) + 1;
            
            List<Address> customerAddresses = addresses.stream()
                .filter(addr -> addr.getUser().getId().equals(customer.getId()))
                .collect(Collectors.toList());
                
            if (customerAddresses.isEmpty()) continue;
            
            for (int i = 0; i < orderCount; i++) {
                Address randomAddress = customerAddresses.get(random.nextInt(customerAddresses.size()));
                
                Order order = createRandomOrder(customer, randomAddress, variants);
                if (order != null) {
                    orders.add(order);
                }
            }
        }
        
        orderRepository.saveAll(orders);
        log.info("Seeded {} orders for {} customers", orders.size(), customers.size());
    }

    private Order createRandomOrder(User customer, Address address, List<ProductVariant> allVariants) {
        // Random order with 1-5 items
        int itemCount = random.nextInt(5) + 1;
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        Set<ProductVariant> selectedVariants = new HashSet<>();
        
        for (int i = 0; i < itemCount; i++) {
            ProductVariant variant = allVariants.get(random.nextInt(allVariants.size()));
            if (!selectedVariants.contains(variant) && variant.getStock() > 0) {
                selectedVariants.add(variant);
                
                int quantity = random.nextInt(3) + 1; // 1-3 items
                BigDecimal unitPrice = variant.getPrice();
                BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
                totalAmount = totalAmount.add(itemTotal);
                
                OrderItem orderItem = OrderItem.builder()
                    .variant(variant)
                    .quantity(quantity)
                    .unitPrice(unitPrice)
                    .build();
                
                orderItems.add(orderItem);
            }
        }
        
        if (orderItems.isEmpty()) return null;
        
        // Add delivery fee
        BigDecimal deliveryFee = new BigDecimal("5.00");
        BigDecimal discountAmount = BigDecimal.ZERO;
        
        // Random discount (20% chance)
        if (random.nextDouble() < 0.2) {
            discountAmount = totalAmount.multiply(new BigDecimal("0.10")); // 10% discount
        }
        
        BigDecimal finalTotal = totalAmount.subtract(discountAmount).add(deliveryFee);
        
        Order order = Order.builder()
            .orderNumber(generateOrderNumber())
            .user(customer)
            .address(address)
            .originalAmount(totalAmount)
            .discountAmount(discountAmount)
            .deliveryFee(deliveryFee)
            .totalAmount(finalTotal)
            .status(generateRandomOrderStatus())
            .build();
        
        order.setOrderItems(orderItems);
        orderItems.forEach(item -> item.setOrder(order));
        
        return order;
    }

    private final AtomicInteger globalOrderCounter = new AtomicInteger(1);
    
    private String generateOrderNumber() {
        int orderNumber = globalOrderCounter.getAndIncrement();
        return String.format("ORD%06d", orderNumber);
    }

    private Order.OrderStatus generateRandomOrderStatus() {
        double rand = random.nextDouble();
        if (rand < 0.6) return Order.OrderStatus.COMPLETED; // 60%
        else if (rand < 0.8) return Order.OrderStatus.SHIPPED; // 20%
        else if (rand < 0.95) return Order.OrderStatus.PENDING; // 15%
        else return Order.OrderStatus.CANCELLED; // 5%
    }

    private void seedEvents() {
        log.info("Seeding events...");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime futureDate = now.plusMonths(3);
        LocalDateTime pastDate = now.minusMonths(1);

        List<Event> events = Arrays.asList(
            Event.builder()
                .name("Fashion\nSale")
                .description("Get amazing discounts on our fashion collection")
                .imageUrl("https://images.unsplash.com/photo-1445205170230-053b83016050?w=800&h=600&fit=crop")
                .startDate(pastDate)
                .endDate(futureDate)
                .status(Event.EventStatus.ACTIVE)
                .build(),

            Event.builder()
                .name("Summer\nCollection")
                .description("Discover our latest summer styles and trending looks")
                .imageUrl("https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=800&h=600&fit=crop")
                .startDate(now.minusWeeks(2))
                .endDate(futureDate.plusWeeks(4))
                .status(Event.EventStatus.ACTIVE)
                .build(),

            Event.builder()
                .name("Winter\nis Coming")
                .description("Prepare for winter with our cozy collection")
                .imageUrl("https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=800&h=600&fit=crop")
                .startDate(now.plusMonths(2))
                .endDate(futureDate.plusMonths(2))
                .status(Event.EventStatus.SCHEDULED)
                .build(),

            Event.builder()
                .name("Athletic\nWear Sale")
                .description("Performance gear at unbeatable prices")
                .imageUrl("https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=800&h=600&fit=crop")
                .startDate(pastDate.plusWeeks(1))
                .endDate(futureDate)
                .status(Event.EventStatus.ACTIVE)
                .build()
        );

        eventRepository.saveAll(events);

        // Create discounts for events
        Event fashionEvent = events.get(0);
        Discount fashionDiscount = Discount.builder()
            .eventId(fashionEvent.getId())
            .type(Discount.DiscountType.PERCENTAGE)
            .value(new BigDecimal("25.00"))
            .minPurchaseAmount(new BigDecimal("50.00"))
            .maxDiscountAmount(new BigDecimal("50.00"))
            .build();

        Event athleticEvent = events.get(3);
        Discount athleticDiscount = Discount.builder()
            .eventId(athleticEvent.getId())
            .type(Discount.DiscountType.PERCENTAGE)
            .value(new BigDecimal("20.00"))
            .minPurchaseAmount(new BigDecimal("75.00"))
            .maxDiscountAmount(new BigDecimal("40.00"))
            .build();

        discountRepository.saveAll(Arrays.asList(fashionDiscount, athleticDiscount));

        log.info("Seeded {} events with discounts", events.size());
    }

    private void seedEventProductLinks() {
        log.info("Linking products to events...");
        
        List<Event> events = eventRepository.findAll();
        List<Product> products = productRepository.findAll();
        
        if (events.isEmpty() || products.isEmpty()) {
            log.warn("No events or products found for linking");
            return;
        }
        
        for (Event event : events) {
            // Each event gets 10-20 random products
            int productCount = random.nextInt(11) + 10;
            Set<Product> eventProducts = new HashSet<>();
            
            for (int i = 0; i < productCount && eventProducts.size() < productCount; i++) {
                Product randomProduct = products.get(random.nextInt(products.size()));
                eventProducts.add(randomProduct);
            }
            
            event.setProducts(eventProducts);
            eventRepository.save(event);
        }
        
        log.info("Successfully linked products to all events");
    }
}