package fei.upce.nnpro.remax.realestates.service;

import fei.upce.nnpro.remax.images.entity.Image;
import fei.upce.nnpro.remax.images.repository.ImageRepository;
import fei.upce.nnpro.remax.realestates.entity.RealEstate;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RealEstateImageHelper {

    private final ImageRepository imageRepository;
    private static final Logger log = LoggerFactory.getLogger(RealEstateImageHelper.class);

    public void handleImages(RealEstate realEstate, List<Long> imageIds) {
        if (imageIds == null) return;

        List<Image> currentImages = realEstate.getImages();
        if (currentImages == null) {
            currentImages = new ArrayList<>();
            realEstate.setImages(currentImages);
        }

        if (imageIds.isEmpty()) {
            currentImages.clear();
            log.debug("Cleared images for estate id={}", realEstate.getId());
            return;
        }

        List<Image> newImages = imageRepository.findAllById(imageIds);

        newImages.forEach(img -> img.setRealEstate(realEstate));

        currentImages.addAll(newImages);

        log.debug("Associated {} images with estate id={}", newImages.size(), realEstate.getId());
    }
}
