package calendar.controller;

import calendar.model.CalendarManager;
import calendar.view.CalendarView;
import java.io.IOException;

/**
 * Represents an executable command that works with CalendarManager.
 * Used for calendar management operations (create, edit, use, copy).
 * Separate from the Command interface to maintain backward compatibility.
 */
public interface ManagerCommand {
  /**
   * Execute this command on the given manager and view.
   *
   * @param manager the calendar manager
   * @param view the calendar view
   * @throws IOException if there's an error with I/O
   * @throws IllegalArgumentException if the command cannot be executed
   */
  void execute(CalendarManager manager, CalendarView view) throws IOException;
}