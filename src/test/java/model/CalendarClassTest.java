package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import calendar.model.Calendar;
import calendar.model.CalendarEvent;
import calendar.model.Event;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for Calendar wrapper class.
 */
public class CalendarClassTest {
  private Calendar calendar;

  /**
   * Test class for the Calendar wrapper class.
   * Tests calendar properties (name, timezone), timezone conversions,
   * delegation to underlying model, and export functionality.
   */
  @Before
  public void setUp() {
    calendar = new Calendar("Work", ZoneId.of("America/New_York"));
  }

  @Test
  public void testConstructor() {
    assertEquals("Work", calendar.getName());
    assertEquals(ZoneId.of("America/New_York"), calendar.getTimezone());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorNullName() {
    new Calendar(null, ZoneId.of("America/New_York"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorEmptyName() {
    new Calendar("", ZoneId.of("America/New_York"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWhitespaceName() {
    new Calendar("   ", ZoneId.of("America/New_York"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorNullTimezone() {
    new Calendar("Work", null);
  }

  @Test
  public void testSetName() {
    calendar.setName("Office");
    assertEquals("Office", calendar.getName());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetNameNull() {
    calendar.setName(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetNameEmpty() {
    calendar.setName("");
  }

  @Test
  public void testSetTimezone() {
    calendar.setTimezone(ZoneId.of("America/Chicago"));
    assertEquals(ZoneId.of("America/Chicago"), calendar.getTimezone());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetTimezoneNull() {
    calendar.setTimezone(null);
  }

  @Test
  public void testConvertToTimezone() {
    LocalDateTime nyTime = LocalDateTime.of(2025, 5, 5, 14, 0);
    LocalDateTime laTime = calendar.convertToTimezone(nyTime,
        ZoneId.of("America/Los_Angeles"));

    assertEquals(LocalDateTime.of(2025, 5, 5, 11, 0), laTime);
  }

  @Test
  public void testConvertFromTimezone() {
    LocalDateTime laTime = LocalDateTime.of(2025, 5, 5, 11, 0);
    LocalDateTime nyTime = calendar.convertFromTimezone(laTime,
        ZoneId.of("America/Los_Angeles"));

    assertEquals(LocalDateTime.of(2025, 5, 5, 14, 0), nyTime);
  }

  @Test
  public void testAddEventDelegation() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();

    calendar.addEvent(event);
    List<Event> events = calendar.getEventsOn(LocalDate.of(2025, 5, 5));
    assertEquals(1, events.size());
  }

  @Test
  public void testExportToIcal() throws IOException {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    calendar.addEvent(event);

    String path = calendar.exportToIcal("test_calendar.ical");
    assertTrue(Files.exists(Paths.get(path)));

    List<String> lines = Files.readAllLines(Paths.get(path));
    assertTrue(lines.stream().anyMatch(line -> line.contains("BEGIN:VCALENDAR")));
    assertTrue(lines.stream().anyMatch(line -> line.contains("SUMMARY:Meeting")));

    Files.deleteIfExists(Paths.get(path));
  }

  @Test
  public void testExportAutoDetectCsv() throws IOException {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    calendar.addEvent(event);

    String path = calendar.export("test.csv");
    assertTrue(path.endsWith("test.csv"));
    assertTrue(Files.exists(Paths.get(path)));

    Files.deleteIfExists(Paths.get(path));
  }

  @Test
  public void testExportAutoDetectIcal() throws IOException {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    calendar.addEvent(event);

    String path = calendar.export("test.ical");
    assertTrue(path.endsWith("test.ical"));
    assertTrue(Files.exists(Paths.get(path)));

    Files.deleteIfExists(Paths.get(path));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExportUnsupportedFormat() throws IOException {
    calendar.export("test.txt");
  }

  @Test
  public void testEquals() {
    Calendar cal1 = new Calendar("Work", ZoneId.of("America/New_York"));
    Calendar cal2 = new Calendar("Work", ZoneId.of("America/Chicago"));

    assertEquals(cal1, cal2);
  }

  @Test
  public void testEqualsSameObject() {
    assertEquals(calendar, calendar);
  }

  @Test
  public void testNotEquals() {
    Calendar other = new Calendar("Personal", ZoneId.of("America/New_York"));
    assertNotEquals(calendar, other);
  }

  @Test
  public void testHashCode() {
    Calendar cal1 = new Calendar("Work", ZoneId.of("America/New_York"));
    Calendar cal2 = new Calendar("Work", ZoneId.of("America/Chicago"));

    assertEquals(cal1.hashCode(), cal2.hashCode());
  }

  @Test
  public void testToString() {
    String str = calendar.toString();
    assertTrue(str.contains("Work"));
    assertTrue(str.contains("America/New_York"));
  }

  @Test
  public void testNotEqualsNull() {
    assertFalse(calendar.equals(null));
  }

  @Test
  public void testNotEqualsDifferentClass() {
    assertFalse(calendar.equals("Not a calendar"));
  }

  /**
   * Test that Calendar requires explicit timezone (no default).
   * This documents that all calendars must have a timezone specified.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testCalendarRequiresTimezone() {
    new Calendar("Work", null);
  }
}