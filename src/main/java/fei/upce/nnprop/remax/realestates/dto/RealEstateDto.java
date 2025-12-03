package fei.upce.nnprop.remax.realestates.dto;

import fei.upce.nnprop.remax.model.realestates.entity.*;
import fei.upce.nnprop.remax.model.realestates.enums.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@Schema(description = "Unified DTO for all types of Real Estates (Apartment, House, Land)")
public class RealEstateDto {

    @Schema(description = "Real Estate ID", example = "100")
    private Long id;

    @NotNull(message = "Real Estate Type is required")
    @Schema(description = "Discriminator for property type", example = "APARTMENT", requiredMode = Schema.RequiredMode.REQUIRED)
    private RealEstateType realEstateType;

    // Common Fields
    @NotBlank(message = "Name cannot be empty")
    @Size(max = 255, message = "Name is too long")
    @Schema(description = "Marketing title", example = "Sunny Apartment in Prague Center")
    private String name;

    @NotBlank(message = "Description cannot be empty")
    @Size(max = 4000, message = "Description cannot exceed 4000 characters")
    @Schema(description = "Detailed description", example = "Beautiful 3-bedroom apartment...")
    private String description;

    @Schema(description = "Current listing status", example = "AVAILABLE")
    private Status status;

    @Positive(message = "Usable area must be positive")
    @Schema(description = "Usable area in square meters", example = "75.5")
    private double usableArea;

    @NotNull(message = "Contract type is required")
    @Schema(description = "Sale or Rental", example = "SALE")
    private ContractType contractType;

    @NotNull(message = "Price disclosure preference is required")
    @Schema(description = "How the price is disclosed", example = "ASK")
    private PriceDisclosure priceDisclosure;

    @NotNull(message = "Commission type is required")
    @Schema(description = "Commission arrangement", example = "INCLUDED")
    private Commission commission;

    @NotNull(message = "Tax arrangement is required")
    @Schema(description = "Tax arrangement", example = "INCLUDED")
    private Taxes taxes;

    @Schema(description = "When the property is available from", example = "2025-01-01T00:00:00Z")
    private ZonedDateTime availableFrom;

    @Schema(description = "Does it have a basement?", example = "true")
    private boolean basement;

    @PositiveOrZero(message = "Price cannot be negative")
    @Schema(description = "Price amount (if disclosed)", example = "5000000.0")
    private Double price;

    // Relationships / Embedded
    @NotNull(message = "Address is required")
    @Valid
    @Schema(description = "Physical address of the property")
    private Address address;

    @Valid
    @Schema(description = "Building characteristics (material, condition, energy class)")
    private BuildingProperties buildingProperties;

    @NotNull(message = "Equipment status is required")
    @Schema(description = "Furnishing status", example = "PARTIALLY_FURNISHED")
    private Equipment equipment;

    @Valid
    @Schema(description = "Available utilities and connections")
    private Utilities utilities;

    @Schema(description = "Transport options nearby")
    private TransportPossibilities transportPossibilities;

    @Schema(description = "Civic amenities nearby")
    private CivicAmenities civicAmenities;

    // --- Specific Fields ---

    // Apartment specific
    @Schema(description = "[APARTMENT ONLY] Ownership type", example = "OWNERSHIP")
    private ApartmentOwnershipType ownershipType;

    @Schema(description = "[APARTMENT ONLY] Floor number", example = "3")
    private Integer floor;

    @Schema(description = "[APARTMENT ONLY] Total floors in building", example = "5")
    private Integer totalFloors;

    @Schema(description = "[APARTMENT ONLY] Has elevator", example = "true")
    private Boolean elevator;

    @Schema(description = "[APARTMENT ONLY] Has balcony", example = "true")
    private Boolean balcony;

    @Schema(description = "[APARTMENT ONLY] Number of rooms", example = "3")
    private Integer rooms;

    // House specific
    @Schema(description = "[HOUSE ONLY] Plot area in m2", example = "400.0")
    private Double plotArea;

    @Schema(description = "[HOUSE ONLY] Type of house", example = "DETACHED")
    private HouseType houseType;

    @Schema(description = "[HOUSE ONLY] Number of stories", example = "2")
    private Integer stories;

    // Land specific
    @Schema(description = "[LAND ONLY] Is designated for housing", example = "true")
    private Boolean isForHousing;

    @Schema(description = "List of Image IDs associated with this property", example = "[10, 11, 12]")
    private List<Long> imageIds;
}
