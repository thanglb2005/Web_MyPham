package vn.service.statistics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import vn.repository.ProductRepository;

@Service
public class ProductStatisticsService {

    public static record ProductStatisticsResult(
            String type,
            String title,
            List<Object[]> rows,
            long totalElements,
            int page,
            int size,
            String search
    ) {
        public ProductStatisticsResult {
            rows = rows == null ? Collections.emptyList() : List.copyOf(rows);
            page = Math.max(page, 0);
            size = Math.max(size, 1);
        }

        public int totalPages() {
            if (size <= 0) {
                return 0;
            }
            return (int) Math.ceil((double) Math.max(totalElements, 0) / size);
        }

        public static ProductStatisticsResult empty(String type, int page, int size, String search, String title) {
            return new ProductStatisticsResult(type, title, Collections.emptyList(), 0, page, size, search);
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductStatisticsService.class);

    private final ProductRepository productRepository;

    public ProductStatisticsService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ProductStatisticsResult fetchList(String type, int page, int size, String search) {
        int safeSize = Math.max(size, 1);
        int safePage = Math.max(page, 0);
        String normalized = normalizeSearch(search);
        return fallbackList(type, safePage, safeSize, normalized, search);
    }

    public ProductStatisticsResult fetchList(String type, int page, int size, String search, String sortBy) {
        int safeSize = Math.max(size, 1);
        int safePage = Math.max(page, 0);
        String normalized = normalizeSearch(search);
        return fallbackList(type, safePage, safeSize, normalized, search, sortBy);
    }

    public List<Object[]> fetchLowStockPreview(int limit) {
        int safeLimit = Math.max(limit, 1);
        try {
            return productRepository.getLowStockProducts().stream()
                    .limit(safeLimit)
                    .map(r -> new Object[]{r[0], r[3]}) // [name, qty]
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            LOGGER.warn("Low-stock preview failed: {}", ex.getMessage());
            return Collections.emptyList();
        }
    }

    public List<Object[]> fetchExpiringPreview(int limit) {
        int safeLimit = Math.max(limit, 1);
        try {
            return productRepository.getProductsExpiringSoon().stream()
                    .limit(safeLimit)
                    .map(r -> new Object[]{r[0], r[3]}) // [name, expiry]
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            LOGGER.warn("Expiring preview failed: {}", ex.getMessage());
            return Collections.emptyList();
        }
    }

    public List<Object[]> fetchDiscountedPreview(int limit) {
        int safeLimit = Math.max(limit, 1);
        try {
            return productRepository.getDiscountedProducts().stream()
                    .limit(safeLimit)
                    .map(r -> new Object[]{r[0], r[1], r[2], r[3]})
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            LOGGER.warn("Discounted preview failed: {}", ex.getMessage());
            return Collections.emptyList();
        }
    }

    public List<Object[]> fetchFavoritesPreview(int limit) {
        int safeLimit = Math.max(limit, 1);
        try {
            return productRepository.getFavoriteProductsStatistics().stream()
                    .limit(safeLimit)
                    .map(r -> {
                        Number fav = r[2] instanceof Number ? (Number) r[2] : 0;
                        Number total = r[3] instanceof Number ? (Number) r[3] : 0;
                        int rate = (total != null && total.doubleValue() > 0)
                                ? (int) Math.round((fav.doubleValue() / total.doubleValue()) * 100)
                                : 0;
                        return new Object[]{r[0], r[1], fav, total, rate};
                    })
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            LOGGER.warn("Favorites preview failed: {}", ex.getMessage());
            return Collections.emptyList();
        }
    }

    public Map<String, Number> buildKpis() {
        Map<String, Number> map = new HashMap<>();
        try { map.put("lowStock", productRepository.getLowStockProducts().size()); } catch (Exception e) { map.put("lowStock", 0); }
        try { map.put("expiringSoon", productRepository.getProductsExpiringSoon().size()); } catch (Exception e) { map.put("expiringSoon", 0); }
        try { map.put("discounted", productRepository.getDiscountedProducts().size()); } catch (Exception e) { map.put("discounted", 0); }
        try {
            int fav = 0; for (Object[] r : productRepository.getFavoriteProductsStatistics()) { if (r[2] instanceof Number n) fav += n.intValue(); }
            map.put("favorites", fav);
        } catch (Exception e) { map.put("favorites", 0); }
        try { map.put("bestSellers", productRepository.getTopSellingProducts(10).size()); } catch (Exception e) { map.put("bestSellers", 0); }
        try { map.put("newProducts", productRepository.countNewProductsLast30Days()); } catch (Exception e) { map.put("newProducts", 0); }
        try { map.put("slowMoving", productRepository.getSlowMovingProducts30Days().size()); } catch (Exception e) { map.put("slowMoving", 0); }
        try {
            int up = 0, down = 0;
            for (Object[] r : productRepository.getProductMonthlyTrend()) {
                Number qtyThis = r[1] instanceof Number ? (Number) r[1] : 0;
                Number qtyPrev = r[2] instanceof Number ? (Number) r[2] : 0;
                double ch = (qtyPrev != null && qtyPrev.doubleValue() > 0)
                        ? ((qtyThis.doubleValue() - qtyPrev.doubleValue()) / qtyPrev.doubleValue())
                        : (qtyThis.doubleValue() > 0 ? 1.0 : 0.0);
                if (ch > 0) up++; else if (ch < 0) down++;
            }
            map.put("trendingUp", up); map.put("trendingDown", down);
        } catch (Exception e) { map.put("trendingUp", 0); map.put("trendingDown", 0); }
        return map;
    }

    private String normalizeSearch(String search) {
        if (search == null) return null;
        String s = search.trim();
        return s.isEmpty() ? null : s.toLowerCase();
    }

    private static String titleFor(String type) {
        return switch (type) {
            case "lowstock" -> "San pham sap het hang";
            case "expiring" -> "San pham sap het han";
            case "discounted" -> "San pham dang khuyen mai";
            case "favorites" -> "Duoc khach hang ua chuong";
            case "bestseller" -> "San pham ban chay";
            case "newest" -> "San pham moi nhap";
            case "slowmoving" -> "San pham ban cham 30 ngay";
            case "trending_up" -> "Dang tang truong";
            case "trending_down" -> "Dang giam suc";
            default -> "Thong ke san pham";
        };
    }

    private ProductStatisticsResult fallbackList(String type, int page, int size, String normalizedSearch, String rawSearch) {
        return fallbackList(type, page, size, normalizedSearch, rawSearch, "quantity");
    }

    private ProductStatisticsResult fallbackList(String type, int page, int size, String normalizedSearch, String rawSearch, String sortBy) {
        List<Object[]> rows = new ArrayList<>();
        String title = titleFor(type);
        try {
            switch (type) {
                case "lowstock": {
                    rows = productRepository.getLowStockProducts();
                    if (normalizedSearch != null && !normalizedSearch.isEmpty()) {
                        String s = normalizedSearch;
                        rows = rows.stream().filter(r -> {
                            String name = String.valueOf(r[0] != null ? r[0] : "").toLowerCase();
                            String cat = String.valueOf(r[1] != null ? r[1] : "").toLowerCase();
                            String brand = String.valueOf(r[2] != null ? r[2] : "").toLowerCase();
                            return name.contains(s) || cat.contains(s) || brand.contains(s);
                        }).collect(Collectors.toList());
                    }
                    break;
                }
                case "expiring": {
                    rows = productRepository.getProductsExpiringSoon();
                    if (normalizedSearch != null && !normalizedSearch.isEmpty()) {
                        String s = normalizedSearch;
                        rows = rows.stream().filter(r -> {
                            String name = String.valueOf(r[0] != null ? r[0] : "").toLowerCase();
                            String cat = String.valueOf(r[1] != null ? r[1] : "").toLowerCase();
                            String brand = String.valueOf(r[2] != null ? r[2] : "").toLowerCase();
                            return name.contains(s) || cat.contains(s) || brand.contains(s);
                        }).collect(Collectors.toList());
                    }
                    break;
                }
                case "discounted": {
                    rows = productRepository.getDiscountedProducts();
                    if (normalizedSearch != null && !normalizedSearch.isEmpty()) {
                        String s = normalizedSearch;
                        rows = rows.stream().filter(r -> {
                            String name = String.valueOf(r[0] != null ? r[0] : "").toLowerCase();
                            String cat = String.valueOf(r[1] != null ? r[1] : "").toLowerCase();
                            String brand = String.valueOf(r[2] != null ? r[2] : "").toLowerCase();
                            return name.contains(s) || cat.contains(s) || brand.contains(s);
                        }).collect(Collectors.toList());
                    }
                    break;
                }
                case "favorites": {
                    rows = productRepository.getFavoriteProductsStatistics();
                    if (normalizedSearch != null && !normalizedSearch.isEmpty()) {
                        String s = normalizedSearch;
                        rows = rows.stream().filter(r -> {
                            String cat = String.valueOf(r[0] != null ? r[0] : "").toLowerCase();
                            String brand = String.valueOf(r[1] != null ? r[1] : "").toLowerCase();
                            return cat.contains(s) || brand.contains(s);
                        }).collect(Collectors.toList());
                    }
                    rows = rows.stream().map(r -> {
                        Number fav = r[2] instanceof Number ? (Number) r[2] : 0;
                        Number total = r[3] instanceof Number ? (Number) r[3] : 0;
                        int rate = (total != null && total.doubleValue() > 0)
                                ? (int) Math.round((fav.doubleValue() / total.doubleValue()) * 100)
                                : 0;
                        return new Object[]{r[0], r[1], fav, total, rate};
                    }).collect(Collectors.toList());
                    break;
                }
                case "bestseller": {
                    List<Object[]> tmp = productRepository.getTopSellingProducts(1000);
                    if (tmp == null) tmp = Collections.emptyList();
                    
                    // Handle sorting: quantity (default) or revenue
                    if ("revenue".equals(sortBy)) {
                        // Sort by revenue (r[3] - total_revenue)
                        tmp = tmp.stream()
                            .sorted((a, b) -> {
                                Number revA = a[3] instanceof Number ? (Number) a[3] : 0;
                                Number revB = b[3] instanceof Number ? (Number) b[3] : 0;
                                return Double.compare(revB.doubleValue(), revA.doubleValue());
                            })
                            .collect(Collectors.toList());
                        rows = tmp.stream().map(r -> new Object[]{r[1], r[2], r[3]}).collect(Collectors.toList());
                    } else {
                        // Default: sort by quantity (r[2] - total_quantity_sold) 
                        tmp = tmp.stream()
                            .sorted((a, b) -> {
                                Number qtyA = a[2] instanceof Number ? (Number) a[2] : 0;
                                Number qtyB = b[2] instanceof Number ? (Number) b[2] : 0;
                                return Double.compare(qtyB.doubleValue(), qtyA.doubleValue());
                            })
                            .collect(Collectors.toList());
                        rows = tmp.stream().map(r -> new Object[]{r[1], r[2], r[3]}).collect(Collectors.toList());
                    }
                    
                    if (normalizedSearch != null && !normalizedSearch.isEmpty()) {
                        String s = normalizedSearch;
                        rows = rows.stream().filter(r -> String.valueOf(r[0]).toLowerCase().contains(s)).collect(Collectors.toList());
                    }
                    break;
                }
                case "newest": {
                    rows = productRepository.getNewestActiveProducts();
                    if (normalizedSearch != null && !normalizedSearch.isEmpty()) {
                        String s = normalizedSearch;
                        rows = rows.stream().filter(r -> {
                            String name = String.valueOf(r[0] != null ? r[0] : "").toLowerCase();
                            String cat = String.valueOf(r[1] != null ? r[1] : "").toLowerCase();
                            String brand = String.valueOf(r[2] != null ? r[2] : "").toLowerCase();
                            return name.contains(s) || cat.contains(s) || brand.contains(s);
                        }).collect(Collectors.toList());
                    }
                    break;
                }
                case "slowmoving": {
                    rows = productRepository.getSlowMovingProducts30Days();
                    if (normalizedSearch != null && !normalizedSearch.isEmpty()) {
                        String s = normalizedSearch;
                        rows = rows.stream().filter(r -> String.valueOf(r[0] != null ? r[0] : "").toLowerCase().contains(s)).collect(Collectors.toList());
                    }
                    break;
                }
                case "trending_up":
                case "trending_down": {
                    boolean up = "trending_up".equals(type);
                    List<Object[]> trend = productRepository.getProductMonthlyTrend();
                    if (trend == null) trend = Collections.emptyList();
                    List<Object[]> mapped = new ArrayList<>();
                    for (Object[] r : trend) {
                        Number qtyThis = (r[1] instanceof Number) ? (Number) r[1] : 0;
                        Number qtyPrev = (r[2] instanceof Number) ? (Number) r[2] : 0;
                        double change = (qtyPrev != null && qtyPrev.doubleValue() > 0)
                                ? ((qtyThis.doubleValue() - qtyPrev.doubleValue()) / qtyPrev.doubleValue()) * 100.0
                                : (qtyThis.doubleValue() > 0 ? 100.0 : 0.0);
                        mapped.add(new Object[]{r[0], qtyThis, qtyPrev, Math.round(change)});
                    }
                    rows = mapped.stream().filter(r -> {
                        double ch = ((Number) r[3]).doubleValue();
                        return up ? ch > 0 : ch < 0;
                    }).collect(Collectors.toList());
                    if (normalizedSearch != null && !normalizedSearch.isEmpty()) {
                        String s = normalizedSearch;
                        rows = rows.stream().filter(r -> String.valueOf(r[0]).toLowerCase().contains(s)).collect(Collectors.toList());
                    }
                    break;
                }
                default:
                    rows = Collections.emptyList();
            }
        } catch (Exception ex) {
            LOGGER.warn("Fallback list failed for type '{}' : {}", type, ex.getMessage());
            rows = Collections.emptyList();
        }

        int total = rows != null ? rows.size() : 0;
        int start = Math.max(page, 0) * Math.max(size, 1);
        if (start >= total) start = 0;
        int end = Math.min(start + Math.max(size, 1), total);
        List<Object[]> pageData = (start <= end && rows != null) ? rows.subList(start, end) : Collections.emptyList();
        return new ProductStatisticsResult(type, title, pageData, total, Math.max(page, 0), Math.max(size, 1), rawSearch);
    }
}
