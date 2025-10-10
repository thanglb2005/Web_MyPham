package vn.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.entity.Shop;

/**
 * Aggregated statistics for products grouped by shop, used in admin dashboards.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopProductStatistics {

    private Long shopId;
    private String shopName;
    private Shop.ShopStatus status;
    private String vendorName;
    private long productCount;
    private long totalQuantity;
    private double totalInventoryValue;

}
