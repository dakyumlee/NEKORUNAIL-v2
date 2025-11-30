package com.nekorunail.repository;

import com.nekorunail.entity.GalleryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GalleryRepository extends JpaRepository<GalleryItem, Long> {
    List<GalleryItem> findAllByOrderByDisplayOrderAscCreatedAtDesc();
    List<GalleryItem> findByFeaturedTrueOrderByDisplayOrderAsc();
    List<GalleryItem> findByMonthlyPickTrueOrderByDisplayOrderAsc();
    List<GalleryItem> findByColorTagsContaining(String colorTag);
    
    default List<GalleryItem> findByColorTag(String color) {
        return findByColorTagsContaining(color);
    }
}
