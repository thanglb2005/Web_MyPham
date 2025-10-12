package vn.controller.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
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
import vn.dto.ShopProductStatistics;
import vn.entity.*;
import vn.service.BrandService;
import vn.service.CategoryService;
import vn.service.ImageStorageService;
import vn.service.ProductService;
import vn.service.ShopService;

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

    @Autowired
    private ImageStorageService imageStorageService;

    @Autowired
    private ShopService shopService;

    @ModelAttribute("user")
    public User user(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            model.addAttribute("user", user);
        }
        return user;
    }

    @ModelAttribute("shopList")
    public List<Shop> showShopList(Model model) {
        List<Shop> shopList = shopService.findAll();
        model.addAttribute("shopList", shopList);
        return shopList;
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
            @RequestParam(required = false) Long shopId,
            HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                    Sort.by(sortBy).descending() :
                    Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        String trimmedSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;

        Page<Product> productPage;
        if (trimmedSearch != null && shopId != null) {
            productPage = productService.findByShopAndName(shopId, trimmedSearch, pageable);
        } else if (trimmedSearch != null) {
            productPage = productService.findByProductNameContainingIgnoreCase(trimmedSearch, pageable);
        } else if (shopId != null) {
            productPage = productService.findByShopId(shopId, pageable);
        } else {
            productPage = productService.findAll(pageable);
        }

        model.addAttribute("productPage", productPage);
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalElements", productPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("search", trimmedSearch);
        model.addAttribute("selectedShopId", shopId);

        List<Shop> shopList = shopService.findAll();
        model.addAttribute("shopList", shopList);
        if (shopId != null) {
            shopList.stream()
                    .filter(shop -> shop.getShopId().equals(shopId))
                    .findFirst()
                    .ifPresent(shop -> model.addAttribute("selectedShopName", shop.getShopName()));
        }

        List<ShopProductStatistics> shopStats = productService.getShopProductStatistics();
        long totalShopCount = shopStats.size();
        long totalProductCount = shopStats.stream().mapToLong(ShopProductStatistics::getProductCount).sum();
        long totalQuantity = shopStats.stream().mapToLong(ShopProductStatistics::getTotalQuantity).sum();
        double totalInventoryValue = shopStats.stream().mapToDouble(ShopProductStatistics::getTotalInventoryValue).sum();

        model.addAttribute("shopStats", shopStats);
        model.addAttribute("totalShopCount", totalShopCount);
        model.addAttribute("totalProductCountSummary", totalProductCount);
        model.addAttribute("totalQuantitySummary", totalQuantity);
        model.addAttribute("totalInventoryValueSummary", totalInventoryValue);

        Product product = new Product();
        model.addAttribute("product", product);

        return "admin/products";
    }

@PostMapping("/addProduct")
    public String addProduct(@ModelAttribute("product") Product product, 
                           @RequestParam("file") MultipartFile file,
                           @RequestParam("categoryId") Long categoryId,
                           @RequestParam("brandId") Long brandId,
                           @RequestParam("shopId") Long shopId,
                           HttpServletRequest httpServletRequest) {
        
        try {
            // Set category and brand
            Category category = categoryService.getCategoryById(categoryId).orElse(null);
            Brand brand = brandService.findById(brandId).orElse(null);
            product.setCategory(category);
            product.setBrand(brand);

            Shop shop = shopService.findById(shopId).orElse(null);
            if (shop == null) {
                return "redirect:/admin/products?error=true&action=add";
            }
            product.setShop(shop);
            
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
                String fileName = imageStorageService.store(file, product.getProductName());
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
            model.addAttribute("shopList", shopService.findAll());
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
                           @RequestParam("shopId") Long shopId,
                              HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        Product existingProduct = productService.findById(id).orElse(null);
        if (existingProduct != null) {
            try {
                existingProduct.setProductName(product.getProductName());
                existingProduct.setDescription(product.getDescription());
                existingProduct.setPrice(product.getPrice());
                existingProduct.setQuantity(product.getQuantity());
                existingProduct.setDiscount(product.getDiscount());
                existingProduct.setStatus(product.getStatus());
                existingProduct.setFavorite(product.getFavorite());
                existingProduct.setManufactureDate(product.getManufactureDate());
                existingProduct.setExpiryDate(product.getExpiryDate());

                Category category = categoryService.getCategoryById(categoryId).orElse(null);
                Brand brand = brandService.findById(brandId).orElse(null);
                existingProduct.setCategory(category);
                existingProduct.setBrand(brand);

                Shop shop = shopService.findById(shopId).orElse(null);
                if (shop != null) {
                    existingProduct.setShop(shop);
                }

                if (file != null && !file.isEmpty()) {
                    String fileName = imageStorageService.store(file, product.getProductName());
                    existingProduct.setProductImage(fileName);
                }

                productService.save(existingProduct);
                return "redirect:/admin/products?success=true&action=edit";
            } catch (Exception e) {
                e.printStackTrace();
                return "redirect:/admin/products?error=true&action=edit";
            }
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
    
}
