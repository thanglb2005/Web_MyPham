package vn.service.statistics;

import org.springframework.stereotype.Service;
import vn.entity.Order;
import vn.entity.OrderDetail;
import vn.entity.Product;
import vn.entity.Refund;
import vn.entity.User;
import vn.repository.OrderRepository;
import vn.repository.OrderDetailRepository;
import vn.repository.RefundRepository;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Service for revenue statistics
 */
@Service
public class RevenueStatisticsService {
    
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final RefundRepository refundRepository;
    private final NumberFormat currencyFormat;

    public RevenueStatisticsService(OrderRepository orderRepository, 
                                   OrderDetailRepository orderDetailRepository,
                                   RefundRepository refundRepository) {
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.refundRepository = refundRepository;
        this.currencyFormat = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
        ((DecimalFormat) this.currencyFormat).applyPattern("#,###");
    }

    /**
     * Check if order should be counted for gross revenue.
     * Gross revenue = orders that have been DELIVERED (have deliveredDate).
     * This is independent of current status (even if later returned/refunded).
     */
    private boolean isOrderDelivered(Order order) {
        // Order must have been delivered (has deliveredDate)
        // This means it was successfully delivered at some point, regardless of current status
        return order != null && order.getDeliveredDate() != null;
    }

    /**
     * Return the effective order amount for gross revenue reporting.
     * Gross revenue = sum of finalAmount for orders that have been DELIVERED (have deliveredDate).
     * This is independent of current status - even if order is later RETURNED, 
     * it still counts towards gross revenue if it was delivered.
     * 
     * Prefer finalAmount when available and positive; otherwise fall back to totalAmount.
     */
    private double effectiveAmount(Order order) {
        // Only count orders that have been delivered (have deliveredDate)
        // This ensures gross revenue is based on delivery, not current status
        if (!isOrderDelivered(order)) {
            return 0.0;
        }
        
        try {
            Double finalAmt = order.getFinalAmount();
            if (finalAmt != null && finalAmt > 0) {
                return finalAmt;
            }
        } catch (Exception ignored) {}
        try {
            Double total = order.getTotalAmount();
            return total != null ? total : 0.0;
        } catch (Exception ignored) {}
        return 0.0;
    }

    /**
     * Calculate total refund amount for completed refunds within a date range
     * @param startDate Start date (inclusive), null means no start limit
     * @param endDate End date (inclusive), null means no end limit
     * @param shopId Shop ID to filter by, null means all shops
     * @return Total refund amount
     */
    private double calculateRefundAmount(LocalDateTime startDate, LocalDateTime endDate, Long shopId) {
        try {
            List<Refund> allRefunds = refundRepository.findAll();
            double totalRefund = 0.0;
            
            for (Refund refund : allRefunds) {
                // Only count completed refunds
                if (refund.getRefundStatus() != Refund.RefundStatus.COMPLETED) {
                    continue;
                }
                
                // Filter by shop if specified
                if (shopId != null && refund.getOrder() != null && refund.getOrder().getShop() != null) {
                    if (!refund.getOrder().getShop().getShopId().equals(shopId)) {
                        continue;
                    }
                }
                
                // Filter by date range (use order date, not refund completion date)
                if (refund.getOrder() != null && refund.getOrder().getOrderDate() != null) {
                    LocalDateTime orderDate = refund.getOrder().getOrderDate();
                    
                    if (startDate != null && orderDate.isBefore(startDate)) {
                        continue;
                    }
                    if (endDate != null && orderDate.isAfter(endDate)) {
                        continue;
                    }
                }
                
                if (refund.getRefundAmount() != null && refund.getRefundAmount() > 0) {
                    totalRefund += refund.getRefundAmount();
                }
            }
            
            return totalRefund;
        } catch (Exception e) {
            System.err.println("Lỗi khi tính tổng refund: " + e.getMessage());
            e.printStackTrace();
            return 0.0;
        }
    }

    /**
     * Calculate refund amount for today
     */
    private double calculateTodayRefundAmount(Long shopId) {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        
        LocalDateTime startOfDay = LocalDateTime.of(
            today.get(Calendar.YEAR),
            today.get(Calendar.MONTH) + 1,
            today.get(Calendar.DAY_OF_MONTH),
            0, 0, 0
        );
        
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);
        
