package controller;

import static org.junit.Assert.assertTrue;

import calendar.controller.ManagerCommand;
import calendar.controller.commands.CopyEventCommand;
import calendar.controller.commands.CopyEventsBetweenCommand;
import calendar.controller.commands.CopyEventsOnDateCommand;
import calendar.controller.commands.CreateCalendarCommand;
import calendar.controller.commands.EditCalendarCommand;
import calendar.controller.commands.UseCalendarCommand;
import calendar.model.CalendarEvent;
import calendar.model.CalendarManager;
import calendar.model.Event;
import calendar.model.SimpleCalendarManager;
import calendar.view.CalendarTextView;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for ManagerCommand implementations.
 */
public class ManagerCommandsTest {
  private CalendarManager manager;
  private StringBuilder output;
  private CalendarTextView view;

  /**
   * Test class for ManagerCommand implementations.
   */
  @Before
  public void setUp() {
    manager = new SimpleCalendarManager();
    output = new StringBuilder();
    view = new CalendarTextView(output);
  }

  @Test
  public void testCreateCalendarCommand() throws IOException {
    ManagerCommand cmd = new CreateCalendarCommand("Work", "America/New_York");
    cmd.execute(manager, view);

    assertTrue(output.toString().contains("created successfully"));
    assertTrue(manager.hasCalendar("Work"));
  }

  @Test
  public void testCreateCalendarCommandInvalidTimezone() throws IOException {
    ManagerCommand cmd = new CreateCalendarCommand("Work", "Invalid/Timezone");
    cmd.execute(manager, view);

    assertTrue(output.toString().contains("Error")
        || output.toString().contains("Invalid timezone"));
  }

  @Test
  public void testEditCalendarCommandName() throws IOException {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));

    ManagerCommand cmd = new EditCalendarCommand("Work", "name", "Office");
    cmd.execute(manager, view);

    assertTrue(output.toString().contains("updated successfully"));
    assertTrue(manager.hasCalendar("Office"));
  }

  @Test
  public void testEditCalendarCommandTimezone() throws IOException {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));

    ManagerCommand cmd = new EditCalendarCommand("Work", "timezone", "America/Chicago");
    cmd.execute(manager, view);

    assertTrue(output.toString().contains("updated successfully"));
  }

  @Test
  public void testEditCalendarCommandInvalidCalendar() throws IOException {
    ManagerCommand cmd = new EditCalendarCommand("NonExistent", "name", "New");
    cmd.execute(manager, view);

    assertTrue(output.toString().contains("Error"));
  }

  @Test
  public void testUseCalendarCommand() throws IOException {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));

    ManagerCommand cmd = new UseCalendarCommand("Work");
    cmd.execute(manager, view);

    assertTrue(output.toString().contains("Now using calendar"));
  }

  @Test
  public void testUseCalendarCommandNonExistent() throws IOException {
    ManagerCommand cmd = new UseCalendarCommand("NonExistent");
    cmd.execute(manager, view);

    assertTrue(output.toString().contains("Error"));
  }

  @Test
  public void testCopyEventCommand() throws IOException {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    manager.getCurrentCalendar().addEvent(event);

    ManagerCommand cmd = new CopyEventCommand("Meeting",
        LocalDateTime.of(2025, 5, 5, 10, 0),
        "Work",
        LocalDateTime.of(2025, 5, 6, 10, 0));
    cmd.execute(manager, view);

    assertTrue(output.toString().contains("copied successfully"));
  }

  @Test
  public void testCopyEventsOnDateCommand() throws IOException {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    manager.getCurrentCalendar().addEvent(event);

    ManagerCommand cmd = new CopyEventsOnDateCommand(
        LocalDate.of(2025, 5, 5),
        "Work",
        LocalDate.of(2025, 5, 10));
    cmd.execute(manager, view);

    assertTrue(output.toString().contains("copied successfully"));
  }

  @Test
  public void testCopyEventsBetweenCommand() throws IOException {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    manager.getCurrentCalendar().addEvent(event);

    ManagerCommand cmd = new CopyEventsBetweenCommand(
        LocalDate.of(2025, 5, 5),
        LocalDate.of(2025, 5, 10),
        "Work",
        LocalDate.of(2025, 6, 1));
    cmd.execute(manager, view);

    assertTrue(output.toString().contains("copied successfully"));
  }

  @Test
  public void testCopyEventCommandError() throws IOException {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    ManagerCommand cmd = new CopyEventCommand("NonExistent",
        LocalDateTime.of(2025, 5, 5, 10, 0),
        "Work",
        LocalDateTime.of(2025, 5, 6, 10, 0));
    cmd.execute(manager, view);

    assertTrue(output.toString().contains("Error"));
  }

  @Test
  public void testCopyEventsBetweenCommandNoCurrentCalendar() throws IOException {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));

    ManagerCommand cmd = new CopyEventsBetweenCommand(
        LocalDate.of(2025, 5, 5),
        LocalDate.of(2025, 5, 10),
        "Work",
        LocalDate.of(2025, 6, 1));
    cmd.execute(manager, view);

    assertTrue(output.toString().contains("Error"));
    assertTrue(output.toString().contains("No calendar is currently in use"));
  }

  @Test
  public void testCopyEventsBetweenCommandTargetNotFound() throws IOException {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    manager.getCurrentCalendar().addEvent(event);

    ManagerCommand cmd = new CopyEventsBetweenCommand(
        LocalDate.of(2025, 5, 5),
        LocalDate.of(2025, 5, 10),
        "NonExistent",
        LocalDate.of(2025, 6, 1));
    cmd.execute(manager, view);

    assertTrue(output.toString().contains("Error"));
  }

  @Test
  public void testCopyEventsOnDateCommandNoCurrentCalendar() throws IOException {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));

    ManagerCommand cmd = new CopyEventsOnDateCommand(
        LocalDate.of(2025, 5, 5),
        "Work",
        LocalDate.of(2025, 5, 10));
    cmd.execute(manager, view);

    assertTrue(output.toString().contains("Error"));
    assertTrue(output.toString().contains("No calendar is currently in use"));
  }

  @Test
  public void testCopyEventsOnDateCommandTargetNotFound() throws IOException {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    manager.getCurrentCalendar().addEvent(event);

    ManagerCommand cmd = new CopyEventsOnDateCommand(
        LocalDate.of(2025, 5, 5),
        "NonExistent",
        LocalDate.of(2025, 5, 10));
    cmd.execute(manager, view);

    assertTrue(output.toString().contains("Error"));
  }
}