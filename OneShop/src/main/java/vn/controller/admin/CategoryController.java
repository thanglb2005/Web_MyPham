package vn.controller.admin;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.entity.Category;
import vn.entity.User;
import vn.service.CategoryService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Controller
@RequestMapping("/admin")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;
    
    @Value("${upload.images.path}")
    private String uploadPath;

    @GetMapping("/categories")
    public String categories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "categoryId") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search,
            HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        // Create Pageable object
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                    Sort.by(sortBy).descending() : 
                    Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Search or get all categories
        Page<Category> categoryPage;
        if (search != null && !search.trim().isEmpty()) {
            categoryPage = categoryService.searchCategories(search, pageable);
        } else {
            categoryPage = categoryService.getAllCategoriesPaged(pageable);
        }
        
        model.addAttribute("categoryPage", categoryPage);
        model.addAttribute("categories", categoryPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", categoryPage.getTotalPages());
        model.addAttribute("totalElements", categoryPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("search", search);
        
        return "admin/categories";
    }

    @PostMapping("/categories/add")
    public String addCategory(@RequestParam String categoryName, 
                             @RequestParam(required = false) MultipartFile categoryImageFile,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size,
                             @RequestParam(defaultValue = "categoryId") String sortBy,
                             @RequestParam(defaultValue = "asc") String sortDir,
                             @RequestParam(required = false) String search,
                             HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        Category category = new Category();
        category.setCategoryName(categoryName);
        
        // Handle file upload only
        String finalImagePath = null;
        if (categoryImageFile != null && !categoryImageFile.isEmpty()) {
            finalImagePath = saveUploadedFile(categoryImageFile, categoryName);
        }
        
        category.setCategoryImage(finalImagePath);
        categoryService.saveCategory(category);
        
        // Build redirect URL with current parameters
        StringBuilder redirectUrl = new StringBuilder("/admin/categories?success=true&action=add");
        redirectUrl.append("&page=").append(page);
        redirectUrl.append("&size=").append(size);
        redirectUrl.append("&sortBy=").append(sortBy);
        redirectUrl.append("&sortDir=").append(sortDir);
        if (search != null && !search.trim().isEmpty()) {
            redirectUrl.append("&search=").append(search);
        }
        
        return "redirect:" + redirectUrl.toString();
    }

    @GetMapping("/categories/edit/{id}")
    public String editCategoryForm(@PathVariable Long id, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        Category category = categoryService.getCategoryById(id).orElse(null);
        if (category != null) {
            model.addAttribute("category", category);
            return "admin/editCategory";
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/edit/{id}")
    public String editCategory(@PathVariable Long id, 
                              @RequestParam String categoryName,
                              @RequestParam(required = false) MultipartFile categoryImageFile,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(defaultValue = "categoryId") String sortBy,
                              @RequestParam(defaultValue = "asc") String sortDir,
                              @RequestParam(required = false) String search,
                              HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        Category category = categoryService.getCategoryById(id).orElse(null);
        if (category != null) {
            category.setCategoryName(categoryName);
            
            // Handle file upload only, keep existing if no new file
            if (categoryImageFile != null && !categoryImageFile.isEmpty()) {
                String finalImagePath = saveUploadedFile(categoryImageFile, categoryName);
                category.setCategoryImage(finalImagePath);
            }
            // If no new file uploaded, keep existing image
            
            categoryService.saveCategory(category);
        }
        
        // Build redirect URL with current parameters
        StringBuilder redirectUrl = new StringBuilder("/admin/categories?success=true&action=edit");
        redirectUrl.append("&page=").append(page);
        redirectUrl.append("&size=").append(size);
        redirectUrl.append("&sortBy=").append(sortBy);
        redirectUrl.append("&sortDir=").append(sortDir);
        if (search != null && !search.trim().isEmpty()) {
            redirectUrl.append("&search=").append(search);
        }
        
        return "redirect:" + redirectUrl.toString();
    }

    @GetMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable Long id, 
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size,
                                @RequestParam(defaultValue = "categoryId") String sortBy,
                                @RequestParam(defaultValue = "asc") String sortDir,
                                @RequestParam(required = false) String search,
                                HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        // Xóa luôn tất cả products liên quan
        categoryService.deleteCategory(id);
        
        // Build redirect URL with current parameters
        StringBuilder redirectUrl = new StringBuilder("/admin/categories?success=true&action=delete");
        redirectUrl.append("&page=").append(page);
        redirectUrl.append("&size=").append(size);
        redirectUrl.append("&sortBy=").append(sortBy);
        redirectUrl.append("&sortDir=").append(sortDir);
        if (search != null && !search.trim().isEmpty()) {
            redirectUrl.append("&search=").append(search);
        }
        
        return "redirect:" + redirectUrl.toString();
    }
    
    /**
     * Save uploaded file to upload directory
     * @param file MultipartFile to save
     * @param categoryName Name of category to use as filename
     * @return String filename for database storage
     */
    private String saveUploadedFile(MultipartFile file, String categoryName) {
        try {
            String workingDir = System.getProperty("user.dir");

            // Determine best upload directory (be resilient to working dir at repo root)
            File primary = new File(workingDir + File.separatorChar + uploadPath); // e.g., D:/DoAnWebMyPham/upload/images
            File moduleRoot = new File(workingDir + File.separator + "DoAn_Web_MyPham" + File.separator + "Web_MyPham" + File.separator + "OneShop");
            File secondary = new File(moduleRoot, uploadPath); // e.g., D:/DoAnWebMyPham/DoAn_Web_MyPham/Web_MyPham/OneShop/upload/images

            // Prefer module upload folder (inside OneShop) if available; fallback to primary root folder
            File targetDir = (moduleRoot.exists() ? secondary : primary);

            if (!targetDir.exists()) {
                System.out.println("Creating upload directory at: " + targetDir.getAbsolutePath());
                targetDir.mkdirs();
            }
            
            // Get file extension
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            
            // Create filename based on category name
            String fileName = slugify(categoryName) + extension;
            
            // Save file
            File convFile = new File(targetDir, fileName);
            System.out.println("Saving file to: " + convFile.getAbsolutePath());
            try (FileOutputStream fos = new FileOutputStream(convFile)) {
                fos.write(file.getBytes());
            }
            
            // Return only filename for database storage
            return fileName;
            
        } catch (IOException e) {
            System.out.println("Error saving file: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Convert category name to slug for filename
     * @param categoryName Category name
     * @return Slugified string
     */
    private String slugify(String categoryName) {
        if (categoryName == null) return "category";
        
        return categoryName.toLowerCase()
                .trim()
                .replaceAll("\\s+", "-")      // Replace spaces with hyphens
                .replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a")
                .replaceAll("[èéẹẻẽêềếệểễ]", "e")
                .replaceAll("[ìíịỉĩ]", "i")
                .replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o")
                .replaceAll("[ùúụủũưừứựửữ]", "u")
                .replaceAll("[ỳýỵỷỹ]", "y")
                .replaceAll("[đ]", "d")
                .replaceAll("[^a-z0-9\\-]", "")  // Remove non-alphanumeric except hyphens
                .replaceAll("-+", "-")          // Replace multiple hyphens with single
                .replaceAll("^-|-$", "");       // Remove leading/trailing hyphens
    }
}
