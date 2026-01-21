package controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import calendar.controller.GuiController;
import calendar.model.CalendarEvent;
import calendar.model.CalendarManager;
import calendar.model.Event;
import calendar.model.RecurringEventSeries;
import calendar.model.SimpleCalendarManager;
import calendar.model.Status;
import calendar.view.CalendarEditData;
import calendar.view.EventDeleteData;
import calendar.view.EventEditData;
import calendar.view.EventFormData;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

/**
 * Comprehensive test class for GuiController.
 * Tests all controller functionality with mocked view for 100% coverage.
 */
public class GuiControllerTest {
  /**
   * Sets up the test environment before each test.
   * Initializes a fresh calendar manager, mock view, and controller.
   */
  private CalendarManager manager;
  private MockCalendarGuiView mockView;
  private GuiController controller;


  /**
   * Sets up the test environment before each test.
   * Initializes a fresh calendar manager, mock view, and controller instance
   */
  @Before
  public void setUp() {
    manager = new SimpleCalendarManager();
    mockView = new MockCalendarGuiView();
    controller = new GuiController(manager, mockView);
  }

  @Test
  public void testConstructor() {
    assertNotNull("Controller should be created", controller);
    assertTrue("View should have features listener added",
        mockView.getLog().contains("addFeaturesListener called"));
  }

  @Test
  public void testStart() {
    controller.start();

    assertTrue("Should display view", mockView.getLog().contains("display called"));
    assertTrue("Should set default calendar",
        mockView.getLog().contains("setCurrentCalendar: Default"));
    assertTrue("Should load events for today", mockView.getLog().contains("setEventsForDate"));
    assertTrue("Should load month events", mockView.getLog().contains("setMonthEvents"));
  }

  @Test
  public void testStartWithExistingDefaultCalendar() {
    manager.createCalendar("Default", ZoneId.systemDefault());
    mockView.clearLog();

    controller.start();

    assertTrue("Should display view", mockView.getLog().contains("display called"));
    assertFalse("Should not show error", mockView.getLog().contains("displayError"));
  }

  @Test
  public void testStartWithError() {
    CalendarManager faultyManager = new SimpleCalendarManager() {
      @Override
      public void createCalendar(String name, ZoneId timezone) {
        throw new IllegalArgumentException("Test error");
      }
    };

    GuiController faultyController = new GuiController(faultyManager, mockView);
    mockView.clearLog();
    faultyController.start();

    assertTrue("Should display error",
        mockView.getLog().contains("displayError: Error initializing default calendar"));
  }

  @Test
  public void testCreateCalendarSuccess() {
    mockView.setCalendarNameInput("Work");
    mockView.setTimezoneInput("America/New_York");
    controller.createCalendar();
    assertTrue("Should display success message",
        mockView.getLog().contains("displayMessage: Calendar 'Work' created successfully!"));
    assertTrue("Calendar should exist", manager.hasCalendar("Work"));

    assertEquals("Calendar should have correct timezone", ZoneId.of("America/New_York"),
        manager.getCalendar("Work").getTimezone());
  }

  @Test
  public void testCreateRecurringEventFollowsSelectedWeekdays() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");
    mockView.clearLog();

    LocalDate startDate = LocalDate.of(2025, 5, 5);
    LocalTime startTime = LocalTime.of(9, 0);
    LocalTime endTime = LocalTime.of(10, 0);

