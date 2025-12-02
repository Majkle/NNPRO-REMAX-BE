package fei.upce.nnpro.remax.profile.repository;

import fei.upce.nnpro.remax.profile.entity.PersonalInformation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonalInformationRepository extends JpaRepository<PersonalInformation, Long> {
}