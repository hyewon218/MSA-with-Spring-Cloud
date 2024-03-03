package org.example.catalogsservice.repository;

import org.example.catalogsservice.entity.Catalogs;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CatalogsRepository extends JpaRepository<Catalogs, Long> {

    Catalogs findByProductId(String productId);
}
