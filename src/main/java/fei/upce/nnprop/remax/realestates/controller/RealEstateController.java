package fei.upce.nnprop.remax.realestates.controller;

import fei.upce.nnprop.remax.model.realestates.entity.RealEstate;
import fei.upce.nnprop.remax.realestates.RealEstateMapper;
import fei.upce.nnprop.remax.realestates.dto.RealEstateDto;
import fei.upce.nnprop.remax.realestates.dto.RealEstateFilterDto;
import fei.upce.nnprop.remax.realestates.service.RealEstateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/real-estates")
@RequiredArgsConstructor
@Tag(name = "Real Estates", description = "Operations related to properties (Apartments, Houses, Lands)")
public class RealEstateController {

    private final RealEstateService realEstateService;
    private final RealEstateMapper realEstateMapper;

    @Operation(summary = "Create a new real estate", description = "Requires specific fields based on RealEstateType")
    @PostMapping
    public ResponseEntity<RealEstateDto> createRealEstate(@Valid @RequestBody RealEstateDto dto) {
        RealEstate createdEntity = realEstateService.createRealEstate(dto);
        RealEstateDto responseDto = realEstateMapper.toDto(createdEntity);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @Operation(summary = "Get real estate by ID")
    @GetMapping("/{id}")
    public ResponseEntity<RealEstateDto> getRealEstate(@PathVariable Long id) {
        RealEstate entity = realEstateService.getRealEstate(id);
        return ResponseEntity.ok(realEstateMapper.toDto(entity));
    }

    @Operation(summary = "Update real estate", description = "Updates fields present in the DTO")
    @PutMapping("/{id}")
    public ResponseEntity<RealEstateDto> updateRealEstate(@PathVariable Long id, @Valid  @RequestBody RealEstateDto dto) {
        RealEstate updatedEntity = realEstateService.updateRealEstate(id, dto);
        return ResponseEntity.ok(realEstateMapper.toDto(updatedEntity));
    }

    @Operation(summary = "Search real estates", description = "Filter properties by type, location, price, etc.")
    @GetMapping
    public ResponseEntity<Page<RealEstateDto>> searchRealEstates(
            @ModelAttribute RealEstateFilterDto filterDto,
            @ParameterObject Pageable pageable) {

        Page<RealEstate> entities = realEstateService.searchRealEstates(filterDto, pageable);

        Page<RealEstateDto> dtos = entities.map(realEstateMapper::toDto);

        return ResponseEntity.ok(dtos);
    }
}