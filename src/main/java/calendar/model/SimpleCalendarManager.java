package calendar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of CalendarManager that manages multiple calendars.
 * Maintains a collection of calendars and tracks the currently active calendar.
 */
public class SimpleCalendarManager implements CalendarManager {
  private final Map<String, Calendar> calendars;
  private Calendar currentCalendar;
  private final CalendarFactory calendarFactory;

  /**
   * Creates a new SimpleCalendarManager with default calendar factory.
   * This constructor maintains backward compatibility with existing code.
   */
  public SimpleCalendarManager() {
    this(new SimpleCalendarFactory());
  }

  /**
   * Creates a new SimpleCalendarManager with custom calendar factory.
   * Allows dependency injection for testing and flexibility.
   * Follows Dependency Inversion Principle - depends on CalendarFactory abstraction,
   * not concrete Calendar class.
   *
   * @param factory the factory to use for creating calendars
   */
  public SimpleCalendarManager(CalendarFactory factory) {
    this.calendars = new HashMap<>();
    this.currentCalendar = null;
    this.calendarFactory = factory;
  }

  @Override
  public void createCalendar(String name, ZoneId timezone) throws IllegalArgumentException {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be null or empty");
    }
    if (timezone == null) {
      throw new IllegalArgumentException("Timezone cannot be null");
    }
    if (calendars.containsKey(name)) {
      throw new IllegalArgumentException("Calendar with name '" + name + "' already exists");
    }

