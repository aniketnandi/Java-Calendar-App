package analytics;

import calendar.model.CalendarAnalyticsSummary;
import calendar.model.CalendarModel;
import calendar.model.Event;
import calendar.model.EventSeries;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Shared mock implementation of CalendarModel for testing purposes.
 * Provides empty implementations of all methods with a default behavior
 * of throwing IllegalStateException for generateAnalytics to simulate
 * a model with no active calendar. Can be extended to override specific
 * methods for different test scenarios.
 */
public class TestModelStub implements CalendarModel {

  @Override
  public void addEvent(Event event) {
  }

  @Override
  public void removeEvent(Event event) {
  }

  @Override
  public void addEventSeries(EventSeries eventSeries) {
  }

  @Override
  public void editEvent(String subject, LocalDateTime start, LocalDateTime end,
                        String property, Object value) {
  }

  @Override
  public void editEventsFrom(String subject, LocalDateTime start, LocalDateTime end,
                             String property, Object value) {
  }

  @Override
  public void editAllEventsInSeries(String subject, LocalDateTime start, LocalDateTime end,
                                    String property, Object value) {
  }

  @Override
  public List<Event> getEventsOn(LocalDate date) {
    return List.of();
  }

  @Override
  public List<Event> getEventsInRange(LocalDateTime start, LocalDateTime end) {
    return List.of();
  }

  @Override
  public boolean isBusy(LocalDateTime dateTime) {
    return false;
  }

  @Override
  public String exportToCsv(String fileName) {
    return "";
  }

  @Override
  public String exportToIcal(String fileName, ZoneId timezone) {
    return "";
  }

  @Override
  public void removeEventFromSeries(Event event) {
  }

  @Override
  public void removeAllEventsInSeries(Event event) {
  }

  @Override
  public CalendarAnalyticsSummary generateAnalytics(LocalDate start, LocalDate end) {
    throw new IllegalStateException("No calendar is currently in use");
  }
}