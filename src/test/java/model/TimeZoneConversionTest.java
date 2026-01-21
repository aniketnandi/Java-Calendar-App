package model;

import static org.junit.Assert.assertEquals;

import calendar.model.Calendar;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.Test;

/**
 * Test class specifically for timezone conversion functionality.
 */
public class TimeZoneConversionTest {

  @Test
  public void testConvertToTimezoneesttoPst() {
    Calendar estCalendar = new Calendar("EST", ZoneId.of("America/New_York"));

    LocalDateTime estTime = LocalDateTime.of(2025, 5, 5, 14, 0);
    LocalDateTime pstTime = estCalendar.convertToTimezone(estTime,
        ZoneId.of("America/Los_Angeles"));

    assertEquals(LocalDateTime.of(2025, 5, 5, 11, 0), pstTime);
  }

  @Test
  public void testConvertToTimezoneesttocst() {
    Calendar estCalendar = new Calendar("EST", ZoneId.of("America/New_York"));

    LocalDateTime estTime = LocalDateTime.of(2025, 5, 5, 14, 0);
    LocalDateTime cstTime = estCalendar.convertToTimezone(estTime,
        ZoneId.of("America/Chicago"));

    assertEquals(LocalDateTime.of(2025, 5, 5, 13, 0), cstTime);
  }

  @Test
  public void testConvertToTimezoneesttoutc() {
    Calendar estCalendar = new Calendar("EST", ZoneId.of("America/New_York"));

    LocalDateTime estTime = LocalDateTime.of(2025, 5, 5, 14, 0);
    LocalDateTime utcTime = estCalendar.convertToTimezone(estTime, ZoneId.of("UTC"));

    assertEquals(LocalDateTime.of(2025, 5, 5, 18, 0), utcTime);
  }

  @Test
  public void testConvertToTimezoneSameTimezone() {
    Calendar estCalendar = new Calendar("EST", ZoneId.of("America/New_York"));

    LocalDateTime estTime = LocalDateTime.of(2025, 5, 5, 14, 0);
    LocalDateTime sameTime = estCalendar.convertToTimezone(estTime,
        ZoneId.of("America/New_York"));

    assertEquals(estTime, sameTime);
  }

  @Test
  public void testConvertFromTimezonePsttoest() {
    Calendar estCalendar = new Calendar("EST", ZoneId.of("America/New_York"));

    LocalDateTime pstTime = LocalDateTime.of(2025, 5, 5, 11, 0);
    LocalDateTime estTime = estCalendar.convertFromTimezone(pstTime,
        ZoneId.of("America/Los_Angeles"));

    assertEquals(LocalDateTime.of(2025, 5, 5, 14, 0), estTime);
  }

  @Test
  public void testConvertToTimezoneAcrossDaylightSavingBoundary() {
    Calendar estCalendar = new Calendar("EST", ZoneId.of("America/New_York"));

    LocalDateTime winterTime = LocalDateTime.of(2025, 1, 15, 14, 0);
    LocalDateTime winterutc = estCalendar.convertToTimezone(winterTime, ZoneId.of("UTC"));
    assertEquals(LocalDateTime.of(2025, 1, 15, 19, 0), winterutc);

    LocalDateTime summerTime = LocalDateTime.of(2025, 7, 15, 14, 0);
    LocalDateTime summerutc = estCalendar.convertToTimezone(summerTime, ZoneId.of("UTC"));
    assertEquals(LocalDateTime.of(2025, 7, 15, 18, 0), summerutc);
  }

  @Test
  public void testConvertToTimezoneInternational() {
    Calendar estCalendar = new Calendar("EST", ZoneId.of("America/New_York"));

    LocalDateTime estTime = LocalDateTime.of(2025, 5, 5, 14, 0);

    LocalDateTime londonTime = estCalendar.convertToTimezone(estTime,
        ZoneId.of("Europe/London"));
    assertEquals(LocalDateTime.of(2025, 5, 5, 19, 0), londonTime);

    LocalDateTime tokyoTime = estCalendar.convertToTimezone(estTime,
        ZoneId.of("Asia/Tokyo"));
    assertEquals(LocalDateTime.of(2025, 5, 6, 3, 0), tokyoTime);
  }
}