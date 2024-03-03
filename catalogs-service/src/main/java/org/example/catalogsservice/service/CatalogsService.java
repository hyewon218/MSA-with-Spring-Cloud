package org.example.catalogsservice.service;

import java.util.List;
import org.example.catalogsservice.dto.CatalogsResponseDto;

public interface CatalogsService {

    List<CatalogsResponseDto> getAllCatalogs();
}