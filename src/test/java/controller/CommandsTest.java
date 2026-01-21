package controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import calendar.controller.Command;
import calendar.controller.commands.CreateEventCommand;
import calendar.controller.commands.EditEventCommand;
import calendar.controller.commands.EditEventsCommand;
import calendar.controller.commands.EditSeriesCommand;
import calendar.controller.commands.EventSeriesCommand;
import calendar.controller.commands.ExportCommand;
import calendar.controller.commands.PrintEventsBetweenCommand;
import calendar.controller.commands.PrintEventsOnCommand;
import calendar.controller.commands.ShowStatusCommand;
import calendar.model.CalendarEvent;
import calendar.model.Event;
import calendar.model.EventSeries;
import calendar.model.RecurringEventSeries;
import calendar.model.SimpleCalendar;
import calendar.model.Status;
import calendar.view.CalendarTextView;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import model.MockCalendarModel;
import org.junit.Before;
import org.junit.Test;
import view.MockCalendarView;

/**
 * Test class for SimpleCalendar.
 */
public class CommandsTest {
  private SimpleCalendar model;
  private StringBuilder output;
  private CalendarTextView view;

  /**
   * Initializes a fresh SimpleCalendar instance before each test run.
   */
  @Before
  public void setUp() {
    model = new SimpleCalendar();
    output = new StringBuilder();
    view = new CalendarTextView(output);
  }

  @Test
  public void testCreateEventCommand() throws IOException {
    Command cmd = new CreateEventCommand("Meeting",
        LocalDateTime.of(2025, 5, 5, 10, 0),
        LocalDateTime.of(2025, 5, 5, 11, 0));

    cmd.execute(model, view);

    assertTrue(output.toString().contains("Event created successfully"));
    assertEquals(1, model.getEventsOn(LocalDate.of(2025, 5, 5)).size());
  }

  @Test
  public void testCreateEventCommandNullEnd() throws IOException {
    Command cmd = new CreateEventCommand("All Day",
        LocalDateTime.of(2025, 5, 5, 0, 0),
        null);

    cmd.execute(model, view);

    assertTrue(output.toString().contains("Event created successfully"));
    assertTrue(model.getEventsOn(LocalDate.of(2025, 5, 5)).get(0).isAllDay());
  }

  @Test
  public void testCreateEventCommandInvalidEvent() throws IOException {
    Command cmd = new CreateEventCommand("",
        LocalDateTime.of(2025, 5, 5, 10, 0),
        LocalDateTime.of(2025, 5, 5, 11, 0));

    cmd.execute(model, view);

    assertTrue(output.toString().contains("Error"));
  }

  @Test
  public void testEditEventCommandSubject() throws IOException {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    model.addEvent(event);

    Command cmd = new EditEventCommand("Meeting",
        LocalDateTime.of(2025, 5, 5, 10, 0),
        LocalDateTime.of(2025, 5, 5, 11, 0),
        "subject", "New Meeting");

    cmd.execute(model, view);

    assertTrue(output.toString().contains("Event edited successfully"));
    assertEquals("New Meeting", model.getEventsOn(LocalDate.of(2025, 5, 5)).get(0).getSubject());
  }

  @Test
  public void testEditEventCommandDescription() throws IOException {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    model.addEvent(event);

    Command cmd = new EditEventCommand("Meeting",
        LocalDateTime.of(2025, 5, 5, 10, 0),
        LocalDateTime.of(2025, 5, 5, 11, 0),
        "description", "New description");

    cmd.execute(model, view);

    assertEquals("New description", model.getEventsOn(LocalDate.of(2025, 5, 5)).get(0)
        .getDescription());
  }

  @Test
  public void testEditEventCommandLocation() throws IOException {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    model.addEvent(event);

    Command cmd = new EditEventCommand("Meeting",
        LocalDateTime.of(2025, 5, 5, 10, 0),
        LocalDateTime.of(2025, 5, 5, 11, 0),
        "location", "Room A");

    cmd.execute(model, view);

    assertEquals("Room A", model.getEventsOn(LocalDate.of(2025, 5, 5)).get(0).getLocation());
  }

