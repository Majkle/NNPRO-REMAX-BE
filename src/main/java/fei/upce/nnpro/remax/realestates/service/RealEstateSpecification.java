package fei.upce.nnpro.remax.realestates.service;

import fei.upce.nnpro.remax.address.entity.Address;
import fei.upce.nnpro.remax.realestates.dto.RealEstateFilterDto;
import fei.upce.nnpro.remax.realestates.entity.*;
import fei.upce.nnpro.remax.realestates.entity.enums.*;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class RealEstateSpecification {

    // 1. Main Entry Point (The Orchestrator)
    public static Specification<RealEstate> filterBy(RealEstateFilterDto criteria) {
        return Specification
                .where(withRealEstateType(criteria.getRealEstateType()))
                .and(withLocation(criteria.getRegion(), criteria.getCity()))
                .and(withPriceRange(criteria.getMinPrice(), criteria.getMaxPrice()))
                .and(withAreaRange(criteria.getMinArea(), criteria.getMaxArea()))
                .and(withStatus(criteria.getStatus()))
                .and(withContractType(criteria.getContractType()))
                .and(withCivicAmenities(criteria.getCivicAmenities()))
                .and(withTransport(criteria.getTransportPossibilities()))
                .and(withUtilities(criteria.getUtilityTypes()))
                .and(withInternet(criteria.getInternetConnection()))
                .and(applyDistinct());
    }

    // ---------------------------------------------------------
    // 2. Polymorphic Type Filter
    // ---------------------------------------------------------
    private static Specification<RealEstate> withRealEstateType(RealEstateType type) {
        return (root, query, cb) -> {
            if (type == null) return null;

            Class<? extends RealEstate> targetClass = switch (type) {
                case APARTMENT -> Apartment.class;
                case HOUSE -> House.class;
                case LAND -> Land.class;
            };
            return cb.equal(root.type(), targetClass);
        };
    }

    // ---------------------------------------------------------
    // 3. Location Filters (Grouped to share the Join)
    // ---------------------------------------------------------
    private static Specification<RealEstate> withLocation(AddressRegion region, String city) {
        return (root, query, cb) -> {
            if (region == null && (city == null || city.isBlank())) {
                return null;
            }

            Join<RealEstate, Address> addressJoin = root.join("address", JoinType.LEFT);
            List<Predicate> predicates = new ArrayList<>();

            if (region != null) {
                predicates.add(cb.equal(addressJoin.get("region"), region));
            }

            if (city != null && !city.isBlank()) {
                predicates.add(cb.like(
                        cb.lower(addressJoin.get("city")),
                        "%" + city.toLowerCase() + "%"
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    // ---------------------------------------------------------
    // 4. Price Filter (Complex Subquery Logic)
    // ---------------------------------------------------------
    private static Specification<RealEstate> withPriceRange(Double minPrice, Double maxPrice) {
        return (root, query, cb) -> {
            if (minPrice == null && maxPrice == null) return null;

            // Create Subquery
            Subquery<Double> priceSubquery = query.subquery(Double.class);
            Root<PriceHistory> priceRoot = priceSubquery.from(PriceHistory.class);

            priceSubquery.select(priceRoot.get("price"))
                    .where(cb.equal(priceRoot.get("realEstate"), root));

            List<Predicate> predicates = new ArrayList<>();
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(priceSubquery, minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(priceSubquery, maxPrice));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    // ---------------------------------------------------------
    // 5. Simple Field Filters
    // ---------------------------------------------------------
    private static Specification<RealEstate> withAreaRange(Double minArea, Double maxArea) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (minArea != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("usableArea"), minArea));
            }
            if (maxArea != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("usableArea"), maxArea));
            }
            return predicates.isEmpty() ? null : cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static Specification<RealEstate> withStatus(Status status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    private static Specification<RealEstate> withContractType(ContractType contractType) {
        return (root, query, cb) ->
                contractType == null ? null : cb.equal(root.get("contractType"), contractType);
    }

    // ---------------------------------------------------------
    // 6. Embedded Collection Filters
    // ---------------------------------------------------------

    // Helper method to reduce code duplication for "contains all" logic
    private static <T> Specification<RealEstate> withCollectionAttribute(
            String embeddableField, String collectionField, Collection<T> criteriaSet) {
        return (root, query, cb) -> {
            if (criteriaSet == null || criteriaSet.isEmpty()) return null;

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.isNotNull(root.get(embeddableField)));

            Expression<Set<T>> collectionPath = root.get(embeddableField).get(collectionField);

            for (T item : criteriaSet) {
                predicates.add(cb.isMember(item, collectionPath));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static Specification<RealEstate> withCivicAmenities(Set<CivicAmenity> amenities) {
        return withCollectionAttribute("civicAmenities", "amenities", amenities);
    }

    private static Specification<RealEstate> withTransport(Set<TransportPossibility> transport) {
        return withCollectionAttribute("transportPossibilities", "possibilities", transport);
    }

    private static Specification<RealEstate> withUtilities(Set<UtilityType> utilities) {
        return withCollectionAttribute("utilities", "availableUtilities", utilities);
    }

    private static Specification<RealEstate> withInternet(InternetConnectionType connection) {
        return (root, query, cb) -> {
            if (connection == null) return null;
            return cb.and(
                    cb.isNotNull(root.get("utilities")),
                    cb.equal(root.get("utilities").get("internetConnection"), connection)
            );
        };
    }

    // ---------------------------------------------------------
    // 7. Utility / Config
    // ---------------------------------------------------------
    private static Specification<RealEstate> applyDistinct() {
        return (root, query, cb) -> {
            if (query != null && RealEstate.class.equals(query.getResultType())) {
                query.distinct(true);
            }
            return null; // Return null creates no predicate, just modifies query side-effect
        };
    }
}
