package fei.upce.nnpro.remax.realestates.dto;

import fei.upce.nnpro.remax.realestates.entity.enums.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Set;

@Data
@Schema(description = "Criteria for filtering real estate properties. " +
        "All fields are optional. " +
        "Filters are combined using AND logic.")
public class RealEstateFilterDto {

    @Schema(description = "Filter by specific type of property", example = "APARTMENT")
    private RealEstateType realEstateType;

    @Schema(description = "Filter by region", example = "PRAHA")
    private AddressRegion region;

    @Schema(description = "Filter by city name (case-insensitive partial match)", example = "Brno")
    private String city;

    @Schema(description = "Minimum price (in currency units)", example = "2000000")
    private Double minPrice;

    @Schema(description = "Maximum price (in currency units)", example = "15000000")
    private Double maxPrice;

    @Schema(description = "Minimum usable area in square meters", example = "45.0")
    private Double minArea;

    @Schema(description = "Maximum usable area in square meters", example = "150.0")
    private Double maxArea;

    @Schema(description = "Filter by contract type (SALE or RENTAL)", example = "SALE")
    private ContractType contractType;

    @Schema(description = "Filter by status (e.g., AVAILABLE, RESERVED)", example = "AVAILABLE")
    private Status status;

    @Schema(description = "List of required civic amenities. The property must contain ALL selected items.",
            example = "[\"SCHOOL\", \"SUPERMARKET\"]")
    private Set<CivicAmenity> civicAmenities;

    @Schema(description = "List of required transport options. The property must contain ALL selected items.",
            example = "[\"BUS\", \"TRAIN\"]")
    private Set<TransportPossibility> transportPossibilities;

    @Schema(description = "List of required utilities. The property must contain ALL selected items.",
            example = "[\"WATER\", \"ELECTRICITY\"]")
    private Set<UtilityType> utilityTypes;

    @Schema(description = "Filter by specific internet connection type", example = "FIBER_OPTIC")
    private InternetConnectionType internetConnection;
}