  @Test
  public void testEditEventCommandStatus() throws IOException {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    model.addEvent(event);

    Command cmd = new EditEventCommand("Meeting",
        LocalDateTime.of(2025, 5, 5, 10, 0),
        LocalDateTime.of(2025, 5, 5, 11, 0),
        "status", "PRIVATE");

    cmd.execute(model, view);

    assertEquals(Status.PRIVATE, model.getEventsOn(LocalDate.of(2025, 5, 5)).get(0).getStatus());
  }

  @Test
  public void testEditEventCommandStart() throws IOException {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    model.addEvent(event);

    Command precmd = new EditEventCommand("Meeting",
        LocalDateTime.of(2025, 5, 5, 10, 0),
        LocalDateTime.of(2025, 5, 5, 11, 0),
        "end", "2025-05-05T14:00");
    Command cmd = new EditEventCommand("Meeting",
        LocalDateTime.of(2025, 5, 5, 10, 0),
        LocalDateTime.of(2025, 5, 5, 14, 0),
        "start", "2025-05-05T11:00");

    precmd.execute(model, view);
    cmd.execute(model, view);

    assertEquals(LocalDateTime.of(2025, 5, 5, 11, 0),
        model.getEventsOn(LocalDate.of(2025, 5, 5)).get(0).getStartDateTime());
  }

  @Test
  public void testEditEventCommandEnd() throws IOException {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    model.addEvent(event);

    Command cmd = new EditEventCommand("Meeting",
        LocalDateTime.of(2025, 5, 5, 10, 0),
        LocalDateTime.of(2025, 5, 5, 11, 0),
        "endDateTime", "2025-05-05T12:00");

    cmd.execute(model, view);

    assertEquals(LocalDateTime.of(2025, 5, 5, 12, 0),
        model.getEventsOn(LocalDate.of(2025, 5, 5)).get(0).getEndDateTime());
  }

  @Test
  public void testEditEventCommandNotFound() throws IOException {
    Command cmd = new EditEventCommand("NonExistent",
        LocalDateTime.of(2025, 5, 5, 10, 0),
        LocalDateTime.of(2025, 5, 5, 11, 0),
        "subject", "New");

    cmd.execute(model, view);

    assertTrue(output.toString().contains("Error"));
  }

  @Test
  public void testEditEventCommandInvalidProperty() throws IOException {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    model.addEvent(event);

    Command cmd = new EditEventCommand("Meeting",
        LocalDateTime.of(2025, 5, 5, 10, 0),
        LocalDateTime.of(2025, 5, 5, 11, 0),
        "invalid", "value");

    cmd.execute(model, view);

    assertTrue(output.toString().contains("Error"));
  }

  @Test
  public void testEditEventsCommand() throws IOException {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);
    days.add(DayOfWeek.WEDNESDAY);

    EventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Standup")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 9, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 9, 30))
        .weekdays(days)
        .repeatCount(4)
        .build();
    model.addEventSeries(series);

    Command cmd = new EditEventsCommand("Standup",
        series.getAllEvents().get(1).getStartDateTime(),
        "subject", "Modified");

    cmd.execute(model, view);

