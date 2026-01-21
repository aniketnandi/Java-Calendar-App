package model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import calendar.model.CalendarEvent;
import calendar.model.Event;
import calendar.model.IcalExporter;
import calendar.model.Status;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

/**
 * Test class for IcalExporter.
 */
public class IcalExporterTest {

  @Test
  public void testExportToIcal() throws IOException {
    List<Event> events = new ArrayList<>();
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    events.add(event);

    String path = IcalExporter.exportToIcal(events,
        ZoneId.of("America/New_York"), "test.ical");

    assertTrue(Files.exists(Paths.get(path)));
    List<String> lines = Files.readAllLines(Paths.get(path));

    assertTrue(lines.stream().anyMatch(l -> l.contains("BEGIN:VCALENDAR")));
    assertTrue(lines.stream().anyMatch(l -> l.contains("END:VCALENDAR")));
    assertTrue(lines.stream().anyMatch(l -> l.contains("BEGIN:VEVENT")));
    assertTrue(lines.stream().anyMatch(l -> l.contains("SUMMARY:Meeting")));

    Files.deleteIfExists(Paths.get(path));
  }

  @Test
  public void testExportWithAllProperties() throws IOException {
    List<Event> events = new ArrayList<>();
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .description("Important meeting")
        .location("Conference Room A")
        .status(Status.PRIVATE)
        .build();
    events.add(event);

    String path = IcalExporter.exportToIcal(events,
        ZoneId.of("America/New_York"), "test_full.ical");

    List<String> lines = Files.readAllLines(Paths.get(path));

    assertTrue(lines.stream().anyMatch(l -> l.contains("DESCRIPTION:Important meeting")));
    assertTrue(lines.stream().anyMatch(l -> l.contains("LOCATION:Conference Room A")));
    assertTrue(lines.stream().anyMatch(l -> l.contains("CLASS:PRIVATE")));

    Files.deleteIfExists(Paths.get(path));
  }

