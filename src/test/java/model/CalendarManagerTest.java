package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import calendar.model.Calendar;
import calendar.model.CalendarEvent;
import calendar.model.CalendarManager;
import calendar.model.Event;
import calendar.model.EventSeries;
import calendar.model.RecurringEventSeries;
import calendar.model.SimpleCalendarManager;
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
 * Test class for SimpleCalendarManager and Calendar management functionality.
 */
public class CalendarManagerTest {
  private CalendarManager manager;

  /**
   * Tests calendar creation, editing, switching between calendars, and copying
   * events across calendars with timezone handling.
   */
  @Before
  public void setUp() {
    manager = new SimpleCalendarManager();
  }

  @Test
  public void testCreateCalendar() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    assertTrue(manager.hasCalendar("Work"));
  }

  @Test
  public void testCreateMultipleCalendars() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.createCalendar("Personal", ZoneId.of("America/Los_Angeles"));

    assertTrue(manager.hasCalendar("Work"));
    assertTrue(manager.hasCalendar("Personal"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateCalendarNullName() {
    manager.createCalendar(null, ZoneId.of("America/New_York"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateCalendarEmptyName() {
    manager.createCalendar("", ZoneId.of("America/New_York"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateCalendarWhitespaceName() {
    manager.createCalendar("   ", ZoneId.of("America/New_York"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateCalendarNullTimezone() {
    manager.createCalendar("Work", null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateCalendarDuplicateName() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.createCalendar("Work", ZoneId.of("America/Chicago"));
  }

  @Test
  public void testGetCalendar() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    Calendar cal = manager.getCalendar("Work");

    assertNotNull(cal);
    assertEquals("Work", cal.getName());
    assertEquals(ZoneId.of("America/New_York"), cal.getTimezone());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetCalendarNonExistent() {
    manager.getCalendar("NonExistent");
  }

  @Test
  public void testUseCalendar() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    Calendar current = manager.getCurrentCalendar();
    assertNotNull(current);
    assertEquals("Work", current.getName());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUseCalendarNonExistent() {
    manager.useCalendar("NonExistent");
  }

  @Test
  public void testGetCurrentCalendarNull() {
    Calendar current = manager.getCurrentCalendar();
    assertNull(current);
  }

  @Test
  public void testEditCalendarName() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.editCalendar("Work", "name", "Office");

    assertTrue(manager.hasCalendar("Office"));
    assertFalse(manager.hasCalendar("Work"));
  }

  @Test
  public void testEditCalendarTimezoneString() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.editCalendar("Work", "timezone", "America/Chicago");

    Calendar cal = manager.getCalendar("Work");
    assertEquals(ZoneId.of("America/Chicago"), cal.getTimezone());
  }

  @Test
  public void testEditCalendarTimezoneZoneId() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.editCalendar("Work", "timezone", ZoneId.of("Europe/London"));

    Calendar cal = manager.getCalendar("Work");
    assertEquals(ZoneId.of("Europe/London"), cal.getTimezone());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditCalendarNullProperty() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.editCalendar("Work", null, "value");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditCalendarEmptyProperty() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.editCalendar("Work", "", "value");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditCalendarInvalidProperty() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.editCalendar("Work", "invalid", "value");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditCalendarNonExistent() {
    manager.editCalendar("NonExistent", "name", "New");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditCalendarNameToDuplicate() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.createCalendar("Personal", ZoneId.of("America/Chicago"));
    manager.editCalendar("Work", "name", "Personal");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditCalendarInvalidTimezoneString() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.editCalendar("Work", "timezone", "Invalid/Timezone");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditCalendarTimezoneInvalidType() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.editCalendar("Work", "timezone", 12345);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditCalendarNameInvalidType() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.editCalendar("Work", "name", 12345);
  }

  @Test
  public void testCopyEventSameCalendar() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    Calendar work = manager.getCurrentCalendar();
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    work.addEvent(event);

    manager.copyEvent("Meeting", LocalDateTime.of(2025, 5, 5, 10, 0),
        "Work", LocalDateTime.of(2025, 5, 6, 14, 0));

    List<Event> events = work.getEventsOn(LocalDate.of(2025, 5, 6));
    assertEquals(1, events.size());
    assertEquals("Meeting", events.get(0).getSubject());
  }

  @Test
  public void testCopyEventDifferentCalendars() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.createCalendar("Personal", ZoneId.of("America/Chicago"));
    manager.useCalendar("Work");

    Calendar work = manager.getCurrentCalendar();
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    work.addEvent(event);

    manager.copyEvent("Meeting", LocalDateTime.of(2025, 5, 5, 10, 0),
        "Personal", LocalDateTime.of(2025, 5, 6, 10, 0));

    Calendar personal = manager.getCalendar("Personal");
    List<Event> events = personal.getEventsOn(LocalDate.of(2025, 5, 6));
    assertEquals(1, events.size());
  }

  @Test
  public void testCopyEventWithTimezoneConversion() {
    manager.createCalendar("EST", ZoneId.of("America/New_York"));
    manager.createCalendar("PST", ZoneId.of("America/Los_Angeles"));
    manager.useCalendar("EST");

    Calendar est = manager.getCurrentCalendar();
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 14, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 15, 0))
        .build();
    est.addEvent(event);

    manager.copyEvent("Meeting", LocalDateTime.of(2025, 5, 5, 14, 0),
        "PST", LocalDateTime.of(2025, 5, 5, 14, 0));

    Calendar pst = manager.getCalendar("PST");
    List<Event> events = pst.getEventsOn(LocalDate.of(2025, 5, 5));
    assertEquals(1, events.size());
    assertEquals(LocalDateTime.of(2025, 5, 5, 11, 0),
        events.get(0).getStartDateTime());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCopyEventNoCurrentCalendar() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.copyEvent("Meeting", LocalDateTime.of(2025, 5, 5, 10, 0),
        "Work", LocalDateTime.of(2025, 5, 6, 10, 0));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCopyEventTargetCalendarNotFound() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    Calendar work = manager.getCurrentCalendar();
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    work.addEvent(event);

    manager.copyEvent("Meeting", LocalDateTime.of(2025, 5, 5, 10, 0),
        "NonExistent", LocalDateTime.of(2025, 5, 6, 10, 0));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCopyEventNotFound() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    manager.copyEvent("NonExistent", LocalDateTime.of(2025, 5, 5, 10, 0),
        "Work", LocalDateTime.of(2025, 5, 6, 10, 0));
  }

  @Test
  public void testCopyEventsOnDate() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    Calendar work = manager.getCurrentCalendar();
    Event event1 = new CalendarEvent.EventBuilder()
        .subject("Meeting1")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    Event event2 = new CalendarEvent.EventBuilder()
        .subject("Meeting2")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 14, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 15, 0))
        .build();
    work.addEvent(event1);
    work.addEvent(event2);

    manager.copyEventsOnDate(LocalDate.of(2025, 5, 5), "Work",
        LocalDate.of(2025, 5, 10));

    List<Event> events = work.getEventsOn(LocalDate.of(2025, 5, 10));
    assertEquals(2, events.size());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCopyEventsOnDateNoCurrentCalendar() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.copyEventsOnDate(LocalDate.of(2025, 5, 5), "Work",
        LocalDate.of(2025, 5, 10));
  }

  @Test
  public void testCopyEventsBetween() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    Calendar work = manager.getCurrentCalendar();
    Event event1 = new CalendarEvent.EventBuilder()
        .subject("Meeting1")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    Event event2 = new CalendarEvent.EventBuilder()
        .subject("Meeting2")
        .startDateTime(LocalDateTime.of(2025, 5, 7, 14, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 7, 15, 0))
        .build();
    work.addEvent(event1);
    work.addEvent(event2);

    manager.copyEventsBetween(LocalDate.of(2025, 5, 5),
        LocalDate.of(2025, 5, 10), "Work", LocalDate.of(2025, 6, 1));

    List<Event> events = work.getEventsInRange(
        LocalDateTime.of(2025, 6, 1, 0, 0),
        LocalDateTime.of(2025, 6, 10, 23, 59));
    assertEquals(2, events.size());
  }

  @Test
  public void testCopyEventsBetweenPartialSeries() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    final Calendar work = manager.getCurrentCalendar();
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);
    days.add(DayOfWeek.WEDNESDAY);
    days.add(DayOfWeek.FRIDAY);

    EventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Standup")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 9, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 9, 30))
        .weekdays(days)
        .repeatCount(10)
        .build();
    work.addEventSeries(series);

    manager.copyEventsBetween(LocalDate.of(2025, 5, 7),
        LocalDate.of(2025, 5, 14), "Work", LocalDate.of(2025, 6, 1));

    List<Event> copiedEvents = work.getEventsInRange(
        LocalDateTime.of(2025, 6, 1, 0, 0),
        LocalDateTime.of(2025, 6, 15, 23, 59));

    assertTrue(copiedEvents.size() > 0);
    for (Event e : copiedEvents) {
      assertNotNull(e.getSeriesId());
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCopyEventsBetweenNoCurrentCalendar() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.copyEventsBetween(LocalDate.of(2025, 5, 5),
        LocalDate.of(2025, 5, 10), "Work", LocalDate.of(2025, 6, 1));
  }

  @Test
  public void testHasCalendarTrue() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    assertTrue(manager.hasCalendar("Work"));
  }

  @Test
  public void testHasCalendarFalse() {
    assertFalse(manager.hasCalendar("NonExistent"));
  }

  @Test
  public void testCopyEventWithSeriesId() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    Calendar work = manager.getCurrentCalendar();
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);

    EventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .weekdays(days)
        .repeatCount(3)
        .build();
    work.addEventSeries(series);

    manager.copyEvent("Meeting", LocalDateTime.of(2025, 5, 5, 10, 0),
        "Work", LocalDateTime.of(2025, 6, 1, 10, 0));

    List<Event> copied = work.getEventsOn(LocalDate.of(2025, 6, 1));
    assertEquals(1, copied.size());
    assertNotNull(copied.get(0).getSeriesId());
  }

  @Test
  public void testCopyEventsOnDateWithDifferentTimezones() {
    manager.createCalendar("EST", ZoneId.of("America/New_York"));
    manager.createCalendar("PST", ZoneId.of("America/Los_Angeles"));
    manager.useCalendar("EST");

    Calendar est = manager.getCurrentCalendar();
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 14, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 15, 0))
        .build();
    est.addEvent(event);

    manager.copyEventsOnDate(LocalDate.of(2025, 5, 5), "PST",
        LocalDate.of(2025, 5, 10));

    Calendar pst = manager.getCalendar("PST");
    List<Event> events = pst.getEventsOn(LocalDate.of(2025, 5, 10));
    assertEquals(1, events.size());
  }

  @Test
  public void testCopyEventsSkipDuplicates() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    Calendar work = manager.getCurrentCalendar();
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    work.addEvent(event);

    Event duplicate = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 10, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 10, 11, 0))
        .build();
    work.addEvent(duplicate);

    manager.copyEventsOnDate(LocalDate.of(2025, 5, 5), "Work",
        LocalDate.of(2025, 5, 10));

    List<Event> events = work.getEventsOn(LocalDate.of(2025, 5, 10));
    assertEquals(1, events.size());
  }

  @Test
  public void testEditCalendarNameCaseInsensitive() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.editCalendar("Work", "NAME", "Office");

    assertTrue(manager.hasCalendar("Office"));
  }

  @Test
  public void testEditCalendarTimezoneCaseInsensitive() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.editCalendar("Work", "TIMEZONE", "America/Chicago");

    Calendar cal = manager.getCalendar("Work");
    assertEquals(ZoneId.of("America/Chicago"), cal.getTimezone());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCopyEventMultipleMatches() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    Calendar work = manager.getCurrentCalendar();
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
    work.addEvent(event1);
    work.addEvent(event2);

    manager.copyEvent("Meeting", LocalDateTime.of(2025, 5, 5, 10, 0),
        "Work", LocalDateTime.of(2025, 5, 6, 10, 0));
  }

  /**
   * Test that copying a single event from a recurring series to a different calendar
   * preserves the series ID (event is still marked as part of a series).
   */
  @Test
  public void testCopyRecurringEventToDifferentCalendar() {
    manager.createCalendar("Source", ZoneId.of("America/New_York"));
    manager.createCalendar("Target", ZoneId.of("America/Chicago"));
    manager.useCalendar("Source");

    Calendar source = manager.getCurrentCalendar();
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);
    days.add(DayOfWeek.WEDNESDAY);

    EventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Standup")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 9, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 9, 30))
        .weekdays(days)
        .repeatCount(5)
        .build();
    source.addEventSeries(series);

    manager.copyEvent("Standup", LocalDateTime.of(2025, 5, 5, 9, 0),
        "Target", LocalDateTime.of(2025, 6, 1, 9, 0));

    Calendar target = manager.getCalendar("Target");
    List<Event> copiedEvents = target.getEventsOn(LocalDate.of(2025, 6, 1));
    String originalSeriesId = series.getSeriesId();
    assertEquals(1, copiedEvents.size());
    assertNotNull(copiedEvents.get(0).getSeriesId());
    assertEquals(originalSeriesId, copiedEvents.get(0).getSeriesId());
  }

  /**
   * Test that copying a single event from a recurring series to the same calendar
   * preserves the series ID.
   */
  @Test
  public void testCopyRecurringEventToSameCalendar() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    Calendar work = manager.getCurrentCalendar();
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);

    EventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 6, 14, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 6, 15, 0))
        .weekdays(days)
        .repeatCount(3)
        .build();
    work.addEventSeries(series);

    manager.copyEvent("Meeting", LocalDateTime.of(2025, 5, 6, 14, 0),
        "Work", LocalDateTime.of(2025, 6, 1, 14, 0));

    List<Event> copiedEvents = work.getEventsOn(LocalDate.of(2025, 6, 1));

    assertEquals(1, copiedEvents.size());
    assertNotNull(copiedEvents.get(0).getSeriesId());
    String originalSeriesId = series.getSeriesId();
    assertEquals(originalSeriesId, copiedEvents.get(0).getSeriesId());
  }

  /**
   * Test that when copying events between calendars with a partial series overlap,
   * only the overlapping events are copied and they retain their series ID.
   */
  @Test
  public void testCopyEventsBetweenPartialSeriesPreservesSeriesId() {
    manager.createCalendar("Fall2024", ZoneId.of("America/New_York"));
    manager.createCalendar("Spring2025", ZoneId.of("America/New_York"));
    manager.useCalendar("Fall2024");

    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);
    days.add(DayOfWeek.WEDNESDAY);
    days.add(DayOfWeek.FRIDAY);
    EventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Lecture")
        .startDateTime(LocalDateTime.of(2024, 9, 2, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 9, 2, 11, 30))
        .weekdays(days)
        .repeatUntil(LocalDate.of(2024, 12, 20))
        .build();
    Calendar fall = manager.getCurrentCalendar();
    fall.addEventSeries(series);

    int totalEventsInSeries = series.getAllEvents().size();
    manager.copyEventsBetween(LocalDate.of(2024, 9, 5),
        LocalDate.of(2024, 9, 30),
        "Spring2025",
        LocalDate.of(2025, 1, 8));

    Calendar spring = manager.getCalendar("Spring2025");
    List<Event> copiedEvents = spring.getEventsInRange(
        LocalDateTime.of(2025, 1, 8, 0, 0),
        LocalDateTime.of(2025, 2, 28, 23, 59));

    assertFalse(copiedEvents.isEmpty());

    assertTrue(copiedEvents.size() < totalEventsInSeries);
    String originalSeriesId = series.getSeriesId();

    for (Event event : copiedEvents) {
      assertNotNull(event.getSeriesId());
      assertEquals(originalSeriesId, event.getSeriesId());
    }
  }

  /**
   * Test copying a complete recurring series (all events) to different calendar.
   */
  @Test
  public void testCopyCompleteRecurringSeriesToDifferentCalendar() {
    manager.createCalendar("Source", ZoneId.of("America/New_York"));
    manager.createCalendar("Target", ZoneId.of("America/Los_Angeles"));
    manager.useCalendar("Source");

    Calendar source = manager.getCurrentCalendar();
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.THURSDAY);

    EventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Weekly Review")
        .startDateTime(LocalDateTime.of(2025, 5, 1, 15, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 1, 16, 0))
        .weekdays(days)
        .repeatCount(4)
        .build();
    source.addEventSeries(series);

    String originalSeriesId = series.getSeriesId();

    manager.copyEventsBetween(LocalDate.of(2025, 5, 1),
        LocalDate.of(2025, 5, 31),
        "Target",
        LocalDate.of(2025, 6, 1));

    Calendar target = manager.getCalendar("Target");
    List<Event> copiedEvents = target.getEventsInRange(
        LocalDateTime.of(2025, 6, 1, 0, 0),
        LocalDateTime.of(2025, 6, 30, 23, 59));

    assertEquals(4, copiedEvents.size());

    for (Event event : copiedEvents) {
      assertNotNull(event.getSeriesId());
      assertEquals(originalSeriesId, event.getSeriesId());
    }

    assertEquals(LocalDateTime.of(2025, 6, 1, 12, 0),
        copiedEvents.get(0).getStartDateTime());
  }

  /**
   * Test that copied events are distinct objects, not references to originals.
   */
  @Test
  public void testCopiedEventIsNewInstance() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    Calendar work = manager.getCurrentCalendar();
    Event original = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .description("Original description")
        .build();
    work.addEvent(original);

    manager.copyEvent("Meeting", LocalDateTime.of(2025, 5, 5, 10, 0),
        "Work", LocalDateTime.of(2025, 5, 10, 10, 0));

    List<Event> originalDateEvents = work.getEventsOn(LocalDate.of(2025, 5, 5));
    List<Event> copiedDateEvents = work.getEventsOn(LocalDate.of(2025, 5, 10));

    assertEquals(1, originalDateEvents.size());
    assertEquals(1, copiedDateEvents.size());

    Event originalEvent = originalDateEvents.get(0);
    Event copiedEvent = copiedDateEvents.get(0);

    assertNotSame(originalEvent, copiedEvent);

    assertEquals(originalEvent.getSubject(), copiedEvent.getSubject());
    assertEquals(originalEvent.getDescription(), copiedEvent.getDescription());

    assertNotEquals(originalEvent.getStartDateTime(), copiedEvent.getStartDateTime());
  }

  @Test
  public void testRemoveEventFromSeriesWithNullSeriesId() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 15, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 15, 11, 0))
        .build();
    manager.addEvent(event);

    manager.removeEventFromSeries(event);

    List<Event> events = manager.getEventsOn(LocalDate.of(2025, 5, 15));
    assertEquals(0, events.size());
  }

  @Test
  public void testRemoveEventFromSeriesWithEmptySeriesId() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 15, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 15, 11, 0))
        .seriesId("")
        .build();
    manager.addEvent(event);

    manager.removeEventFromSeries(event);

    List<Event> events = manager.getEventsOn(LocalDate.of(2025, 5, 15));
    assertEquals(0, events.size());
  }

  @Test
  public void testRemoveEventFromSeriesWithValidSeriesIdRemovesFromEventOnwards() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    Set<DayOfWeek> weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.MONDAY);

    RecurringEventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Standup")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 9, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .weekdays(weekdays)
        .repeatCount(5)
        .build();
    manager.addEventSeries(series);

    List<Event> allSeriesEvents = manager.getEventsInRange(
        LocalDateTime.of(2025, 5, 1, 0, 0),
        LocalDateTime.of(2025, 6, 30, 23, 59));
    Event thirdEvent = allSeriesEvents.get(2);

    manager.removeEventFromSeries(thirdEvent);

    List<Event> remainingEvents = manager.getEventsInRange(
        LocalDateTime.of(2025, 5, 1, 0, 0),
        LocalDateTime.of(2025, 6, 30, 23, 59));
    assertEquals(2, remainingEvents.size());
  }

  @Test
  public void testRemoveAllEventsInSeriesWithNullSeriesId() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 15, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 15, 11, 0))
        .build();
    manager.addEvent(event);

    manager.removeAllEventsInSeries(event);

    List<Event> events = manager.getEventsOn(LocalDate.of(2025, 5, 15));
    assertEquals(0, events.size());
  }

  @Test
  public void testRemoveAllEventsInSeriesWithEmptySeriesId() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 15, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 15, 11, 0))
        .seriesId("")
        .build();
    manager.addEvent(event);

    manager.removeAllEventsInSeries(event);

    List<Event> events = manager.getEventsOn(LocalDate.of(2025, 5, 15));
    assertEquals(0, events.size());
  }

  @Test
  public void testRemoveAllEventsInSeriesWithValidSeriesIdRemovesAllInSeries() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    Set<DayOfWeek> weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.TUESDAY);

    RecurringEventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Review")
        .startDateTime(LocalDateTime.of(2025, 5, 6, 14, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 6, 15, 0))
        .weekdays(weekdays)
        .repeatCount(4)
        .build();
    manager.addEventSeries(series);

    List<Event> allSeriesEvents = manager.getEventsOn(LocalDate.of(2025, 5, 6));
    Event anyEvent = allSeriesEvents.get(0);

    manager.removeAllEventsInSeries(anyEvent);

    List<Event> remainingEvents = manager.getEventsInRange(
        LocalDateTime.of(2025, 5, 1, 0, 0),
        LocalDateTime.of(2025, 6, 30, 23, 59));
    assertEquals(0, remainingEvents.size());
  }
}