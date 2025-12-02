package fei.upce.nnpro.remax.address;

import fei.upce.nnpro.remax.model.realestates.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
}