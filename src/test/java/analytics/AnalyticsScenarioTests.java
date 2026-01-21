package analytics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import calendar.controller.Command;
import calendar.controller.Features;
import calendar.controller.GuiController;
import calendar.controller.commands.ShowDashboardCommand;
import calendar.controller.parser.QueryParser;
import calendar.model.CalendarAnalyticsSummary;
import calendar.model.CalendarEvent;
import calendar.model.CalendarManager;
import calendar.model.Event;
import calendar.model.SimpleCalendar;
import calendar.model.SimpleCalendarManager;
import calendar.view.CalendarEditData;
import calendar.view.CalendarGuiView;
import calendar.view.EventDeleteData;
import calendar.view.EventEditData;
import calendar.view.EventFormData;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

/**
 * Comprehensive scenario tests for calendar analytics functionality.
 * Combines tests for:
 * 1. CalendarAnalyticsSummary correctness (metrics verification)
 * 2. Command parsing ("show calendar dashboard from start to end")
 * 3. Adapter-level logic (success and failure scenarios)
 */
public class AnalyticsScenarioTests {
  private SimpleCalendarManager manager;
  private TestGuiViewStub view;
  private GuiController controller;
  private SimpleCalendar calendar;

  /**
   * Sets up the test fixture before each test.
   * Initializes the manager, view, controller, and calendar instances.
   */
  @Before
  public void setUp() {
    manager = new SimpleCalendarManager();
    view = new TestGuiViewStub();
    controller = new GuiController(manager, view);
    calendar = new SimpleCalendar();
  }

  /**
   * Tests total events count with multiple events.
   */
  @Test
  public void testTotalEventsCount() {
    for (int i = 1; i <= 5; i++) {
      CalendarEvent event = new CalendarEvent.EventBuilder()
          .subject("Event " + i)
          .startDateTime(LocalDateTime.of(2024, 1, i, 10, 0))
          .endDateTime(LocalDateTime.of(2024, 1, i, 11, 0))
          .build();
      calendar.addEvent(event);
    }

    LocalDate startDate = LocalDate.of(2024, 1, 1);
    LocalDate endDate = LocalDate.of(2024, 1, 31);
    CalendarAnalyticsSummary summary = calendar.generateAnalytics(startDate, endDate);

    assertEquals("Should count 5 total events", 5, summary.getTotalEvents());
  }

  /**
   * Tests total events count is zero when no events in range.
   */
  @Test
  public void testTotalEventsCountEmpty() {
    LocalDate startDate = LocalDate.of(2024, 1, 1);
    LocalDate endDate = LocalDate.of(2024, 1, 31);
    CalendarAnalyticsSummary summary = calendar.generateAnalytics(startDate, endDate);

    assertEquals("Should count 0 events when calendar is empty", 0, summary.getTotalEvents());
  }

