package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import calendar.model.CalendarEvent;
import calendar.model.CalendarModelAdapter;
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
 * Test class for CalendarModelAdapter.
 */
public class CalendarModelAdapterTest {
  private SimpleCalendarManager manager;
  private CalendarModelAdapter adapter;

  /**
   * Tests the adapter pattern implementation that bridges CalendarManager
   * with code expecting CalendarModel interface, including error handling
   * when no calendar is currently in use.
   */
  @Before
  public void setUp() {
    manager = new SimpleCalendarManager();
    adapter = new CalendarModelAdapter(manager);
  }

  @Test(expected = IllegalStateException.class)
  public void testAddEventNoCalendarInUse() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    adapter.addEvent(event);
  }

  @Test
  public void testAddEventWithCalendarInUse() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    adapter.addEvent(event);

    List<Event> events = adapter.getEventsOn(LocalDate.of(2025, 5, 5));
    assertEquals(1, events.size());
  }

  @Test(expected = IllegalStateException.class)
  public void testRemoveEventNoCalendarInUse() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    adapter.removeEvent(event);
  }

  @Test(expected = IllegalStateException.class)
  public void testEditEventNoCalendarInUse() {
    adapter.editEvent("Meeting",
        LocalDateTime.of(2025, 5, 5, 10, 0),
        LocalDateTime.of(2025, 5, 5, 11, 0),
        "subject", "New Meeting");
  }

  @Test(expected = IllegalStateException.class)
  public void testGetEventsOnNoCalendarInUse() {
    adapter.getEventsOn(LocalDate.of(2025, 5, 5));
  }

  @Test(expected = IllegalStateException.class)
  public void testGetEventsInRangeNoCalendarInUse() {
    adapter.getEventsInRange(
        LocalDateTime.of(2025, 5, 5, 0, 0),
        LocalDateTime.of(2025, 5, 6, 0, 0));
  }

  @Test(expected = IllegalStateException.class)
  public void testIsBusyNoCalendarInUse() {
    adapter.isBusy(LocalDateTime.of(2025, 5, 5, 10, 0));
  }

  @Test(expected = IllegalStateException.class)
  public void testExportToCsvNoCalendarInUse() throws Exception {
    adapter.exportToCsv("test.csv");
  }

  @Test
  public void testEditEventWithAdapter() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    adapter.addEvent(event);

    adapter.editEvent("Meeting",
        LocalDateTime.of(2025, 5, 5, 10, 0),
        LocalDateTime.of(2025, 5, 5, 11, 0),
        "subject", "Updated Meeting");

    List<Event> events = adapter.getEventsOn(LocalDate.of(2025, 5, 5));
    assertEquals("Updated Meeting", events.get(0).getSubject());
  }

  @Test
  public void testIsBusyWithAdapter() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    adapter.addEvent(event);

    assertTrue(adapter.isBusy(LocalDateTime.of(2025, 5, 5, 10, 30)));
    assertFalse(adapter.isBusy(LocalDateTime.of(2025, 5, 5, 12, 0)));
  }

  @Test
  public void testSwitchCalendarsWithAdapter() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.createCalendar("Personal", ZoneId.of("America/Chicago"));

    manager.useCalendar("Work");
    Event workEvent = new CalendarEvent.EventBuilder()
        .subject("Work Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    adapter.addEvent(workEvent);

    manager.useCalendar("Personal");
    Event personalEvent = new CalendarEvent.EventBuilder()
        .subject("Personal Appointment")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 14, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 15, 0))
        .build();
    adapter.addEvent(personalEvent);

    List<Event> personalEvents = adapter.getEventsOn(LocalDate.of(2025, 5, 5));
    assertEquals(1, personalEvents.size());
    assertEquals("Personal Appointment", personalEvents.get(0).getSubject());

    manager.useCalendar("Work");
    List<Event> workEvents = adapter.getEventsOn(LocalDate.of(2025, 5, 5));
    assertEquals(1, workEvents.size());
    assertEquals("Work Meeting", workEvents.get(0).getSubject());
  }

  @Test
  public void testExportWithAdapter() throws Exception {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    adapter.addEvent(event);

    String path = adapter.exportToCsv("adapter_test.csv");
    assertTrue(path.contains("adapter_test.csv"));
  }

  @Test(expected = IllegalStateException.class)
  public void testAddEventSeriesNoCalendarInUse() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);

    EventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .weekdays(days)
        .repeatCount(3)
        .build();

    adapter.addEventSeries(series);
  }

  @Test
  public void testAddEventSeriesWithCalendarInUse() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);

    EventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .weekdays(days)
        .repeatCount(3)
        .build();

    adapter.addEventSeries(series);

    List<Event> events = adapter.getEventsInRange(
        LocalDateTime.of(2025, 5, 1, 0, 0),
        LocalDateTime.of(2025, 5, 31, 23, 59));
    assertEquals(3, events.size());
  }

  @Test
  public void testRemoveEventWithCalendarInUse() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();

    adapter.addEvent(event);
    List<Event> eventsBefore = adapter.getEventsOn(LocalDate.of(2025, 5, 5));
    assertEquals(1, eventsBefore.size());

    adapter.removeEvent(event);
    List<Event> eventsAfter = adapter.getEventsOn(LocalDate.of(2025, 5, 5));
    assertEquals(0, eventsAfter.size());
  }

  @Test(expected = IllegalStateException.class)
  public void testEditEventsFromNoCalendarInUse() {
    adapter.editEventsFrom("Meeting",
        LocalDateTime.of(2025, 5, 5, 10, 0), null,
        "subject", "New Meeting");
  }

  @Test
  public void testEditEventsFromWithAdapter() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);

    EventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .weekdays(days)
        .repeatCount(3)
        .build();

    adapter.addEventSeries(series);

    adapter.editEventsFrom("Meeting",
        LocalDateTime.of(2025, 5, 12, 10, 0), null,
        "subject", "Updated Meeting");

    List<Event> events = adapter.getEventsInRange(
        LocalDateTime.of(2025, 5, 1, 0, 0),
        LocalDateTime.of(2025, 5, 31, 23, 59));

    int updatedCount = 0;
    int originalCount = 0;
    for (Event e : events) {
      if (e.getSubject().equals("Updated Meeting")) {
        updatedCount++;
      } else if (e.getSubject().equals("Meeting")) {
        originalCount++;
      }
    }

    assertTrue(updatedCount > 0);
    assertTrue(originalCount > 0);
  }

  @Test(expected = IllegalStateException.class)
  public void testEditAllEventsInSeriesNoCalendarInUse() {
    adapter.editAllEventsInSeries("Meeting",
        LocalDateTime.of(2025, 5, 5, 10, 0), null,
        "subject", "New Meeting");
  }

  @Test
  public void testEditAllEventsInSeriesWithAdapter() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);

    EventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .weekdays(days)
        .repeatCount(3)
        .build();

    adapter.addEventSeries(series);

    adapter.editAllEventsInSeries("Meeting",
        LocalDateTime.of(2025, 5, 5, 10, 0), null,
        "subject", "Updated Meeting");

    List<Event> events = adapter.getEventsInRange(
        LocalDateTime.of(2025, 5, 1, 0, 0),
        LocalDateTime.of(2025, 5, 31, 23, 59));

    assertEquals(3, events.size());
    for (Event e : events) {
      assertEquals("Updated Meeting", e.getSubject());
    }
  }

  @Test(expected = IllegalStateException.class)
  public void testExportToIcalNoCalendarInUse() throws Exception {
    adapter.exportToIcal("test.ical", ZoneId.of("America/New_York"));
  }

  @Test
  public void testExportToIcalWithAdapter() throws Exception {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    adapter.addEvent(event);

    String path = adapter.exportToIcal("adapter_test.ical", ZoneId.of("America/New_York"));
    assertTrue(path.contains("adapter_test.ical"));
  }
}