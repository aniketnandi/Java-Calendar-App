package calendar.controller;

import calendar.model.AbstractEventBuilder;
import calendar.model.CalendarAnalyticsSummary;
import calendar.model.CalendarEvent;
import calendar.model.CalendarManager;
import calendar.model.CalendarModel;
import calendar.model.CalendarModelAdapter;
import calendar.model.Event;
import calendar.model.RecurringEventSeries;
import calendar.view.CalendarEditData;
import calendar.view.CalendarGuiView;
import calendar.view.EventDeleteData;
import calendar.view.EventEditData;
import calendar.view.EventFormData;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for GUI mode of the calendar application.
 * Implements the Features interface to handle high-level user interactions.
 * Follows MVC pattern by mediating between the model (CalendarManager) and view (CalendarGuiView).
 */
public class GuiController implements Features {
  private final CalendarManager manager;
  private final CalendarGuiView view;
  private final Map<String, EditOperation> editOperations;
  private final Map<String, DeleteOperation> deleteOperations;
  private final Map<String, String> deletionMessages;

  /**
   * Creates a GUI controller with the specified manager and view.
   *
   * @param manager the calendar manager
   * @param view    the GUI view
   */
  public GuiController(CalendarManager manager, CalendarGuiView view) {
    this.manager = manager;
    this.view = view;
    this.editOperations = createEditOperationsMap();
    this.deleteOperations = createDeleteOperationsMap();
    this.deletionMessages = createDeletionMessagesMap();
    view.addFeaturesListener(this);
  }

  /**
   * Creates the map of edit scope strings to edit operations.
   * Converts control flow (switch statement) into data.
   *
   * @return map of edit scopes to their corresponding operations
   */
  private Map<String, EditOperation> createEditOperationsMap() {
    Map<String, EditOperation> map = new HashMap<>();
    map.put("single", this::applySingleEdit);
    map.put("from", this::applyEditsFrom);
    map.put("all", this::applyEditsToAll);
    return map;
  }

  /**
   * Creates the map of delete scope strings to delete operations.
   * Converts control flow (switch statement) into data.
   *
   * @return map of delete scopes to their corresponding operations
   */
  private Map<String, DeleteOperation> createDeleteOperationsMap() {
    Map<String, DeleteOperation> map = new HashMap<>();
    map.put("THIS", manager::removeEvent);
    map.put("THIS_AND_FUTURE", manager::removeEventFromSeries);
    map.put("ALL", manager::removeAllEventsInSeries);
    return map;
  }

  /**
   * Creates the map of delete scope strings to success messages.
   * Converts control flow (switch statement) into data.
   *
   * @return map of delete scopes to their success messages
   */
  private Map<String, String> createDeletionMessagesMap() {
    Map<String, String> map = new HashMap<>();
    map.put("THIS", "Event deleted successfully!");
    map.put("THIS_AND_FUTURE", "Event and future occurrences deleted successfully!");
    map.put("ALL", "All events in series deleted successfully!");
    return map;
  }

  /**
   * Starts the GUI application.
   * Initializes with a default calendar and displays the view.
   */
  public void start() {
    initializeDefaultCalendar();
    view.display();
    dateSelected(LocalDate.now());
    monthChanged(YearMonth.now().getYear(), YearMonth.now().getMonthValue());
  }

  /**
   * Ensures a default calendar exists for immediate use.
   */
  private void initializeDefaultCalendar() {
    try {
      if (!manager.hasCalendar("Default")) {
        manager.createCalendar("Default", ZoneId.systemDefault());
      }
      manager.useCalendar("Default");
      view.setCurrentCalendar("Default");
    } catch (Exception e) {
      view.displayError("Error initializing default calendar: " + e.getMessage());
    }
  }

  @Override
  public void createCalendar() {
    String name = view.promptCalendarName();
    if (name == null || name.trim().isEmpty()) {
      return;
    }

    String timezone = view.promptTimezone();
    if (timezone == null) {
      return;
    }

    try {
      manager.createCalendar(name.trim(), ZoneId.of(timezone));
      view.displayMessage("Calendar '" + name + "' created successfully!");
    } catch (Exception e) {
      view.displayError("Error creating calendar: " + e.getMessage());
    }
  }

  @Override
  public void switchCalendar() {
    List<String> calendarNames = manager.getAllCalendarNames();

    if (calendarNames.isEmpty()) {
      view.displayError("No calendars available. Please create a calendar first.");
      return;
    }

    String selected = view.promptSelectCalendar(calendarNames);
    if (selected == null) {
      return;
    }

    try {
      manager.useCalendar(selected);
      view.setCurrentCalendar(selected);
      refreshCurrentView();
      view.displayMessage("Switched to calendar: " + selected);
    } catch (Exception e) {
      view.displayError("Error switching calendar: " + e.getMessage());
    }
  }

