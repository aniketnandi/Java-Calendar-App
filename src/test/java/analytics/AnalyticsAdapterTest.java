package analytics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import calendar.model.CalendarAnalyticsSummary;
import calendar.model.CalendarEvent;
import calendar.model.CalendarModelAdapter;
import calendar.model.Event;
import calendar.model.RecurringEventSeries;
import calendar.model.SimpleCalendarManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for CalendarModelAdapter focusing on analytics and series event removal methods.
 */
public class AnalyticsAdapterTest {
  private SimpleCalendarManager manager;
  private CalendarModelAdapter adapter;

  /**
   * Sets up the test fixture before each test.
   */
  @Before
  public void setUp() {
    manager = new SimpleCalendarManager();
    adapter = new CalendarModelAdapter(manager);
  }

  /**
   * Tests generateAnalytics with valid date range and events.
   */
  @Test
  public void testGenerateAnalyticsWithValidRange() {
    manager.createCalendar("TestCal", ZoneId.of("America/New_York"));
    manager.useCalendar("TestCal");

    CalendarEvent event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 15, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 15, 11, 0))
        .build();
    adapter.addEvent(event);

    LocalDate startDate = LocalDate.of(2024, 1, 1);
    LocalDate endDate = LocalDate.of(2024, 1, 31);

    CalendarAnalyticsSummary result = adapter.generateAnalytics(startDate, endDate);

    assertNotNull("Should return analytics summary", result);
  }

  /**
   * Tests generateAnalytics throws exception when no calendar is active.
   */
  @Test
  public void testGenerateAnalyticsNoActiveCalendar() {
    LocalDate startDate = LocalDate.of(2024, 1, 1);
    LocalDate endDate = LocalDate.of(2024, 1, 31);

    try {
      adapter.generateAnalytics(startDate, endDate);
      fail("Should throw IllegalStateException when no calendar is active");
    } catch (IllegalStateException e) {
      assertNotNull("Exception message should not be null", e.getMessage());
    }
  }

  /**
   * Tests removeEventFromSeries with valid series event.
   */
  @Test
  public void testRemoveEventFromSeriesValid() {
    manager.createCalendar("TestCal", ZoneId.of("America/New_York"));
    manager.useCalendar("TestCal");

    RecurringEventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Daily Standup")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 9, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 9, 30))
        .weekdays(java.util.EnumSet.of(java.time.DayOfWeek.MONDAY))
        .repeatCount(5)
        .build();

    adapter.addEventSeries(series);
    Event event = adapter.getEventsOn(LocalDate.of(2024, 1, 1)).get(0);

    adapter.removeEventFromSeries(event);

    assertEquals("Event should be removed from series", 0,
        adapter.getEventsOn(LocalDate.of(2024, 1, 1)).size());
  }

  /**
   * Tests removeEventFromSeries when no calendar is active.
   */
  @Test
  public void testRemoveEventFromSeriesNoActiveCalendar() {
    CalendarEvent event = new CalendarEvent.EventBuilder()
        .subject("Test")
        .startDateTime(LocalDateTime.of(2024, 1, 15, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 15, 11, 0))
        .build();

    try {
      adapter.removeEventFromSeries(event);
      fail("Should throw IllegalStateException when no calendar is active");
    } catch (IllegalStateException e) {
      assertNotNull("Exception message should not be null", e.getMessage());
    }
  }

  /**
   * Tests removeEventFromSeries with null event.
   */
  @Test(expected = NullPointerException.class)
  public void testRemoveEventFromSeriesNull() {
    manager.createCalendar("TestCal", ZoneId.of("America/New_York"));
    manager.useCalendar("TestCal");

    adapter.removeEventFromSeries(null);
  }

  /**
   * Tests removeEventFromSeries with standalone event not in series.
   */
  @Test
  public void testRemoveEventFromSeriesStandaloneEvent() {
    manager.createCalendar("TestCal", ZoneId.of("America/New_York"));
    manager.useCalendar("TestCal");

    CalendarEvent event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 15, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 15, 11, 0))
        .build();

    adapter.addEvent(event);
    adapter.removeEventFromSeries(event);
  }

  /**
   * Tests removeAllEventsInSeries with valid series event.
   */
  @Test
  public void testRemoveAllEventsInSeriesValid() {
    manager.createCalendar("TestCal", ZoneId.of("America/New_York"));
    manager.useCalendar("TestCal");

    RecurringEventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Weekly Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 14, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 15, 0))
        .weekdays(java.util.EnumSet.of(java.time.DayOfWeek.MONDAY))
        .repeatCount(3)
        .build();

    adapter.addEventSeries(series);
    Event event = adapter.getEventsOn(LocalDate.of(2024, 1, 1)).get(0);

    adapter.removeAllEventsInSeries(event);

    assertEquals("All events in series should be removed", 0,
        adapter.getEventsOn(LocalDate.of(2024, 1, 1)).size());
  }

  /**
   * Tests removeAllEventsInSeries when no calendar is active.
   */
  @Test
  public void testRemoveAllEventsInSeriesNoActiveCalendar() {
    CalendarEvent event = new CalendarEvent.EventBuilder()
        .subject("Test")
        .startDateTime(LocalDateTime.of(2024, 1, 15, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 15, 11, 0))
        .build();

    try {
      adapter.removeAllEventsInSeries(event);
      fail("Should throw IllegalStateException when no calendar is active");
    } catch (IllegalStateException e) {
      assertNotNull("Exception message should not be null", e.getMessage());
    }
  }

  /**
   * Tests removeAllEventsInSeries with null event.
   */
  @Test(expected = NullPointerException.class)
  public void testRemoveAllEventsInSeriesNull() {
    manager.createCalendar("TestCal", ZoneId.of("America/New_York"));
    manager.useCalendar("TestCal");

    adapter.removeAllEventsInSeries(null);
  }

  /**
   * Tests removeAllEventsInSeries with standalone event.
   */
  @Test
  public void testRemoveAllEventsInSeriesStandaloneEvent() {
    manager.createCalendar("TestCal", ZoneId.of("America/New_York"));
    manager.useCalendar("TestCal");

    CalendarEvent event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 15, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 15, 11, 0))
        .build();

    adapter.addEvent(event);
    adapter.removeAllEventsInSeries(event);
  }
}