package fei.upce.nnprop.remax.realestates.dto;

import fei.upce.nnprop.remax.model.realestates.enums.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Set;

@Data
@Schema(description = "Criteria for searching real estates")
public class RealEstateFilterDto {
    @Schema(description = "Filter by specific type", example = "APARTMENT")
    private RealEstateType realEstateType;

    @Schema(description = "Filter by region", example = "PRAHA")
    private AddressRegion region;

    @Schema(description = "Filter by city (partial match)", example = "Pardubice")
    private String city;

    @Schema(description = "Minimum price", example = "1000000")
    private Double minPrice;

    @Schema(description = "Maximum price", example = "5000000")
    private Double maxPrice;

    @Schema(description = "Minimum area in m2", example = "50")
    private Double minArea;

    @Schema(description = "Maximum area in m2", example = "150")
    private Double maxArea;

    @Schema(description = "Filter by contract type", example = "SALE")
    private ContractType contractType;

    @Schema(description = "Filter by status", example = "AVAILABLE")
    private Status status;

    @Schema(description = "Must contain ALL selected amenities")
    private Set<CivicAmenity> civicAmenities;

    @Schema(description = "Must contain ALL selected transport options")
    private Set<TransportPossibility> transportPossibilities;

    @Schema(description = "Must contain ALL selected utilities")
    private Set<UtilityType> utilityTypes;

    @Schema(description = "Filter by internet connection type")
    private InternetConnectionType internetConnection;
}
