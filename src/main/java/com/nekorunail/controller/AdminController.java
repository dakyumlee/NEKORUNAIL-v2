package com.nekorunail.controller;

import com.nekorunail.entity.Category;
import com.nekorunail.entity.GalleryItem;
import com.nekorunail.entity.Review;
import com.nekorunail.entity.SiteSettings;
import com.nekorunail.repository.CategoryRepository;
import com.nekorunail.repository.ReviewRepository;
import com.nekorunail.repository.SiteSettingsRepository;
import com.nekorunail.service.GalleryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final GalleryService galleryService;
    private final ReviewRepository reviewRepository;
    private final SiteSettingsRepository settingsRepository;
    private final CategoryRepository categoryRepository;

    @GetMapping("/login")
    public String login() {
        return "admin/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("galleryCount", galleryService.getAllItems().size());
        model.addAttribute("reviewCount", reviewRepository.count());
        return "admin/dashboard";
    }

    @GetMapping("/gallery")
    public String galleryManage(Model model) {
        model.addAttribute("items", galleryService.getAllItems());
        return "admin/gallery";
    }

    @GetMapping("/gallery/new")
    public String newGalleryItem(Model model) {
        model.addAttribute("item", new GalleryItem());
        model.addAttribute("categories", categoryRepository.findAllByOrderByDisplayOrderAsc());
        return "admin/gallery-form";
    }

    @GetMapping("/gallery/edit/{id}")
    public String editGalleryItem(@PathVariable Long id, Model model) {
        model.addAttribute("item", galleryService.getById(id));
        model.addAttribute("categories", categoryRepository.findAllByOrderByDisplayOrderAsc());
        return "admin/gallery-form";
    }

    @PostMapping("/gallery/save")
    public String saveGalleryItem(@ModelAttribute GalleryItem item,
                                  @RequestParam(required = false) MultipartFile imageFile) throws IOException {
        galleryService.save(item, imageFile);
        return "redirect:/admin/gallery";
    }

    @PostMapping("/gallery/delete/{id}")
    public String deleteGalleryItem(@PathVariable Long id) {
        galleryService.delete(id);
        return "redirect:/admin/gallery";
    }

    @GetMapping("/categories")
    public String categoryManage(Model model) {
        model.addAttribute("categories", categoryRepository.findAllByOrderByDisplayOrderAsc());
        return "admin/categories";
    }

    @PostMapping("/categories/save")
    public String saveCategory(@RequestParam String name, @RequestParam String displayName, @RequestParam(required = false) Integer displayOrder) {
        Category category = categoryRepository.findByName(name).orElse(new Category());
        category.setName(name);
        category.setDisplayName(displayName);
        category.setDisplayOrder(displayOrder != null ? displayOrder : 0);
        categoryRepository.save(category);
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable Long id) {
        categoryRepository.deleteById(id);
        return "redirect:/admin/categories";
    }

    @GetMapping("/reviews")
    public String reviewManage(Model model) {
        model.addAttribute("reviews", reviewRepository.findAllByOrderByCreatedAtDesc());
        return "admin/reviews";
    }

    @GetMapping("/reviews/new")
    public String newReview(Model model) {
        model.addAttribute("review", new Review());
        return "admin/review-form";
    }

    @PostMapping("/reviews/save")
    public String saveReview(@ModelAttribute Review review) {
        reviewRepository.save(review);
        return "redirect:/admin/reviews";
    }

    @PostMapping("/reviews/delete/{id}")
    public String deleteReview(@PathVariable Long id) {
        reviewRepository.deleteById(id);
        return "redirect:/admin/reviews";
    }

    @GetMapping("/settings")
    public String settings(Model model) {
        String aiPrompt = settingsRepository.findByKey("ai_prompt")
            .map(SiteSettings::getValue)
            .orElse("");
        model.addAttribute("aiPrompt", aiPrompt);
        return "admin/settings";
    }

    @PostMapping("/settings/ai-prompt")
    public String saveAiPrompt(@RequestParam String aiPrompt) {
        SiteSettings settings = settingsRepository.findByKey("ai_prompt")
            .orElse(new SiteSettings());
        settings.setKey("ai_prompt");
        settings.setValue(aiPrompt);
        settingsRepository.save(settings);
        return "redirect:/admin/settings";
    }
}
