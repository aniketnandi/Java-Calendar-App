package calendar.controller;

import java.time.LocalDate;

/**
 * Feature interface for calendar GUI operations.
 * Decoupling the view from Swing-specific events and providing application-level callbacks.
 */
public interface Features {

  /**
   * User requests to create a new calendar.
   */
  void createCalendar();

  /**
   * User requests to switch to a different calendar.
   */
  void switchCalendar();

  /**
   * User requests to edit the current calendar's properties.
   */
  void editCalendar();

  /**
   * User requests to create a new event on the specified date.
   *
   * @param date the date for the new event
   */
  void createEvent(LocalDate date);

  /**
   * User requests to edit events on the specified date.
   *
   * @param date the date containing events to edit
   */
  void editEvents(LocalDate date);

  /**
   * User has selected a different date in the calendar view.
   *
   * @param date the newly selected date
   */
  void dateSelected(LocalDate date);

  /**
   * User has navigated to a different month.
   *
   * @param year the year of the new month
   * @param month the month number (1-12)
   */
  void monthChanged(int year, int month);

  /**
   * User requests to delete an event on the specified date.
   *
   * @param date the date containing events to delete
   */
  void deleteEvent(LocalDate date);

  /**
   * User requests to view a dashboard for a date interval.
   *
   * @param start start date (inclusive)
   * @param end end date (inclusive)
   */
  void showDashboard(LocalDate start, LocalDate end);
}