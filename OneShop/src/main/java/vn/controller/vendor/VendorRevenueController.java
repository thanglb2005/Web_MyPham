package vn.controller.vendor;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.entity.Order;
import vn.entity.User;
import vn.service.OrderService;
import vn.service.ShopService;
import vn.service.statistics.RevenueStatisticsService;

import java.time.LocalDate;
import java.util.*;

/**
 * Controller quản lý doanh thu cho Vendor
 * @author OneShop Team
 */
@Controller
@RequestMapping("/vendor/revenue")
public class VendorRevenueController {

    @Autowired
    private OrderService orderService;
    
    @Autowired
    private ShopService shopService;
    
    @Autowired
    private RevenueStatisticsService revenueStatisticsService;

    /**
     * Trang chính quản lý doanh thu
     */
    @GetMapping
    public String revenueDashboard(
            @RequestParam(value = "type", defaultValue = "month") String type,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "month", required = false) Integer month,
            @RequestParam(value = "quarter", required = false) Integer quarter,
            @RequestParam(value = "shopId", required = false) Long shopId,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            HttpSession session, Model model) {
        
        User vendor = ensureVendor(session);
        if (vendor == null) {
            return "redirect:/login";
        }

        // Lấy shop của vendor
        List<Long> shopIds = getShopIdsByVendor(vendor);
        if (shopIds.isEmpty()) {
            model.addAttribute("error", "Bạn chưa có shop nào.");
            return "vendor/revenue/dashboard";
        }

        // Hạn chế theo shopId nếu được chọn và thuộc vendor
        if (shopId != null && shopIds.contains(shopId)) {
            shopIds = java.util.List.of(shopId);
        }

        // Set current values
        LocalDate today = LocalDate.now();
        int currentYear = year != null ? year : today.getYear();
        int currentMonth = month != null ? month : today.getMonthValue();
        int currentQuarter = quarter != null ? quarter : (currentMonth - 1) / 3 + 1;

        try {
            // Thống kê hôm nay
            Map<String, Object> todayStats = getTodayRevenueStats(shopIds);
            model.addAttribute("todayOrders", todayStats.get("orderCount"));
            model.addAttribute("todayRevenue", todayStats.get("formattedRevenue"));

            // Thống kê tháng hiện tại
            Map<String, Object> currentMonthStats = getCurrentMonthRevenueStats(shopIds);
            model.addAttribute("currentMonthRevenue", currentMonthStats.get("formattedRevenue"));
            model.addAttribute("currentMonthOrders", currentMonthStats.get("orderCount"));
            model.addAttribute("growthRate", currentMonthStats.get("growthRate"));
            model.addAttribute("isPositiveGrowth", currentMonthStats.get("isPositiveGrowth"));

            // Thống kê theo kỳ được chọn
            Map<String, Object> periodStats = getSelectedPeriodRevenueStats(shopIds, type, currentYear, currentMonth, currentQuarter,
                    parseDate(startDate), parseDate(endDate));
            model.addAttribute("selectedPeriod", periodStats.get("periodName"));
            model.addAttribute("selectedRevenue", periodStats.get("formattedRevenue"));
            model.addAttribute("selectedOrders", periodStats.get("orderCount"));

            // Thống kê tổng quan
            Map<String, Object> overviewStats = getOverviewStats(shopIds, parseDate(startDate), parseDate(endDate));
            model.addAttribute("totalRevenue", overviewStats.get("formattedTotalRevenue"));
            model.addAttribute("totalOrders", overviewStats.get("totalOrders"));
            model.addAttribute("completedOrders", overviewStats.get("completedOrders"));
            model.addAttribute("cancelledOrders", overviewStats.get("cancelledOrders"));
            model.addAttribute("completionRate", overviewStats.get("completionRate"));

            // Lược bỏ phần top sản phẩm để trang gọn hơn

            // Dữ liệu cho biểu đồ
            Map<String, Object> chartData = getChartDataForVendor(shopIds, type, currentYear);
            model.addAttribute("chartLabels", chartData.get("labels"));
            model.addAttribute("chartRevenueData", chartData.get("revenueData"));
            model.addAttribute("chartOrderData", chartData.get("orderData"));

            // Thông tin filter
            model.addAttribute("selectedType", type);
            model.addAttribute("currentYear", currentYear);
            model.addAttribute("currentMonth", currentMonth);
            model.addAttribute("currentQuarter", currentQuarter);
            model.addAttribute("selectedShopId", shopId);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            model.addAttribute("vendor", vendor);
            model.addAttribute("pageTitle", "Quản lý doanh thu");

            // Danh sách shop để chọn
            var shops = shopService.findAllByVendor(vendor);
            model.addAttribute("shopList", shops);

        } catch (Exception e) {
            System.err.println("Lỗi trong vendor revenue controller: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Có lỗi xảy ra khi tải dữ liệu thống kê: " + e.getMessage());
        }

        return "vendor/revenue/dashboard";
    }

    /**
     * API endpoint để lấy dữ liệu biểu đồ theo thời gian
     */
    @GetMapping("/api/chart-data")
    @ResponseBody
    public Map<String, Object> getChartData(
            @RequestParam String type,
            @RequestParam int year,
            HttpSession session) {
        
        User vendor = ensureVendor(session);
        if (vendor == null) {
            return Collections.emptyMap();
        }

        List<Long> shopIds = getShopIdsByVendor(vendor);
        if (shopIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return getChartDataForVendor(shopIds, type, year);
    }

    /**
     * API endpoint để so sánh doanh thu giữa các kỳ
     */
    @GetMapping("/api/compare-periods")
    @ResponseBody
    public Map<String, Object> comparePeriods(
            @RequestParam String type,
            @RequestParam String period,
            HttpSession session) {
        
        User vendor = ensureVendor(session);
        if (vendor == null) {
            return Collections.emptyMap();
        }

        List<Long> shopIds = getShopIdsByVendor(vendor);
        if (shopIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Object> result = new HashMap<>();
        
        try {
            if (type.equals("month")) {
                String[] parts = period.split("-");
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                
                int prevMonth = month - 1;
                int prevYear = year;
                if (prevMonth == 0) {
                    prevMonth = 12;
                    prevYear--;
                }
                
                Map<String, Object> currentMonthData = getMonthRevenueStats(shopIds, month, year);
                Map<String, Object> previousMonthData = getMonthRevenueStats(shopIds, prevMonth, prevYear);
                
                result.put("currentPeriod", "Tháng " + month + "/" + year);
                result.put("previousPeriod", "Tháng " + prevMonth + "/" + prevYear);
                result.put("currentRevenue", currentMonthData.get("revenue"));
                result.put("previousRevenue", previousMonthData.get("revenue"));
                result.put("currentOrders", currentMonthData.get("orderCount"));
                result.put("previousOrders", previousMonthData.get("orderCount"));
                
            } else if (type.equals("quarter")) {
                String[] parts = period.split("-Q");
                int year = Integer.parseInt(parts[0]);
                int quarter = Integer.parseInt(parts[1]);
                
                int prevQuarter = quarter - 1;
                int prevYear = year;
                if (prevQuarter == 0) {
                    prevQuarter = 4;
                    prevYear--;
                }
                
                Map<String, Object> currentQuarterData = getQuarterRevenueStats(shopIds, quarter, year);
                Map<String, Object> previousQuarterData = getQuarterRevenueStats(shopIds, prevQuarter, prevYear);
                
                result.put("currentPeriod", "Quý " + quarter + "/" + year);
                result.put("previousPeriod", "Quý " + prevQuarter + "/" + prevYear);
                result.put("currentRevenue", currentQuarterData.get("revenue"));
                result.put("previousRevenue", previousQuarterData.get("revenue"));
                result.put("currentOrders", currentQuarterData.get("orderCount"));
                result.put("previousOrders", previousQuarterData.get("orderCount"));
                
            } else if (type.equals("year")) {
                int year = Integer.parseInt(period);
                int prevYear = year - 1;
                
                Map<String, Object> currentYearData = getYearRevenueStats(shopIds, year);
                Map<String, Object> previousYearData = getYearRevenueStats(shopIds, prevYear);
                
                result.put("currentPeriod", "Năm " + year);
                result.put("previousPeriod", "Năm " + prevYear);
                result.put("currentRevenue", currentYearData.get("revenue"));
                result.put("previousRevenue", previousYearData.get("revenue"));
                result.put("currentOrders", currentYearData.get("orderCount"));
                result.put("previousOrders", previousYearData.get("orderCount"));
            }
            
            // Tính toán chênh lệch và tỷ lệ tăng trưởng
            double currentRevenue = ((Number) result.get("currentRevenue")).doubleValue();
            double previousRevenue = ((Number) result.get("previousRevenue")).doubleValue();
            double diff = currentRevenue - previousRevenue;
            
            double growthRate;
            if (previousRevenue > 0) {
                growthRate = (diff / previousRevenue) * 100;
            } else if (currentRevenue > 0) {
                growthRate = 100;
            } else {
                growthRate = 0;
            }
            
            result.put("diff", diff);
            result.put("growthRate", growthRate);
            result.put("isPositiveGrowth", growthRate >= 0);
            
        } catch (Exception e) {
            result.put("error", "Có lỗi xảy ra: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }

    // ========== HELPER METHODS ==========

    /**
     * Kiểm tra quyền vendor
     */
    private User ensureVendor(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !user.getRoles().stream().anyMatch(role -> "ROLE_VENDOR".equals(role.getName()))) {
            return null;
        }
        return user;
    }

    /**
     * Lấy shop IDs của vendor
     */
    private List<Long> getShopIdsByVendor(User vendor) {
        return shopService.findShopIdsByVendor(vendor);
    }

    /**
     * Lấy đơn hàng đã hoàn thành của vendor theo shop IDs
     */
    private List<Order> getCompletedOrdersByShops(List<Long> shopIds) {
        if (shopIds.isEmpty()) {
            return Collections.emptyList();
        }
        // Dùng truy vấn trực tiếp theo orders.shop_id để khớp dữ liệu thực tế
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
        Page<Order> orderPage = orderService.findByShopIdInAndStatusDirect(shopIds, Order.OrderStatus.DELIVERED, pageable);
        return orderPage.getContent();
    }

    private LocalDate parseDate(String iso) {
        if (iso == null || iso.isBlank()) return null;
        try {
            return LocalDate.parse(iso);
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Thống kê doanh thu hôm nay
     */
    private Map<String, Object> getTodayRevenueStats(List<Long> shopIds) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<Order> completedOrders = getCompletedOrdersByShops(shopIds);
            LocalDate today = LocalDate.now();
            
            int orderCount = 0;
            double revenue = 0.0;
            
            // COD: ghi nhận theo delivered_date
            // Online (MOMO/BANK/VIETQR): ghi nhận theo payment_date nếu đã thanh toán (payment_status = true)
            for (Order order : completedOrders) {
                boolean isCod = order.getPaymentMethod() == Order.PaymentMethod.COD;
                boolean isOnline = order.getPaymentMethod() != Order.PaymentMethod.COD;

                boolean countToday = false;
                if (isCod) {
                    countToday = (order.getDeliveredDate() != null && order.getDeliveredDate().toLocalDate().equals(today));
                } else if (isOnline) {
                    // paymentPaid = true nghĩa là đã thanh toán
                    countToday = Boolean.TRUE.equals(order.getPaymentPaid())
                            && order.getPaymentDate() != null
                            && order.getPaymentDate().toLocalDate().equals(today);
                }

                if (countToday) {
                    orderCount++;
                    if (order.getTotalAmount() != null) {
                        revenue += order.getTotalAmount();
                    }
                }
            }
            
            result.put("orderCount", orderCount);
            result.put("revenue", revenue);
            result.put("formattedRevenue", formatCurrency(revenue));
            
        } catch (Exception e) {
            result.put("orderCount", 0);
            result.put("revenue", 0.0);
            result.put("formattedRevenue", "0 đ");
        }
        
        return result;
    }

    /**
     * Thống kê doanh thu tháng hiện tại
     */
    private Map<String, Object> getCurrentMonthRevenueStats(List<Long> shopIds) {
        LocalDate today = LocalDate.now();
        return getMonthRevenueStats(shopIds, today.getMonthValue(), today.getYear());
    }

    /**
     * Thống kê doanh thu theo tháng
     */
    private Map<String, Object> getMonthRevenueStats(List<Long> shopIds, int month, int year) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<Order> completedOrders = getCompletedOrdersByShops(shopIds);
            
            double currentMonthRevenue = 0.0;
            double previousMonthRevenue = 0.0;
            int currentMonthOrders = 0;
            
            // Tính tháng trước
            int prevMonth = month - 1;
            int prevYear = year;
            if (prevMonth == 0) {
                prevMonth = 12;
                prevYear--;
            }
            
            for (Order order : completedOrders) {
                if (order.getTotalAmount() == null) continue;
                boolean isCod = order.getPaymentMethod() == Order.PaymentMethod.COD;
                boolean isOnline = order.getPaymentMethod() != Order.PaymentMethod.COD;
                LocalDate d = null;
                if (isCod && order.getDeliveredDate() != null) d = order.getDeliveredDate().toLocalDate();
                if (isOnline && Boolean.TRUE.equals(order.getPaymentPaid()) && order.getPaymentDate() != null) d = order.getPaymentDate().toLocalDate();
                if (d == null) continue;

                int m = d.getMonthValue();
                int y = d.getYear();
                if (y == year && m == month) {
                    currentMonthRevenue += order.getTotalAmount();
                    currentMonthOrders++;
                } else if (y == prevYear && m == prevMonth) {
                    previousMonthRevenue += order.getTotalAmount();
                }
            }
            
            // Tính tỷ lệ tăng trưởng
            double growthRate = 0.0;
            if (previousMonthRevenue > 0) {
                growthRate = ((currentMonthRevenue - previousMonthRevenue) / previousMonthRevenue) * 100;
            } else if (currentMonthRevenue > 0) {
                growthRate = 100.0;
            }
            
            result.put("revenue", currentMonthRevenue);
            result.put("formattedRevenue", formatCurrency(currentMonthRevenue));
            result.put("orderCount", currentMonthOrders);
            result.put("growthRate", String.format("%.1f", growthRate));
            result.put("isPositiveGrowth", growthRate >= 0);
            
        } catch (Exception e) {
            result.put("revenue", 0.0);
            result.put("formattedRevenue", "0 đ");
            result.put("orderCount", 0);
            result.put("growthRate", "0.0");
            result.put("isPositiveGrowth", false);
        }
        
        return result;
    }

    /**
     * Thống kê doanh thu theo quý
     */
    private Map<String, Object> getQuarterRevenueStats(List<Long> shopIds, int quarter, int year) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<Order> completedOrders = getCompletedOrdersByShops(shopIds);
            
            double quarterRevenue = 0.0;
            int quarterOrders = 0;
            
            int startMonth = (quarter - 1) * 3 + 1;
            int endMonth = quarter * 3;
            
            for (Order order : completedOrders) {
                if (order.getTotalAmount() == null) continue;
                boolean isCod = order.getPaymentMethod() == Order.PaymentMethod.COD;
                boolean isOnline = order.getPaymentMethod() != Order.PaymentMethod.COD;
                LocalDate d = null;
                if (isCod && order.getDeliveredDate() != null) d = order.getDeliveredDate().toLocalDate();
                if (isOnline && Boolean.TRUE.equals(order.getPaymentPaid()) && order.getPaymentDate() != null) d = order.getPaymentDate().toLocalDate();
                if (d == null) continue;

                int m = d.getMonthValue();
                int y = d.getYear();
                if (y == year && m >= startMonth && m <= endMonth) {
                    quarterRevenue += order.getTotalAmount();
                    quarterOrders++;
                }
            }
            
            result.put("revenue", quarterRevenue);
            result.put("formattedRevenue", formatCurrency(quarterRevenue));
            result.put("orderCount", quarterOrders);
            
        } catch (Exception e) {
            result.put("revenue", 0.0);
            result.put("formattedRevenue", "0 đ");
            result.put("orderCount", 0);
        }
        
        return result;
    }

    /**
     * Thống kê doanh thu theo năm
     */
    private Map<String, Object> getYearRevenueStats(List<Long> shopIds, int year) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<Order> completedOrders = getCompletedOrdersByShops(shopIds);
            
            double yearRevenue = 0.0;
            int yearOrders = 0;
            
            for (Order order : completedOrders) {
                if (order.getTotalAmount() == null) continue;
                boolean isCod = order.getPaymentMethod() == Order.PaymentMethod.COD;
                boolean isOnline = order.getPaymentMethod() != Order.PaymentMethod.COD;
                LocalDate d = null;
                if (isCod && order.getDeliveredDate() != null) d = order.getDeliveredDate().toLocalDate();
                if (isOnline && Boolean.TRUE.equals(order.getPaymentPaid()) && order.getPaymentDate() != null) d = order.getPaymentDate().toLocalDate();
                if (d == null) continue;

                if (d.getYear() == year) {
                    yearRevenue += order.getTotalAmount();
                    yearOrders++;
                }
            }
            
            result.put("revenue", yearRevenue);
            result.put("formattedRevenue", formatCurrency(yearRevenue));
            result.put("orderCount", yearOrders);
            
        } catch (Exception e) {
            result.put("revenue", 0.0);
            result.put("formattedRevenue", "0 đ");
            result.put("orderCount", 0);
        }
        
        return result;
    }

    /**
     * Thống kê theo kỳ được chọn
     */
    private Map<String, Object> getSelectedPeriodRevenueStats(List<Long> shopIds, String type, int year, int month, int quarter,
                                                             LocalDate start, LocalDate end) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String periodName = "";
            double revenue = 0.0;
            int orderCount = 0;
            
            switch (type) {
                case "month":
                    periodName = "Tháng " + month + "/" + year;
                    Map<String, Object> monthStats = getMonthRevenueStats(shopIds, month, year);
                    revenue = ((Number) monthStats.get("revenue")).doubleValue();
                    orderCount = (Integer) monthStats.get("orderCount");
                    break;
                    
                case "quarter":
                    periodName = "Quý " + quarter + "/" + year;
                    Map<String, Object> quarterStats = getQuarterRevenueStats(shopIds, quarter, year);
                    revenue = ((Number) quarterStats.get("revenue")).doubleValue();
                    orderCount = (Integer) quarterStats.get("orderCount");
                    break;
                    
                case "year":
                    periodName = "Năm " + year;
                    Map<String, Object> yearStats = getYearRevenueStats(shopIds, year);
                    revenue = ((Number) yearStats.get("revenue")).doubleValue();
                    orderCount = (Integer) yearStats.get("orderCount");
                    break;
                default:
                    // Tùy chọn: nếu người dùng chọn khoảng ngày cụ thể
                    if (start != null || end != null) {
                        String startTxt = start != null ? start.toString() : "...";
                        String endTxt = end != null ? end.toString() : "...";
                        periodName = "Từ " + startTxt + " đến " + endTxt;
                        var filtered = filterOrdersBySettlementDate(getCompletedOrdersByShops(shopIds), start, end);
                        revenue = filtered.stream()
                                .filter(o -> o.getTotalAmount() != null)
                                .mapToDouble(Order::getTotalAmount)
                                .sum();
                        orderCount = filtered.size();
                    }
                    break;
            }
            
            result.put("periodName", periodName);
            result.put("revenue", revenue);
            result.put("formattedRevenue", formatCurrency(revenue));
            result.put("orderCount", orderCount);
            
        } catch (Exception e) {
            result.put("periodName", "Không xác định");
            result.put("revenue", 0.0);
            result.put("formattedRevenue", "0 đ");
            result.put("orderCount", 0);
        }
        
        return result;
    }

    /**
     * Thống kê tổng quan
     */
    private Map<String, Object> getOverviewStats(List<Long> shopIds, LocalDate start, LocalDate end) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Lấy tất cả đơn theo orders.shop_id
            Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
            Page<Order> orderPage = orderService.findByShopIdInDirect(shopIds, pageable);
            List<Order> allOrders = orderPage.getContent();
            if (start != null || end != null) {
                allOrders = filterOrdersBySettlementDate(allOrders, start, end);
            }
            
            double totalRevenue = 0.0;
            int totalOrders = allOrders.size();
            int completedOrders = 0;
            int cancelledOrders = 0;
            
            for (Order order : allOrders) {
                if (order.getStatus() == Order.OrderStatus.DELIVERED && order.getTotalAmount() != null) {
                    totalRevenue += order.getTotalAmount();
                    completedOrders++;
                } else if (order.getStatus() == Order.OrderStatus.CANCELLED || 
                          order.getStatus() == Order.OrderStatus.CANCELED) {
                    cancelledOrders++;
                }
            }
            
            double completionRate = totalOrders > 0 ? ((double) completedOrders / totalOrders) * 100 : 0.0;
            
            result.put("totalRevenue", totalRevenue);
            result.put("formattedTotalRevenue", formatCurrency(totalRevenue));
            result.put("totalOrders", totalOrders);
            result.put("completedOrders", completedOrders);
            result.put("cancelledOrders", cancelledOrders);
            result.put("completionRate", String.format("%.1f", completionRate));
            
        } catch (Exception e) {
            result.put("totalRevenue", 0.0);
            result.put("formattedTotalRevenue", "0 đ");
            result.put("totalOrders", 0);
            result.put("completedOrders", 0);
            result.put("cancelledOrders", 0);
            result.put("completionRate", "0.0");
        }
        
        return result;
    }

    private List<Order> filterOrdersBySettlementDate(List<Order> orders, LocalDate start, LocalDate end) {
        return orders.stream().filter(o -> {
            boolean isCod = o.getPaymentMethod() == Order.PaymentMethod.COD;
            boolean isOnline = o.getPaymentMethod() != Order.PaymentMethod.COD;
            LocalDate d = null;
            if (isCod && o.getDeliveredDate() != null) d = o.getDeliveredDate().toLocalDate();
            if (isOnline && Boolean.TRUE.equals(o.getPaymentPaid()) && o.getPaymentDate() != null) d = o.getPaymentDate().toLocalDate();
            if (d == null) return false;
            boolean afterStart = (start == null) || !d.isBefore(start);
            boolean beforeEnd = (end == null) || !d.isAfter(end);
            return afterStart && beforeEnd;
        }).toList();
    }

    /**
     * Top sản phẩm bán chạy của vendor
     */
    private List<Map<String, Object>> getTopProductsByVendor(List<Long> shopIds, int limit) {
        try {
            // Sử dụng service có sẵn nhưng filter theo shop IDs
            List<Map<String, Object>> allTopProducts = revenueStatisticsService.getTopProducts(100);
            
            // Filter theo shop của vendor (cần implement logic này trong service)
            // Tạm thời return top products chung
            return allTopProducts.stream()
                    .limit(limit)
                    .collect(java.util.stream.Collectors.toList());
                    
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * Dữ liệu biểu đồ cho vendor
     */
    private Map<String, Object> getChartDataForVendor(List<Long> shopIds, String type, int year) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<Order> completedOrders = getCompletedOrdersByShops(shopIds);
            
            switch (type) {
                case "month":
                    String[] monthLabels = {"T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "T10", "T11", "T12"};
                    double[] monthRevenueData = new double[12];
                    int[] monthOrderData = new int[12];
                    
                    for (Order order : completedOrders) {
                        if (order.getTotalAmount() == null) continue;
                        boolean isCod = order.getPaymentMethod() == Order.PaymentMethod.COD;
                        boolean isOnline = order.getPaymentMethod() != Order.PaymentMethod.COD;
                        LocalDate d = null;
                        if (isCod && order.getDeliveredDate() != null) d = order.getDeliveredDate().toLocalDate();
                        if (isOnline && Boolean.TRUE.equals(order.getPaymentPaid()) && order.getPaymentDate() != null) d = order.getPaymentDate().toLocalDate();
                        if (d == null) continue;
                        if (d.getYear() == year) {
                            monthRevenueData[d.getMonthValue() - 1] += order.getTotalAmount();
                            monthOrderData[d.getMonthValue() - 1]++;
                        }
                    }
                    
                    result.put("labels", monthLabels);
                    result.put("revenueData", monthRevenueData);
                    result.put("orderData", monthOrderData);
                    break;
                    
                case "quarter":
                    String[] quarterLabels = {"Q1", "Q2", "Q3", "Q4"};
                    double[] quarterRevenueData = new double[4];
                    int[] quarterOrderData = new int[4];
                    
                    for (Order order : completedOrders) {
                        if (order.getTotalAmount() == null) continue;
                        boolean isCod = order.getPaymentMethod() == Order.PaymentMethod.COD;
                        boolean isOnline = order.getPaymentMethod() != Order.PaymentMethod.COD;
                        LocalDate d = null;
                        if (isCod && order.getDeliveredDate() != null) d = order.getDeliveredDate().toLocalDate();
                        if (isOnline && Boolean.TRUE.equals(order.getPaymentPaid()) && order.getPaymentDate() != null) d = order.getPaymentDate().toLocalDate();
                        if (d == null) continue;
                        if (d.getYear() == year) {
                            int quarterIndex = (d.getMonthValue() - 1) / 3;
                            quarterRevenueData[quarterIndex] += order.getTotalAmount();
                            quarterOrderData[quarterIndex]++;
                        }
                    }
                    
                    result.put("labels", quarterLabels);
                    result.put("revenueData", quarterRevenueData);
                    result.put("orderData", quarterOrderData);
                    break;
                    
                case "year":
                    // Tạo dữ liệu cho 5 năm gần nhất
                    int currentYear = LocalDate.now().getYear();
                    int minYear = currentYear - 4;
                    int maxYear = currentYear;
                    
                    // Tìm năm min/max từ dữ liệu thực tế
                    for (Order order : completedOrders) {
                        boolean isCod = order.getPaymentMethod() == Order.PaymentMethod.COD;
                        boolean isOnline = order.getPaymentMethod() != Order.PaymentMethod.COD;
                        LocalDate d = null;
                        if (isCod && order.getDeliveredDate() != null) d = order.getDeliveredDate().toLocalDate();
                        if (isOnline && Boolean.TRUE.equals(order.getPaymentPaid()) && order.getPaymentDate() != null) d = order.getPaymentDate().toLocalDate();
                        if (d == null) continue;
                        int y = d.getYear();
                        if (y < minYear) minYear = y;
                        if (y > maxYear) maxYear = y;
                    }
                    
                    int numYears = maxYear - minYear + 1;
                    String[] yearLabels = new String[numYears];
                    double[] yearRevenueData = new double[numYears];
                    int[] yearOrderData = new int[numYears];
                    
                    for (int i = 0; i < numYears; i++) {
                        yearLabels[i] = String.valueOf(minYear + i);
                    }
                    
                    for (Order order : completedOrders) {
                        if (order.getOrderDate() != null && order.getTotalAmount() != null) {
                            LocalDate orderDate = order.getOrderDate().toLocalDate();
                            int orderYear = orderDate.getYear();
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
                    break;
                    
                default:
                    result.put("labels", new String[0]);
                    result.put("revenueData", new double[0]);
                    result.put("orderData", new int[0]);
            }
            
        } catch (Exception e) {
            result.put("labels", new String[0]);
            result.put("revenueData", new double[0]);
            result.put("orderData", new int[0]);
        }
        
        return result;
    }

    /**
     * Format currency
     */
    private String formatCurrency(double value) {
        if (value == 0.0) {
            return "0 đ";
        }
        java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
        return df.format(value) + " đ";
    }
}
