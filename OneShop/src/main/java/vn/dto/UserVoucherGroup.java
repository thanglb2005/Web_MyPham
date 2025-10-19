package vn.dto;

import vn.entity.Promotion;
import vn.entity.Shop;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * View model grouping shop-specific vouchers for the user voucher hub.
 */
public class UserVoucherGroup {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final Long shopId;
    private final String shopName;
    private final String shopSlug;
    private final String shopLogo;
    private final String location;
    private final Boolean allowCod;
    private final List<VoucherItem> vouchers = new ArrayList<>();

    public UserVoucherGroup(Shop shop) {
        this.shopId = shop.getShopId();
        this.shopName = shop.getShopName();
        this.shopSlug = shop.getShopSlug();
        this.shopLogo = shop.getShopLogo();
        String address = "";
        if (shop.getDistrict() != null && !shop.getDistrict().isBlank()) {
            address += shop.getDistrict();
        }
        if (shop.getCity() != null && !shop.getCity().isBlank()) {
            if (!address.isEmpty()) {
                address += ", ";
            }
            address += shop.getCity();
        }
        this.location = address.isEmpty() ? "Toan quoc" : address;
        this.allowCod = shop.getAllowCod();
    }

    public void addVoucher(Promotion promotion) {
        vouchers.add(VoucherItem.fromPromotion(promotion));
    }

    public Long getShopId() {
        return shopId;
    }

    public String getShopName() {
        return shopName;
    }

    public String getShopSlug() {
        return shopSlug;
    }

    public String getShopLogo() {
        return shopLogo;
    }

    public String getLocation() {
        return location;
    }

    public Boolean getAllowCod() {
        return allowCod;
    }

    public List<VoucherItem> getVouchers() {
        return vouchers;
    }

    public long getExpiringSoonCount() {
        return vouchers.stream().filter(VoucherItem::isExpiringSoon).count();
    }

    public long getActiveVoucherCount() {
        return vouchers.stream().filter(VoucherItem::isActive).count();
    }

    public String getShopUrl() {
        if (shopSlug != null && !shopSlug.isBlank()) {
            return "/shop/" + shopSlug;
        }
        return "/shop/" + shopId;
    }

    /**
     * Lightweight view for a promotion/voucher.
     */
    public static class VoucherItem {
        private final Long promotionId;
        private final String promotionName;
        private final String promotionCode;
        private final Promotion.PromotionType type;
        private final String discountLabel;
        private final String minimumOrderLabel;
        private final String maximumDiscountLabel;
        private final Integer usageLimit;
        private final Integer usedCount;
        private final Integer remainingUses;
        private final LocalDateTime startDate;
        private final LocalDateTime endDate;
        private final boolean active;
        private final boolean expiringSoon;
        private final long daysRemaining;
        private final String expiryLabel;

        private VoucherItem(Long promotionId,
                            String promotionName,
                            String promotionCode,
                            Promotion.PromotionType type,
                            String discountLabel,
                            String minimumOrderLabel,
                            String maximumDiscountLabel,
                            Integer usageLimit,
                            Integer usedCount,
                            Integer remainingUses,
                            LocalDateTime startDate,
                            LocalDateTime endDate,
                            boolean active,
                            boolean expiringSoon,
                            long daysRemaining,
                            String expiryLabel) {
            this.promotionId = promotionId;
            this.promotionName = promotionName;
            this.promotionCode = promotionCode;
            this.type = type;
            this.discountLabel = discountLabel;
            this.minimumOrderLabel = minimumOrderLabel;
            this.maximumDiscountLabel = maximumDiscountLabel;
            this.usageLimit = usageLimit;
            this.usedCount = usedCount;
            this.remainingUses = remainingUses;
            this.startDate = startDate;
            this.endDate = endDate;
            this.active = active;
            this.expiringSoon = expiringSoon;
            this.daysRemaining = daysRemaining;
            this.expiryLabel = expiryLabel;
        }

        public static VoucherItem fromPromotion(Promotion promotion) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime endDate = promotion.getEndDate();
            long daysRemaining = 0;
            boolean expiringSoon = false;
            if (endDate != null) {
                daysRemaining = Duration.between(now, endDate).toDays();
                expiringSoon = daysRemaining >= 0 && daysRemaining <= 3;
            }

            String discountLabel = buildDiscountLabel(promotion.getPromotionType(), promotion.getDiscountValue());
            String minOrder = buildCurrencyLabel("Don toi thieu", promotion.getMinimumOrderAmount());
            String maxDiscount = buildCurrencyLabel("Giam toi da", promotion.getMaximumDiscountAmount());
            Integer usageLimit = promotion.getUsageLimit();
            Integer usedCount = promotion.getUsedCount() != null ? promotion.getUsedCount() : 0;
            Integer remainingUses = usageLimit != null ? Math.max(usageLimit - usedCount, 0) : null;
            String expiry = endDate != null ? DATE_FORMATTER.format(endDate) : "Khong thoi han";

            boolean active = Boolean.TRUE.equals(promotion.getIsActive()) && (endDate == null || !endDate.isBefore(now));

            return new VoucherItem(
                    promotion.getPromotionId(),
                    promotion.getPromotionName(),
                    promotion.getPromotionCode(),
                    promotion.getPromotionType(),
                    discountLabel,
                    minOrder,
                    maxDiscount,
                    usageLimit,
                    usedCount,
                    remainingUses,
                    promotion.getStartDate(),
                    endDate,
                    active,
                    expiringSoon,
                    daysRemaining,
                    expiry
            );
        }

        private static String buildDiscountLabel(Promotion.PromotionType type, BigDecimal value) {
            if (value == null) {
                return "Uu dai";
            }
            return switch (type) {
                case PERCENTAGE -> value.stripTrailingZeros().toPlainString() + "% giam";
                case FIXED_AMOUNT -> formatCurrency(value) + " giam ngay";
                case FREE_SHIPPING -> "Freeship toi " + formatCurrency(value);
                case BUY_X_GET_Y -> "Mua X tang Y";
            };
        }

        private static String buildCurrencyLabel(String prefix, BigDecimal value) {
            if (value == null) {
                return prefix + ": Khong ro";
            }
            return prefix + ": " + formatCurrency(value);
        }

        private static String formatCurrency(BigDecimal value) {
            DecimalFormat formatter = new DecimalFormat("#,###");
            formatter.setGroupingSize(3);
            BigDecimal scaled = value.setScale(0, RoundingMode.HALF_UP);
            return formatter.format(scaled) + " VND";
        }

        public Long getPromotionId() {
            return promotionId;
        }

        public String getPromotionName() {
            return promotionName;
        }

        public String getPromotionCode() {
            return promotionCode;
        }

        public Promotion.PromotionType getType() {
            return type;
        }

        public String getDiscountLabel() {
            return discountLabel;
        }

        public String getMinimumOrderLabel() {
            return minimumOrderLabel;
        }

        public String getMaximumDiscountLabel() {
            return maximumDiscountLabel;
        }

        public Integer getUsageLimit() {
            return usageLimit;
        }

        public Integer getUsedCount() {
            return usedCount;
        }

        public Integer getRemainingUses() {
            return remainingUses;
        }

        public LocalDateTime getStartDate() {
            return startDate;
        }

        public LocalDateTime getEndDate() {
            return endDate;
        }

        public boolean isActive() {
            return active;
        }

        public boolean isExpiringSoon() {
            return expiringSoon;
        }

        public long getDaysRemaining() {
            return daysRemaining;
        }

        public String getExpiryLabel() {
            return expiryLabel;
        }
    }
}
