package analytics;

import calendar.model.Event;
import calendar.view.CalendarView;
import java.util.List;

/**
 * Shared test stub implementation of CalendarView for testing purposes.
 * This is used for testing with the simpler CalendarView interface
 * (as opposed to CalendarGuiView which has more methods).
 */
public class TestCalendarViewStub implements CalendarView {
  public String lastMessage;
  public String lastError;

  @Override
  public void displayMessage(String message) {
    this.lastMessage = message;
  }

  @Override
  public void displayError(String error) {
    this.lastError = error;
  }

  @Override
  public void displayStatus(boolean status) {
  }

  @Override
  public void displayEvents(List<Event> events) {
  }

  public String getLastMessage() {
    return lastMessage;
  }

  public String getLastError() {
    return lastError;
  }
}