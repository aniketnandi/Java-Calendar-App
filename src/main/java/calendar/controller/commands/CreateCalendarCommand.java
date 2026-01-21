package calendar.controller.commands;

import calendar.controller.ManagerCommand;
import calendar.model.CalendarManager;
import calendar.view.CalendarView;
import java.io.IOException;
import java.time.ZoneId;

/**
 * Command to create a new calendar with a specified name and timezone.
 */
public class CreateCalendarCommand implements ManagerCommand {
  private final String calendarName;
  private final String timezone;

  /**
   * Constructs a CreateCalendarCommand.
   *
   * @param calendarName the name of the calendar to create
   * @param timezone the timezone in IANA format (e.g., "America/New_York")
   */
  public CreateCalendarCommand(String calendarName, String timezone) {
    this.calendarName = calendarName;
    this.timezone = timezone;
  }

  @Override
  public void execute(CalendarManager manager, CalendarView view) throws IOException {
    try {
      ZoneId zoneId = ZoneId.of(timezone);
      manager.createCalendar(calendarName, zoneId);
      view.displayMessage("Calendar '" + calendarName + "' created successfully with timezone "
          + timezone);
    } catch (IllegalArgumentException e) {
      view.displayError(e.getMessage());
    } catch (Exception e) {
      view.displayError("Invalid timezone: " + timezone);
    }
  }
}