  @Override
  public void editCalendar() {
    if (manager.getCurrentCalendar() == null) {
      view.displayError("No calendar selected.");
      return;
    }

    CalendarEditData editData = view.showCalendarEditDialog(
        manager.getCurrentCalendar().getName(),
        manager.getCurrentCalendar().getTimezone().getId());

    if (editData == null) {
      return;
    }

    try {
      String currentName = manager.getCurrentCalendar().getName();

      manager.editCalendar(currentName, editData.getProperty(), editData.getNewValue());

      if (editData.getProperty().equalsIgnoreCase("name")) {
        manager.useCalendar(editData.getNewValue());
        view.setCurrentCalendar(editData.getNewValue());
      }

      view.displayMessage("Calendar updated successfully!");
    } catch (Exception e) {
      view.displayError("Error editing calendar: " + e.getMessage());
    }
  }

  @Override
  public void createEvent(LocalDate date) {
    if (isCalendarUnselected()) {
      return;
    }

    EventFormData formData = view.showEventCreationDialog(date);
    if (formData == null) {
      return;
    }

    String validationError = validateEventFormData(formData);
    if (validationError != null) {
      view.displayError(validationError);
      return;
    }

    try {
      if (formData.isRecurring()) {
        createRecurringEvent(formData);
      } else {
        createSingleEvent(formData);
      }

      view.displayMessage("Event created successfully!");
      refreshDateView(date);
    } catch (Exception e) {
      view.displayError("Error creating event: " + e.getMessage());
    }
  }

  /**
   * Shows the analytics dashboard for the given inclusive date range in the GUI.
   *
   * <p>This method:
   * <ol>
   *   <li>validates that both dates are non-null and that {@code end} is not
   *       before {@code start},</li>
   *   <li>creates a {@link CalendarModelAdapter} for the current manager,</li>
   *   <li>invokes {@link CalendarModel#generateAnalytics(LocalDate, LocalDate)} to
   *       compute metrics,</li>
   *   <li>and asks the {@link CalendarGuiView} to display the resulting dashboard.</li>
   * </ol>
   * Any validation or runtime errors are reported through the view.
   *
   * @param start the start date of the interval (inclusive); must not be {@code null}
   * @param end   the end date of the interval (inclusive); must not be {@code null}
   */
  @Override
  public void showDashboard(LocalDate start, LocalDate end) {
    if (start == null || end == null) {
      view.displayError("Start date and end date must not be null.");
      return;
    }
    if (end.isBefore(start)) {
      view.displayError("End date must not be before start date.");
      return;
    }

    try {
      CalendarModel model = new CalendarModelAdapter(manager);
      CalendarAnalyticsSummary summary = model.generateAnalytics(start, end);
      view.displayDashboard(summary, start, end);
    } catch (IllegalArgumentException e) {
      view.displayError(e.getMessage());
    } catch (Exception e) {
      view.displayError("Error generating dashboard: " + e.getMessage());
    }
  }

  /**
   * Creates a single (non-recurring) event.
   */
  private void createSingleEvent(EventFormData data) {
    LocalDateTime startDateTime = calculateStartDateTime(data);
    LocalDateTime endDateTime = calculateEndDateTime(data);

    CalendarEvent.EventBuilder builder = new CalendarEvent.EventBuilder()
        .subject(data.getSubject())
        .startDateTime(startDateTime)
        .endDateTime(endDateTime);

    addOptionalFields(builder, data);

    Event event = builder.build();
    manager.addEvent(event);
  }

  /**
   * Creates a recurring event series.
   */
  private void createRecurringEvent(EventFormData data) {
    LocalDateTime startDateTime = calculateStartDateTime(data);
    LocalDateTime endDateTime = calculateEndDateTime(data);

    RecurringEventSeries.EventSeriesBuilder builder =
        new RecurringEventSeries.EventSeriesBuilder()
            .subject(data.getSubject())
            .startDateTime(startDateTime)
            .endDateTime(endDateTime)
            .weekdays(data.getWeekdays());

    addOptionalFields(builder, data);

    if (data.isUseCount()) {
      builder.repeatCount(data.getRepeatCount());
    } else {
      builder.repeatUntil(data.getRepeatUntil());
    }

    RecurringEventSeries series = builder.build();
    manager.addEventSeries(series);
  }

