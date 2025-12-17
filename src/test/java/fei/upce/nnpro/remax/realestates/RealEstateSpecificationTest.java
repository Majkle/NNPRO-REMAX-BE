package fei.upce.nnpro.remax.realestates;

import fei.upce.nnpro.remax.realestates.dto.RealEstateFilterDto;
import fei.upce.nnpro.remax.realestates.entity.RealEstate;
import fei.upce.nnpro.remax.realestates.entity.enums.AddressRegion;
import fei.upce.nnpro.remax.realestates.entity.enums.RealEstateType;
import fei.upce.nnpro.remax.realestates.entity.enums.Status;
import fei.upce.nnpro.remax.realestates.service.RealEstateSpecification;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
public class RealEstateSpecificationTest {

    @Test
    void filterBy_AllCriteriaProvided_BuildsPredicates() {
        // Arrange
        RealEstateFilterDto criteria = new RealEstateFilterDto();
        criteria.setRealEstateType(RealEstateType.APARTMENT);
        criteria.setRegion(AddressRegion.PRAHA);
        criteria.setCity("Praha 1");
        criteria.setMinPrice(1000.0);
        criteria.setStatus(Status.AVAILABLE);

        Root<RealEstate> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Join addressJoin = mock(Join.class);
        Path path = mock(Path.class);
        Subquery subquery = mock(Subquery.class);

        // Mocking the behavior of CriteriaBuilder/Root
        when(root.type()).thenReturn(path);
        when(root.join(eq("address"), any(JoinType.class))).thenReturn(addressJoin);
        when(addressJoin.get(anyString())).thenReturn(path);
        when(query.subquery(any())).thenReturn(subquery);
        when(subquery.from(any(Class.class))).thenReturn(mock(Root.class));
        when(subquery.select(any())).thenReturn(subquery);
        when(subquery.where(any(Predicate.class))).thenReturn(subquery);

        when(cb.equal(any(), any())).thenReturn(mock(Predicate.class));
        when(cb.like(any(), anyString())).thenReturn(mock(Predicate.class));
        when(cb.greaterThanOrEqualTo(any(Expression.class), any(Double.class))).thenReturn(mock(Predicate.class));
        when(cb.conjunction()).thenReturn(mock(Predicate.class));

        // Act
        Specification<RealEstate> spec = RealEstateSpecification.filterBy(criteria);
        Predicate predicate = spec.toPredicate(root, query, cb);

        // Assert
        assertNotNull(spec);
        verify(cb, atLeastOnce()).equal(any(), any());
        verify(root).join(eq("address"), any());
    }
}
