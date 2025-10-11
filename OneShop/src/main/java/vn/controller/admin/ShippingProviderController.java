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
import vn.entity.ShippingProvider;
import vn.service.ShippingProviderService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

/**
 * Controller for Shipping Provider management in admin panel
 * @author OneShop Team
 */
@Controller
@RequestMapping("/admin")
public class ShippingProviderController {
    
    @Autowired
    private ShippingProviderService shippingProviderService;
    
    @Value("${upload.images.path}")
    private String uploadPath;
    
    /**
     * Display shipping provider management page
     */
    @GetMapping("/shipping-providers")
    public String shippingProvidersPage(
            Model model,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "sortBy", defaultValue = "providerId") String sortBy,
            @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir,
            Principal principal
    ) {
        Sort sort = Sort.by(sortDir.equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ShippingProvider> providerPage;
        if (search != null && !search.trim().isEmpty()) {
            providerPage = shippingProviderService.findByProviderNameContainingIgnoreCase(search, pageable);
        } else {
            providerPage = shippingProviderService.findAll(pageable);
        }
        
        model.addAttribute("providers", providerPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", providerPage.getTotalPages());
        model.addAttribute("totalItems", providerPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("search", search);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        
        // For new provider form
        model.addAttribute("provider", new ShippingProvider());
        
        return "admin/shipping-providers";
    }
    
    /**
     * Add a new shipping provider
     */
    @PostMapping("/addShippingProvider")
    public String addShippingProvider(
            @Valid @ModelAttribute("provider") ShippingProvider provider,
            BindingResult result,
            @RequestParam(value = "logoFile", required = false) MultipartFile logoFile
    ) {
        // Check if provider name exists
        if (shippingProviderService.existsByProviderName(provider.getProviderName())) {
            result.rejectValue("providerName", "error.provider", "Tên nhà vận chuyển đã tồn tại");
        }
        
        if (result.hasErrors()) {
            return "redirect:/admin/shipping-providers?error=true";
        }
        
        // Handle logo upload
        if (logoFile != null && !logoFile.isEmpty()) {
            try {
                String fileName = UUID.randomUUID().toString() + "_" + logoFile.getOriginalFilename();
                Path path = Paths.get(uploadPath, fileName);
                Files.write(path, logoFile.getBytes());
                provider.setLogo(fileName);
            } catch (IOException e) {
                return "redirect:/admin/shipping-providers?error=image";
            }
        }
        
        shippingProviderService.save(provider);
        return "redirect:/admin/shipping-providers?success=added";
    }
    
    /**
     * Update an existing shipping provider
     */
    @PostMapping("/updateShippingProvider")
    public String updateShippingProvider(
            @Valid @ModelAttribute("provider") ShippingProvider provider,
            BindingResult result,
            @RequestParam(value = "logoFile", required = false) MultipartFile logoFile
    ) {
        // Check if provider exists
        Optional<ShippingProvider> existingProvider = shippingProviderService.findById(provider.getProviderId());
        if (existingProvider.isEmpty()) {
            return "redirect:/admin/shipping-providers?error=notfound";
        }
        
        // Check if name exists for other providers
        Optional<ShippingProvider> providerWithName = shippingProviderService.findByProviderName(provider.getProviderName());
        if (providerWithName.isPresent() && !providerWithName.get().getProviderId().equals(provider.getProviderId())) {
            result.rejectValue("providerName", "error.provider", "Tên nhà vận chuyển đã tồn tại");
            return "redirect:/admin/shipping-providers?error=duplicate";
        }
        
        if (result.hasErrors()) {
            return "redirect:/admin/shipping-providers?error=true";
        }
        
        // Keep existing logo if no new logo is uploaded
        if (logoFile == null || logoFile.isEmpty()) {
            provider.setLogo(existingProvider.get().getLogo());
        } else {
            try {
                String fileName = UUID.randomUUID().toString() + "_" + logoFile.getOriginalFilename();
                Path path = Paths.get(uploadPath, fileName);
                Files.write(path, logoFile.getBytes());
                
                // Delete old logo if exists
                if (existingProvider.get().getLogo() != null) {
                    try {
                        Path oldLogoPath = Paths.get(uploadPath, existingProvider.get().getLogo());
                        Files.deleteIfExists(oldLogoPath);
                    } catch (IOException e) {
                        // Log error but continue
                        System.err.println("Could not delete old logo: " + e.getMessage());
                    }
                }
                
                provider.setLogo(fileName);
            } catch (IOException e) {
                return "redirect:/admin/shipping-providers?error=image";
            }
        }
        
        shippingProviderService.save(provider);
        return "redirect:/admin/shipping-providers?success=updated";
    }
    
    /**
     * Delete a shipping provider
     */
    @PostMapping("/deleteShippingProvider/{id}")
    public String deleteShippingProvider(@PathVariable("id") Long id) {
        Optional<ShippingProvider> provider = shippingProviderService.findById(id);
        if (provider.isPresent()) {
            // Delete provider logo if exists
            if (provider.get().getLogo() != null) {
                try {
                    Path logoPath = Paths.get(uploadPath, provider.get().getLogo());
                    Files.deleteIfExists(logoPath);
                } catch (IOException e) {
                    // Log error but continue with deletion
                    System.err.println("Could not delete logo: " + e.getMessage());
                }
            }
            
            shippingProviderService.deleteById(id);
            return "redirect:/admin/shipping-providers?success=deleted";
        }
        
        return "redirect:/admin/shipping-providers?error=notfound";
    }
    
    /**
     * Get shipping provider data for editing or viewing
     */
    @GetMapping("/getShippingProviderData/{id}")
    @ResponseBody
    public ShippingProvider getShippingProviderData(@PathVariable("id") Long id) {
        System.out.println("Fetching shipping provider data for ID: " + id);
        Optional<ShippingProvider> provider = shippingProviderService.findById(id);
        if (provider.isPresent()) {
            System.out.println("Found provider: " + provider.get().getProviderName());
            return provider.get();
        } else {
            System.out.println("Provider not found for ID: " + id);
            return null;
        }
    }
    
    /**
     * Toggle shipping provider status (active/inactive) with redirect
     */
    @PostMapping("/toggleShippingProviderStatus/{id}")
    public String toggleShippingProviderStatus(@PathVariable("id") Long id) {
        Optional<ShippingProvider> providerOpt = shippingProviderService.findById(id);
        if (providerOpt.isPresent()) {
            ShippingProvider provider = providerOpt.get();
            provider.setStatus(!provider.getStatus());
            shippingProviderService.save(provider);
            return "redirect:/admin/shipping-providers?success=toggled";
        }
        
        return "redirect:/admin/shipping-providers?error=notfound";
    }
    
    /**
     * Toggle shipping provider status (active/inactive) via AJAX
     */
    @PostMapping("/shipping-providers/toggle-status/{id}")
    @ResponseBody
    public String toggleShippingProviderStatusAjax(@PathVariable("id") Long id) {
        Optional<ShippingProvider> providerOpt = shippingProviderService.findById(id);
        if (providerOpt.isPresent()) {
            ShippingProvider provider = providerOpt.get();
            provider.setStatus(!provider.getStatus());
            shippingProviderService.save(provider);
            return "success";
        }
        
        return "error";
    }
}