  /**
   * Calculates start date/time from form data.
   */
  private LocalDateTime calculateStartDateTime(EventFormData data) {
    if (data.isAllDay()) {
      return data.getStartDate().atTime(8, 0);
    } else {
      return LocalDateTime.of(data.getStartDate(), data.getStartTime());
    }
  }

  /**
   * Calculates end date/time from form data.
   */
  private LocalDateTime calculateEndDateTime(EventFormData data) {
    if (data.isAllDay()) {
      return data.getEndDate().atTime(17, 0);
    } else {
      return LocalDateTime.of(data.getEndDate(), data.getEndTime());
    }
  }

  /**
   * Adds optional fields (location, description) to any event builder.
   *
   * @param builder the builder to add fields to
   * @param data    the form data containing optional fields
   * @param <T>     the type of builder that extends AbstractEventBuilder
   */
  private <T extends AbstractEventBuilder<T>> void addOptionalFields(
      T builder, EventFormData data) {
    if (data.getLocation() != null && !data.getLocation().trim().isEmpty()) {
      builder.location(data.getLocation());
    }
    if (data.getDescription() != null && !data.getDescription().trim().isEmpty()) {
      builder.description(data.getDescription());
    }
  }

  @Override
  public void editEvents(LocalDate date) {
    if (isCalendarUnselected()) {
      return;
    }

    List<Event> events = manager.getEventsOn(date);
    if (events.isEmpty()) {
      view.displayMessage("No events on this date to edit.");
      return;
    }

    EventEditData editData = view.showEventEditDialog(date, events);
    if (editData == null) {
      return;
    }

    String validationError = validateEventEditData(editData);
    if (validationError != null) {
      view.displayError(validationError);
      return;
    }

    try {
      Event eventToEdit = editData.getOriginalEvent();

      applyEdits(eventToEdit, editData);

      view.displayMessage("Event(s) updated successfully!");
      refreshDateView(date);
    } catch (Exception e) {
      view.displayError("Error editing event: " + e.getMessage());
    }
  }

  /**
   * Applies edits based on the edit scope (single, from, or all).
   * Uses a map-based approach to convert control flow into data,
   * following the pattern taught in lecture for eliminating switch statements.
   */
  private void applyEdits(Event event, EventEditData editData) {
    EditOperation operation = editOperations.get(editData.getEditScope());
    if (operation == null) {
      throw new IllegalArgumentException("Invalid edit scope: " + editData.getEditScope());
    }
    operation.apply(event, editData);
  }

  /**
   * Applies edits to a single event.
   */
  private void applySingleEdit(Event event, EventEditData editData) {
    Event currentEvent = event;

    applyPropertyIfChanged(currentEvent, "location",
        editData.getNewLocation(),
        event.getLocation());

    applyPropertyIfChanged(currentEvent, "description",
        editData.getNewDescription(),
        event.getDescription());

    applyPropertyIfChanged(currentEvent, "status",
        editData.getNewStatus(), event.getStatus().toString());

    currentEvent = applyPropertyIfChanged(currentEvent, "subject",
        editData.getNewSubject(), event.getSubject());

    currentEvent = applyPropertyIfChanged(currentEvent, "startdatetime",
        editData.getNewStartDateTime(), event.getStartDateTime());

    applyPropertyIfChanged(currentEvent, "enddatetime",
        editData.getNewEndDateTime(), event.getEndDateTime());
  }

  /**
   * Applies edits to this event and all future events in the series.
   */
  private void applyEditsFrom(Event event, EventEditData editData) {
    applyPropertyIfChangedToSeries(event, editData, true);
  }

  /**
   * Applies edits to all events in the series.
   */
  private void applyEditsToAll(Event event, EventEditData editData) {
    applyPropertyIfChangedToSeries(event, editData, false);
  }

