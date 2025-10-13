package vn.controller.admin;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import vn.entity.User;
import vn.entity.Order;
import vn.repository.OrderRepository;
import vn.service.statistics.RevenueStatisticsService;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Revenue Statistics Controller - Kết nối với dữ liệu thật
 */
@Controller
@RequestMapping("/admin")
public class RevenueController {

    private final RevenueStatisticsService revenueStatisticsService;
    private final OrderRepository orderRepository;
    
    // API endpoint để lấy dữ liệu cho biểu đồ
    @GetMapping("/api/monthly-revenue-data")
    @ResponseBody
    public Map<String, Object> getMonthlyRevenueData() {
        List<Object[]> monthlyStats = orderRepository.getMonthlyOrderStatistics();
        Map<String, Object> result = new HashMap<>();
        
        // Tạo mảng dữ liệu cho 12 tháng
        double[] monthlyRevenue = new double[12];
        int[] monthlyOrders = new int[12];
        String[] months = {"Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6", 
                          "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"};
        
        // Điền dữ liệu từ kết quả truy vấn
        for (Object[] stats : monthlyStats) {
            int year = ((Number) stats[0]).intValue();
            int month = ((Number) stats[1]).intValue();
            
            // Lấy năm hiện tại
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            
            // Chỉ xử lý dữ liệu của năm hiện tại
            if (year == currentYear && month >= 1 && month <= 12) {
                // Cố gắng chuyển đổi revenue sang double, xử lý nhiều kiểu dữ liệu có thể có
                Object revenueObj = stats[3];
                double revenue = 0;
                if (revenueObj instanceof Number) {
                    revenue = ((Number) revenueObj).doubleValue();
                }
                
                // Cố gắng chuyển đổi orderCount sang int
                Object orderCountObj = stats[2];
                int orderCount = 0;
                if (orderCountObj instanceof Number) {
                    orderCount = ((Number) orderCountObj).intValue();
                }
                
                monthlyRevenue[month - 1] = revenue;
                monthlyOrders[month - 1] = orderCount;
            }
        }
        
        // Chỉ trả về dữ liệu thật từ database
        result.put("revenues", monthlyRevenue);
        result.put("orders", monthlyOrders);
        result.put("months", months);
        
        return result;
    }
    
    /**
     * API endpoint để so sánh doanh thu giữa các kỳ
     */
    @GetMapping("/api/compare-periods")
    @ResponseBody
    public Map<String, Object> comparePeriods(
            @RequestParam("type") String type,
            @RequestParam("period") String period) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (type.equals("month")) {
                // So sánh tháng với tháng trước
                String[] parts = period.split("-");
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                
                // Tính toán tháng trước
                int prevMonth = month - 1;
                int prevYear = year;
                if (prevMonth == 0) {
                    prevMonth = 12;
                    prevYear--;
                }
                
                // Lấy dữ liệu tháng hiện tại
                Map<String, Object> currentMonthData = revenueStatisticsService.getMonthStatistics(month, year);
                
                // Lấy dữ liệu tháng trước
                Map<String, Object> previousMonthData = revenueStatisticsService.getMonthStatistics(prevMonth, prevYear);
                
                // Lấy số đơn hàng của tháng hiện tại và tháng trước
                int currentMonthOrders = revenueStatisticsService.getOrderCountByMonth(month, year);
                int previousMonthOrders = revenueStatisticsService.getOrderCountByMonth(prevMonth, prevYear);
                
                // Thêm dữ liệu vào kết quả
                result.put("currentPeriod", "Tháng " + month + "/" + year);
                result.put("previousPeriod", "Tháng " + prevMonth + "/" + prevYear);
                result.put("currentRevenue", currentMonthData.get("revenue"));
                result.put("previousRevenue", previousMonthData.get("revenue"));
                result.put("currentOrders", currentMonthOrders);
                result.put("previousOrders", previousMonthOrders);
                
                // Thêm dữ liệu bổ sung cho báo cáo
                
                // 1. Thêm giá trị trung bình đơn hàng
                double currentAvgOrderValue = currentMonthOrders > 0 ? 
                    ((Number)currentMonthData.get("revenue")).doubleValue() / currentMonthOrders : 0;
                double prevAvgOrderValue = previousMonthOrders > 0 ? 
                    ((Number)previousMonthData.get("revenue")).doubleValue() / previousMonthOrders : 0;
                
                // 2. Thêm biến động trung bình giá trị đơn hàng
                double avgOrderValueChange = prevAvgOrderValue > 0 ? 
                    ((currentAvgOrderValue - prevAvgOrderValue) / prevAvgOrderValue) * 100 : 0;
                    
                // 3. Thêm chi tiết thời gian báo cáo
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.YEAR, year);
                cal.set(Calendar.MONTH, month - 1);
                String monthName = new java.text.SimpleDateFormat("MMMM", java.util.Locale.forLanguageTag("vi")).format(cal.getTime());
                
