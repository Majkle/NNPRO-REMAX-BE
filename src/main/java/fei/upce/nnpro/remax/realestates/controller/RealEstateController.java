package fei.upce.nnpro.remax.realestates.controller;

import fei.upce.nnpro.remax.realestates.dto.RealEstateDto;
import fei.upce.nnpro.remax.realestates.dto.RealEstateFilterDto;
import fei.upce.nnpro.remax.realestates.dto.RealEstateMapper;
import fei.upce.nnpro.remax.realestates.entity.RealEstate;
import fei.upce.nnpro.remax.realestates.service.RealEstateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/real-estates")
@RequiredArgsConstructor
@Tag(name = "Real Estates", description = "Management and searching of real estate properties")
@SecurityRequirement(name = "bearerAuth")
public class RealEstateController {

    private final RealEstateService realEstateService;
    private final RealEstateMapper realEstateMapper;

    @Operation(summary = "Create a new property",
            description = "Adds a new real estate listing. Requires ROLE_REALTOR.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Property created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ROLE_REALTOR", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @PostMapping
    @PreAuthorize("hasRole('ROLE_REALTOR')")
    public ResponseEntity<RealEstateDto> createRealEstate(@Valid @RequestBody RealEstateDto dto) {
        RealEstate createdEntity = realEstateService.createRealEstate(dto);
        RealEstateDto responseDto = realEstateMapper.toDto(createdEntity);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }


    @Operation(summary = "Get property details",
            description = "Public endpoint to retrieve details of a specific real estate listing.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Property found"),
            @ApiResponse(responseCode = "404", description = "Property not found", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<RealEstateDto> getRealEstate(@PathVariable Long id) {
        RealEstate entity = realEstateService.getRealEstate(id);
        return ResponseEntity.ok(realEstateMapper.toDto(entity));
    }


    @Operation(summary = "Update property",
            description = "Updates an existing real estate listing. Requires ROLE_REALTOR.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Property updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
            @ApiResponse(responseCode = "404", description = "Property not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ROLE_REALTOR", content = @Content)
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_REALTOR')")
    public ResponseEntity<RealEstateDto> updateRealEstate(
            @Parameter(description = "ID of the property to update") @PathVariable Long id,
            @Valid  @RequestBody RealEstateDto dto) {
        RealEstate updatedEntity = realEstateService.updateRealEstate(id, dto);
        return ResponseEntity.ok(realEstateMapper.toDto(updatedEntity));
    }

    @Operation(summary = "Search and filter properties",
            description = "Public endpoint to list properties with dynamic filtering (price, region, parameters) and pagination.")
    @ApiResponse(responseCode = "200", description = "Paginated list of properties retrieved")
    @GetMapping
    public ResponseEntity<Page<RealEstateDto>> searchRealEstates(
            @ModelAttribute RealEstateFilterDto filterDto,
            @ParameterObject Pageable pageable) {

        Page<RealEstate> entities = realEstateService.searchRealEstates(filterDto, pageable);

        Page<RealEstateDto> dtos = entities.map(realEstateMapper::toDto);

        return ResponseEntity.ok(dtos);
    }
}