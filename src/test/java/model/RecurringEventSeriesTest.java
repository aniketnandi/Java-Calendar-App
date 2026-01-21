package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import calendar.model.Event;
import calendar.model.RecurringEventSeries;
import calendar.model.Status;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;

/**
 * Test class for RecurringEventSeries.
 */
public class RecurringEventSeriesTest {

  @Test
  public void testSeriesCreationWithCount() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);
    days.add(DayOfWeek.WEDNESDAY);

    RecurringEventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .weekdays(days)
        .repeatCount(4)
        .build();

    assertEquals("Meeting", series.getSubject());
    assertEquals(Integer.valueOf(4), series.getRepeatCount());
    assertNull(series.getRepeatUntil());
    assertNotNull(series.getSeriesId());
    assertEquals(4, series.getAllEvents().size());
  }

  @Test
  public void testSeriesCreationWithRepeatUntil() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);
    days.add(DayOfWeek.WEDNESDAY);
    days.add(DayOfWeek.FRIDAY);

    RecurringEventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .weekdays(days)
        .repeatUntil(LocalDate.of(2025, 5, 16))
        .build();

    assertEquals("Meeting", series.getSubject());
    assertNull(series.getRepeatCount());
    assertEquals(LocalDate.of(2025, 5, 16), series.getRepeatUntil());
    assertNotNull(series.getSeriesId());
    assertTrue(series.getAllEvents().size() > 0);
  }

  @Test
  public void testSeriesWithAllProperties() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);

    RecurringEventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 6, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 6, 11, 0))
        .description("Important")
        .location("Room A")
        .status(Status.PRIVATE)
        .weekdays(days)
        .repeatCount(2)
        .build();

    assertEquals("Meeting", series.getSubject());
    assertEquals("Important", series.getDescription());
    assertEquals("Room A", series.getLocation());
    assertEquals(Status.PRIVATE, series.getStatus());
  }

  @Test
  public void testAllDaySeriesGeneration() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);

    RecurringEventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("All Day")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 0, 0))
        .weekdays(days)
        .repeatCount(3)
        .build();

    List<Event> events = series.getAllEvents();
    assertEquals(3, events.size());
    assertTrue(events.get(0).isAllDay());
  }

  @Test
  public void testEventGenerationByCount() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);
    days.add(DayOfWeek.WEDNESDAY);
    days.add(DayOfWeek.FRIDAY);

    RecurringEventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Test")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .weekdays(days)
        .repeatCount(6)
        .build();

    List<Event> events = series.getAllEvents();
    assertEquals(6, events.size());

    for (Event event : events) {
      assertEquals("Test", event.getSubject());
      assertEquals(series.getSeriesId(), event.getSeriesId());
    }
  }

  @Test
  public void testEventGenerationByRepeatUntil() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);

    RecurringEventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Test")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .weekdays(days)
        .repeatUntil(LocalDate.of(2025, 5, 19))
        .build();

    List<Event> events = series.getAllEvents();
    assertEquals(3, events.size());

    Event lastEvent = events.get(events.size() - 1);
    assertFalse(lastEvent.getStartDateTime().toLocalDate()
        .isAfter(LocalDate.of(2025, 5, 19)));
  }

  @Test
  public void testGetEventsStartingFrom() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);
    days.add(DayOfWeek.WEDNESDAY);

    RecurringEventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Test")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .weekdays(days)
        .repeatCount(6)
        .build();

    List<Event> allEvents = series.getAllEvents();
    Event secondEvent = allEvents.get(1);

    List<Event> eventsFrom = series.getEventsStartingFrom(secondEvent);
    assertEquals(5, eventsFrom.size());
  }

  @Test
  public void testGetEventsStartingFromFirst() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);

    RecurringEventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Test")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .weekdays(days)
        .repeatCount(3)
        .build();

    List<Event> allEvents = series.getAllEvents();
    List<Event> eventsFrom = series.getEventsStartingFrom(allEvents.get(0));

    assertEquals(3, eventsFrom.size());
  }

  @Test
  public void testGetEventsStartingFromLast() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);

    RecurringEventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Test")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .weekdays(days)
        .repeatCount(3)
        .build();

    List<Event> allEvents = series.getAllEvents();
    List<Event> eventsFrom = series.getEventsStartingFrom(allEvents.get(2));

    assertEquals(1, eventsFrom.size());
  }

  @Test
  public void testWeekdaysGetter() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);
    days.add(DayOfWeek.FRIDAY);

    RecurringEventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Test")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .weekdays(days)
        .repeatCount(2)
        .build();

    List<DayOfWeek> resultDays = series.getWeekdays();
    assertEquals(2, resultDays.size());
    assertTrue(resultDays.contains(DayOfWeek.MONDAY));
    assertTrue(resultDays.contains(DayOfWeek.FRIDAY));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullSubject() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);

    new RecurringEventSeries.EventSeriesBuilder()
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .weekdays(days)
        .repeatCount(3)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullStartDateTime() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);

    new RecurringEventSeries.EventSeriesBuilder()
        .subject("Test")
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .weekdays(days)
        .repeatCount(3)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullWeekdays() {
    new RecurringEventSeries.EventSeriesBuilder()
        .subject("Test")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .repeatCount(3)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyWeekdays() {
    Set<DayOfWeek> days = new HashSet<>();

    new RecurringEventSeries.EventSeriesBuilder()
        .subject("Test")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .weekdays(days)
        .repeatCount(3)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNoRepeatCountOrUntil() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);

    new RecurringEventSeries.EventSeriesBuilder()
        .subject("Test")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .weekdays(days)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testZeroRepeatCount() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);

    new RecurringEventSeries.EventSeriesBuilder()
        .subject("Test")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .weekdays(days)
        .repeatCount(0)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeRepeatCount() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);

    new RecurringEventSeries.EventSeriesBuilder()
        .subject("Test")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .weekdays(days)
        .repeatCount(-1)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullRepeatCountExplicit() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);

    new RecurringEventSeries.EventSeriesBuilder()
        .subject("Test")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .weekdays(days)
        .repeatCount(null)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRepeatUntilBeforeStart() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);

    new RecurringEventSeries.EventSeriesBuilder()
        .subject("Test")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .weekdays(days)
        .repeatUntil(LocalDate.of(2025, 5, 4))
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRepeatUntilSameAsStart() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);

    new RecurringEventSeries.EventSeriesBuilder()
        .subject("Test")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .weekdays(days)
        .repeatUntil(LocalDate.of(2025, 5, 5))
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testStartAndEndOnDifferentDates() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);

    new RecurringEventSeries.EventSeriesBuilder()
        .subject("Test")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 6, 11, 0))
        .weekdays(days)
        .repeatCount(3)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEndBeforeStart() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);

    new RecurringEventSeries.EventSeriesBuilder()
        .subject("Test")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .weekdays(days)
        .repeatCount(3)
        .build();
  }

  @Test
  public void testMultipleWeekdays() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);
    days.add(DayOfWeek.TUESDAY);
    days.add(DayOfWeek.WEDNESDAY);
    days.add(DayOfWeek.THURSDAY);
    days.add(DayOfWeek.FRIDAY);

    RecurringEventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Test")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .weekdays(days)
        .repeatCount(10)
        .build();

    assertEquals(10, series.getAllEvents().size());
  }

  @Test
  public void testSeriesGenerationStartsCorrectly() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.WEDNESDAY);

    RecurringEventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Test")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .weekdays(days)
        .repeatCount(3)
        .build();

    List<Event> events = series.getAllEvents();
    assertEquals(LocalDate.of(2025, 5, 7),
        events.get(0).getStartDateTime().toLocalDate());
  }

  @Test
  public void testEventGenerationBreaksAtRepeatCount() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);
    days.add(DayOfWeek.TUESDAY);
    days.add(DayOfWeek.WEDNESDAY);
    days.add(DayOfWeek.THURSDAY);
    days.add(DayOfWeek.FRIDAY);

    RecurringEventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Test")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .weekdays(days)
        .repeatCount(3)
        .build();

    List<Event> events = series.getAllEvents();
    assertEquals(3, events.size());
  }

  @Test
  public void testEventGenerationSkipsEarlierDaysInWeek() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);
    days.add(DayOfWeek.WEDNESDAY);
    days.add(DayOfWeek.FRIDAY);

    RecurringEventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Test")
        .startDateTime(LocalDateTime.of(2025, 5, 7, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 7, 11, 0))
        .weekdays(days)
        .repeatCount(5)
        .build();

    List<Event> events = series.getAllEvents();
    assertEquals(5, events.size());
    assertTrue(events.get(0).getStartDateTime().toLocalDate().equals(LocalDate.of(2025, 5, 7))
        || events.get(0).getStartDateTime().toLocalDate().equals(LocalDate.of(2025, 5, 9))
        || events.get(0).getStartDateTime().toLocalDate().equals(LocalDate.of(2025, 5, 12)));
  }

  @Test
  public void testRepeatUntilSkipsEarlierDaysInWeek() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);
    days.add(DayOfWeek.WEDNESDAY);
    days.add(DayOfWeek.FRIDAY);

    RecurringEventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Test")
        .startDateTime(LocalDateTime.of(2025, 5, 9, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 9, 11, 0))
        .weekdays(days)
        .repeatUntil(LocalDate.of(2025, 5, 23))
        .build();

    List<Event> events = series.getAllEvents();
    assertTrue(events.size() > 0);
    assertFalse(events.get(0).getStartDateTime().toLocalDate().isBefore(LocalDate.of(2025, 5, 9)));
  }

  @Test
  public void testRepeatUntilExcludesEventsAfterLimit() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);
    days.add(DayOfWeek.WEDNESDAY);
    days.add(DayOfWeek.FRIDAY);

    RecurringEventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Test")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .weekdays(days)
        .repeatUntil(LocalDate.of(2025, 5, 12))
        .build();

    List<Event> events = series.getAllEvents();
    for (Event e : events) {
      assertFalse(e.getStartDateTime().toLocalDate().isAfter(LocalDate.of(2025, 5, 12)));
    }
  }

  @Test
  public void testGenerateEventsByCountChronological() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);
    days.add(DayOfWeek.FRIDAY);

    RecurringEventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 11, 21, 9, 0))
        .endDateTime(LocalDateTime.of(2025, 11, 21, 10, 0))
        .weekdays(days)
        .repeatCount(5)
        .build();

    List<Event> events = series.getAllEvents();

    // Should be: Nov 21 (Fri), Nov 25 (Tue), Nov 28 (Fri), Dec 2 (Tue), Dec 5 (Fri)
    assertEquals(5, events.size());
    assertEquals(LocalDate.of(2025, 11, 21), events.get(0).getStartDateTime().toLocalDate());
    assertEquals(LocalDate.of(2025, 11, 25), events.get(1).getStartDateTime().toLocalDate());
    assertEquals(LocalDate.of(2025, 11, 28), events.get(2).getStartDateTime().toLocalDate());
    assertEquals(LocalDate.of(2025, 12, 2), events.get(3).getStartDateTime().toLocalDate());
    assertEquals(LocalDate.of(2025, 12, 5), events.get(4).getStartDateTime().toLocalDate());
  }

  @Test
  public void testGenerateEventsByCountStartingMonday() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);
    days.add(DayOfWeek.WEDNESDAY);
    days.add(DayOfWeek.FRIDAY);

    RecurringEventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Class")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 13, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 14, 0))
        .weekdays(days)
        .repeatCount(6)
        .build();

    List<Event> events = series.getAllEvents();

    // Should be: May 5 (Mon), May 7 (Wed), May 9 (Fri), May 12 (Mon), May 14 (Wed), May 16 (Fri)
    assertEquals(6, events.size());
    assertEquals(LocalDate.of(2025, 5, 5), events.get(0).getStartDateTime().toLocalDate());
    assertEquals(LocalDate.of(2025, 5, 7), events.get(1).getStartDateTime().toLocalDate());
    assertEquals(LocalDate.of(2025, 5, 9), events.get(2).getStartDateTime().toLocalDate());
    assertEquals(LocalDate.of(2025, 5, 12), events.get(3).getStartDateTime().toLocalDate());
    assertEquals(LocalDate.of(2025, 5, 14), events.get(4).getStartDateTime().toLocalDate());
    assertEquals(LocalDate.of(2025, 5, 16), events.get(5).getStartDateTime().toLocalDate());
  }

  @Test
  public void testGenerateEventsByUntilChronological() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);
    days.add(DayOfWeek.THURSDAY);

    RecurringEventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Workshop")
        .startDateTime(LocalDateTime.of(2025, 5, 6, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 6, 12, 0))
        .weekdays(days)
        .repeatUntil(LocalDate.of(2025, 5, 20))
        .build();

    List<Event> events = series.getAllEvents();

    // Verify chronological order
    for (int i = 1; i < events.size(); i++) {
      assertTrue(events.get(i).getStartDateTime()
          .isAfter(events.get(i - 1).getStartDateTime()));
    }

    // Should include: May 6, 8, 13, 15, 20
    assertEquals(5, events.size());
    assertEquals(LocalDate.of(2025, 5, 6), events.get(0).getStartDateTime().toLocalDate());
    assertEquals(LocalDate.of(2025, 5, 8), events.get(1).getStartDateTime().toLocalDate());
    assertEquals(LocalDate.of(2025, 5, 13), events.get(2).getStartDateTime().toLocalDate());
    assertEquals(LocalDate.of(2025, 5, 15), events.get(3).getStartDateTime().toLocalDate());
    assertEquals(LocalDate.of(2025, 5, 20), events.get(4).getStartDateTime().toLocalDate());
  }

  @Test
  public void testGenerateEventsSingleDayOfWeek() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.FRIDAY);

    RecurringEventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 2, 15, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 2, 16, 0))
        .weekdays(days)
        .repeatCount(4)
        .build();

    List<Event> events = series.getAllEvents();

    assertEquals(4, events.size());
    assertEquals(LocalDate.of(2025, 5, 2), events.get(0).getStartDateTime().toLocalDate());
    assertEquals(LocalDate.of(2025, 5, 9), events.get(1).getStartDateTime().toLocalDate());
    assertEquals(LocalDate.of(2025, 5, 16), events.get(2).getStartDateTime().toLocalDate());
    assertEquals(LocalDate.of(2025, 5, 23), events.get(3).getStartDateTime().toLocalDate());
  }

  @Test
  public void testGenerateEventsAllWeekdays() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);
    days.add(DayOfWeek.TUESDAY);
    days.add(DayOfWeek.WEDNESDAY);
    days.add(DayOfWeek.THURSDAY);
    days.add(DayOfWeek.FRIDAY);

    RecurringEventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Daily Standup")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 9, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 9, 15))
        .weekdays(days)
        .repeatCount(10)
        .build();

    List<Event> events = series.getAllEvents();

    assertEquals(10, events.size());

    // Verify chronological order
    for (int i = 1; i < events.size(); i++) {
      assertTrue(events.get(i).getStartDateTime()
          .isAfter(events.get(i - 1).getStartDateTime()));
    }

    // Should be May 5, 6, 7, 8, 9, 12, 13, 14, 15, 16
    assertEquals(LocalDate.of(2025, 5, 5), events.get(0).getStartDateTime().toLocalDate());
    assertEquals(LocalDate.of(2025, 5, 16), events.get(9).getStartDateTime().toLocalDate());
  }

  @Test
  public void testGenerateEventsStartingWeekend() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.SATURDAY);
    days.add(DayOfWeek.SUNDAY);

    RecurringEventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Weekend Event")
        .startDateTime(LocalDateTime.of(2025, 5, 3, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 3, 11, 0))
        .weekdays(days)
        .repeatCount(6)
        .build();

    List<Event> events = series.getAllEvents();

    assertEquals(6, events.size());
    // May 3 (Sat), 4 (Sun), 10 (Sat), 11 (Sun), 17 (Sat), 18 (Sun)
    assertEquals(LocalDate.of(2025, 5, 3), events.get(0).getStartDateTime().toLocalDate());
    assertEquals(LocalDate.of(2025, 5, 4), events.get(1).getStartDateTime().toLocalDate());
    assertEquals(LocalDate.of(2025, 5, 10), events.get(2).getStartDateTime().toLocalDate());
  }

  @Test
  public void testGenerateEventsByUntilNotIncludingFutureDate() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);

    RecurringEventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .weekdays(days)
        .repeatUntil(LocalDate.of(2025, 5, 18))
        .build();

    List<Event> events = series.getAllEvents();

    // Should include: May 5, 12 (but not 19)
    assertEquals(2, events.size());
    assertEquals(LocalDate.of(2025, 5, 5), events.get(0).getStartDateTime().toLocalDate());
    assertEquals(LocalDate.of(2025, 5, 12), events.get(1).getStartDateTime().toLocalDate());
  }

  @Test
  public void testGenerateEventsSpanningMonths() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.WEDNESDAY);
    days.add(DayOfWeek.FRIDAY);

    RecurringEventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Lecture")
        .startDateTime(LocalDateTime.of(2025, 5, 28, 13, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 28, 14, 30))
        .weekdays(days)
        .repeatCount(5)
        .build();

    List<Event> events = series.getAllEvents();

    assertEquals(5, events.size());
    // May 28 (Wed), 30 (Fri), June 4 (Wed), 6 (Fri), 11 (Wed)
    assertEquals(LocalDate.of(2025, 5, 28), events.get(0).getStartDateTime().toLocalDate());
    assertEquals(LocalDate.of(2025, 5, 30), events.get(1).getStartDateTime().toLocalDate());
    assertEquals(LocalDate.of(2025, 6, 4), events.get(2).getStartDateTime().toLocalDate());
    assertEquals(LocalDate.of(2025, 6, 6), events.get(3).getStartDateTime().toLocalDate());
    assertEquals(LocalDate.of(2025, 6, 11), events.get(4).getStartDateTime().toLocalDate());
  }

  @Test
  public void testGenerateEventsPreservesTime() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);
    days.add(DayOfWeek.THURSDAY);

    RecurringEventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 14, 30))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 16, 15))
        .weekdays(days)
        .repeatCount(4)
        .build();

    List<Event> events = series.getAllEvents();

    // Verify all events have same time
    for (Event event : events) {
      assertEquals(14, event.getStartDateTime().getHour());
      assertEquals(30, event.getStartDateTime().getMinute());
      assertEquals(16, event.getEndDateTime().getHour());
      assertEquals(15, event.getEndDateTime().getMinute());
    }
  }

  @Test
  public void testGenerateEventsNoDuplicates() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);
    days.add(DayOfWeek.FRIDAY);

    RecurringEventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Class")
        .startDateTime(LocalDateTime.of(2025, 5, 6, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 6, 11, 0))
        .weekdays(days)
        .repeatCount(10)
        .build();

    List<Event> events = series.getAllEvents();

    // Check no duplicate dates
    Set<LocalDate> uniqueDates = new HashSet<>();
    for (Event event : events) {
      LocalDate date = event.getStartDateTime().toLocalDate();
      assertFalse("Duplicate date found: " + date, uniqueDates.contains(date));
      uniqueDates.add(date);
    }

    assertEquals(10, uniqueDates.size());
  }

  @Test
  public void testGenerateEventsByUntilExactEndDate() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.WEDNESDAY);

    RecurringEventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 7, 9, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 7, 10, 0))
        .weekdays(days)
        .repeatUntil(LocalDate.of(2025, 5, 21))
        .build();

    List<Event> events = series.getAllEvents();

    // Should include: May 7, 14, 21
    assertEquals(3, events.size());
    assertEquals(LocalDate.of(2025, 5, 21), events.get(2).getStartDateTime().toLocalDate());
  }
}