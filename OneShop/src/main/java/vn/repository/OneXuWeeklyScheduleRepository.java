package vn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.entity.OneXuWeeklySchedule;

import java.util.List;

@Repository
public interface OneXuWeeklyScheduleRepository extends JpaRepository<OneXuWeeklySchedule, Integer> {
    
    // Lấy tất cả lịch trình theo thứ tự ngày trong tuần
    List<OneXuWeeklySchedule> findAllByOrderByDayOfWeek();
    
    // Lấy phần thưởng theo ngày trong tuần
    OneXuWeeklySchedule findByDayOfWeek(Integer dayOfWeek);
}
