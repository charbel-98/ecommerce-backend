package com.charbel.ecommerce.address.repository;

import com.charbel.ecommerce.address.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AddressRepository extends JpaRepository<Address, UUID> {

    @Query("SELECT a FROM Address a WHERE a.isDeleted = false AND a.user.id = :userId ORDER BY a.isDefault DESC, a.createdAt DESC")
    List<Address> findByUserIdOrderByIsDefaultDescCreatedAtDesc(@Param("userId") UUID userId);

    @Query("SELECT a FROM Address a WHERE a.isDeleted = false AND a.id = :addressId AND a.user.id = :userId")
    Optional<Address> findByIdAndUserId(@Param("addressId") UUID addressId, @Param("userId") UUID userId);

    @Query("SELECT a FROM Address a WHERE a.isDeleted = false AND a.user.id = :userId AND a.isDefault = true")
    Optional<Address> findByUserIdAndIsDefaultTrue(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.isDeleted = false AND a.user.id = :userId AND a.id != :excludeId")
    void clearDefaultFlagForUser(@Param("userId") UUID userId, @Param("excludeId") UUID excludeId);

    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.isDeleted = false AND a.user.id = :userId")
    void clearAllDefaultFlagsForUser(@Param("userId") UUID userId);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Address a WHERE a.isDeleted = false AND a.user.id = :userId AND a.isDefault = true")
    boolean existsByUserIdAndIsDefaultTrue(@Param("userId") UUID userId);

    @Query("SELECT a FROM Address a WHERE a.isDeleted = false AND a.id = :id")
    Optional<Address> findByIdAndNotDeleted(@Param("id") UUID id);

    @Query("SELECT a FROM Address a WHERE a.isDeleted = false AND a.id = :addressId AND a.user.id = :userId")
    Optional<Address> findByIdAndUserIdAndNotDeleted(@Param("addressId") UUID addressId, @Param("userId") UUID userId);

    @Query("SELECT a FROM Address a WHERE a.isDeleted = false ORDER BY a.createdAt DESC")
    List<Address> findAllAndNotDeleted();
}