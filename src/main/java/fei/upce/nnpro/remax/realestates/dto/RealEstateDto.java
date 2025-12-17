package fei.upce.nnpro.remax.realestates.dto;

import fei.upce.nnpro.remax.address.dto.AddressDto;
import fei.upce.nnpro.remax.realestates.entity.BuildingProperties;
import fei.upce.nnpro.remax.realestates.entity.CivicAmenities;
import fei.upce.nnpro.remax.realestates.entity.TransportPossibilities;
import fei.upce.nnpro.remax.realestates.entity.Utilities;
import fei.upce.nnpro.remax.realestates.entity.enums.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "Data Transfer Object for creating, updating, and retrieving Real Estate properties.")
public class RealEstateDto {

    @Schema(description = "Unique identifier of the property", example = "101", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotNull(message = "Real Estate Type is required")
    @Schema(description = "Type of the property. Determines which specific fields (like floor vs. plot area) are relevant.", example = "APARTMENT")
    private RealEstateType type;

    // ==========================
    // Common Fields
    // ==========================

    @NotBlank(message = "Name cannot be empty")
    @Size(max = 255, message = "Name is too long")
    @Schema(description = "Marketing title of the listing", example = "Sunny 3-bedroom apartment in Prague center")
    private String name;

    @NotBlank(message = "Description cannot be empty")
    @Size(max = 4000, message = "Description cannot exceed 4000 characters")
    @Schema(description = "Detailed description of the property", example = "A beautiful renovated apartment with a large balcony...")
    private String description;

    private Long realtorId;

    @Schema(description = "Current availability status", example = "AVAILABLE")
    private Status status;

    @Positive(message = "Usable area must be positive")
    @Schema(description = "Internal usable area in square meters", example = "78.5")
    private double usableArea;

    @NotNull(message = "Contract type is required")
    @Schema(description = "Type of transaction (Sale or Rental)", example = "SALE")
    private ContractType contractType;

    @NotNull(message = "Price disclosure preference is required")
    @Schema(description = "How the price should be displayed to the public", example = "ASK")
    private PriceDisclosure priceDisclosure;

    @NotNull(message = "Commission type is required")
    @Schema(description = "Commission arrangement", example = "INCLUDED")
    private Commission commission;

    @NotNull(message = "Tax arrangement is required")
    @Schema(description = "Tax arrangement", example = "EXCLUDED")
    private Taxes taxes;

    @Schema(description = "Date when the property becomes available. ISO-8601 format.", example = "2025-01-01T00:00:00Z")
    private ZonedDateTime availableFrom;

    @Schema(description = "Indicates if the property has a basement", example = "true")
    private boolean basement;

    @PositiveOrZero(message = "Price cannot be negative")
    @Schema(description = "Listing price in local currency", example = "5500000.00")
    private Double price;

    // ==========================
    // Relationships / Embedded
    // ==========================

    @NotNull(message = "Address is required")
    @Valid
    @Schema(description = "Physical address of the property")
    private AddressDto address;

    @Valid
    @Schema(description = "Structural and energy properties of the building")
    private BuildingProperties buildingProperties;

    @NotNull(message = "Equipment status is required")
    @Schema(description = "Furnishing level", example = "PARTIALLY_FURNISHED")
    private Equipment equipment;

    @Valid
    @Schema(description = "Available utilities (Water, Gas, Internet, Parking)")
    private Utilities utilities;

    @Schema(description = "Nearby transport options")
    private TransportPossibilities transportPossibilities;

    @Schema(description = "Nearby civic amenities (Schools, Shops, etc.)")
    private CivicAmenities civicAmenities;

    // ==========================
    // Apartment Specific
    // ==========================

    @Schema(description = "[Apartment Only] Ownership type", example = "OWNERSHIP")
    private ApartmentOwnershipType ownershipType;

    @Schema(description = "[Apartment Only] Floor number", example = "3")
    private Integer floor;

    @Schema(description = "[Apartment Only] Total floors in the building", example = "6")
    private Integer totalFloors;

    @Schema(description = "[Apartment Only] Has elevator", example = "true")
    private Boolean elevator;

    @Schema(description = "[Apartment Only] Has balcony", example = "true")
    private Boolean balcony;

    @Schema(description = "[Apartment Only] Number of rooms", example = "3")
    private Integer rooms;

    // ==========================
    // House Specific
    // ==========================

    @Schema(description = "[House Only] Total plot area in square meters", example = "450.0")
    private Double plotArea;

    @Schema(description = "[House Only] Type of house structure", example = "DETACHED")
    private HouseType houseType;

    @Schema(description = "[House Only] Number of stories", example = "2")
    private Integer stories;

    // ==========================
    // Land Specific
    // ==========================

    @Schema(description = "[Land Only] Is this land intended for housing construction?", example = "true")
    private Boolean isForHousing;

    // ==========================
    // Images
    // ==========================

    @Schema(description = "List of Image IDs associated with this property", example = "[10, 25, 32]")
    private List<Long> images = new ArrayList<>();
}
