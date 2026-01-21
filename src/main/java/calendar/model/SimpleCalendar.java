package calendar.model;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a simple calendar that stores and manages events and event series.
 * Provides functionality to add, edit, remove, query, and export calendar events.
 *
 * <p>This class is responsible for computing aggregated analytics for.
 *     the dashboard feature via {@link #generateAnalytics(LocalDate, LocalDate)}.</p>
 */
public class SimpleCalendar implements CalendarModel {
  private final Set<Event> allEvents;

  /**
   * Creates a new SimpleCalendar with empty event and series collections.
   */
  public SimpleCalendar() {
    this.allEvents = new HashSet<>();
  }

  /**
   * Adds a new event to the calendar.
   * Throws an IllegalArgumentException if an event with the same subject, start date,
   * and end date already exists.
   *
   * @param event the event to be added
   * @throws IllegalArgumentException if an event with the same subject, start,
   *                                  and end time already exists
   */
  @Override
  public void addEvent(Event event) throws IllegalArgumentException {
    if (this.allEvents.contains(event)) {
      throw new IllegalArgumentException(
          "Event with same subject, startDateTime and endDateTime already exists");
    }
    this.allEvents.add(event);
  }

  /**
   * Adds an event series to the calendar.
   * Adds all events from the event series to the calendar.
   *
   * @param eventSeries the event series to be added
   * @throws IllegalArgumentException if the event series contains events that already exist
   */
  @Override
  public void addEventSeries(EventSeries eventSeries) throws IllegalArgumentException {
    List<Event> events = eventSeries.getAllEvents();
    for (Event event : events) {
      addEvent(event);
    }
  }

  /**
   * Removes an event from the calendar.
   *
   * @param event the event to be removed
   */
  @Override
  public void removeEvent(Event event) {
    allEvents.remove(event);
  }

  @Override
  public void removeEventFromSeries(Event event) {
    if (event.getSeriesId() == null || event.getSeriesId().isEmpty()) {
      removeEvent(event);
      return;
    }
    String seriesId = event.getSeriesId();
    LocalDateTime fromTime = event.getStartDateTime();
    List<Event> toRemove = new ArrayList<>();
    for (Event e : allEvents) {
      if (seriesId.equals(e.getSeriesId())
          && !e.getStartDateTime().isBefore(fromTime)) {
        toRemove.add(e);
      }
    }
    toRemove.forEach(this::removeEvent);
  }

  @Override
  public void removeAllEventsInSeries(Event event) {
    if (event.getSeriesId() == null || event.getSeriesId().isEmpty()) {
      removeEvent(event);
      return;
    }
    String seriesId = event.getSeriesId();
    List<Event> toRemove = new ArrayList<>();
    for (Event e : allEvents) {
      if (seriesId.equals(e.getSeriesId())) {
        toRemove.add(e);
      }
    }
    toRemove.forEach(this::removeEvent);
  }

  /**
   * Edits an existing event based on the provided properties and values.
   * Throws an IllegalArgumentException if an event with the new properties would cause a duplicate.
   *
   * @param subject  the subject of the event to be edited
   * @param start    the start date and time of the event to be edited
   * @param end      the end date and time of the event to be edited
   * @param property the property of the event to be edited
   * @param value    the new value for the specified property
   * @throws IllegalArgumentException if the event does not exist or the edit results in a duplicate
   */
  @Override
  public void editEvent(String subject, LocalDateTime start, LocalDateTime end,
                        String property, Object value) throws IllegalArgumentException {
    Event targetEvent = findEvent(subject, start, end);
    editSingleEvent(targetEvent, property, value);
  }

  /**
   * Helper method to edit a single event.
   * Removes the old event, applies the changes, and adds the updated event back to the calendar.
   *
   * @param event    the event to be edited
   * @param property the property to change
   * @param value    the new value for the property
   */
  private void editSingleEvent(Event event, String property, Object value) {
    removeEvent(event);

    boolean shouldDetachFromSeries = (event.getSeriesId() != null && !event.getSeriesId().isEmpty())
        && (property.equalsIgnoreCase("start")
        || property.equalsIgnoreCase("startdatetime")
        || property.equalsIgnoreCase("end")
        || property.equalsIgnoreCase("enddatetime"));

    CalendarEvent.EventBuilder builder = updateEventProperty(event, property, value, false);
    if (shouldDetachFromSeries) {
      builder.seriesId(null);
    }

    CalendarEvent updatedEvent = builder.build();
    if (allEvents.contains(updatedEvent)) {
      addEvent(event);
      throw new IllegalArgumentException("Edit causes duplicate event");
    }
    addEvent(updatedEvent);
  }

  /**
   * Finds an event based on the provided subject, start, and end times.
   * Throws an IllegalArgumentException if no matching event is found.
   *
   * @param subject the subject of the event
   * @param start   the start date and time of the event
   * @param end     the end date and time of the event
   * @return the event matching the criteria
   * @throws IllegalArgumentException if no event with the provided subject, start, and end is found
   */
  private Event findEvent(String subject, LocalDateTime start, LocalDateTime end) {
    for (Event event : allEvents) {
      if (event.getSubject().equals(subject)
          && event.getStartDateTime().equals(start)
          && event.getEndDateTime().equals(end)) {
        return event;
      }
    }
    throw new IllegalArgumentException(
        "Event with given subject, startDateTime and endDateTime not found");
  }

  /**
   * Finds an event for a recurring event series based on subject and start time.
   *
   * @param subject the subject of the event
   * @param start   the start date and time of the event
   * @return the event found for the series
   * @throws IllegalArgumentException if multiple or no matching events are found
   */
  private Event findEventForSeries(String subject, LocalDateTime start, LocalDateTime end) {
    int cnt = 0;
    Event singleEvent = null;
    Event seriesEvent = null;
    boolean foundSeries = false;
    for (Event event : allEvents) {
      if (event.getSubject().equals(subject) && event.getStartDateTime().equals(start)
          && (end == null || event.getEndDateTime().equals(end))) {
        cnt++;
        if (event.getSeriesId() != null) {
          seriesEvent = event;
          foundSeries = true;
        } else {
          singleEvent = event;
        }
      }
    }
    if (cnt > 1) {
      throw new IllegalArgumentException(
          "There exists multiple events with the given subject and startTime");
    } else if (cnt == 0) {
      throw new IllegalArgumentException("Event with given subject and Time not found");
    } else {
      return foundSeries ? seriesEvent : singleEvent;
    }
  }

  /**
   * Edits all events in a series starting from the specified event and start time.
   * Throws an IllegalArgumentException if no such event exists or if the edit causes a duplicate.
   *
   * @param subject  the subject of the events to be edited
   * @param start    the start date and time of the event to be edited
   * @param property the property to be edited
   * @param value    the new value for the property
   * @throws IllegalArgumentException if no matching events are found or the edit causes duplicates
   */
  @Override
  public void editEventsFrom(String subject, LocalDateTime start, LocalDateTime end,
                             String property, Object value)
      throws IllegalArgumentException {
    Event targetEvent = findEventForSeries(subject, start, end);
    if (targetEvent.getSeriesId() == null) {
      editSingleEvent(targetEvent, property, value);
      return;
    }

    List<Event> eventsToEdit = getEventsInSeriesFrom(targetEvent.getSeriesId(), start);
    boolean needsNewSeriesId = property.equalsIgnoreCase("start")
        || property.equalsIgnoreCase("startdatetime") || property.equalsIgnoreCase("end")
        || property.equalsIgnoreCase("enddatetime");

    String newSeriesId = needsNewSeriesId ? UUID.randomUUID().toString()
        : targetEvent.getSeriesId();
    editMultipleEvents(eventsToEdit, property, value, newSeriesId);
  }

  /**
   * Edits all events in the series with the specified subject and start time.
   * Applies the changes to all events in the series.
   *
   * @param subject  the subject of the event series
   * @param start    the start date and time of the event series
   * @param property the property to be edited
   * @param value    the new value for the property
   * @throws IllegalArgumentException if no matching events are found or the edit causes duplicates
   */
  @Override
  public void editAllEventsInSeries(String subject, LocalDateTime start, LocalDateTime end,
                                    String property,
                                    Object value) throws IllegalArgumentException {
    Event targetEvent = findEventForSeries(subject, start, end);
    if (targetEvent.getSeriesId() == null) {
      editSingleEvent(targetEvent, property, value);
      return;
    }
    List<Event> eventsToEdit = getEventsInSeries(targetEvent.getSeriesId());
    editMultipleEvents(eventsToEdit, property, value, targetEvent.getSeriesId());
  }

  /**
   * Helper method to get all events in a series identified by seriesId.
   *
   * @param seriesId the ID of the event series
   * @return a list of events in the series
   */
  private List<Event> getEventsInSeries(String seriesId) {
    List<Event> result = new ArrayList<>();
    for (Event event : allEvents) {
      if (seriesId.equals(event.getSeriesId())) {
        result.add(event);
      }
    }
    return result;
  }

  /**
   * Helper method to get all events in a series from a specific start date/time.
   *
   * @param seriesId     the ID of the event series
   * @param fromDateTime the start date/time to filter events
   * @return a list of events starting from the specified date/time
   */
  private List<Event> getEventsInSeriesFrom(String seriesId, LocalDateTime fromDateTime) {
    List<Event> result = new ArrayList<>();
    for (Event event : allEvents) {
      if (seriesId.equals(event.getSeriesId())
          && !event.getStartDateTime().isBefore(fromDateTime)) {
        result.add(event);
      }
    }
    return result;
  }

  /**
   * Edits multiple events by removing them, updating the specified property, and re-adding them.
   *
   * @param eventsToEdit the events to modify
   * @param property     the property to change
   * @param value        the new value for the property
   * @param newSeriesId  the new seriesId to assign
   */
  private void editMultipleEvents(List<Event> eventsToEdit, String property,
                                  Object value, String newSeriesId) {
    for (Event event : eventsToEdit) {
      removeEvent(event);
    }

    for (Event event : eventsToEdit) {
      CalendarEvent.EventBuilder updatedEvent = updateEventProperty(event, property, value, true)
          .seriesId(newSeriesId);
      addEvent(updatedEvent.build());
    }
  }

  /**
   * Updates an event's property based on the provided property name and new value.
   *
   * @param event    the event to update
   * @param property the property to update
   * @param newValue the new value for the property
   * @return a builder with the updated event
   */
  private CalendarEvent.EventBuilder updateEventProperty(Event event, String property,
                       Object newValue, boolean isPartOfSeriesEdit) {
    CalendarEvent.EventBuilder builder = new CalendarEvent.EventBuilder()
        .subject(event.getSubject())
        .startDateTime(event.getStartDateTime())
        .endDateTime(event.getEndDateTime())
        .description(event.getDescription())
        .location(event.getLocation())
        .status(event.getStatus())
        .seriesId(event.getSeriesId());

    switch (property.toLowerCase()) {
      case "subject":
        builder.subject((String) newValue);
        break;
      case "start":
      case "startdatetime":
        updateStartDateTime(builder, event, (LocalDateTime) newValue, isPartOfSeriesEdit);
        break;
      case "end":
      case "enddatetime":
        updateEndDateTime(builder, event, (LocalDateTime) newValue, isPartOfSeriesEdit);
        break;
      case "description":
        builder.description((String) newValue);
        break;
      case "location":
        builder.location((String) newValue);
        break;
      case "status":
        builder.status(Status.valueOf(((String) newValue).toUpperCase()));
        break;
      default:
        throw new IllegalArgumentException("Unknown property: " + property);
    }
    return builder;
  }

  /**
   * Updates the start datetime of an event.
   *
   * @param builder the event builder to update
   * @param event the original event
   * @param newStart the new start datetime
   * @param isPartOfSeriesEdit if true, only time is changed; if false, full datetime is used
   */
  private void updateStartDateTime(CalendarEvent.EventBuilder builder, Event event,
                                   LocalDateTime newStart, boolean isPartOfSeriesEdit) {
    LocalDateTime actualStart;

    if (isPartOfSeriesEdit) {
      actualStart = event.getStartDateTime().toLocalDate().atTime(newStart.toLocalTime());
    } else {
      actualStart = newStart;
    }

    builder.startDateTime(actualStart);

    long durationMinutes = java.time.Duration.between(
        event.getStartDateTime(), event.getEndDateTime()).toMinutes();
    builder.endDateTime(actualStart.plusMinutes(durationMinutes));
  }

  /**
   * Updates the end datetime of an event.
   *
   * @param builder the event builder to update
   * @param event the original event
   * @param newEnd the new end datetime
   * @param isPartOfSeriesEdit if true, only time is changed; if false, full datetime is used
   */
  private void updateEndDateTime(CalendarEvent.EventBuilder builder, Event event,
                                 LocalDateTime newEnd, boolean isPartOfSeriesEdit) {
    LocalDateTime actualEnd;

    if (isPartOfSeriesEdit) {
      actualEnd = event.getEndDateTime().toLocalDate().atTime(newEnd.toLocalTime());
    } else {
      actualEnd = newEnd;
    }

    builder.endDateTime(actualEnd);
  }

  /**
   * Retrieves all events on a given date.
   *
   * @param date the date to query for events
   * @return a list of events occurring on the specified date
   */
  @Override
  public List<Event> getEventsOn(LocalDate date) {
    LocalDateTime dayStart = date.atStartOfDay();
    LocalDateTime dayEnd = date.atTime(23, 59, 59);
    List<Event> events = new ArrayList<>();
    for (Event event : allEvents) {
      if (!event.getStartDateTime().isAfter(dayEnd) && event.getEndDateTime().isAfter(dayStart)) {
        events.add(event);
      }
    }
    events.sort(Comparator.comparing(Event::getStartDateTime));
    return events;
  }

  /**
   * Retrieves all events within a specified range of date and time.
   *
   * @param start the start date and time of the range
   * @param end   the end date and time of the range
   * @return a list of events occurring within the specified range
   */
  @Override
  public List<Event> getEventsInRange(LocalDateTime start, LocalDateTime end) {
    List<Event> events = new ArrayList<>();
    for (Event event : allEvents) {
      if (event.getStartDateTime().isBefore(end) && event.getEndDateTime().isAfter(start)) {
        events.add(event);
      }
    }
    events.sort(Comparator.comparing(Event::getStartDateTime));
    return events;
  }

  /**
   * Computes aggregated analytics for all events that overlap the inclusive
   * date interval from {@code startDate} to {@code endDate}.
   *
   * <p>The method:
   * <ul>
   *   <li>Converts the date interval {@code [startDate, endDate]} into a
   *       date/time range {@code [startDate at 00:00, (endDate + 1) at 00:00)}.</li>
   *   <li>Retrieves all events that overlap this range using
   *       {@link #getEventsInRange(LocalDateTime, LocalDateTime)}.</li>
   *   <li>Counts events grouped by subject, weekday, week index within the interval,
   *       month, and per individual day.</li>
   *   <li>Determines the busiest and least busy days based on the number of
   *       events on each day (ties are broken by choosing the first day encountered).</li>
   *   <li>Classifies events as "online" or "offline" based on their location
   *       (location equals "online", ignoring case and surrounding whitespace).</li>
   *   <li>Computes the average number of events per day over the interval.</li>
   * </ul>
   *
   * <p>The resulting metrics are returned in an immutable
   * {@link CalendarAnalyticsSummary} instance, which is then used by the
   * dashboard feature in both text and GUI modes.
   *
   * @param startDate the first date (inclusive) in the analytics interval;
   *                  must not be {@code null}
   * @param endDate   the last date (inclusive) in the analytics interval;
   *                  must not be {@code null} and must not be before {@code startDate}
   * @return a {@link CalendarAnalyticsSummary} describing all events that overlap
   *         the specified date interval
   * @throws IllegalArgumentException if {@code startDate} or {@code endDate} is {@code null},
   *                                  or if {@code endDate} is before {@code startDate}
   */
  @Override
  public CalendarAnalyticsSummary generateAnalytics(LocalDate startDate, LocalDate endDate) {
    if (startDate == null || endDate == null) {
      throw new IllegalArgumentException("Start date and end date must not be null");
    }
    if (endDate.isBefore(startDate)) {
      throw new IllegalArgumentException("End date must not be before start date");
    }

    LocalDateTime startDateTime = startDate.atStartOfDay();
    LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

    List<Event> events = getEventsInRange(startDateTime, endDateTime);

    Map<String, Integer> bySubject = new HashMap<>();
    Map<DayOfWeek, Integer> byWeekday = new EnumMap<>(DayOfWeek.class);
    Map<Integer, Integer> byWeekIndex = new HashMap<>();
    Map<YearMonth, Integer> byMonth = new HashMap<>();
    Map<LocalDate, Integer> eventsPerDay = new HashMap<>();

    int onlineCount = 0;
    int offlineCount = 0;

    for (Event event : events) {
      LocalDate eventDate = event.getStartDateTime().toLocalDate();

      String subject = event.getSubject();
      if (subject == null || subject.trim().isEmpty()) {
        subject = "(no subject)";
      }
      bySubject.put(subject, bySubject.getOrDefault(subject, 0) + 1);

      DayOfWeek dayOfWeek = eventDate.getDayOfWeek();
      byWeekday.put(dayOfWeek, byWeekday.getOrDefault(dayOfWeek, 0) + 1);

      long weeksBetween = ChronoUnit.WEEKS.between(startDate, eventDate);
      int weekIndex = (int) weeksBetween + 1;
      byWeekIndex.put(weekIndex, byWeekIndex.getOrDefault(weekIndex, 0) + 1);

      YearMonth month = YearMonth.from(eventDate);
      byMonth.put(month, byMonth.getOrDefault(month, 0) + 1);

      eventsPerDay.put(eventDate, eventsPerDay.getOrDefault(eventDate, 0) + 1);

      String location = event.getLocation();
      if (location != null && location.trim().equalsIgnoreCase("online")) {
        onlineCount += 1;
      } else {
        offlineCount += 1;
      }
    }

    LocalDate busiestDay = null;
    LocalDate leastBusyDay = null;
    int maxCount = 0;
    int minCount = Integer.MAX_VALUE;

    for (Map.Entry<LocalDate, Integer> entry : eventsPerDay.entrySet()) {
      int count = entry.getValue();
      if (count > maxCount) {
        maxCount = count;
        busiestDay = entry.getKey();
      }
      if (count < minCount) {
        minCount = count;
        leastBusyDay = entry.getKey();
      }
    }

    long daysInInterval = ChronoUnit.DAYS.between(startDate, endDate) + 1;
    double averagePerDay = daysInInterval > 0
        ? (double) events.size() / (double) daysInInterval
        : 0.0;

    return CalendarAnalyticsSummary.builder()
        .totalEvents(events.size())
        .eventsBySubject(bySubject)
        .eventsByWeekday(byWeekday)
        .eventsByWeekIndex(byWeekIndex)
        .eventsByMonth(byMonth)
        .averageEventsPerDay(averagePerDay)
        .busiestDay(busiestDay)
        .leastBusyDay(leastBusyDay)
        .onlineEventsCount(onlineCount)
        .offlineEventsCount(offlineCount)
        .build();
  }

  /**
   * Checks if the calendar is busy at a specific date and time.
   *
   * @param dateTime the date and time to check
   * @return true if there is an event at the specified date/time, false otherwise
   */
  @Override
  public boolean isBusy(LocalDateTime dateTime) {
    for (Event event : allEvents) {
      if (!dateTime.isBefore(event.getStartDateTime())
          && dateTime.isBefore(event.getEndDateTime())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Exports all events to a CSV file.
   *
   * @param fileName the name of the CSV file to be created
   * @return the absolute path to the exported CSV file
   * @throws IOException if there is an error writing the file
   */
  @Override
  public String exportToCsv(String fileName) throws IOException {
    List<Event> eventList = new ArrayList<>(allEvents);
    return CsvExporter.exportToCsv(eventList, fileName);
  }

  /**
   * Exports all events to an iCal file.
   *
   * @param fileName the name of the iCal file to be created
   * @param timezone the timezone to use for the export
   * @return the absolute path to the exported iCal file
   * @throws IOException if there is an error writing the file
   */
  @Override
  public String exportToIcal(String fileName, ZoneId timezone) throws IOException {
    List<Event> eventList = new ArrayList<>(allEvents);
    return IcalExporter.exportToIcal(eventList, timezone, fileName);
  }
}