package vn.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.entity.Provider;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProviderRepository extends JpaRepository<Provider, Long> {
    
    Optional<Provider> findByProviderName(String providerName);
    
    List<Provider> findByStatus(Boolean status);
    
    Page<Provider> findByProviderNameContainingIgnoreCase(String providerName, Pageable pageable);
    
    @Query("SELECT p FROM Provider p WHERE p.status = true ORDER BY p.providerName")
    List<Provider> findAllActiveProviders();
    
    boolean existsByProviderName(String providerName);
}
