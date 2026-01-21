package calendar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Interface for managing multiple calendars.
 * Provides operations to create, edit, and switch between calendars,
 * as well as copy events between calendars.
 */
public interface CalendarManager {

  /**
   * Creates a new calendar with the specified name and timezone.
   *
   * @param name the unique name for the calendar
   * @param timezone the timezone for the calendar (IANA format)
   * @throws IllegalArgumentException if name already exists or is invalid,
   *                                  or if timezone is invalid
   */
  void createCalendar(String name, ZoneId timezone) throws IllegalArgumentException;

  /**
   * Edits a property of an existing calendar.
   *
   * @param calendarName the name of the calendar to edit
   * @param property the property to edit ("name" or "timezone")
   * @param value the new value for the property
   * @throws IllegalArgumentException if calendar doesn't exist, property is invalid,
   *                                  or new value is invalid
   */
  void editCalendar(String calendarName, String property, Object value)
      throws IllegalArgumentException;

  /**
   * Sets the current active calendar for operations.
   *
   * @param calendarName the name of the calendar to use
   * @throws IllegalArgumentException if calendar doesn't exist
   */
  void useCalendar(String calendarName) throws IllegalArgumentException;

  /**
   * Gets the currently active calendar.
   *
   * @return the current calendar, or null if no calendar is active
   */
  Calendar getCurrentCalendar();

  /**
   * Gets a calendar by name.
   *
   * @param name the name of the calendar
   * @return the calendar with the specified name
   * @throws IllegalArgumentException if calendar doesn't exist
   */
  Calendar getCalendar(String name) throws IllegalArgumentException;

  /**
   * Copies a single event from the current calendar to a target calendar.
   *
   * @param eventSubject the subject of the event to copy
   * @param eventStartDateTime the start date/time of the event to copy
   * @param targetCalendarName the name of the target calendar
   * @param targetStartDateTime the start date/time in the target calendar
   * @throws IllegalArgumentException if calendars don't exist, event not found,
   *                                  or copy would create conflicts
   */
  void copyEvent(String eventSubject, LocalDateTime eventStartDateTime,
                 String targetCalendarName, LocalDateTime targetStartDateTime)
      throws IllegalArgumentException;

  /**
   * Copies all events on a specific date from the current calendar to a target calendar.
   *
   * @param sourceDate the date to copy events from
   * @param targetCalendarName the name of the target calendar
   * @param targetDate the date in the target calendar
   * @throws IllegalArgumentException if calendars don't exist or copy would create conflicts
   */
  void copyEventsOnDate(LocalDate sourceDate, String targetCalendarName, LocalDate targetDate)
      throws IllegalArgumentException;

  /**
   * Copies all events within a date range from the current calendar to a target calendar.
   *
   * @param startDate the start of the range (inclusive)
   * @param endDate the end of the range (inclusive)
   * @param targetCalendarName the name of the target calendar
   * @param targetStartDate the start date in the target calendar
   * @throws IllegalArgumentException if calendars don't exist or copy would create conflicts
   */
  void copyEventsBetween(LocalDate startDate, LocalDate endDate,
                         String targetCalendarName, LocalDate targetStartDate)
      throws IllegalArgumentException;

  /**
   * Checks if a calendar with the given name exists.
   *
   * @param name the calendar name to check
   * @return true if a calendar with this name exists, false otherwise
   */
  boolean hasCalendar(String name);

  /**
   * Gets a list of all calendar names.
   *
   * @return list of calendar names
   */
  List<String> getAllCalendarNames();

  /**
   * Adds an event to the current calendar.
   *
   * @param event the event to add
   * @throws IllegalStateException if no calendar is currently in use
   * @throws IllegalArgumentException if event already exists
   */
  void addEvent(Event event) throws IllegalStateException, IllegalArgumentException;

  /**
   * Adds an event series to the current calendar.
   *
   * @param eventSeries the event series to add
   * @throws IllegalStateException if no calendar is currently in use
   * @throws IllegalArgumentException if any event in series already exists
   */
  void addEventSeries(EventSeries eventSeries) throws IllegalStateException,
      IllegalArgumentException;

  /**
   * Gets events on a specific date from the current calendar.
   *
   * @param date the date to query
   * @return list of events on that date
   * @throws IllegalStateException if no calendar is currently in use
   */
  List<Event> getEventsOn(LocalDate date) throws IllegalStateException;

  /**
   * Gets events in a time range from the current calendar.
   *
   * @param start the start of the range
   * @param end the end of the range
   * @return list of events in that range
   * @throws IllegalStateException if no calendar is currently in use
   */
  List<Event> getEventsInRange(LocalDateTime start, LocalDateTime end) throws IllegalStateException;

  /**
   * Removes an event from the current calendar.
   *
   * @param event the event to remove
   * @throws IllegalStateException if no calendar is currently in use
   */
  void removeEvent(Event event) throws IllegalStateException;

  /**
   * Removes an event and all future events in its series from the current calendar.
   *
   * @param event the event to remove from
   * @throws IllegalStateException if no calendar is currently in use
   */
  void removeEventFromSeries(Event event) throws IllegalStateException;

  /**
   * Removes all events in a series from the current calendar.
   *
   * @param event an event in the series to remove
   * @throws IllegalStateException if no calendar is currently in use
   */
  void removeAllEventsInSeries(Event event) throws IllegalStateException;

  /**
   * Edits an event in the current calendar.
   *
   * @param subject the subject of the event
   * @param start start date/time
   * @param end end date/time
   * @param property the property to modify
   * @param value the new value
   * @throws IllegalStateException if no calendar is currently in use
   * @throws IllegalArgumentException if event not found or edit creates duplicate
   */
  void editEvent(String subject, LocalDateTime start, LocalDateTime end,
                 String property, Object value)
      throws IllegalStateException, IllegalArgumentException;

  /**
   * Edits events from a point onwards in a series in the current calendar.
   *
   * @param subject the subject of the event
   * @param start start date/time
   * @param end end date/time
   * @param property the property to modify
   * @param value the new value
   * @throws IllegalStateException if no calendar is currently in use
   * @throws IllegalArgumentException if event not found or edit creates duplicate
   */
  void editEventsFrom(String subject, LocalDateTime start, LocalDateTime end,
                      String property, Object value)
      throws IllegalStateException, IllegalArgumentException;

  /**
   * Edits all events in a series in the current calendar.
   *
   * @param subject the subject of the event
   * @param start start date/time
   * @param end end date/time
   * @param property the property to modify
   * @param value the new value
   * @throws IllegalStateException if no calendar is currently in use
   * @throws IllegalArgumentException if event not found or edit creates duplicate
   */
  void editAllEventsInSeries(String subject, LocalDateTime start, LocalDateTime end,
                             String property, Object value)
      throws IllegalStateException, IllegalArgumentException;
}