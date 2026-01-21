package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import calendar.model.CalendarEvent;
import calendar.model.Event;
import calendar.model.EventSeries;
import calendar.model.RecurringEventSeries;
import calendar.model.SimpleCalendar;
import calendar.model.Status;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for SimpleCalendar.
 */
public class SimpleCalendarTest {
  private SimpleCalendar calendar;

  /**
   * Initializes a fresh SimpleCalendar instance before each test run.
   */
  @Before
  public void setUp() {
    calendar = new SimpleCalendar();
  }

  @Test
  public void testAddSingleEvent() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();

    calendar.addEvent(event);
    List<Event> events = calendar.getEventsOn(LocalDate.of(2025, 5, 5));
    assertEquals(1, events.size());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddDuplicateEvent() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();

    calendar.addEvent(event);
    calendar.addEvent(event);
  }

  @Test
  public void testAddEventSeries() {
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

    calendar.addEventSeries(series);
    List<Event> events = calendar.getEventsInRange(
        LocalDateTime.of(2025, 5, 1, 0, 0),
        LocalDateTime.of(2025, 5, 31, 23, 59));

    assertEquals(4, events.size());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddEventSeriesWithDuplicate() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    calendar.addEvent(event);

    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);

    EventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .weekdays(days)
        .repeatCount(2)
        .build();

    calendar.addEventSeries(series);
  }

  @Test
  public void testRemoveEvent() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();

    calendar.addEvent(event);
    calendar.removeEvent(event);

    List<Event> events = calendar.getEventsOn(LocalDate.of(2025, 5, 5));
    assertEquals(0, events.size());
  }

  @Test
  public void testRemoveNonExistentEvent() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();

    calendar.removeEvent(event);
  }

  @Test
  public void testEditEventSubject() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();

    calendar.addEvent(event);
    calendar.editEvent("Meeting",
        LocalDateTime.of(2025, 5, 5, 10, 0),
        LocalDateTime.of(2025, 5, 5, 11, 0),
        "subject", "Updated Meeting");

    List<Event> events = calendar.getEventsOn(LocalDate.of(2025, 5, 5));
    assertEquals("Updated Meeting", events.get(0).getSubject());
  }

  @Test
  public void testEditEventDescription() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();

    calendar.addEvent(event);
    calendar.editEvent("Meeting",
        LocalDateTime.of(2025, 5, 5, 10, 0),
        LocalDateTime.of(2025, 5, 5, 11, 0),
        "description", "New description");

    List<Event> events = calendar.getEventsOn(LocalDate.of(2025, 5, 5));
    assertEquals("New description", events.get(0).getDescription());
  }

  @Test
  public void testEditEventLocation() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();

    calendar.addEvent(event);
    calendar.editEvent("Meeting",
        LocalDateTime.of(2025, 5, 5, 10, 0),
        LocalDateTime.of(2025, 5, 5, 11, 0),
        "location", "Room A");

    List<Event> events = calendar.getEventsOn(LocalDate.of(2025, 5, 5));
    assertEquals("Room A", events.get(0).getLocation());
  }

  @Test
  public void testEditEventStatus() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();

    calendar.addEvent(event);
    calendar.editEvent("Meeting",
        LocalDateTime.of(2025, 5, 5, 10, 0),
        LocalDateTime.of(2025, 5, 5, 11, 0),
        "status", "PRIVATE");

    List<Event> events = calendar.getEventsOn(LocalDate.of(2025, 5, 5));
    assertEquals(Status.PRIVATE, events.get(0).getStatus());
  }

  @Test
  public void testEditEventStartDateTime() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();

    calendar.addEvent(event);
    calendar.editEvent("Meeting",
        LocalDateTime.of(2025, 5, 5, 10, 0),
        LocalDateTime.of(2025, 5, 5, 11, 0),
        "startDateTime", LocalDateTime.of(2025, 5, 5, 11, 0));

    List<Event> laterEvents = calendar.getEventsInRange(
        LocalDateTime.of(2025, 5, 5, 11, 0),
        LocalDateTime.of(2025, 5, 5, 12, 0)
    );

    for (Event newEvent : laterEvents) {
      assertEquals(LocalTime.of(11, 0), newEvent.getStartDateTime().toLocalTime());
      assertEquals(LocalTime.of(12, 0), newEvent.getEndDateTime().toLocalTime());
    }
  }

  @Test
  public void testEditEventEndDateTime() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();

    calendar.addEvent(event);
    calendar.editEvent("Meeting",
        LocalDateTime.of(2025, 5, 5, 10, 0),
        LocalDateTime.of(2025, 5, 5, 11, 0),
        "endDateTime", LocalDateTime.of(2025, 5, 5, 12, 0));

    List<Event> events = calendar.getEventsOn(LocalDate.of(2025, 5, 5));
    assertEquals(LocalDateTime.of(2025, 5, 5, 12, 0), events.get(0).getEndDateTime());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditEventNotFound() {
    calendar.editEvent("NonExistent",
        LocalDateTime.of(2025, 5, 5, 10, 0),
        LocalDateTime.of(2025, 5, 5, 11, 0),
        "subject", "New");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditEventCreatesDuplicate() {
    Event event1 = new CalendarEvent.EventBuilder()
        .subject("Meeting1")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();

    Event event2 = new CalendarEvent.EventBuilder()
        .subject("Meeting2")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();

    calendar.addEvent(event1);
    calendar.addEvent(event2);

    calendar.editEvent("Meeting2",
        LocalDateTime.of(2025, 5, 5, 10, 0),
        LocalDateTime.of(2025, 5, 5, 11, 0),
        "subject", "Meeting1");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditEventUnknownProperty() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();

    calendar.addEvent(event);
    calendar.editEvent("Meeting",
        LocalDateTime.of(2025, 5, 5, 10, 0),
        LocalDateTime.of(2025, 5, 5, 11, 0),
        "unknownProperty", "value");
  }

  @Test
  public void testEditEventsFromSingleEvent() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();

    calendar.addEvent(event);
    calendar.editEventsFrom("Meeting",
        LocalDateTime.of(2025, 5, 5, 10, 0), null,
        "subject", "Updated");

    List<Event> events = calendar.getEventsOn(LocalDate.of(2025, 5, 5));
    assertEquals("Updated", events.get(0).getSubject());
  }

  @Test
  public void testEditEventsFromSeriesEvent() {
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

    calendar.addEventSeries(series);
    List<Event> allEvents = series.getAllEvents();

    calendar.editEventsFrom("Standup",
        allEvents.get(1).getStartDateTime(), null,
        "subject", "Modified");

    List<Event> events = calendar.getEventsInRange(
        LocalDateTime.of(2025, 5, 1, 0, 0),
        LocalDateTime.of(2025, 5, 31, 23, 59));

    int modifiedCount = 0;
    int originalCount = 0;
    for (Event e : events) {
      if (e.getSubject().equals("Modified")) {
        modifiedCount++;
      }
      if (e.getSubject().equals("Standup")) {
        originalCount++;
      }
    }

    assertEquals(3, modifiedCount);
    assertEquals(1, originalCount);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditEventsFromNotFound() {
    calendar.editEventsFrom("NonExistent",
        LocalDateTime.of(2025, 5, 5, 10, 0), null,
        "subject", "New");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditEventsFromMultipleMatches() {
    Event event1 = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();

    Event event2 = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 12, 0))
        .build();

    calendar.addEvent(event1);
    calendar.addEvent(event2);

    calendar.editEventsFrom("Meeting",
        LocalDateTime.of(2025, 5, 5, 10, 0), null,
        "subject", "New");
  }

  @Test
  public void testEditAllEventsInSeriesSingleEvent() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();

    calendar.addEvent(event);
    calendar.editAllEventsInSeries("Meeting",
        LocalDateTime.of(2025, 5, 5, 10, 0), null,
        "subject", "Updated");

    List<Event> events = calendar.getEventsOn(LocalDate.of(2025, 5, 5));
    assertEquals("Updated", events.get(0).getSubject());
  }

  @Test
  public void testEditAllEventsInSeriesAll() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);

    EventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Standup")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 9, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 9, 30))
        .weekdays(days)
        .repeatCount(3)
        .build();

    calendar.addEventSeries(series);
    List<Event> allEvents = series.getAllEvents();

    calendar.editAllEventsInSeries("Standup",
        allEvents.get(0).getStartDateTime(), null,
        "subject", "Modified");

    List<Event> events = calendar.getEventsInRange(
        LocalDateTime.of(2025, 5, 1, 0, 0),
        LocalDateTime.of(2025, 5, 31, 23, 59));

    for (Event e : events) {
      assertEquals("Modified", e.getSubject());
    }
  }

  @Test
  public void testGetEventsOn() {
    Event event1 = new CalendarEvent.EventBuilder()
        .subject("Morning")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 9, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .build();

    Event event2 = new CalendarEvent.EventBuilder()
        .subject("Afternoon")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 14, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 15, 0))
        .build();

    calendar.addEvent(event1);
    calendar.addEvent(event2);

    List<Event> events = calendar.getEventsOn(LocalDate.of(2025, 5, 5));
    assertEquals(2, events.size());
  }

  @Test
  public void testGetEventsOnNoEvents() {
    List<Event> events = calendar.getEventsOn(LocalDate.of(2025, 5, 5));
    assertEquals(0, events.size());
  }

  @Test
  public void testGetEventsOnSorted() {
    Event event1 = new CalendarEvent.EventBuilder()
        .subject("Second")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 14, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 15, 0))
        .build();

    Event event2 = new CalendarEvent.EventBuilder()
        .subject("First")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 9, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .build();

    calendar.addEvent(event1);
    calendar.addEvent(event2);

    List<Event> events = calendar.getEventsOn(LocalDate.of(2025, 5, 5));
    assertEquals("First", events.get(0).getSubject());
    assertEquals("Second", events.get(1).getSubject());
  }

  @Test
  public void testGetEventsInRange() {
    Event event1 = new CalendarEvent.EventBuilder()
        .subject("Meeting1")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();

    Event event2 = new CalendarEvent.EventBuilder()
        .subject("Meeting2")
        .startDateTime(LocalDateTime.of(2025, 5, 6, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 6, 11, 0))
        .build();

    calendar.addEvent(event1);
    calendar.addEvent(event2);

    List<Event> events = calendar.getEventsInRange(
        LocalDateTime.of(2025, 5, 5, 0, 0),
        LocalDateTime.of(2025, 5, 7, 0, 0));

    assertEquals(2, events.size());
  }

  @Test
  public void testGetEventsInRangePartialOverlap() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 12, 0))
        .build();

    calendar.addEvent(event);

    List<Event> events = calendar.getEventsInRange(
        LocalDateTime.of(2025, 5, 5, 11, 0),
        LocalDateTime.of(2025, 5, 5, 13, 0));

    assertEquals(1, events.size());
  }

  @Test
  public void testGetEventsInRangeNoEvents() {
    List<Event> events = calendar.getEventsInRange(
        LocalDateTime.of(2025, 5, 5, 0, 0),
        LocalDateTime.of(2025, 5, 6, 0, 0));

    assertEquals(0, events.size());
  }

  @Test
  public void testIsBusyTrue() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();

    calendar.addEvent(event);
    assertTrue(calendar.isBusy(LocalDateTime.of(2025, 5, 5, 10, 30)));
  }

  @Test
  public void testIsBusyAtStart() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();

    calendar.addEvent(event);
    assertTrue(calendar.isBusy(LocalDateTime.of(2025, 5, 5, 10, 0)));
  }

  @Test
  public void testIsBusyAtEndFalse() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();

    calendar.addEvent(event);
    assertFalse(calendar.isBusy(LocalDateTime.of(2025, 5, 5, 11, 0)));
  }

  @Test
  public void testIsBusyFalse() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();

    calendar.addEvent(event);
    assertFalse(calendar.isBusy(LocalDateTime.of(2025, 5, 5, 9, 0)));
  }

  @Test
  public void testIsBusyNoEvents() {
    assertFalse(calendar.isBusy(LocalDateTime.of(2025, 5, 5, 10, 0)));
  }

  @Test
  public void testExportToCsv() throws IOException {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .description("Test meeting")
        .location("Room A")
        .status(Status.PRIVATE)
        .build();

    calendar.addEvent(event);
    String path = calendar.exportToCsv("test.csv");

    assertNotNull(path);
    Path filePath = Paths.get(path);
    assertTrue(Files.exists(filePath));

    List<String> lines = Files.readAllLines(filePath);
    assertTrue(lines.size() >= 2);
    assertTrue(lines.get(0).contains("Subject"));
    assertTrue(lines.get(1).contains("Meeting"));

    Files.deleteIfExists(filePath);
  }

  @Test
  public void testExportToCsvWithEscaping() throws IOException {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting, with comma")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .description("Description with \"quotes\"")
        .build();

    calendar.addEvent(event);
    String path = calendar.exportToCsv("test_escape.csv");

    Path filePath = Paths.get(path);
    List<String> lines = Files.readAllLines(filePath);
    assertTrue(lines.get(1).contains("\"Meeting, with comma\""));

    Files.deleteIfExists(filePath);
  }

  @Test
  public void testExportToCsvAllDayEvent() throws IOException {
    Event event = new CalendarEvent.EventBuilder()
        .subject("All Day")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 0, 0))
        .build();

    calendar.addEvent(event);
    String path = calendar.exportToCsv("test_allday.csv");

    Path filePath = Paths.get(path);
    List<String> lines = Files.readAllLines(filePath);
    assertTrue(lines.get(1).contains("True"));

    Files.deleteIfExists(filePath);
  }

  @Test
  public void testExportToCsvMultipleEvents() throws IOException {
    Event event1 = new CalendarEvent.EventBuilder()
        .subject("First")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();

    Event event2 = new CalendarEvent.EventBuilder()
        .subject("Second")
        .startDateTime(LocalDateTime.of(2025, 5, 6, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 6, 11, 0))
        .build();

    calendar.addEvent(event1);
    calendar.addEvent(event2);
    String path = calendar.exportToCsv("test_multiple.csv");

    Path filePath = Paths.get(path);
    List<String> lines = Files.readAllLines(filePath);
    assertEquals(3, lines.size());

    Files.deleteIfExists(filePath);
  }

  @Test
  public void testExportCreatesDirectory() throws IOException {
    Path exportDir = Paths.get("exports");
    if (Files.exists(exportDir)) {
      Files.walk(exportDir)
          .sorted((a, b) -> b.compareTo(a))
          .forEach(p -> {
            try {
              Files.deleteIfExists(p);
            } catch (IOException e) {
              e.printStackTrace();
            }
          });
    }

    String path = calendar.exportToCsv("test_dir.csv");
    assertTrue(Files.exists(Paths.get("exports")));

    Files.deleteIfExists(Paths.get(path));
  }

  @Test
  public void testEditEventsFromSeriesWithNonDateTimeProperty() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);
    days.add(DayOfWeek.THURSDAY);

    EventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 6, 14, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 6, 15, 0))
        .weekdays(days)
        .repeatCount(4)
        .build();

    calendar.addEventSeries(series);
    List<Event> allEvents = series.getAllEvents();
    String originalSeriesId = allEvents.get(0).getSeriesId();

    calendar.editEventsFrom("Meeting",
        allEvents.get(2).getStartDateTime(), null,
        "location", "Conference Room");

    List<Event> events = calendar.getEventsInRange(
        LocalDateTime.of(2025, 5, 1, 0, 0),
        LocalDateTime.of(2025, 5, 31, 23, 59));

    for (Event e : events) {
      assertEquals(originalSeriesId, e.getSeriesId());
    }
  }

  @Test
  public void testEditAllEventsInSeriesVerifyAllUpdated() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.FRIDAY);

    EventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Weekly")
        .startDateTime(LocalDateTime.of(2025, 5, 2, 16, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 2, 17, 0))
        .weekdays(days)
        .repeatCount(5)
        .build();

    calendar.addEventSeries(series);
    List<Event> allEvents = series.getAllEvents();

    calendar.editAllEventsInSeries("Weekly",
        allEvents.get(2).getStartDateTime(), null,
        "description", "Updated description");

    List<Event> events = calendar.getEventsInRange(
        LocalDateTime.of(2025, 5, 1, 0, 0),
        LocalDateTime.of(2025, 5, 31, 23, 59));

    assertEquals(5, events.size());
    for (Event e : events) {
      assertEquals("Updated description", e.getDescription());
    }
  }

  @Test
  public void testExportToCsvWithNullAndEmptyFields() throws IOException {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Simple")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .description("")
        .location("")
        .build();

    calendar.addEvent(event);
    String path = calendar.exportToCsv("test_empty.csv");

    Path filePath = Paths.get(path);
    List<String> lines = Files.readAllLines(filePath);
    assertTrue(lines.size() >= 2);

    Files.deleteIfExists(filePath);
  }

  @Test
  public void testExportToCsvWithNewlineInDescription() throws IOException {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .description("First line\nSecond line")
        .location("Room\nA")
        .build();

    calendar.addEvent(event);
    String path = calendar.exportToCsv("test_newline.csv");

    Path filePath = Paths.get(path);
    assertTrue(Files.exists(filePath));
    List<String> lines = Files.readAllLines(filePath);
    assertTrue(lines.size() >= 2);

    Files.deleteIfExists(filePath);
  }

  @Test
  public void testFindEventSubjectMatchesButStartDoesNot() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    calendar.addEvent(event);

    try {
      calendar.editEvent("Meeting",
          LocalDateTime.of(2025, 5, 5, 14, 0),
          LocalDateTime.of(2025, 5, 5, 11, 0),
          "location", "Room");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("not found"));
    }
  }

  @Test
  public void testFindEventSubjectAndStartMatchButEndDoesNot() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    calendar.addEvent(event);

    try {
      calendar.editEvent("Meeting",
          LocalDateTime.of(2025, 5, 5, 10, 0),
          LocalDateTime.of(2025, 5, 5, 12, 0),
          "location", "Room");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("not found"));
    }
  }

  @Test
  public void testFindEventForSeriesSubjectMatchStartDoesNot() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    calendar.addEvent(event);

    try {
      calendar.editEventsFrom("Meeting",
          LocalDateTime.of(2025, 5, 5, 14, 0), null,
          "subject", "New");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("not found"));
    }
  }

  @Test
  public void testFindEventForSeriesReturnsSingleEvent() {
    Event singleEvent = new CalendarEvent.EventBuilder()
        .subject("Single")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    calendar.addEvent(singleEvent);

    calendar.editEventsFrom("Single",
        LocalDateTime.of(2025, 5, 5, 10, 0), null,
        "description", "Updated");

    assertEquals("Updated", calendar.getEventsOn(LocalDate.of(2025, 5, 5)).get(0).getDescription());
  }

  @Test
  public void testEditAllEventsInSeriesWithNonMatchingEvents() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);

    EventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Series1")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 9, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .weekdays(days)
        .repeatCount(2)
        .build();
    calendar.addEventSeries(series);

    Event nonSeriesEvent = new CalendarEvent.EventBuilder()
        .subject("Single")
        .startDateTime(LocalDateTime.of(2025, 5, 6, 9, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 6, 10, 0))
        .build();
    calendar.addEvent(nonSeriesEvent);

    calendar.editAllEventsInSeries("Series1",
        LocalDateTime.of(2025, 5, 5, 9, 0), null,
        "location", "Room A");

    List<Event> allEvents = calendar.getEventsInRange(
        LocalDateTime.of(2025, 5, 1, 0, 0),
        LocalDateTime.of(2025, 5, 31, 23, 59));

    int withLocation = 0;
    int withoutLocation = 0;
    for (Event e : allEvents) {
      if (e.getLocation().equals("Room A")) {
        withLocation++;
      } else {
        withoutLocation++;
      }
    }

    assertEquals(2, withLocation);
    assertEquals(1, withoutLocation);
  }

  @Test
  public void testGetEventsOnEventStartsAfterDayEnd() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("LateEvent")
        .startDateTime(LocalDateTime.of(2025, 5, 6, 0, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 6, 1, 0))
        .build();
    calendar.addEvent(event);

    List<Event> events = calendar.getEventsOn(LocalDate.of(2025, 5, 5));
    assertEquals(0, events.size());
  }

  @Test
  public void testGetEventsOnEventEndsBeforeDayStart() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("EarlyEvent")
        .startDateTime(LocalDateTime.of(2025, 5, 4, 23, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 0, 0))
        .build();
    calendar.addEvent(event);

    List<Event> events = calendar.getEventsOn(LocalDate.of(2025, 5, 5));
    assertEquals(0, events.size());
  }

  @Test
  public void testEditEventsFromWithStartProperty() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);

    EventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .weekdays(days)
        .repeatCount(3)
        .build();
    calendar.addEventSeries(series);

    calendar.editEventsFrom("Meeting",
        LocalDateTime.of(2025, 5, 5, 10, 0), null,
        "subject",
        "Updated");

    List<Event> events = calendar.getEventsInRange(
        LocalDateTime.of(2025, 5, 1, 0, 0),
        LocalDateTime.of(2025, 5, 31, 23, 59));

    assertTrue(events.stream().anyMatch(e -> e.getSubject().equals("Updated")));
  }

  @Test
  public void testEditEventsFromWithStartDateTimeProperty() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.WEDNESDAY);

    EventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Standup")
        .startDateTime(LocalDateTime.of(2025, 5, 7, 9, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 7, 9, 30))
        .weekdays(days)
        .repeatCount(3)
        .build();
    calendar.addEventSeries(series);

    calendar.editEventsFrom("Standup",
        LocalDateTime.of(2025, 5, 7, 9, 0), null,
        "description",
        "Daily meeting");

    List<Event> events = calendar.getEventsInRange(
        LocalDateTime.of(2025, 5, 1, 0, 0),
        LocalDateTime.of(2025, 5, 31, 23, 59));

    assertEquals(3, events.size());
    for (Event e : events) {
      assertEquals("Daily meeting", e.getDescription());
    }
  }

  @Test
  public void testEditEventsFromWithNonStartProperty() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.FRIDAY);

    EventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Review")
        .startDateTime(LocalDateTime.of(2025, 5, 2, 14, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 2, 15, 0))
        .weekdays(days)
        .repeatCount(3)
        .build();
    calendar.addEventSeries(series);

    calendar.editEventsFrom("Review",
        LocalDateTime.of(2025, 5, 2, 14, 0), null,
        "location",
        "Room 101");

    List<Event> events = calendar.getEventsInRange(
        LocalDateTime.of(2025, 5, 1, 0, 0),
        LocalDateTime.of(2025, 5, 31, 23, 59));

    assertEquals(3, events.size());
    for (Event e : events) {
      assertEquals("Room 101", e.getLocation());
    }
  }

  @Test
  public void testEditSingleEventStartTimeDetachesFromSeries() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);

    RecurringEventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .weekdays(days)
        .repeatCount(5)
        .build();

    calendar.addEventSeries(series);
    final String originalSeriesId = series.getSeriesId();

    // Edit single event's start time
    calendar.editEvent("Meeting",
        LocalDateTime.of(2025, 5, 5, 10, 0),
        LocalDateTime.of(2025, 5, 5, 11, 0),
        "startdatetime",
        LocalDateTime.of(2025, 5, 5, 10, 30));

    // Check that edited event is detached
    List<Event> events = calendar.getEventsOn(LocalDate.of(2025, 5, 5));
    assertEquals(1, events.size());
    assertNull(events.get(0).getSeriesId());

    // Check that other events still have series ID
    List<Event> laterEvents = calendar.getEventsOn(LocalDate.of(2025, 5, 12));
    assertEquals(1, laterEvents.size());
    assertEquals(originalSeriesId, laterEvents.get(0).getSeriesId());
  }

  @Test
  public void testEditSingleEventEndTimeDetachesFromSeries() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);

    RecurringEventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Review")
        .startDateTime(LocalDateTime.of(2025, 5, 6, 14, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 6, 15, 0))
        .weekdays(days)
        .repeatCount(3)
        .build();

    calendar.addEventSeries(series);

    // Edit single event's end time
    calendar.editEvent("Review",
        LocalDateTime.of(2025, 5, 6, 14, 0),
        LocalDateTime.of(2025, 5, 6, 15, 0),
        "enddatetime",
        LocalDateTime.of(2025, 5, 6, 16, 0));

    List<Event> events = calendar.getEventsOn(LocalDate.of(2025, 5, 6));
    assertNull(events.get(0).getSeriesId());
  }

  @Test
  public void testEditSingleEventSubjectDoesNotDetach() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.WEDNESDAY);

    RecurringEventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Standup")
        .startDateTime(LocalDateTime.of(2025, 5, 7, 9, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 7, 9, 30))
        .weekdays(days)
        .repeatCount(3)
        .build();

    calendar.addEventSeries(series);
    String originalSeriesId = series.getSeriesId();

    // Edit single event's subject
    calendar.editEvent("Standup",
        LocalDateTime.of(2025, 5, 7, 9, 0),
        LocalDateTime.of(2025, 5, 7, 9, 30),
        "subject",
        "Daily Standup");

    // Should still have series ID
    List<Event> events = calendar.getEventsOn(LocalDate.of(2025, 5, 7));
    assertNotNull(events.get(0).getSeriesId());
    assertEquals(originalSeriesId, events.get(0).getSeriesId());
  }

  @Test
  public void testEditEventsFromPreservesDate() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.THURSDAY);
    days.add(DayOfWeek.FRIDAY);

    RecurringEventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 1, 11, 0))
        .weekdays(days)
        .repeatCount(6)
        .build();

    calendar.addEventSeries(series);

    calendar.editEventsFrom("Meeting",
        LocalDateTime.of(2025, 5, 8, 10, 0), null,
        "startdatetime",
        LocalDateTime.of(2025, 5, 8, 10, 30));

    List<Event> laterEvents = calendar.getEventsInRange(
        LocalDateTime.of(2025, 5, 8, 0, 0),
        LocalDateTime.of(2025, 5, 31, 23, 59)
    );

    for (Event event : laterEvents) {
      assertEquals(LocalTime.of(10, 30), event.getStartDateTime().toLocalTime());
      assertEquals(LocalTime.of(11, 30), event.getEndDateTime().toLocalTime());
    }
  }
}