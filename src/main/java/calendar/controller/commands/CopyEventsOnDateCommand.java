package calendar.controller.commands;

import calendar.controller.ManagerCommand;
import calendar.model.CalendarManager;
import calendar.view.CalendarView;
import java.io.IOException;
import java.time.LocalDate;

/**
 * Command to copy all events on a specific date from the current calendar to a target calendar.
 */
public class CopyEventsOnDateCommand implements ManagerCommand {
  private final LocalDate sourceDate;
  private final String targetCalendarName;
  private final LocalDate targetDate;

  /**
   * Constructs a CopyEventsOnDateCommand.
   *
   * @param sourceDate the date to copy events from
   * @param targetCalendarName the name of the target calendar
   * @param targetDate the date in the target calendar
   */
  public CopyEventsOnDateCommand(LocalDate sourceDate, String targetCalendarName,
                                 LocalDate targetDate) {
    this.sourceDate = sourceDate;
    this.targetCalendarName = targetCalendarName;
    this.targetDate = targetDate;
  }

  @Override
  public void execute(CalendarManager manager, CalendarView view) throws IOException {
    try {
      manager.copyEventsOnDate(sourceDate, targetCalendarName, targetDate);
      view.displayMessage("Events on " + sourceDate + " copied successfully to calendar: "
          + targetCalendarName + " on " + targetDate);
    } catch (IllegalArgumentException e) {
      view.displayError(e.getMessage());
    }
  }
}