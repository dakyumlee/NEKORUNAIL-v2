package com.nekorunail.controller;

import com.nekorunail.repository.CategoryRepository;
import com.nekorunail.service.GalleryService;
import com.nekorunail.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class MainController {
    private final GalleryService galleryService;
    private final ReviewRepository reviewRepository;
    private final CategoryRepository categoryRepository;

    @Value("${naver.booking.url}")
    private String naverBookingUrl;

    @Value("${naver.review.url}")
    private String naverReviewUrl;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("monthlyPicks", galleryService.getMonthlyPicks());
        model.addAttribute("featuredReviews", reviewRepository.findByFeaturedTrueOrderByDisplayOrderAsc());
        model.addAttribute("naverBookingUrl", naverBookingUrl);
        return "pages/home";
    }

    @GetMapping("/gallery")
    public String gallery(Model model) {
        model.addAttribute("galleryItems", galleryService.getAllItems());
        model.addAttribute("categories", categoryRepository.findAllByOrderByDisplayOrderAsc());
        return "pages/gallery";
    }

    @GetMapping("/price")
    public String price() {
        return "pages/price";
    }

    @GetMapping("/location")
    public String location() {
        return "pages/location";
    }

    @GetMapping("/review")
    public String review(Model model) {
        model.addAttribute("naverReviewUrl", naverReviewUrl);
        model.addAttribute("reviews", reviewRepository.findByFeaturedTrueOrderByDisplayOrderAsc());
        return "pages/review";
    }
}
