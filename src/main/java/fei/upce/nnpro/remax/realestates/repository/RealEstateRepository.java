package fei.upce.nnpro.remax.realestates.repository;

import fei.upce.nnpro.remax.model.realestates.entity.RealEstate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RealEstateRepository extends JpaRepository<RealEstate, Long>, JpaSpecificationExecutor<RealEstate> {
}