    Calendar calendar = calendarFactory.createCalendar(name, timezone);
    calendars.put(name, calendar);
  }

  @Override
  public void editCalendar(String calendarName, String property, Object value)
      throws IllegalArgumentException {
    Calendar calendar = getCalendar(calendarName);

    if (property == null || property.trim().isEmpty()) {
      throw new IllegalArgumentException("Property cannot be null or empty");
    }

    String prop = property.toLowerCase();
    switch (prop) {
      case "name":
        if (!(value instanceof String)) {
          throw new IllegalArgumentException("Name must be a string");
        }
        String newName = (String) value;
        if (calendars.containsKey(newName) && !newName.equals(calendarName)) {
          throw new IllegalArgumentException("Calendar with name '" + newName
              + "' already exists");
        }
        calendars.remove(calendarName);
        calendar.setName(newName);
        calendars.put(newName, calendar);
        break;

      case "timezone":
        ZoneId newTimezone;
        if (value instanceof String) {
          try {
            newTimezone = ZoneId.of((String) value);
          } catch (Exception e) {
            throw new IllegalArgumentException("Invalid timezone: " + value);
          }
        } else if (value instanceof ZoneId) {
          newTimezone = (ZoneId) value;
        } else {
          throw new IllegalArgumentException("Timezone must be a string or ZoneId");
        }
        convertCalendarEventTimezones(calendar, newTimezone);
        calendar.setTimezone(newTimezone);
        break;

      default:
        throw new IllegalArgumentException("Unknown property: " + property
            + ". Valid properties are: name, timezone");
    }
  }

  /**
   * Converts all event times in a calendar from its current timezone to a new timezone.
   * This ensures that events maintain their "wall clock" time in the new timezone.
   *
   * @param calendar    the calendar whose events need timezone conversion
   * @param newTimezone the new timezone to convert to
   */
  private void convertCalendarEventTimezones(Calendar calendar, ZoneId newTimezone) {
    ZoneId oldTimezone = calendar.getTimezone();

    if (oldTimezone.equals(newTimezone)) {
      return;
    }

    List<Event> allEvents = calendar.getEventsInRange(
        LocalDateTime.of(1900, 1, 1, 0, 0),
        LocalDateTime.of(2100, 12, 31, 23, 59));

    for (Event event : allEvents) {
      calendar.removeEvent(event);
    }

    for (Event event : allEvents) {
      LocalDateTime convertedStart = calendar.convertToTimezone(
          event.getStartDateTime(), newTimezone);
      LocalDateTime convertedEnd = calendar.convertToTimezone(
          event.getEndDateTime(), newTimezone);

      createUpdatedEventHelper(calendar, event, convertedStart, convertedEnd);
    }
  }

  /**
   * Helper to create updated event using event builder.
   *
   * @param calendar       - target calendar in copy.
   * @param event          - event to copy.
   * @param convertedStart - updated start date time for the event.
   * @param convertedEnd   - updated end dte time for the event.
   */
  private void createUpdatedEventHelper(Calendar calendar, Event event,
                                        LocalDateTime convertedStart, LocalDateTime convertedEnd) {
    CalendarEvent.EventBuilder builder = new CalendarEvent.EventBuilder()
        .subject(event.getSubject())
        .startDateTime(convertedStart)
        .endDateTime(convertedEnd)
        .description(event.getDescription())
        .location(event.getLocation())
        .status(event.getStatus());

    if (event.getSeriesId() != null) {
      builder.seriesId(event.getSeriesId());
    }

    calendar.addEvent(builder.build());
  }

  @Override
  public void useCalendar(String calendarName) throws IllegalArgumentException {
    currentCalendar = getCalendar(calendarName);
  }

  @Override
  public Calendar getCurrentCalendar() {
    return currentCalendar;
  }

  @Override
  public Calendar getCalendar(String name) throws IllegalArgumentException {
    if (!calendars.containsKey(name)) {
      throw new IllegalArgumentException("Calendar '" + name + "' does not exist");
    }
    return calendars.get(name);
  }

  @Override
  public void copyEvent(String eventSubject, LocalDateTime eventStartDateTime,
                        String targetCalendarName, LocalDateTime targetStartDateTime)
      throws IllegalArgumentException {
    if (currentCalendar == null) {
      throw new IllegalArgumentException("No calendar is currently in use");
    }

    Calendar targetCalendar = getCalendar(targetCalendarName);

    Event eventToCopy = findEvent(currentCalendar, eventSubject, eventStartDateTime);

    long duration = ChronoUnit.MINUTES.between(
        eventToCopy.getStartDateTime(),
        eventToCopy.getEndDateTime());

    LocalDateTime adjustedTargetStart = targetStartDateTime;
    if (!currentCalendar.getTimezone().equals(targetCalendar.getTimezone())) {
      adjustedTargetStart = currentCalendar.convertToTimezone(
          targetStartDateTime,
          targetCalendar.getTimezone());
    }

    LocalDateTime adjustedTargetEnd = adjustedTargetStart.plusMinutes(duration);

    createUpdatedEventHelper(targetCalendar, eventToCopy, adjustedTargetStart, adjustedTargetEnd);
  }

  @Override
  public void copyEventsOnDate(LocalDate sourceDate, String targetCalendarName,
                               LocalDate targetDate) throws IllegalArgumentException {
    if (currentCalendar == null) {
      throw new IllegalArgumentException("No calendar is currently in use");
    }

    Calendar targetCalendar = getCalendar(targetCalendarName);
    List<Event> eventsToCopy = currentCalendar.getEventsOn(sourceDate);

    copyEventsHelper(sourceDate, targetDate, targetCalendar, eventsToCopy);
  }

  /**
   * Helper method to copy multiple events with timezone conversion.
   *
   * @param sourceDate the source date
   * @param targetDate the target date
   * @param targetCalendar the target calendar
   * @param eventsToCopy the events to copy
   */
  private void copyEventsHelper(LocalDate sourceDate, LocalDate targetDate,
                                Calendar targetCalendar, List<Event> eventsToCopy) {
    long daysDifference = ChronoUnit.DAYS.between(sourceDate, targetDate);

    for (Event event : eventsToCopy) {
      LocalDateTime newStart = event.getStartDateTime().plusDays(daysDifference);
      LocalDateTime newEnd = event.getEndDateTime().plusDays(daysDifference);

      if (!currentCalendar.getTimezone().equals(targetCalendar.getTimezone())) {
        newStart = currentCalendar.convertToTimezone(newStart, targetCalendar.getTimezone());
        newEnd = currentCalendar.convertToTimezone(newEnd, targetCalendar.getTimezone());
      }

      CalendarEvent.EventBuilder builder = new CalendarEvent.EventBuilder()
          .subject(event.getSubject())
          .startDateTime(newStart)
          .endDateTime(newEnd)
          .description(event.getDescription())
          .location(event.getLocation())
          .status(event.getStatus());

      if (event.getSeriesId() != null) {
        builder.seriesId(event.getSeriesId());
      }

      try {
        targetCalendar.addEvent(builder.build());
      } catch (IllegalArgumentException ignored) {
        // Ignore
      }
    }
  }

  @Override
  public void copyEventsBetween(LocalDate startDate, LocalDate endDate,
                                String targetCalendarName, LocalDate targetStartDate)
      throws IllegalArgumentException {
    if (currentCalendar == null) {
      throw new IllegalArgumentException("No calendar is currently in use");
    }

    Calendar targetCalendar = getCalendar(targetCalendarName);

    LocalDateTime rangeStart = startDate.atStartOfDay();
    LocalDateTime rangeEnd = endDate.atTime(23, 59, 59);

    List<Event> eventsToCopy = currentCalendar.getEventsInRange(rangeStart, rangeEnd);

    copyEventsHelper(startDate, targetStartDate, targetCalendar, eventsToCopy);
  }

  @Override
  public boolean hasCalendar(String name) {
    return calendars.containsKey(name);
  }

  /**
   * Helper method to find an event in a calendar by subject and start time.
   *
   * @param calendar      the calendar to search
   * @param subject       the event subject
   * @param startDateTime the event start time
   * @return the found event
   * @throws IllegalArgumentException if event not found or multiple events match
   */
  private Event findEvent(Calendar calendar, String subject, LocalDateTime startDateTime)
      throws IllegalArgumentException {
    List<Event> allEvents = calendar.getEventsInRange(
        startDateTime.minusYears(100),
        startDateTime.plusYears(100));

    Event found = null;
    int count = 0;

    for (Event event : allEvents) {
      if (event.getSubject().equals(subject)
          && event.getStartDateTime().equals(startDateTime)) {
        found = event;
        count++;
      }
    }

    if (count == 0) {
      throw new IllegalArgumentException("Event not found: " + subject
          + " at " + startDateTime);
    }
    if (count > 1) {
      throw new IllegalArgumentException("Multiple events found with same subject and start time");
    }

    return found;
  }

  @Override
  public List<String> getAllCalendarNames() {
    return new ArrayList<>(calendars.keySet());
  }

  @Override
  public void addEvent(Event event) throws IllegalStateException, IllegalArgumentException {
    if (currentCalendar == null) {
      throw new IllegalStateException("No calendar is currently in use");
    }
    currentCalendar.addEvent(event);
  }

  @Override
  public void addEventSeries(EventSeries eventSeries)
      throws IllegalStateException, IllegalArgumentException {
    if (currentCalendar == null) {
      throw new IllegalStateException("No calendar is currently in use");
    }
    currentCalendar.addEventSeries(eventSeries);
  }

  @Override
  public List<Event> getEventsOn(LocalDate date) throws IllegalStateException {
    if (currentCalendar == null) {
      throw new IllegalStateException("No calendar is currently in use");
    }
    return currentCalendar.getEventsOn(date);
  }

  @Override
  public List<Event> getEventsInRange(LocalDateTime start, LocalDateTime end)
      throws IllegalStateException {
    if (currentCalendar == null) {
      throw new IllegalStateException("No calendar is currently in use");
    }
    return currentCalendar.getEventsInRange(start, end);
  }

  @Override
  public void removeEvent(Event event) throws IllegalStateException {
    if (currentCalendar == null) {
      throw new IllegalStateException("No calendar is currently in use");
    }
    currentCalendar.removeEvent(event);
  }

  @Override
  public void removeEventFromSeries(Event event) throws IllegalStateException {
    if (currentCalendar == null) {
      throw new IllegalStateException("No calendar is currently in use");
    }
    currentCalendar.removeEventFromSeries(event);
  }

  @Override
  public void removeAllEventsInSeries(Event event) throws IllegalStateException {
    if (currentCalendar == null) {
      throw new IllegalStateException("No calendar is currently in use");
    }
    currentCalendar.removeAllEventsInSeries(event);
  }

  @Override
  public void editEvent(String subject, LocalDateTime start, LocalDateTime end,
                        String property, Object value)
      throws IllegalStateException, IllegalArgumentException {
    if (currentCalendar == null) {
      throw new IllegalStateException("No calendar is currently in use");
    }
    currentCalendar.editEvent(subject, start, end, property, value);
  }

  @Override
  public void editEventsFrom(String subject, LocalDateTime start, LocalDateTime end,
                             String property, Object value)
      throws IllegalStateException, IllegalArgumentException {
    if (currentCalendar == null) {
      throw new IllegalStateException("No calendar is currently in use");
    }
    currentCalendar.editEventsFrom(subject, start, end, property, value);
  }

  @Override
  public void editAllEventsInSeries(String subject, LocalDateTime start, LocalDateTime end,
                                    String property, Object value)
      throws IllegalStateException, IllegalArgumentException {
    if (currentCalendar == null) {
      throw new IllegalStateException("No calendar is currently in use");
    }
    currentCalendar.editAllEventsInSeries(subject, start, end, property, value);
  }
}