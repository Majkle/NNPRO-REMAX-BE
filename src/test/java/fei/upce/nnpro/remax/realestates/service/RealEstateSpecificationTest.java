package fei.upce.nnpro.remax.realestates.service;

import fei.upce.nnpro.remax.realestates.dto.RealEstateFilterDto;
import fei.upce.nnpro.remax.realestates.entity.*;
import fei.upce.nnpro.remax.realestates.entity.enums.*;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class RealEstateSpecificationTest {
    @Mock private Root<RealEstate> root;
    @Mock private CriteriaQuery<?> query;
    @Mock private CriteriaBuilder cb;
    @Mock private Path<Object> path;
    @Mock private Predicate predicate;
    private Specification<RealEstate> specification;

    @BeforeEach
    void setUp() {
        lenient().when(root.get(anyString())).thenReturn(path);
        lenient().when(cb.conjunction()).thenReturn(predicate);
        lenient().when(cb.and(any(Predicate[].class))).thenReturn(predicate);
    }

    @Test
    void filterBy_withApartmentType_createsApartmentTypePredicate() {
        RealEstateFilterDto criteria = new RealEstateFilterDto();
        criteria.setRealEstateType(RealEstateType.APARTMENT);
        when(cb.equal(root.type(), Apartment.class)).thenReturn(predicate);
        when(cb.and(any(Predicate[].class))).thenReturn(predicate);
        specification = RealEstateSpecification.filterBy(criteria);
        Predicate result = specification.toPredicate(root, query, cb);
        assertNotNull(result);
        verify(cb).equal(root.type(), Apartment.class);
    }

    @Test
    void filterBy_withNullCriteria_throwsWhenCallingGetMethods() {
        assertThrows(NullPointerException.class, () -> {
            RealEstateSpecification.filterBy(null).toPredicate(root, query, cb);
        });
    }

    // ...existing code for all other test cases as previously generated...
}

