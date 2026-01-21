package calendar.view;

import calendar.controller.Features;
import calendar.model.CalendarAnalyticsSummary;
import calendar.model.Event;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Interface for graphical calendar view operations.
 * Extends CalendarView to add GUI-specific functionality.
 */
public interface CalendarGuiView extends CalendarView {

  /**
   * Registers a Features listener to handle high-level user interactions.
   * The view will call methods on this listener when users perform actions.
   *
   * @param features the features handler
   */
  void addFeaturesListener(Features features);

  /**
   * Updates the display to show which calendar is currently active.
   *
   * @param calendarName the name of the active calendar
   */
  void setCurrentCalendar(String calendarName);

  /**
   * Updates the events displayed for a specific date.
   *
   * @param date the date
   * @param events the events on that date
   */
  void setEventsForDate(LocalDate date, List<Event> events);

  /**
   * Updates the entire month's events for visual indicators on the calendar grid.
   *
   * @param eventsByDate map of dates to their events
   */
  void setMonthEvents(Map<LocalDate, List<Event>> eventsByDate);

  /**
   * Makes the GUI visible to the user.
   */
  void display();

  /**
   * Prompts user to input a calendar name.
   *
   * @return the entered name, or null if cancelled
   */
  String promptCalendarName();

  /**
   * Prompts user to select a timezone.
   *
   * @return the selected timezone string, or null if cancelled
   */
  String promptTimezone();

  /**
   * Prompts user to select a calendar from available options.
   *
   * @param calendarNames list of available calendar names
   * @return the selected calendar name, or null if cancelled
   */
  String promptSelectCalendar(List<String> calendarNames);

  /**
   * Shows an event creation dialog for the specified date.
   * Returns the created event details, or null if cancelled.
   *
   * @param date the date for the new event
   * @return EventFormData containing user input, or null if cancelled
   */
  EventFormData showEventCreationDialog(LocalDate date);

  /**
   * Shows an event editing dialog for events on the specified date.
   *
   * @param date the date of events to edit
   * @param events the events on that date
   * @return EventEditData containing edit information, or null if cancelled
   */
  EventEditData showEventEditDialog(LocalDate date, List<Event> events);

  /**
   * Shows a calendar editing dialog.
   *
   * @param currentName the current calendar name
   * @param currentTimezone the current timezone
   * @return CalendarEditData containing edit information, or null if cancelled
   */
  CalendarEditData showCalendarEditDialog(String currentName, String currentTimezone);

  /**
   * Displays a read-only analytics dashboard for a given date interval.
   *
   * <p>The controller calls this method after computing analytics in the model.
   * Implementations are responsible for formatting the contents of
   * {@code summary} (such as totals, breakdowns, busiest/least busy days,
   * and online vs. offline counts) in a user-friendly way, for example using
   * a dialog or dedicated panel.
   *
   * <p>This method must not modify the underlying calendar data; it is purely
   * for visualization of pre-computed metrics.
   *
   * @param summary the {@link CalendarAnalyticsSummary} produced by the model;
   *                  may be {@code null} if no analytics are available
   * @param startDate the first date (inclusive) of the interval for which analytics
   *                  were computed; used for labeling; must not be {@code null}
   * @param endDate the last date (inclusive) of the interval for which analytics
   *                  were computed; used for labeling; must not be {@code null}
   */
  void displayDashboard(CalendarAnalyticsSummary summary,
                        LocalDate startDate,
                        LocalDate endDate);

  /**
   * Shows an event deletion dialog for events on the specified date.
   *
   * @param date the date of events to delete
   * @param events the events on that date
   * @return EventDeleteData containing delete information, or null if cancelled
   */
  EventDeleteData showEventDeleteDialog(LocalDate date, List<Event> events);
}