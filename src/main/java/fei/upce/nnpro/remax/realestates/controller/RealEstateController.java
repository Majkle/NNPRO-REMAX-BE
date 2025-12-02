package fei.upce.nnpro.remax.realestates.controller;

import fei.upce.nnpro.remax.model.realestates.entity.RealEstate;
import fei.upce.nnpro.remax.realestates.RealEstateMapper;
import fei.upce.nnpro.remax.realestates.dto.RealEstateDto;
import fei.upce.nnpro.remax.realestates.dto.RealEstateFilterDto;
import fei.upce.nnpro.remax.realestates.service.RealEstateService;
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
public class RealEstateController {

    private final RealEstateService realEstateService;
    private final RealEstateMapper realEstateMapper;

    @PostMapping
    public ResponseEntity<RealEstateDto> createRealEstate(@Valid @RequestBody RealEstateDto dto) {
        RealEstate createdEntity = realEstateService.createRealEstate(dto);
        RealEstateDto responseDto = realEstateMapper.toDto(createdEntity);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }


    @GetMapping("/{id}")
    public ResponseEntity<RealEstateDto> getRealEstate(@PathVariable Long id) {
        RealEstate entity = realEstateService.getRealEstate(id);
        return ResponseEntity.ok(realEstateMapper.toDto(entity));
    }


    @PutMapping("/{id}")
    public ResponseEntity<RealEstateDto> updateRealEstate(@PathVariable Long id, @Valid  @RequestBody RealEstateDto dto) {
        RealEstate updatedEntity = realEstateService.updateRealEstate(id, dto);
        return ResponseEntity.ok(realEstateMapper.toDto(updatedEntity));
    }

    @GetMapping
    public ResponseEntity<Page<RealEstateDto>> searchRealEstates(
            @ModelAttribute RealEstateFilterDto filterDto,
            @ParameterObject Pageable pageable) {

        Page<RealEstate> entities = realEstateService.searchRealEstates(filterDto, pageable);

        Page<RealEstateDto> dtos = entities.map(realEstateMapper::toDto);

        return ResponseEntity.ok(dtos);
    }
}