    assertTrue(output.toString().contains("Events edited successfully"));
  }

  @Test
  public void testEditEventsCommandInvalidProperty() throws IOException {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    model.addEvent(event);

    Command cmd = new EditEventsCommand("Meeting",
        LocalDateTime.of(2025, 5, 5, 10, 0),
        "invalid", "value");

    cmd.execute(model, view);

    assertTrue(output.toString().contains("Error"));
  }

  @Test
  public void testEditSeriesCommand() throws IOException {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);

    EventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Standup")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 9, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 9, 30))
        .weekdays(days)
        .repeatCount(3)
        .build();
    model.addEventSeries(series);

    Command cmd = new EditSeriesCommand("Standup",
        series.getAllEvents().get(0).getStartDateTime(),
        "subject", "Modified");

    cmd.execute(model, view);

    assertTrue(output.toString().contains("Event series edited successfully"));
  }

  @Test
  public void testEditSeriesCommandInvalidProperty() throws IOException {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    model.addEvent(event);

    Command cmd = new EditSeriesCommand("Meeting",
        LocalDateTime.of(2025, 5, 5, 10, 0),
        "invalid", "value");

    cmd.execute(model, view);

    assertTrue(output.toString().contains("Error"));
  }

  @Test
  public void testEventSeriesCommandWithCount() throws IOException {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);
    days.add(DayOfWeek.WEDNESDAY);

    Command cmd = new EventSeriesCommand("Standup",
        LocalDateTime.of(2025, 5, 5, 9, 0),
        LocalDateTime.of(2025, 5, 5, 9, 30),
        days, 4, null);

    cmd.execute(model, view);

    assertTrue(output.toString().contains("Event series created successfully"));
    assertTrue(output.toString().contains("4 events"));
  }

  @Test
  public void testEventSeriesCommandWithUntil() throws IOException {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.FRIDAY);

    Command cmd = new EventSeriesCommand("Meeting",
        LocalDateTime.of(2025, 5, 2, 10, 0),
        LocalDateTime.of(2025, 5, 2, 11, 0),
        days, null, LocalDate.of(2025, 5, 30));

    cmd.execute(model, view);

    assertTrue(output.toString().contains("Event series created successfully"));
  }

  @Test
  public void testEventSeriesCommandInvalid() throws IOException {
    Set<DayOfWeek> days = new HashSet<>();

    Command cmd = new EventSeriesCommand("Meeting",
        LocalDateTime.of(2025, 5, 5, 10, 0),
        LocalDateTime.of(2025, 5, 5, 11, 0),
        days, 3, null);

    cmd.execute(model, view);

    assertTrue(output.toString().contains("Error"));
  }

  @Test
  public void testExportCommand() throws IOException {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    model.addEvent(event);

    Command cmd = new ExportCommand("test_export.csv");
    cmd.execute(model, view);

    assertTrue(output.toString().contains("Calendar exported successfully"));

    Files.deleteIfExists(Paths.get("exports/test_export.csv"));
  }

  @Test
  public void testPrintEventsBetweenCommand() throws IOException {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    model.addEvent(event);

    Command cmd = new PrintEventsBetweenCommand(
        LocalDateTime.of(2025, 5, 5, 0, 0),
        LocalDateTime.of(2025, 5, 5, 23, 59));

    cmd.execute(model, view);

    assertTrue(output.toString().contains("Meeting"));
  }

  @Test
  public void testPrintEventsBetweenCommandNoEvents() throws IOException {
    Command cmd = new PrintEventsBetweenCommand(
        LocalDateTime.of(2025, 5, 5, 0, 0),
        LocalDateTime.of(2025, 5, 5, 23, 59));

    cmd.execute(model, view);

    assertTrue(output.toString().contains("No events found"));
  }

  @Test
  public void testPrintEventsOnCommand() throws IOException {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    model.addEvent(event);

    Command cmd = new PrintEventsOnCommand(LocalDate.of(2025, 5, 5));
    cmd.execute(model, view);

    assertTrue(output.toString().contains("Meeting"));
  }

  @Test
  public void testPrintEventsOnCommandNoEvents() throws IOException {
    Command cmd = new PrintEventsOnCommand(LocalDate.of(2025, 5, 5));
    cmd.execute(model, view);

    assertTrue(output.toString().contains("No events found"));
  }

  @Test
  public void testShowStatusCommandBusy() throws IOException {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    model.addEvent(event);

    Command cmd = new ShowStatusCommand(LocalDateTime.of(2025, 5, 5, 10, 30));
    cmd.execute(model, view);

    assertTrue(output.toString().contains("Busy"));
  }

  @Test
  public void testShowStatusCommandAvailable() throws IOException {
    Command cmd = new ShowStatusCommand(LocalDateTime.of(2025, 5, 5, 10, 30));
    cmd.execute(model, view);

    assertTrue(output.toString().contains("Available"));
  }

  @Test
  public void testCreateEventCommandWithMock() throws IOException {
    StringBuilder mockLog = new StringBuilder();
    StringBuilder viewLog = new StringBuilder();
    MockCalendarModel mockModel = new MockCalendarModel(mockLog);
    MockCalendarView mockView = new MockCalendarView(viewLog);

    Command cmd = new CreateEventCommand("TestEvent",
        LocalDateTime.of(2025, 5, 5, 10, 0),
        LocalDateTime.of(2025, 5, 5, 11, 0));

    cmd.execute(mockModel, mockView);

    assertTrue(mockLog.toString().contains("addEvent called with: subject=TestEvent"));
    assertTrue(mockLog.toString().contains("start=2025-05-05T10:00"));
    assertTrue(mockLog.toString().contains("end=2025-05-05T11:00"));
    assertTrue(viewLog.toString().contains("displayMessage: Event created successfully"));
  }

  @Test
  public void testEditEventCommandWithMock() throws IOException {
    StringBuilder mockLog = new StringBuilder();
    StringBuilder viewLog = new StringBuilder();
    MockCalendarModel mockModel = new MockCalendarModel(mockLog);
    MockCalendarView mockView = new MockCalendarView(viewLog);

    Command cmd = new EditEventCommand("Meeting",
        LocalDateTime.of(2025, 5, 5, 10, 0),
        LocalDateTime.of(2025, 5, 5, 11, 0),
        "subject", "NewMeeting");

    cmd.execute(mockModel, mockView);

    assertTrue(mockLog.toString().contains("editEvent called with: subject=Meeting"));
    assertTrue(mockLog.toString().contains("property=subject"));
    assertTrue(mockLog.toString().contains("value=NewMeeting"));
    assertTrue(viewLog.toString().contains("displayMessage: Event edited successfully"));
  }

  @Test
  public void testEditEventsCommandWithMock() throws IOException {
    StringBuilder mockLog = new StringBuilder();
    StringBuilder viewLog = new StringBuilder();
    MockCalendarModel mockModel = new MockCalendarModel(mockLog);
    MockCalendarView mockView = new MockCalendarView(viewLog);

    Command cmd = new EditEventsCommand("Standup",
        LocalDateTime.of(2025, 5, 5, 9, 0),
        "location", "Room B");

    cmd.execute(mockModel, mockView);

    assertTrue(mockLog.toString().contains("editEventsFrom called with: subject=Standup"));
    assertTrue(mockLog.toString().contains("property=location"));
    assertTrue(mockLog.toString().contains("value=Room B"));
    assertTrue(viewLog.toString().contains("displayMessage: Events edited successfully"));
  }

  @Test
  public void testEditSeriesCommandWithMock() throws IOException {
    StringBuilder mockLog = new StringBuilder();
    StringBuilder viewLog = new StringBuilder();
    MockCalendarModel mockModel = new MockCalendarModel(mockLog);
    MockCalendarView mockView = new MockCalendarView(viewLog);

    Command cmd = new EditSeriesCommand("Standup",
        LocalDateTime.of(2025, 5, 5, 9, 0),
        "description", "Daily standup meeting");

    cmd.execute(mockModel, mockView);

    assertTrue(mockLog.toString().contains("editAllEventsInSeries called with: subject=Standup"));
    assertTrue(mockLog.toString().contains("property=description"));
    assertTrue(mockLog.toString().contains("value=Daily standup meeting"));
    assertTrue(viewLog.toString().contains("displayMessage: Event series edited successfully"));
  }

  @Test
  public void testPrintEventsOnCommandWithMock() throws IOException {
    StringBuilder mockLog = new StringBuilder();
    StringBuilder viewLog = new StringBuilder();
    MockCalendarModel mockModel = new MockCalendarModel(mockLog);
    MockCalendarView mockView = new MockCalendarView(viewLog);

    Command cmd = new PrintEventsOnCommand(LocalDate.of(2025, 5, 5));
    cmd.execute(mockModel, mockView);

    assertTrue(mockLog.toString().contains("getEventsOn called with: date=2025-05-05"));
    assertTrue(viewLog.toString().contains("displayEvents called with 0 events"));
  }

  @Test
  public void testShowStatusCommandWithMock() throws IOException {
    StringBuilder mockLog = new StringBuilder();
    StringBuilder viewLog = new StringBuilder();
    MockCalendarModel mockModel = new MockCalendarModel(mockLog);
    MockCalendarView mockView = new MockCalendarView(viewLog);

    Command cmd = new ShowStatusCommand(LocalDateTime.of(2025, 5, 5, 10, 30));
    cmd.execute(mockModel, mockView);

    assertTrue(mockLog.toString().contains("isBusy called with: dateTime=2025-05-05T10:30"));
    assertTrue(viewLog.toString().contains("displayStatus: Available"));
  }

  @Test
  public void testExportCommandWithMock() throws IOException {
    StringBuilder mockLog = new StringBuilder();
    StringBuilder viewLog = new StringBuilder();
    MockCalendarModel mockModel = new MockCalendarModel(mockLog);
    MockCalendarView mockView = new MockCalendarView(viewLog);

    Command cmd = new ExportCommand("export.csv");
    cmd.execute(mockModel, mockView);

    assertTrue(mockLog.toString().contains("exportToCsv called with: fileName=export.csv"));
    assertTrue(viewLog.toString().contains("displayMessage: Calendar exported successfully"));
  }

  @Test
  public void testMockCalendarViewGetLog() throws IOException {
    StringBuilder mockLog = new StringBuilder();
    StringBuilder viewLog = new StringBuilder();
    MockCalendarModel mockModel = new MockCalendarModel(mockLog);
    MockCalendarView mockView = new MockCalendarView(viewLog);

    Command cmd = new ShowStatusCommand(LocalDateTime.of(2025, 5, 5, 10, 30));
    cmd.execute(mockModel, mockView);

    String log = mockView.getLog();
    assertTrue(log.contains("displayStatus: Available"));
  }

  @Test
  public void testShowStatusCommandWithMockBusy() throws IOException {
    StringBuilder mockLog = new StringBuilder();
    StringBuilder viewLog = new StringBuilder();

    MockCalendarModel mockModel = new MockCalendarModel(mockLog,
        new ArrayList<>(), true, "/mock/path/export.csv");
    MockCalendarView mockView = new MockCalendarView(viewLog);

    Command cmd = new ShowStatusCommand(LocalDateTime.of(2025, 5, 5, 10, 30));
    cmd.execute(mockModel, mockView);

    assertTrue(viewLog.toString().contains("displayStatus: Busy"));
  }

  @Test
  public void testDisplayEventsWithMockNonEmpty() throws IOException {
    StringBuilder mockLog = new StringBuilder();
    StringBuilder viewLog = new StringBuilder();

    List<Event> mockEvents = new ArrayList<>();
    Event event = new CalendarEvent.EventBuilder()
        .subject("TestEvent")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    mockEvents.add(event);

    MockCalendarModel mockModel = new MockCalendarModel(mockLog, mockEvents,
        false, "/mock/path/export.csv");
    MockCalendarView mockView = new MockCalendarView(viewLog);

    Command cmd = new PrintEventsOnCommand(LocalDate.of(2025, 5, 5));
    cmd.execute(mockModel, mockView);

    assertTrue(viewLog.toString().contains("displayEvents called with 1 events"));
    assertTrue(viewLog.toString().contains("Event: TestEvent"));
  }

  @Test
  public void testEditSeriesCommandStatus() throws IOException {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);

    EventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Standup")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 9, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 9, 30))
        .weekdays(days)
        .repeatCount(3)
        .build();
    model.addEventSeries(series);

    Command cmd = new EditSeriesCommand("Standup",
        LocalDateTime.of(2025, 5, 5, 9, 0),
        "status", "PRIVATE");

    cmd.execute(model, view);

    List<Event> events = model.getEventsInRange(
        LocalDateTime.of(2025, 5, 1, 0, 0),
        LocalDateTime.of(2025, 5, 31, 23, 59));

    for (Event e : events) {
      assertEquals(Status.PRIVATE, e.getStatus());
    }
  }

  @Test
  public void testEditSeriesCommandStartDateTime() throws IOException {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    model.addEvent(event);

    Command precmd = new EditSeriesCommand("Meeting",
        LocalDateTime.of(2025, 5, 5, 10, 0),
        "end", "2025-05-05T14:00");
    precmd.execute(model, view);

    Command cmd = new EditSeriesCommand("Meeting",
        LocalDateTime.of(2025, 5, 5, 10, 0),
        "startDateTime", "2025-05-05T11:00");

    cmd.execute(model, view);

    assertEquals(LocalDateTime.of(2025, 5, 5, 11, 0),
        model.getEventsOn(LocalDate.of(2025, 5, 5)).get(0).getStartDateTime());
  }

  @Test
  public void testEditEventsCommandStartDateTimeVerifyValue() throws IOException {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);

    EventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 6, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 6, 11, 0))
        .weekdays(days)
        .repeatCount(3)
        .build();
    model.addEventSeries(series);

    Command cmd = new EditEventsCommand("Meeting",
        LocalDateTime.of(2025, 5, 6, 10, 0),
        "subject", "Modified");

    cmd.execute(model, view);

    List<Event> events = model.getEventsInRange(
        LocalDateTime.of(2025, 5, 1, 0, 0),
        LocalDateTime.of(2025, 5, 31, 23, 59));

    int modifiedCount = 0;
    int originalCount = 0;
    for (Event e : events) {
      if (e.getSubject().equals("Modified")) {
        modifiedCount++;
      }
      if (e.getSubject().equals("Meeting")) {
        originalCount++;
      }
    }

    assertTrue(modifiedCount > 0);
    assertEquals(3, events.size());
  }

  @Test
  public void testExportCommandIoException() throws IOException {
    StringBuilder mockLog = new StringBuilder();
    StringBuilder viewLog = new StringBuilder();

    MockCalendarModel mockModel = new MockCalendarModel(mockLog) {
      @Override
      public String exportToCsv(String fileName) throws IOException {
        mockLog.append("exportToCsv called with: fileName=").append(fileName).append("\n");
        throw new IOException("Disk full");
      }
    };

    MockCalendarView mockView = new MockCalendarView(viewLog);

    Command cmd = new ExportCommand("test.csv");
    cmd.execute(mockModel, mockView);

    assertTrue(viewLog.toString().contains("displayError"));
    assertTrue(viewLog.toString().contains("Failed to export calendar"));
    assertTrue(viewLog.toString().contains("Disk full"));
  }

  @Test
  public void testEditEventCommandStatusUpperCase() throws IOException {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting2")
        .startDateTime(LocalDateTime.of(2025, 5, 6, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 6, 11, 0))
        .build();
    model.addEvent(event);

    Command cmd = new EditEventCommand("Meeting2",
        LocalDateTime.of(2025, 5, 6, 10, 0),
        LocalDateTime.of(2025, 5, 6, 11, 0),
        "status", "private");

    cmd.execute(model, view);

    assertEquals(Status.PRIVATE, model.getEventsOn(LocalDate.of(2025, 5, 6)).get(0).getStatus());
  }

  @Test
  public void testEventSeriesCommandVerifyEventsInModel() throws IOException {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);
    days.add(DayOfWeek.FRIDAY);

    Command cmd = new EventSeriesCommand("TestSeries",
        LocalDateTime.of(2025, 5, 5, 10, 0),
        LocalDateTime.of(2025, 5, 5, 11, 0),
        days, 4, null);

    cmd.execute(model, view);

    List<Event> events = model.getEventsInRange(
        LocalDateTime.of(2025, 5, 1, 0, 0),
        LocalDateTime.of(2025, 5, 31, 23, 59));

    assertEquals(4, events.size());

    for (Event e : events) {
      assertEquals("TestSeries", e.getSubject());
      assertNotNull(e.getSeriesId());
    }
  }

  @Test
  public void testMockCalendarModelAddEventSeries() throws IOException {
    StringBuilder mockLog = new StringBuilder();
    StringBuilder viewLog = new StringBuilder();
    MockCalendarModel mockModel = new MockCalendarModel(mockLog);
    MockCalendarView mockView = new MockCalendarView(viewLog);

    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);

    Command cmd = new EventSeriesCommand("Series",
        LocalDateTime.of(2025, 5, 5, 10, 0),
        LocalDateTime.of(2025, 5, 5, 11, 0),
        days, 3, null);

    cmd.execute(mockModel, mockView);

    assertTrue(mockLog.toString().contains("addEventSeries called with: subject=Series"));
    assertTrue(mockLog.toString().contains("events count=3"));
  }

  @Test
  public void testMockCalendarModelRemoveEvent() {
    StringBuilder mockLog = new StringBuilder();
    MockCalendarModel mockModel = new MockCalendarModel(mockLog);

    Event event = new CalendarEvent.EventBuilder()
        .subject("ToRemove")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();

    mockModel.removeEvent(event);

    assertTrue(mockLog.toString().contains("removeEvent called with: subject=ToRemove"));
  }

  @Test
  public void testMockCalendarModelGetEventsInRangeReturnValue() throws IOException {
    StringBuilder mockLog = new StringBuilder();
    StringBuilder viewLog = new StringBuilder();

    Event mockEvent = new CalendarEvent.EventBuilder()
        .subject("MockEvent")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    List<Event> mockEvents = new ArrayList<>();
    mockEvents.add(mockEvent);

    MockCalendarModel mockModel = new MockCalendarModel(mockLog, mockEvents, false, "/path");
    MockCalendarView mockView = new MockCalendarView(viewLog);

    Command cmd = new PrintEventsBetweenCommand(
        LocalDateTime.of(2025, 5, 5, 0, 0),
        LocalDateTime.of(2025, 5, 6, 0, 0));

    cmd.execute(mockModel, mockView);

    assertTrue(viewLog.toString().contains("displayEvents called with 1 events"));
    assertTrue(viewLog.toString().contains("Event: MockEvent"));
  }

  @Test
  public void testMockCalendarModelGetLog() {
    StringBuilder mockLog = new StringBuilder();
    MockCalendarModel mockModel = new MockCalendarModel(mockLog);

    Event event = new CalendarEvent.EventBuilder()
        .subject("Test")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();

    mockModel.addEvent(event);

    String log = mockModel.getLog();

    assertTrue(log.contains("addEvent called with: subject=Test"));
  }

  @Test
  public void testExportCommandEmptyFilenameBeforeExtension() throws IOException {
    Command cmd = new ExportCommand(".csv");
    cmd.execute(model, view);

    assertTrue(output.toString().contains("Filename cannot be empty"));
    assertTrue(output.toString().contains("Please provide a name before the extension"));
  }

  @Test
  public void testExportCommandIllegalArgumentException() throws IOException {
    StringBuilder mockLog = new StringBuilder();
    StringBuilder viewLog = new StringBuilder();

    MockCalendarModel mockModel = new MockCalendarModel(mockLog) {
      @Override
      public String exportToCsv(String fileName) throws IOException {
        throw new IllegalArgumentException("Invalid calendar state");
      }
    };

    MockCalendarView mockView = new MockCalendarView(viewLog);

    Command cmd = new ExportCommand("test.csv");
    cmd.execute(mockModel, mockView);

    assertTrue(viewLog.toString().contains("displayError"));
    assertTrue(viewLog.toString().contains("Invalid calendar state"));
  }

  @Test
  public void testExportCommandIcalExtension() throws IOException {
    StringBuilder mockLog = new StringBuilder();
    StringBuilder viewLog = new StringBuilder();
    MockCalendarModel mockModel = new MockCalendarModel(mockLog);
    MockCalendarView mockView = new MockCalendarView(viewLog);

    Command cmd = new ExportCommand("test_ical.ical");
    cmd.execute(mockModel, mockView);

    assertTrue(mockLog.toString().contains("exportToICal called with: fileName=test_ical.ical"));
    assertTrue(viewLog.toString().contains("displayMessage: Calendar exported successfully"));
  }

  @Test
  public void testExportIcalRealFile() throws IOException {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    model.addEvent(event);

    Command cmd = new ExportCommand("test.ical");
    cmd.execute(model, view);

    assertTrue(output.toString().contains("Calendar exported successfully"));
    Path file = Paths.get("exports/test.ical");
    assertTrue(Files.exists(file));

    List<String> lines = Files.readAllLines(file);
    assertTrue(lines.stream().anyMatch(l -> l.contains("BEGIN:VCALENDAR")));
    assertTrue(lines.stream().anyMatch(l -> l.contains("SUMMARY:Meeting")));

    Files.deleteIfExists(file);
  }

  @Test
  public void testExportShowsAbsolutePath() throws IOException {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    model.addEvent(event);

    Command cmd = new ExportCommand("path_test.csv");
    cmd.execute(model, view);

    String result = output.toString();
    assertTrue(result.contains("Calendar exported successfully to:"));
    assertTrue(result.contains("exports"));

    Files.deleteIfExists(Paths.get("exports/path_test.csv"));
  }

  @Test
  public void testExportAutoDetectsCsvVsIcal() throws IOException {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Test")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    model.addEvent(event);

    Command csvCmd = new ExportCommand("detect_test.csv");
    csvCmd.execute(model, view);

    Path csvPath = Paths.get("exports/detect_test.csv");
    assertTrue(Files.exists(csvPath));
    final List<String> csvLines = Files.readAllLines(csvPath);

    output.setLength(0);

    Command icalCmd = new ExportCommand("detect_test.ical");
    icalCmd.execute(model, view);

    Path icalPath = Paths.get("exports/detect_test.ical");
    assertTrue(Files.exists(icalPath));
    List<String> icalLines = Files.readAllLines(icalPath);

    assertTrue(csvLines.stream().anyMatch(l -> l.contains(",")));
    assertTrue(icalLines.stream().anyMatch(l -> l.contains("BEGIN:VCALENDAR")));

    Files.deleteIfExists(csvPath);
    Files.deleteIfExists(icalPath);
  }

  @Test
  public void testExportUnsupportedExtension() throws IOException {
    Command cmd = new ExportCommand("test.txt");
    cmd.execute(model, view);

    assertTrue(output.toString().contains("Unsupported file format"));
  }

  @Test
  public void testExportNoExtension() throws IOException {
    Command cmd = new ExportCommand("myfile");
    cmd.execute(model, view);

    assertTrue(output.toString().contains("No file extension provided"));
  }

  /**
   * Test that export command validates filename has content before extension.
   */
  @Test
  public void testExportCommandFilenameOnlyExtension() throws IOException {
    Command cmd = new ExportCommand(".ical");
    cmd.execute(model, view);

    assertTrue(output.toString().contains("Error"));
    assertTrue(output.toString().contains("Filename cannot be empty")
        || output.toString().contains("Please provide a name before the extension"));
  }
}