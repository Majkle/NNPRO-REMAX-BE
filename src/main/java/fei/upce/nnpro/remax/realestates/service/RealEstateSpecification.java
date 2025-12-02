package fei.upce.nnpro.remax.realestates.service;

import fei.upce.nnpro.remax.address.entity.Address;
import fei.upce.nnpro.remax.realestates.entity.*;
import fei.upce.nnpro.remax.realestates.entity.enums.CivicAmenity;
import fei.upce.nnpro.remax.realestates.entity.enums.TransportPossibility;
import fei.upce.nnpro.remax.realestates.entity.enums.UtilityType;
import fei.upce.nnpro.remax.realestates.dto.RealEstateFilterDto;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RealEstateSpecification {

    /**
     * Generates a JPA Specification based on the provided filter DTO.
     *
     * @param criteria The filtering criteria.
     * @return The JPA Specification for RealEstate entities.
     */
    public static Specification<RealEstate> filterBy(RealEstateFilterDto criteria) {
        return (root, query, cb) -> {
            final List<Predicate> predicates = new ArrayList<>();

            // ---------------------------------------------------------
            // 1. Real Estate Type (Polymorphism)
            // ---------------------------------------------------------
            if (criteria.getRealEstateType() != null) {
                Class<? extends RealEstate> targetClass = switch (criteria.getRealEstateType()) {
                    case APARTMENT -> Apartment.class;
                    case HOUSE -> House.class;
                    case LAND -> Land.class;
                };

                predicates.add(cb.equal(root.type(), targetClass));
            }

            // ---------------------------------------------------------
            // 2. Location Filters
            // ---------------------------------------------------------
            if (criteria.getRegion() != null || (criteria.getCity() != null && !criteria.getCity().isBlank())) {
                Join<RealEstate, Address> addressJoin = root.join("address", JoinType.LEFT);

                // Filter by Region (Exact match)
                if (criteria.getRegion() != null) {
                    predicates.add(cb.equal(addressJoin.get("region"), criteria.getRegion()));
                }

                // Filter by City (Case-insensitive partial match)
                if (criteria.getCity() != null && !criteria.getCity().isBlank()) {
                    predicates.add(cb.like(
                            cb.lower(addressJoin.get("city")),
                            "%" + criteria.getCity().toLowerCase() + "%"
                    ));
                }
            }

            // ---------------------------------------------------------
            // 3. Price & Area Filters
            // ---------------------------------------------------------

            // Price (searches against PriceHistory)
            if (criteria.getMinPrice() != null || criteria.getMaxPrice() != null) {
                Join<RealEstate, PriceHistory> priceJoin = root.join("priceHistory", JoinType.LEFT);

                if (criteria.getMinPrice() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(priceJoin.get("price"), criteria.getMinPrice()));
                }
                if (criteria.getMaxPrice() != null) {
                    predicates.add(cb.lessThanOrEqualTo(priceJoin.get("price"), criteria.getMaxPrice()));
                }
            }

            // Usable Area
            if (criteria.getMinArea() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("usableArea"), criteria.getMinArea()));
            }
            if (criteria.getMaxArea() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("usableArea"), criteria.getMaxArea()));
            }

            // ---------------------------------------------------------
            // 4. Basic Properties (Enums)
            // ---------------------------------------------------------
            if (criteria.getContractType() != null) {
                predicates.add(cb.equal(root.get("contractType"), criteria.getContractType()));
            }

            if (criteria.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), criteria.getStatus()));
            }

            // ---------------------------------------------------------
            // 5. Embedded Collections Filters
            // ---------------------------------------------------------

            // Civic Amenities: Property must have ALL selected amenities
            if (criteria.getCivicAmenities() != null && !criteria.getCivicAmenities().isEmpty()) {
                Expression<Set<CivicAmenity>> amenitiesPath = root.get("civicAmenities").get("amenities");
                for (CivicAmenity amenity : criteria.getCivicAmenities()) {
                    predicates.add(cb.isMember(amenity, amenitiesPath));
                }
            }

            // Transport Possibilities: Property must have ALL selected options
            if (criteria.getTransportPossibilities() != null && !criteria.getTransportPossibilities().isEmpty()) {
                Expression<Set<TransportPossibility>> transportPath = root.get("transportPossibilities").get("possibilities");
                for (TransportPossibility transport : criteria.getTransportPossibilities()) {
                    predicates.add(cb.isMember(transport, transportPath));
                }
            }

            // Utilities: Property must have ALL selected utilities
            if (criteria.getUtilityTypes() != null && !criteria.getUtilityTypes().isEmpty()) {
                Expression<Set<UtilityType>> utilPath = root.get("utilities").get("availableUtilities");
                for (UtilityType utility : criteria.getUtilityTypes()) {
                    predicates.add(cb.isMember(utility, utilPath));
                }
            }

            // Internet Connection: Exact match
            if (criteria.getInternetConnection() != null) {
                predicates.add(cb.equal(root.get("utilities").get("internetConnection"), criteria.getInternetConnection()));
            }

            // ---------------------------------------------------------
            // Final Query Construction
            // ---------------------------------------------------------

            // Apply Distinct to avoid duplicates from Joins (especially priceHistory)
            query.distinct(true);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
