package fei.upce.nnprop.remax.images.repository;

import fei.upce.nnprop.remax.model.image.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {
}