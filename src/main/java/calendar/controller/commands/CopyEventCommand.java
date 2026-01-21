package calendar.controller.commands;

import calendar.controller.ManagerCommand;
import calendar.model.CalendarManager;
import calendar.view.CalendarView;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Command to copy a single event from the current calendar to a target calendar.
 */
public class CopyEventCommand implements ManagerCommand {
  private final String eventSubject;
  private final LocalDateTime sourceStartDateTime;
  private final String targetCalendarName;
  private final LocalDateTime targetStartDateTime;

  /**
   * Constructs a CopyEventCommand.
   *
   * @param eventSubject the subject of the event to copy
   * @param sourceStartDateTime the start date/time of the event to copy
   * @param targetCalendarName the name of the target calendar
   * @param targetStartDateTime the start date/time in the target calendar
   */
  public CopyEventCommand(String eventSubject, LocalDateTime sourceStartDateTime,
                          String targetCalendarName, LocalDateTime targetStartDateTime) {
    this.eventSubject = eventSubject;
    this.sourceStartDateTime = sourceStartDateTime;
    this.targetCalendarName = targetCalendarName;
    this.targetStartDateTime = targetStartDateTime;
  }

  @Override
  public void execute(CalendarManager manager, CalendarView view) throws IOException {
    try {
      manager.copyEvent(eventSubject, sourceStartDateTime,
          targetCalendarName, targetStartDateTime);
      view.displayMessage("Event '" + eventSubject + "' copied successfully to calendar: "
          + targetCalendarName);
    } catch (IllegalArgumentException e) {
      view.displayError(e.getMessage());
    }
  }
}