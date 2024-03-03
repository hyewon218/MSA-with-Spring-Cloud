package org.example.catalogsservice.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.catalogsservice.dto.CatalogsResponseDto;
import org.example.catalogsservice.service.CatalogsService;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/catalogs-service")
@RestController
public class CatalogsController {
    private final CatalogsService catalogsService;
    private final Environment environment;
    @GetMapping("/health-check")
    public String status() {
        return String.format("It's Working in User Service on PORT %s",
            environment.getProperty("local.server.port"));
    }

    @GetMapping("/catalogs")
    public ResponseEntity<List<CatalogsResponseDto>> getAllCatalogs() {
        return ResponseEntity.status(HttpStatus.OK).body(catalogsService.getAllCatalogs());
    }
}