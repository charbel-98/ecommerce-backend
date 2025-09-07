package com.charbel.ecommerce.address.controller;

import com.charbel.ecommerce.address.dto.AddressResponse;
import com.charbel.ecommerce.address.dto.CreateAddressRequest;
import com.charbel.ecommerce.address.dto.UpdateAddressRequest;
import com.charbel.ecommerce.address.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.charbel.ecommerce.service.SecurityService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Addresses", description = "User address management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class AddressController {

    private final AddressService addressService;
    private final SecurityService securityService;

    @GetMapping
    @Operation(summary = "Get all addresses for the current user")
    public ResponseEntity<List<AddressResponse>> getUserAddresses() {
        UUID userId = getCurrentUserId();
        log.info("Fetching addresses for user: {}", userId);
        List<AddressResponse> addresses = addressService.getUserAddresses(userId);
        return ResponseEntity.ok(addresses);
    }

    @GetMapping("/{addressId}")
    @Operation(summary = "Get a specific address by ID")
    public ResponseEntity<AddressResponse> getAddressById(@PathVariable UUID addressId) {
        UUID userId = getCurrentUserId();
        log.info("Fetching address {} for user: {}", addressId, userId);
        AddressResponse address = addressService.getAddressById(addressId, userId);
        return ResponseEntity.ok(address);
    }

    @PostMapping
    @Operation(summary = "Create a new address")
    public ResponseEntity<AddressResponse> createAddress(@Valid @RequestBody CreateAddressRequest request) {
        UUID userId = getCurrentUserId();
        log.info("Creating new address for user: {}", userId);
        AddressResponse address = addressService.createAddress(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(address);
    }

    @PutMapping("/{addressId}")
    @Operation(summary = "Update an existing address")
    public ResponseEntity<AddressResponse> updateAddress(
            @PathVariable UUID addressId,
            @Valid @RequestBody UpdateAddressRequest request) {
        UUID userId = getCurrentUserId();
        log.info("Updating address {} for user: {}", addressId, userId);
        AddressResponse address = addressService.updateAddress(addressId, request, userId);
        return ResponseEntity.ok(address);
    }

    @DeleteMapping("/{addressId}")
    @Operation(summary = "Delete an address")
    public ResponseEntity<Void> deleteAddress(@PathVariable UUID addressId) {
        UUID userId = getCurrentUserId();
        log.info("Deleting address {} for user: {}", addressId, userId);
        addressService.deleteAddress(addressId, userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{addressId}/default")
    @Operation(summary = "Set an address as the default address")
    public ResponseEntity<AddressResponse> setDefaultAddress(@PathVariable UUID addressId) {
        UUID userId = getCurrentUserId();
        log.info("Setting address {} as default for user: {}", addressId, userId);
        AddressResponse address = addressService.setDefaultAddress(addressId, userId);
        return ResponseEntity.ok(address);
    }

    private UUID getCurrentUserId() {
        return securityService.getCurrentUserId();
    }
}