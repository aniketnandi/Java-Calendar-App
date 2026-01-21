package calendar.model;

import java.time.ZoneId;

/**
 * Factory interface for creating Calendar instances.
 * Allows SimpleCalendarManager to depend on abstraction rather than concrete class.
 * Follows Dependency Inversion Principle.
 */
public interface CalendarFactory {
  /**
   * Creates a new Calendar with the specified name and timezone.
   *
   * @param name the calendar name
   * @param timezone the calendar timezone
   * @return a new Calendar instance
   */
  Calendar createCalendar(String name, ZoneId timezone);
}