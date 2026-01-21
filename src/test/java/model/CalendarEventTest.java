package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import calendar.model.CalendarEvent;
import calendar.model.Status;
import java.time.LocalDateTime;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test class for CalendarEvent.
 */
public class CalendarEventTest {

  @Test
  public void testBasicEventCreation() {
    CalendarEvent event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();

    assertEquals("Meeting", event.getSubject());
    assertEquals(LocalDateTime.of(2025, 5, 5, 10, 0), event.getStartDateTime());
    assertEquals(LocalDateTime.of(2025, 5, 5, 11, 0), event.getEndDateTime());
    assertEquals("", event.getDescription());
    assertEquals("", event.getLocation());
    Assert.assertEquals(Status.PUBLIC, event.getStatus());
    assertNull(event.getSeriesId());
  }

  @Test
  public void testEventWithAllProperties() {
    CalendarEvent event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .description("Important meeting")
        .location("Conference Room A")
        .status(Status.PRIVATE)
        .seriesId("series-123")
        .build();

    assertEquals("Meeting", event.getSubject());
    assertEquals("Important meeting", event.getDescription());
    assertEquals("Conference Room A", event.getLocation());
    assertEquals(Status.PRIVATE, event.getStatus());
    assertEquals("series-123", event.getSeriesId());
  }

  @Test
  public void testAllDayEventCreation() {
    CalendarEvent event = new CalendarEvent.EventBuilder()
        .subject("All Day Event")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 0, 0))
        .build();

    assertTrue(event.isAllDay());
    assertEquals(LocalDateTime.of(2025, 5, 5, 8, 0), event.getStartDateTime());
    assertEquals(LocalDateTime.of(2025, 5, 5, 17, 0), event.getEndDateTime());
  }

  @Test
  public void testIsAllDayTrue() {
    CalendarEvent event = new CalendarEvent.EventBuilder()
        .subject("Event")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 8, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 17, 0))
        .build();

    assertTrue(event.isAllDay());
  }

  @Test
  public void testIsAllDayFalseDifferentTimes() {
    CalendarEvent event = new CalendarEvent.EventBuilder()
        .subject("Event")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();

    assertFalse(event.isAllDay());
  }

  @Test
  public void testIsAllDayFalseDifferentDates() {
    CalendarEvent event = new CalendarEvent.EventBuilder()
        .subject("Event")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 8, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 6, 17, 0))
        .build();

    assertFalse(event.isAllDay());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullSubject() {
    new CalendarEvent.EventBuilder()
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptySubject() {
    new CalendarEvent.EventBuilder()
        .subject("")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullStartDateTime() {
    new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEndBeforeStart() {
    new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEndEqualsStart() {
    new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .build();
  }

  @Test
  public void testEquals() {
    CalendarEvent event1 = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();

    CalendarEvent event2 = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();

    assertEquals(event1, event2);
  }

  @Test
  public void testEqualsSameObject() {
    CalendarEvent event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();

    assertEquals(event, event);
  }

  @Test
  public void testNotEqualsDifferentSubject() {
    CalendarEvent event1 = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();

    CalendarEvent event2 = new CalendarEvent.EventBuilder()
        .subject("Different")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();

    assertNotEquals(event1, event2);
  }

  @Test
  public void testNotEqualsDifferentStart() {
    CalendarEvent event1 = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();

    CalendarEvent event2 = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 12, 0))
        .build();

    assertNotEquals(event1, event2);
  }

  @Test
  public void testNotEqualsDifferentEnd() {
    CalendarEvent event1 = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();

    CalendarEvent event2 = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 12, 0))
        .build();

    assertNotEquals(event1, event2);
  }

  @Test
  public void testNotEqualsNull() {
    CalendarEvent event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();

    assertNotEquals(event, null);
  }

  @Test
  public void testNotEqualsDifferentClass() {
    CalendarEvent event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();

    assertNotEquals(event, "Not an event");
  }

  @Test
  public void testHashCode() {
    CalendarEvent event1 = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();

    CalendarEvent event2 = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();

    assertEquals(event1.hashCode(), event2.hashCode());
  }

  @Test
  public void testToString() {
    CalendarEvent event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();

    String str = event.toString();
    assertTrue(str.contains("Meeting"));
    assertTrue(str.contains("2025-05-05T10:00"));
    assertTrue(str.contains("2025-05-05T11:00"));
  }

  @Test
  public void testBuilderChaining() {
    CalendarEvent event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .description("Test")
        .location("Room A")
        .status(Status.PRIVATE)
        .seriesId("123")
        .build();

    assertEquals("Meeting", event.getSubject());
    assertEquals("Test", event.getDescription());
    assertEquals("Room A", event.getLocation());
    assertEquals(Status.PRIVATE, event.getStatus());
    assertEquals("123", event.getSeriesId());
  }

  @Test
  public void testIsAllDayFalseStartMinuteNotZero() {
    CalendarEvent event = new CalendarEvent.EventBuilder()
        .subject("Event")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 8, 15))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 17, 0))
        .build();

    assertFalse(event.isAllDay());
  }

  @Test
  public void testIsAllDayFalseEndHourNot17() {
    CalendarEvent event = new CalendarEvent.EventBuilder()
        .subject("Event")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 8, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 16, 0))
        .build();

    assertFalse(event.isAllDay());
  }

  @Test
  public void testIsAllDayFalseEndMinuteNotZero() {
    CalendarEvent event = new CalendarEvent.EventBuilder()
        .subject("Event")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 8, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 17, 30))
        .build();

    assertFalse(event.isAllDay());
  }
}