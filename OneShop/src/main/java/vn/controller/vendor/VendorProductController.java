package vn.controller.vendor;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.dto.VendorProductForm;
import vn.entity.Brand;
import vn.entity.Category;
import vn.entity.Product;
import vn.entity.Shop;
import vn.entity.User;
import vn.service.BrandService;
import vn.service.CategoryService;
import vn.service.ImageStorageService;
import vn.service.ProductService;
import vn.service.ShopService;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/vendor/products")
public class VendorProductController {

    @Autowired
    private ShopService shopService;

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private ImageStorageService imageStorageService;

    @ModelAttribute("categories")
    public List<Category> categories() {
        return categoryService.getAllCategories();
    }

    @ModelAttribute("brands")
    public List<Brand> brands() {
        return brandService.findAll();
    }

    @GetMapping
    public String listProducts(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User vendor = ensureVendor(session);
        if (vendor == null) {
            return "redirect:/login";
        }

        Optional<Shop> shopOpt = shopService.findByVendor(vendor);
        if (shopOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("warning", "Bạn chưa có shop. Hãy đăng ký trước khi quản lý sản phẩm.");
            return "redirect:/vendor/shop/register";
        }

        Shop shop = shopOpt.get();
        List<Product> products = productService.findByShopId(shop.getShopId());

        model.addAttribute("shop", shop);
        model.addAttribute("products", products);
        model.addAttribute("pageTitle", "Sản phẩm của tôi");
        return "vendor/product-list";
    }

    @GetMapping("/new")
    public String createForm(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User vendor = ensureVendor(session);
        if (vendor == null) {
            return "redirect:/login";
        }

        Optional<Shop> shopOpt = shopService.findByVendor(vendor);
        if (shopOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("warning", "Bạn chưa có shop. Hãy đăng ký trước khi quản lý sản phẩm.");
            return "redirect:/vendor/shop/register";
        }

        if (!model.containsAttribute("productForm")) {
            VendorProductForm form = new VendorProductForm();
            model.addAttribute("productForm", form);
        }
        model.addAttribute("shop", shopOpt.get());
        model.addAttribute("pageTitle", "Thêm sản phẩm mới");
        return "vendor/product-form";
    }

    @PostMapping
    public String createProduct(@Valid @ModelAttribute("productForm") VendorProductForm form,
                                BindingResult bindingResult,
                                @RequestParam("image") MultipartFile imageFile,
                                HttpSession session,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        User vendor = ensureVendor(session);
        if (vendor == null) {
            return "redirect:/login";
        }

        Optional<Shop> shopOpt = shopService.findByVendor(vendor);
        if (shopOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("warning", "Bạn chưa có shop. Đăng ký trước khi quản lý sản phẩm.");
            return "redirect:/vendor/shop/register";
        }

        if (imageFile == null || imageFile.isEmpty()) {
            bindingResult.rejectValue("productName", "image", "Vui lòng chọn hình ảnh cho sản phẩm");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("shop", shopOpt.get());
            model.addAttribute("pageTitle", "Thêm sản phẩm mới");
            return "vendor/product-form";
        }

        Shop shop = shopOpt.get();
        Category category = categoryService.getCategoryById(form.getCategoryId()).orElse(null);
        Brand brand = brandService.findById(form.getBrandId()).orElse(null);

        if (category == null) {
            bindingResult.rejectValue("categoryId", "invalid", "Danh mục không hợp lệ");
        }
        if (brand == null) {
            bindingResult.rejectValue("brandId", "invalid", "Thương hiệu không hợp lệ");
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("shop", shop);
            model.addAttribute("pageTitle", "Thêm sản phẩm mới");
            return "vendor/product-form";
        }

        Product product = new Product();
        product.setProductName(form.getProductName().trim());
        product.setDescription(form.getDescription());
        product.setPrice(form.getPrice());
        product.setQuantity(form.getQuantity());
        product.setDiscount(form.getDiscount());
        product.setStatus(Boolean.TRUE.equals(form.getActive()));
        product.setFavorite(false);
        product.setEnteredDate(new Date());
        product.setCategory(category);
        product.setBrand(brand);
        product.setShop(shop);

        try {
            String imageName = imageStorageService.store(imageFile, form.getProductName());
            product.setProductImage(imageName);
        } catch (IOException e) {
            bindingResult.reject("upload", "Không thể lưu hình ảnh sản phẩm");
            model.addAttribute("shop", shop);
            model.addAttribute("pageTitle", "Thêm sản phẩm mới");
            return "vendor/product-form";
        }

        productService.save(product);
        shopService.refreshStatistics(shop);
        redirectAttributes.addFlashAttribute("success", "Thêm sản phẩm thành công");
        return "redirect:/vendor/products";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") Long productId,
                           HttpSession session,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        User vendor = ensureVendor(session);
        if (vendor == null) {
            return "redirect:/login";
        }

        Optional<Shop> shopOpt = shopService.findByVendor(vendor);
        if (shopOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("warning", "Bạn chưa có shop.");
            return "redirect:/vendor/shop/register";
        }

        Shop shop = shopOpt.get();
        Optional<Product> productOpt = productService.findByIdAndShop(productId, shop.getShopId());
        if (productOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy sản phẩm.");
            return "redirect:/vendor/products";
        }

        if (!model.containsAttribute("productForm")) {
            model.addAttribute("productForm", toForm(productOpt.get()));
        }

        model.addAttribute("shop", shop);
        model.addAttribute("product", productOpt.get());
        model.addAttribute("pageTitle", "Cập nhật sản phẩm");
        return "vendor/product-form";
    }

