package com.charbel.ecommerce.address.service;

import com.charbel.ecommerce.address.dto.AddressResponse;
import com.charbel.ecommerce.address.dto.CreateAddressRequest;
import com.charbel.ecommerce.address.dto.UpdateAddressRequest;
import com.charbel.ecommerce.address.entity.Address;
import com.charbel.ecommerce.address.repository.AddressRepository;
import com.charbel.ecommerce.user.entity.User;
import com.charbel.ecommerce.user.repository.UserRepository;
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
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public List<AddressResponse> getUserAddresses(UUID userId) {
        log.debug("Fetching addresses for user: {}", userId);
        List<Address> addresses = addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
        return addresses.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public AddressResponse getAddressById(UUID addressId, UUID userId) {
        log.debug("Fetching address {} for user: {}", addressId, userId);
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new RuntimeException("Address not found"));
        return mapToResponse(address);
    }

    @Transactional
    public AddressResponse createAddress(CreateAddressRequest request, UUID userId) {
        log.info("Creating new address for user: {}", userId);

        User user = userRepository.findByIdAndNotDeleted(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // If this is set as default, clear existing default
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            addressRepository.clearAllDefaultFlagsForUser(userId);
        } else {
            // If no default exists and this is the first address, make it default
            if (!addressRepository.existsByUserIdAndIsDefaultTrue(userId)) {
                request.setIsDefault(true);
            }
        }

        Address address = Address.builder()
                .user(user)
                .street(request.getStreet())
                .city(request.getCity())
                .state(request.getState())
                .zipCode(request.getZipCode())
                .country(request.getCountry())
                .isDefault(request.getIsDefault())
                .build();

        Address savedAddress = addressRepository.save(address);
        log.info("Address created with ID: {}", savedAddress.getId());
        return mapToResponse(savedAddress);
    }

    @Transactional
    public AddressResponse updateAddress(UUID addressId, UpdateAddressRequest request, UUID userId) {
        log.info("Updating address {} for user: {}", addressId, userId);

        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setZipCode(request.getZipCode());
        address.setCountry(request.getCountry());

        Address savedAddress = addressRepository.save(address);
        log.info("Address updated: {}", addressId);
        return mapToResponse(savedAddress);
    }

    @Transactional
    public void deleteAddress(UUID addressId, UUID userId) {
        log.info("Soft deleting address {} for user: {}", addressId, userId);

        Address address = addressRepository.findByIdAndUserIdAndNotDeleted(addressId, userId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        // If deleting the default address, make another address default if available
        if (Boolean.TRUE.equals(address.getIsDefault())) {
            List<Address> userAddresses = addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
            // Find another address to make default (exclude the one being deleted)
            Address nextDefaultAddress = userAddresses.stream()
                    .filter(addr -> !addr.getId().equals(addressId))
                    .findFirst()
                    .orElse(null);

            if (nextDefaultAddress != null) {
                nextDefaultAddress.setIsDefault(true);
                addressRepository.save(nextDefaultAddress);
            }
        }

        address.softDelete();
        addressRepository.save(address);
        log.info("Address soft deleted: {}", addressId);
    }

    @Transactional
    public AddressResponse setDefaultAddress(UUID addressId, UUID userId) {
        log.info("Setting address {} as default for user: {}", addressId, userId);

        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        // Clear all default flags for the user
        addressRepository.clearAllDefaultFlagsForUser(userId);

        // Set the specified address as default
        address.setIsDefault(true);
        Address savedAddress = addressRepository.save(address);

        log.info("Address {} set as default for user: {}", addressId, userId);
        return mapToResponse(savedAddress);
    }

    private AddressResponse mapToResponse(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .street(address.getStreet())
                .city(address.getCity())
                .state(address.getState())
                .zipCode(address.getZipCode())
                .country(address.getCountry())
                .isDefault(address.getIsDefault())
                .createdAt(address.getCreatedAt())
                .updatedAt(address.getUpdatedAt())
                .build();
    }
}