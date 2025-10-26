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
import vn.entity.Provider;
import vn.service.ProviderService;
import vn.service.StorageService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

/**
 * Controller for Provider management in admin panel
 * @author OneShop Team
 */
@Controller
@RequestMapping("/admin")
public class ProviderController {
    
    @Autowired
    private ProviderService providerService;
    
    @Autowired
    private StorageService storageService;

    @Value("${upload.providers.path}")
    private String uploadPath;
    
    /**
     * Display provider management page
     */
    @GetMapping("/providers")
    public String providersPage(
            Model model,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "sortBy", defaultValue = "providerId") String sortBy,
            @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir,
            @RequestParam(name = "success", required = false) String success,
            @RequestParam(name = "error", required = false) String error,
            Principal principal
    ) {
        Sort sort = Sort.by(sortDir.equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Provider> providerPage;
        if (search != null && !search.trim().isEmpty()) {
            providerPage = providerService.findByProviderNameContainingIgnoreCase(search, pageable);
        } else {
            providerPage = providerService.findAll(pageable);
        }
        
        int totalPages = providerPage.getTotalPages();
        if (totalPages > 0 && page >= totalPages) {
            int lastPage = Math.max(0, totalPages - 1);
            StringBuilder redirect = new StringBuilder("redirect:/admin/providers");
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

        model.addAttribute("providers", providerPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", providerPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("search", search);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        
        // For new provider form
        model.addAttribute("provider", new Provider());
        
        return "admin/providers";
    }
    
    /**
     * Add a new provider
     */
    @PostMapping("/addProvider")
    public String addProvider(
            @Valid @ModelAttribute("provider") Provider provider,
            BindingResult bindingResult,
            @RequestParam("imageFile") MultipartFile imageFile,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            return "redirect:/admin/providers?error=validation";
        }
        
        // Check if provider name already exists
        if (providerService.existsByProviderName(provider.getProviderName())) {
            return "redirect:/admin/providers?error=duplicate";
        }
        
        // Handle image upload - sử dụng Cloudinary với fallback
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String imageUrl = storageService.storeGeneralImage(imageFile);
                provider.setLogo(imageUrl);
            } catch (Exception e) {
                // Fallback to old method if Cloudinary fails
                try {
                    String fileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
                    Path filePath = Paths.get(uploadPath, fileName);
                    Files.createDirectories(filePath.getParent());
                    Files.write(filePath, imageFile.getBytes());
                    provider.setLogo(fileName);
                } catch (IOException ioException) {
                    System.err.println("Error uploading provider image: " + ioException.getMessage());
                    return "redirect:/admin/providers?error=upload";
                }
            }
        }
        
        providerService.save(provider);
        return "redirect:/admin/providers?success=added";
    }
    
    /**
     * Update a provider
     */
    @PostMapping("/updateProvider")
    public String updateProvider(
            @Valid @ModelAttribute("provider") Provider provider,
            BindingResult bindingResult,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile
    ) {
        if (bindingResult.hasErrors()) {
            return "redirect:/admin/providers?error=validation";
        }
        
        // Handle image upload if new image is provided - sử dụng Cloudinary với fallback
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String imageUrl = storageService.storeGeneralImage(imageFile);
                
                // Delete old image if exists (Cloudinary URL)
                Optional<Provider> existingProvider = providerService.findById(provider.getProviderId());
                if (existingProvider.isPresent() && existingProvider.get().getLogo() != null) {
                    try {
                        storageService.deleteImage(existingProvider.get().getLogo());
                    } catch (Exception e) {
                        System.err.println("Could not delete old image: " + e.getMessage());
                    }
                }
                
                provider.setLogo(imageUrl);
            } catch (Exception e) {
                // Fallback to old method if Cloudinary fails
                try {
                    // Delete old image if exists (local file)
                    Optional<Provider> existingProvider = providerService.findById(provider.getProviderId());
                    if (existingProvider.isPresent() && existingProvider.get().getLogo() != null) {
                        Path oldImagePath = Paths.get(uploadPath, existingProvider.get().getLogo());
                        Files.deleteIfExists(oldImagePath);
                    }
                    
                    // Save new image
                    String fileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
                    Path filePath = Paths.get(uploadPath, fileName);
                    Files.createDirectories(filePath.getParent());
                    Files.write(filePath, imageFile.getBytes());
                    provider.setLogo(fileName);
                } catch (IOException ioException) {
                    System.err.println("Error uploading provider image: " + ioException.getMessage());
                    return "redirect:/admin/providers?error=upload";
                }
            }
        }
        
        providerService.save(provider);
        return "redirect:/admin/providers?success=updated";
    }
    
    /**
     * Delete a provider
     */
    @PostMapping("/deleteProvider/{id}")
    public String deleteProvider(@PathVariable("id") Long id) {
        Optional<Provider> provider = providerService.findById(id);
        if (provider.isPresent()) {
            try {
                // Delete provider image if exists
                if (provider.get().getLogo() != null) {
                    try {
                        // Try to delete from Cloudinary first
                        storageService.deleteImage(provider.get().getLogo());
                    } catch (Exception e) {
                        // Fallback to local file deletion
                        try {
                            Path imagePath = Paths.get(uploadPath, provider.get().getLogo());
                            Files.deleteIfExists(imagePath);
                        } catch (IOException ioException) {
                            // Log error but continue with deletion
                            System.err.println("Could not delete provider image: " + ioException.getMessage());
                        }
                    }
                }
                
                providerService.deleteById(id);
                return "redirect:/admin/providers?success=deleted";
            } catch (Exception e) {
                // Handle foreign key constraint violation
                System.err.println("Cannot delete provider due to foreign key constraint: " + e.getMessage());
                return "redirect:/admin/providers?error=constraint";
            }
        }
        
        return "redirect:/admin/providers?error=notfound";
    }
    
    /**
     * Get provider data for editing
     */
    @GetMapping("/getProviderData/{id}")
    @ResponseBody
    public Provider getProviderData(@PathVariable("id") Long id) {
        Optional<Provider> provider = providerService.findById(id);
        return provider.orElse(null);
    }
    
    /**
     * Toggle provider status (active/inactive)
     */
    @GetMapping("/toggleProviderStatus/{id}")
    public String toggleProviderStatus(@PathVariable("id") Long id) {
        Optional<Provider> providerOpt = providerService.findById(id);
        if (providerOpt.isPresent()) {
            Provider provider = providerOpt.get();
            provider.setStatus(!provider.getStatus());
            providerService.save(provider);
            return "redirect:/admin/providers?success=toggled";
        }
        
        return "redirect:/admin/providers?error=notfound";
    }
}
