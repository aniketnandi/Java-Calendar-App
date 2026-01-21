package model;

import static org.junit.Assert.assertEquals;

import calendar.model.CalendarEvent;
import calendar.model.RecurringEventSeries;
import calendar.model.Status;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

/**
 * Test class for Status enum and AbstractEventBuilder.
 */
public class StatusAndBuilderTest {

  @Test
  public void testStatusValues() {
    Status[] values = Status.values();
    assertEquals(2, values.length);
    assertEquals(Status.PUBLIC, values[0]);
    assertEquals(Status.PRIVATE, values[1]);
  }

  @Test
  public void testStatusValueOf() {
    assertEquals(Status.PUBLIC, Status.valueOf("PUBLIC"));
    assertEquals(Status.PRIVATE, Status.valueOf("PRIVATE"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testStatusValueOfInvalid() {
    Status.valueOf("INVALID");
  }

  @Test
  public void testBuilderSubject() {
    CalendarEvent.EventBuilder builder = new CalendarEvent.EventBuilder();
    builder.subject("Test");

    CalendarEvent event = builder
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();

    assertEquals("Test", event.getSubject());
  }

  @Test
  public void testBuilderStartDateTime() {
    LocalDateTime start = LocalDateTime.of(2025, 5, 5, 10, 0);
    CalendarEvent.EventBuilder builder = new CalendarEvent.EventBuilder();
    builder.startDateTime(start);

    CalendarEvent event = builder
        .subject("Test")
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();

    assertEquals(start, event.getStartDateTime());
  }

  @Test
  public void testBuilderEndDateTime() {
    LocalDateTime end = LocalDateTime.of(2025, 5, 5, 11, 0);
    CalendarEvent.EventBuilder builder = new CalendarEvent.EventBuilder();
    builder.endDateTime(end);

    CalendarEvent event = builder
        .subject("Test")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .build();

    assertEquals(end, event.getEndDateTime());
  }

  @Test
  public void testBuilderDescription() {
    CalendarEvent.EventBuilder builder = new CalendarEvent.EventBuilder();
    builder.description("Test description");

    CalendarEvent event = builder
        .subject("Test")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();

    assertEquals("Test description", event.getDescription());
  }

  @Test
  public void testBuilderLocation() {
    CalendarEvent.EventBuilder builder = new CalendarEvent.EventBuilder();
    builder.location("Room A");

    CalendarEvent event = builder
        .subject("Test")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();

    assertEquals("Room A", event.getLocation());
  }

  @Test
  public void testBuilderStatus() {
    CalendarEvent.EventBuilder builder = new CalendarEvent.EventBuilder();
    builder.status(Status.PRIVATE);

    CalendarEvent event = builder
        .subject("Test")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();

    assertEquals(Status.PRIVATE, event.getStatus());
  }

  @Test
  public void testBuilderDefaultDescription() {
    CalendarEvent event = new CalendarEvent.EventBuilder()
        .subject("Test")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();

    assertEquals("", event.getDescription());
  }

  @Test
  public void testBuilderDefaultLocation() {
    CalendarEvent event = new CalendarEvent.EventBuilder()
        .subject("Test")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();

    assertEquals("", event.getLocation());
  }

  @Test
  public void testBuilderDefaultStatus() {
    CalendarEvent event = new CalendarEvent.EventBuilder()
        .subject("Test")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();

    assertEquals(Status.PUBLIC, event.getStatus());
  }

  @Test
  public void testBuilderApplyAllDayCheck() {
    CalendarEvent event = new CalendarEvent.EventBuilder()
        .subject("Test")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 14, 30))
        .build();

    assertEquals(LocalDateTime.of(2025, 5, 5, 8, 0), event.getStartDateTime());
    assertEquals(LocalDateTime.of(2025, 5, 5, 17, 0), event.getEndDateTime());
  }

  @Test
  public void testBuilderApplyAllDayCheckNotAppliedWhenEndPresent() {
    CalendarEvent event = new CalendarEvent.EventBuilder()
        .subject("Test")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();

    assertEquals(LocalDateTime.of(2025, 5, 5, 10, 0), event.getStartDateTime());
    assertEquals(LocalDateTime.of(2025, 5, 5, 11, 0), event.getEndDateTime());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBuilderValidateNullSubject() {
    new CalendarEvent.EventBuilder()
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBuilderValidateEmptySubject() {
    new CalendarEvent.EventBuilder()
        .subject("")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBuilderValidateNullStartDateTime() {
    new CalendarEvent.EventBuilder()
        .subject("Test")
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBuilderValidateEndBeforeStart() {
    new CalendarEvent.EventBuilder()
        .subject("Test")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBuilderValidateEndEqualStart() {
    new CalendarEvent.EventBuilder()
        .subject("Test")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .build();
  }

  @Test
  public void testRecurringBuilderInheritance() {
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);

    RecurringEventSeries series = new RecurringEventSeries.EventSeriesBuilder()
        .subject("Test")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .description("Desc")
        .location("Loc")
        .status(Status.PRIVATE)
        .weekdays(days)
        .repeatCount(3)
        .build();

    assertEquals("Test", series.getSubject());
    assertEquals("Desc", series.getDescription());
    assertEquals("Loc", series.getLocation());
    assertEquals(Status.PRIVATE, series.getStatus());
  }
}