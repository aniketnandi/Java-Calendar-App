package controller;

import calendar.controller.Features;
import calendar.model.CalendarAnalyticsSummary;
import calendar.model.Event;
import calendar.view.CalendarEditData;
import calendar.view.CalendarGuiView;
import calendar.view.EventDeleteData;
import calendar.view.EventEditData;
import calendar.view.EventFormData;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Mock implementation of CalendarGuiView for testing GuiController.
 * Logs all method calls and allows setting return values for prompts.
 */
public class MockCalendarGuiView implements CalendarGuiView {
  private final StringBuilder log;
  private String calendarNameInput;
  private String timezoneInput;
  private String calendarSelectionInput;
  private EventFormData eventFormInput;
  private EventEditData eventEditInput;
  private EventDeleteData eventDeleteInput;
  private CalendarEditData calendarEditInput;
  private List<Event> lastEventsForDate;

  /**
   * Creates a MockCalendarGuiView with an internal log.
   */
  public MockCalendarGuiView() {
    this.log = new StringBuilder();
  }

  @Override
  public void addFeaturesListener(Features features) {
    log.append("addFeaturesListener called\n");
  }

  @Override
  public void setCurrentCalendar(String calendarName) {
    log.append("setCurrentCalendar: ").append(calendarName).append("\n");
  }

  @Override
  public void setEventsForDate(LocalDate date, List<Event> events) {
    log.append("setEventsForDate: ").append(date)
        .append(" with ").append(events.size()).append(" events\n");
    this.lastEventsForDate = new ArrayList<>(events);
  }

  public List<Event> getLastEventsForDate() {
    return lastEventsForDate;
  }

  @Override
  public void setMonthEvents(Map<LocalDate, List<Event>> eventsByDate) {
    log.append("setMonthEvents called with ").append(eventsByDate.size())
        .append(" dates\n");
  }

  @Override
  public void displayDashboard(CalendarAnalyticsSummary summary,
                               LocalDate startDate,
                               LocalDate endDate) {
    System.out.println("Mock displayDashboard called");
  }

  @Override
  public void display() {
    log.append("display called\n");
  }

  @Override
  public String promptCalendarName() {
    log.append("promptCalendarName called\n");
    return calendarNameInput;
  }

  @Override
  public String promptTimezone() {
    log.append("promptTimezone called\n");
    return timezoneInput;
  }

  @Override
  public String promptSelectCalendar(List<String> calendarNames) {
    log.append("promptSelectCalendar called with ")
        .append(calendarNames.size()).append(" calendars\n");
    return calendarSelectionInput;
  }

  @Override
  public EventFormData showEventCreationDialog(LocalDate date) {
    log.append("showEventCreationDialog called for date: ").append(date).append("\n");
    return eventFormInput;
  }

  @Override
  public EventEditData showEventEditDialog(LocalDate date, List<Event> events) {
    log.append("showEventEditDialog called for date: ").append(date)
        .append(" with ").append(events.size()).append(" events\n");
    return eventEditInput;
  }

  @Override
  public CalendarEditData showCalendarEditDialog(String currentName, String currentTimezone) {
    log.append("showCalendarEditDialog called for: ").append(currentName).append("\n");
    return calendarEditInput;
  }

  @Override
  public EventDeleteData showEventDeleteDialog(LocalDate date, List<Event> events) {
    log.append("showEventDeleteDialog called for date: ").append(date)
        .append(" with ").append(events.size()).append(" events\n");
    return eventDeleteInput;
  }

  @Override
  public void displayMessage(String message) {
    log.append("displayMessage: ").append(message).append("\n");
  }

  @Override
  public void displayError(String error) {
    log.append("displayError: ").append(error).append("\n");
  }

  @Override
  public void displayEvents(List<Event> events) {
    log.append("displayEvents called with ").append(events.size()).append(" events\n");
  }

  @Override
  public void displayStatus(boolean isBusy) {
    log.append("displayStatus: ").append(isBusy ? "Busy" : "Available").append("\n");
  }

  /**
   * Gets the log of all method calls.
   *
   * @return the log as a string
   */
  public String getLog() {
    return log.toString();
  }

  /**
   * Clears the log.
   */
  public void clearLog() {
    log.setLength(0);
  }

  /**
   * Sets the calendar name to be returned by promptCalendarName.
   *
   * @param name the calendar name
   */
  public void setCalendarNameInput(String name) {
    this.calendarNameInput = name;
  }

  /**
   * Sets the timezone to be returned by promptTimezone.
   *
   * @param timezone the timezone string
   */
  public void setTimezoneInput(String timezone) {
    this.timezoneInput = timezone;
  }

  /**
   * Sets the calendar selection to be returned by promptSelectCalendar.
   *
   * @param selection the selected calendar name
   */
  public void setCalendarSelectionInput(String selection) {
    this.calendarSelectionInput = selection;
  }

  /**
   * Sets the event form data to be returned by showEventCreationDialog.
   *
   * @param formData the event form data
   */
  public void setEventFormInput(EventFormData formData) {
    this.eventFormInput = formData;
  }

  /**
   * Sets the event edit data to be returned by showEventEditDialog.
   *
   * @param editData the event edit data
   */
  public void setEventEditInput(EventEditData editData) {
    this.eventEditInput = editData;
  }

  /**
   * Sets the event delete data to be returned by showEventDeleteDialog.
   *
   * @param deleteData the event delete data
   */
  public void setEventDeleteInput(EventDeleteData deleteData) {
    this.eventDeleteInput = deleteData;
  }

  /**
   * Sets the calendar edit data to be returned by showCalendarEditDialog.
   *
   * @param editData the calendar edit data
   */
  public void setCalendarEditInput(CalendarEditData editData) {
    this.calendarEditInput = editData;
  }
}
