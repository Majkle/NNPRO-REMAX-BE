package fei.upce.nnpro.remax.realestates.service;

import fei.upce.nnpro.remax.address.dto.AddressDto;
import fei.upce.nnpro.remax.address.dto.AddressMapper;
import fei.upce.nnpro.remax.address.entity.Address;
import fei.upce.nnpro.remax.address.service.AddressService;
import fei.upce.nnpro.remax.profile.entity.RemaxUser;
import fei.upce.nnpro.remax.profile.service.ProfileService;
import fei.upce.nnpro.remax.realestates.dto.RealEstateDto;
import fei.upce.nnpro.remax.realestates.dto.RealEstateFilterDto;
import fei.upce.nnpro.remax.realestates.entity.*;
import fei.upce.nnpro.remax.realestates.factory.RealEstateFactory;
import fei.upce.nnpro.remax.realestates.factory.strategy.RealEstateUpdateStrategy;
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

import java.util.List;

@Service
@RequiredArgsConstructor
public class RealEstateService {

    private static final Logger log = LoggerFactory.getLogger(RealEstateService.class);

    // Core Dependencies
    private final RealEstateRepository realEstateRepository;
    private final ProfileService profileService;
    private final AddressService addressService;
    private final AddressMapper addressMapper;

    // Refactored Components
    private final RealEstateFactory realEstateFactory;
    private final RealEstateCommonUpdater commonUpdater;
    private final RealEstatePriceHelper priceHelper;
    private final RealEstateImageHelper imageHelper;
    private final List<RealEstateUpdateStrategy> updateStrategies;

    /**
     * Orchestrates the creation of a new Real Estate property.
     */
    @Transactional
    public RealEstate createRealEstate(RealEstateDto dto) {
        log.info("Creating new RealEstate: {}", dto.getName());

        // 1. Create Base Entity (Factory Pattern)
        RealEstate realEstate = realEstateFactory.createEntity(dto);

        // 2. Handle Mandatory Relationships
        assignAddress(realEstate, dto.getAddress());
        assignRealtor(realEstate, dto.getRealtorId());

        // 3. Persist to generate ID (needed for child entities)
        RealEstate saved = realEstateRepository.save(realEstate);

        // 4. Handle Complex Sub-Resources (Helpers)
        priceHelper.initializePrice(saved, dto.getPrice());
        imageHelper.handleImages(saved, dto.getImages());

        // 5. Final Save to cascade updates
        return realEstateRepository.save(saved);
    }

    /**
     * Orchestrates the update of an existing property.
     */
    @Transactional
    public RealEstate updateRealEstate(Long id, RealEstateDto dto) {
        log.info("Updating RealEstate id={}", id);
        RealEstate existing = getByIdOrThrow(id);

        // 1. Update Common Fields (Setter Helpers)
        commonUpdater.updateCommonFields(existing, dto);

        // 2. Update Subclass Fields (Strategy Pattern)
        applyUpdateStrategy(existing, dto);

        // 3. Update Relationships
        updateAddress(existing, dto.getAddress());
        priceHelper.updatePrice(existing, dto.getPrice());
        imageHelper.handleImages(existing, dto.getImages());

        if (dto.getRealtorId() != null) {
            assignRealtor(existing, dto.getRealtorId());
        }

        return realEstateRepository.save(existing);
    }

    @Transactional(readOnly = true)
    public RealEstate getRealEstate(Long id) {
        return getByIdOrThrow(id);
    }

    @Transactional(readOnly = true)
    public Page<RealEstate> searchRealEstates(RealEstateFilterDto filter, Pageable pageable) {
        log.debug("Searching RealEstates with filter: {}", filter);
        Specification<RealEstate> spec = RealEstateSpecification.filterBy(filter);
        return realEstateRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public List<RealEstate> listRealEstatesByRealtor(Long realtorId) {
        return realEstateRepository.findAllByRealtorId(realtorId);
    }

    // =========================================================================
    // PRIVATE HELPER METHODS (Orchestration Logic)
    // =========================================================================

    private RealEstate getByIdOrThrow(Long id) {
        return realEstateRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("RealEstate with ID " + id + " not found"));
    }

    private void applyUpdateStrategy(RealEstate entity, RealEstateDto dto) {
        updateStrategies.stream()
                .filter(strategy -> strategy.supports(entity))
                .findFirst()
                .ifPresent(strategy -> strategy.update(entity, dto));
    }

    private void assignAddress(RealEstate entity, AddressDto addressDto) {
        if (addressDto == null) {
            throw new IllegalArgumentException("Address is required for new Real Estate");
        }
        Address savedAddress = addressService.save(addressMapper.toEntity(addressDto));
        entity.setAddress(savedAddress);
    }

    private void updateAddress(RealEstate entity, AddressDto addressDto) {
        if (addressDto != null && entity.getAddress() != null) {
            addressService.update(addressMapper.toEntity(addressDto), entity.getAddress());
        }
    }

    private void assignRealtor(RealEstate entity, Long realtorId) {
        if (realtorId != null) {
            RemaxUser realtor = profileService.getProfile(realtorId)
                    .orElseThrow(() -> new EntityNotFoundException("Realtor not found with ID: " + realtorId));
            entity.setRealtor(realtor);
        }
    }
}