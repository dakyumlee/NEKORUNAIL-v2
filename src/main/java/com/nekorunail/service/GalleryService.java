package com.nekorunail.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.nekorunail.entity.GalleryItem;
import com.nekorunail.repository.GalleryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GalleryService {
    private final GalleryRepository galleryRepository;
    private final Cloudinary cloudinary;

    public List<GalleryItem> getAllItems() {
        return galleryRepository.findAllByOrderByDisplayOrderAscCreatedAtDesc();
    }

    public List<GalleryItem> getFeaturedItems() {
        return galleryRepository.findByFeaturedTrueOrderByDisplayOrderAsc();
    }

    public List<GalleryItem> getMonthlyPicks() {
        return galleryRepository.findByMonthlyPickTrueOrderByDisplayOrderAsc();
    }

    public List<GalleryItem> getByColorTag(String color) {
        return galleryRepository.findByColorTag(color);
    }

    public GalleryItem getById(Long id) {
        return galleryRepository.findById(id).orElse(null);
    }

    @Transactional
    public GalleryItem save(GalleryItem item, MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "folder", "nekorunail/gallery",
                "resource_type", "image"
            ));
            item.setImageUrl((String) uploadResult.get("secure_url"));
        }
        return galleryRepository.save(item);
    }

    @Transactional
    public void delete(Long id) {
        galleryRepository.deleteById(id);
    }

    @Transactional
    public void updateOrder(Long id, Integer order) {
        galleryRepository.findById(id).ifPresent(item -> {
            item.setDisplayOrder(order);
            galleryRepository.save(item);
        });
    }
}
