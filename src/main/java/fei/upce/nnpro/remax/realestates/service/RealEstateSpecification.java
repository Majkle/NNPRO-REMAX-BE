package fei.upce.nnpro.remax.realestates.service;

import fei.upce.nnpro.remax.address.entity.Address;
import fei.upce.nnpro.remax.realestates.dto.RealEstateFilterDto;
import fei.upce.nnpro.remax.realestates.entity.*;
import fei.upce.nnpro.remax.realestates.entity.enums.CivicAmenity;
import fei.upce.nnpro.remax.realestates.entity.enums.TransportPossibility;
import fei.upce.nnpro.remax.realestates.entity.enums.UtilityType;
import jakarta.persistence.criteria.*;
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

            // Price Filter - FIXED: Uses subquery to get latest price only
            if (criteria.getMinPrice() != null || criteria.getMaxPrice() != null) {
                // Option 1: Subquery approach (if currentPrice field doesn't exist)
                Subquery<Double> priceSubquery = query.subquery(Double.class);
                Root<PriceHistory> priceRoot = priceSubquery.from(PriceHistory.class);
                priceSubquery.select(priceRoot.get("price"))
                        .where(cb.equal(priceRoot.get("realEstate"), root));

                if (criteria.getMinPrice() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(priceSubquery, criteria.getMinPrice()));
                }
                if (criteria.getMaxPrice() != null) {
                    predicates.add(cb.lessThanOrEqualTo(priceSubquery, criteria.getMaxPrice()));
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
            // 5. Embedded Collections Filters (with null safety)
            // ---------------------------------------------------------

            // Civic Amenities: Property must have ALL selected amenities
            if (criteria.getCivicAmenities() != null && !criteria.getCivicAmenities().isEmpty()) {
                // Add null check for the embedded object
                predicates.add(cb.isNotNull(root.get("civicAmenities")));

                Expression<Set<CivicAmenity>> amenitiesPath = root.get("civicAmenities").get("amenities");
                for (CivicAmenity amenity : criteria.getCivicAmenities()) {
                    predicates.add(cb.isMember(amenity, amenitiesPath));
                }
            }

            // Transport Possibilities: Property must have ALL selected options
            if (criteria.getTransportPossibilities() != null && !criteria.getTransportPossibilities().isEmpty()) {
                predicates.add(cb.isNotNull(root.get("transportPossibilities")));

                Expression<Set<TransportPossibility>> transportPath = root.get("transportPossibilities").get("possibilities");
                for (TransportPossibility transport : criteria.getTransportPossibilities()) {
                    predicates.add(cb.isMember(transport, transportPath));
                }
            }

            // Utilities: Property must have ALL selected utilities
            if (criteria.getUtilityTypes() != null && !criteria.getUtilityTypes().isEmpty()) {
                predicates.add(cb.isNotNull(root.get("utilities")));

                Expression<Set<UtilityType>> utilPath = root.get("utilities").get("availableUtilities");
                for (UtilityType utility : criteria.getUtilityTypes()) {
                    predicates.add(cb.isMember(utility, utilPath));
                }
            }

            // Internet Connection: Exact match (with null safety)
            if (criteria.getInternetConnection() != null) {
                predicates.add(cb.isNotNull(root.get("utilities")));
                predicates.add(cb.equal(root.get("utilities").get("internetConnection"), criteria.getInternetConnection()));
            }

            // ---------------------------------------------------------
            // Final Query Construction
            // ---------------------------------------------------------

            // Only apply distinct if necessary (no longer needed if subquery is used for price)
            if (query != null && query.getResultType() != null &&
                    query.getResultType().equals(RealEstate.class)) {
                query.distinct(true);
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
