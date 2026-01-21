package analytics;

import calendar.controller.Features;
import calendar.model.CalendarAnalyticsSummary;
import calendar.model.Event;
import calendar.view.CalendarEditData;
import calendar.view.CalendarGuiView;
import calendar.view.EventDeleteData;
import calendar.view.EventEditData;
import calendar.view.EventFormData;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Shared test stub implementation of CalendarGuiView for testing purposes.
 * Captures method calls and their parameters for verification in tests.
 * This class can be used across multiple test files to eliminate duplication.
 */
public class TestGuiViewStub implements CalendarGuiView {
  public boolean dashboardDisplayed = false;
  public CalendarAnalyticsSummary displayedSummary = null;
  public LocalDate displayedStartDate = null;
  public LocalDate displayedEndDate = null;
  public String lastError = null;
  public String lastMessage = null;

  @Override
  public void displayDashboard(CalendarAnalyticsSummary summary,
                               LocalDate startDate, LocalDate endDate) {
    this.dashboardDisplayed = true;
    this.displayedSummary = summary;
    this.displayedStartDate = startDate;
    this.displayedEndDate = endDate;
  }

  @Override
  public void displayError(String error) {
    this.lastError = error;
  }

  @Override
  public void displayMessage(String message) {
    this.lastMessage = message;
  }

  @Override
  public void addFeaturesListener(Features features) {
  }

  @Override
  public void setCurrentCalendar(String calendarName) {
  }

  @Override
  public void setEventsForDate(LocalDate date, List<Event> events) {
  }

  @Override
  public void setMonthEvents(Map<LocalDate, List<Event>> eventsByDate) {
  }

  @Override
  public void display() {
  }

  @Override
  public String promptCalendarName() {
    return null;
  }

  @Override
  public String promptTimezone() {
    return null;
  }

  @Override
  public String promptSelectCalendar(List<String> calendarNames) {
    return null;
  }

  @Override
  public EventFormData showEventCreationDialog(LocalDate date) {
    return null;
  }

  @Override
  public EventEditData showEventEditDialog(LocalDate date, List<Event> events) {
    return null;
  }

  @Override
  public CalendarEditData showCalendarEditDialog(String currentName, String currentTimezone) {
    return null;
  }

  @Override
  public EventDeleteData showEventDeleteDialog(LocalDate date, List<Event> events) {
    return null;
  }

  @Override
  public void displayEvents(List<Event> events) {
  }

  @Override
  public void displayStatus(boolean isBusy) {
  }
}