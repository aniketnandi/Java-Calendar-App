package calendar.controller.commands;

import calendar.controller.ManagerCommand;
import calendar.model.CalendarManager;
import calendar.view.CalendarView;
import java.io.IOException;
import java.time.LocalDate;

/**
 * Command to copy all events within a date range from the current calendar to a target calendar.
 */
public class CopyEventsBetweenCommand implements ManagerCommand {
  private final LocalDate startDate;
  private final LocalDate endDate;
  private final String targetCalendarName;
  private final LocalDate targetStartDate;

  /**
   * Constructs a CopyEventsBetweenCommand.
   *
   * @param startDate the start of the range (inclusive)
   * @param endDate the end of the range (inclusive)
   * @param targetCalendarName the name of the target calendar
   * @param targetStartDate the start date in the target calendar
   */
  public CopyEventsBetweenCommand(LocalDate startDate, LocalDate endDate,
                                  String targetCalendarName, LocalDate targetStartDate) {
    this.startDate = startDate;
    this.endDate = endDate;
    this.targetCalendarName = targetCalendarName;
    this.targetStartDate = targetStartDate;
  }

  @Override
  public void execute(CalendarManager manager, CalendarView view) throws IOException {
    try {
      manager.copyEventsBetween(startDate, endDate, targetCalendarName, targetStartDate);
      view.displayMessage("Events between " + startDate + " and " + endDate
          + " copied successfully to calendar: " + targetCalendarName
          + " starting on " + targetStartDate);
    } catch (IllegalArgumentException e) {
      view.displayError(e.getMessage());
    }
  }
}