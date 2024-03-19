package org.example.catalogsservice.repository;

import java.util.Optional;
import org.example.catalogsservice.entity.Catalogs;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CatalogsRepository extends JpaRepository<Catalogs, Long> {

    Optional<Catalogs> findByProductId(String productId);
}