                Calendar prevCal = Calendar.getInstance();
                prevCal.set(Calendar.YEAR, prevYear);
                prevCal.set(Calendar.MONTH, prevMonth - 1);
                String prevMonthName = new java.text.SimpleDateFormat("MMMM", java.util.Locale.forLanguageTag("vi")).format(prevCal.getTime());
                
                // Đưa các dữ liệu chi tiết vào kết quả
                result.put("currentAvgOrderValue", currentAvgOrderValue);
                result.put("prevAvgOrderValue", prevAvgOrderValue);
                result.put("avgOrderValueChange", avgOrderValueChange);
                result.put("currentPeriodName", monthName);
                result.put("previousPeriodName", prevMonthName);
                result.put("hasIncreasedAvgValue", currentAvgOrderValue > prevAvgOrderValue);
                
            } else if (type.equals("quarter")) {
                // So sánh quý với quý trước
                String[] parts = period.split("-Q");
                int year = Integer.parseInt(parts[0]);
                int quarter = Integer.parseInt(parts[1]);
                
                // Tính toán quý trước
                int prevQuarter = quarter - 1;
                int prevYear = year;
                if (prevQuarter == 0) {
                    prevQuarter = 4;
                    prevYear--;
                }
                
                // Lấy dữ liệu quý hiện tại
                double currentQuarterRevenue = revenueStatisticsService.getQuarterStatistics(quarter, year);
                
                // Lấy dữ liệu quý trước
                double previousQuarterRevenue = revenueStatisticsService.getQuarterStatistics(prevQuarter, prevYear);
                
                // Lấy số đơn hàng của quý hiện tại và quý trước
                int currentQuarterOrders = revenueStatisticsService.getOrderCountByQuarter(quarter, year);
                int previousQuarterOrders = revenueStatisticsService.getOrderCountByQuarter(prevQuarter, prevYear);
                
                // Thêm dữ liệu vào kết quả
                result.put("currentPeriod", "Quý " + quarter + "/" + year);
                result.put("previousPeriod", "Quý " + prevQuarter + "/" + prevYear);
                result.put("currentRevenue", currentQuarterRevenue);
                result.put("previousRevenue", previousQuarterRevenue);
                result.put("currentOrders", currentQuarterOrders);
                result.put("previousOrders", previousQuarterOrders);
                
            } else if (type.equals("year")) {
                // So sánh năm với năm trước
                int year = Integer.parseInt(period);
                int prevYear = year - 1;
                
                // Lấy dữ liệu năm hiện tại
                double currentYearRevenue = revenueStatisticsService.getYearStatistics(year);
                
                // Lấy dữ liệu năm trước
                double previousYearRevenue = revenueStatisticsService.getYearStatistics(prevYear);
                
                // Lấy số đơn hàng của năm hiện tại và năm trước
                int currentYearOrders = revenueStatisticsService.getOrderCountByYear(year);
                int previousYearOrders = revenueStatisticsService.getOrderCountByYear(prevYear);
                
                // Thêm dữ liệu vào kết quả
                result.put("currentPeriod", "Năm " + year);
                result.put("previousPeriod", "Năm " + prevYear);
                result.put("currentRevenue", currentYearRevenue);
                result.put("previousRevenue", previousYearRevenue);
                result.put("currentOrders", currentYearOrders);
                result.put("previousOrders", previousYearOrders);
            }
            
            // Tính toán chênh lệch và tỷ lệ tăng trưởng
            double currentRevenue = ((Number) result.get("currentRevenue")).doubleValue();
            double previousRevenue = ((Number) result.get("previousRevenue")).doubleValue();
            double diff = currentRevenue - previousRevenue;
            
            // Xử lý các trường hợp đặc biệt để tránh hiển thị -100%
            double growthRate;
            if (previousRevenue > 0) {
                growthRate = (diff / previousRevenue) * 100;
            } else if (currentRevenue > 0) {
                growthRate = 100; // Tăng trưởng 100% nếu kỳ trước không có doanh thu
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
    
    /**
     * API endpoint để lấy danh sách vendor và doanh thu của họ
     */
    @GetMapping("/api/vendor-revenue")
    @ResponseBody
    public Map<String, Object> getVendorRevenueData(
            @RequestParam(value = "type", defaultValue = "month") String type,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "month", required = false) Integer month,
            @RequestParam(value = "quarter", required = false) Integer quarter) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Lấy tất cả đơn hàng đã hoàn thành
            List<Order> allOrders = orderRepository.findAll();
            List<Order> completedOrders = allOrders.stream()
                    .filter(order -> order.getStatus() == Order.OrderStatus.DELIVERED)
                    .collect(java.util.stream.Collectors.toList());
            
            // Gom nhóm theo vendor
            Map<Long, Map<String, Object>> vendorRevenueMap = new HashMap<>();
            
            for (Order order : completedOrders) {
                if (order.getTotalAmount() != null && order.getOrderDate() != null) {
                    // Lấy shop của order (cần implement method này trong OrderService)
                    // Tạm thời sử dụng user của order làm vendor
                    Long vendorId = order.getUser().getUserId();
                    
                    Map<String, Object> vendorStats = vendorRevenueMap.getOrDefault(vendorId, new HashMap<>());
                    
                    // Kiểm tra thời gian theo filter
                    LocalDate orderDate = order.getOrderDate().toLocalDate();
                    boolean includeOrder = false;
                    
                    if (year != null) {
                        if (type.equals("month") && month != null) {
                            includeOrder = orderDate.getYear() == year && orderDate.getMonthValue() == month;
                        } else if (type.equals("quarter") && quarter != null) {
                            int startMonth = (quarter - 1) * 3 + 1;
                            int endMonth = quarter * 3;
                            includeOrder = orderDate.getYear() == year && 
                                         orderDate.getMonthValue() >= startMonth && 
                                         orderDate.getMonthValue() <= endMonth;
                        } else if (type.equals("year")) {
                            includeOrder = orderDate.getYear() == year;
                        }
                    } else {
                        // Nếu không có filter, lấy tất cả
                        includeOrder = true;
                    }
                    
                    if (includeOrder) {
                        double currentRevenue = (Double) vendorStats.getOrDefault("revenue", 0.0);
                        int currentOrders = (Integer) vendorStats.getOrDefault("orderCount", 0);
                        
                        vendorStats.put("vendorId", vendorId);
                        vendorStats.put("vendorName", order.getUser().getName());
                        vendorStats.put("revenue", currentRevenue + order.getTotalAmount());
                        vendorStats.put("orderCount", currentOrders + 1);
                        
                        vendorRevenueMap.put(vendorId, vendorStats);
                    }
                }
            }
            
            // Chuyển thành list và sắp xếp theo doanh thu
            List<Map<String, Object>> vendorList = new java.util.ArrayList<>(vendorRevenueMap.values());
            Collections.sort(vendorList, (v1, v2) -> Double.compare(
                (Double) v2.get("revenue"), 
                (Double) v1.get("revenue")
            ));
            
            // Format revenue
            for (Map<String, Object> vendor : vendorList) {
                double revenue = (Double) vendor.get("revenue");
                vendor.put("formattedRevenue", formatCurrency(revenue));
            }
            
            result.put("vendors", vendorList);
            result.put("totalVendors", vendorList.size());
            
        } catch (Exception e) {
            result.put("error", "Có lỗi xảy ra: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    public RevenueController(RevenueStatisticsService revenueStatisticsService,
                            OrderRepository orderRepository) {
        this.revenueStatisticsService = revenueStatisticsService;
        this.orderRepository = orderRepository;
    }

    /**
     * Revenue Statistics Page - Admin view with vendor breakdown
     */
    @GetMapping("/revenue-statistics")
    public String revenueStatistics(HttpSession session, Model model,
                                   @RequestParam(value = "type", defaultValue = "month") String type,
                                   @RequestParam(value = "year", required = false) Integer year,
                                   @RequestParam(value = "month", required = false) Integer month,
                                   @RequestParam(value = "quarter", required = false) Integer quarter,
                                   @RequestParam(value = "startDate", required = false) String startDate,
                                   @RequestParam(value = "endDate", required = false) String endDate,
                                   @RequestParam(value = "vendorId", required = false) Long vendorId) {
        
        // Authentication check
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        // Xử lý bộ lọc theo khoảng thời gian
        // Commented out unused date parsing until we implement date filtering
        /*
        if (startDate != null && !startDate.isEmpty()) {
            try {
                LocalDate start = LocalDate.parse(startDate);
                // TODO: Use start date for filtering
            } catch (Exception e) {
                System.err.println("Lỗi chuyển đổi ngày bắt đầu: " + e.getMessage());
            }
        }
        
        if (endDate != null && !endDate.isEmpty()) {
            try {
                LocalDate end = LocalDate.parse(endDate);
                // TODO: Use end date for filtering
            } catch (Exception e) {
                System.err.println("Lỗi chuyển đổi ngày kết thúc: " + e.getMessage());
            }
        }
        */
        
        // Truyền giá trị của bộ lọc ra view
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("vendorId", vendorId);
        
        model.addAttribute("user", user);
        
        // Set current values
        LocalDate today = LocalDate.now();
        int currentYear = year != null ? year : today.getYear();
        int currentMonth = month != null ? month : today.getMonthValue();
        int currentQuarter = quarter != null ? quarter : (currentMonth - 1) / 3 + 1;
        
        try {
            // Kiểm tra kết nối cơ sở dữ liệu
            List<Order> allOrders = orderRepository.findAll();
            System.out.println("Kiểm tra số lượng đơn hàng trong cơ sở dữ liệu: " + allOrders.size());
            
            // Current month revenue statistics
            Map<String, Object> currentMonthStats = revenueStatisticsService.getCurrentMonthStatistics();
            model.addAttribute("currentMonthRevenue", currentMonthStats.get("formattedRevenue"));
            model.addAttribute("growthRate", currentMonthStats.get("growthRate"));
            model.addAttribute("isPositiveGrowth", currentMonthStats.get("isPositiveGrowth"));
            
            // Today's statistics
            Map<String, Object> todayStats = revenueStatisticsService.getTodayStatistics();
            model.addAttribute("todayOrders", todayStats.get("formattedOrderCount"));
            model.addAttribute("todayRevenue", todayStats.get("formattedRevenue"));
            
            // Load default data for charts if needed
            // You can add more specific data here if needed
            
            // Selected period info
            model.addAttribute("selectedType", type);
            model.addAttribute("currentYear", currentYear);
            model.addAttribute("currentMonth", currentMonth);
            model.addAttribute("currentQuarter", currentQuarter);
            
            // Selected period statistics
            Map<String, Object> periodStats = revenueStatisticsService.getSelectedPeriodStatistics(
                type, currentYear, currentMonth, currentQuarter);
            System.out.println("CONTROLLER - Period Name: " + periodStats.get("periodName"));
            System.out.println("CONTROLLER - Formatted Revenue: " + periodStats.get("formattedRevenue"));
            
            model.addAttribute("selectedPeriod", periodStats.get("periodName"));
            // Lấy dữ liệu thật từ database, không sử dụng dữ liệu giả
            String revenueDisplay = (String)periodStats.get("formattedRevenue");
            // Nếu không có dữ liệu, hiển thị 0 đ thay vì dữ liệu giả
            if (revenueDisplay == null) {
                revenueDisplay = "0 đ";
            }
            model.addAttribute("selectedRevenue", revenueDisplay);
            
            // Order completion rate statistics
            Map<String, Object> completionStats = revenueStatisticsService.getOrderCompletionStats();
            model.addAttribute("completionRate", completionStats.get("completionRate"));
            model.addAttribute("completionText", completionStats.get("completionText"));
            
            // Thêm dữ liệu tổng số đơn hàng và doanh thu trung bình
            long totalOrderCount = orderRepository.count();
            model.addAttribute("totalOrderCount", totalOrderCount);
            
            // Tính doanh thu trung bình
            double avgRevenue = 0.0;
            if (totalOrderCount > 0) {
                List<Order> orderList = orderRepository.findAll();
                double totalRevenue = 0.0;
                int validOrders = 0;
                for (Order order : orderList) {
                    if (order.getTotalAmount() != null) {
                        totalRevenue += order.getTotalAmount();
                        validOrders++;
                    }
                }
                avgRevenue = validOrders > 0 ? totalRevenue / validOrders : 0.0;
            }
            
            // Format giá trị
            java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
            String formattedAvgRevenue = df.format(avgRevenue) + " đ";
            model.addAttribute("averageRevenue", formattedAvgRevenue);
            System.out.println("Doanh thu trung bình: " + formattedAvgRevenue);
            System.out.println("Tổng số đơn hàng: " + totalOrderCount);
            
            // Top customers and products have been removed from the UI
            
            return "admin/revenue-statistics";
            
        } catch (Exception e) {
            System.err.println("Lỗi trong revenue controller: " + e.getMessage());
            e.printStackTrace();
            
            // Thêm thông báo lỗi cho người dùng
            model.addAttribute("error", "Có lỗi xảy ra khi tải dữ liệu thống kê: " + e.getMessage());
            
            // Đặt các giá trị mặc định để tránh lỗi null
            model.addAttribute("currentMonthRevenue", "0 đ");
            model.addAttribute("growthRate", "0.0");
            model.addAttribute("isPositiveGrowth", true);
            model.addAttribute("todayOrders", "0");
            model.addAttribute("todayRevenue", "0 đ");
            model.addAttribute("selectedType", type);
            model.addAttribute("currentYear", currentYear);
            model.addAttribute("currentMonth", currentMonth);
            model.addAttribute("currentQuarter", currentQuarter);
            model.addAttribute("selectedPeriod", "");
            model.addAttribute("selectedRevenue", "0 đ");
            model.addAttribute("completionRate", "0.0");
            model.addAttribute("completionText", "0/0");
            model.addAttribute("topCustomers", Collections.emptyList());
            model.addAttribute("topProducts", Collections.emptyList());
            
            return "admin/revenue-statistics";
        }
    }
    
    /**
     * Format currency helper method
     */
    private String formatCurrency(double value) {
        if (value == 0.0) {
            return "0 đ";
        }
        java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
        return df.format(value) + " đ";
    }
}