package calendar.controller.commands;

import calendar.controller.ManagerCommand;
import calendar.model.CalendarManager;
import calendar.view.CalendarView;
import java.io.IOException;

/**
 * Command to set the active calendar for subsequent operations.
 */
public class UseCalendarCommand implements ManagerCommand {
  private final String calendarName;

  /**
   * Constructs a UseCalendarCommand.
   *
   * @param calendarName the name of the calendar to use
   */
  public UseCalendarCommand(String calendarName) {
    this.calendarName = calendarName;
  }

  @Override
  public void execute(CalendarManager manager, CalendarView view) throws IOException {
    try {
      manager.useCalendar(calendarName);
      view.displayMessage("Now using calendar: " + calendarName);
    } catch (IllegalArgumentException e) {
      view.displayError(e.getMessage());
    }
  }
}