  /**
   * Helper to apply property changes to series events.
   */
  private void applyPropertyIfChangedToSeries(Event event,
                                              EventEditData editData, boolean fromOnly) {
    String currentSubject = event.getSubject();
    LocalDateTime currentStart = event.getStartDateTime();
    LocalDateTime currentEnd = event.getEndDateTime();

    String originalLocation = event.getLocation();
    if (!editData.getNewLocation().equals(originalLocation)) {
      editSeriesProperty(currentSubject, currentStart, currentEnd,
          "location", editData.getNewLocation(), fromOnly);
    }

    String originalDescription = event.getDescription();
    if (!editData.getNewDescription().equals(originalDescription)) {
      editSeriesProperty(currentSubject, currentStart, currentEnd,
          "description", editData.getNewDescription(), fromOnly);
    }

    if (!editData.getNewStatus().equals(event.getStatus().toString())) {
      editSeriesProperty(currentSubject, currentStart, currentEnd,
          "status", editData.getNewStatus(), fromOnly);
    }

    if (!editData.getNewSubject().equals(event.getSubject())) {
      editSeriesProperty(currentSubject, currentStart, currentEnd,
          "subject", editData.getNewSubject(), fromOnly);
      currentSubject = editData.getNewSubject();
    }

    if (!editData.getNewStartDateTime().equals(event.getStartDateTime())) {
      editSeriesProperty(currentSubject, currentStart, currentEnd,
          "startdatetime", editData.getNewStartDateTime(), fromOnly);

      long durationMinutes = java.time.Duration.between(currentStart, currentEnd).toMinutes();
      currentStart = editData.getNewStartDateTime();
      currentEnd = currentStart.plusMinutes(durationMinutes);
    }

    if (!editData.getNewEndDateTime().equals(event.getEndDateTime())) {
      editSeriesProperty(currentSubject, currentStart, currentEnd,
          "enddatetime", editData.getNewEndDateTime(), fromOnly);
    }
  }

  /**
   * Helper method to edit a series property while handling potential ambiguity.
   * If multiple series have the same subject and start time, uses end time to disambiguate.
   */

  private void editSeriesProperty(String subject,
                                  LocalDateTime start, LocalDateTime end,
                                  String property, Object value, boolean fromOnly) {
    if (fromOnly) {
      manager.editEventsFrom(subject, start, end, property, value);
    } else {
      manager.editAllEventsInSeries(subject, start, end, property, value);
    }
  }

  /**
   * Applies a property change only if the value differs.
   * Returns the updated event (with potentially new subject/time).
   */
  private Event applyPropertyIfChanged(Event event,
                                       String property, Object newValue, Object oldValue) {
    LocalDateTime currentStart = event.getStartDateTime();
    LocalDateTime currentEnd = event.getEndDateTime();

    if (!newValue.equals(oldValue)) {
      manager.editEvent(event.getSubject(), event.getStartDateTime(),
          event.getEndDateTime(), property, newValue);

      if (property.equals("subject")) {
        return findEventBySubjectAndTime((String) newValue,
            event.getStartDateTime(), event.getEndDateTime());
      } else if (property.equals("startdatetime")) {
        long durationMinutes = java.time.Duration.between(currentStart, currentEnd).toMinutes();

        LocalDateTime newEnd = ((LocalDateTime) newValue).plusMinutes(durationMinutes);
        return findEventBySubjectAndTime(event.getSubject(), (LocalDateTime) newValue, newEnd);
      }
    }
    return event;
  }

  /**
   * Finds an event by subject and start time.
   */
  private Event findEventBySubjectAndTime(String subject, LocalDateTime startTime,
                                          LocalDateTime endTime) {
    List<Event> events = manager.getEventsInRange(
        startTime.minusMinutes(1), startTime.plusMinutes(1));

    for (Event e : events) {
      if (e.getSubject().equals(subject) && e.getStartDateTime().equals(startTime)
          && e.getEndDateTime().equals(endTime)) {
        return e;
      }
    }
    return null;
  }

  @Override
  public void deleteEvent(LocalDate date) {
    if (isCalendarUnselected()) {
      return;
    }

    List<Event> events = manager.getEventsOn(date);
    if (events.isEmpty()) {
      view.displayMessage("No events on this date to delete.");
      return;
    }

    EventDeleteData deleteData = view.showEventDeleteDialog(date, events);
    if (deleteData == null) {
      return;
    }

    try {
      Event eventToDelete = deleteData.getEvent();
      if (eventToDelete == null) {
        view.displayError("Could not find the selected event.");
        return;
      }

      performDeletion(eventToDelete, deleteData.getScope());

      view.displayMessage(getDeletionSuccessMessage(deleteData.getScope()));
      refreshDateView(date);
    } catch (Exception e) {
      view.displayError("Error deleting event: " + e.getMessage());
    }
  }

  /**
   * Performs the deletion based on scope.
   * Uses a map-based approach to convert control flow into data,
   * following the pattern taught in lecture for eliminating switch statements.
   */
  private void performDeletion(Event event, String scope) {
    DeleteOperation operation = deleteOperations.get(scope);
    if (operation == null) {
      throw new IllegalArgumentException("Invalid deletion scope: " + scope);
    }
    operation.apply(event);
  }

  /**
   * Returns appropriate success message for deletion scope.
   * Uses a map-based approach to convert control flow into data,
   * following the pattern taught in lecture for eliminating switch statements.
   */
  private String getDeletionSuccessMessage(String scope) {
    String message = deletionMessages.get(scope);
    if (message == null) {
      return "Event deleted successfully!";
    }
    return message;
  }

