package fei.upce.nnpro.remax.realestates.dto;

import fei.upce.nnpro.remax.realestates.entity.enums.*;
import lombok.Data;

import java.util.Set;

@Data
public class RealEstateFilterDto {
    private RealEstateType realEstateType;
    private AddressRegion region;
    private String city;

    private Double minPrice;
    private Double maxPrice;
    private Double minArea;
    private Double maxArea;

    private ContractType contractType;
    private Status status;

    private Set<CivicAmenity> civicAmenities;

    private Set<TransportPossibility> transportPossibilities;

    private Set<UtilityType> utilityTypes;

    private InternetConnectionType internetConnection;
}
