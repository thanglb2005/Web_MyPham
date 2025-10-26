package vn.controller.admin;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.entity.Brand;
import vn.service.BrandService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

/**
 * Controller for Brand management in admin panel
 * @author OneShop Team
 */
@Controller
@RequestMapping("/admin")
public class BrandController {
    
    @Autowired
    private BrandService brandService;

    @Value("${upload.brands.path}")
    private String uploadPath;    /**
     * Display brand management page
     */
    @GetMapping("/brands")
    public String brandsPage(
            Model model,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "sortBy", defaultValue = "brandId") String sortBy,
            @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir,
            @RequestParam(name = "success", required = false) String success,
            @RequestParam(name = "error", required = false) String error,
            Principal principal
    ) {
        Sort sort = Sort.by(sortDir.equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Brand> brandPage;
        if (search != null && !search.trim().isEmpty()) {
            brandPage = brandService.findByBrandNameContainingIgnoreCase(search, pageable);
        } else {
            brandPage = brandService.findAll(pageable);
        }
        
        int totalPages = brandPage.getTotalPages();
        if (totalPages > 0 && page >= totalPages) {
            int lastPage = Math.max(0, totalPages - 1);
            StringBuilder redirect = new StringBuilder("redirect:/admin/brands");
            redirect.append("?page=").append(lastPage);
            redirect.append("&size=").append(size);
            redirect.append("&sortBy=").append(sortBy);
            redirect.append("&sortDir=").append(sortDir);
            if (search != null && !search.trim().isEmpty()) {
                redirect.append("&search=").append(search);
            }
            if (success != null && !success.isBlank()) {
                redirect.append("&success=").append(success);
            }
            if (error != null && !error.isBlank()) {
                redirect.append("&error=").append(error);
            }
            return redirect.toString();
        }

        model.addAttribute("brands", brandPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", brandPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("search", search);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        
        // For new brand form
        model.addAttribute("brand", new Brand());
        
        return "admin/brands";
    }
    
    /**
     * Add a new brand
     */
    @PostMapping("/addBrand")
    public String addBrand(
            @Valid @ModelAttribute("brand") Brand brand,
            BindingResult result,
            @RequestParam("brandImageFile") MultipartFile brandImageFile
    ) {
        // Check if brand name exists
        if (brandService.existsByBrandName(brand.getBrandName())) {
            result.rejectValue("brandName", "error.brand", "Tên thương hiệu đã tồn tại");
        }
        
        if (result.hasErrors()) {
            return "redirect:/admin/brands?error=true";
        }
        
        // Handle image upload
        if (!brandImageFile.isEmpty()) {
            try {
                String fileName = UUID.randomUUID().toString() + "_" + brandImageFile.getOriginalFilename();
                Path path = Paths.get(uploadPath, fileName);
                Files.write(path, brandImageFile.getBytes());
                brand.setBrandImage(fileName);
            } catch (IOException e) {
                return "redirect:/admin/brands?error=image";
            }
        }
        
        brandService.save(brand);
        return "redirect:/admin/brands?success=added";
    }
    
    /**
     * Update an existing brand
     */
    @PostMapping("/updateBrand")
    public String updateBrand(
            @Valid @ModelAttribute("brand") Brand brand,
            BindingResult result,
            @RequestParam("brandImageFile") MultipartFile brandImageFile
    ) {
        // Check if brand exists
        Optional<Brand> existingBrand = brandService.findById(brand.getBrandId());
        if (existingBrand.isEmpty()) {
            return "redirect:/admin/brands?error=notfound";
        }
        
        // Check if name exists for other brands
        Optional<Brand> brandWithName = brandService.findByBrandName(brand.getBrandName());
        if (brandWithName.isPresent() && !brandWithName.get().getBrandId().equals(brand.getBrandId())) {
            result.rejectValue("brandName", "error.brand", "Tên thương hiệu đã tồn tại");
            return "redirect:/admin/brands?error=duplicate";
        }
        
        if (result.hasErrors()) {
            return "redirect:/admin/brands?error=true";
        }
        
        // Keep existing image if no new image is uploaded
        if (brandImageFile.isEmpty()) {
            brand.setBrandImage(existingBrand.get().getBrandImage());
        } else {
            try {
                String fileName = UUID.randomUUID().toString() + "_" + brandImageFile.getOriginalFilename();
                Path path = Paths.get(uploadPath, fileName);
                Files.write(path, brandImageFile.getBytes());
                
                // Delete old image if exists
                if (existingBrand.get().getBrandImage() != null) {
                    try {
                        Path oldImagePath = Paths.get(uploadPath, existingBrand.get().getBrandImage());
                        Files.deleteIfExists(oldImagePath);
                    } catch (IOException e) {
                        // Log error but continue
                        System.err.println("Could not delete old image: " + e.getMessage());
                    }
                }
                
                brand.setBrandImage(fileName);
            } catch (IOException e) {
                return "redirect:/admin/brands?error=image";
            }
        }
        
        brandService.save(brand);
        return "redirect:/admin/brands?success=updated";
    }
    
    /**
     * Delete a brand
     */
    @PostMapping("/deleteBrand/{id}")
    public String deleteBrand(@PathVariable("id") Long id) {
        Optional<Brand> brand = brandService.findById(id);
        if (brand.isPresent()) {
            // Delete brand image if exists
            if (brand.get().getBrandImage() != null) {
                try {
                    Path imagePath = Paths.get(uploadPath, brand.get().getBrandImage());
                    Files.deleteIfExists(imagePath);
                } catch (IOException e) {
                    // Log error but continue with deletion
                    System.err.println("Could not delete brand image: " + e.getMessage());
                }
            }
            
            brandService.deleteById(id);
            return "redirect:/admin/brands?success=deleted";
        }
        
        return "redirect:/admin/brands?error=notfound";
    }
    
    /**
     * Get brand data for editing
     */
    @GetMapping("/getBrandData/{id}")
    @ResponseBody
    public Brand getBrandData(@PathVariable("id") Long id) {
        Optional<Brand> brand = brandService.findById(id);
        return brand.orElse(null);
    }
    
    /**
     * Toggle brand status (active/inactive)
     */
    @PostMapping("/toggleBrandStatus/{id}")
    public String toggleBrandStatus(@PathVariable("id") Long id) {
        Optional<Brand> brandOpt = brandService.findById(id);
        if (brandOpt.isPresent()) {
            Brand brand = brandOpt.get();
            brand.setStatus(!brand.getStatus());
            brandService.save(brand);
            return "redirect:/admin/brands?success=toggled";
        }
        
        return "redirect:/admin/brands?error=notfound";
    }
}