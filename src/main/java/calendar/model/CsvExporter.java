package calendar.model;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Utility class for exporting calendar events to CSV format.
 * Conforms to the Google Calendar CSV format specification.
 */
public class CsvExporter {
  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("MM/dd/yyyy");
  private static final DateTimeFormatter TIME_FORMATTER =
      DateTimeFormatter.ofPattern("HH:mm a");

  /**
   * Exports events to a CSV file.
   *
   * @param events the events to export
   * @param fileName the name of the file to create
   * @return the absolute path of the exported file
   * @throws IOException if there's an error writing the file
   */
  public static String exportToCsv(List<Event> events, String fileName) throws IOException {
    Path exportDir = Paths.get("exports");

    if (!Files.exists(exportDir)) {
      Files.createDirectories(exportDir);
    }

    Path filePath = exportDir.resolve(fileName);
    String absolutePath = filePath.toAbsolutePath().toString();

    try (FileWriter fw = new FileWriter(absolutePath)) {
      fw.write("Subject,Start Date,Start Time,End Date,End Time,All Day Event,"
          + "Description,Location,Private\n");

      List<Event> sortedEvents = new ArrayList<>(events);
      sortedEvents.sort(Comparator.comparing(Event::getStartDateTime));

      for (Event event : sortedEvents) {
        fw.write(formatEventForCsv(event));
        fw.write("\n");
      }
    }

    return absolutePath;
  }

  /**
   * Formats an event for CSV export.
   *
   * @param event the event to format
   * @return a string representing the event in CSV format
   */
  private static String formatEventForCsv(Event event) {
    String subject = escapeCsv(event.getSubject());
    String startDate = event.getStartDateTime().format(DATE_FORMATTER);
    String startTime = event.getStartDateTime().format(TIME_FORMATTER);
    String endDate = event.getEndDateTime().format(DATE_FORMATTER);
    String endTime = event.getEndDateTime().format(TIME_FORMATTER);
    String allDay = event.isAllDay() ? "True" : "False";
    String description = escapeCsv(event.getDescription());
    String location = escapeCsv(event.getLocation());
    String isPrivate = event.getStatus() == Status.PRIVATE ? "True" : "False";

    return String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s",
        subject, startDate, startTime, endDate, endTime, allDay,
        description, location, isPrivate);
  }

  /**
   * Escapes a string for CSV formatting.
   * Handles commas, quotes, and newlines according to CSV standards.
   *
   * @param value the value to escape
   * @return the escaped string
   */
  private static String escapeCsv(String value) {
    if (value == null || value.isEmpty()) {
      return "";
    }
    if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
      return "\"" + value.replace("\"", "\"\"") + "\"";
    }
    return value;
  }
}