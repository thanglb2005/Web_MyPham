package vn.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class VendorProductForm {

    @NotNull(message = "Shop không được để trống")
    private Long shopId;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String productName;

    @Size(max = 1000, message = "Mô tả tối đa 1000 ký tự")
    private String description;

    @NotNull(message = "Giá không được để trống")
    @DecimalMin(value = "0.01", message = "Giá phải lớn hơn 0")
    private Double price;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 0, message = "Số lượng phải >= 0")
    private Integer quantity;

    @NotNull(message = "Giảm giá không được để trống")
    @Min(value = 0, message = "Giảm giá tối thiểu 0%")
    @Max(value = 90, message = "Giảm giá tối đa 90%")
    private Integer discount = 0;

    @NotNull(message = "Danh mục không được để trống")
    private Long categoryId;

    @NotNull(message = "Thương hiệu không được để trống")
    private Long brandId;

    private Boolean active = Boolean.TRUE;
}