  @Override
  public void dateSelected(LocalDate date) {
    if (manager.getCurrentCalendar() == null) {
      return;
    }

    try {
      List<Event> events = manager.getEventsOn(date);
      view.setEventsForDate(date, events);
    } catch (Exception e) {
      view.displayError("Error loading events: " + e.getMessage());
    }
  }

  @Override
  public void monthChanged(int year, int month) {
    if (manager.getCurrentCalendar() == null) {
      return;
    }

    try {
      YearMonth yearMonth = YearMonth.of(year, month);
      LocalDate start = yearMonth.atDay(1);
      LocalDate end = yearMonth.atEndOfMonth();

      Map<LocalDate, List<Event>> eventsByDate = new HashMap<>();

      for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
        List<Event> events = manager.getEventsOn(date);
        if (!events.isEmpty()) {
          eventsByDate.put(date, events);
        }
      }

      view.setMonthEvents(eventsByDate);
    } catch (Exception e) {
      view.displayError("Error loading month events: " + e.getMessage());
    }
  }

  /**
   * Ensures a calendar is selected, shows error if not.
   *
   * @return true if calendar is selected, false otherwise
   */
  private boolean isCalendarUnselected() {
    if (manager.getCurrentCalendar() == null) {
      view.displayError("No calendar selected. Please create or select a calendar first.");
      return true;
    }
    return false;
  }

  /**
   * Refreshes the current view after changes.
   */
  private void refreshCurrentView() {
    LocalDate currentDate = LocalDate.now();
    dateSelected(currentDate);
    monthChanged(YearMonth.now().getYear(), YearMonth.now().getMonthValue());
  }

  /**
   * Refreshes the view for a specific date and its month.
   */
  private void refreshDateView(LocalDate date) {
    dateSelected(date);
    monthChanged(date.getYear(), date.getMonthValue());
  }

  /**
   * Validates event form data before creating event.
   *
   * @param data the form data to validate
   * @return error message if invalid, null if valid
   */
  private String validateEventFormData(EventFormData data) {
    String error;

    error = validateBasicEventData(data);
    if (error != null) {
      return error;
    }

    error = validateEventTiming(data);
    if (error != null) {
      return error;
    }

    if (data.isRecurring()) {
      error = validateRecurrenceData(data);
      if (error != null) {
        return error;
      }
    }

    return null;
  }

  /**
   * Validates basic event data (subject, dates).
   */
  private String validateBasicEventData(EventFormData data) {
    if (data.getSubject() == null || data.getSubject().trim().isEmpty()) {
      return "Subject cannot be empty";
    }

    if (data.getEndDate().isBefore(data.getStartDate())) {
      return "End date cannot be before start date";
    }

    return null;
  }

  /**
   * Validates event timing for non-all-day events.
   */
  private String validateEventTiming(EventFormData data) {
    if (!data.isAllDay() && data.getStartTime() != null && data.getEndTime() != null) {
      if (data.getStartDate().equals(data.getEndDate())
          && !data.getEndTime().isAfter(data.getStartTime())) {
        return "End time must be after start time for same-day events";
      }
    }
    return null;
  }

  /**
   * Validates recurring event data (weekdays, repeat settings).
   */
  private String validateRecurrenceData(EventFormData data) {
    if (data.getWeekdays() == null || data.getWeekdays().isEmpty()) {
      return "Please select at least one weekday for recurring events";
    }

    if (data.isUseCount() && data.getRepeatCount() <= 0) {
      return "Repeat count must be positive";
    }

    if (!data.isUseCount() && data.getRepeatUntil() != null
        && !data.getRepeatUntil().isAfter(data.getStartDate())) {
      return "Repeat until date must be after start date";
    }

    return null;
  }

  /**
   * Validates event edit data before applying edits.
   *
   * @param data the edit data to validate
   * @return error message if invalid, null if valid
   */
  private String validateEventEditData(EventEditData data) {
    if (data.getNewSubject() == null || data.getNewSubject().trim().isEmpty()) {
      return "Subject cannot be empty";
    }

    if (data.getNewEndDateTime().isBefore(data.getNewStartDateTime())) {
      return "End date/time cannot be before start date/time";
    }

    return null;
  }

  /**
   * Functional interface for edit operations.
   * Represents an operation that applies edits to an event.
   */
  @FunctionalInterface
  private interface EditOperation {
    void apply(Event event, EventEditData editData);
  }

  /**
   * Functional interface for delete operations.
   * Represents an operation that deletes an event.
   */
  @FunctionalInterface
  private interface DeleteOperation {
    void apply(Event event);
  }
}