  @Test
  public void testExportAllDayEvent() throws IOException {
    List<Event> events = new ArrayList<>();
    Event event = new CalendarEvent.EventBuilder()
        .subject("All Day Event")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 0, 0))
        .build();
    events.add(event);

    String path = IcalExporter.exportToIcal(events,
        ZoneId.of("America/New_York"), "test_allday.ical");

    List<String> lines = Files.readAllLines(Paths.get(path));

    assertTrue(lines.stream().anyMatch(l -> l.contains("DTSTART;VALUE=DATE:")));
    assertTrue(lines.stream().anyMatch(l -> l.contains("DTEND;VALUE=DATE:")));

    Files.deleteIfExists(Paths.get(path));
  }

  @Test
  public void testExportMultipleEvents() throws IOException {
    List<Event> events = new ArrayList<>();
    events.add(new CalendarEvent.EventBuilder()
        .subject("Event1")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build());
    events.add(new CalendarEvent.EventBuilder()
        .subject("Event2")
        .startDateTime(LocalDateTime.of(2025, 5, 6, 14, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 6, 15, 0))
        .build());

    String path = IcalExporter.exportToIcal(events,
        ZoneId.of("America/New_York"), "test_multiple.ical");

    List<String> lines = Files.readAllLines(Paths.get(path));

    long eventCount = lines.stream().filter(l -> l.contains("BEGIN:VEVENT")).count();
    assertTrue(eventCount == 2);

    Files.deleteIfExists(Paths.get(path));
  }

  @Test
  public void testExportWithSpecialCharacters() throws IOException {
    List<Event> events = new ArrayList<>();
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting, with; special\\chars")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .description("Description\nwith newline")
        .location("Room, A; Building\\B")
        .build();
    events.add(event);

    String path = IcalExporter.exportToIcal(events,
        ZoneId.of("America/New_York"), "test_special.ical");

    List<String> lines = Files.readAllLines(Paths.get(path));

    assertTrue(lines.stream().anyMatch(l -> l.contains("\\,")));
    assertTrue(lines.stream().anyMatch(l -> l.contains("\\;")));
    assertTrue(lines.stream().anyMatch(l -> l.contains("\\n")));

    Files.deleteIfExists(Paths.get(path));
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
              // Ignore
            }
          });
    }

    List<Event> events = new ArrayList<>();
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    events.add(event);

    String path = IcalExporter.exportToIcal(events,
        ZoneId.of("America/New_York"), "test_dir.ical");

    assertTrue(Files.exists(Paths.get("exports")));
    assertTrue(Files.exists(Paths.get(path)));

    Files.deleteIfExists(Paths.get(path));
  }

  @Test
  public void testExportWithPublicStatus() throws IOException {
    List<Event> events = new ArrayList<>();
    Event event = new CalendarEvent.EventBuilder()
        .subject("Public Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .status(Status.PUBLIC)
        .build();
    events.add(event);

    String path = IcalExporter.exportToIcal(events,
        ZoneId.of("America/New_York"), "test_public.ical");

    List<String> lines = Files.readAllLines(Paths.get(path));
    assertTrue(lines.stream().anyMatch(l -> l.contains("CLASS:PUBLIC")));
    assertFalse(lines.stream().anyMatch(l -> l.contains("CLASS:PRIVATE")));

    Files.deleteIfExists(Paths.get(path));
  }

  @Test
  public void testExportTimedEvent() throws IOException {
    List<Event> events = new ArrayList<>();
    Event event = new CalendarEvent.EventBuilder()
        .subject("Timed Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 14, 30))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 15, 45))
        .build();
    events.add(event);

    String path = IcalExporter.exportToIcal(events,
        ZoneId.of("America/New_York"), "test_timed.ical");

    List<String> lines = Files.readAllLines(Paths.get(path));

    assertTrue(lines.stream().anyMatch(l -> l.contains("DTSTART;TZID=")));
    assertTrue(lines.stream().anyMatch(l -> l.contains("DTEND;TZID=")));
    assertFalse(lines.stream().anyMatch(l -> l.contains("VALUE=DATE")));

    Files.deleteIfExists(Paths.get(path));
  }

  @Test
  public void testExportWithEmptyDescription() throws IOException {
    List<Event> events = new ArrayList<>();
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .description("")
        .build();
    events.add(event);

    String path = IcalExporter.exportToIcal(events,
        ZoneId.of("America/New_York"), "test_empty_desc.ical");

    List<String> lines = Files.readAllLines(Paths.get(path));
    String fullContent = String.join("\n", lines);
    assertFalse(fullContent.contains("DESCRIPTION:"));

    Files.deleteIfExists(Paths.get(path));
  }

  @Test
  public void testExportWithNullDescription() throws IOException {
    List<Event> events = new ArrayList<>();
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .description(null)
        .build();
    events.add(event);

    String path = IcalExporter.exportToIcal(events,
        ZoneId.of("America/New_York"), "test_null_desc.ical");

    List<String> lines = Files.readAllLines(Paths.get(path));
    String fullContent = String.join("\n", lines);
    assertFalse(fullContent.contains("DESCRIPTION:"));

    Files.deleteIfExists(Paths.get(path));
  }

  @Test
  public void testExportWithEmptyLocation() throws IOException {
    List<Event> events = new ArrayList<>();
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .location("")
        .build();
    events.add(event);

    String path = IcalExporter.exportToIcal(events,
        ZoneId.of("America/New_York"), "test_empty_loc.ical");

    List<String> lines = Files.readAllLines(Paths.get(path));
    String fullContent = String.join("\n", lines);
    assertFalse(fullContent.contains("LOCATION:"));

    Files.deleteIfExists(Paths.get(path));
  }

  @Test
  public void testExportWithNullLocation() throws IOException {
    List<Event> events = new ArrayList<>();
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .location(null)
        .build();
    events.add(event);

    String path = IcalExporter.exportToIcal(events,
        ZoneId.of("America/New_York"), "test_null_loc.ical");

    List<String> lines = Files.readAllLines(Paths.get(path));
    String fullContent = String.join("\n", lines);
    assertFalse(fullContent.contains("LOCATION:"));

    Files.deleteIfExists(Paths.get(path));
  }

  @Test
  public void testExportHeaderFields() throws IOException {
    List<Event> events = new ArrayList<>();
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    events.add(event);

    String path = IcalExporter.exportToIcal(events,
        ZoneId.of("America/New_York"), "test_header.ical");

    List<String> lines = Files.readAllLines(Paths.get(path));

    assertTrue(lines.stream().anyMatch(l -> l.equals("BEGIN:VCALENDAR")));
    assertTrue(lines.stream().anyMatch(l -> l.equals("VERSION:2.0")));
    assertTrue(lines.stream().anyMatch(l -> l.equals("PRODID:-//Calendar Application//EN")));
    assertTrue(lines.stream().anyMatch(l -> l.equals("CALSCALE:GREGORIAN")));
    assertTrue(lines.stream().anyMatch(l -> l.equals("METHOD:PUBLISH")));
    assertTrue(lines.stream().anyMatch(l -> l.equals("END:VCALENDAR")));

    Files.deleteIfExists(Paths.get(path));
  }

  @Test
  public void testExportTimezoneFields() throws IOException {
    List<Event> events = new ArrayList<>();
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    events.add(event);

    String path = IcalExporter.exportToIcal(events,
        ZoneId.of("America/New_York"), "test_tz.ical");

    List<String> lines = Files.readAllLines(Paths.get(path));

    assertTrue(lines.stream().anyMatch(l -> l.equals("BEGIN:VTIMEZONE")));
    assertTrue(lines.stream().anyMatch(l -> l.equals("TZID:America/New_York")));
    assertTrue(lines.stream().anyMatch(l -> l.equals("BEGIN:STANDARD")));
    assertTrue(lines.stream().anyMatch(l -> l.equals("DTSTART:19700101T000000")));
    assertTrue(lines.stream().anyMatch(l -> l.startsWith("TZOFFSETFROM:")));
    assertTrue(lines.stream().anyMatch(l -> l.startsWith("TZOFFSETTO:")));
    assertTrue(lines.stream().anyMatch(l -> l.equals("END:STANDARD")));
    assertTrue(lines.stream().anyMatch(l -> l.equals("END:VTIMEZONE")));

    Files.deleteIfExists(Paths.get(path));
  }

  @Test
  public void testExportEventFields() throws IOException {
    List<Event> events = new ArrayList<>();
    Event event = new CalendarEvent.EventBuilder()
        .subject("Test Event")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    events.add(event);

    String path = IcalExporter.exportToIcal(events,
        ZoneId.of("America/New_York"), "test_event.ical");

    List<String> lines = Files.readAllLines(Paths.get(path));

    assertTrue(lines.stream().anyMatch(l -> l.equals("BEGIN:VEVENT")));
    assertTrue(lines.stream().anyMatch(l -> l.startsWith("UID:")));
    assertTrue(lines.stream().anyMatch(l -> l.startsWith("DTSTAMP:")));
    assertTrue(lines.stream().anyMatch(l -> l.equals("SUMMARY:Test Event")));
    assertTrue(lines.stream().anyMatch(l -> l.equals("END:VEVENT")));

    Files.deleteIfExists(Paths.get(path));
  }

  @Test
  public void testExportSortsEvents() throws IOException {
    List<Event> events = new ArrayList<>();

    events.add(new CalendarEvent.EventBuilder()
        .subject("Third")
        .startDateTime(LocalDateTime.of(2025, 5, 7, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 7, 11, 0))
        .build());

    events.add(new CalendarEvent.EventBuilder()
        .subject("First")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build());

    events.add(new CalendarEvent.EventBuilder()
        .subject("Second")
        .startDateTime(LocalDateTime.of(2025, 5, 6, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 6, 11, 0))
        .build());

    String path = IcalExporter.exportToIcal(events,
        ZoneId.of("America/New_York"), "test_sorted.ical");

    List<String> lines = Files.readAllLines(Paths.get(path));

    int firstIndex = -1;
    int secondIndex = -1;
    int thirdIndex = -1;

    for (int i = 0; i < lines.size(); i++) {
      if (lines.get(i).contains("SUMMARY:First")) {
        firstIndex = i;
      } else if (lines.get(i).contains("SUMMARY:Second")) {
        secondIndex = i;
      } else if (lines.get(i).contains("SUMMARY:Third")) {
        thirdIndex = i;
      }
    }

    assertTrue(firstIndex < secondIndex);
    assertTrue(secondIndex < thirdIndex);
    assertTrue(firstIndex > 0);

    Files.deleteIfExists(Paths.get(path));
  }

  @Test
  public void testExportWithBackslashInText() throws IOException {
    List<Event> events = new ArrayList<>();
    Event event = new CalendarEvent.EventBuilder()
        .subject("Path\\to\\file")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    events.add(event);

    String path = IcalExporter.exportToIcal(events,
        ZoneId.of("America/New_York"), "test_backslash.ical");

    List<String> lines = Files.readAllLines(Paths.get(path));
    assertTrue(lines.stream().anyMatch(l -> l.contains("SUMMARY:Path\\\\to\\\\file")));

    Files.deleteIfExists(Paths.get(path));
  }

  @Test
  public void testGenerateUidWithSpecialCharacters() throws IOException {
    List<Event> events = new ArrayList<>();
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting@#$%^&*()")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    events.add(event);

    String path = IcalExporter.exportToIcal(events,
        ZoneId.of("America/New_York"), "test_uid.ical");

    List<String> lines = Files.readAllLines(Paths.get(path));
    assertTrue(lines.stream().anyMatch(l -> l.startsWith("UID:")
        && l.endsWith("@calendar.app")
        && !l.contains("@#$%^&*()")));

    Files.deleteIfExists(Paths.get(path));
  }

  @Test
  public void testEscapeIcalTextNull() throws IOException {
    List<Event> events = new ArrayList<>();
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .description(null)
        .location(null)
        .build();
    events.add(event);

    String path = IcalExporter.exportToIcal(events,
        ZoneId.of("America/New_York"), "test_null.ical");

    assertTrue(Files.exists(Paths.get(path)));

    Files.deleteIfExists(Paths.get(path));
  }

  @Test
  public void testExportWithDifferentTimezone() throws IOException {
    List<Event> events = new ArrayList<>();
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    events.add(event);

    String path = IcalExporter.exportToIcal(events,
        ZoneId.of("Europe/London"), "test_london.ical");

    List<String> lines = Files.readAllLines(Paths.get(path));
    assertTrue(lines.stream().anyMatch(l -> l.equals("TZID:Europe/London")));

    Files.deleteIfExists(Paths.get(path));
  }

  @Test
  public void testTimezoneOffsetPositive() throws IOException {
    List<Event> events = new ArrayList<>();
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    events.add(event);

    String path = IcalExporter.exportToIcal(events,
        ZoneId.of("Asia/Kolkata"), "test_kolkata.ical");

    List<String> lines = Files.readAllLines(Paths.get(path));
    assertTrue(lines.stream().anyMatch(l -> l.contains("TZOFFSETFROM:+0")));
    assertTrue(lines.stream().anyMatch(l -> l.contains("TZOFFSETTO:+0")));

    Files.deleteIfExists(Paths.get(path));
  }

  @Test
  public void testTimezoneOffsetNegative() throws IOException {
    List<Event> events = new ArrayList<>();
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();
    events.add(event);

    String path = IcalExporter.exportToIcal(events,
        ZoneId.of("America/Los_Angeles"), "test_la.ical");

    List<String> lines = Files.readAllLines(Paths.get(path));
    assertTrue(lines.stream().anyMatch(l -> l.contains("TZOFFSETFROM:-0")));
    assertTrue(lines.stream().anyMatch(l -> l.contains("TZOFFSETTO:-0")));

    Files.deleteIfExists(Paths.get(path));
  }
}