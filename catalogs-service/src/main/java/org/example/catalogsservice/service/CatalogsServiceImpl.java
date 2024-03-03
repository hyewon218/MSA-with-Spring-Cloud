package org.example.catalogsservice.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.catalogsservice.dto.CatalogsResponseDto;
import org.example.catalogsservice.repository.CatalogsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CatalogsServiceImpl implements CatalogsService {

    private final CatalogsRepository catalogsRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CatalogsResponseDto> getAllCatalogs() {
        return this.catalogsRepository.findAll()
            .stream()
            .map(CatalogsResponseDto::of)
            .collect(Collectors.toList());
    }
}

