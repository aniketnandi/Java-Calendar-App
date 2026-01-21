package calendar.view;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

/**
 * Immutable data class representing event creation form input.
 * Used to transfer data from view to controller
 */
public final class EventFormData {
  private final String subject;
  private final LocalDate startDate;
  private final LocalDate endDate;
  private final boolean allDay;
  private final LocalTime startTime;
  private final LocalTime endTime;
  private final String location;
  private final String description;
  private final String status;
  private final boolean recurring;
  private final Set<DayOfWeek> weekdays;
  private final boolean useCount;
  private final int repeatCount;
  private final LocalDate repeatUntil;

  /**
   * Creates a new EventFormData with all fields.
   */
  public EventFormData(String subject, LocalDate startDate, LocalDate endDate, boolean allDay,
                       LocalTime startTime, LocalTime endTime,
                       String location, String description, String status,
                       boolean recurring, Set<DayOfWeek> weekdays,
                       boolean useCount, int repeatCount, LocalDate repeatUntil) {
    this.subject = subject;
    this.startDate = startDate;
    this.endDate = endDate;
    this.allDay = allDay;
    this.startTime = startTime;
    this.endTime = endTime;
    this.location = location;
    this.description = description;
    this.status = status;
    this.recurring = recurring;
    this.weekdays = weekdays;
    this.useCount = useCount;
    this.repeatCount = repeatCount;
    this.repeatUntil = repeatUntil;
  }

  public String getSubject() {
    return subject;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

  public boolean isAllDay() {
    return allDay;
  }

  public LocalTime getStartTime() {
    return startTime;
  }

  public LocalTime getEndTime() {
    return endTime;
  }

  public String getLocation() {
    return location;
  }

  public String getDescription() {
    return description;
  }

  public String getStatus() {
    return status;
  }

  public boolean isRecurring() {
    return recurring;
  }

  public Set<DayOfWeek> getWeekdays() {
    return weekdays;
  }

  public boolean isUseCount() {
    return useCount;
  }

  public int getRepeatCount() {
    return repeatCount;
  }

  public LocalDate getRepeatUntil() {
    return repeatUntil;
  }
}