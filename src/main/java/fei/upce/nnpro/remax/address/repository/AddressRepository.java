package fei.upce.nnpro.remax.address.repository;

import fei.upce.nnpro.remax.address.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
}