    Set<DayOfWeek> weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.MONDAY);
    weekdays.add(DayOfWeek.WEDNESDAY);

    EventFormData formData =
        new EventFormData("Standup", startDate, startDate, false, startTime, endTime, "Room A",
            "Daily standup", "PUBLIC", true, weekdays, false, 0, LocalDate.of(2025, 5, 31));
    mockView.setEventFormInput(formData);

    controller.createEvent(startDate);

    assertTrue("Should display success message",
        mockView.getLog().contains("displayMessage: Event created successfully!"));

    boolean sawMonday = false;
    boolean sawWednesday = false;
    LocalDate endDate = LocalDate.of(2025, 5, 31);

    for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
      List<Event> events = manager.getCurrentCalendar().getEventsOn(date);
      for (Event e : events) {
        if ("Standup".equals(e.getSubject())) {
          DayOfWeek dow = date.getDayOfWeek();

          assertTrue("Recurring event should only occur on selected weekdays (Mon/Wed)",
              dow == DayOfWeek.MONDAY || dow == DayOfWeek.WEDNESDAY);

          if (dow == DayOfWeek.MONDAY) {
            sawMonday = true;
          } else if (dow == DayOfWeek.WEDNESDAY) {
            sawWednesday = true;
          }
        }
      }
    }

    assertTrue("Should create at least one Monday occurrence", sawMonday);
    assertTrue("Should create at least one Wednesday occurrence", sawWednesday);
  }

  @Test
  public void testCreateCalendarWithNullName() {
    mockView.setCalendarNameInput(null);

    controller.createCalendar();

    assertFalse("Should not create calendar", manager.hasCalendar("null"));
    assertFalse("Should not display success message",
        mockView.getLog().contains("created successfully"));
  }

  @Test
  public void testCreateCalendarWithEmptyName() {
    mockView.setCalendarNameInput("   ");

    controller.createCalendar();

    assertFalse("Should not display success message",
        mockView.getLog().contains("created successfully"));
  }

  @Test
  public void testCreateCalendarWithNullTimezone() {
    mockView.setCalendarNameInput("Work");
    mockView.setTimezoneInput(null);

    controller.createCalendar();

    assertFalse("Should not create calendar", manager.hasCalendar("Work"));
  }

  @Test
  public void testCreateCalendarWithInvalidTimezone() {
    mockView.setCalendarNameInput("Work");
    mockView.setTimezoneInput("Invalid/Timezone");

    controller.createCalendar();

    assertTrue("Should display error",
        mockView.getLog().contains("displayError: Error creating calendar"));
  }

  @Test
  public void testSwitchCalendarSuccess() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.createCalendar("Personal", ZoneId.of("America/Chicago"));
    manager.useCalendar("Work");

    LocalDate today = LocalDate.now();
    Event event =
        new CalendarEvent.EventBuilder().subject("Test Event").startDateTime(today.atTime(10, 0))
            .endDateTime(today.atTime(11, 0)).build();
    manager.getCurrentCalendar().addEvent(event);

    mockView.clearLog();
    mockView.setCalendarSelectionInput("Personal");

    controller.switchCalendar();

    assertTrue("Should display success message",
        mockView.getLog().contains("displayMessage: Switched to calendar: Personal"));
    assertTrue("Should set current calendar in view",
        mockView.getLog().contains("setCurrentCalendar: Personal"));
    assertTrue("Should refresh events for current date",
        mockView.getLog().contains("setEventsForDate"));
    assertTrue("Should refresh month events", mockView.getLog().contains("setMonthEvents"));
  }

  @Test
  public void testSwitchCalendarWithNoCalendars() {
    controller.switchCalendar();

    assertTrue("Should display error",
        mockView.getLog().contains("displayError: No calendars available"));
  }

  @Test
  public void testSwitchCalendarWithNullSelection() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    mockView.clearLog();
    mockView.setCalendarSelectionInput(null);

    controller.switchCalendar();

    assertFalse("Should not display success message",
        mockView.getLog().contains("Switched to calendar"));
  }

  @Test
  public void testSwitchCalendarWithError() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    mockView.clearLog();
    mockView.setCalendarSelectionInput("NonExistent");

    controller.switchCalendar();

    assertTrue("Should display error",
        mockView.getLog().contains("displayError: Error switching calendar"));
  }

  @Test
  public void testEditCalendarNameVerifiesSwitch() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    // Add an event to the Work calendar
    LocalDate date = LocalDate.of(2025, 5, 15);
    Event event =
        new CalendarEvent.EventBuilder().subject("Meeting").startDateTime(date.atTime(10, 0))
            .endDateTime(date.atTime(11, 0)).build();
    manager.getCurrentCalendar().addEvent(event);

    mockView.clearLog();

    CalendarEditData editData = new CalendarEditData("name", "Office");
    mockView.setCalendarEditInput(editData);

    controller.editCalendar();

    // Verify manager is actually using the renamed calendar
    assertEquals("Manager should be using renamed calendar", "Office",
        manager.getCurrentCalendar().getName());

    // Verify the event is still accessible through the renamed calendar
    List<Event> events = manager.getCurrentCalendar().getEventsOn(date);
    assertEquals("Should still have the event", 1, events.size());
    assertEquals("Meeting", events.get(0).getSubject());
  }

  @Test
  public void testEditCalendarTimezone() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");
    mockView.clearLog();

    CalendarEditData editData = new CalendarEditData("timezone", "America/Chicago");
    mockView.setCalendarEditInput(editData);

    controller.editCalendar();

    assertTrue("Should display success message",
        mockView.getLog().contains("displayMessage: Calendar updated successfully!"));
    assertEquals("Timezone should be updated", ZoneId.of("America/Chicago"),
        manager.getCurrentCalendar().getTimezone());
  }

  @Test
  public void testEditCalendarWithNoCalendarSelected() {
    controller.editCalendar();

    assertTrue("Should display error",
        mockView.getLog().contains("displayError: No calendar selected"));
  }

  @Test
  public void testEditCalendarWithNullEditData() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");
    mockView.clearLog();
    mockView.setCalendarEditInput(null);

    controller.editCalendar();

    assertFalse("Should not display success message",
        mockView.getLog().contains("Calendar updated successfully"));
  }

  @Test
  public void testEditCalendarWithError() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");
    mockView.clearLog();

    CalendarEditData editData = new CalendarEditData("timezone", "Invalid/Zone");
    mockView.setCalendarEditInput(editData);

    controller.editCalendar();

    assertTrue("Should display error",
        mockView.getLog().contains("displayError: Error editing calendar"));
  }

  @Test
  public void testCreateSingleEventSuccess() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");
    mockView.clearLog();

    LocalDate date = LocalDate.of(2025, 5, 15);
    LocalTime startTime = LocalTime.of(10, 0);
    LocalTime endTime = LocalTime.of(11, 0);

    EventFormData formData =
        new EventFormData("Team Meeting", date, date, false, startTime, endTime, "Conference Room",
            "Weekly sync", "PUBLIC", false, null, false, 0, null);
    mockView.setEventFormInput(formData);

    controller.createEvent(date);

    assertTrue("Should display success message",
        mockView.getLog().contains("displayMessage: Event created successfully!"));

    List<Event> events = manager.getCurrentCalendar().getEventsOn(date);
    assertEquals("Should have one event", 1, events.size());
    assertEquals("Event subject should match", "Team Meeting", events.get(0).getSubject());
  }

  @Test
  public void testCreateRecurringEventRespectsRepeatCount() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");
    mockView.clearLog();

    LocalDate startDate = LocalDate.of(2025, 5, 5);
    LocalTime startTime = LocalTime.of(9, 0);
    LocalTime endTime = LocalTime.of(10, 0);

    Set<DayOfWeek> weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.MONDAY);

    int repeatCount = 5;

    EventFormData formData =
        new EventFormData("Standup", startDate, startDate, false, startTime, endTime, "Room A",
            "Count-limited standup", "PUBLIC", true, weekdays, true, repeatCount, null);
    mockView.setEventFormInput(formData);

    controller.createEvent(startDate);

    assertTrue("Should display success message",
        mockView.getLog().contains("displayMessage: Event created successfully!"));

    int occurrenceCount = 0;
    LocalDate endSearchDate = startDate.plusWeeks(10);

    for (LocalDate date = startDate; !date.isAfter(endSearchDate); date = date.plusDays(1)) {
      List<Event> events = manager.getCurrentCalendar().getEventsOn(date);
      for (Event e : events) {
        if ("Standup".equals(e.getSubject())) {
          occurrenceCount++;

          assertEquals("Recurring event should only occur on MONDAY", DayOfWeek.MONDAY,
              date.getDayOfWeek());
        }
      }
    }

    assertEquals("Recurring event should be created exactly repeatCount times", repeatCount,
        occurrenceCount);
  }

  @Test
  public void testCreateAllDayEventSuccess() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");
    mockView.clearLog();

    LocalDate date = LocalDate.of(2025, 5, 15);

    EventFormData formData =
        new EventFormData("Conference", date, date, true, null, null, "Convention Center",
            "Annual conference", "PUBLIC", false, null, false, 0, null);
    mockView.setEventFormInput(formData);

    controller.createEvent(date);

    assertTrue("Should display success message",
        mockView.getLog().contains("displayMessage: Event created successfully!"));

    List<Event> events = manager.getCurrentCalendar().getEventsOn(date);
    assertEquals("Should have one event", 1, events.size());
    assertTrue("Event should be all day", events.get(0).isAllDay());
  }

  @Test
  public void testCreateRecurringEventWithCount() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");
    mockView.clearLog();

    LocalDate date = LocalDate.of(2025, 5, 15);
    LocalTime startTime = LocalTime.of(9, 0);
    LocalTime endTime = LocalTime.of(10, 0);

    Set<DayOfWeek> weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.MONDAY);
    weekdays.add(DayOfWeek.WEDNESDAY);

    EventFormData formData =
        new EventFormData("Standup", date, date, false, startTime, endTime, "Office",
            "Daily standup", "PUBLIC", true, weekdays, true, 5, null);
    mockView.setEventFormInput(formData);

    controller.createEvent(date);

    assertTrue("Should display success message",
        mockView.getLog().contains("displayMessage: Event created successfully!"));
  }

  @Test
  public void testCreateRecurringEventWithUntilDate() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");
    mockView.clearLog();

    LocalDate date = LocalDate.of(2025, 5, 15);
    LocalTime startTime = LocalTime.of(14, 0);
    LocalTime endTime = LocalTime.of(15, 0);

    Set<DayOfWeek> weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.FRIDAY);

    EventFormData formData =
        new EventFormData("Weekly Review", date, date, false, startTime, endTime, null, null,
            "PUBLIC", true, weekdays, false, 0, LocalDate.of(2025, 6, 30));
    mockView.setEventFormInput(formData);

    controller.createEvent(date);

    assertTrue("Should display success message",
        mockView.getLog().contains("displayMessage: Event created successfully!"));
  }

  @Test
  public void testCreateEventWithNoCalendarSelected() {
    LocalDate date = LocalDate.of(2025, 5, 15);
    controller.createEvent(date);

    assertTrue("Should display error",
        mockView.getLog().contains("displayError: No calendar selected"));
  }

  @Test
  public void testCreateEventWithNullFormData() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");
    mockView.clearLog();

    LocalDate date = LocalDate.of(2025, 5, 15);
    mockView.setEventFormInput(null);

    controller.createEvent(date);

    assertFalse("Should not display success message",
        mockView.getLog().contains("Event created successfully"));
  }

  @Test
  public void testCreateEventWithError() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate date = LocalDate.of(2025, 5, 15);
    LocalDateTime start = date.atTime(10, 0);
    LocalDateTime end = date.atTime(11, 0);

    Event existing =
        new CalendarEvent.EventBuilder().subject("Meeting").startDateTime(start).endDateTime(end)
            .build();
    manager.getCurrentCalendar().addEvent(existing);

    mockView.clearLog();

    EventFormData formData =
        new EventFormData("Meeting", date, date, false, LocalTime.of(10, 0), LocalTime.of(11, 0),
            null, null, "PUBLIC", false, null, false, 0, null);
    mockView.setEventFormInput(formData);

    controller.createEvent(date);

    assertTrue("Should display error",
        mockView.getLog().contains("displayError: Error creating event"));
  }

  @Test
  public void testEditEventSingleScope() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate date = LocalDate.of(2025, 5, 15);
    LocalDateTime start = date.atTime(10, 0);
    LocalDateTime end = date.atTime(11, 0);

    Event event =
        new CalendarEvent.EventBuilder().subject("Meeting").startDateTime(start).endDateTime(end)
            .location("Room A").description("Old description").status(Status.PUBLIC).build();
    manager.getCurrentCalendar().addEvent(event);

    mockView.clearLog();

    EventEditData editData =
        new EventEditData(event, "Updated Meeting", start.plusHours(1), end.plusHours(1), "Room B",
            "New description", "PRIVATE", "single");
    mockView.setEventEditInput(editData);
    controller.editEvents(date);
    assertTrue("Should display success message",
        mockView.getLog().contains("displayMessage: Event(s) updated successfully!"));

    List<Event> events = manager.getCurrentCalendar().getEventsOn(date);
    assertEquals("Should still have one event on that date", 1, events.size());

    Event updated = events.get(0);
    assertEquals("Subject should be updated", "Updated Meeting", updated.getSubject());
    assertEquals("Location should be updated", "Room B", updated.getLocation());
    assertEquals("Description should be updated", "New description", updated.getDescription());
    assertEquals("Status should be updated", Status.PRIVATE, updated.getStatus());
    assertEquals("Start time should be updated", start.plusHours(1), updated.getStartDateTime());
    assertEquals("End time should be updated", end.plusHours(1), updated.getEndDateTime());
  }

  @Test
  public void testEditEventAllScope() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate date = LocalDate.of(2025, 5, 15);
    LocalDateTime start = date.atTime(14, 0);
    LocalDateTime end = date.atTime(15, 0);

    Set<DayOfWeek> weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.THURSDAY);

    RecurringEventSeries series =
        new RecurringEventSeries.EventSeriesBuilder().subject("Review").startDateTime(start)
            .endDateTime(end).weekdays(weekdays).repeatCount(5).build();
    manager.getCurrentCalendar().addEventSeries(series);

    List<Event> events = manager.getCurrentCalendar().getEventsOn(date);
    Event event = events.get(0);

    mockView.clearLog();

    EventEditData editData =
        new EventEditData(event, "Review", start, end, "Building B", "Weekly review meeting",
            "PUBLIC", "all");
    mockView.setEventEditInput(editData);

    controller.editEvents(date);

    assertTrue("Should display success message",
        mockView.getLog().contains("displayMessage: Event(s) updated successfully!"));
  }

  @Test
  public void testEditEventsWithNoCalendarSelected() {
    LocalDate date = LocalDate.of(2025, 5, 15);
    controller.editEvents(date);

    assertTrue("Should display error",
        mockView.getLog().contains("displayError: No calendar selected"));
  }

  @Test
  public void testEditEventsWithNoEvents() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");
    mockView.clearLog();

    LocalDate date = LocalDate.of(2025, 5, 15);
    controller.editEvents(date);

    assertTrue("Should display message",
        mockView.getLog().contains("displayMessage: No events on this date to edit"));
  }

  @Test
  public void testEditEventsWithNullEditData() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate date = LocalDate.of(2025, 5, 15);
    Event event =
        new CalendarEvent.EventBuilder().subject("Meeting").startDateTime(date.atTime(10, 0))
            .endDateTime(date.atTime(11, 0)).build();
    manager.getCurrentCalendar().addEvent(event);

    mockView.clearLog();
    mockView.setEventEditInput(null);

    controller.editEvents(date);

    assertFalse("Should not display success message",
        mockView.getLog().contains("Event(s) updated successfully"));
  }

  @Test
  public void testEditEventsWithError() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate date = LocalDate.of(2025, 5, 15);
    Event event =
        new CalendarEvent.EventBuilder().subject("Meeting").startDateTime(date.atTime(10, 0))
            .endDateTime(date.atTime(11, 0)).build();
    manager.getCurrentCalendar().addEvent(event);

    mockView.clearLog();

    EventEditData editData =
        new EventEditData(event, "Meeting", date.atTime(10, 0), date.atTime(11, 0), null, null,
            "INVALID_STATUS", "invalid_scope");
    mockView.setEventEditInput(editData);

    controller.editEvents(date);

    assertTrue("Should display error",
        mockView.getLog().contains("displayError: Error editing event"));
  }

  @Test
  public void testDeleteEventThis() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate date = LocalDate.of(2025, 5, 15);
    Event event =
        new CalendarEvent.EventBuilder().subject("Meeting").startDateTime(date.atTime(10, 0))
            .endDateTime(date.atTime(11, 0)).build();
    manager.getCurrentCalendar().addEvent(event);

    mockView.clearLog();

    EventDeleteData deleteData = new EventDeleteData(event, "THIS");
    mockView.setEventDeleteInput(deleteData);

    controller.deleteEvent(date);

    assertTrue("Should display success message",
        mockView.getLog().contains("displayMessage: Event deleted successfully!"));

    List<Event> events = manager.getCurrentCalendar().getEventsOn(date);
    assertEquals("Should have no events", 0, events.size());
  }

  @Test
  public void testDeleteEventThisAndFuture() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate date = LocalDate.of(2025, 5, 15);
    LocalDateTime start = date.atTime(9, 0);
    LocalDateTime end = date.atTime(10, 0);

    Set<DayOfWeek> weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.THURSDAY);

    RecurringEventSeries series =
        new RecurringEventSeries.EventSeriesBuilder().subject("Standup").startDateTime(start)
            .endDateTime(end).weekdays(weekdays).repeatCount(10).build();
    manager.getCurrentCalendar().addEventSeries(series);

    List<Event> events = manager.getCurrentCalendar().getEventsOn(date);
    Event event = events.get(0);

    mockView.clearLog();

    EventDeleteData deleteData = new EventDeleteData(event, "THIS_AND_FUTURE");
    mockView.setEventDeleteInput(deleteData);

    controller.deleteEvent(date);

    assertTrue("Should display success message",
        mockView.getLog().contains("Event and future occurrences deleted successfully!"));
  }

  @Test
  public void testDeleteEventAll() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate date = LocalDate.of(2025, 5, 15);
    LocalDateTime start = date.atTime(14, 0);
    LocalDateTime end = date.atTime(15, 0);

    Set<DayOfWeek> weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.THURSDAY);

    RecurringEventSeries series =
        new RecurringEventSeries.EventSeriesBuilder().subject("Review").startDateTime(start)
            .endDateTime(end).weekdays(weekdays).repeatCount(5).build();
    manager.getCurrentCalendar().addEventSeries(series);

    List<Event> events = manager.getCurrentCalendar().getEventsOn(date);
    Event event = events.get(0);

    mockView.clearLog();

    EventDeleteData deleteData = new EventDeleteData(event, "ALL");
    mockView.setEventDeleteInput(deleteData);

    controller.deleteEvent(date);

    assertTrue("Should display success message",
        mockView.getLog().contains("displayMessage: All events in series deleted successfully!"));
  }

  @Test
  public void testDeleteEventWithNoCalendarSelected() {
    LocalDate date = LocalDate.of(2025, 5, 15);
    controller.deleteEvent(date);

    assertTrue("Should display error",
        mockView.getLog().contains("displayError: No calendar selected"));
  }

  @Test
  public void testDeleteEventWithNoEvents() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");
    mockView.clearLog();

    LocalDate date = LocalDate.of(2025, 5, 15);
    controller.deleteEvent(date);

    assertTrue("Should display message",
        mockView.getLog().contains("displayMessage: No events on this date to delete"));
  }

  @Test
  public void testDeleteEventWithNullDeleteData() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate date = LocalDate.of(2025, 5, 15);
    Event event =
        new CalendarEvent.EventBuilder().subject("Meeting").startDateTime(date.atTime(10, 0))
            .endDateTime(date.atTime(11, 0)).build();
    manager.getCurrentCalendar().addEvent(event);

    mockView.clearLog();
    mockView.setEventDeleteInput(null);

    controller.deleteEvent(date);

    assertFalse("Should not display success message",
        mockView.getLog().contains("deleted successfully"));
  }

  @Test
  public void testDeleteEventWithNullEvent() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate date = LocalDate.of(2025, 5, 15);
    Event event =
        new CalendarEvent.EventBuilder().subject("Meeting").startDateTime(date.atTime(10, 0))
            .endDateTime(date.atTime(11, 0)).build();
    manager.getCurrentCalendar().addEvent(event);

    mockView.clearLog();

    EventDeleteData deleteData = new EventDeleteData(null, "THIS");
    mockView.setEventDeleteInput(deleteData);

    controller.deleteEvent(date);

    assertTrue("Should display error",
        mockView.getLog().contains("displayError: Could not find the selected event"));
  }

  @Test
  public void testDeleteEventWithInvalidScope() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate date = LocalDate.of(2025, 5, 15);
    Event event =
        new CalendarEvent.EventBuilder().subject("Meeting").startDateTime(date.atTime(10, 0))
            .endDateTime(date.atTime(11, 0)).build();
    manager.getCurrentCalendar().addEvent(event);

    mockView.clearLog();

    EventDeleteData deleteData = new EventDeleteData(event, "INVALID");
    mockView.setEventDeleteInput(deleteData);

    controller.deleteEvent(date);

    assertTrue("Should display error",
        mockView.getLog().contains("displayError: Error deleting event"));
  }

  @Test
  public void testDateSelected() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate date = LocalDate.of(2025, 5, 15);
    Event event =
        new CalendarEvent.EventBuilder().subject("Meeting").startDateTime(date.atTime(10, 0))
            .endDateTime(date.atTime(11, 0)).build();
    manager.getCurrentCalendar().addEvent(event);

    mockView.clearLog();

    controller.dateSelected(date);

    assertTrue("Should set events for date",
        mockView.getLog().contains("setEventsForDate: " + date));

    List<Event> shown = mockView.getLastEventsForDate();
    assertEquals("View should receive 1 event", 1, shown.size());
    assertEquals("Event subject should match", "Meeting", shown.get(0).getSubject());
  }

  @Test
  public void testDateSelectedWithNoCalendar() {
    LocalDate date = LocalDate.of(2025, 5, 15);
    controller.dateSelected(date);

    assertFalse("Should not display error", mockView.getLog().contains("displayError"));
  }

  @Test
  public void testDateSelectedWithError() {
    manager.createCalendar("Faulty", ZoneId.systemDefault());
    manager.useCalendar("Faulty");

    CalendarManager faultyManager = new SimpleCalendarManager() {
      private calendar.model.Calendar faultyCalendar =
          new calendar.model.Calendar("Faulty", ZoneId.systemDefault()) {
            @Override
            public List<Event> getEventsOn(LocalDate date) {
              throw new RuntimeException("Test error");
            }
          };

      @Override
      public calendar.model.Calendar getCurrentCalendar() {
        return faultyCalendar;
      }
    };

    GuiController faultyController = new GuiController(faultyManager, mockView);
    mockView.clearLog();

    LocalDate date = LocalDate.of(2025, 5, 15);
    faultyController.dateSelected(date);

    assertTrue("Should display error",
        mockView.getLog().contains("displayError: Error loading events"));
  }

  @Test
  public void testMonthChanged() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate date = LocalDate.of(2025, 5, 15);
    Event event =
        new CalendarEvent.EventBuilder().subject("Meeting").startDateTime(date.atTime(10, 0))
            .endDateTime(date.atTime(11, 0)).build();
    manager.getCurrentCalendar().addEvent(event);

    mockView.clearLog();

    controller.monthChanged(2025, 5);

    assertTrue("Should set month events", mockView.getLog().contains("setMonthEvents"));
  }

  @Test
  public void testMonthChangedWithNoCalendar() {
    controller.monthChanged(2025, 5);

    assertFalse("Should not display error", mockView.getLog().contains("displayError"));
  }

  @Test
  public void testMonthChangedWithError() {
    manager.createCalendar("Faulty", ZoneId.systemDefault());
    manager.useCalendar("Faulty");

    CalendarManager faultyManager = new SimpleCalendarManager() {
      private calendar.model.Calendar faultyCalendar =
          new calendar.model.Calendar("Faulty", ZoneId.systemDefault()) {
            @Override
            public List<Event> getEventsOn(LocalDate date) {
              throw new RuntimeException("Test error");
            }
          };

      @Override
      public calendar.model.Calendar getCurrentCalendar() {
        return faultyCalendar;
      }
    };

    GuiController faultyController = new GuiController(faultyManager, mockView);
    mockView.clearLog();

    faultyController.monthChanged(2025, 5);

    assertTrue("Should display error",
        mockView.getLog().contains("displayError: Error loading month events"));
  }

  @Test
  public void testEditEventWithAllPropertiesChanged() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate date = LocalDate.of(2025, 5, 15);
    Event event =
        new CalendarEvent.EventBuilder().subject("Meeting").startDateTime(date.atTime(10, 0))
            .endDateTime(date.atTime(11, 0)).location("Room A").description("Old description")
            .status(Status.PUBLIC).build();
    manager.getCurrentCalendar().addEvent(event);

    mockView.clearLog();

    EventEditData editData =
        new EventEditData(event, "New Meeting", date.atTime(14, 0), date.atTime(15, 0), "Room B",
            "New description", "PRIVATE", "single");
    mockView.setEventEditInput(editData);

    controller.editEvents(date);

    assertTrue("Should display success message",
        mockView.getLog().contains("displayMessage: Event(s) updated successfully!"));
  }

  @Test
  public void testEditEventWithNoPropertiesChanged() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate date = LocalDate.of(2025, 5, 15);
    Event event =
        new CalendarEvent.EventBuilder().subject("Meeting").startDateTime(date.atTime(10, 0))
            .endDateTime(date.atTime(11, 0)).location("Room A").description("Description")
            .status(Status.PUBLIC).build();
    manager.getCurrentCalendar().addEvent(event);

    mockView.clearLog();

    EventEditData editData =
        new EventEditData(event, "Meeting", date.atTime(10, 0), date.atTime(11, 0), "Room A",
            "Description", "PUBLIC", "single");
    mockView.setEventEditInput(editData);

    controller.editEvents(date);

    assertTrue("Should display success message",
        mockView.getLog().contains("displayMessage: Event(s) updated successfully!"));
  }

  @Test
  public void testCreateEventWithOptionalFields() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");
    mockView.clearLog();

    LocalDate date = LocalDate.of(2025, 5, 15);

    EventFormData formData =
        new EventFormData("Meeting", date, date, false, LocalTime.of(10, 0), LocalTime.of(11, 0),
            "  ", "", "PUBLIC", false, null, false, 0, null);
    mockView.setEventFormInput(formData);

    controller.createEvent(date);

    assertTrue("Should display success message",
        mockView.getLog().contains("displayMessage: Event created successfully!"));
  }

  @Test
  public void testMonthChangedWithMultipleEvents() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    for (int day = 1; day <= 10; day++) {
      LocalDate date = LocalDate.of(2025, 5, day);
      Event event =
          new CalendarEvent.EventBuilder().subject("Event " + day).startDateTime(date.atTime(10, 0))
              .endDateTime(date.atTime(11, 0)).build();
      manager.getCurrentCalendar().addEvent(event);
    }

    mockView.clearLog();

    controller.monthChanged(2025, 5);

    assertTrue("Should set month events", mockView.getLog().contains("setMonthEvents"));
  }

  @Test
  public void testEditSeriesWithLocationChange() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate date = LocalDate.of(2025, 5, 15);
    LocalDateTime start = date.atTime(9, 0);
    LocalDateTime end = date.atTime(10, 0);

    Set<DayOfWeek> weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.THURSDAY);

    RecurringEventSeries series =
        new RecurringEventSeries.EventSeriesBuilder().subject("Standup").startDateTime(start)
            .endDateTime(end).location("Room A").weekdays(weekdays).repeatCount(5).build();
    manager.getCurrentCalendar().addEventSeries(series);

    List<Event> events = manager.getCurrentCalendar().getEventsOn(date);
    Event event = events.get(0);

    mockView.clearLog();

    EventEditData editData = new EventEditData(event, "Standup", start, end, "Room B",
        event.getDescription() != null ? event.getDescription() : "", "PUBLIC", "all");
    mockView.setEventEditInput(editData);

    controller.editEvents(date);

    assertTrue("Should display success message",
        mockView.getLog().contains("displayMessage: Event(s) updated successfully!"));
  }

  @Test
  public void testEditSeriesWithDescriptionChange() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate date = LocalDate.of(2025, 5, 15);
    LocalDateTime start = date.atTime(9, 0);
    LocalDateTime end = date.atTime(10, 0);

    Set<DayOfWeek> weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.THURSDAY);

    RecurringEventSeries series =
        new RecurringEventSeries.EventSeriesBuilder().subject("Review").startDateTime(start)
            .endDateTime(end).description("Old description").weekdays(weekdays).repeatCount(5)
            .build();
    manager.getCurrentCalendar().addEventSeries(series);

    List<Event> events = manager.getCurrentCalendar().getEventsOn(date);
    Event event = events.get(0);

    mockView.clearLog();

    EventEditData editData = new EventEditData(event, "Review", start, end,
        event.getLocation() != null ? event.getLocation() : "", "New description", "PUBLIC", "all");
    mockView.setEventEditInput(editData);

    controller.editEvents(date);

    assertTrue("Should display success message",
        mockView.getLog().contains("displayMessage: Event(s) updated successfully!"));
  }

  @Test
  public void testEditSeriesWithStatusChange() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate date = LocalDate.of(2025, 5, 15);
    LocalDateTime start = date.atTime(9, 0);
    LocalDateTime end = date.atTime(10, 0);

    Set<DayOfWeek> weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.THURSDAY);

    RecurringEventSeries series =
        new RecurringEventSeries.EventSeriesBuilder().subject("Meeting").startDateTime(start)
            .endDateTime(end).status(Status.PUBLIC).weekdays(weekdays).repeatCount(5).build();
    manager.getCurrentCalendar().addEventSeries(series);

    List<Event> events = manager.getCurrentCalendar().getEventsOn(date);
    Event event = events.get(0);

    mockView.clearLog();

    EventEditData editData =
        new EventEditData(event, "Meeting", start, end, "", "", "PRIVATE", "all");
    mockView.setEventEditInput(editData);

    controller.editEvents(date);

    assertTrue("Should display success message",
        mockView.getLog().contains("displayMessage: Event(s) updated successfully!"));
  }

  @Test
  public void testEditSeriesWithSubjectChange() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate date = LocalDate.of(2025, 5, 15);
    LocalDateTime start = date.atTime(9, 0);
    LocalDateTime end = date.atTime(10, 0);

    Set<DayOfWeek> weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.THURSDAY);

    RecurringEventSeries series =
        new RecurringEventSeries.EventSeriesBuilder().subject("OldMeeting").startDateTime(start)
            .endDateTime(end).weekdays(weekdays).repeatCount(5).build();
    manager.getCurrentCalendar().addEventSeries(series);

    List<Event> events = manager.getCurrentCalendar().getEventsOn(date);
    Event event = events.get(0);

    mockView.clearLog();

    EventEditData editData =
        new EventEditData(event, "NewMeeting", start, end, "", "", "PUBLIC", "all");
    mockView.setEventEditInput(editData);

    controller.editEvents(date);

    assertTrue("Should display success message",
        mockView.getLog().contains("displayMessage: Event(s) updated successfully!"));
  }

  @Test
  public void testEditSeriesWithStartDateTimeChange() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate date = LocalDate.of(2025, 5, 15);
    LocalDateTime start = date.atTime(9, 0);
    LocalDateTime end = date.atTime(10, 0);

    Set<DayOfWeek> weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.THURSDAY);

    RecurringEventSeries series =
        new RecurringEventSeries.EventSeriesBuilder().subject("Standup").startDateTime(start)
            .endDateTime(end).weekdays(weekdays).repeatCount(5).build();
    manager.getCurrentCalendar().addEventSeries(series);

    List<Event> events = manager.getCurrentCalendar().getEventsOn(date);
    Event event = events.get(0);

    mockView.clearLog();

    EventEditData editData =
        new EventEditData(event, "Standup", start.plusHours(1), end.plusHours(1), "", "", "PUBLIC",
            "all");
    mockView.setEventEditInput(editData);

    controller.editEvents(date);

    assertTrue("Should display success message",
        mockView.getLog().contains("displayMessage: Event(s) updated successfully!"));
  }

  @Test
  public void testEditSeriesWithEndDateTimeChange() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate date = LocalDate.of(2025, 5, 15);
    LocalDateTime start = date.atTime(9, 0);
    LocalDateTime end = date.atTime(10, 0);

    Set<DayOfWeek> weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.THURSDAY);

    RecurringEventSeries series =
        new RecurringEventSeries.EventSeriesBuilder().subject("Meeting").startDateTime(start)
            .endDateTime(end).weekdays(weekdays).repeatCount(5).build();
    manager.getCurrentCalendar().addEventSeries(series);

    List<Event> events = manager.getCurrentCalendar().getEventsOn(date);
    Event event = events.get(0);

    mockView.clearLog();

    EventEditData editData =
        new EventEditData(event, "Meeting", start, end.plusMinutes(30), "", "", "PUBLIC", "all");
    mockView.setEventEditInput(editData);

    controller.editEvents(date);

    assertTrue("Should display success message",
        mockView.getLog().contains("displayMessage: Event(s) updated successfully!"));
  }

  @Test
  public void testEditSeriesFromScopeWithLocationChange() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate date = LocalDate.of(2025, 5, 15);
    LocalDateTime start = date.atTime(9, 0);
    LocalDateTime end = date.atTime(10, 0);

    Set<DayOfWeek> weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.THURSDAY);

    RecurringEventSeries series =
        new RecurringEventSeries.EventSeriesBuilder().subject("Standup").startDateTime(start)
            .endDateTime(end).location("Room A").weekdays(weekdays).repeatCount(10).build();
    manager.getCurrentCalendar().addEventSeries(series);

    List<Event> events = manager.getCurrentCalendar().getEventsOn(date);
    Event event = events.get(0);

    mockView.clearLog();

    EventEditData editData =
        new EventEditData(event, "Standup", start, end, "Room C", "", "PUBLIC", "from");
    mockView.setEventEditInput(editData);

    controller.editEvents(date);

    assertTrue("Should display success message",
        mockView.getLog().contains("displayMessage: Event(s) updated successfully!"));
  }

  @Test
  public void testEditSeriesFromScopeWithMultipleChanges() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate date = LocalDate.of(2025, 5, 15);
    LocalDateTime start = date.atTime(9, 0);
    LocalDateTime end = date.atTime(10, 0);

    Set<DayOfWeek> weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.THURSDAY);

    RecurringEventSeries series =
        new RecurringEventSeries.EventSeriesBuilder().subject("Review").startDateTime(start)
            .endDateTime(end).location("Room A").description("Old desc").weekdays(weekdays)
            .repeatCount(10).build();
    manager.getCurrentCalendar().addEventSeries(series);

    List<Event> events = manager.getCurrentCalendar().getEventsOn(date);
    Event event = events.get(0);

    mockView.clearLog();

    EventEditData editData =
        new EventEditData(event, "Updated Review", start.plusHours(1), end.plusHours(1), "Room D",
            "New description", "PRIVATE", "from");
    mockView.setEventEditInput(editData);

    controller.editEvents(date);

    assertTrue("Should display success message",
        mockView.getLog().contains("displayMessage: Event(s) updated successfully!"));
  }

  @Test
  public void testEditSeriesAllScopeWithMultipleChanges() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate date = LocalDate.of(2025, 5, 15);
    LocalDateTime start = date.atTime(14, 0);
    LocalDateTime end = date.atTime(15, 0);

    Set<DayOfWeek> weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.THURSDAY);

    RecurringEventSeries series =
        new RecurringEventSeries.EventSeriesBuilder().subject("Weekly").startDateTime(start)
            .endDateTime(end).location("Building A").description("Weekly meeting")
            .status(Status.PUBLIC).weekdays(weekdays).repeatCount(8).build();
    manager.getCurrentCalendar().addEventSeries(series);

    List<Event> events = manager.getCurrentCalendar().getEventsOn(date);
    Event event = events.get(0);

    mockView.clearLog();

    EventEditData editData =
        new EventEditData(event, "Monthly", start.plusMinutes(30), end.plusMinutes(45),
            "Building B", "Monthly review", "PRIVATE", "all");
    mockView.setEventEditInput(editData);

    controller.editEvents(date);

    assertTrue("Should display success message",
        mockView.getLog().contains("displayMessage: Event(s) updated successfully!"));
  }

  @Test
  public void testMonthChangeWithMultipleEvents() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    for (int day = 1; day <= 10; day++) {
      LocalDate date = LocalDate.of(2025, 5, day);
      Event event =
          new CalendarEvent.EventBuilder().subject("Event " + day).startDateTime(date.atTime(10, 0))
              .endDateTime(date.atTime(11, 0)).build();
      manager.getCurrentCalendar().addEvent(event);
    }

    mockView.clearLog();

    controller.monthChanged(2025, 5);

    assertTrue("Should set month events", mockView.getLog().contains("setMonthEvents"));
  }

  @Test
  public void testCreateRecurringEventWithLocationAndDescription() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");
    mockView.clearLog();

    LocalDate date = LocalDate.of(2025, 5, 15);
    LocalTime startTime = LocalTime.of(9, 0);
    LocalTime endTime = LocalTime.of(10, 0);

    Set<DayOfWeek> weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.THURSDAY);

    EventFormData formData =
        new EventFormData("Team Standup", date, date, false, startTime, endTime,
            "Conference Room A", "Daily team sync", "PUBLIC", true, weekdays, true, 5, null);
    mockView.setEventFormInput(formData);

    controller.createEvent(date);

    assertTrue("Should display success message",
        mockView.getLog().contains("displayMessage: Event created successfully!"));
  }

  @Test
  public void testCreateRecurringEventWithOnlyLocation() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");
    mockView.clearLog();

    LocalDate date = LocalDate.of(2025, 5, 16);
    LocalTime startTime = LocalTime.of(14, 0);
    LocalTime endTime = LocalTime.of(15, 0);

    Set<DayOfWeek> weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.FRIDAY);

    EventFormData formData =
        new EventFormData("Weekly Review", date, date, false, startTime, endTime, "Room B", null,
            "PUBLIC", true, weekdays, true, 3, null);
    mockView.setEventFormInput(formData);

    controller.createEvent(date);

    assertTrue("Should display success message",
        mockView.getLog().contains("displayMessage: Event created successfully!"));
  }

  @Test
  public void testCreateRecurringEventWithOnlyDescription() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");
    mockView.clearLog();

    LocalDate date = LocalDate.of(2025, 5, 17);
    LocalTime startTime = LocalTime.of(11, 0);
    LocalTime endTime = LocalTime.of(12, 0);

    Set<DayOfWeek> weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.MONDAY);

    EventFormData formData =
        new EventFormData("Planning Meeting", date, date, false, startTime, endTime, null,
            "Sprint planning session", "PUBLIC", true, weekdays, false, 0,
            LocalDate.of(2025, 6, 30));
    mockView.setEventFormInput(formData);

    controller.createEvent(date);

    assertTrue("Should display success message",
        mockView.getLog().contains("displayMessage: Event created successfully!"));
  }

  @Test
  public void testCreateRecurringEventWithEmptyLocation() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");
    mockView.clearLog();

    LocalDate date = LocalDate.of(2025, 5, 18);
    LocalTime startTime = LocalTime.of(10, 0);
    LocalTime endTime = LocalTime.of(11, 0);

    Set<DayOfWeek> weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.TUESDAY);

    EventFormData formData =
        new EventFormData("Daily Scrum", date, date, false, startTime, endTime, "   ",
            "Description here", "PUBLIC", true, weekdays, true, 10, null);
    mockView.setEventFormInput(formData);

    controller.createEvent(date);

    assertTrue("Should display success message",
        mockView.getLog().contains("displayMessage: Event created successfully!"));
  }

  @Test
  public void testCreateRecurringEventWithEmptyDescription() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");
    mockView.clearLog();

    LocalDate date = LocalDate.of(2025, 5, 19);
    LocalTime startTime = LocalTime.of(15, 0);
    LocalTime endTime = LocalTime.of(16, 0);

    Set<DayOfWeek> weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.WEDNESDAY);

    EventFormData formData =
        new EventFormData("Retrospective", date, date, false, startTime, endTime, "Conference Hall",
            "", "PUBLIC", true, weekdays, true, 4, null);
    mockView.setEventFormInput(formData);

    controller.createEvent(date);

    assertTrue("Should display success message",
        mockView.getLog().contains("displayMessage: Event created successfully!"));
  }

  @Test
  public void testCreateAllDayEventEndTimeIs17() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");
    mockView.clearLog();

    LocalDate date = LocalDate.of(2025, 5, 15);

    EventFormData formData =
        new EventFormData("All Day Meeting", date, date, true, null, null, null, null, "PUBLIC",
            false, null, false, 0, null);
    mockView.setEventFormInput(formData);

    controller.createEvent(date);

    List<Event> events = manager.getCurrentCalendar().getEventsOn(date);
    assertEquals("Should have one event", 1, events.size());
    assertEquals("End time should be 17:00", 17, events.get(0).getEndDateTime().getHour());
    assertEquals("End time minutes should be 0", 0, events.get(0).getEndDateTime().getMinute());
  }

  @Test
  public void testCreateSingleEventWithNonEmptyLocation() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");
    mockView.clearLog();

    LocalDate date = LocalDate.of(2025, 5, 15);

    EventFormData formData =
        new EventFormData("Team Sync", date, date, false, LocalTime.of(10, 0), LocalTime.of(11, 0),
            "Conference Room", null, "PUBLIC", false, null, false, 0, null);
    mockView.setEventFormInput(formData);

    controller.createEvent(date);

    List<Event> events = manager.getCurrentCalendar().getEventsOn(date);
    assertEquals("Should have one event", 1, events.size());
    assertEquals("Location should be set", "Conference Room", events.get(0).getLocation());
  }

  @Test
  public void testCreateSingleEventWithNullLocation() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");
    mockView.clearLog();

    LocalDate date = LocalDate.of(2025, 5, 16);

    EventFormData formData = new EventFormData("Quick Call", date, date, false, LocalTime.of(14, 0),
        LocalTime.of(14, 30), null, "Important call", "PUBLIC", false, null, false, 0, null);
    mockView.setEventFormInput(formData);

    controller.createEvent(date);

    List<Event> events = manager.getCurrentCalendar().getEventsOn(date);
    assertEquals("Should have one event", 1, events.size());
    assertTrue("Location should be null or empty",
        events.get(0).getLocation() == null || events.get(0).getLocation().isEmpty());
  }

  @Test
  public void testCreateSingleEventWithEmptyLocation() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");
    mockView.clearLog();

    LocalDate date = LocalDate.of(2025, 5, 17);

    EventFormData formData =
        new EventFormData("Virtual Meeting", date, date, false, LocalTime.of(15, 0),
            LocalTime.of(16, 0), "   ", "Online meeting", "PUBLIC", false, null, false, 0, null);
    mockView.setEventFormInput(formData);

    controller.createEvent(date);

    List<Event> events = manager.getCurrentCalendar().getEventsOn(date);
    assertEquals("Should have one event", 1, events.size());
    assertTrue("Location should be null or empty",
        events.get(0).getLocation() == null || events.get(0).getLocation().isEmpty());
  }

  @Test
  public void testCreateSingleEventWithNonEmptyDescription() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");
    mockView.clearLog();

    LocalDate date = LocalDate.of(2025, 5, 18);

    EventFormData formData =
        new EventFormData("Sprint Planning", date, date, false, LocalTime.of(9, 0),
            LocalTime.of(10, 0), null, "Planning for next sprint", "PUBLIC", false, null, false, 0,
            null);
    mockView.setEventFormInput(formData);

    controller.createEvent(date);

    List<Event> events = manager.getCurrentCalendar().getEventsOn(date);
    assertEquals("Should have one event", 1, events.size());
    assertEquals("Description should be set", "Planning for next sprint",
        events.get(0).getDescription());
  }

  @Test
  public void testCreateSingleEventWithNullDescription() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");
    mockView.clearLog();

    LocalDate date = LocalDate.of(2025, 5, 19);

    EventFormData formData = new EventFormData("Brief Sync", date, date, false, LocalTime.of(11, 0),
        LocalTime.of(11, 15), "Room 101", null, "PUBLIC", false, null, false, 0, null);
    mockView.setEventFormInput(formData);

    controller.createEvent(date);

    List<Event> events = manager.getCurrentCalendar().getEventsOn(date);
    assertEquals("Should have one event", 1, events.size());
    assertTrue("Description should be null or empty",
        events.get(0).getDescription() == null || events.get(0).getDescription().isEmpty());
  }

  @Test
  public void testCreateSingleEventWithEmptyDescription() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");
    mockView.clearLog();

    LocalDate date = LocalDate.of(2025, 5, 20);

    EventFormData formData =
        new EventFormData("Standup", date, date, false, LocalTime.of(9, 30), LocalTime.of(9, 45),
            "Office", "", "PUBLIC", false, null, false, 0, null);
    mockView.setEventFormInput(formData);

    controller.createEvent(date);

    List<Event> events = manager.getCurrentCalendar().getEventsOn(date);
    assertEquals("Should have one event", 1, events.size());
    assertTrue("Description should be null or empty",
        events.get(0).getDescription() == null || events.get(0).getDescription().isEmpty());
  }

  @Test
  public void testCreateRecurringEventWithNullLocationAndDescription() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");
    mockView.clearLog();

    LocalDate date = LocalDate.of(2025, 5, 21);
    LocalTime startTime = LocalTime.of(10, 0);
    LocalTime endTime = LocalTime.of(11, 0);

    Set<DayOfWeek> weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.WEDNESDAY);

    EventFormData formData =
        new EventFormData("Weekly Review", date, date, false, startTime, endTime, null, null,
            "PUBLIC", true, weekdays, true, 4, null);
    mockView.setEventFormInput(formData);

    controller.createEvent(date);

    List<Event> events = manager.getCurrentCalendar().getEventsOn(date);
    assertEquals("Should have one event", 1, events.size());
    assertTrue("Location should be null or empty",
        events.get(0).getLocation() == null || events.get(0).getLocation().isEmpty());
    assertTrue("Description should be null or empty",
        events.get(0).getDescription() == null || events.get(0).getDescription().isEmpty());
  }

  @Test
  public void testEditSingleEventSubjectChanged() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate date = LocalDate.of(2025, 5, 15);
    LocalDateTime start = date.atTime(10, 0);
    LocalDateTime end = date.atTime(11, 0);

    Event event =
        new CalendarEvent.EventBuilder().subject("OldMeeting").startDateTime(start).endDateTime(end)
            .build();
    manager.getCurrentCalendar().addEvent(event);

    mockView.clearLog();

    EventEditData editData =
        new EventEditData(event, "NewMeeting", start, end, "", "", "PUBLIC", "single");
    mockView.setEventEditInput(editData);

    controller.editEvents(date);

    assertTrue("Should display success",
        mockView.getLog().contains("Event(s) updated successfully!"));
  }

  @Test
  public void testEditSingleEventStartTimeChanged() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate date = LocalDate.of(2025, 5, 15);
    LocalDateTime start = date.atTime(10, 0);
    LocalDateTime end = date.atTime(11, 0);

    Event event =
        new CalendarEvent.EventBuilder().subject("Meeting").startDateTime(start).endDateTime(end)
            .build();
    manager.getCurrentCalendar().addEvent(event);

    mockView.clearLog();

    EventEditData editData =
        new EventEditData(event, "Meeting", start.plusHours(1), end.plusHours(1), "", "", "PUBLIC",
            "single");
    mockView.setEventEditInput(editData);

    controller.editEvents(date);

    assertTrue("Should display success",
        mockView.getLog().contains("Event(s) updated successfully!"));
  }

  @Test
  public void testEditSingleEventOriginalLocationNull() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate date = LocalDate.of(2025, 5, 20);
    LocalDateTime start = date.atTime(10, 0);
    LocalDateTime end = date.atTime(11, 0);

    Event event =
        new CalendarEvent.EventBuilder().subject("Meeting").startDateTime(start).endDateTime(end)
            .description("Some description").build();
    manager.getCurrentCalendar().addEvent(event);

    mockView.clearLog();

    EventEditData editData =
        new EventEditData(event, "Meeting", start, end, "New Room", "Some description", "PUBLIC",
            "single");
    mockView.setEventEditInput(editData);

    controller.editEvents(date);

    assertTrue("Should display success message",
        mockView.getLog().contains("displayMessage: Event(s) updated successfully!"));
  }

  @Test
  public void testEditSingleEventOriginalLocationNotNull() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate date = LocalDate.of(2025, 5, 21);
    LocalDateTime start = date.atTime(10, 0);
    LocalDateTime end = date.atTime(11, 0);

    Event event =
        new CalendarEvent.EventBuilder().subject("Conference").startDateTime(start).endDateTime(end)
            .location("Room A").build();
    manager.getCurrentCalendar().addEvent(event);

    mockView.clearLog();

    EventEditData editData =
        new EventEditData(event, "Conference", start, end, "Room B", "", "PUBLIC", "single");
    mockView.setEventEditInput(editData);

    controller.editEvents(date);

    assertTrue("Should display success message",
        mockView.getLog().contains("displayMessage: Event(s) updated successfully!"));
  }

  @Test
  public void testEditSingleEventOriginalDescriptionNull() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate date = LocalDate.of(2025, 5, 22);
    LocalDateTime start = date.atTime(10, 0);
    LocalDateTime end = date.atTime(11, 0);

    Event event =
        new CalendarEvent.EventBuilder().subject("Standup").startDateTime(start).endDateTime(end)
            .location("Office").build();
    manager.getCurrentCalendar().addEvent(event);

    mockView.clearLog();

    EventEditData editData =
        new EventEditData(event, "Standup", start, end, "Office", "Daily sync", "PUBLIC", "single");
    mockView.setEventEditInput(editData);

    controller.editEvents(date);

    assertTrue("Should display success message",
        mockView.getLog().contains("displayMessage: Event(s) updated successfully!"));
  }

  @Test
  public void testEditSingleEventOriginalDescriptionNotNull() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate date = LocalDate.of(2025, 5, 23);
    LocalDateTime start = date.atTime(10, 0);
    LocalDateTime end = date.atTime(11, 0);

    Event event =
        new CalendarEvent.EventBuilder().subject("Review").startDateTime(start).endDateTime(end)
            .description("Old description").build();
    manager.getCurrentCalendar().addEvent(event);

    mockView.clearLog();

    EventEditData editData =
        new EventEditData(event, "Review", start, end, "", "New description", "PUBLIC", "single");
    mockView.setEventEditInput(editData);

    controller.editEvents(date);

    assertTrue("Should display success message",
        mockView.getLog().contains("displayMessage: Event(s) updated successfully!"));
  }

  @Test
  public void testEditSeriesOriginalLocationNull() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate date = LocalDate.of(2025, 5, 15);
    LocalDateTime start = date.atTime(9, 0);
    LocalDateTime end = date.atTime(10, 0);

    Set<DayOfWeek> weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.THURSDAY);

    RecurringEventSeries series =
        new RecurringEventSeries.EventSeriesBuilder().subject("Standup").startDateTime(start)
            .endDateTime(end).description("Daily meeting").weekdays(weekdays).repeatCount(5)
            .build();
    manager.getCurrentCalendar().addEventSeries(series);

    List<Event> events = manager.getCurrentCalendar().getEventsOn(date);
    Event event = events.get(0);

    mockView.clearLog();

    EventEditData editData =
        new EventEditData(event, "Standup", start, end, "Room A", "Daily meeting", "PUBLIC", "all");
    mockView.setEventEditInput(editData);

    controller.editEvents(date);

    assertTrue("Should display success message",
        mockView.getLog().contains("displayMessage: Event(s) updated successfully!"));
  }

  @Test
  public void testEditSeriesOriginalLocationNotNull() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate date = LocalDate.of(2025, 5, 15);
    LocalDateTime start = date.atTime(14, 0);
    LocalDateTime end = date.atTime(15, 0);

    Set<DayOfWeek> weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.THURSDAY);

    RecurringEventSeries series =
        new RecurringEventSeries.EventSeriesBuilder().subject("Review").startDateTime(start)
            .endDateTime(end).location("Building A").weekdays(weekdays).repeatCount(5).build();
    manager.getCurrentCalendar().addEventSeries(series);

    List<Event> events = manager.getCurrentCalendar().getEventsOn(date);
    Event event = events.get(0);

    mockView.clearLog();

    EventEditData editData =
        new EventEditData(event, "Review", start, end, "Building B", "", "PUBLIC", "all");
    mockView.setEventEditInput(editData);

    controller.editEvents(date);

    assertTrue("Should display success message",
        mockView.getLog().contains("displayMessage: Event(s) updated successfully!"));
  }

  @Test
  public void testEditEventFromScope() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate startDate = LocalDate.of(2025, 5, 15);
    LocalDateTime start = startDate.atTime(9, 0);
    LocalDateTime end = startDate.atTime(10, 0);

    Set<DayOfWeek> weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.MONDAY);
    weekdays.add(DayOfWeek.WEDNESDAY);
    weekdays.add(DayOfWeek.THURSDAY);

    RecurringEventSeries series =
        new RecurringEventSeries.EventSeriesBuilder().subject("Standup").startDateTime(start)
            .endDateTime(end).location("Room A").description("Daily standup").weekdays(weekdays)
            .repeatCount(10).build();
    manager.getCurrentCalendar().addEventSeries(series);

    int totalEventsBefore = 0;
    LocalDate endSearchDate = startDate.plusWeeks(5);
    for (LocalDate date = startDate; !date.isAfter(endSearchDate); date = date.plusDays(1)) {
      totalEventsBefore += manager.getCurrentCalendar().getEventsOn(date).size();
    }
    assertTrue("Should have created 10 events", totalEventsBefore == 10);

    LocalDate editFromDate = LocalDate.of(2025, 5, 21);
    List<Event> eventsOnEditDate = manager.getCurrentCalendar().getEventsOn(editFromDate);
    assertTrue("Should have at least one event on edit date", eventsOnEditDate.size() > 0);
    Event eventToEdit = eventsOnEditDate.get(0);

    mockView.clearLog();

    EventEditData editData =
        new EventEditData(eventToEdit, "Standup", start, end, "Room B", "Updated standup meeting",
            "PUBLIC", "from");
    mockView.setEventEditInput(editData);

    controller.editEvents(editFromDate);

    assertTrue("Should display success message",
        mockView.getLog().contains("displayMessage: Event(s) updated successfully!"));

    int eventsWithOldLocation = 0;
    int eventsWithNewLocation = 0;

    for (LocalDate date = startDate; !date.isAfter(endSearchDate); date = date.plusDays(1)) {
      List<Event> events = manager.getCurrentCalendar().getEventsOn(date);
      for (Event e : events) {
        if ("Standup".equals(e.getSubject())) {
          if ("Room A".equals(e.getLocation())) {
            eventsWithOldLocation++;
          } else if ("Room B".equals(e.getLocation())) {
            eventsWithNewLocation++;
          }
        }
      }
    }

    assertEquals("Should have 2 events with old location (before edit point)", 2,
        eventsWithOldLocation);
    assertEquals("Should have 8 events with new location (from edit point onwards)", 8,
        eventsWithNewLocation);
  }

  @Test
  public void testEditSeriesOriginalDescriptionNull() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate date = LocalDate.of(2025, 5, 16);
    LocalDateTime start = date.atTime(10, 0);
    LocalDateTime end = date.atTime(11, 0);

    Set<DayOfWeek> weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.FRIDAY);

    RecurringEventSeries series =
        new RecurringEventSeries.EventSeriesBuilder().subject("Planning").startDateTime(start)
            .endDateTime(end).location("Conference Room").weekdays(weekdays).repeatCount(5).build();
    manager.getCurrentCalendar().addEventSeries(series);

    List<Event> events = manager.getCurrentCalendar().getEventsOn(date);
    Event event = events.get(0);

    mockView.clearLog();

    EventEditData editData =
        new EventEditData(event, "Planning", start, end, "Conference Room", "Sprint planning",
            "PUBLIC", "all");
    mockView.setEventEditInput(editData);

    controller.editEvents(date);

    assertTrue("Should display success message",
        mockView.getLog().contains("displayMessage: Event(s) updated successfully!"));
  }

  @Test
  public void testEditMultipleEventsWithSameName() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate date = LocalDate.of(2025, 5, 15);

    Event event1 =
        new CalendarEvent.EventBuilder().subject("Meeting").startDateTime(date.atTime(9, 0))
            .endDateTime(date.atTime(10, 0)).location("Room A").build();
    manager.getCurrentCalendar().addEvent(event1);

    Event event2 =
        new CalendarEvent.EventBuilder().subject("Meeting").startDateTime(date.atTime(14, 0))
            .endDateTime(date.atTime(15, 0)).location("Room B").build();
    manager.getCurrentCalendar().addEvent(event2);

    Event event3 =
        new CalendarEvent.EventBuilder().subject("Meeting").startDateTime(date.atTime(16, 0))
            .endDateTime(date.atTime(17, 0)).location("Room C").build();
    manager.getCurrentCalendar().addEvent(event3);

    mockView.clearLog();

    EventEditData editData =
        new EventEditData(event2, "Updated Meeting", date.atTime(14, 0), date.atTime(15, 0),
            "Room D", "Updated description", "PRIVATE", "single");
    mockView.setEventEditInput(editData);

    controller.editEvents(date);

    assertTrue("Should display success message",
        mockView.getLog().contains("displayMessage: Event(s) updated successfully!"));

    List<Event> events = manager.getCurrentCalendar().getEventsOn(date);
    assertEquals("Should have 3 events", 3, events.size());

    Event editedEvent =
        events.stream().filter(e -> e.getStartDateTime().equals(date.atTime(14, 0))).findFirst()
            .orElse(null);

    assertNotNull("Should find the 2pm event", editedEvent);
    assertEquals("Subject should be updated", "Updated Meeting", editedEvent.getSubject());
    assertEquals("Location should be updated", "Room D", editedEvent.getLocation());

    Event event1After =
        events.stream().filter(e -> e.getStartDateTime().equals(date.atTime(9, 0))).findFirst()
            .orElse(null);
    assertEquals("First event subject unchanged", "Meeting", event1After.getSubject());
    assertEquals("First event location unchanged", "Room A", event1After.getLocation());
  }

  @Test(expected = IllegalStateException.class)
  public void testGetEventsInRangeWithNoCurrentCalendar() {
    LocalDateTime start = LocalDateTime.of(2025, 5, 1, 0, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 31, 23, 59);

    manager.getEventsInRange(start, end);
  }

  @Test
  public void testGetEventsInRangeWithCurrentCalendar() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    Event event = new CalendarEvent.EventBuilder().subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 15, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 15, 11, 0)).build();
    manager.addEvent(event);

    LocalDateTime start = LocalDateTime.of(2025, 5, 1, 0, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 31, 23, 59);

    List<Event> events = manager.getEventsInRange(start, end);
    assertEquals(1, events.size());
  }

  @Test(expected = IllegalStateException.class)
  public void testRemoveEventWithNoCurrentCalendar() {
    Event event = new CalendarEvent.EventBuilder().subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 15, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 15, 11, 0)).build();

    manager.removeEvent(event);
  }

  @Test
  public void testRemoveEventWithCurrentCalendar() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    Event event = new CalendarEvent.EventBuilder().subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 15, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 15, 11, 0)).build();
    manager.addEvent(event);

    manager.removeEvent(event);

    LocalDate date = LocalDate.of(2025, 5, 15);
    assertEquals(0, manager.getEventsOn(date).size());
  }

  @Test(expected = IllegalStateException.class)
  public void testRemoveEventFromSeriesWithNoCurrentCalendar() {
    Event event = new CalendarEvent.EventBuilder().subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 15, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 15, 11, 0)).seriesId("series-123").build();

    manager.removeEventFromSeries(event);
  }

  @Test
  public void testRemoveEventFromSeriesWithCurrentCalendar() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    Set<DayOfWeek> weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.THURSDAY);

    RecurringEventSeries series = new RecurringEventSeries.EventSeriesBuilder().subject("Standup")
        .startDateTime(LocalDateTime.of(2025, 5, 15, 9, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 15, 10, 0)).weekdays(weekdays).repeatCount(5)
        .build();
    manager.addEventSeries(series);

    List<Event> events = manager.getEventsOn(LocalDate.of(2025, 5, 15));
    Event eventToRemove = events.get(0);

    manager.removeEventFromSeries(eventToRemove);

    assertTrue(manager.getEventsOn(LocalDate.of(2025, 5, 15)).isEmpty());
  }

  @Test(expected = IllegalStateException.class)
  public void testRemoveAllEventsInSeriesWithNoCurrentCalendar() {
    Event event = new CalendarEvent.EventBuilder().subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 15, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 15, 11, 0)).seriesId("series-123").build();

    manager.removeAllEventsInSeries(event);
  }

  @Test
  public void testRemoveAllEventsInSeriesWithCurrentCalendar() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    Set<DayOfWeek> weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.THURSDAY);

    RecurringEventSeries series = new RecurringEventSeries.EventSeriesBuilder().subject("Review")
        .startDateTime(LocalDateTime.of(2025, 5, 15, 14, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 15, 15, 0)).weekdays(weekdays).repeatCount(5)
        .build();
    manager.addEventSeries(series);

    List<Event> events = manager.getEventsOn(LocalDate.of(2025, 5, 15));
    Event eventToRemove = events.get(0);

    manager.removeAllEventsInSeries(eventToRemove);

    assertTrue(manager.getEventsOn(LocalDate.of(2025, 5, 15)).isEmpty());
    assertTrue(manager.getEventsOn(LocalDate.of(2025, 5, 22)).isEmpty());
  }

  @Test(expected = IllegalStateException.class)
  public void testEditEventWithNoCurrentCalendar() {
    manager.editEvent("Meeting", LocalDateTime.of(2025, 5, 15, 10, 0),
        LocalDateTime.of(2025, 5, 15, 11, 0), "subject", "New Meeting");
  }

  @Test
  public void testEditEventWithCurrentCalendar() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    Event event = new CalendarEvent.EventBuilder().subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 15, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 15, 11, 0)).build();
    manager.addEvent(event);

    manager.editEvent("Meeting", LocalDateTime.of(2025, 5, 15, 10, 0),
        LocalDateTime.of(2025, 5, 15, 11, 0), "subject", "Updated Meeting");

    List<Event> events = manager.getEventsOn(LocalDate.of(2025, 5, 15));
    assertEquals("Updated Meeting", events.get(0).getSubject());
  }

  @Test(expected = IllegalStateException.class)
  public void testEditEventsFromWithNoCurrentCalendar() {
    manager.editEventsFrom("Meeting", LocalDateTime.of(2025, 5, 15, 10, 0),
        LocalDateTime.of(2025, 5, 15, 11, 0), "subject", "New Meeting");
  }

  @Test
  public void testEditEventsFromWithCurrentCalendar() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    Set<DayOfWeek> weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.MONDAY);

    RecurringEventSeries series = new RecurringEventSeries.EventSeriesBuilder().subject("Standup")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 9, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 10, 0)).weekdays(weekdays).repeatCount(5).build();
    manager.addEventSeries(series);

    List<Event> events = manager.getEventsInRange(LocalDateTime.of(2025, 5, 1, 0, 0),
        LocalDateTime.of(2025, 6, 1, 0, 0));
    Event secondEvent = events.get(1);
  }

  @Test
  public void testCreateSingleEventVerifiesRefresh() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");
    mockView.clearLog();

    LocalDate date = LocalDate.of(2025, 5, 15);
    EventFormData formData =
        new EventFormData("Team Meeting", date, date, false, LocalTime.of(10, 0),
            LocalTime.of(11, 0), "Conference Room", "Weekly sync", "PUBLIC", false, null, false, 0,
            null);
    mockView.setEventFormInput(formData);

    controller.createEvent(date);

    assertTrue("Should display success message",
        mockView.getLog().contains("displayMessage: Event created successfully!"));
    assertTrue("Should refresh date view", mockView.getLog().contains("setEventsForDate: " + date));
    assertTrue("Should refresh month view", mockView.getLog().contains("setMonthEvents"));
  }

  @Test
  public void testCreateRecurringEventWithOptionalFieldsVerified() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");
    mockView.clearLog();

    LocalDate date = LocalDate.of(2025, 5, 15);
    Set<DayOfWeek> weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.THURSDAY);

    EventFormData formData =
        new EventFormData("Standup", date, date, false, LocalTime.of(9, 0), LocalTime.of(10, 0),
            "Room 101", "Daily standup meeting", "PUBLIC", true, weekdays, true, 3, null);
    mockView.setEventFormInput(formData);

    controller.createEvent(date);

    List<Event> events = manager.getCurrentCalendar().getEventsOn(date);
    assertEquals("Should have one event", 1, events.size());
    assertEquals("Location should be set", "Room 101", events.get(0).getLocation());
    assertEquals("Description should be set", "Daily standup meeting",
        events.get(0).getDescription());
  }

  @Test
  public void testEditSingleEventLocationUnchanged() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate date = LocalDate.of(2025, 5, 15);
    Event event =
        new CalendarEvent.EventBuilder().subject("Meeting").startDateTime(date.atTime(10, 0))
            .endDateTime(date.atTime(11, 0)).location("Room A").build();
    manager.getCurrentCalendar().addEvent(event);

    mockView.clearLog();

    EventEditData editData =
        new EventEditData(event, "Meeting", date.atTime(10, 0), date.atTime(11, 0), "Room A", "",
            "PUBLIC", "single");
    mockView.setEventEditInput(editData);

    controller.editEvents(date);

    List<Event> events = manager.getCurrentCalendar().getEventsOn(date);
    assertEquals("Location should remain unchanged", "Room A", events.get(0).getLocation());
  }

  @Test
  public void testEditSeriesAllVerifiesAllEventsChanged() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate date = LocalDate.of(2025, 5, 15);
    Set<DayOfWeek> weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.THURSDAY);

    RecurringEventSeries series = new RecurringEventSeries.EventSeriesBuilder().subject("Review")
        .startDateTime(date.atTime(14, 0)).endDateTime(date.atTime(15, 0)).location("Building A")
        .weekdays(weekdays).repeatCount(5).build();
    manager.getCurrentCalendar().addEventSeries(series);

    List<Event> eventsBefore = manager.getCurrentCalendar().getEventsOn(date);
    Event event = eventsBefore.get(0);

    mockView.clearLog();

    EventEditData editData =
        new EventEditData(event, "Review", date.atTime(14, 0), date.atTime(15, 0), "Building B", "",
            "PUBLIC", "all");
    mockView.setEventEditInput(editData);

    controller.editEvents(date);

    LocalDate endSearch = date.plusWeeks(10);
    for (LocalDate d = date; !d.isAfter(endSearch); d = d.plusDays(1)) {
      List<Event> events = manager.getCurrentCalendar().getEventsOn(d);
      for (Event e : events) {
        if ("Review".equals(e.getSubject())) {
          assertEquals("All series events should have new location", "Building B", e.getLocation());
        }
      }
    }
  }

  @Test
  public void testCreateAllDayEventEndTimeCalculated() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");
    mockView.clearLog();

    LocalDate date = LocalDate.of(2025, 5, 15);

    EventFormData formData =
        new EventFormData("Conference", date, date, true, null, null, null, null, "PUBLIC", false,
            null, false, 0, null);
    mockView.setEventFormInput(formData);

    controller.createEvent(date);

    assertTrue("Should create successfully",
        mockView.getLog().contains("Event created successfully!"));

    List<Event> events = manager.getCurrentCalendar().getEventsOn(date);
    assertFalse("Should have at least one event", events.isEmpty());

    Event event = events.get(0);
    assertNotNull("End date time must not be null", event.getEndDateTime());
    assertEquals("End hour should be 17", 17, event.getEndDateTime().getHour());
    assertEquals("End minute should be 0", 0, event.getEndDateTime().getMinute());
  }

  @Test
  public void testEditEventSingleScopeRefreshesView() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate date = LocalDate.of(2025, 5, 15);
    Event event =
        new CalendarEvent.EventBuilder().subject("Meeting").startDateTime(date.atTime(10, 0))
            .endDateTime(date.atTime(11, 0)).build();
    manager.getCurrentCalendar().addEvent(event);

    mockView.clearLog();

    EventEditData editData =
        new EventEditData(event, "Updated Meeting", date.atTime(10, 0), date.atTime(11, 0), "", "",
            "PUBLIC", "single");
    mockView.setEventEditInput(editData);

    controller.editEvents(date);

    assertTrue("Should refresh date view", mockView.getLog().contains("setEventsForDate: " + date));
    assertTrue("Should refresh month view", mockView.getLog().contains("setMonthEvents"));
  }

  @Test
  public void testEditSingleEventDescriptionUnchanged() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate date = LocalDate.of(2025, 5, 15);
    Event event =
        new CalendarEvent.EventBuilder().subject("Meeting").startDateTime(date.atTime(10, 0))
            .endDateTime(date.atTime(11, 0)).description("Important meeting").build();
    manager.getCurrentCalendar().addEvent(event);

    mockView.clearLog();

    EventEditData editData =
        new EventEditData(event, "Meeting", date.atTime(10, 0), date.atTime(11, 0), "",
            "Important meeting", "PUBLIC", "single");
    mockView.setEventEditInput(editData);

    controller.editEvents(date);

    List<Event> events = manager.getCurrentCalendar().getEventsOn(date);
    assertEquals("Description should remain unchanged", "Important meeting",
        events.get(0).getDescription());
  }

  @Test
  public void testEditSeriesLocationUnchanged() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate date = LocalDate.of(2025, 5, 15);
    Set<DayOfWeek> weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.THURSDAY);

    RecurringEventSeries series = new RecurringEventSeries.EventSeriesBuilder().subject("Standup")
        .startDateTime(date.atTime(9, 0)).endDateTime(date.atTime(10, 0)).location("Room A")
        .weekdays(weekdays).repeatCount(5).build();
    manager.getCurrentCalendar().addEventSeries(series);

    List<Event> events = manager.getCurrentCalendar().getEventsOn(date);
    Event event = events.get(0);

    mockView.clearLog();

    EventEditData editData =
        new EventEditData(event, "Standup", date.atTime(9, 0), date.atTime(10, 0), "Room A", "",
            "PUBLIC", "all");
    mockView.setEventEditInput(editData);

    controller.editEvents(date);

    LocalDate endSearch = date.plusWeeks(10);
    for (LocalDate d = date; !d.isAfter(endSearch); d = d.plusDays(1)) {
      List<Event> evts = manager.getCurrentCalendar().getEventsOn(d);
      for (Event e : evts) {
        if ("Standup".equals(e.getSubject())) {
          assertEquals("Location should remain unchanged", "Room A", e.getLocation());
        }
      }
    }
  }

  @Test
  public void testEditSeriesDescriptionUnchanged() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate date = LocalDate.of(2025, 5, 15);
    Set<DayOfWeek> weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.THURSDAY);

    RecurringEventSeries series = new RecurringEventSeries.EventSeriesBuilder().subject("Review")
        .startDateTime(date.atTime(14, 0)).endDateTime(date.atTime(15, 0))
        .description("Weekly review").weekdays(weekdays).repeatCount(5).build();
    manager.getCurrentCalendar().addEventSeries(series);

    List<Event> events = manager.getCurrentCalendar().getEventsOn(date);
    Event event = events.get(0);

    mockView.clearLog();

    EventEditData editData =
        new EventEditData(event, "Review", date.atTime(14, 0), date.atTime(15, 0), "",
            "Weekly review", "PUBLIC", "all");
    mockView.setEventEditInput(editData);

    controller.editEvents(date);

    assertTrue("Should display success",
        mockView.getLog().contains("Event(s) updated successfully!"));
  }

  @Test
  public void testEditSeriesStatusUnchanged() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate date = LocalDate.of(2025, 5, 15);
    Set<DayOfWeek> weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.THURSDAY);

    RecurringEventSeries series = new RecurringEventSeries.EventSeriesBuilder().subject("Meeting")
        .startDateTime(date.atTime(10, 0)).endDateTime(date.atTime(11, 0)).status(Status.PUBLIC)
        .weekdays(weekdays).repeatCount(5).build();
    manager.getCurrentCalendar().addEventSeries(series);

    List<Event> events = manager.getCurrentCalendar().getEventsOn(date);
    Event event = events.get(0);

    mockView.clearLog();

    EventEditData editData =
        new EventEditData(event, "Meeting", date.atTime(10, 0), date.atTime(11, 0), "", "",
            "PUBLIC", "all");
    mockView.setEventEditInput(editData);

    controller.editEvents(date);

    assertTrue("Should display success",
        mockView.getLog().contains("Event(s) updated successfully!"));
  }

  @Test
  public void testEditSeriesSubjectUnchanged() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate date = LocalDate.of(2025, 5, 15);
    Set<DayOfWeek> weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.THURSDAY);

    RecurringEventSeries series = new RecurringEventSeries.EventSeriesBuilder().subject("Standup")
        .startDateTime(date.atTime(9, 0)).endDateTime(date.atTime(10, 0)).weekdays(weekdays)
        .repeatCount(5).build();
    manager.getCurrentCalendar().addEventSeries(series);

    List<Event> events = manager.getCurrentCalendar().getEventsOn(date);
    Event event = events.get(0);

    mockView.clearLog();

    EventEditData editData =
        new EventEditData(event, "Standup", date.atTime(9, 0), date.atTime(10, 0), "", "", "PUBLIC",
            "all");
    mockView.setEventEditInput(editData);

    controller.editEvents(date);

    assertTrue("Should display success",
        mockView.getLog().contains("Event(s) updated successfully!"));
  }

  @Test
  public void testEditSeriesStartTimeUnchanged() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate date = LocalDate.of(2025, 5, 15);
    Set<DayOfWeek> weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.THURSDAY);

    RecurringEventSeries series = new RecurringEventSeries.EventSeriesBuilder().subject("Review")
        .startDateTime(date.atTime(14, 0)).endDateTime(date.atTime(15, 0)).weekdays(weekdays)
        .repeatCount(5).build();
    manager.getCurrentCalendar().addEventSeries(series);

    List<Event> events = manager.getCurrentCalendar().getEventsOn(date);
    Event event = events.get(0);

    mockView.clearLog();

    EventEditData editData =
        new EventEditData(event, "Review", date.atTime(14, 0), date.atTime(15, 0), "", "", "PUBLIC",
            "all");
    mockView.setEventEditInput(editData);

    controller.editEvents(date);

    assertTrue("Should display success",
        mockView.getLog().contains("Event(s) updated successfully!"));
  }

  @Test
  public void testEditSeriesEndTimeUnchanged() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate date = LocalDate.of(2025, 5, 15);
    Set<DayOfWeek> weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.THURSDAY);

    RecurringEventSeries series = new RecurringEventSeries.EventSeriesBuilder().subject("Planning")
        .startDateTime(date.atTime(10, 0)).endDateTime(date.atTime(11, 0)).weekdays(weekdays)
        .repeatCount(5).build();
    manager.getCurrentCalendar().addEventSeries(series);

    List<Event> events = manager.getCurrentCalendar().getEventsOn(date);
    Event event = events.get(0);

    mockView.clearLog();

    EventEditData editData =
        new EventEditData(event, "Planning", date.atTime(10, 0), date.atTime(11, 0), "", "",
            "PUBLIC", "all");
    mockView.setEventEditInput(editData);

    controller.editEvents(date);

    assertTrue("Should display success",
        mockView.getLog().contains("Event(s) updated successfully!"));
  }

  @Test
  public void testEditSingleEventEndTimeChanged() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate date = LocalDate.of(2025, 5, 15);
    Event event =
        new CalendarEvent.EventBuilder().subject("Meeting").startDateTime(date.atTime(10, 0))
            .endDateTime(date.atTime(11, 0)).build();
    manager.getCurrentCalendar().addEvent(event);

    mockView.clearLog();

    EventEditData editData =
        new EventEditData(event, "Meeting", date.atTime(10, 0), date.atTime(12, 0), "", "",
            "PUBLIC", "single");
    mockView.setEventEditInput(editData);

    controller.editEvents(date);

    assertTrue("Should display success",
        mockView.getLog().contains("Event(s) updated successfully!"));

    List<Event> events = manager.getCurrentCalendar().getEventsOn(date);
    assertEquals("End time should be updated", 12, events.get(0).getEndDateTime().getHour());
  }

  @Test
  public void testDeleteEventThisRefreshesView() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate date = LocalDate.of(2025, 5, 15);
    Event event =
        new CalendarEvent.EventBuilder().subject("Meeting").startDateTime(date.atTime(10, 0))
            .endDateTime(date.atTime(11, 0)).build();
    manager.getCurrentCalendar().addEvent(event);

    mockView.clearLog();

    EventDeleteData deleteData = new EventDeleteData(event, "THIS");
    mockView.setEventDeleteInput(deleteData);

    controller.deleteEvent(date);

    assertTrue("Should refresh date view", mockView.getLog().contains("setEventsForDate: " + date));
    assertTrue("Should refresh month view", mockView.getLog().contains("setMonthEvents"));
  }

  @Test
  public void testMonthChangedExcludesEmptyDates() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate eventDate = LocalDate.of(2025, 5, 15);
    Event event =
        new CalendarEvent.EventBuilder().subject("Meeting").startDateTime(eventDate.atTime(10, 0))
            .endDateTime(eventDate.atTime(11, 0)).build();
    manager.getCurrentCalendar().addEvent(event);

    mockView.clearLog();

    controller.monthChanged(2025, 5);

    String log = mockView.getLog();
    assertTrue("Should set month events", log.contains("setMonthEvents"));
    assertTrue("Should include only 1 date", log.contains("with 1 dates"));
  }

  @Test
  public void testCreateAllDayEventPassesValidation() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");
    mockView.clearLog();

    LocalDate date = LocalDate.of(2025, 5, 15);


    EventFormData formData =
        new EventFormData("All Day Event", date, date, true, null, null, null, null, "PUBLIC",
            false, null, false, 0, null);
    mockView.setEventFormInput(formData);

    controller.createEvent(date);

    assertTrue("Should create successfully",
        mockView.getLog().contains("Event created successfully!"));
  }

  @Test
  public void testCreateRecurringEventWithZeroRepeatCount() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");
    mockView.clearLog();

    LocalDate date = LocalDate.of(2025, 5, 15);
    Set<DayOfWeek> weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.THURSDAY);

    EventFormData formData =
        new EventFormData("Standup", date, date, false, LocalTime.of(9, 0), LocalTime.of(10, 0),
            null, null, "PUBLIC", true, weekdays, true, 0, null);
    mockView.setEventFormInput(formData);

    controller.createEvent(date);

    assertTrue("Should display error for zero count", mockView.getLog().contains("displayError:"));
  }

  @Test
  public void testEditSingleEventNullLocationToEmpty() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate date = LocalDate.of(2025, 5, 15);
    // Event with NULL location (not empty string)
    Event event =
        new CalendarEvent.EventBuilder().subject("Meeting").startDateTime(date.atTime(10, 0))
            .endDateTime(date.atTime(11, 0)).build();
    manager.getCurrentCalendar().addEvent(event);

    mockView.clearLog();

    // Try to set location to empty string
    EventEditData editData =
        new EventEditData(event, "Meeting", date.atTime(10, 0), date.atTime(11, 0), "",
            // Empty string - should match the null→"" conversion
            "", "PUBLIC", "single");
    mockView.setEventEditInput(editData);

    controller.editEvents(date);

    assertTrue("Should succeed", mockView.getLog().contains("Event(s) updated successfully!"));

    // Location should still be null/empty (unchanged)
    List<Event> events = manager.getCurrentCalendar().getEventsOn(date);
    String location = events.get(0).getLocation();
    assertTrue("Location should be null or empty", location == null || location.isEmpty());
  }

  @Test
  public void testEditSingleEventNullDescriptionToEmpty() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate date = LocalDate.of(2025, 5, 15);
    // Event with NULL description
    Event event =
        new CalendarEvent.EventBuilder().subject("Meeting").startDateTime(date.atTime(10, 0))
            .endDateTime(date.atTime(11, 0)).build();
    manager.getCurrentCalendar().addEvent(event);

    mockView.clearLog();

    EventEditData editData =
        new EventEditData(event, "Meeting", date.atTime(10, 0), date.atTime(11, 0), "", "",
            // Empty description - should match null→""
            "PUBLIC", "single");
    mockView.setEventEditInput(editData);

    controller.editEvents(date);

    assertTrue("Should succeed", mockView.getLog().contains("Event(s) updated successfully!"));
  }

  @Test
  public void testEditSeriesNullLocationToEmpty() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate date = LocalDate.of(2025, 5, 15);
    Set<DayOfWeek> weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.THURSDAY);

    // Series with NULL location
    RecurringEventSeries series = new RecurringEventSeries.EventSeriesBuilder().subject("Standup")
        .startDateTime(date.atTime(9, 0)).endDateTime(date.atTime(10, 0)).weekdays(weekdays)
        .repeatCount(5).build();
    manager.getCurrentCalendar().addEventSeries(series);

    List<Event> events = manager.getCurrentCalendar().getEventsOn(date);
    Event event = events.get(0);

    mockView.clearLog();

    EventEditData editData =
        new EventEditData(event, "Standup", date.atTime(9, 0), date.atTime(10, 0), "",
            // Empty string matches null→""
            "", "PUBLIC", "all");
    mockView.setEventEditInput(editData);

    controller.editEvents(date);

    assertTrue("Should succeed", mockView.getLog().contains("Event(s) updated successfully!"));
  }

  @Test
  public void testEditSeriesNullDescriptionToEmpty() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate date = LocalDate.of(2025, 5, 15);
    Set<DayOfWeek> weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.THURSDAY);

    RecurringEventSeries series = new RecurringEventSeries.EventSeriesBuilder().subject("Review")
        .startDateTime(date.atTime(14, 0)).endDateTime(date.atTime(15, 0)).weekdays(weekdays)
        .repeatCount(5).build();
    manager.getCurrentCalendar().addEventSeries(series);

    List<Event> events = manager.getCurrentCalendar().getEventsOn(date);
    Event event = events.get(0);

    mockView.clearLog();

    EventEditData editData =
        new EventEditData(event, "Review", date.atTime(14, 0), date.atTime(15, 0), "", "",
            // Both empty
            "PUBLIC", "all");
    mockView.setEventEditInput(editData);

    controller.editEvents(date);

    assertTrue("Should succeed", mockView.getLog().contains("Event(s) updated successfully!"));
  }

  @Test
  public void testEditSeriesMultipleChanges() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate date = LocalDate.of(2025, 5, 15);
    Set<DayOfWeek> weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.THURSDAY);

    RecurringEventSeries series = new RecurringEventSeries.EventSeriesBuilder().subject("Review")
        .startDateTime(date.atTime(14, 0)).endDateTime(date.atTime(15, 0))
        .description("Old description").weekdays(weekdays).repeatCount(5).build();
    manager.getCurrentCalendar().addEventSeries(series);

    List<Event> events = manager.getCurrentCalendar().getEventsOn(date);
    Event event = events.get(0);

    mockView.clearLog();

    EventEditData editData =
        new EventEditData(event, "Review New", date.atTime(9, 0), date.atTime(10, 0), "",
            "New description",  // Changed description
            "PUBLIC", "all");
    mockView.setEventEditInput(editData);

    controller.editEvents(date);

    // VERIFY all events have new description
    LocalDate endSearch = date.plusWeeks(10);
    for (LocalDate d = date; !d.isAfter(endSearch); d = d.plusDays(1)) {
      List<Event> evts = manager.getCurrentCalendar().getEventsOn(d);
      for (Event e : evts) {
        assertEquals("Subject should be updated", "Review New", e.getSubject());
        assertEquals("Starttime updated", date.atTime(9, 0).toLocalTime(),
            e.getStartDateTime().toLocalTime());
        assertEquals("Endtime updated", date.atTime(10, 0).toLocalTime(),
            e.getEndDateTime().toLocalTime());
      }
    }
  }

  @Test
  public void testEditSeriesStatusActuallyChanges() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate date = LocalDate.of(2025, 5, 15);
    Set<DayOfWeek> weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.THURSDAY);

    RecurringEventSeries series = new RecurringEventSeries.EventSeriesBuilder().subject("Meeting")
        .startDateTime(date.atTime(10, 0)).endDateTime(date.atTime(11, 0)).status(Status.PUBLIC)
        .weekdays(weekdays).repeatCount(5).build();
    manager.getCurrentCalendar().addEventSeries(series);

    List<Event> events = manager.getCurrentCalendar().getEventsOn(date);
    Event event = events.get(0);

    mockView.clearLog();

    EventEditData editData =
        new EventEditData(event, "Meeting", date.atTime(10, 0), date.atTime(11, 0), "", "",
            "PRIVATE",  // Changed status
            "all");
    mockView.setEventEditInput(editData);

    controller.editEvents(date);

    // VERIFY all events have new status
    LocalDate endSearch = date.plusWeeks(10);
    for (LocalDate d = date; !d.isAfter(endSearch); d = d.plusDays(1)) {
      List<Event> evts = manager.getCurrentCalendar().getEventsOn(d);
      for (Event e : evts) {
        if ("Meeting".equals(e.getSubject())) {
          assertEquals("Status should be updated", Status.PRIVATE, e.getStatus());
        }
      }
    }
  }

  @Test
  public void testEditSeriesEndTimeActuallyChanges() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate date = LocalDate.of(2025, 5, 15);
    Set<DayOfWeek> weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.THURSDAY);

    RecurringEventSeries series = new RecurringEventSeries.EventSeriesBuilder().subject("Planning")
        .startDateTime(date.atTime(10, 0)).endDateTime(date.atTime(11, 0)).weekdays(weekdays)
        .repeatCount(5).build();
    manager.getCurrentCalendar().addEventSeries(series);

    List<Event> events = manager.getCurrentCalendar().getEventsOn(date);
    Event event = events.get(0);

    mockView.clearLog();

    EventEditData editData =
        new EventEditData(event, "Planning", date.atTime(10, 0), date.atTime(12, 0),
            // Changed end time
            "", "", "PUBLIC", "all");
    mockView.setEventEditInput(editData);

    controller.editEvents(date);

    // VERIFY all events have new end time
    LocalDate endSearch = date.plusWeeks(10);
    for (LocalDate d = date; !d.isAfter(endSearch); d = d.plusDays(1)) {
      List<Event> evts = manager.getCurrentCalendar().getEventsOn(d);
      for (Event e : evts) {
        if ("Planning".equals(e.getSubject())) {
          assertEquals("End time should be updated", 12, e.getEndDateTime().getHour());
        }
      }
    }
  }

  @Test
  public void testEditSingleEventEndTimeUpdatesReference() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate date = LocalDate.of(2025, 5, 15);
    Event originalEvent =
        new CalendarEvent.EventBuilder().subject("Meeting").startDateTime(date.atTime(10, 0))
            .endDateTime(date.atTime(11, 0)).build();
    manager.getCurrentCalendar().addEvent(originalEvent);

    mockView.clearLog();

    EventEditData editData =
        new EventEditData(originalEvent, "Meeting", date.atTime(10, 0), date.atTime(12, 30),
            // Changed end time
            "", "", "PUBLIC", "single");
    mockView.setEventEditInput(editData);

    controller.editEvents(date);

    assertTrue("Should succeed", mockView.getLog().contains("Event(s) updated successfully!"));

    // Verify the actual event has new end time
    List<Event> events = manager.getCurrentCalendar().getEventsOn(date);
    assertEquals("Should have one event", 1, events.size());
    assertEquals("End hour should be 12", 12, events.get(0).getEndDateTime().getHour());
    assertEquals("End minute should be 30", 30, events.get(0).getEndDateTime().getMinute());
  }

  @Test
  public void testValidationReturnsErrorForBadData() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");
    mockView.clearLog();

    LocalDate date = LocalDate.of(2025, 5, 15);

    // Missing subject - should fail validation
    EventFormData formData = new EventFormData("",  // Empty subject
        date, date, false, LocalTime.of(10, 0), LocalTime.of(11, 0), null, null, "PUBLIC", false,
        null, false, 0, null);
    mockView.setEventFormInput(formData);

    controller.createEvent(date);

    assertTrue("Should display error", mockView.getLog().contains("displayError:"));
    assertFalse("Should not create event",
        mockView.getLog().contains("Event created successfully!"));
  }

  @Test(expected = IllegalStateException.class)
  public void testEditAllEventsInSeriesWithNoCurrentCalendar() {
    manager.editAllEventsInSeries("Meeting", LocalDateTime.of(2025, 5, 15, 10, 0),
        LocalDateTime.of(2025, 5, 15, 11, 0), "subject", "New Meeting");
  }

  @Test
  public void testEditAllEventsInSeriesWithCurrentCalendar() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    Set<DayOfWeek> weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.TUESDAY);

    RecurringEventSeries series = new RecurringEventSeries.EventSeriesBuilder().subject("Review")
        .startDateTime(LocalDateTime.of(2025, 5, 6, 14, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 6, 15, 0)).weekdays(weekdays).repeatCount(4).build();
    manager.addEventSeries(series);

    List<Event> events = manager.getEventsOn(LocalDate.of(2025, 5, 6));
    Event firstEvent = events.get(0);

    manager.editAllEventsInSeries("Review", firstEvent.getStartDateTime(),
        firstEvent.getEndDateTime(), "subject", "Updated Review");

    List<Event> allEvents = manager.getEventsInRange(LocalDateTime.of(2025, 5, 1, 0, 0),
        LocalDateTime.of(2025, 6, 1, 0, 0));

    for (Event e : allEvents) {
      assertEquals("Updated Review", e.getSubject());
    }
  }

  @Test(expected = IllegalStateException.class)
  public void testAddEventWithNoCurrentCalendar() {
    Event event = new CalendarEvent.EventBuilder().subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 15, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 15, 11, 0)).build();

    manager.addEvent(event);
  }

  @Test
  public void testAddEventWithCurrentCalendar() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    Event event = new CalendarEvent.EventBuilder().subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 15, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 15, 11, 0)).build();

    manager.addEvent(event);

    List<Event> events = manager.getEventsOn(LocalDate.of(2025, 5, 15));
    assertEquals(1, events.size());
    assertEquals("Meeting", events.get(0).getSubject());
  }

  @Test(expected = IllegalStateException.class)
  public void testAddEventSeriesWithNoCurrentCalendar() {
    Set<DayOfWeek> weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.MONDAY);

    RecurringEventSeries series = new RecurringEventSeries.EventSeriesBuilder().subject("Standup")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 9, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 10, 0)).weekdays(weekdays).repeatCount(5).build();

    manager.addEventSeries(series);
  }

  @Test
  public void testAddEventSeriesWithCurrentCalendar() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    Set<DayOfWeek> weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.MONDAY);

    RecurringEventSeries series = new RecurringEventSeries.EventSeriesBuilder().subject("Standup")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 9, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 10, 0)).weekdays(weekdays).repeatCount(5).build();

    manager.addEventSeries(series);

    List<Event> events = manager.getEventsOn(LocalDate.of(2025, 5, 5));
    assertTrue(events.size() > 0);
    assertEquals("Standup", events.get(0).getSubject());
  }

  @Test
  public void testValidateEventFormDataWithNullSubject() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");
    mockView.clearLog();

    LocalDate date = LocalDate.of(2025, 5, 15);

    EventFormData formData =
        new EventFormData(null, date, date, false, LocalTime.of(10, 0), LocalTime.of(11, 0), null,
            null, "PUBLIC", false, null, false, 0, null);
    mockView.setEventFormInput(formData);

    controller.createEvent(date);

    assertTrue(mockView.getLog().contains("displayError: Subject cannot be empty"));
  }

  @Test
  public void testValidateEventFormDataWithEmptySubject() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");
    mockView.clearLog();

    LocalDate date = LocalDate.of(2025, 5, 15);

    EventFormData formData =
        new EventFormData("   ", date, date, false, LocalTime.of(10, 0), LocalTime.of(11, 0), null,
            null, "PUBLIC", false, null, false, 0, null);
    mockView.setEventFormInput(formData);

    controller.createEvent(date);

    assertTrue(mockView.getLog().contains("displayError: Subject cannot be empty"));
  }

  @Test
  public void testValidateEventFormDataWithEndDateBeforeStartDate() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");
    mockView.clearLog();

    LocalDate startDate = LocalDate.of(2025, 5, 15);
    LocalDate endDate = LocalDate.of(2025, 5, 14);

    EventFormData formData =
        new EventFormData("Meeting", startDate, endDate, false, LocalTime.of(10, 0),
            LocalTime.of(11, 0), null, null, "PUBLIC", false, null, false, 0, null);
    mockView.setEventFormInput(formData);

    controller.createEvent(startDate);

    assertTrue(mockView.getLog().contains("displayError: End date cannot be before start date"));
  }

  @Test
  public void testValidateEventTimingWithEndTimeBeforeStartTimeSameDay() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");
    mockView.clearLog();

    LocalDate date = LocalDate.of(2025, 5, 15);

    EventFormData formData =
        new EventFormData("Meeting", date, date, false, LocalTime.of(14, 0), LocalTime.of(10, 0),
            null, null, "PUBLIC", false, null, false, 0, null);
    mockView.setEventFormInput(formData);

    controller.createEvent(date);

    assertTrue(mockView.getLog()
        .contains("displayError: " + "End time must be after start time for same-day events"));
  }

  @Test
  public void testValidateEventTimingWithEndTimeEqualToStartTimeSameDay() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");
    mockView.clearLog();

    LocalDate date = LocalDate.of(2025, 5, 15);

    EventFormData formData =
        new EventFormData("Meeting", date, date, false, LocalTime.of(10, 0), LocalTime.of(10, 0),
            null, null, "PUBLIC", false, null, false, 0, null);
    mockView.setEventFormInput(formData);

    controller.createEvent(date);

    assertTrue(mockView.getLog()
        .contains("displayError: " + "End time must be after start time for same-day events"));
  }

  @Test
  public void testValidateRecurrenceDataWithNullWeekdays() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");
    mockView.clearLog();

    LocalDate date = LocalDate.of(2025, 5, 15);

    EventFormData formData =
        new EventFormData("Meeting", date, date, false, LocalTime.of(10, 0), LocalTime.of(11, 0),
            null, null, "PUBLIC", true, null, true, 5, null);
    mockView.setEventFormInput(formData);

    controller.createEvent(date);

    assertTrue(mockView.getLog()
        .contains("displayError: " + "Please select at least one weekday for recurring events"));
  }

  @Test
  public void testValidateRecurrenceDataWithEmptyWeekdays() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");
    mockView.clearLog();

    LocalDate date = LocalDate.of(2025, 5, 15);

    EventFormData formData =
        new EventFormData("Meeting", date, date, false, LocalTime.of(10, 0), LocalTime.of(11, 0),
            null, null, "PUBLIC", true, new HashSet<>(), true, 5, null);
    mockView.setEventFormInput(formData);

    controller.createEvent(date);

    assertTrue(mockView.getLog()
        .contains("displayError: " + "Please select at least one weekday for recurring events"));
  }

  @Test
  public void testValidateEditEventDataWithEmptySubject() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate date = LocalDate.of(2025, 5, 15);
    Event event =
        new CalendarEvent.EventBuilder().subject("Meeting").startDateTime(date.atTime(10, 0))
            .endDateTime(date.atTime(11, 0)).build();
    manager.addEvent(event);

    mockView.clearLog();

    EventEditData editData =
        new EventEditData(event, "   ", date.atTime(10, 0), date.atTime(11, 0), "", "", "PUBLIC",
            "single");
    mockView.setEventEditInput(editData);

    controller.editEvents(date);

    assertTrue(mockView.getLog().contains("displayError: Subject cannot be empty"));
  }

  @Test
  public void testValidateEditEventDataWithEndBeforeStart() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");

    LocalDate date = LocalDate.of(2025, 5, 15);
    Event event =
        new CalendarEvent.EventBuilder().subject("Meeting").startDateTime(date.atTime(10, 0))
            .endDateTime(date.atTime(11, 0)).build();
    manager.addEvent(event);

    mockView.clearLog();

    EventEditData editData =
        new EventEditData(event, "Meeting", date.atTime(14, 0), date.atTime(10, 0), "", "",
            "PUBLIC", "single");
    mockView.setEventEditInput(editData);

    controller.editEvents(date);

    assertTrue(mockView.getLog()
        .contains("displayError: " + "End date/time cannot be before start date/time"));
  }

  @Test
  public void testValidateRecurrenceDataWithZeroRepeatCount() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");
    mockView.clearLog();

    LocalDate date = LocalDate.of(2025, 5, 15);
    Set<DayOfWeek> weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.MONDAY);

    EventFormData formData = new EventFormData(
        "Meeting",
        date, date,
        false,
        LocalTime.of(10, 0),
        LocalTime.of(11, 0),
        null,
        null,
        "PUBLIC",
        true,
        weekdays,
        true,
        0,
        null
    );
    mockView.setEventFormInput(formData);

    controller.createEvent(date);

    assertTrue(mockView.getLog().contains("displayError: Repeat count must be positive"));
  }

  @Test
  public void testValidateRecurrenceDataWithNegativeRepeatCount() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");
    mockView.clearLog();

    LocalDate date = LocalDate.of(2025, 5, 15);
    Set<DayOfWeek> weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.MONDAY);

    EventFormData formData = new EventFormData(
        "Meeting",
        date, date,
        false,
        LocalTime.of(10, 0),
        LocalTime.of(11, 0),
        null,
        null,
        "PUBLIC",
        true,
        weekdays,
        true,
        -5,
        null
    );
    mockView.setEventFormInput(formData);

    controller.createEvent(date);

    assertTrue(mockView.getLog().contains("displayError: Repeat count must be positive"));
  }

  @Test
  public void testValidateRecurrenceDataWithRepeatUntilBeforeStartDate() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");
    mockView.clearLog();

    LocalDate startDate = LocalDate.of(2025, 5, 15);
    LocalDate repeatUntil = LocalDate.of(2025, 5, 10);
    Set<DayOfWeek> weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.MONDAY);

    EventFormData formData = new EventFormData(
        "Meeting",
        startDate, startDate,
        false,
        LocalTime.of(10, 0),
        LocalTime.of(11, 0),
        null,
        null,
        "PUBLIC",
        true,
        weekdays,
        false,
        0,
        repeatUntil
    );
    mockView.setEventFormInput(formData);

    controller.createEvent(startDate);

    assertTrue(mockView.getLog().contains("displayError:"
        + " Repeat until date must be after start date"));
  }

  @Test
  public void testValidateRecurrenceDataWithRepeatUntilEqualToStartDate() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");
    mockView.clearLog();

    LocalDate date = LocalDate.of(2025, 5, 15);
    Set<DayOfWeek> weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.MONDAY);

    EventFormData formData = new EventFormData(
        "Meeting",
        date, date,
        false,
        LocalTime.of(10, 0),
        LocalTime.of(11, 0),
        null,
        null,
        "PUBLIC",
        true,
        weekdays,
        false,
        0,
        date
    );
    mockView.setEventFormInput(formData);

    controller.createEvent(date);

    assertTrue(mockView.getLog().contains("displayError: "
        + "Repeat until date must be after start date"));
  }

  @Test
  public void testValidateEventTimingWithNullStartTime() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");
    mockView.clearLog();

    LocalDate date = LocalDate.of(2025, 5, 15);

    EventFormData formData = new EventFormData(
        "Meeting",
        date, date,
        false,
        null,
        LocalTime.of(11, 0),
        null,
        null,
        "PUBLIC", false,
        null,
        false,
        0,
        null
    );
    mockView.setEventFormInput(formData);

    controller.createEvent(date);

    assertFalse(mockView.getLog().contains("displayError: "
        + "End time must be after start time"));
  }

  @Test
  public void testValidateEventTimingWithNullEndTime() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");
    mockView.clearLog();

    LocalDate date = LocalDate.of(2025, 5, 15);

    EventFormData formData = new EventFormData(
        "Meeting",
        date, date,
        false,
        LocalTime.of(10, 0),
        null,
        null,
        null,
        "PUBLIC", false,
        null,
        false,
        0,
        null
    );
    mockView.setEventFormInput(formData);

    controller.createEvent(date);

    assertFalse(mockView.getLog().contains("displayError:"
        + " End time must be after start time"));
  }

  @Test
  public void testValidateEventTimingWithDifferentDates() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");
    mockView.clearLog();

    LocalDate startDate = LocalDate.of(2025, 5, 15);
    LocalDate endDate = LocalDate.of(2025, 5, 16);

    EventFormData formData = new EventFormData(
        "Conference",
        startDate, endDate,
        false,
        LocalTime.of(14, 0),
        LocalTime.of(10, 0),
        null,
        null,
        "PUBLIC", false,
        null,
        false,
        0,
        null
    );
    mockView.setEventFormInput(formData);

    controller.createEvent(startDate);

    assertTrue(mockView.getLog().contains("displayMessage: "
        + "Event created successfully!"));
  }

  @Test
  public void testValidateRecurrenceDataWithNullRepeatUntil() {
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.useCalendar("Work");
    mockView.clearLog();

    LocalDate date = LocalDate.of(2025, 5, 15);
    Set<DayOfWeek> weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.MONDAY);

    EventFormData formData = new EventFormData(
        "Meeting",
        date, date,
        false,
        LocalTime.of(10, 0),
        LocalTime.of(11, 0),
        null,
        null,
        "PUBLIC",
        true,
        weekdays,
        false,
        0,
        null
    );
    mockView.setEventFormInput(formData);

    controller.createEvent(date);

    assertFalse(mockView.getLog().contains("displayError: "
        + "Repeat until date must be after start date"));
  }
}
