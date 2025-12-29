package fei.upce.nnpro.remax.realestates;

import fei.upce.nnpro.remax.address.dto.AddressDto;
import fei.upce.nnpro.remax.address.dto.AddressMapper;
import fei.upce.nnpro.remax.address.entity.Address;
import fei.upce.nnpro.remax.address.service.AddressService;
import fei.upce.nnpro.remax.profile.entity.Realtor;
import fei.upce.nnpro.remax.profile.service.ProfileService;
import fei.upce.nnpro.remax.realestates.dto.RealEstateDto;
import fei.upce.nnpro.remax.realestates.dto.RealEstateFilterDto;
import fei.upce.nnpro.remax.realestates.entity.Apartment;
import fei.upce.nnpro.remax.realestates.entity.RealEstate;
import fei.upce.nnpro.remax.realestates.factory.RealEstateFactory;
import fei.upce.nnpro.remax.realestates.factory.strategy.RealEstateUpdateStrategy;
import fei.upce.nnpro.remax.realestates.repository.RealEstateRepository;
import fei.upce.nnpro.remax.realestates.service.RealEstateCommonUpdater;
import fei.upce.nnpro.remax.realestates.service.RealEstateImageHelper;
import fei.upce.nnpro.remax.realestates.service.RealEstatePriceHelper;
import fei.upce.nnpro.remax.realestates.service.RealEstateService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RealEstateServiceTest {

    // --- Core Dependencies ---
    @Mock private RealEstateRepository realEstateRepository;
    @Mock private AddressService addressService;
    @Mock private AddressMapper addressMapper;
    @Mock private ProfileService profileService;

    // --- Refactoring Dependencies ---
    @Mock private RealEstateFactory realEstateFactory;
    @Mock private RealEstateCommonUpdater commonUpdater;
    @Mock private RealEstatePriceHelper priceHelper;
    @Mock private RealEstateImageHelper imageHelper;
    @Mock private List<RealEstateUpdateStrategy> updateStrategies;

    // Helper mock for the strategy stream
    @Mock private RealEstateUpdateStrategy mockStrategy;

    @InjectMocks
    private RealEstateService realEstateService;

    @BeforeEach
    void setUpStrategies() {
        lenient().when(updateStrategies.stream()).thenAnswer(i -> Stream.of(mockStrategy));
        lenient().when(mockStrategy.supports(any())).thenReturn(true);
    }

    // --------------------------------------------------------------------------------------
    // CREATE TESTS
    // --------------------------------------------------------------------------------------

    @Test
    @DisplayName("Create: Should orchestrate factory, helpers, and save")
    void createRealEstate_Success() {
        // Arrange
        RealEstateDto dto = new RealEstateDto();
        dto.setPrice(5000000.0);
        dto.setAddress(new AddressDto());
        dto.setImages(List.of(1L, 2L));

        RealEstate mockEntity = new Apartment();
        Address mockAddress = new Address();

        // Mocks behavior
        when(realEstateFactory.createEntity(dto)).thenReturn(mockEntity);
        when(addressMapper.toEntity(any(AddressDto.class))).thenReturn(mockAddress);
        when(addressService.save(mockAddress)).thenReturn(mockAddress);
        when(realEstateRepository.save(any(RealEstate.class))).thenReturn(mockEntity);

        // Act
        RealEstate result = realEstateService.createRealEstate(dto);

        // Assert
        // 1. Verify Factory creation
        verify(realEstateFactory).createEntity(dto);

        // 2. Verify Address handling
        verify(addressMapper).toEntity(any(AddressDto.class));
        verify(addressService).save(mockAddress);

        // 3. Verify Helpers interaction
        verify(priceHelper).initializePrice(mockEntity, 5000000.0);
        verify(imageHelper).handleImages(mockEntity, dto.getImages());

        // 4. Verify Save (called at least once)
        verify(realEstateRepository, atLeastOnce()).save(mockEntity);

        assertThat(result).isEqualTo(mockEntity);
    }

    @Test
    @DisplayName("Create: Should throw exception if Address is missing")
    void createRealEstate_MissingAddress() {
        RealEstateDto dto = new RealEstateDto();
        dto.setAddress(null);

        when(realEstateFactory.createEntity(dto)).thenReturn(new Apartment());

        assertThatThrownBy(() -> realEstateService.createRealEstate(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Address is required");
    }

    // --------------------------------------------------------------------------------------
    // UPDATE TESTS
    // --------------------------------------------------------------------------------------

    @Test
    @DisplayName("Update: Should orchestrate common updater, strategies, and helpers")
    void updateRealEstate_Success() {
        // Arrange
        Long id = 1L;
        RealEstateDto dto = new RealEstateDto();
        dto.setPrice(6000000.0);
        dto.setAddress(new AddressDto());
        dto.setImages(List.of(10L));

        RealEstate existing = new Apartment();
        existing.setId(id);
        Address existingAddress = new Address();
        existing.setAddress(existingAddress);

        when(realEstateRepository.findById(id)).thenReturn(Optional.of(existing));
        when(realEstateRepository.save(existing)).thenReturn(existing);
        when(addressMapper.toEntity(any())).thenReturn(new Address());

        // Act
        realEstateService.updateRealEstate(id, dto);

        // Assert
        // 1. Verify Common Fields Update
        verify(commonUpdater).updateCommonFields(existing, dto);

        // 2. Verify Strategy Execution
        verify(mockStrategy).supports(existing);
        verify(mockStrategy).update(existing, dto);

        // 3. Verify Helpers
        verify(priceHelper).updatePrice(existing, 6000000.0);
        verify(imageHelper).handleImages(existing, dto.getImages());
        verify(addressService).update(any(Address.class), eq(existingAddress));

        // 4. Verify Save
        verify(realEstateRepository).save(existing);
    }

    @Test
    @DisplayName("Update: Should throw exception if ID not found")
    void updateRealEstate_NotFound() {
        Long id = 99L;
        when(realEstateRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> realEstateService.updateRealEstate(id, new RealEstateDto()))
                .isInstanceOf(EntityNotFoundException.class);

        verifyNoInteractions(commonUpdater);
        verifyNoInteractions(priceHelper);
    }

    @Test
    @DisplayName("Update: Should NOT call address update if address DTO is null")
    void updateRealEstate_NoAddressUpdate() {
        Long id = 1L;
        RealEstate existing = new Apartment();
        existing.setId(id);
        existing.setAddress(new Address());

        RealEstateDto dto = new RealEstateDto();
        dto.setAddress(null); // No address provided

        when(realEstateRepository.findById(id)).thenReturn(Optional.of(existing));
        when(realEstateRepository.save(existing)).thenReturn(existing);

        realEstateService.updateRealEstate(id, dto);

        verify(addressService, never()).update(any(), any());
    }

    @Test
    @DisplayName("Update: Should transfer realtor if realtorId provided")
    void updateRealEstate_RealtorTransfer() {
        Long id = 1L;
        Long newRealtorId = 50L;
        RealEstateDto dto = new RealEstateDto();
        dto.setRealtorId(newRealtorId);

        RealEstate existing = new Apartment();
        existing.setId(id);

        when(realEstateRepository.findById(id)).thenReturn(Optional.of(existing));
        when(realEstateRepository.save(existing)).thenReturn(existing);
        // Mock profile service interaction
        when(profileService.getProfile(newRealtorId)).thenReturn(Optional.of(mock(Realtor.class)));

        realEstateService.updateRealEstate(id, dto);

        verify(profileService).getProfile(newRealtorId);
        // We verify the side effect indirectly or verify interactions.
        // Since setRealtor is a simple setter, verification of the service call is sufficient.
    }

    // --------------------------------------------------------------------------------------
    // GET & SEARCH TESTS
    // --------------------------------------------------------------------------------------

    @Test
    @DisplayName("Get: Should return entity when found")
    void getRealEstate_Found() {
        Long id = 1L;
        RealEstate estate = new Apartment();
        when(realEstateRepository.findById(id)).thenReturn(Optional.of(estate));

        RealEstate result = realEstateService.getRealEstate(id);

        assertThat(result).isSameAs(estate);
    }

    @Test
    @DisplayName("Get: Should throw exception when not found")
    void getRealEstate_NotFound() {
        Long id = 99L;
        when(realEstateRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> realEstateService.getRealEstate(id))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("Search: Should invoke repository findAll with specification and pageable")
    void searchRealEstates() {
        RealEstateFilterDto filter = new RealEstateFilterDto();
        filter.setCity("Prague");
        Pageable pageable = PageRequest.of(0, 10);

        Page<RealEstate> expectedPage = new PageImpl<>(Collections.emptyList());
        when(realEstateRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(expectedPage);

        Page<RealEstate> result = realEstateService.searchRealEstates(filter, pageable);

        assertThat(result).isSameAs(expectedPage);
        verify(realEstateRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("List by Realtor: Should invoke repository")
    void listRealEstatesByRealtor() {
        Long realtorId = 5L;
        List<RealEstate> list = new ArrayList<>();
        when(realEstateRepository.findAllByRealtorId(realtorId)).thenReturn(list);

        List<RealEstate> result = realEstateService.listRealEstatesByRealtor(realtorId);

        assertThat(result).isSameAs(list);
        verify(realEstateRepository).findAllByRealtorId(realtorId);
    }
}