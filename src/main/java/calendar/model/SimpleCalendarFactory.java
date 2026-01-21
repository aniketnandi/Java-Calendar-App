package calendar.model;

import java.time.ZoneId;

/**
 * Default implementation of CalendarFactory.
 * Creates instances of Calendar class.
 * This is the concrete factory that SimpleCalendarManager uses by default.
 */
public class SimpleCalendarFactory implements CalendarFactory {

  /**
   * Creates a new Calendar instance with the specified name and timezone.
   *
   * @param name the calendar name
   * @param timezone the calendar timezone
   * @return a new Calendar instance
   */
  @Override
  public Calendar createCalendar(String name, ZoneId timezone) {
    return new Calendar(name, timezone);
  }
}