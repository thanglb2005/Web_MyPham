package vn.service.statistics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.repository.CategoryRepository;

import java.util.*;

/**
 * Service for category statistics
 * @author OneShop Team
 */
@Service
public class CategoryStatisticsService {

    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * 1. Thống kê số lượng sản phẩm theo danh mục
     */
    public List<Object[]> getProductCountByCategory() {
        try {
            return categoryRepository.getProductCountByCategory();
        } catch (Exception e) {
            System.err.println("Error getting product count by category: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 2. Thống kê doanh thu theo danh mục
     */
    public List<Object[]> getRevenueByCategory() {
        try {
            return categoryRepository.getRevenueByCategory();
        } catch (Exception e) {
            System.err.println("Error getting revenue by category: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 3. Thống kê đánh giá trung bình theo danh mục
     */
    public List<Object[]> getRatingByCategory() {
        try {
            return categoryRepository.getRatingByCategory();
        } catch (Exception e) {
            System.err.println("Error getting rating by category: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 4. Thống kê lượt yêu thích theo danh mục
     */
    public List<Object[]> getFavoritesByCategory() {
        try {
            return categoryRepository.getFavoritesByCategory();
        } catch (Exception e) {
            System.err.println("Error getting favorites by category: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 5. Thống kê tổng hợp nhiều tiêu chí
     */
    public List<Object[]> getComprehensiveCategoryStats() {
        try {
            return categoryRepository.getComprehensiveCategoryStats();
        } catch (Exception e) {
            System.err.println("Error getting comprehensive category stats: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Thống kê doanh thu theo danh mục trong khoảng thời gian
     */
    public List<Object[]> getRevenueByCategoryInDateRange(String startDate, String endDate) {
        try {
            return categoryRepository.getRevenueByCategoryInDateRange(startDate, endDate);
        } catch (Exception e) {
            System.err.println("Error getting revenue by category in date range: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Thống kê số lượng sản phẩm theo danh mục có trạng thái active
     */
    public List<Object[]> getActiveProductCountByCategory() {
        try {
            return categoryRepository.getActiveProductCountByCategory();
        } catch (Exception e) {
            System.err.println("Error getting active product count by category: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Thống kê doanh thu theo danh mục có trạng thái active
     */
    public List<Object[]> getActiveRevenueByCategory() {
        try {
            return categoryRepository.getActiveRevenueByCategory();
        } catch (Exception e) {
            System.err.println("Error getting active revenue by category: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Tính tổng số sản phẩm
     */
    public long getTotalProducts() {
        try {
            List<Object[]> stats = getProductCountByCategory();
            return stats.stream()
                    .mapToLong(row -> (Long) row[1])
                    .sum();
        } catch (Exception e) {
            System.err.println("Error calculating total products: " + e.getMessage());
            return 0L;
        }
    }

    /**
     * Tính tổng doanh thu
     */
    public double getTotalRevenue() {
        try {
            List<Object[]> stats = getRevenueByCategory();
            return stats.stream()
                    .mapToDouble(row -> (Double) row[1])
                    .sum();
        } catch (Exception e) {
            System.err.println("Error calculating total revenue: " + e.getMessage());
            return 0.0;
        }
    }

    /**
     * Tính tổng lượt yêu thích
     */
    public long getTotalFavorites() {
        try {
            List<Object[]> stats = getFavoritesByCategory();
            return stats.stream()
                    .mapToLong(row -> (Long) row[1])
                    .sum();
        } catch (Exception e) {
            System.err.println("Error calculating total favorites: " + e.getMessage());
            return 0L;
        }
    }

    /**
     * Tính đánh giá trung bình tổng thể
     */
    public double getOverallAverageRating() {
        try {
            List<Object[]> stats = getRatingByCategory();
            if (stats.isEmpty()) return 0.0;
            
            double totalRating = 0.0;
            long totalCount = 0;
            
            for (Object[] row : stats) {
                Double rating = (Double) row[1];
                Long count = (Long) row[2];
                if (rating != null && count != null && count > 0) {
                    totalRating += rating * count;
                    totalCount += count;
                }
            }
            
            return totalCount > 0 ? totalRating / totalCount : 0.0;
        } catch (Exception e) {
            System.err.println("Error calculating overall average rating: " + e.getMessage());
            return 0.0;
        }
    }

    /**
     * Lấy danh mục có nhiều sản phẩm nhất
     */
    public Object[] getTopCategoryByProductCount() {
        try {
            List<Object[]> stats = getProductCountByCategory();
            return stats.isEmpty() ? null : stats.get(0);
        } catch (Exception e) {
            System.err.println("Error getting top category by product count: " + e.getMessage());
            return null;
        }
    }

    /**
     * Lấy danh mục có doanh thu cao nhất
     */
    public Object[] getTopCategoryByRevenue() {
        try {
            List<Object[]> stats = getRevenueByCategory();
            return stats.isEmpty() ? null : stats.get(0);
        } catch (Exception e) {
            System.err.println("Error getting top category by revenue: " + e.getMessage());
            return null;
        }
    }

    /**
     * Lấy danh mục có đánh giá cao nhất
     */
    public Object[] getTopCategoryByRating() {
        try {
            List<Object[]> stats = getRatingByCategory();
            return stats.isEmpty() ? null : stats.get(0);
        } catch (Exception e) {
            System.err.println("Error getting top category by rating: " + e.getMessage());
            return null;
        }
    }

    /**
     * Lấy danh mục có nhiều lượt yêu thích nhất
     */
    public Object[] getTopCategoryByFavorites() {
        try {
            List<Object[]> stats = getFavoritesByCategory();
            return stats.isEmpty() ? null : stats.get(0);
        } catch (Exception e) {
            System.err.println("Error getting top category by favorites: " + e.getMessage());
            return null;
        }
    }
}
