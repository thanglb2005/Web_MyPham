package vn.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class VendorShopForm {

    private Long shopId;

    @NotBlank(message = "Tên shop không được để trống")
    @Size(max = 255, message = "Tên shop tối đa 255 ký tự")
    private String shopName;

    @Size(max = 2000, message = "Mô tả tối đa 2000 ký tự")
    private String shopDescription;

    @Pattern(regexp = "^$|^(0)(3|5|7|8|9)[0-9]{8}$", message = "Số điện thoại phải là 10 số và bắt đầu bằng 03, 05, 07, 08 hoặc 09")
    private String phoneNumber;

    @Size(max = 500, message = "Địa chỉ tối đa 500 ký tự")
    private String address;

    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String district;

    @Size(max = 100)
    private String ward;

    private Boolean allowCod = Boolean.TRUE;

    @NotNull(message = "Thời gian chuẩn bị không được để trống")
    @Min(value = 1, message = "Thời gian chuẩn bị tối thiểu 1 ngày")
    @Max(value = 14, message = "Thời gian chuẩn bị tối đa 14 ngày")
    private Integer preparationDays = 2;
}

