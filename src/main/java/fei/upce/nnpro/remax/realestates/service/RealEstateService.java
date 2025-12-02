package fei.upce.nnpro.remax.realestates.service;

import fei.upce.nnpro.remax.address.service.AddressService;
import fei.upce.nnpro.remax.images.repository.ImageRepository;
import fei.upce.nnpro.remax.images.entity.Image;
import fei.upce.nnpro.remax.realestates.dto.RealEstateMapper;
import fei.upce.nnpro.remax.realestates.dto.RealEstateDto;
import fei.upce.nnpro.remax.realestates.dto.RealEstateFilterDto;
import fei.upce.nnpro.remax.realestates.entity.*;
import fei.upce.nnpro.remax.realestates.repository.RealEstateRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RealEstateService {

    private final RealEstateRepository realEstateRepository;
    private final AddressService addressService;
    private final RealEstateMapper realEstateMapper;
    private final ImageRepository imageRepository;

    /**
     * Creates a new Real Estate property.
     * Saves the address and initializes price history.
     */
    @Transactional
    public RealEstate createRealEstate(RealEstateDto dto) {
        // 1. Map DTO to new Entity
        RealEstate realEstate = realEstateMapper.toEntity(dto);

        // 2. Persist Address first (OneToOne)
        if (realEstate.getAddress() != null) {
            addressService.save(realEstate.getAddress());
        }

        // 3. Initialize Price History
        if (dto.getPrice() != null) {
            PriceHistory initialPrice = new PriceHistory();
            initialPrice.setPrice(dto.getPrice());
            initialPrice.setTimestamp(ZonedDateTime.now());

            List<PriceHistory> history = new ArrayList<>();
            history.add(initialPrice);
            realEstate.setPriceHistory(history);
        }

        // 4. Save and Return Entity
        return realEstateRepository.save(realEstate);
    }

    /**
     * Updates an existing property.
     * Handles partial updates by checking all fields and updating only those present in the DTO.
     */
    @Transactional
    public RealEstate updateRealEstate(Long id, RealEstateDto dto) {
        RealEstate existing = realEstateRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("RealEstate with ID " + id + " not found"));

        // 1. Handle Address Updates
        if (dto.getAddress() != null && existing.getAddress() != null) {
            addressService.update(dto.getAddress(), existing.getAddress());
        }

        // 2. Handle Price History
        if (dto.getPrice() != null) {
            double currentPrice = 0.0;
            if (existing.getPriceHistory() != null && !existing.getPriceHistory().isEmpty()) {
                currentPrice = existing.getPriceHistory().getLast().getPrice();
            }

            // Only add history if price actually changed
            if (!dto.getPrice().equals(currentPrice)) {
                PriceHistory newPrice = new PriceHistory();
                newPrice.setPrice(dto.getPrice());
                newPrice.setTimestamp(ZonedDateTime.now());

                if (existing.getPriceHistory() == null) {
                    existing.setPriceHistory(new ArrayList<>());
                }
                existing.getPriceHistory().add(newPrice);
            }
        }

        // 3. Update Common Fields
        updateCommonFields(existing, dto);

        // 4. Update Subclass Specific Fields (Apartment, House, Land)
        updateSubclassFields(existing, dto);

        // 5. Save and Return Entity
        return realEstateRepository.save(existing);
    }

    private void updateCommonFields(RealEstate existing, RealEstateDto dto) {
        if (dto.getName() != null) {
            existing.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            existing.setDescription(dto.getDescription());
        }
        if (dto.getStatus() != null) {
            existing.setStatus(dto.getStatus());
        }

        if (dto.getUsableArea() > 0) {
            existing.setUsableArea(dto.getUsableArea());
        }
        if (dto.getContractType() != null) {
            existing.setContractType(dto.getContractType());
        }
        if (dto.getPriceDisclosure() != null) {
            existing.setPriceDisclosure(dto.getPriceDisclosure());
        }
        if (dto.getCommission() != null) {
            existing.setCommission(dto.getCommission());
        }
        if (dto.getTaxes() != null) {
            existing.setTaxes(dto.getTaxes());
        }
        if (dto.getAvailableFrom() != null) {
            existing.setAvailableFrom(dto.getAvailableFrom());
        }

        existing.setBasement(dto.isBasement());

        if (dto.getBuildingProperties() != null) {
            existing.setBuildingProperties(dto.getBuildingProperties());
        }
        if (dto.getEquipment() != null) {
            existing.setEquipment(dto.getEquipment());
        }
        if (dto.getUtilities() != null) {
            existing.setUtilities(dto.getUtilities());
        }
        if (dto.getTransportPossibilities() != null) {
            existing.setTransportPossibilities(dto.getTransportPossibilities());
        }
        if (dto.getCivicAmenities() != null) {
            existing.setCivicAmenities(dto.getCivicAmenities());
        }

        // Images
        if (dto.getImageIds() != null) {
            if (dto.getImageIds().isEmpty()) {
                existing.setImage(new ArrayList<>());
            } else {
                List<Image> images = imageRepository.findAllById(dto.getImageIds());
                existing.setImage(images);
            }
        }
    }

    private void updateSubclassFields(RealEstate existing, RealEstateDto dto) {
        if (existing instanceof Apartment apartment) {
            if (dto.getOwnershipType() != null) apartment.setOwnershipType(dto.getOwnershipType());
            if (dto.getFloor() != null) apartment.setFloor(dto.getFloor());
            if (dto.getTotalFloors() != null) apartment.setTotalFloors(dto.getTotalFloors());
            if (dto.getElevator() != null) apartment.setElevator(dto.getElevator());
            if (dto.getBalcony() != null) apartment.setBalcony(dto.getBalcony());
            if (dto.getRooms() != null) apartment.setRooms(dto.getRooms());

        } else if (existing instanceof House house) {
            if (dto.getPlotArea() != null) house.setPlotArea(dto.getPlotArea());
            if (dto.getHouseType() != null) house.setHouseType(dto.getHouseType());
            if (dto.getStories() != null) house.setStories(dto.getStories());

        } else if (existing instanceof Land land) {
            if (dto.getIsForHousing() != null) land.setForHousing(dto.getIsForHousing());
        }
    }

    /**
     * Retrieves a single Real Estate entity by ID.
     */
    @Transactional(readOnly = true)
    public RealEstate getRealEstate(Long id) {
        return realEstateRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("RealEstate with ID " + id + " not found"));
    }

    /**
     * Searches for properties using dynamic filters.
     */
    @Transactional(readOnly = true)
    public Page<RealEstate> searchRealEstates(RealEstateFilterDto filter, Pageable pageable) {
        Specification<RealEstate> spec = RealEstateSpecification.filterBy(filter);
        return realEstateRepository.findAll(spec, pageable);
    }
}