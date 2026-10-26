package com.smartkitchen.repository;

import com.smartkitchen.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    /** The 8 most recent activities, newest first. */
    List<ActivityLog> findTop8ByOrderByCreatedAtDescIdDesc();
}
