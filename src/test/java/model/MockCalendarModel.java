package model;

import calendar.model.CalendarAnalyticsSummary;
import calendar.model.CalendarModel;
import calendar.model.Event;
import calendar.model.EventSeries;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Mock implementation of CalendarModel for testing purposes.
 * Logs all method calls to verify controller behavior.
 */
public class MockCalendarModel implements CalendarModel {
  private final StringBuilder log;
  private final List<Event> mockEvents;
  private final boolean mockIsBusy;
  private final String mockExportPath;

  /**
   * Creates a MockCalendarModel with a log to record interactions.
   *
   * @param log StringBuilder to record method calls
   */
  public MockCalendarModel(StringBuilder log) {
    this(log, new ArrayList<>(), false, "/mock/path/export.csv");
  }

  /**
   * Creates a MockCalendarModel with custom return values.
   *
   * @param log StringBuilder to record method calls
   * @param mockEvents list of events to return from queries
   * @param mockIsBusy boolean to return from isBusy
   * @param mockExportPath path to return from exportToCsv
   */
  public MockCalendarModel(StringBuilder log, List<Event> mockEvents,
                           boolean mockIsBusy, String mockExportPath) {
    this.log = log;
    this.mockEvents = mockEvents;
    this.mockIsBusy = mockIsBusy;
    this.mockExportPath = mockExportPath;
  }

  @Override
  public void addEvent(Event event) throws IllegalArgumentException {
    log.append("addEvent called with: subject=").append(event.getSubject())
        .append(", start=").append(event.getStartDateTime())
        .append(", end=").append(event.getEndDateTime()).append("\n");
  }

  @Override
  public void addEventSeries(EventSeries eventSeries) throws IllegalArgumentException {
    log.append("addEventSeries called with: subject=").append(eventSeries.getSubject())
        .append(", events count=").append(eventSeries.getAllEvents().size()).append("\n");
  }

  @Override
  public void removeEvent(Event event) {
    log.append("removeEvent called with: subject=").append(event.getSubject()).append("\n");
  }

  @Override
  public void editEvent(String subject, LocalDateTime start, LocalDateTime end,
                        String property, Object value) throws IllegalArgumentException {
    log.append("editEvent called with: subject=").append(subject)
        .append(", start=").append(start)
        .append(", end=").append(end)
        .append(", property=").append(property)
        .append(", value=").append(value).append("\n");
  }

  @Override
  public void editEventsFrom(String subject, LocalDateTime start, LocalDateTime end,
                             String property, Object value) throws IllegalArgumentException {
    log.append("editEventsFrom called with: subject=").append(subject)
        .append(", start=").append(start)
        .append(", end=").append(end)
        .append(", property=").append(property)
        .append(", value=").append(value).append("\n");
  }

  @Override
  public void editAllEventsInSeries(String subject, LocalDateTime start, LocalDateTime end,
                                    String property, Object value) throws IllegalArgumentException {
    log.append("editAllEventsInSeries called with: subject=").append(subject)
        .append(", start=").append(start)
        .append(", end=").append(end)
        .append(", property=").append(property)
        .append(", value=").append(value).append("\n");
  }

  @Override
  public List<Event> getEventsOn(LocalDate date) {
    log.append("getEventsOn called with: date=").append(date).append("\n");
    return new ArrayList<>(mockEvents);
  }

  @Override
  public List<Event> getEventsInRange(LocalDateTime start, LocalDateTime end) {
    log.append("getEventsInRange called with: start=").append(start)
        .append(", end=").append(end).append("\n");
    return new ArrayList<>(mockEvents);
  }

  @Override
  public CalendarAnalyticsSummary generateAnalytics(LocalDate startDate, LocalDate endDate) {
    return CalendarAnalyticsSummary.builder()
        .totalEvents(0)
        .build();
  }

  @Override
  public boolean isBusy(LocalDateTime dateTime) {
    log.append("isBusy called with: dateTime=").append(dateTime).append("\n");
    return mockIsBusy;
  }

  @Override
  public String exportToCsv(String fileName) throws IOException {
    log.append("exportToCsv called with: fileName=").append(fileName).append("\n");
    return mockExportPath;
  }

  /**
   * Gets the log of all method calls.
   *
   * @return the log as a string
   */
  public String getLog() {
    return log.toString();
  }

  @Override
  public String exportToIcal(String fileName, ZoneId timezone) throws IOException {
    log.append("exportToICal called with: fileName=").append(fileName)
        .append(", timezone=").append(timezone).append("\n");
    return mockExportPath;
  }

  @Override
  public void removeEventFromSeries(Event event) {
    log.append("removeEventFromSeries called with: subject=").append(event.getSubject())
        .append(", start=").append(event.getStartDateTime())
        .append(", end=").append(event.getEndDateTime()).append("\n");
  }

  @Override
  public void removeAllEventsInSeries(Event event) {
    log.append("removeAllEventInSeries called with: subject=").append(event.getSubject())
        .append(", start=").append(event.getStartDateTime())
        .append(", end=").append(event.getEndDateTime()).append("\n");
  }
}