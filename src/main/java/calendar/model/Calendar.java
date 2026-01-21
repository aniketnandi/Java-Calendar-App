package calendar.model;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Represents a named calendar with a specific timezone.
 * Wraps a CalendarModel implementation and adds calendar-level properties.
 */
public class Calendar {
  private String name;
  private ZoneId timezone;
  private final CalendarModel model;

  /**
   * Creates a new Calendar with the specified name and timezone.
   *
   * @param name     the unique name of this calendar
   * @param timezone the timezone for this calendar
   * @throws IllegalArgumentException if name is null/empty or timezone is null
   */
  public Calendar(String name, ZoneId timezone) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be null or empty");
    }
    if (timezone == null) {
      throw new IllegalArgumentException("Timezone cannot be null");
    }
    this.name = name;
    this.timezone = timezone;
    this.model = new SimpleCalendar();
  }

  /**
   * Gets the name of this calendar.
   *
   * @return the calendar name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name of this calendar.
   *
   * @param name the new name
   * @throws IllegalArgumentException if name is null or empty
   */
  public void setName(String name) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be null or empty");
    }
    this.name = name;
  }

  /**
   * Gets the timezone of this calendar.
   *
   * @return the calendar's timezone
   */
  public ZoneId getTimezone() {
    return timezone;
  }

  /**
   * Sets the timezone of this calendar.
   *
   * @param timezone the new timezone
   * @throws IllegalArgumentException if timezone is null
   */
  public void setTimezone(ZoneId timezone) {
    if (timezone == null) {
      throw new IllegalArgumentException("Timezone cannot be null");
    }
    this.timezone = timezone;
  }

  /**
   * Converts a LocalDateTime from this calendar's timezone to another timezone.
   *
   * @param dateTime       the date/time in this calendar's timezone
   * @param targetTimezone the target timezone
   * @return the date/time converted to the target timezone
   */
  public LocalDateTime convertToTimezone(LocalDateTime dateTime, ZoneId targetTimezone) {
    ZonedDateTime zonedDateTime = dateTime.atZone(this.timezone);
    ZonedDateTime converted = zonedDateTime.withZoneSameInstant(targetTimezone);
    return converted.toLocalDateTime();
  }

  /**
   * Converts a LocalDateTime from another timezone to this calendar's timezone.
   *
   * @param dateTime       the date/time in the source timezone
   * @param sourceTimezone the source timezone
   * @return the date/time converted to this calendar's timezone
   */
  public LocalDateTime convertFromTimezone(LocalDateTime dateTime, ZoneId sourceTimezone) {
    ZonedDateTime zonedDateTime = dateTime.atZone(sourceTimezone);
    ZonedDateTime converted = zonedDateTime.withZoneSameInstant(this.timezone);
    return converted.toLocalDateTime();
  }

  /**
   * Add a single event to this calendar.
   *
   * @param event the event to add
   * @throws IllegalArgumentException if event already exists
   */
  public void addEvent(Event event) throws IllegalArgumentException {
    model.addEvent(event);
  }

  /**
   * Add an event series to this calendar.
   *
   * @param eventSeries the event series to add
   * @throws IllegalArgumentException if any event in series already exists
   */
  public void addEventSeries(EventSeries eventSeries) throws IllegalArgumentException {
    model.addEventSeries(eventSeries);
  }

  /**
   * Remove an event from this calendar.
   *
   * @param event the event to remove
   */
  public void removeEvent(Event event) {
    model.removeEvent(event);
  }

  /**
   * Removes an event and all future events in its series.
   * If event is not part of a series, removes only that event.
   *
   * @param event particular event from which operation is done.
   */
  public void removeEventFromSeries(Event event) {
    model.removeEventFromSeries(event);
  }

  /**
   * Removes all events in a series.
   * If event is not part of a series, removes only that event.
   *
   * @param event particular event on which operation is done.
   */
  public void removeAllEventsInSeries(Event event) {
    model.removeAllEventsInSeries(event);
  }

  /**
   * Edit an event in this calendar.
   *
   * @param subject  the subject of the event to edit
   * @param start    start date and time of the event to edit
   * @param end      end date and time of the event to edit
   * @param property the property to be modified
   * @param value    the new value to be set
   * @throws IllegalArgumentException if event is not found or edit creates duplicate
   */
  public void editEvent(String subject, LocalDateTime start, LocalDateTime end,
                        String property, Object value) throws IllegalArgumentException {
    model.editEvent(subject, start, end, property, value);
  }

  /**
   * Edits events from a particular point onwards in a series.
   *
   * @param subject  the subject of the event to edit
   * @param start    start date and time of the event to edit
   * @param property the property to be modified
   * @param value    the new value to be set
   * @throws IllegalArgumentException if event is not found or edit creates duplicate
   */
  public void editEventsFrom(String subject, LocalDateTime start, LocalDateTime end,
                             String property, Object value)
      throws IllegalArgumentException {
    model.editEventsFrom(subject, start, end, property, value);
  }

  /**
   * Edits all events in the series.
   *
   * @param subject  the subject of the event to edit
   * @param start    start date and time of the event to edit
   * @param property the property to be modified
   * @param value    the new value to be set
   * @throws IllegalArgumentException if eventseries is not found or edit creates duplicate event
   */
  public void editAllEventsInSeries(String subject, LocalDateTime start, LocalDateTime end,
                                    String property,
                                    Object value) throws IllegalArgumentException {
    model.editAllEventsInSeries(subject, start, end, property, value);
  }

  /**
   * Gets all events occurring on a specific date.
   *
   * @param date the date to query
   * @return list of events on that date, sorted by start time
   */
  public List<Event> getEventsOn(LocalDate date) {
    return model.getEventsOn(date);
  }

  /**
   * Gets all events in a time range.
   *
   * @param start the start of the range
   * @param end   the end of the range
   * @return list of events in that range, sorted by start time
   */
  public List<Event> getEventsInRange(LocalDateTime start, LocalDateTime end) {
    return model.getEventsInRange(start, end);
  }

  /**
   * Check if user is busy at a specific time.
   *
   * @param dateTime the date/time to check
   * @return true if busy, false if available
   */
  public boolean isBusy(LocalDateTime dateTime) {
    return model.isBusy(dateTime);
  }

  /**
   * Exports all calendar events to csv format.
   *
   * @param fileName the name of the file to export to
   * @return the absolute path of the exported file
   * @throws IOException if file cannot be written
   */
  public String exportToCsv(String fileName) throws IOException {
    return model.exportToCsv(fileName);
  }

  /**
   * Exports all calendar events to iCal format.
   *
   * @param fileName the name of the file to export to
   * @return the absolute path of the exported file
   * @throws IOException if file cannot be written
   */
  public String exportToIcal(String fileName) throws IOException {
    List<Event> allEvents = model.getEventsInRange(
        LocalDateTime.of(1900, 1, 1, 0, 0),
        LocalDateTime.of(2100, 12, 31, 23, 59));
    return IcalExporter.exportToIcal(allEvents, this.timezone, fileName);
  }

  /**
   * Exports calendar events to the appropriate format based on file extension.
   * Supports .csv and .ical formats.
   *
   * @param fileName the name of the file to export to
   * @return the absolute path of the exported file
   * @throws IOException              if file cannot be written
   * @throws IllegalArgumentException if file extension is not recognized
   */
  public String export(String fileName) throws IOException {
    String lowerFileName = fileName.toLowerCase();
    if (lowerFileName.endsWith(".csv")) {
      return exportToCsv(fileName);
    } else if (lowerFileName.endsWith(".ical")) {
      return exportToIcal(fileName);
    } else {
      throw new IllegalArgumentException(
          "Unsupported file format. Use .csv or .ical extension");
    }
  }

  /**
   * Gets the underlying calendar model.
   * Package-private for use by CalendarManager.
   *
   * @return the underlying CalendarModel
   */
  CalendarModel getModel() {
    return model;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Calendar)) {
      return false;
    }
    Calendar otherCal = (Calendar) other;
    return Objects.equals(name, otherCal.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  @Override
  public String toString() {
    return String.format("Calendar{name='%s', timezone=%s}", name, timezone);
  }
}