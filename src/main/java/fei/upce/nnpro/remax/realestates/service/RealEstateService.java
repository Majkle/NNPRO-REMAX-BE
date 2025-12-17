package fei.upce.nnpro.remax.realestates.service;

import fei.upce.nnpro.remax.address.dto.AddressMapper;
import fei.upce.nnpro.remax.address.entity.Address;
import fei.upce.nnpro.remax.address.service.AddressService;
import fei.upce.nnpro.remax.images.entity.Image;
import fei.upce.nnpro.remax.images.repository.ImageRepository;
import fei.upce.nnpro.remax.images.service.ImageService;
import fei.upce.nnpro.remax.realestates.dto.RealEstateDto;
import fei.upce.nnpro.remax.realestates.dto.RealEstateFilterDto;
import fei.upce.nnpro.remax.realestates.dto.RealEstateMapper;
import fei.upce.nnpro.remax.realestates.entity.*;
import fei.upce.nnpro.remax.realestates.repository.RealEstateRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RealEstateService {

    private final RealEstateRepository realEstateRepository;
    private final AddressService addressService;
    private final RealEstateMapper realEstateMapper;
    private final ImageRepository imageRepository;
    private static final Logger log = LoggerFactory.getLogger(RealEstateService.class);
    private final ImageService imageService;
    private final AddressMapper addressMapper;

    /**
     * Creates a new Real Estate property.
     * Saves the address and initializes price history.
     */
    @Transactional
    public RealEstate createRealEstate(RealEstateDto dto) {
        log.info("Creating RealEstate name={} price={}", dto.getName(), dto.getPrice());
        // 1. Map DTO to new Entity
        RealEstate realEstate = realEstateMapper.toEntity(dto);

        // 2. Persist Address first (OneToOne)
        if (realEstate.getAddress() != null) {
            Address savedAddr = addressService.save(realEstate.getAddress());
            realEstate.setAddress(savedAddr); // Ensure the entity has the saved address
            log.debug("Saved address id={} for new realEstate", savedAddr.getId());
        }

        // 3. Save RealEstate
        RealEstate savedRealEstate = realEstateRepository.save(realEstate);

        // 4. Initialize Price History
        if (dto.getPrice() != null) {
            PriceHistory initialPrice = new PriceHistory();
            initialPrice.setPrice(dto.getPrice());
            initialPrice.setTimestamp(ZonedDateTime.now());
            initialPrice.setRealEstate(savedRealEstate);
            savedRealEstate.setPriceHistory(new ArrayList<>());
            savedRealEstate.getPriceHistory().add(initialPrice);

            savedRealEstate = realEstateRepository.save(savedRealEstate);
            log.debug("Initialized price history with price={}", dto.getPrice());
        }

        // 5. Image
        if (!dto.getImages().isEmpty()) {
            List<Image> images = new LinkedList<>();
            for (Long imageId : dto.getImages()) {
                Image image = imageService.getImageEntity(imageId);
                images.add(image);
                image.setRealEstate(savedRealEstate);
            }

            // set both sides and persist images to update the foreign key in the image table
            savedRealEstate.setImages(images);
            savedRealEstate = realEstateRepository.save(savedRealEstate);
        }

        log.info("Created RealEstate id={} name={}", savedRealEstate.getId(), savedRealEstate.getName());
        return savedRealEstate;
    }

    /**
     * Updates an existing property.
     * Handles partial updates by checking all fields and updating only those present in the DTO.
     */
    @Transactional
    public RealEstate updateRealEstate(Long id, RealEstateDto dto) {
        log.info("Updating RealEstate id={}", id);
        RealEstate existing = realEstateRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("RealEstate not found id={}", id);
                    return new EntityNotFoundException("RealEstate with ID " + id + " not found");
                });

        // 1. Handle Address Updates
        if (dto.getAddress() != null && existing.getAddress() != null) {
            // Convert AddressDto to temporary Address entity for the service update call
            Address newAddrData = addressMapper.toEntity(dto.getAddress());
            addressService.update(newAddrData, existing.getAddress());
            log.debug("Updated address for realEstate id={}", id);
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
                newPrice.setRealEstate(existing);

                if (existing.getPriceHistory() == null) {
                    existing.setPriceHistory(new ArrayList<>());
                }
                existing.getPriceHistory().add(newPrice);
                log.info("Price changed for realEstate id={} from={} to={}", id, currentPrice, dto.getPrice());
            }
        }

        // 3. Update Common Fields
        updateCommonFields(existing, dto);

        // 4. Update Subclass Specific Fields (Apartment, House, Land)
        updateSubclassFields(existing, dto);

        // 5. Save and Return Entity
        RealEstate saved = realEstateRepository.save(existing);
        log.info("Updated RealEstate id={} name={}", saved.getId(), saved.getName());
        log.debug("There are still {} images in the RealEstate", saved.getImages().size());
        return saved;
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
        // Never call existing.setImages(...)! That breaks because off orphanRemoval=true.
        // Instead, you must get the existing image list and update that.
        if (dto.getImages() != null) {
            List<Image> images = existing.getImages();

            log.debug("There are {} images in the DTO", dto.getImages().size());
            if (!dto.getImages().isEmpty()) {
                List<Image> newImages = imageRepository.findAllById(dto.getImages());
                log.debug("The image repository finds {} images", newImages.size());
                newImages.forEach((i) -> i.setRealEstate(existing));
                images.addAll(newImages);
            } else {
                images.clear();
            }
            log.debug("There are {} images in the RealEstate", images.size());
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
        log.info("Fetching RealEstate id={}", id);
        RealEstate found = realEstateRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("RealEstate not found id={}", id);
                    return new EntityNotFoundException("RealEstate with ID " + id + " not found");
                });
        log.debug("Fetched RealEstate id={} name={}", found.getId(), found.getName());
        return found;
    }

    /**
     * Searches for properties using dynamic filters.
     */
    @Transactional(readOnly = true)
    public Page<RealEstate> searchRealEstates(RealEstateFilterDto filter, Pageable pageable) {
        log.info("Searching RealEstates filter={} pageable={}", filter, pageable);
        Specification<RealEstate> spec = RealEstateSpecification.filterBy(filter);
        Page<RealEstate> page = realEstateRepository.findAll(spec, pageable);

        log.info("Found {} real estates", page.getNumberOfElements());
        return page;
    }

    @Transactional(readOnly = true)
    public List<RealEstate> listRealEstatesByRealtor(Long id) {
        return realEstateRepository.findAllByRealtorId(id);
    }
}