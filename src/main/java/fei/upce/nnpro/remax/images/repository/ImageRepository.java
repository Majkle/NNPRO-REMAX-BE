package fei.upce.nnpro.remax.images.repository;

import fei.upce.nnpro.remax.images.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {
}