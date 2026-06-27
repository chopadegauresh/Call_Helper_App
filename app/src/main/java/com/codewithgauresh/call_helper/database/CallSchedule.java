package com.codewithgauresh.call_helper.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "call_schedules")
public class CallSchedule {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String phoneNumber;
    private String contactName;
    private long scheduledTime;
    private String ringtoneUri;
    private boolean isEnabled;
    private String notes;
    private boolean isRecurring;
    private String recurringDays; // e.g., "1,2,3" for Mon, Tue, Wed
    private boolean useSpeakerphone;
    private boolean isCompleted;
    private String status; // "Snoozed", "Cancelled", "Completed", "Pending"
    private long actualCallTime;
    private int snoozeCount;

    public CallSchedule(String phoneNumber, String contactName, long scheduledTime, String ringtoneUri, boolean isEnabled) {
        this.phoneNumber = phoneNumber;
        this.contactName = contactName;
        this.scheduledTime = scheduledTime;
        this.ringtoneUri = ringtoneUri;
        this.isEnabled = isEnabled;
        this.notes = "";
        this.isRecurring = false;
        this.useSpeakerphone = false;
        this.isCompleted = false;
        this.status = "Pending";
        this.snoozeCount = 0;
    }

    // Getters and Setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getActualCallTime() { return actualCallTime; }
    public void setActualCallTime(long actualCallTime) { this.actualCallTime = actualCallTime; }
    public int getSnoozeCount() { return snoozeCount; }
    public void setSnoozeCount(int snoozeCount) { this.snoozeCount = snoozeCount; }

    // Getters and Setters for new fields
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public boolean isRecurring() { return isRecurring; }
    public void setRecurring(boolean recurring) { isRecurring = recurring; }
    public String getRecurringDays() { return recurringDays; }
    public void setRecurringDays(String recurringDays) { this.recurringDays = recurringDays; }
    public boolean isUseSpeakerphone() { return useSpeakerphone; }
    public void setUseSpeakerphone(boolean useSpeakerphone) { this.useSpeakerphone = useSpeakerphone; }
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public long getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(long scheduledTime) { this.scheduledTime = scheduledTime; }
    public String getRingtoneUri() { return ringtoneUri; }
    public void setRingtoneUri(String ringtoneUri) { this.ringtoneUri = ringtoneUri; }
    public boolean isEnabled() { return isEnabled; }
    public void setEnabled(boolean enabled) { isEnabled = enabled; }
}
