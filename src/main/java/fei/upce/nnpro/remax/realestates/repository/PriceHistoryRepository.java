package fei.upce.nnpro.remax.realestates.repository;

import fei.upce.nnpro.remax.realestates.entity.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long>, JpaSpecificationExecutor<PriceHistory> {
}
