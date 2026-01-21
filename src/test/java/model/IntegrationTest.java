package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import calendar.controller.Calendar;
import calendar.controller.CalendarController;
import calendar.model.CalendarEvent;
import calendar.model.CalendarManager;
import calendar.model.Event;
import calendar.model.EventSeries;
import calendar.model.RecurringEventSeries;
import calendar.model.SimpleCalendarManager;
import calendar.view.CalendarTextView;
import calendar.view.CalendarView;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

/**
 * End-to-end integration tests exercising controller, model, and view together.
 */
public class IntegrationTest {
  private CalendarManager manager;
  private StringBuilder output;
  private CalendarView view;

  /**
   * Creates a fresh in-memory calendar manager and view for each test run.
   */
  @Before
  public void setUp() {
    manager = new SimpleCalendarManager();
    manager.createCalendar("TestCal", ZoneId.of("America/New_York"));
    manager.useCalendar("TestCal");
    output = new StringBuilder();
    view = new CalendarTextView(output);
  }

  @Test
  public void testOverlappingEvents() {
    Event event1 = new CalendarEvent.EventBuilder()
        .subject("Event1")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 12, 0))
        .build();

    Event event2 = new CalendarEvent.EventBuilder()
        .subject("Event2")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 13, 0))
        .build();

    calendar.model.Calendar model = manager.getCurrentCalendar();
    model.addEvent(event1);
    model.addEvent(event2);

    assertTrue(model.isBusy(LocalDateTime.of(2025, 5, 5, 11, 30)));

    List<Event> events = model.getEventsInRange(
        LocalDateTime.of(2025, 5, 5, 10, 30),
        LocalDateTime.of(2025, 5, 5, 11, 30));

    assertEquals(2, events.size());
  }

  @Test
  public void testMultipleDayEvent() {
    calendar.model.Calendar model = manager.getCurrentCalendar();

    Event event = new CalendarEvent.EventBuilder()
        .subject("Conference")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 9, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 7, 17, 0))
        .build();

    model.addEvent(event);

    assertTrue(model.isBusy(LocalDateTime.of(2025, 5, 5, 10, 0)));
    assertTrue(model.isBusy(LocalDateTime.of(2025, 5, 6, 14, 0)));
    assertTrue(model.isBusy(LocalDateTime.of(2025, 5, 7, 16, 0)));
    assertFalse(model.isBusy(LocalDateTime.of(2025, 5, 7, 17, 0)));

    List<Event> day1 = model.getEventsOn(LocalDate.of(2025, 5, 5));
    List<Event> day2 = model.getEventsOn(LocalDate.of(2025, 5, 6));
    List<Event> day3 = model.getEventsOn(LocalDate.of(2025, 5, 7));

    assertEquals(1, day1.size());
    assertEquals(1, day2.size());
    assertEquals(1, day3.size());
  }

  @Test
  public void testAllDayAndTimedEventsSameDay() {
    calendar.model.Calendar model = manager.getCurrentCalendar();

    Event allDay = new CalendarEvent.EventBuilder()
        .subject("All Day Event")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 0, 0))
        .build();

    Event timed = new CalendarEvent.EventBuilder()
        .subject("Timed Event")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 14, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 15, 0))
        .build();

    model.addEvent(allDay);
    model.addEvent(timed);

    List<Event> events = model.getEventsOn(LocalDate.of(2025, 5, 5));
    assertEquals(2, events.size());

    assertTrue(allDay.isAllDay());
    assertFalse(timed.isAllDay());
  }

  @Test
  public void testSeriesWithSingleOccurrence() {
    calendar.model.Calendar model = manager.getCurrentCalendar();

    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);

    EventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Single")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .weekdays(days)
        .repeatCount(1)
        .build();

    model.addEventSeries(series);

    List<Event> events = model.getEventsInRange(
        LocalDateTime.of(2025, 5, 1, 0, 0),
        LocalDateTime.of(2025, 5, 31, 23, 59));

    assertEquals(1, events.size());
  }

  @Test
  public void testExportWithSpecialCharacters() throws IOException {
    calendar.model.Calendar model = manager.getCurrentCalendar();

    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting, with \"special\" characters\nand newline")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .description("Description with, comma and \"quotes\"")
        .location("Room A, Building B")
        .build();

    model.addEvent(event);
    String path = model.exportToCsv("special_chars.csv");

    Path filePath = Path.of(path);
    assertTrue(Files.exists(filePath));

    List<String> lines = Files.readAllLines(filePath);
    assertTrue(lines.size() >= 2);

    Files.deleteIfExists(filePath);
  }

  @Test
  public void testEmptyCalendarOperations() {
    calendar.model.Calendar model = manager.getCurrentCalendar();

    List<Event> events = model.getEventsOn(LocalDate.of(2025, 5, 5));
    assertEquals(0, events.size());

    assertFalse(model.isBusy(LocalDateTime.of(2025, 5, 5, 10, 0)));

    List<Event> rangeEvents = model.getEventsInRange(
        LocalDateTime.of(2025, 5, 1, 0, 0),
        LocalDateTime.of(2025, 5, 31, 23, 59));
    assertEquals(0, rangeEvents.size());
  }

  @Test
  public void testBoundaryDateTime() {
    calendar.model.Calendar model = manager.getCurrentCalendar();

    Event event = new CalendarEvent.EventBuilder()
        .subject("Boundary")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 23, 59))
        .endDateTime(LocalDateTime.of(2025, 5, 6, 0, 1))
        .build();

    model.addEvent(event);

    List<Event> day1 = model.getEventsOn(LocalDate.of(2025, 5, 5));
    List<Event> day2 = model.getEventsOn(LocalDate.of(2025, 5, 6));

    assertEquals(1, day1.size());
    assertEquals(1, day2.size());

    assertTrue(model.isBusy(LocalDateTime.of(2025, 5, 5, 23, 59)));
    assertTrue(model.isBusy(LocalDateTime.of(2025, 5, 6, 0, 0)));
  }

  @Test
  public void testSeriesAcrossMonths() {
    calendar.model.Calendar model = manager.getCurrentCalendar();

    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.FRIDAY);

    EventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Monthly")
        .startDateTime(LocalDateTime.of(2025, 4, 25, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 4, 25, 11, 0))
        .weekdays(days)
        .repeatUntil(LocalDate.of(2025, 5, 30))
        .build();

    model.addEventSeries(series);

    List<Event> aprilEvents = model.getEventsInRange(
        LocalDateTime.of(2025, 4, 1, 0, 0),
        LocalDateTime.of(2025, 4, 30, 23, 59));

    List<Event> mayEvents = model.getEventsInRange(
        LocalDateTime.of(2025, 5, 1, 0, 0),
        LocalDateTime.of(2025, 5, 31, 23, 59));

    assertTrue(aprilEvents.size() >= 1);
    assertTrue(mayEvents.size() >= 1);
  }

  @Test
  public void testEventSeriesAllWeekdays() {
    final calendar.model.Calendar model = manager.getCurrentCalendar();

    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);
    days.add(DayOfWeek.TUESDAY);
    days.add(DayOfWeek.WEDNESDAY);
    days.add(DayOfWeek.THURSDAY);
    days.add(DayOfWeek.FRIDAY);
    days.add(DayOfWeek.SATURDAY);
    days.add(DayOfWeek.SUNDAY);

    EventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Daily")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .weekdays(days)
        .repeatCount(7)
        .build();

    model.addEventSeries(series);

    List<Event> events = series.getAllEvents();
    assertEquals(7, events.size());
  }

  @Test
  public void testEditNonSeriesEventWithSeriesMethods() {
    calendar.model.Calendar model = manager.getCurrentCalendar();

    Event event = new CalendarEvent.EventBuilder()
        .subject("Single")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();

    model.addEvent(event);

    model.editEventsFrom("Single",
        LocalDateTime.of(2025, 5, 5, 10, 0), null,
        "subject", "Modified");

    assertEquals("Modified", model.getEventsOn(LocalDate.of(2025, 5, 5)).get(0).getSubject());

    model.editAllEventsInSeries("Modified",
        LocalDateTime.of(2025, 5, 5, 10, 0), null,
        "subject", "Final");

    assertEquals("Final", model.getEventsOn(LocalDate.of(2025, 5, 5)).get(0).getSubject());
  }

  @Test
  public void testPrintCommandsWithDifferentFormats() throws IOException {
    String commands = "create calendar --name Test --timezone America/New_York\n"
        + "use calendar --name Test\n"
        + "create event Meeting from 2025-05-05T10:00 to 2025-05-05T11:00\n"
        + "print events on 2025-05-05\n"
        + "print events from 2025-05-05T09:00 to 2025-05-05T12:00\n"
        + "exit\n";

    StringReader input = new StringReader(commands);
    CalendarController controller = new Calendar(manager, view, input);
    controller.runInteractive();

    String result = output.toString();
    int meetingCount = 0;
    int index = 0;
    while ((index = result.indexOf("Meeting", index)) != -1) {
      meetingCount++;
      index++;
    }
    assertTrue(meetingCount >= 2);
  }

  @Test
  public void testStatusAtEventBoundaries() {
    calendar.model.Calendar model = manager.getCurrentCalendar();

    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();

    model.addEvent(event);

    assertFalse(model.isBusy(LocalDateTime.of(2025, 5, 5, 9, 59)));
    assertTrue(model.isBusy(LocalDateTime.of(2025, 5, 5, 10, 0)));
    assertTrue(model.isBusy(LocalDateTime.of(2025, 5, 5, 10, 30)));
    assertFalse(model.isBusy(LocalDateTime.of(2025, 5, 5, 11, 0)));
    assertFalse(model.isBusy(LocalDateTime.of(2025, 5, 5, 11, 1)));
  }

  @Test
  public void testMultipleCalendarsIntegration() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.createCalendar("Personal", ZoneId.of("America/Chicago"));

    manager.useCalendar("Work");
    calendar.model.Calendar work = manager.getCurrentCalendar();
    Event workEvent = new CalendarEvent.EventBuilder()
        .subject("Work Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    work.addEvent(workEvent);

    manager.useCalendar("Personal");
    calendar.model.Calendar personal = manager.getCurrentCalendar();
    Event personalEvent = new CalendarEvent.EventBuilder()
        .subject("Doctor Appointment")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 14, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 15, 0))
        .build();
    personal.addEvent(personalEvent);

    assertEquals(1, work.getEventsOn(LocalDate.of(2025, 5, 5)).size());
    assertEquals(1, personal.getEventsOn(LocalDate.of(2025, 5, 5)).size());
  }

  @Test
  public void testTimezoneConversionIntegration() {
    manager.createCalendar("EST", ZoneId.of("America/New_York"));
    manager.createCalendar("PST", ZoneId.of("America/Los_Angeles"));

    manager.useCalendar("EST");
    calendar.model.Calendar est = manager.getCurrentCalendar();
    Event event = new CalendarEvent.EventBuilder()
        .subject("Conference Call")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 14, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 15, 0))
        .build();
    est.addEvent(event);

    manager.copyEvent("Conference Call", LocalDateTime.of(2025, 5, 5, 14, 0),
        "PST", LocalDateTime.of(2025, 5, 5, 14, 0));

    calendar.model.Calendar pst = manager.getCalendar("PST");
    List<Event> pstEvents = pst.getEventsOn(LocalDate.of(2025, 5, 5));
    assertEquals(1, pstEvents.size());
    assertEquals(LocalDateTime.of(2025, 5, 5, 11, 0),
        pstEvents.get(0).getStartDateTime());
  }

  @Test
  public void testCopyEventsIntegration() {
    manager.createCalendar("Source", ZoneId.of("America/New_York"));
    manager.createCalendar("Target", ZoneId.of("America/New_York"));

    manager.useCalendar("Source");
    calendar.model.Calendar source = manager.getCurrentCalendar();
    source.addEvent(new CalendarEvent.EventBuilder()
        .subject("Event1")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build());
    source.addEvent(new CalendarEvent.EventBuilder()
        .subject("Event2")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 14, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 15, 0))
        .build());

    manager.copyEventsOnDate(LocalDate.of(2025, 5, 5), "Target",
        LocalDate.of(2025, 5, 10));

    calendar.model.Calendar target = manager.getCalendar("Target");
    List<Event> copiedEvents = target.getEventsOn(LocalDate.of(2025, 5, 10));
    assertEquals(2, copiedEvents.size());
  }

  /**
   * Integration test: Copy events on date with different timezones and verify conversion.
   */
  @Test
  public void testCopyEventsOnDateWithTimezoneConversion() {
    manager.createCalendar("NYC", ZoneId.of("America/New_York"));
    manager.createCalendar("LA", ZoneId.of("America/Los_Angeles"));

    manager.useCalendar("NYC");
    calendar.model.Calendar nyc = manager.getCurrentCalendar();

    Event event1 = new CalendarEvent.EventBuilder()
        .subject("Meeting1")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 14, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 15, 0))
        .build();
    Event event2 = new CalendarEvent.EventBuilder()
        .subject("Meeting2")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 15, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 16, 0))
        .build();
    nyc.addEvent(event1);
    nyc.addEvent(event2);

    manager.copyEventsOnDate(LocalDate.of(2025, 5, 5), "LA", LocalDate.of(2025, 5, 10));

    calendar.model.Calendar la = manager.getCalendar("LA");
    List<Event> copiedEvents = la.getEventsOn(LocalDate.of(2025, 5, 10));

    assertEquals(2, copiedEvents.size());

    assertEquals(LocalDateTime.of(2025, 5, 10, 11, 0),
        copiedEvents.get(0).getStartDateTime());
    assertEquals(LocalDateTime.of(2025, 5, 10, 12, 0),
        copiedEvents.get(1).getStartDateTime());
  }

  /**
   * Integration test: Verify that events in a calendar operate in that calendar's timezone.
   */
  @Test
  public void testEventsInheritCalendarTimezoneContext() {
    manager.createCalendar("EST", ZoneId.of("America/New_York"));
    manager.createCalendar("PST", ZoneId.of("America/Los_Angeles"));

    manager.useCalendar("EST");
    calendar.model.Calendar est = manager.getCurrentCalendar();
    Event estEvent = new CalendarEvent.EventBuilder()
        .subject("EST Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    est.addEvent(estEvent);

    manager.useCalendar("PST");
    calendar.model.Calendar pst = manager.getCurrentCalendar();
    Event pstEvent = new CalendarEvent.EventBuilder()
        .subject("PST Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    pst.addEvent(pstEvent);

    assertEquals(ZoneId.of("America/New_York"), est.getTimezone());
    assertEquals(ZoneId.of("America/Los_Angeles"), pst.getTimezone());

    assertEquals(estEvent.getStartDateTime(), pstEvent.getStartDateTime());

    manager.useCalendar("EST");
    manager.copyEvent("EST Meeting", LocalDateTime.of(2025, 5, 5, 10, 0),
        "PST", LocalDateTime.of(2025, 5, 5, 10, 0));

    List<Event> pstEvents = pst.getEventsOn(LocalDate.of(2025, 5, 5));
    assertEquals(2, pstEvents.size());

    boolean found7am = false;
    for (Event e : pstEvents) {
      if (e.getSubject().equals("EST Meeting")) {
        assertEquals(LocalDateTime.of(2025, 5, 5, 7, 0), e.getStartDateTime());
        found7am = true;
      }
    }
    assertTrue(found7am);
  }

  /**
   * Integration test: Full semester copy scenario from requirements.
   */
  @Test
  public void testFullSemesterCopyScenario() {
    manager.createCalendar("Fall2024", ZoneId.of("America/New_York"));
    manager.createCalendar("Spring2025", ZoneId.of("America/New_York"));

    manager.useCalendar("Fall2024");
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);
    days.add(DayOfWeek.WEDNESDAY);
    days.add(DayOfWeek.FRIDAY);
    calendar.model.Calendar fall = manager.getCurrentCalendar();
    EventSeries lectures = new RecurringEventSeries.EventSeriesBuilder()
        .subject("CS 5010 Lecture")
        .startDateTime(LocalDateTime.of(2024, 9, 5, 13, 30))
        .endDateTime(LocalDateTime.of(2024, 9, 5, 15, 10))
        .weekdays(days)
        .repeatUntil(LocalDate.of(2024, 12, 18))
        .build();
    fall.addEventSeries(lectures);

    int originalEventCount = lectures.getAllEvents().size();
    assertTrue(originalEventCount > 30);

    manager.copyEventsBetween(LocalDate.of(2024, 9, 5),
        LocalDate.of(2024, 12, 18),
        "Spring2025",
        LocalDate.of(2025, 1, 8));

    calendar.model.Calendar spring = manager.getCalendar("Spring2025");
    List<Event> copiedLectures = spring.getEventsInRange(
        LocalDateTime.of(2025, 1, 8, 0, 0),
        LocalDateTime.of(2025, 5, 31, 23, 59));

    assertEquals(originalEventCount, copiedLectures.size());

    for (Event e : copiedLectures) {
      assertEquals("CS 5010 Lecture", e.getSubject());
      assertNotNull(e.getSeriesId());
      assertEquals(lectures.getSeriesId(), e.getSeriesId());
    }
  }
}