  /**
   * Tests events grouped by subject.
   */
  @Test
  public void testEventsBySubject() {
    CalendarEvent meeting1 = new CalendarEvent.EventBuilder()
        .subject("Team Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 5, 11, 0))
        .build();

    CalendarEvent meeting2 = new CalendarEvent.EventBuilder()
        .subject("Team Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 10, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 10, 11, 0))
        .build();

    CalendarEvent standup = new CalendarEvent.EventBuilder()
        .subject("Daily Standup")
        .startDateTime(LocalDateTime.of(2024, 1, 15, 9, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 15, 9, 30))
        .build();

    calendar.addEvent(meeting1);
    calendar.addEvent(meeting2);
    calendar.addEvent(standup);

    LocalDate startDate = LocalDate.of(2024, 1, 1);
    LocalDate endDate = LocalDate.of(2024, 1, 31);
    CalendarAnalyticsSummary summary = calendar.generateAnalytics(startDate, endDate);

    Map<String, Integer> bySubject = summary.getEventsBySubject();

    assertEquals("Should have 2 Team Meetings", Integer.valueOf(2), bySubject.get("Team Meeting"));
    assertEquals("Should have 1 Daily Standup", Integer.valueOf(1), bySubject.get("Daily Standup"));
  }

  /**
   * Tests events grouped by weekday.
   */
  @Test
  public void testEventsByWeekday() {
    CalendarEvent monday1 = new CalendarEvent.EventBuilder()
        .subject("Monday Event 1")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .build();

    CalendarEvent monday2 = new CalendarEvent.EventBuilder()
        .subject("Monday Event 2")
        .startDateTime(LocalDateTime.of(2024, 1, 8, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 8, 11, 0))
        .build();

    CalendarEvent wednesday = new CalendarEvent.EventBuilder()
        .subject("Wednesday Event")
        .startDateTime(LocalDateTime.of(2024, 1, 3, 14, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 3, 15, 0))
        .build();

    calendar.addEvent(monday1);
    calendar.addEvent(monday2);
    calendar.addEvent(wednesday);

    LocalDate startDate = LocalDate.of(2024, 1, 1);
    LocalDate endDate = LocalDate.of(2024, 1, 31);
    CalendarAnalyticsSummary summary = calendar.generateAnalytics(startDate, endDate);

    Map<DayOfWeek, Integer> byWeekday = summary.getEventsByWeekday();

    assertEquals("Monday should have 2 events", Integer.valueOf(2),
        byWeekday.get(DayOfWeek.MONDAY));
    assertEquals("Wednesday should have 1 event", Integer.valueOf(1),
        byWeekday.get(DayOfWeek.WEDNESDAY));
  }

  /**
   * Tests events grouped by week index.
   */
  @Test
  public void testEventsByWeekIndex() {
    CalendarEvent week1Event = new CalendarEvent.EventBuilder()
        .subject("Week 1 Event")
        .startDateTime(LocalDateTime.of(2024, 1, 3, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 3, 11, 0))
        .build();

    CalendarEvent week2Event1 = new CalendarEvent.EventBuilder()
        .subject("Week 2 Event 1")
        .startDateTime(LocalDateTime.of(2024, 1, 10, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 10, 11, 0))
        .build();

    CalendarEvent week2Event2 = new CalendarEvent.EventBuilder()
        .subject("Week 2 Event 2")
        .startDateTime(LocalDateTime.of(2024, 1, 11, 14, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 11, 15, 0))
        .build();

    calendar.addEvent(week1Event);
    calendar.addEvent(week2Event1);
    calendar.addEvent(week2Event2);

    LocalDate startDate = LocalDate.of(2024, 1, 1);
    LocalDate endDate = LocalDate.of(2024, 1, 31);
    CalendarAnalyticsSummary summary = calendar.generateAnalytics(startDate, endDate);

    Map<Integer, Integer> byWeekIndex = summary.getEventsByWeekIndex();

    assertTrue("Week 1 should have events", byWeekIndex.containsKey(1));
    assertTrue("Week 2 should have events", byWeekIndex.containsKey(2));
  }

  /**
   * Tests events grouped by month.
   */
  @Test
  public void testEventsByMonth() {
    CalendarEvent jan1 = new CalendarEvent.EventBuilder()
        .subject("January Event 1")
        .startDateTime(LocalDateTime.of(2024, 1, 15, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 15, 11, 0))
        .build();

    CalendarEvent jan2 = new CalendarEvent.EventBuilder()
        .subject("January Event 2")
        .startDateTime(LocalDateTime.of(2024, 1, 20, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 20, 11, 0))
        .build();

    CalendarEvent feb1 = new CalendarEvent.EventBuilder()
        .subject("February Event")
        .startDateTime(LocalDateTime.of(2024, 2, 5, 14, 0))
        .endDateTime(LocalDateTime.of(2024, 2, 5, 15, 0))
        .build();

    calendar.addEvent(jan1);
    calendar.addEvent(jan2);
    calendar.addEvent(feb1);

    LocalDate startDate = LocalDate.of(2024, 1, 1);
    LocalDate endDate = LocalDate.of(2024, 2, 29);
    CalendarAnalyticsSummary summary = calendar.generateAnalytics(startDate, endDate);

    Map<YearMonth, Integer> byMonth = summary.getEventsByMonth();

    assertEquals("January should have 2 events", Integer.valueOf(2),
        byMonth.get(YearMonth.of(2024, 1)));
    assertEquals("February should have 1 event", Integer.valueOf(1),
        byMonth.get(YearMonth.of(2024, 2)));
  }

  /**
   * Tests busiest day calculation.
   */
  @Test
  public void testBusiestDay() {
    LocalDate busiestDate = LocalDate.of(2024, 1, 15);

    for (int i = 0; i < 3; i++) {
      CalendarEvent event = new CalendarEvent.EventBuilder()
          .subject("Event " + i)
          .startDateTime(busiestDate.atTime(10 + i, 0))
          .endDateTime(busiestDate.atTime(11 + i, 0))
          .build();
      calendar.addEvent(event);
    }

    CalendarEvent otherEvent = new CalendarEvent.EventBuilder()
        .subject("Other Event")
        .startDateTime(LocalDateTime.of(2024, 1, 10, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 10, 11, 0))
        .build();
    calendar.addEvent(otherEvent);

    LocalDate startDate = LocalDate.of(2024, 1, 1);
    LocalDate endDate = LocalDate.of(2024, 1, 31);
    CalendarAnalyticsSummary summary = calendar.generateAnalytics(startDate, endDate);

    assertEquals("Busiest day should be Jan 15", busiestDate, summary.getBusiestDay());
  }

  /**
   * Tests least busy day calculation.
   */
  @Test
  public void testLeastBusyDay() {
    LocalDate leastBusyDate = LocalDate.of(2024, 1, 5);

    CalendarEvent leastBusyEvent = new CalendarEvent.EventBuilder()
        .subject("Least Busy Event")
        .startDateTime(leastBusyDate.atTime(10, 0))
        .endDateTime(leastBusyDate.atTime(11, 0))
        .build();
    calendar.addEvent(leastBusyEvent);

    for (int i = 0; i < 3; i++) {
      CalendarEvent event = new CalendarEvent.EventBuilder()
          .subject("Event " + i)
          .startDateTime(LocalDateTime.of(2024, 1, 15, 10 + i, 0))
          .endDateTime(LocalDateTime.of(2024, 1, 15, 11 + i, 0))
          .build();
      calendar.addEvent(event);
    }

    LocalDate startDate = LocalDate.of(2024, 1, 1);
    LocalDate endDate = LocalDate.of(2024, 1, 31);
    CalendarAnalyticsSummary summary = calendar.generateAnalytics(startDate, endDate);

    assertEquals("Least busy day should be Jan 5", leastBusyDate, summary.getLeastBusyDay());
  }

  /**
   * Tests online/offline event classification.
   */
  @Test
  public void testOnlineOfflineClassification() {
    CalendarEvent onlineEvent1 = new CalendarEvent.EventBuilder()
        .subject("Online Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 5, 11, 0))
        .location("online")
        .build();

    CalendarEvent onlineEvent2 = new CalendarEvent.EventBuilder()
        .subject("Virtual Standup")
        .startDateTime(LocalDateTime.of(2024, 1, 10, 9, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 10, 9, 30))
        .location("ONLINE")
        .build();

    CalendarEvent offlineEvent = new CalendarEvent.EventBuilder()
        .subject("In-Person Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 15, 14, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 15, 15, 0))
        .location("Conference Room A")
        .build();

    calendar.addEvent(onlineEvent1);
    calendar.addEvent(onlineEvent2);
    calendar.addEvent(offlineEvent);

    LocalDate startDate = LocalDate.of(2024, 1, 1);
    LocalDate endDate = LocalDate.of(2024, 1, 31);
    CalendarAnalyticsSummary summary = calendar.generateAnalytics(startDate, endDate);

    assertEquals("Should have 2 online events", 2, summary.getOnlineEventsCount());
    assertEquals("Should have 1 offline event", 1, summary.getOfflineEventsCount());
  }

  /**
   * Tests average events per day calculation.
   */
  @Test
  public void testAverageEventsPerDay() {
    for (int day = 1; day <= 5; day++) {
      for (int event = 0; event < 2; event++) {
        CalendarEvent e = new CalendarEvent.EventBuilder()
            .subject("Event " + event)
            .startDateTime(LocalDateTime.of(2024, 1, day, 10 + event, 0))
            .endDateTime(LocalDateTime.of(2024, 1, day, 11 + event, 0))
            .build();
        calendar.addEvent(e);
      }
    }

    LocalDate startDate = LocalDate.of(2024, 1, 1);
    LocalDate endDate = LocalDate.of(2024, 1, 31);
    CalendarAnalyticsSummary summary = calendar.generateAnalytics(startDate, endDate);

    double expected = 10.0 / 31.0;
    assertEquals("Average should be 10 events / 31 days", expected,
        summary.getAverageEventsPerDay(), 0.001);
  }

  /**
   * Tests average events per day when no events exist.
   */
  @Test
  public void testAverageEventsPerDayEmpty() {
    LocalDate startDate = LocalDate.of(2024, 1, 1);
    LocalDate endDate = LocalDate.of(2024, 1, 31);
    CalendarAnalyticsSummary summary = calendar.generateAnalytics(startDate, endDate);

    assertEquals("Average should be 0 when no events", 0.0,
        summary.getAverageEventsPerDay(), 0.001);
  }

  /**
   * Tests that events outside date range are excluded.
   */
  @Test
  public void testEventsOutsideRangeExcluded() {
    CalendarEvent before = new CalendarEvent.EventBuilder()
        .subject("Before Range")
        .startDateTime(LocalDateTime.of(2023, 12, 15, 10, 0))
        .endDateTime(LocalDateTime.of(2023, 12, 15, 11, 0))
        .build();

    CalendarEvent inRange = new CalendarEvent.EventBuilder()
        .subject("In Range")
        .startDateTime(LocalDateTime.of(2024, 1, 15, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 15, 11, 0))
        .build();

    CalendarEvent after = new CalendarEvent.EventBuilder()
        .subject("After Range")
        .startDateTime(LocalDateTime.of(2024, 2, 15, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 2, 15, 11, 0))
        .build();

    calendar.addEvent(before);
    calendar.addEvent(inRange);
    calendar.addEvent(after);

    LocalDate startDate = LocalDate.of(2024, 1, 1);
    LocalDate endDate = LocalDate.of(2024, 1, 31);
    CalendarAnalyticsSummary summary = calendar.generateAnalytics(startDate, endDate);

    assertEquals("Should only count event in range", 1, summary.getTotalEvents());
  }

  /**
   * Tests analytics with edge case: single-day range.
   */
  @Test
  public void testAnalyticsSingleDayRange() {
    CalendarEvent event = new CalendarEvent.EventBuilder()
        .subject("Single Day Event")
        .startDateTime(LocalDateTime.of(2024, 1, 15, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 15, 11, 0))
        .build();

    calendar.addEvent(event);

    LocalDate singleDay = LocalDate.of(2024, 1, 15);
    CalendarAnalyticsSummary summary = calendar.generateAnalytics(singleDay, singleDay);

    assertEquals("Should count 1 event on single day", 1, summary.getTotalEvents());
    assertEquals("Average should be 1.0 for single day", 1.0,
        summary.getAverageEventsPerDay(), 0.001);
  }

  /**
   * Tests correct parsing of valid dashboard command.
   */
  @Test
  public void testDashboardCommandValidParsing() {
    QueryParser parser = new QueryParser();
    String command = "show calendar dashboard from 2024-01-01 to 2024-01-31";

    Command result = parser.parseCommand(command);

    assertNotNull("Command should not be null", result);
    assertTrue("Should return ShowDashboardCommand",
        result instanceof ShowDashboardCommand);
  }

  /**
   * Tests dashboard command with valid dates calls dashboard method.
   */
  @Test
  public void testDashboardCommandValidDatesCallsMethod() {
    manager.createCalendar("TestCal", ZoneId.of("America/New_York"));
    manager.useCalendar("TestCal");

    LocalDate startDate = LocalDate.of(2024, 1, 1);
    LocalDate endDate = LocalDate.of(2024, 12, 31);

    controller.showDashboard(startDate, endDate);

    assertTrue("Dashboard should be displayed", view.dashboardDisplayed);
    assertNotNull("Summary should not be null", view.displayedSummary);
  }

  /**
   * Tests dashboard command with invalid date format produces error.
   */
  @Test
  public void testDashboardCommandInvalidDateFormat() {
    QueryParser parser = new QueryParser();
    String command = "show calendar dashboard from 2024-13-01 to 2024-12-31";

    try {
      parser.parseCommand(command);
      fail("Should throw IllegalArgumentException for invalid month");
    } catch (IllegalArgumentException e) {
      assertTrue("Exception message should mention date format",
          e.getMessage().contains("Invalid date format"));
    }
  }

  /**
   * Tests dashboard command with invalid date (e.g., Feb 30) produces error.
   */
  @Test
  public void testDashboardCommandInvalidDate() {
    QueryParser parser = new QueryParser();
    String command = "show calendar dashboard from 2024-02-30 to 2024-03-01";

    try {
      parser.parseCommand(command);
      fail("Should throw IllegalArgumentException for invalid day");
    } catch (IllegalArgumentException e) {
      assertTrue("Exception message should mention date format",
          e.getMessage().contains("Invalid date format"));
    }
  }

  /**
   * Tests dashboard command with start date after end date produces error.
   */
  @Test
  public void testDashboardCommandStartAfterEnd() {
    manager.createCalendar("TestCal", ZoneId.of("America/New_York"));
    manager.useCalendar("TestCal");

    LocalDate startDate = LocalDate.of(2024, 12, 31);
    LocalDate endDate = LocalDate.of(2024, 1, 1);

    controller.showDashboard(startDate, endDate);

    assertEquals("Error message should indicate invalid date order",
        "End date must not be before start date.", view.lastError);
    assertTrue("Dashboard should not be displayed", !view.dashboardDisplayed);
  }

  /**
   * Tests dashboard command when no calendar is in use throws error.
   */
  @Test
  public void testDashboardCommandNoActiveCalendar() {
    CalendarManager newManager = new SimpleCalendarManager();
    TestGuiViewStub newView = new TestGuiViewStub();
    GuiController newController = new GuiController(newManager, newView);

    LocalDate startDate = LocalDate.of(2024, 1, 1);
    LocalDate endDate = LocalDate.of(2024, 1, 31);

    newController.showDashboard(startDate, endDate);

    assertNotNull("Should display error", newView.lastError);
    assertTrue("Dashboard should not be displayed", !newView.dashboardDisplayed);
  }

  /**
   * Tests dashboard command with wrong format is rejected by parser.
   */
  @Test
  public void testDashboardCommandWrongFormatMissingFrom() {
    QueryParser parser = new QueryParser();
    String command = "show calendar dashboard 2024-01-01 to 2024-01-31";

    Command result = parser.parseCommand(command);

    assertTrue("Should not return ShowDashboardCommand for invalid syntax",
        !(result instanceof ShowDashboardCommand));
  }

  /**
   * Tests dashboard command with wrong format is rejected - missing 'to'.
   */
  @Test
  public void testDashboardCommandWrongFormatMissingTo() {
    QueryParser parser = new QueryParser();
    String command = "show calendar dashboard from 2024-01-01 2024-01-31";

    try {
      parser.parseCommand(command);
      fail("Should throw IllegalArgumentException for missing 'to'");
    } catch (IllegalArgumentException e) {
      assertTrue("Exception should mention invalid syntax",
          e.getMessage().contains("Invalid dashboard syntax"));
    }
  }

  /**
   * Tests dashboard command with missing end date is rejected.
   */
  @Test
  public void testDashboardCommandMissingEndDate() {
    QueryParser parser = new QueryParser();
    String command = "show calendar dashboard from 2024-01-01 to";

    try {
      parser.parseCommand(command);
      fail("Should throw IllegalArgumentException for missing end date");
    } catch (IllegalArgumentException e) {
      assertNotNull("Should have exception message", e.getMessage());
    }
  }

  /**
   * Tests dashboard command with missing start date is rejected.
   */
  @Test
  public void testDashboardCommandMissingStartDate() {
    QueryParser parser = new QueryParser();
    String command = "show calendar dashboard from to 2024-01-31";

    try {
      parser.parseCommand(command);
      fail("Should throw IllegalArgumentException for missing start date");
    } catch (IllegalArgumentException e) {
      assertNotNull("Should have exception message", e.getMessage());
    }
  }

  /**
   * Tests dashboard command with same start and end date.
   */
  @Test
  public void testDashboardCommandSameDate() {
    QueryParser parser = new QueryParser();
    String command = "show calendar dashboard from 2024-06-15 to 2024-06-15";

    Command result = parser.parseCommand(command);

    assertNotNull("Command should not be null", result);
    assertTrue("Should return ShowDashboardCommand",
        result instanceof ShowDashboardCommand);
  }

  /**
   * Tests that dashboard command produces output when successful.
   */
  @Test
  public void testDashboardCommandProducesOutput() {
    manager.createCalendar("TestCal", ZoneId.of("America/New_York"));
    manager.useCalendar("TestCal");

    LocalDate startDate = LocalDate.of(2024, 1, 1);
    LocalDate endDate = LocalDate.of(2024, 1, 31);

    controller.showDashboard(startDate, endDate);

    assertTrue("Dashboard should be displayed", view.dashboardDisplayed);
    assertNotNull("Should have summary", view.displayedSummary);
  }

  /**
   * Tests that dashboard fails when no calendar is in use.
   */
  @Test
  public void testDashboardFailsWithNoCalendar() {
    CalendarManager newManager = new SimpleCalendarManager();
    TestGuiViewStub newView = new TestGuiViewStub();
    GuiController newController = new GuiController(newManager, newView);

    LocalDate startDate = LocalDate.of(2024, 1, 1);
    LocalDate endDate = LocalDate.of(2024, 1, 31);

    newController.showDashboard(startDate, endDate);

    assertNotNull("Should display error", newView.lastError);
    assertTrue("Dashboard should not be displayed", !newView.dashboardDisplayed);
  }

  /**
   * Tests that dashboard succeeds when a calendar is selected (empty calendar).
   */
  @Test
  public void testDashboardSucceedsWithEmptyCalendar() {
    manager.createCalendar("EmptyCal", ZoneId.of("America/New_York"));
    manager.useCalendar("EmptyCal");

    LocalDate startDate = LocalDate.of(2024, 1, 1);
    LocalDate endDate = LocalDate.of(2024, 1, 31);

    controller.showDashboard(startDate, endDate);

    assertTrue("Dashboard should be displayed", view.dashboardDisplayed);
    assertNotNull("Should return analytics summary", view.displayedSummary);
    assertEquals("Empty calendar should have 0 events", 0, view.displayedSummary.getTotalEvents());
  }

  /**
   * Tests that dashboard succeeds when a calendar is selected (with events).
   */
  @Test
  public void testDashboardSucceedsWithCalendarWithEvents() {
    manager.createCalendar("EventCal", ZoneId.of("America/New_York"));
    manager.useCalendar("EventCal");

    manager.addEvent(new CalendarEvent.EventBuilder()
        .subject("Meeting 1")
        .startDateTime(LocalDateTime.of(2024, 1, 10, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 10, 11, 0))
        .build());

    manager.addEvent(new CalendarEvent.EventBuilder()
        .subject("Meeting 2")
        .startDateTime(LocalDateTime.of(2024, 1, 20, 14, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 20, 15, 0))
        .build());

    LocalDate startDate = LocalDate.of(2024, 1, 1);
    LocalDate endDate = LocalDate.of(2024, 1, 31);

    controller.showDashboard(startDate, endDate);

    assertTrue("Dashboard should be displayed", view.dashboardDisplayed);
    assertNotNull("Should return analytics summary", view.displayedSummary);
    assertEquals("Should count 2 events", 2, view.displayedSummary.getTotalEvents());
  }

  /**
   * Tests dashboard with multiple calendars - only active one is analyzed.
   */
  @Test
  public void testDashboardOnlyAnalyzesActiveCalendar() {
    manager.createCalendar("Cal1", ZoneId.of("America/New_York"));
    manager.useCalendar("Cal1");
    manager.addEvent(new CalendarEvent.EventBuilder()
        .subject("Cal1 Event")
        .startDateTime(LocalDateTime.of(2024, 1, 10, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 10, 11, 0))
        .build());

    manager.createCalendar("Cal2", ZoneId.of("America/Los_Angeles"));
    manager.useCalendar("Cal2");

    manager.addEvent(new CalendarEvent.EventBuilder()
        .subject("Cal2 Event")
        .startDateTime(LocalDateTime.of(2024, 1, 15, 14, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 15, 15, 0))
        .build());

    LocalDate startDate = LocalDate.of(2024, 1, 1);
    LocalDate endDate = LocalDate.of(2024, 1, 31);

    controller.showDashboard(startDate, endDate);

    assertNotNull("Should return analytics summary", view.displayedSummary);
    assertEquals("Should only count Cal2 events", 1, view.displayedSummary.getTotalEvents());
  }

  /**
   * Tests dashboard with null start date.
   */
  @Test
  public void testDashboardWithNullStartDate() {
    manager.createCalendar("TestCal", ZoneId.of("America/New_York"));
    manager.useCalendar("TestCal");

    LocalDate endDate = LocalDate.of(2024, 1, 31);

    controller.showDashboard(null, endDate);

    assertEquals("Error message should indicate null dates",
        "Start date and end date must not be null.", view.lastError);
    assertTrue("Dashboard should not be displayed", !view.dashboardDisplayed);
  }

  /**
   * Tests dashboard with null end date.
   */
  @Test
  public void testDashboardWithNullEndDate() {
    manager.createCalendar("TestCal", ZoneId.of("America/New_York"));
    manager.useCalendar("TestCal");

    LocalDate startDate = LocalDate.of(2024, 1, 1);

    controller.showDashboard(startDate, null);

    assertEquals("Error message should indicate null dates",
        "Start date and end date must not be null.", view.lastError);
    assertTrue("Dashboard should not be displayed", !view.dashboardDisplayed);
  }

  /**
   * Tests dashboard with start date after end date.
   */
  @Test
  public void testDashboardWithInvalidDateRange() {
    manager.createCalendar("TestCal", ZoneId.of("America/New_York"));
    manager.useCalendar("TestCal");

    LocalDate startDate = LocalDate.of(2024, 1, 31);
    LocalDate endDate = LocalDate.of(2024, 1, 1);

    controller.showDashboard(startDate, endDate);

    assertEquals("Error message should indicate invalid date order",
        "End date must not be before start date.", view.lastError);
    assertTrue("Dashboard should not be displayed", !view.dashboardDisplayed);
  }

  /**
   * Tests dashboard succeeds with same start and end date.
   */
  @Test
  public void testDashboardWithSameDateRange() {
    manager.createCalendar("TestCal", ZoneId.of("America/New_York"));
    manager.useCalendar("TestCal");

    manager.addEvent(new CalendarEvent.EventBuilder()
        .subject("Single Day Event")
        .startDateTime(LocalDateTime.of(2024, 1, 15, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 15, 11, 0))
        .build());

    LocalDate singleDay = LocalDate.of(2024, 1, 15);

    controller.showDashboard(singleDay, singleDay);

    assertTrue("Dashboard should be displayed", view.dashboardDisplayed);
    assertNotNull("Should return analytics summary", view.displayedSummary);
    assertEquals("Should count event on single day", 1, view.displayedSummary.getTotalEvents());
  }

  /**
   * Tests dashboard succeeds across year boundary.
   */
  @Test
  public void testDashboardAcrossYearBoundary() {
    manager.createCalendar("TestCal", ZoneId.of("America/New_York"));
    manager.useCalendar("TestCal");

    manager.addEvent(new CalendarEvent.EventBuilder()
        .subject("2023 Event")
        .startDateTime(LocalDateTime.of(2023, 12, 30, 10, 0))
        .endDateTime(LocalDateTime.of(2023, 12, 30, 11, 0))
        .build());

    manager.addEvent(new CalendarEvent.EventBuilder()
        .subject("2024 Event")
        .startDateTime(LocalDateTime.of(2024, 1, 2, 14, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 2, 15, 0))
        .build());

    LocalDate startDate = LocalDate.of(2023, 12, 1);
    LocalDate endDate = LocalDate.of(2024, 1, 31);

    controller.showDashboard(startDate, endDate);

    assertTrue("Dashboard should be displayed", view.dashboardDisplayed);
    assertNotNull("Should return analytics summary", view.displayedSummary);
    assertEquals("Should count events across year boundary", 2,
        view.displayedSummary.getTotalEvents());
  }

  /**
   * Tests dashboard with very large date range.
   */
  @Test
  public void testDashboardWithLargeDateRange() {
    manager.createCalendar("TestCal", ZoneId.of("America/New_York"));
    manager.useCalendar("TestCal");

    manager.addEvent(new CalendarEvent.EventBuilder()
        .subject("Event")
        .startDateTime(LocalDateTime.of(2024, 6, 15, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 6, 15, 11, 0))
        .build());

    LocalDate startDate = LocalDate.of(2024, 1, 1);
    LocalDate endDate = LocalDate.of(2024, 12, 31);

    controller.showDashboard(startDate, endDate);

    assertTrue("Dashboard should be displayed", view.dashboardDisplayed);
    assertNotNull("Should return analytics summary", view.displayedSummary);
    assertEquals("Should count event in large range", 1, view.displayedSummary.getTotalEvents());
  }

  @Test
  public void testDashboardCommandCompletelyWrongSyntax() {
    QueryParser parser = new QueryParser();
    String command = "dashboard show calendar from 2024-01-01 to 2024-01-31";

    Command result = parser.parseCommand(command);
    assertTrue("Should not return ShowDashboardCommand for wrong syntax",
        !(result instanceof ShowDashboardCommand));
  }

  @Test
  public void testDashboardCommandTypoInKeyword() {
    QueryParser parser = new QueryParser();
    String command = "show calendar dashbaord from 2024-01-01 to 2024-01-31";

    Command result = parser.parseCommand(command);
    assertTrue("Should not return ShowDashboardCommand for typo",
        !(result instanceof ShowDashboardCommand));
  }
}