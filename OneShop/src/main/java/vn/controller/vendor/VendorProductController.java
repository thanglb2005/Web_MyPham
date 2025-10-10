package vn.controller.vendor;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public String listProducts(@RequestParam(value = "shopId", required = false) Long shopId,
                               HttpSession session,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        User vendor = ensureVendor(session);
        if (vendor == null) {
            return "redirect:/login";
        }

        List<Shop> shopList = shopService.findAllByVendor(vendor);
        if (shopList.isEmpty()) {
            redirectAttributes.addFlashAttribute("warning", "Bạn chưa có shop. Hãy đăng ký trước khi quản lý sản phẩm.");
            return "redirect:/vendor/shop/register";
        }

        Shop shop = resolveShopFromList(shopList, shopId);
        if (shop == null) {
            redirectAttributes.addFlashAttribute("warning", "Không tìm thấy shop cần quản lý.");
            return "redirect:/vendor/my-shops";
        }

        List<Product> products = productService.findByShopId(shop.getShopId());

        model.addAttribute("vendor", vendor);
        model.addAttribute("shop", shop);
        model.addAttribute("shopList", shopList);
        model.addAttribute("selectedShopId", shop.getShopId());
        model.addAttribute("products", products);
        model.addAttribute("shopStatus", shop.getStatus());
        model.addAttribute("pageTitle", "Sản phẩm của tôi");
        return "vendor/product-list";
    }

    @GetMapping("/new")
    public String createForm(@RequestParam(value = "shopId", required = false) Long shopId,
                             HttpSession session,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        User vendor = ensureVendor(session);
        if (vendor == null) {
            return "redirect:/login";
        }

        List<Shop> shopList = shopService.findAllByVendor(vendor);
        if (shopList.isEmpty()) {
            redirectAttributes.addFlashAttribute("warning", "Bạn chưa có shop. Hãy đăng ký trước khi quản lý sản phẩm.");
            return "redirect:/vendor/shop/register";
        }

        Shop shop = resolveShopFromList(shopList, shopId);
        if (shop == null) {
            redirectAttributes.addFlashAttribute("warning", "Không tìm thấy shop cần quản lý.");
            return "redirect:/vendor/my-shops";
        }

        if (!model.containsAttribute("productForm")) {
            VendorProductForm form = new VendorProductForm();
            form.setShopId(shop.getShopId());
            model.addAttribute("productForm", form);
        } else {
            VendorProductForm boundForm = (VendorProductForm) model.getAttribute("productForm");
            if (boundForm != null && boundForm.getShopId() == null) {
                boundForm.setShopId(shop.getShopId());
            }
        }

        model.addAttribute("vendor", vendor);
        model.addAttribute("shop", shop);
        model.addAttribute("shopList", shopList);
        model.addAttribute("selectedShopId", shop.getShopId());
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

        List<Shop> shopList = shopService.findAllByVendor(vendor);
        if (shopList.isEmpty()) {
            redirectAttributes.addFlashAttribute("warning", "Bạn chưa có shop. Hãy đăng ký trước khi quản lý sản phẩm.");
            return "redirect:/vendor/shop/register";
        }

        Shop shop = null;
        if (form.getShopId() != null) {
            shop = shopList.stream()
                    .filter(s -> s.getShopId().equals(form.getShopId()))
                    .findFirst()
                    .orElse(null);
        }

        if (shop == null && form.getShopId() != null) {
            bindingResult.rejectValue("shopId", "invalid", "Shop không hợp lệ.");
            form.setShopId(null);
        } else if (shop == null) {
            shop = shopList.get(0);
            form.setShopId(shop.getShopId());
        }

        if (shop == null && !shopList.isEmpty()) {
            shop = shopList.get(0);
        }

        if (imageFile == null || imageFile.isEmpty()) {
            bindingResult.rejectValue("productName", "image", "Vui lòng chọn hình ảnh cho sản phẩm.");
        }

        model.addAttribute("vendor", vendor);
        model.addAttribute("shopList", shopList);
        model.addAttribute("shop", shop);
        model.addAttribute("selectedShopId", shop != null ? shop.getShopId() : null);
        model.addAttribute("pageTitle", "Thêm sản phẩm mới");

        if (bindingResult.hasErrors()) {
            return "vendor/product-form";
        }

        Category category = categoryService.getCategoryById(form.getCategoryId()).orElse(null);
        Brand brand = brandService.findById(form.getBrandId()).orElse(null);

        if (category == null) {
            bindingResult.rejectValue("categoryId", "invalid", "Danh mục không hợp lệ.");
        }
        if (brand == null) {
            bindingResult.rejectValue("brandId", "invalid", "Thương hiệu không hợp lệ.");
        }

        if (bindingResult.hasErrors()) {
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
            bindingResult.reject("upload", "Không thể lưu hình ảnh sản phẩm.");
            return "vendor/product-form";
        }

        productService.save(product);
        shopService.refreshStatistics(shop);
        redirectAttributes.addFlashAttribute("success", "Thêm sản phẩm thành công.");
        return "redirect:/vendor/products?shopId=" + shop.getShopId();
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

        Optional<Product> productOpt = findVendorProduct(vendor, productId);
        if (productOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy sản phẩm.");
            return "redirect:/vendor/products";
        }

        Product product = productOpt.get();
        Shop shop = product.getShop();

        List<Shop> shopList = shopService.findAllByVendor(vendor);

        if (!model.containsAttribute("productForm")) {
            VendorProductForm form = toForm(product);
            model.addAttribute("productForm", form);
        } else {
            VendorProductForm boundForm = (VendorProductForm) model.getAttribute("productForm");
            if (boundForm != null) {
                boundForm.setShopId(shop.getShopId());
            }
        }

        model.addAttribute("vendor", vendor);
        model.addAttribute("shop", shop);
        model.addAttribute("shopList", shopList);
        model.addAttribute("selectedShopId", shop.getShopId());
        model.addAttribute("product", product);
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

        Optional<Product> productOpt = findVendorProduct(vendor, productId);
        if (productOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy sản phẩm.");
            return "redirect:/vendor/products";
        }

        Product product = productOpt.get();
        Shop shop = product.getShop();
        form.setShopId(shop.getShopId());

        List<Shop> shopList = shopService.findAllByVendor(vendor);

        Category category = categoryService.getCategoryById(form.getCategoryId()).orElse(null);
        Brand brand = brandService.findById(form.getBrandId()).orElse(null);

        if (category == null) {
            bindingResult.rejectValue("categoryId", "invalid", "Danh mục không hợp lệ.");
        }
        if (brand == null) {
            bindingResult.rejectValue("brandId", "invalid", "Thương hiệu không hợp lệ.");
        }

        model.addAttribute("vendor", vendor);
        model.addAttribute("shop", shop);
        model.addAttribute("shopList", shopList);
        model.addAttribute("selectedShopId", shop.getShopId());
        model.addAttribute("product", product);
        model.addAttribute("pageTitle", "Cập nhật sản phẩm");

        if (bindingResult.hasErrors()) {
            return "vendor/product-form";
        }

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
                bindingResult.reject("upload", "Không thể lưu hình ảnh sản phẩm.");
                return "vendor/product-form";
            }
        }

        productService.save(product);
        shopService.refreshStatistics(shop);
        redirectAttributes.addFlashAttribute("success", "Cập nhật sản phẩm thành công.");
        return "redirect:/vendor/products?shopId=" + shop.getShopId();
    }

    @PostMapping("/{id}/delete")
    public String deleteProduct(@PathVariable("id") Long productId,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        User vendor = ensureVendor(session);
        if (vendor == null) {
            return "redirect:/login";
        }

        Optional<Product> productOpt = findVendorProduct(vendor, productId);
        if (productOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy sản phẩm.");
            return "redirect:/vendor/products";
        }

        Product product = productOpt.get();
        Shop shop = product.getShop();

        productService.deleteById(productId);
        shopService.refreshStatistics(shop);
        redirectAttributes.addFlashAttribute("success", "Xóa sản phẩm thành công.");
        return "redirect:/vendor/products?shopId=" + shop.getShopId();
    }

    private VendorProductForm toForm(Product product) {
        VendorProductForm form = new VendorProductForm();
        form.setShopId(product.getShop() != null ? product.getShop().getShopId() : null);
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

    private Shop resolveShopFromList(List<Shop> shopList, Long requestedShopId) {
        if (shopList == null || shopList.isEmpty()) {
            return null;
        }
        if (requestedShopId != null) {
            return shopList.stream()
                    .filter(shop -> shop.getShopId().equals(requestedShopId))
                    .findFirst()
                    .orElse(null);
        }
        return shopList.get(0);
    }

    private Optional<Product> findVendorProduct(User vendor, Long productId) {
        return productService.findById(productId)
                .filter(product -> product.getShop() != null
                        && product.getShop().getVendor() != null
                        && product.getShop().getVendor().getUserId().equals(vendor.getUserId()));
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
