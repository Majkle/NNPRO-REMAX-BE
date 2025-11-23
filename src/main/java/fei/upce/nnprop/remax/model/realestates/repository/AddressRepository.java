package fei.upce.nnprop.remax.model.realestates.repository;

import fei.upce.nnprop.remax.model.realestates.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
}