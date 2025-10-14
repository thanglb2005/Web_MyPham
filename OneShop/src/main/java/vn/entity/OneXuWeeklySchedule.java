package vn.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "one_xu_weekly_schedule")
public class OneXuWeeklySchedule {
    
    @Id
    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek; // 1=Monday, 2=Tuesday, ..., 7=Sunday
    
    @Column(name = "xu_reward", nullable = false)
    private Double xuReward;
    
    @Column(name = "description")
    private String description;
    
    // Constructors
    public OneXuWeeklySchedule() {}
    
    public OneXuWeeklySchedule(Integer dayOfWeek, Double xuReward, String description) {
        this.dayOfWeek = dayOfWeek;
        this.xuReward = xuReward;
        this.description = description;
    }
    
    // Getters and Setters
    public Integer getDayOfWeek() {
        return dayOfWeek;
    }
    
    public void setDayOfWeek(Integer dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }
    
    public Double getXuReward() {
        return xuReward;
    }
    
    public void setXuReward(Double xuReward) {
        this.xuReward = xuReward;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    // Helper methods
    public String getDayName() {
        switch (dayOfWeek) {
            case 1: return "Thứ 2";
            case 2: return "Thứ 3";
            case 3: return "Thứ 4";
            case 4: return "Thứ 5";
            case 5: return "Thứ 6";
            case 6: return "Thứ 7";
            case 7: return "Chủ nhật";
            default: return "Không xác định";
        }
    }
}