        return calculateRefundAmount(startOfDay, endOfDay, shopId);
    }

    /**
     * Calculate refund amount for a specific month
     */
    private double calculateMonthRefundAmount(int month, int year, Long shopId) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        
        LocalDateTime startOfMonth = LocalDateTime.of(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            1, 0, 0, 0
        );
        
        cal.add(Calendar.MONTH, 1);
        LocalDateTime endOfMonth = LocalDateTime.of(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            1, 0, 0, 0
        ).minusSeconds(1);
        
        return calculateRefundAmount(startOfMonth, endOfMonth, shopId);
    }

    /**
     * Count delivered orders (orders with deliveredDate) in a specific month
     */
    private int countDeliveredOrdersInMonth(int month, int year, Long shopId) {
        try {
            List<Order> allOrders = orderRepository.findAll();
            int count = 0;
            
            for (Order order : allOrders) {
                if (order.getOrderDate() == null || order.getDeliveredDate() == null) continue;
                
                // Filter by shop if specified
                if (shopId != null) {
                    if (order.getShop() == null || !order.getShop().getShopId().equals(shopId)) {
                        continue;
                    }
                }
                
                Calendar orderDate = Calendar.getInstance();
                orderDate.setTime(java.sql.Timestamp.valueOf(order.getOrderDate()));
                int orderMonth = orderDate.get(Calendar.MONTH) + 1;
                int orderYear = orderDate.get(Calendar.YEAR);
                
                if (orderMonth == month && orderYear == year) {
                    count++;
                }
            }
            
            return count;
        } catch (Exception e) {
            System.err.println("Lỗi khi đếm đơn hàng đã giao: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Count delivered orders for today
     */
    private int countDeliveredOrdersToday(Long shopId) {
        try {
            List<Order> allOrders = orderRepository.findAll();
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);
            
            int count = 0;
            
            for (Order order : allOrders) {
                if (order.getOrderDate() == null || order.getDeliveredDate() == null) continue;
                
                // Filter by shop if specified
                if (shopId != null) {
                    if (order.getShop() == null || !order.getShop().getShopId().equals(shopId)) {
                        continue;
                    }
                }
                
                Calendar orderDate = Calendar.getInstance();
                orderDate.setTime(java.sql.Timestamp.valueOf(order.getOrderDate()));
                orderDate.set(Calendar.HOUR_OF_DAY, 0);
                orderDate.set(Calendar.MINUTE, 0);
                orderDate.set(Calendar.SECOND, 0);
                orderDate.set(Calendar.MILLISECOND, 0);
                
                if (orderDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    orderDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
                    count++;
                }
            }
            
            return count;
        } catch (Exception e) {
            System.err.println("Lỗi khi đếm đơn hàng hôm nay: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Get today's revenue statistics - Returns both gross and net revenue
     * @return Map containing orders count, gross revenue, net revenue, and refund amount
     */
    public Map<String, Object> getTodayStatistics() {
        return getTodayStatisticsByShop(null);
    }

    /**
     * Get current month's revenue statistics - Returns both gross and net revenue
     * @return Map containing gross revenue, net revenue, refund amount, and growth rate
     */
    public Map<String, Object> getCurrentMonthStatistics() {
        // Lấy tháng và năm hiện tại
        Calendar currentDate = Calendar.getInstance();
        int currentMonth = currentDate.get(Calendar.MONTH) + 1; // Chuyển sang 1-based (1 = January)
        int currentYear = currentDate.get(Calendar.YEAR);
        
        return getMonthStatistics(currentMonth, currentYear);
    }
    
    /**
     * Get current month statistics by shop - Returns both gross and net revenue
     */
    public Map<String, Object> getCurrentMonthStatisticsByShop(Long shopId) {
        Calendar currentDate = Calendar.getInstance();
        int currentMonth = currentDate.get(Calendar.MONTH) + 1;
        int currentYear = currentDate.get(Calendar.YEAR);
        
        return getMonthStatisticsByShop(shopId, currentMonth, currentYear);
    }
    
    /**
     * Get month statistics for a specific shop - Returns both gross and net revenue
     */
    public Map<String, Object> getMonthStatisticsByShop(Long shopId, int month, int year) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<Order> allOrders = orderRepository.findAll();
            double currentMonthGrossRevenue = 0.0;
            double previousMonthGrossRevenue = 0.0;
            
            for (Order order : allOrders) {
                if (order.getOrderDate() == null) continue;
                
                // Filter by shop if specified
                if (shopId != null) {
                    if (order.getShop() == null || !order.getShop().getShopId().equals(shopId)) {
                        continue;
                    }
                }
                
                Calendar orderDate = Calendar.getInstance();
                orderDate.setTime(java.sql.Timestamp.valueOf(order.getOrderDate()));
                int orderMonth = orderDate.get(Calendar.MONTH) + 1;
                int orderYear = orderDate.get(Calendar.YEAR);
                
                double amount = effectiveAmount(order);
                
                if (orderMonth == month && orderYear == year) {
                    currentMonthGrossRevenue += amount;
                }
                
                int localPrevMonth = month - 1;
                int localPrevYear = year;
                if (localPrevMonth == 0) {
                    localPrevMonth = 12;
                    localPrevYear = year - 1;
                }
                if (orderMonth == localPrevMonth && orderYear == localPrevYear) {
                    previousMonthGrossRevenue += amount;
                }
            }
            
            // Calculate refund amounts
            double currentMonthRefund = calculateMonthRefundAmount(month, year, shopId);
            // Net revenue = Gross - Refund, but cannot be negative
            double currentMonthNetRevenue = Math.max(0.0, currentMonthGrossRevenue - currentMonthRefund);
            
            // Calculate AOV (Average Order Value) = Net Revenue / Number of delivered orders
            int deliveredOrderCount = countDeliveredOrdersInMonth(month, year, shopId);
            double aov = deliveredOrderCount > 0 ? currentMonthNetRevenue / deliveredOrderCount : 0.0;
            
            int prevMonth = month - 1;
            int prevYear = year;
            if (prevMonth == 0) {
                prevMonth = 12;
                prevYear = year - 1;
            }
            double previousMonthRefund = calculateMonthRefundAmount(prevMonth, prevYear, shopId);
            double previousMonthNetRevenue = Math.max(0.0, previousMonthGrossRevenue - previousMonthRefund);
            
            // Tính tỷ lệ tăng trưởng (dựa trên net revenue)
            String growthRateDisplay;
            boolean isPositiveGrowth = true;
            
            if (previousMonthNetRevenue > 0) {
                double growthRate = ((currentMonthNetRevenue - previousMonthNetRevenue) / previousMonthNetRevenue) * 100;
                growthRateDisplay = formatNumber(growthRate) + "%";
                isPositiveGrowth = growthRate >= 0;
            } else if (currentMonthNetRevenue > 0) {
                growthRateDisplay = "Mới";
                isPositiveGrowth = true;
            } else {
                growthRateDisplay = "0%";
                isPositiveGrowth = false;
            }
            
            result.put("grossRevenue", currentMonthGrossRevenue);
            result.put("formattedGrossRevenue", formatCurrency(currentMonthGrossRevenue));
            result.put("refundAmount", currentMonthRefund);
            result.put("formattedRefundAmount", formatCurrency(currentMonthRefund));
            result.put("netRevenue", currentMonthNetRevenue);
            result.put("formattedNetRevenue", formatCurrency(currentMonthNetRevenue));
            result.put("deliveredOrderCount", deliveredOrderCount);
            result.put("aov", aov);
            result.put("formattedAov", formatCurrency(aov));
            result.put("growthRate", growthRateDisplay);
            result.put("isPositiveGrowth", isPositiveGrowth);
            
            // Backward compatibility
            result.put("revenue", currentMonthNetRevenue);
            result.put("formattedRevenue", formatCurrency(currentMonthNetRevenue));
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy thống kê tháng: " + e.getMessage());
            e.printStackTrace();
            result.put("grossRevenue", 0.0);
            result.put("formattedGrossRevenue", formatCurrency(0.0));
            result.put("refundAmount", 0.0);
            result.put("formattedRefundAmount", formatCurrency(0.0));
            result.put("netRevenue", 0.0);
            result.put("formattedNetRevenue", formatCurrency(0.0));
            result.put("revenue", 0.0);
            result.put("formattedRevenue", formatCurrency(0.0));
            result.put("growthRate", "0.0");
            result.put("isPositiveGrowth", false);
        }
        
        return result;
    }
    
    /**
     * Get today statistics by shop - Returns both gross and net revenue
     * @param shopId Shop ID, null for all shops
     * @return Map containing orders count, gross revenue, net revenue, and refund amount
     */
    public Map<String, Object> getTodayStatisticsByShop(Long shopId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<Order> allOrders = orderRepository.findAll();
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);
            
            int orderCount = 0;
            double grossRevenue = 0.0;
            
            for (Order order : allOrders) {
                if (order.getOrderDate() == null) continue;
                
                // Filter by shop if specified
                if (shopId != null) {
                    if (order.getShop() == null || !order.getShop().getShopId().equals(shopId)) {
                        continue;
                    }
                }
                
                Calendar orderDate = Calendar.getInstance();
                orderDate.setTime(java.sql.Timestamp.valueOf(order.getOrderDate()));
                orderDate.set(Calendar.HOUR_OF_DAY, 0);
                orderDate.set(Calendar.MINUTE, 0);
                orderDate.set(Calendar.SECOND, 0);
                orderDate.set(Calendar.MILLISECOND, 0);
                
                if (orderDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    orderDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
                    double amount = effectiveAmount(order);
                    if (amount > 0) {
                        orderCount++;
                        grossRevenue += amount;
                    }
                }
            }
            
            // Calculate refund amount for today
            double refundAmount = calculateTodayRefundAmount(shopId);
            // Net revenue = Gross - Refund, but cannot be negative
            double netRevenue = Math.max(0.0, grossRevenue - refundAmount);
            
            // Calculate AOV (Average Order Value) = Net Revenue / Number of delivered orders
            int deliveredOrderCount = countDeliveredOrdersToday(shopId);
            double aov = deliveredOrderCount > 0 ? netRevenue / deliveredOrderCount : 0.0;
            
            result.put("orderCount", orderCount);
            result.put("formattedOrderCount", String.valueOf(orderCount));
            result.put("deliveredOrderCount", deliveredOrderCount);
            result.put("grossRevenue", grossRevenue);
            result.put("formattedGrossRevenue", formatCurrency(grossRevenue));
            result.put("refundAmount", refundAmount);
            result.put("formattedRefundAmount", formatCurrency(refundAmount));
            result.put("netRevenue", netRevenue);
            result.put("formattedNetRevenue", formatCurrency(netRevenue));
            result.put("aov", aov);
            result.put("formattedAov", formatCurrency(aov));
            
            // Backward compatibility
            result.put("revenue", netRevenue);
            result.put("formattedRevenue", formatCurrency(netRevenue));
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy thống kê hôm nay: " + e.getMessage());
            e.printStackTrace();
            result.put("orderCount", 0);
            result.put("formattedOrderCount", "0");
            result.put("grossRevenue", 0.0);
            result.put("formattedGrossRevenue", formatCurrency(0.0));
            result.put("refundAmount", 0.0);
            result.put("formattedRefundAmount", formatCurrency(0.0));
            result.put("netRevenue", 0.0);
            result.put("formattedNetRevenue", formatCurrency(0.0));
            result.put("revenue", 0.0);
            result.put("formattedRevenue", formatCurrency(0.0));
        }
        
        return result;
    }
    
    /**
     * Get month statistics for a specific month and year - Returns both gross and net revenue
     * @param month Month (1-12)
     * @param year Year
     * @return Map containing gross revenue, net revenue, refund amount, and growth rate
     */
    public Map<String, Object> getMonthStatistics(int month, int year) {
        return getMonthStatisticsByShop(null, month, year);
    }

    /**
     * Get quarter statistics for a specific quarter and year
     * @param quarter Quarter (1-4)
     * @param year Year
     * @return Map containing revenue and growth rate
     */
    public double getQuarterStatistics(int quarter, int year) {
        try {
            // Không còn sử dụng dữ liệu mẫu, luôn lấy dữ liệu thật
            
            // Truy vấn trực tiếp từ danh sách đơn hàng
            List<Order> allOrders = orderRepository.findAll();
            System.out.println("Tổng số đơn hàng cho quý " + quarter + "/" + year + ": " + allOrders.size());
            
            double quarterRevenue = 0.0;
            
            // Xác định các tháng trong quý
            int startMonth = (quarter - 1) * 3 + 1;
            int endMonth = quarter * 3;
            
            for (Order order : allOrders) {
                if (order.getOrderDate() != null) {
                    Calendar orderDate = Calendar.getInstance();
                    orderDate.setTime(java.sql.Timestamp.valueOf(order.getOrderDate()));
                    int orderMonth = orderDate.get(Calendar.MONTH) + 1; // Chuyển sang 1-based (1-12)
                    int orderYear = orderDate.get(Calendar.YEAR);
                    
                    if (orderYear == year && orderMonth >= startMonth && orderMonth <= endMonth) {
                        quarterRevenue += effectiveAmount(order);
                        System.out.println("[Đơn hàng - Quý " + quarter + "/" + year + "] ID: " + order.getOrderId() + 
                                          ", Tháng: " + orderMonth + "/" + orderYear + 
                                          ", Giá trị: " + order.getTotalAmount());
                    }
                }
            }
            
            System.out.println("Tổng doanh thu quý " + quarter + "/" + year + ": " + formatCurrency(quarterRevenue));
            return quarterRevenue;
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy thống kê quý: " + e.getMessage());
            e.printStackTrace();
            return 0.0;
        }
    }

    /**
     * Get statistics for selected period - Returns both gross and net revenue
     * @param type Period type: 'month', 'quarter', 'year'
     * @param year Selected year
     * @param month Selected month (for monthly view)
     * @param quarter Selected quarter (for quarterly view)
     * @return Map containing period name, gross revenue, net revenue, and refund amount
     */
    public Map<String, Object> getSelectedPeriodStatistics(String type, int year, int month, int quarter) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String periodName = "";
            double grossRevenue = 0.0;
            double refundAmount = 0.0;
            List<Order> allOrders = orderRepository.findAll();
            
            // Log thông tin đầu vào để debug
            System.out.println("============= THÔNG KÊ DOANH THU ==============");
            System.out.println("Loại: " + type + ", Năm: " + year + ", Tháng: " + month + ", Quý: " + quarter);
            System.out.println("Tổng số đơn hàng trong CSDL: " + allOrders.size());
            
            LocalDateTime startDate = null;
            LocalDateTime endDate = null;
            
            switch (type) {
                case "month":
                    // Monthly statistics
                    periodName = "Tháng " + month + "/" + year;
                    
                    Calendar cal = Calendar.getInstance();
                    cal.set(year, month - 1, 1, 0, 0, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    startDate = LocalDateTime.of(
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH) + 1,
                        1, 0, 0, 0
                    );
                    cal.add(Calendar.MONTH, 1);
                    endDate = LocalDateTime.of(
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH) + 1,
                        1, 0, 0, 0
                    ).minusSeconds(1);
                    
                    for (Order order : allOrders) {
                        if (order.getOrderDate() != null) {
                            Calendar orderDate = Calendar.getInstance();
                            orderDate.setTime(java.sql.Timestamp.valueOf(order.getOrderDate()));
                            int orderMonth = orderDate.get(Calendar.MONTH) + 1;
                            int orderYear = orderDate.get(Calendar.YEAR);
                            
                            if (orderYear == year && orderMonth == month) {
                                grossRevenue += effectiveAmount(order);
                            }
                        }
                    }
                    
                    refundAmount = calculateRefundAmount(startDate, endDate, null);
                    break;
                    
                case "quarter":
                    // Quarterly statistics
                    periodName = "Quý " + quarter + "/" + year;
                    int startMonth = (quarter - 1) * 3 + 1;
                    int endMonth = quarter * 3;
                    
                    Calendar quarterCal = Calendar.getInstance();
                    quarterCal.set(year, startMonth - 1, 1, 0, 0, 0);
                    quarterCal.set(Calendar.MILLISECOND, 0);
                    startDate = LocalDateTime.of(
                        quarterCal.get(Calendar.YEAR),
                        quarterCal.get(Calendar.MONTH) + 1,
                        1, 0, 0, 0
                    );
                    quarterCal.set(year, endMonth - 1, 1, 0, 0, 0);
                    quarterCal.add(Calendar.MONTH, 1);
                    endDate = LocalDateTime.of(
                        quarterCal.get(Calendar.YEAR),
                        quarterCal.get(Calendar.MONTH) + 1,
                        1, 0, 0, 0
                    ).minusSeconds(1);
                    
                    for (Order order : allOrders) {
                        if (order.getOrderDate() != null) {
                            Calendar orderCal = Calendar.getInstance();
                            orderCal.setTime(java.sql.Timestamp.valueOf(order.getOrderDate()));
                            int orderMonth = orderCal.get(Calendar.MONTH) + 1;
                            int orderYear = orderCal.get(Calendar.YEAR);
                            
                            if (orderYear == year && orderMonth >= startMonth && orderMonth <= endMonth) {
                                grossRevenue += effectiveAmount(order);
                            }
                        }
                    }
                    
                    refundAmount = calculateRefundAmount(startDate, endDate, null);
                    break;
                    
                case "year":
                    // Yearly statistics
                    periodName = "Năm " + year;
                    
                    Calendar yearCal = Calendar.getInstance();
                    yearCal.set(year, 0, 1, 0, 0, 0);
                    yearCal.set(Calendar.MILLISECOND, 0);
                    startDate = LocalDateTime.of(year, 1, 1, 0, 0, 0);
                    endDate = LocalDateTime.of(year + 1, 1, 1, 0, 0, 0).minusSeconds(1);
                    
                    for (Order order : allOrders) {
                        if (order.getOrderDate() != null) {
                            Calendar orderCal = Calendar.getInstance();
                            orderCal.setTime(java.sql.Timestamp.valueOf(order.getOrderDate()));
                            int orderYear = orderCal.get(Calendar.YEAR);
                            
                            if (orderYear == year) {
                                grossRevenue += effectiveAmount(order);
                            }
                        }
                    }
                    
                    refundAmount = calculateRefundAmount(startDate, endDate, null);
                    break;
            }
            
            // Net revenue = Gross - Refund, but cannot be negative
            double netRevenue = Math.max(0.0, grossRevenue - refundAmount);
            
            System.out.println("\n===== KẾT QUẢ CUỐI CÙNG =====");
            System.out.println("Kỳ báo cáo: " + periodName);
            System.out.println("Doanh thu gộp: " + formatCurrency(grossRevenue));
            System.out.println("Tổng hoàn tiền: " + formatCurrency(refundAmount));
            System.out.println("Doanh thu thực: " + formatCurrency(netRevenue));
            
            result.put("periodName", periodName);
            result.put("grossRevenue", grossRevenue);
            result.put("formattedGrossRevenue", formatCurrency(grossRevenue));
            result.put("refundAmount", refundAmount);
            result.put("formattedRefundAmount", formatCurrency(refundAmount));
            result.put("netRevenue", netRevenue);
            result.put("formattedNetRevenue", formatCurrency(netRevenue));
            
            // Backward compatibility
            result.put("revenue", netRevenue);
            result.put("formattedRevenue", formatCurrency(netRevenue));
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy thống kê kỳ đã chọn: " + e.getMessage());
            e.printStackTrace();
            result.put("periodName", "Không xác định");
            result.put("grossRevenue", 0.0);
            result.put("formattedGrossRevenue", formatCurrency(0.0));
            result.put("refundAmount", 0.0);
            result.put("formattedRefundAmount", formatCurrency(0.0));
            result.put("netRevenue", 0.0);
            result.put("formattedNetRevenue", formatCurrency(0.0));
            result.put("revenue", 0.0);
            result.put("formattedRevenue", formatCurrency(0.0));
        }
        
        return result;
    }

    /**
     * Calculate revenue for orders with specific status (SHIPPING or CONFIRMED)
     * @param status Order status (SHIPPING or CONFIRMED)
     * @param shopId Shop ID to filter by, null for all shops
     * @return Map containing revenue amount and formatted revenue
     */
    public Map<String, Object> getRevenueByStatus(Order.OrderStatus status, Long shopId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<Order> allOrders = orderRepository.findAll();
            double revenue = 0.0;
            int orderCount = 0;
            
            for (Order order : allOrders) {
                // Filter by shop if specified
                if (shopId != null) {
                    if (order.getShop() == null || !order.getShop().getShopId().equals(shopId)) {
                        continue;
                    }
                }
                
                // Check if order has the specified status
                if (order.getStatus() == status) {
                    orderCount++;
                    // Get order amount (prefer finalAmount, fallback to totalAmount)
                    Double amount = null;
                    try {
                        amount = order.getFinalAmount();
                        if (amount == null || amount <= 0) {
                            amount = order.getTotalAmount();
                        }
                    } catch (Exception ignored) {}
                    
                    if (amount != null && amount > 0) {
                        revenue += amount;
                    }
                }
            }
            
            result.put("revenue", revenue);
            result.put("formattedRevenue", formatCurrency(revenue));
            result.put("orderCount", orderCount);
            result.put("formattedOrderCount", String.valueOf(orderCount));
        } catch (Exception e) {
            System.err.println("Lỗi khi tính doanh thu theo trạng thái " + status + ": " + e.getMessage());
            e.printStackTrace();
            result.put("revenue", 0.0);
            result.put("formattedRevenue", formatCurrency(0.0));
            result.put("orderCount", 0);
            result.put("formattedOrderCount", "0");
        }
        
        return result;
    }
    
    /**
     * Get order completion rate statistics
     * @return Map containing completion rate and text
     */
    public Map<String, Object> getOrderCompletionStats() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<Order> allOrders = orderRepository.findAll();
            
            int totalOrders = allOrders.size();
            int completedOrders = 0;
            int cancelledOrders = 0;
            
            // Đếm đơn hàng theo trạng thái thực tế
            for (Order order : allOrders) {
                if (order.getStatus() != null) {
                    Order.OrderStatus status = order.getStatus();
                    if (status == Order.OrderStatus.DELIVERED) {
                        completedOrders++;
                    } else if (status == Order.OrderStatus.CANCELLED) {
                        cancelledOrders++;
                    }
                }
            }
            
            // In thông tin debug
            System.out.println("Thống kê hoàn thành đơn hàng:");
            System.out.println("Tổng đơn hàng: " + totalOrders);
            System.out.println("Hoàn thành: " + completedOrders);
            System.out.println("Hủy: " + cancelledOrders);
            
            // Tính toán tỉ lệ hoàn thành dựa trên dữ liệu thật
            double completionRate = totalOrders > 0 ? ((double) completedOrders / totalOrders) * 100 : 0.0;
            
            System.out.println("Thông tin hoàn thành đơn hàng: Tổng=" + totalOrders 
                + ", Hoàn thành=" + completedOrders + ", Hủy=" + cancelledOrders 
                + ", Tỷ lệ=" + completionRate + "%");
            
            result.put("totalOrders", totalOrders);
            result.put("completedOrders", completedOrders);
            result.put("cancelledOrders", cancelledOrders);
            result.put("completionRate", formatNumber(completionRate));
            result.put("completionText", completedOrders + "/" + totalOrders);
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy thống kê hoàn thành đơn hàng: " + e.getMessage());
            e.printStackTrace();
            result.put("totalOrders", 0);
            result.put("completedOrders", 0);
            result.put("cancelledOrders", 0);
            result.put("completionRate", "0.0");
            result.put("completionText", "0/0");
        }
        
        return result;
    }

    /**
     * Get top customers by order value - Sửa để hiển thị khách hàng nhiều hơn
     * @param limit Maximum number of customers to return
     * @return List of top customers
     */
    public List<Map<String, Object>> getTopCustomersByValue(int limit) {
        try {
            System.out.println("Lấy danh sách top " + limit + " khách hàng");
            // Lấy tất cả đơn hàng
            List<Order> allOrders = orderRepository.findAll();
            
            if (allOrders.isEmpty()) {
                return Collections.emptyList();
            }
            
            // Gom nhóm theo khách hàng
            Map<User, Map<String, Object>> customerMap = new HashMap<>();
            
            for (Order order : allOrders) {
                if (order.getUser() != null && order.getTotalAmount() != null) {
                    User user = order.getUser();
                    Map<String, Object> stats;
                    
                    if (customerMap.containsKey(user)) {
                        stats = customerMap.get(user);
                        int orderCount = (int) stats.get("orderCount");
                        double totalSpent = (double) stats.get("totalSpent");
                        
                        stats.put("orderCount", orderCount + 1);
                        stats.put("totalSpent", totalSpent + order.getTotalAmount());
                    } else {
                        stats = new HashMap<>();
                        stats.put("userId", user.getUserId());
                        stats.put("name", user.getName());
                        stats.put("orderCount", 1);
                        stats.put("totalSpent", order.getTotalAmount());
                    }
                    
                    customerMap.put(user, stats);
                }
            }
            
            // Chuyển map thành list và sắp xếp theo tổng chi tiêu
            List<Map<String, Object>> customers = new ArrayList<>(customerMap.values());
            Collections.sort(customers, (c1, c2) -> Double.compare(
                (double) c2.get("totalSpent"), 
                (double) c1.get("totalSpent")
            ));
            
            // Tính giá trị trung bình đơn hàng và thêm định dạng
            return customers.stream()
                .limit(limit)
                .map(customer -> {
                    int orderCount = (int) customer.get("orderCount");
                    double totalSpent = (double) customer.get("totalSpent");
                    double avgOrderValue = orderCount > 0 ? totalSpent / orderCount : 0.0;
                    
                    customer.put("avgOrderValue", avgOrderValue);
                    customer.put("formattedTotalSpent", formatCurrency(totalSpent));
                    return customer;
                })
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy danh sách khách hàng hàng đầu: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * Get top selling products - Sửa để hiển thị sản phẩm nhiều hơn
     * @param limit Maximum number of products to return
     * @return List of top products
     */
    public List<Map<String, Object>> getTopProducts(int limit) {
        try {
            System.out.println("Lấy danh sách top " + limit + " sản phẩm");
            // Lấy tất cả chi tiết đơn hàng
            List<OrderDetail> allOrderDetails = orderDetailRepository.findAll();
            
            if (allOrderDetails.isEmpty()) {
                return Collections.emptyList();
            }
            
            // Gom nhóm theo sản phẩm
            Map<Product, Map<String, Object>> productMap = new HashMap<>();
            
            for (OrderDetail detail : allOrderDetails) {
                if (detail.getProduct() != null && detail.getQuantity() != null && detail.getUnitPrice() != null) {
                    Product product = detail.getProduct();
                    double subtotal = detail.getQuantity() * detail.getUnitPrice();
                    Map<String, Object> stats;
                    
                    if (productMap.containsKey(product)) {
                        stats = productMap.get(product);
                        int quantitySold = (int) stats.get("quantitySold");
                        double revenue = (double) stats.get("revenue");
                        @SuppressWarnings("unchecked")
                        List<Double> prices = (List<Double>) stats.get("prices");
                        
                        stats.put("quantitySold", quantitySold + detail.getQuantity());
                        stats.put("revenue", revenue + subtotal);
                        prices.add(detail.getUnitPrice());
                    } else {
                        stats = new HashMap<>();
                        List<Double> prices = new ArrayList<>();
                        prices.add(detail.getUnitPrice());
                        
                        stats.put("productId", product.getProductId());
                        stats.put("name", product.getProductName());
                        stats.put("quantitySold", detail.getQuantity());
                        stats.put("revenue", subtotal);
                        stats.put("prices", prices);
                    }
                    
                    productMap.put(product, stats);
                }
            }
            
            // Chuyển map thành list và sắp xếp theo doanh thu
            List<Map<String, Object>> products = new ArrayList<>();
            
            for (Map<String, Object> product : productMap.values()) {
                @SuppressWarnings("unchecked")
                List<Double> prices = (List<Double>) product.get("prices");
                
                if (prices != null && !prices.isEmpty()) {
                    double sum = 0.0;
                    double min = Double.MAX_VALUE;
                    double max = Double.MIN_VALUE;
                    
                    for (Double price : prices) {
                        sum += price;
                        min = Math.min(min, price);
                        max = Math.max(max, price);
                    }
                    
                    double avgPrice = sum / prices.size();
                    
                    product.put("avgPrice", avgPrice);
                    product.put("minPrice", min);
                    product.put("maxPrice", max);
                    product.put("formattedRevenue", formatCurrency((double) product.get("revenue")));
                    product.remove("prices");
                    products.add(product);
                }
            }
            
            Collections.sort(products, (p1, p2) -> Double.compare(
                (double) p2.get("revenue"), 
                (double) p1.get("revenue")
            ));
            
            return products.stream()
                .limit(limit)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy danh sách sản phẩm bán chạy: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
    
    /**
     * Get chart data for revenue statistics - Sửa để phân bố dữ liệu hợp lý vào biểu đồ
     * @param type Type of chart: 'month', 'quarter', 'year'
     * @param year Selected year
     * @return Chart data
     */
    public Map<String, Object> getChartData(String type, int year) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<Order> allOrders = orderRepository.findAll();
            System.out.println("Tổng số đơn hàng cho biểu đồ: " + allOrders.size());
            
            // Tính tổng doanh thu và đơn hàng từ tất cả các dữ liệu
            double totalRevenue = 0.0;
            int totalOrders = 0;
            
            for (Order order : allOrders) {
                if (order.getOrderDate() != null && order.getTotalAmount() != null) {
                    totalRevenue += order.getTotalAmount();
                    totalOrders++;
                }
            }
            
            System.out.println("Tổng doanh thu: " + totalRevenue);
            System.out.println("Tổng số đơn hàng: " + totalOrders);
            
            switch (type) {
                case "month":
                    // Monthly data for the selected year
                    String[] monthLabels = {"T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "T10", "T11", "T12"};
                    double[] monthRevenueData = new double[12];
                    int[] monthOrderData = new int[12];
                    
                    // Sử dụng dữ liệu thực từ đơn hàng phân loại theo tháng
                    if (totalOrders > 0) {
                        System.out.println("Đang xử lý dữ liệu theo tháng cho năm " + year);
                        
                        // Duyệt qua tất cả đơn hàng và phân loại theo tháng
                        for (Order order : allOrders) {
                            if (order.getOrderDate() != null && order.getTotalAmount() != null) {
                                // Sử dụng Calendar để lấy thông tin tháng và năm
                                Calendar cal = Calendar.getInstance();
                                cal.setTime(java.sql.Timestamp.valueOf(order.getOrderDate()));
                                int orderMonth = cal.get(Calendar.MONTH) + 1; // Chuyển sang 1-based (1-12)
                                int orderYear = cal.get(Calendar.YEAR);
                                
                                // Chỉ tính các đơn hàng trong năm được chọn
                                if (orderYear == year) {
                                    // Chú ý: mảng monthRevenueData và monthOrderData bắt đầu từ index 0, nhưng tháng từ 1
                                    monthRevenueData[orderMonth-1] += order.getTotalAmount();
                                    monthOrderData[orderMonth-1]++;
                                    System.out.println("Đơn hàng tháng " + orderMonth + 
                                                     ", ID: " + order.getOrderId() + 
                                                     ", Giá trị: " + order.getTotalAmount());
                                }
                            }
                        }
                    }
                    
                    // Trả về kết quả
                    result.put("labels", monthLabels);
                    result.put("revenueData", monthRevenueData);
                    result.put("orderData", monthOrderData);
                    break;
                    
                case "quarter":
                    // Quarterly data for the selected year
                    String[] quarterLabels = {"Q1", "Q2", "Q3", "Q4"};
                    double[] quarterRevenueData = new double[4];
                    int[] quarterOrderData = new int[4];
                    
                    // Sử dụng dữ liệu thực từ đơn hàng phân loại theo quý
                    if (totalOrders > 0) {
                        System.out.println("Đang xử lý dữ liệu theo quý cho năm " + year);
                        
                        // Duyệt qua tất cả đơn hàng và phân loại theo quý
                        for (Order order : allOrders) {
                            if (order.getOrderDate() != null && order.getTotalAmount() != null) {
                                // Sử dụng Calendar để lấy thông tin tháng và năm
                                Calendar cal = Calendar.getInstance();
                                cal.setTime(java.sql.Timestamp.valueOf(order.getOrderDate()));
                                int orderMonth = cal.get(Calendar.MONTH) + 1; // Chuyển sang 1-based (1-12)
                                int orderYear = cal.get(Calendar.YEAR);
                                
                                // Xác định quý từ tháng (1-3: Q1, 4-6: Q2, 7-9: Q3, 10-12: Q4)
                                int quarterIndex = (orderMonth - 1) / 3;
                                
                                // Chỉ tính các đơn hàng trong năm được chọn
                                if (orderYear == year) {
                                    quarterRevenueData[quarterIndex] += order.getTotalAmount();
                                    quarterOrderData[quarterIndex]++;
                                    System.out.println("Đơn hàng quý " + (quarterIndex + 1) + 
                                                     ", ID: " + order.getOrderId() + 
                                                     ", Giá trị: " + order.getTotalAmount());
                                }
                            }
                        }
                        
                        System.out.println("Thống kê theo quý:");
                        for (int i = 0; i < 4; i++) {
                            System.out.println("Q" + (i+1) + ": Doanh thu " + quarterRevenueData[i] + ", Đơn hàng: " + quarterOrderData[i]);
                        }
                    }
                    
                    result.put("labels", quarterLabels);
                    result.put("revenueData", quarterRevenueData);
                    result.put("orderData", quarterOrderData);
                    break;
                    
                case "year":
                    // Yearly data - tạo dữ liệu theo năm
                    int thisYear = LocalDate.now().getYear();
                    
                    // Tạo dữ liệu cho 5 năm gần nhất
                    int minYear = thisYear - 4;
                    int maxYear = thisYear;
                    
                    System.out.println("Tạo dữ liệu theo năm từ " + minYear + " đến " + maxYear);
                    
                    // In thông tin về các đơn hàng
                    for (Order order : allOrders) {
                        if (order.getOrderDate() != null) {
                            // Chuyển đổi java.util.Date sang năm bằng Calendar
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(java.sql.Timestamp.valueOf(order.getOrderDate()));
                            int orderYear = cal.get(Calendar.YEAR);
                            System.out.println("Năm của đơn hàng: " + orderYear + ", ID: " + order.getOrderId());
                            
                            // Cập nhật min và max năm từ dữ liệu thực tế
                            if (orderYear < minYear) minYear = orderYear;
                            if (orderYear > maxYear) maxYear = orderYear;
                        }
                    }
                    
                    if (minYear == Integer.MAX_VALUE || maxYear == Integer.MIN_VALUE) {
                        // No order data
                        int currentYear = LocalDate.now().getYear();
                        String[] defaultYearLabels = {
                            String.valueOf(currentYear - 4),
                            String.valueOf(currentYear - 3),
                            String.valueOf(currentYear - 2),
                            String.valueOf(currentYear - 1),
                            String.valueOf(currentYear)
                        };
                        result.put("labels", defaultYearLabels);
                        result.put("revenueData", new double[5]);
                        result.put("orderData", new int[5]);
                    } else {
                        // Use actual data years
                        int numYears = maxYear - minYear + 1;
                        String[] yearLabels = new String[numYears];
                        double[] yearRevenueData = new double[numYears];
                        int[] yearOrderData = new int[numYears];
                        
                        for (int i = 0; i < numYears; i++) {
                            yearLabels[i] = String.valueOf(minYear + i);
                        }
                        
                        for (Order order : allOrders) {
                            if (order.getOrderDate() != null && order.getTotalAmount() != null) {
                                // Sử dụng Calendar thay vì toInstant()
                                Calendar cal = Calendar.getInstance();
                                cal.setTime(java.sql.Timestamp.valueOf(order.getOrderDate()));
                                int orderYear = cal.get(Calendar.YEAR);
                                int index = orderYear - minYear;
                                
                                if (index >= 0 && index < numYears) {
                                    yearRevenueData[index] += order.getTotalAmount();
                                    yearOrderData[index]++;
                                }
                            }
                        }
                        
                        result.put("labels", yearLabels);
                        result.put("revenueData", yearRevenueData);
                        result.put("orderData", yearOrderData);
                    }
                    break;
                    
                default:
                    result.put("labels", new String[0]);
                    result.put("revenueData", new double[0]);
                    result.put("orderData", new int[0]);
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy dữ liệu biểu đồ: " + e.getMessage());
            e.printStackTrace();
            result.put("labels", new String[0]);
            result.put("revenueData", new double[0]);
            result.put("orderData", new int[0]);
        }
        
        return result;
    }

    /**
     * Get year statistics for a specific year
     * @param year Year
     * @return Double revenue for the year
     */
    /**
     * Get order count for a specific month and year
     * @param month Month (1-12)
     * @param year Year
     * @return int Order count for the specified month
     */
    public int getOrderCountByMonth(int month, int year) {
        try {
            List<Order> allOrders = orderRepository.findAll();
            int orderCount = 0;
            
            for (Order order : allOrders) {
                // Chỉ đếm orders đã được delivered (có deliveredDate)
                if (order.getOrderDate() != null && order.getDeliveredDate() != null) {
                    Calendar orderDate = Calendar.getInstance();
                    orderDate.setTime(java.sql.Timestamp.valueOf(order.getOrderDate()));
                    int orderMonth = orderDate.get(Calendar.MONTH) + 1; // Chuyển sang 1-based (1-12)
                    int orderYear = orderDate.get(Calendar.YEAR);
                    
                    if (orderYear == year && orderMonth == month) {
                        orderCount++;
                    }
                }
            }
            
            return orderCount;
        } catch (Exception e) {
            System.err.println("Lỗi khi đếm đơn hàng theo tháng: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * Get order count for a specific quarter and year
     * @param quarter Quarter (1-4)
     * @param year Year
     * @return int Order count for the specified quarter
     */
    public int getOrderCountByQuarter(int quarter, int year) {
        try {
            List<Order> allOrders = orderRepository.findAll();
            int orderCount = 0;
            
            // Xác định các tháng trong quý
            int startMonth = (quarter - 1) * 3 + 1;
            int endMonth = quarter * 3;
            
            for (Order order : allOrders) {
                if (order.getOrderDate() != null) {
                    Calendar orderDate = Calendar.getInstance();
                    orderDate.setTime(java.sql.Timestamp.valueOf(order.getOrderDate()));
                    int orderMonth = orderDate.get(Calendar.MONTH) + 1; // Chuyển sang 1-based (1-12)
                    int orderYear = orderDate.get(Calendar.YEAR);
                    
                    if (orderYear == year && orderMonth >= startMonth && orderMonth <= endMonth) {
                        orderCount++;
                    }
                }
            }
            
            return orderCount;
        } catch (Exception e) {
            System.err.println("Lỗi khi đếm đơn hàng theo quý: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * Get order count for a specific year
     * @param year Year
     * @return int Order count for the specified year
     */
    public int getOrderCountByYear(int year) {
        try {
            List<Order> allOrders = orderRepository.findAll();
            int orderCount = 0;
            
            for (Order order : allOrders) {
                if (order.getOrderDate() != null) {
                    Calendar orderDate = Calendar.getInstance();
                    orderDate.setTime(java.sql.Timestamp.valueOf(order.getOrderDate()));
                    int orderYear = orderDate.get(Calendar.YEAR);
                    
                    if (orderYear == year) {
                        orderCount++;
                    }
                }
            }
            
            return orderCount;
        } catch (Exception e) {
            System.err.println("Lỗi khi đếm đơn hàng theo năm: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    public double getYearStatistics(int year) {
        try {
            // Truy vấn trực tiếp từ danh sách đơn hàng
            List<Order> allOrders = orderRepository.findAll();
            System.out.println("Tổng số đơn hàng cho năm " + year + ": " + allOrders.size());
            
            double yearRevenue = 0.0;
            int orderCount = 0;
            
            for (Order order : allOrders) {
                if (order.getOrderDate() != null && order.getTotalAmount() != null) {
                    Calendar orderDate = Calendar.getInstance();
                    orderDate.setTime(java.sql.Timestamp.valueOf(order.getOrderDate()));
                    int orderYear = orderDate.get(Calendar.YEAR);
                    
                    if (orderYear == year) {
                        yearRevenue += order.getTotalAmount();
                        orderCount++;
                        System.out.println("[Đơn hàng - Năm " + year + "] ID: " + order.getOrderId() + 
                                          ", Ngày: " + order.getOrderDate() + 
                                          ", Giá trị: " + order.getTotalAmount());
                    }
                }
            }
            
            System.out.println("Số đơn hàng năm " + year + ": " + orderCount);
            System.out.println("Tổng doanh thu năm " + year + ": " + formatCurrency(yearRevenue));
            return yearRevenue;
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy thống kê năm: " + e.getMessage());
            e.printStackTrace();
            return 0.0;
        }
    }
    
    // Helper methods
    private String formatCurrency(double value) {
        // Thêm debug để kiểm tra giá trị
        System.out.println("Định dạng giá trị tiền: " + value);
        
        if (value == 0.0) {
            // Hiển thị "0 đ" khi giá trị là 0
            return "0 đ";
        }
        
        // Định dạng thông thường
        return currencyFormat.format(value) + " đ";
    }
    
    private String formatNumber(double value) {
        return String.format("%.1f", value);
    }
}