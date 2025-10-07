package vn.controller.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.entity.*;
import vn.service.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class ProductController {

    @Autowired
    private ProductService productService;
    
    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private BrandService brandService;
    
    @Value("${upload.path}")
    private String uploadPath;

    @ModelAttribute("user")
    public User user(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            model.addAttribute("user", user);
        }
        return user;
    }

    @ModelAttribute("products")
    public List<Product> showProduct(Model model) {
        List<Product> products = productService.findAll();
        model.addAttribute("products", products);
        return products;
    }

    @ModelAttribute("categoryList")
    public List<Category> showCategory(Model model) {
        List<Category> categoryList = categoryService.getAllCategories();
        model.addAttribute("categoryList", categoryList);
        return categoryList;
    }
    
    @ModelAttribute("brandList")
    public List<Brand> showBrand(Model model) {
        List<Brand> brandList = brandService.findAll();
        model.addAttribute("brandList", brandList);
        return brandList;
    }

    @GetMapping("/products")
    public String products(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "productId") String sortBy,
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
        
        // Search or get all products
        Page<Product> productPage;
        if (search != null && !search.trim().isEmpty()) {
            productPage = productService.findByProductNameContainingIgnoreCase(search, pageable);
        } else {
            // For now, we'll get all products and create a manual page
            List<Product> allProducts = productService.findAll();
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), allProducts.size());
            List<Product> pageContent = allProducts.subList(start, end);
            productPage = new org.springframework.data.domain.PageImpl<>(pageContent, pageable, allProducts.size());
        }
        
        model.addAttribute("productPage", productPage);
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalElements", productPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("search", search);
        
        Product product = new Product();
        model.addAttribute("product", product);
        
        return "admin/products";
    }

    @PostMapping("/addProduct")
    public String addProduct(@ModelAttribute("product") Product product, 
                           @RequestParam("file") MultipartFile file,
                           @RequestParam("categoryId") Long categoryId,
                           @RequestParam("brandId") Long brandId,
                           HttpServletRequest httpServletRequest) {
        
        try {
            // Set category and brand
            Category category = categoryService.getCategoryById(categoryId).orElse(null);
            Brand brand = brandService.findById(brandId).orElse(null);
            product.setCategory(category);
            product.setBrand(brand);
            
            // Set entered date if not set
            if (product.getEnteredDate() == null) {
                product.setEnteredDate(new Date());
            }
            
            // Set default values
            if (product.getStatus() == null) {
                product.setStatus(true);
            }
            if (product.getFavorite() == null) {
                product.setFavorite(false);
            }
            if (product.getDiscount() == null) {
                product.setDiscount(0);
            }

            // Handle file upload
            if (file != null && !file.isEmpty()) {
                String fileName = saveUploadedFile(file, product.getProductName());
                product.setProductImage(fileName);
            }

            Product savedProduct = productService.save(product);
            if (savedProduct != null) {
                return "redirect:/admin/products?success=true&action=add";
            } else {
                return "redirect:/admin/products?error=true&action=add";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/admin/products?error=true&action=add";
        }
    }

    @GetMapping("/editProduct/{id}")
    public String editProduct(@PathVariable("id") Long id, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        Product product = productService.findById(id).orElse(null);
        if (product != null) {
            model.addAttribute("product", product);
            return "admin/editProduct";
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/editProduct/{id}")
    public String updateProduct(@PathVariable("id") Long id,
                              @ModelAttribute("product") Product product,
                              @RequestParam(value = "file", required = false) MultipartFile file,
                              @RequestParam("categoryId") Long categoryId,
                              @RequestParam("brandId") Long brandId,
                              HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        Product existingProduct = productService.findById(id).orElse(null);
        if (existingProduct != null) {
            // Update fields
            existingProduct.setProductName(product.getProductName());
            existingProduct.setDescription(product.getDescription());
            existingProduct.setPrice(product.getPrice());
            existingProduct.setQuantity(product.getQuantity());
            existingProduct.setDiscount(product.getDiscount());
            existingProduct.setStatus(product.getStatus());
            existingProduct.setFavorite(product.getFavorite());
            existingProduct.setManufactureDate(product.getManufactureDate());
            existingProduct.setExpiryDate(product.getExpiryDate());
            
            // Set category and brand
            Category category = categoryService.getCategoryById(categoryId).orElse(null);
            Brand brand = brandService.findById(brandId).orElse(null);
            existingProduct.setCategory(category);
            existingProduct.setBrand(brand);
            
            // Handle file upload only if new file is provided
            if (file != null && !file.isEmpty()) {
                String fileName = saveUploadedFile(file, product.getProductName());
                existingProduct.setProductImage(fileName);
            }
            
            productService.save(existingProduct);
            return "redirect:/admin/products?success=true&action=edit";
        }
        return "redirect:/admin/products?error=true&action=edit";
    }

    @GetMapping("/deleteProduct/{id}")
    public String deleteProduct(@PathVariable("id") Long id, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        productService.deleteById(id);
        return "redirect:/admin/products?success=true&action=delete";
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(true);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(sdf, true));
    }
    
    /**
     * Save uploaded file to upload directory
     * @param file MultipartFile to save
     * @param productName Name of product to use as filename
     * @return String filename for database storage
     */
    private String saveUploadedFile(MultipartFile file, String productName) {
        try {
            String workingDir = System.getProperty("user.dir");

            // Determine best upload directory (be resilient to working dir at repo root)
            File primary = new File(workingDir + File.separatorChar + uploadPath); // e.g., D:/DoAnWebMyPham/upload/images
            File moduleRoot = new File(workingDir + File.separator + "Web_MyPham" + File.separator + "OneShop");
            File secondary = new File(moduleRoot, uploadPath); // e.g., D:/DoAnWebMyPham/Web_MyPham/OneShop/upload/images

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

            // Create filename based on product name
            String fileName = slugify(productName) + "_" + System.currentTimeMillis() + extension;

            // Save file
            File convFile = new File(targetDir, fileName);
            System.out.println("Saving file to: " + convFile.getAbsolutePath());
            try (FileOutputStream fos = new FileOutputStream(convFile)) {
                fos.write(file.getBytes());
            }

            return fileName;

        } catch (IOException e) {
            System.out.println("Error saving file: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Convert product name to slug for filename
     * @param productName Product name
     * @return Slugified string
     */
    private String slugify(String productName) {
        if (productName == null) return "product";
        
        return productName.toLowerCase()
                .trim()
                .replaceAll("\\s+", "-")
                .replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a")
                .replaceAll("[èéẹẻẽêềếệểễ]", "e")
                .replaceAll("[ìíịỉĩ]", "i")
                .replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o")
                .replaceAll("[ùúụủũưừứựửữ]", "u")
                .replaceAll("[ỳýỵỷỹ]", "y")
                .replaceAll("[đ]", "d")
                .replaceAll("[^a-z0-9\\-]", "")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }
}
