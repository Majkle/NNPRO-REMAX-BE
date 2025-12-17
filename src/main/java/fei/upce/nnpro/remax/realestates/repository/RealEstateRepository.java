package fei.upce.nnpro.remax.realestates.repository;

import fei.upce.nnpro.remax.realestates.entity.RealEstate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface RealEstateRepository extends JpaRepository<RealEstate, Long>, JpaSpecificationExecutor<RealEstate> {
    List<RealEstate> findAllByRealtorId(Long id);
}
