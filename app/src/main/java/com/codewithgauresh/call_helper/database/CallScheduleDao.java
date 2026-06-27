package com.codewithgauresh.call_helper.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CallScheduleDao {
    @Insert
    long insert(CallSchedule schedule);

    @Update
    void update(CallSchedule schedule);

    @Delete
    void delete(CallSchedule schedule);

    @Query("SELECT * FROM call_schedules WHERE isCompleted = 0 ORDER BY scheduledTime ASC")
    LiveData<List<CallSchedule>> getAllSchedules();

    @Query("SELECT * FROM call_schedules WHERE isCompleted = 1 ORDER BY scheduledTime DESC")
    LiveData<List<CallSchedule>> getCallHistory();

    @Query("SELECT * FROM call_schedules WHERE isCompleted = 0 ORDER BY scheduledTime ASC")
    List<CallSchedule> getAllSchedulesDirect();

    @Query("SELECT * FROM call_schedules WHERE id = :id")
    CallSchedule getScheduleById(int id);

    @Query("DELETE FROM call_schedules WHERE isCompleted = 1")
    void deleteAllHistory();
}
