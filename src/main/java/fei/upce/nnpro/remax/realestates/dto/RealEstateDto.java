package fei.upce.nnpro.remax.realestates.dto;

import fei.upce.nnpro.remax.model.realestates.entity.*;
import fei.upce.nnpro.remax.model.realestates.enums.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;

@Data
public class RealEstateDto {
    private Long id;
    @NotNull(message = "Real Estate Type is required")
    private RealEstateType realEstateType;

    // Common Fields
    @NotBlank(message = "Name cannot be empty")
    @Size(max = 255, message = "Name is too long")
    private String name;

    @NotBlank(message = "Description cannot be empty")
    @Size(max = 4000, message = "Description cannot exceed 4000 characters")
    private String description;

    private Status status;
    @Positive(message = "Usable area must be positive")
    private double usableArea;

    @NotNull(message = "Contract type is required")
    private ContractType contractType;

    @NotNull(message = "Price disclosure preference is required")
    private PriceDisclosure priceDisclosure;

    @NotNull(message = "Commission type is required")
    private Commission commission;

    @NotNull(message = "Tax arrangement is required")
    private Taxes taxes;

    private ZonedDateTime availableFrom;
    private boolean basement;

    @PositiveOrZero(message = "Price cannot be negative")
    private Double price;

    // Relationships / Embedded
    @NotNull(message = "Address is required")
    @Valid
    private Address address;

    @Valid
    private BuildingProperties buildingProperties;

    @NotNull(message = "Equipment status is required")
    private Equipment equipment;

    @Valid
    private Utilities utilities;
    private TransportPossibilities transportPossibilities;
    private CivicAmenities civicAmenities;

    // Apartment specific
    private ApartmentOwnershipType ownershipType;
    private Integer floor;
    private Integer totalFloors;
    private Boolean elevator;
    private Boolean balcony;
    private Integer rooms;

    // House specific
    private Double plotArea;
    private HouseType houseType;
    private Integer stories;

    // Land specific
    private Boolean isForHousing;

    // Images (represented effectively by IDs)
    private List<Long> imageIds;
}
