package com.charbel.ecommerce.admin;

import com.charbel.ecommerce.product.dto.LowStockResponse;
import com.charbel.ecommerce.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin", description = "Admin management endpoints")
public class AdminController {

	private final ProductService productService;

	@GetMapping("/low-stock")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
		summary = "Get low stock products",
		description = "Returns products with stock less than 5. Admin only.",
		security = @SecurityRequirement(name = "bearerAuth")
	)
	public ResponseEntity<List<LowStockResponse>> getLowStockProducts() {
		log.info("Admin requesting low stock products");
		List<LowStockResponse> response = productService.getLowStockProducts();
		return ResponseEntity.ok(response);
	}
}