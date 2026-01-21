package calendar.model;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Utility class for exporting calendar events to iCal format.
 * Conforms to the iCalendar (RFC 5545) specification.
 */
public class IcalExporter {
  private static final DateTimeFormatter ICAL_DATETIME_FORMAT =
      DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
  private static final String ICAL_VERSION = "2.0";
  private static final String PRODID = "-//Calendar Application//EN";

  /**
   * Exports events to an iCal file.
   *
   * @param events the events to export
   * @param timezone the timezone of the calendar
   * @param fileName the name of the file to create
   * @return the absolute path of the exported file
   * @throws IOException if there's an error writing the file
   */
  public static String exportToIcal(List<Event> events, ZoneId timezone, String fileName)
      throws IOException {
    Path exportDir = Paths.get("exports");

    if (!Files.exists(exportDir)) {
      Files.createDirectories(exportDir);
    }

    Path filePath = exportDir.resolve(fileName);
    String absolutePath = filePath.toAbsolutePath().toString();

    try (FileWriter fw = new FileWriter(absolutePath)) {
      fw.write("BEGIN:VCALENDAR\r\n");
      fw.write("VERSION:" + ICAL_VERSION + "\r\n");
      fw.write("PRODID:" + PRODID + "\r\n");
      fw.write("CALSCALE:GREGORIAN\r\n");
      fw.write("METHOD:PUBLISH\r\n");

      writeTimezone(fw, timezone);

      List<Event> sortedEvents = new ArrayList<>(events);
      sortedEvents.sort(Comparator.comparing(Event::getStartDateTime));

      for (Event event : sortedEvents) {
        writeEvent(fw, event, timezone);
      }

      fw.write("END:VCALENDAR\r\n");
    }

    return absolutePath;
  }

  /**
   * Writes timezone information to the iCal file.
   *
   * @param fw the file writer
   * @param timezone the timezone
   * @throws IOException if there's an error writing
   */
  private static void writeTimezone(FileWriter fw, ZoneId timezone) throws IOException {
    String tzId = timezone.getId();
    fw.write("BEGIN:VTIMEZONE\r\n");
    fw.write("TZID:" + tzId + "\r\n");

    fw.write("BEGIN:STANDARD\r\n");
    fw.write("DTSTART:19700101T000000\r\n");
    fw.write("TZOFFSETFROM:" + getTimezoneOffset(timezone) + "\r\n");
    fw.write("TZOFFSETTO:" + getTimezoneOffset(timezone) + "\r\n");
    fw.write("END:STANDARD\r\n");

    fw.write("END:VTIMEZONE\r\n");
  }

  /**
   * Gets the timezone offset in iCal format (+/-HHMM).
   *
   * @param timezone the timezone
   * @return the offset string
   */
  private static String getTimezoneOffset(ZoneId timezone) {
    ZonedDateTime now = ZonedDateTime.now(timezone);
    int offsetSeconds = now.getOffset().getTotalSeconds();
    int hours = offsetSeconds / 3600;
    int minutes = Math.abs((offsetSeconds % 3600) / 60);
    return String.format("%+03d%02d", hours, minutes);
  }

  /**
   * Writes a single event to the iCal file.
   *
   * @param fw the file writer
   * @param event the event to write
   * @param timezone the timezone
   * @throws IOException if there's an error writing
   */
  private static void writeEvent(FileWriter fw, Event event, ZoneId timezone)
      throws IOException {
    fw.write("BEGIN:VEVENT\r\n");

    String uid = generateUiD(event);
    fw.write("UID:" + uid + "\r\n");

    String timestamp = ZonedDateTime.now(timezone).format(ICAL_DATETIME_FORMAT);
    fw.write("DTSTAMP:" + timestamp + "\r\n");

    if (event.isAllDay()) {
      String startDate = event.getStartDateTime().toLocalDate()
          .format(DateTimeFormatter.BASIC_ISO_DATE);
      fw.write("DTSTART;VALUE=DATE:" + startDate + "\r\n");
    } else {
      ZonedDateTime startZoned = event.getStartDateTime().atZone(timezone);
      fw.write("DTSTART;TZID=" + timezone.getId() + ":"
          + startZoned.format(ICAL_DATETIME_FORMAT) + "\r\n");
    }

    if (event.isAllDay()) {
      String endDate = event.getEndDateTime().toLocalDate().plusDays(1)
          .format(DateTimeFormatter.BASIC_ISO_DATE);
      fw.write("DTEND;VALUE=DATE:" + endDate + "\r\n");
    } else {
      ZonedDateTime endZoned = event.getEndDateTime().atZone(timezone);
      fw.write("DTEND;TZID=" + timezone.getId() + ":"
          + endZoned.format(ICAL_DATETIME_FORMAT) + "\r\n");
    }

    fw.write("SUMMARY:" + escapeIcalText(event.getSubject()) + "\r\n");

    if (event.getDescription() != null && !event.getDescription().isEmpty()) {
      fw.write("DESCRIPTION:" + escapeIcalText(event.getDescription()) + "\r\n");
    }

    if (event.getLocation() != null && !event.getLocation().isEmpty()) {
      fw.write("LOCATION:" + escapeIcalText(event.getLocation()) + "\r\n");
    }

    if (event.getStatus() == Status.PRIVATE) {
      fw.write("CLASS:PRIVATE\r\n");
    } else {
      fw.write("CLASS:PUBLIC\r\n");
    }

    fw.write("END:VEVENT\r\n");
  }

  /**
   * Generates a unique identifier for an event.
   *
   * @param event the event
   * @return a UID string
   */
  private static String generateUiD(Event event) {
    String base = event.getSubject() + "-"
        + event.getStartDateTime().toString() + "-"
        + event.getEndDateTime().toString();
    return base.replaceAll("[^a-zA-Z0-9-]", "") + "@calendar.app";
  }

  /**
   * Escapes special characters in iCal text fields.
   *
   * @param text the text to escape
   * @return the escaped text
   */
  private static String escapeIcalText(String text) {
    if (text == null) {
      return "";
    }
    return text.replace("\\", "\\\\")
        .replace(",", "\\,")
        .replace(";", "\\;")
        .replace("\n", "\\n");
  }
}