package view;

import calendar.model.Event;
import calendar.view.CalendarView;
import java.util.List;

/**
 * Mock implementation of CalendarView for testing purposes.
 * Logs all method calls to verify controller output behavior.
 */
public class MockCalendarView implements CalendarView {
  private final StringBuilder log;

  /**
   * Creates a MockCalendarView with a log to record interactions.
   *
   * @param log StringBuilder to record method calls
   */
  public MockCalendarView(StringBuilder log) {
    this.log = log;
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
    for (Event event : events) {
      log.append("  Event: ").append(event.getSubject()).append("\n");
    }
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
}