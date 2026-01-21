package calendar.controller.commands;

import calendar.controller.ManagerCommand;
import calendar.model.CalendarManager;
import calendar.view.CalendarView;
import java.io.IOException;

/**
 * Command to edit a calendar's properties (name or timezone).
 */
public class EditCalendarCommand implements ManagerCommand {
  private final String calendarName;
  private final String property;
  private final String newValue;

  /**
   * Constructs an EditCalendarCommand.
   *
   * @param calendarName the name of the calendar to edit
   * @param property the property to edit ("name" or "timezone")
   * @param newValue the new value for the property
   */
  public EditCalendarCommand(String calendarName, String property, String newValue) {
    this.calendarName = calendarName;
    this.property = property;
    this.newValue = newValue;
  }

  @Override
  public void execute(CalendarManager manager, CalendarView view) throws IOException {
    try {
      manager.editCalendar(calendarName, property, newValue);
      view.displayMessage("Calendar '" + calendarName + "' updated successfully. "
          + property + " changed to: " + newValue);
    } catch (IllegalArgumentException e) {
      view.displayError(e.getMessage());
    }
  }
}