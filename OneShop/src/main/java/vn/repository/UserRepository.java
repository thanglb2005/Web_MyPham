package vn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByStatus(Boolean status);
    boolean existsByEmail(String email);
    
    // Tìm kiếm user theo tên hoặc email
    List<User> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email);
    
    // ========== CUSTOMER STATISTICS QUERIES ==========
    
    /**
     * Get user registration statistics by month
     * Returns: year, month, user_count
     */
    @Query(value = "SELECT YEAR(register_date) as year, " +
            "MONTH(register_date) as month, " +
            "COUNT(user_id) as user_count " +
            "FROM [user] " +
            "GROUP BY YEAR(register_date), MONTH(register_date) " +
            "ORDER BY year DESC, month DESC", nativeQuery = true)
    List<Object[]> getUserRegistrationByMonth();
    
    /**
     * Get user registration statistics by year
     * Returns: year, user_count
     */
    @Query(value = "SELECT YEAR(register_date) as year, " +
            "COUNT(user_id) as user_count " +
            "FROM [user] " +
            "GROUP BY YEAR(register_date) " +
            "ORDER BY year DESC", nativeQuery = true)
    List<Object[]> getUserRegistrationByYear();
    
    /**
     * Get user status statistics
     * Returns: status, status_name, user_count
     */
    @Query(value = "SELECT status, " +
            "CASE " +
            "WHEN status = 1 THEN 'Hoạt động' " +
            "WHEN status = 0 THEN 'Không hoạt động' " +
            "ELSE 'Không xác định' " +
            "END as status_name, " +
            "COUNT(user_id) as user_count " +
            "FROM [user] " +
            "GROUP BY status", nativeQuery = true)
    List<Object[]> getUserStatusStatistics();
    
    /**
     * Get user registration by day (last 30 days)
     * Returns: register_date, user_count
     */
    @Query(value = "SELECT CONVERT(DATE, register_date) as register_date, " +
            "COUNT(user_id) as user_count " +
            "FROM [user] " +
            "WHERE register_date >= DATEADD(day, -30, GETDATE()) " +
            "GROUP BY CONVERT(DATE, register_date) " +
            "ORDER BY register_date DESC", nativeQuery = true)
    List<Object[]> getUserRegistrationLast30Days();
    
    /**
     * Get user activity statistics
     * Returns: total_users, active_users, inactive_users, new_users_today, new_users_this_month
     */
    @Query(value = "SELECT " +
            "COUNT(*) as total_users, " +
            "COUNT(CASE WHEN status = 1 THEN 1 END) as active_users, " +
            "COUNT(CASE WHEN status = 0 THEN 1 END) as inactive_users, " +
            "COUNT(CASE WHEN CONVERT(DATE, register_date) = CONVERT(DATE, GETDATE()) THEN 1 END) as new_users_today, " +
            "COUNT(CASE WHEN register_date >= DATEADD(month, -1, GETDATE()) THEN 1 END) as new_users_this_month " +
            "FROM [user]", nativeQuery = true)
    List<Object[]> getUserActivityStatistics();
    
    /**
     * Get user registration trend (last 12 months)
     * Returns: year, month, user_count
     */
    @Query(value = "SELECT YEAR(register_date) as year, " +
            "MONTH(register_date) as month, " +
            "COUNT(user_id) as user_count " +
            "FROM [user] " +
            "WHERE register_date >= DATEADD(month, -12, GETDATE()) " +
            "GROUP BY YEAR(register_date), MONTH(register_date) " +
            "ORDER BY year DESC, month DESC", nativeQuery = true)
    List<Object[]> getUserRegistrationTrend();
    
    /**
     * Get top users by registration date (newest first)
     * Returns: user_id, name, email, register_date, status
     */
    @Query(value = "SELECT TOP 10 user_id, name, email, register_date, status " +
            "FROM [user] " +
            "ORDER BY register_date DESC", nativeQuery = true)
    List<Object[]> getNewestUsers();
    
    /**
     * Get user distribution by registration period
     * Returns: period, user_count
     */
    @Query(value = "SELECT " +
            "CASE " +
            "WHEN register_date >= DATEADD(day, -7, GETDATE()) THEN 'Tuần này' " +
            "WHEN register_date >= DATEADD(day, -30, GETDATE()) THEN 'Tháng này' " +
            "WHEN register_date >= DATEADD(day, -90, GETDATE()) THEN '3 tháng gần đây' " +
            "WHEN register_date >= DATEADD(day, -365, GETDATE()) THEN 'Năm này' " +
            "ELSE 'Trước đó' " +
            "END as period, " +
            "COUNT(user_id) as user_count " +
            "FROM [user] " +
            "GROUP BY CASE " +
            "WHEN register_date >= DATEADD(day, -7, GETDATE()) THEN 'Tuần này' " +
            "WHEN register_date >= DATEADD(day, -30, GETDATE()) THEN 'Tháng này' " +
            "WHEN register_date >= DATEADD(day, -90, GETDATE()) THEN '3 tháng gần đây' " +
            "WHEN register_date >= DATEADD(day, -365, GETDATE()) THEN 'Năm này' " +
            "ELSE 'Trước đó' " +
            "END " +
            "ORDER BY user_count DESC", nativeQuery = true)
    List<Object[]> getUserDistributionByPeriod();
}