    @PostMapping("/{id}/update")
    public String updateProduct(@PathVariable("id") Long productId,
                                @Valid @ModelAttribute("productForm") VendorProductForm form,
                                BindingResult bindingResult,
                                @RequestParam(value = "image", required = false) MultipartFile imageFile,
                                HttpSession session,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        User vendor = ensureVendor(session);
        if (vendor == null) {
            return "redirect:/login";
        }

        Optional<Shop> shopOpt = shopService.findByVendor(vendor);
        if (shopOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("warning", "Bạn chưa có shop.");
            return "redirect:/vendor/shop/register";
        }

        Shop shop = shopOpt.get();
        Optional<Product> productOpt = productService.findByIdAndShop(productId, shop.getShopId());
        if (productOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy sản phẩm.");
            return "redirect:/vendor/products";
        }

        Category category = categoryService.getCategoryById(form.getCategoryId()).orElse(null);
        Brand brand = brandService.findById(form.getBrandId()).orElse(null);
        if (category == null) {
            bindingResult.rejectValue("categoryId", "invalid", "Danh mục không hợp lệ");
        }
        if (brand == null) {
            bindingResult.rejectValue("brandId", "invalid", "Thương hiệu không hợp lệ");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("shop", shop);
            model.addAttribute("product", productOpt.get());
            model.addAttribute("pageTitle", "Cập nhật sản phẩm");
            return "vendor/product-form";
        }

        Product product = productOpt.get();
        product.setProductName(form.getProductName().trim());
        product.setDescription(form.getDescription());
        product.setPrice(form.getPrice());
        product.setQuantity(form.getQuantity());
        product.setDiscount(form.getDiscount());
        product.setStatus(Boolean.TRUE.equals(form.getActive()));
        product.setCategory(category);
        product.setBrand(brand);

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String imageName = imageStorageService.store(imageFile, form.getProductName());
                product.setProductImage(imageName);
            } catch (IOException e) {
                bindingResult.reject("upload", "Không thể lưu hình ảnh sản phẩm");
                model.addAttribute("shop", shop);
                model.addAttribute("product", product);
                model.addAttribute("pageTitle", "Cập nhật sản phẩm");
                return "vendor/product-form";
            }
        }

        productService.save(product);
        shopService.refreshStatistics(shop);
        redirectAttributes.addFlashAttribute("success", "Cập nhật sản phẩm thành công");
        return "redirect:/vendor/products";
    }

    @PostMapping("/{id}/delete")
    public String deleteProduct(@PathVariable("id") Long productId,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        User vendor = ensureVendor(session);
        if (vendor == null) {
            return "redirect:/login";
        }

        Optional<Shop> shopOpt = shopService.findByVendor(vendor);
        if (shopOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("warning", "Bạn chưa có shop.");
            return "redirect:/vendor/shop/register";
        }

        Shop shop = shopOpt.get();
        Optional<Product> productOpt = productService.findByIdAndShop(productId, shop.getShopId());
        if (productOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy sản phẩm.");
            return "redirect:/vendor/products";
        }

        productService.deleteById(productId);
        shopService.refreshStatistics(shop);
        redirectAttributes.addFlashAttribute("success", "Xóa sản phẩm thành công");
        return "redirect:/vendor/products";
    }

    private VendorProductForm toForm(Product product) {
        VendorProductForm form = new VendorProductForm();
        form.setProductName(product.getProductName());
        form.setDescription(product.getDescription());
        form.setPrice(product.getPrice());
        form.setQuantity(product.getQuantity());
        form.setDiscount(product.getDiscount());
        form.setCategoryId(product.getCategory() != null ? product.getCategory().getCategoryId() : null);
        form.setBrandId(product.getBrand() != null ? product.getBrand().getBrandId() : null);
        form.setActive(product.getStatus() != null ? product.getStatus() : Boolean.TRUE);
        return form;
    }

    private User ensureVendor(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return null;
        }
        boolean isVendor = user.getRoles() != null &&
                user.getRoles().stream().anyMatch(role -> "ROLE_VENDOR".equals(role.getName()));
        return isVendor ? user : null;
    }
}

