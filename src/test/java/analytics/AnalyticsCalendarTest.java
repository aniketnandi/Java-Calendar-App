package analytics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import calendar.controller.commands.ShowDashboardCommand;
import calendar.model.CalendarAnalyticsSummary;
import calendar.model.CalendarEvent;
import calendar.model.CalendarModel;
import calendar.model.Event;
import calendar.model.EventSeries;
import calendar.model.SimpleCalendar;
import calendar.view.CalendarView;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for Calendar Analytics.
 */
public class AnalyticsCalendarTest {
  private SimpleCalendar calendar;
  private TestConfigurableModelStub model;
  private TestCalendarViewStub view;
  private LocalDate startDate;
  private LocalDate endDate;

  /**
   * Sets up test.
   */
  @Before
  public void setUp() {
    calendar = new SimpleCalendar();
    model = new TestConfigurableModelStub();
    view = new TestCalendarViewStub();
    startDate = LocalDate.of(2024, 1, 1);
    endDate = LocalDate.of(2024, 1, 31);
  }

  /**
   * Tests builder creates summary with all fields set correctly.
   */
  @Test
  public void testBuilderAllFieldsSet() {
    Map<String, Integer> eventsBySubject = new HashMap<>();
    eventsBySubject.put("Meeting", 5);
    eventsBySubject.put("Workshop", 3);

    Map<DayOfWeek, Integer> eventsByWeekday = new EnumMap<>(DayOfWeek.class);
    eventsByWeekday.put(DayOfWeek.MONDAY, 2);
    eventsByWeekday.put(DayOfWeek.FRIDAY, 3);

    Map<Integer, Integer> eventsByWeekIndex = new HashMap<>();
    eventsByWeekIndex.put(1, 4);
    eventsByWeekIndex.put(2, 4);

    Map<YearMonth, Integer> eventsByMonth = new HashMap<>();
    eventsByMonth.put(YearMonth.of(2024, 1), 8);

    LocalDate busiest = LocalDate.of(2024, 1, 15);
    LocalDate leastBusy = LocalDate.of(2024, 1, 3);

    CalendarAnalyticsSummary summary = CalendarAnalyticsSummary.builder()
        .totalEvents(8)
        .eventsBySubject(eventsBySubject)
        .eventsByWeekday(eventsByWeekday)
        .eventsByWeekIndex(eventsByWeekIndex)
        .eventsByMonth(eventsByMonth)
        .averageEventsPerDay(2.5)
        .busiestDay(busiest)
        .leastBusyDay(leastBusy)
        .onlineEventsCount(5)
        .offlineEventsCount(3)
        .build();

    assertEquals(8, summary.getTotalEvents());
    assertEquals(2.5, summary.getAverageEventsPerDay(), 0.001);
    assertEquals(busiest, summary.getBusiestDay());
    assertEquals(leastBusy, summary.getLeastBusyDay());
    assertEquals(5, summary.getOnlineEventsCount());
    assertEquals(3, summary.getOfflineEventsCount());
    assertEquals(2, summary.getEventsBySubject().size());
    assertEquals(2, summary.getEventsByWeekday().size());
    assertEquals(2, summary.getEventsByWeekIndex().size());
    assertEquals(1, summary.getEventsByMonth().size());
  }

  /**
   * Tests builder with null maps creates empty unmodifiable maps.
   */
  @Test
  public void testBuilderWithNullMaps() {
    CalendarAnalyticsSummary summary = CalendarAnalyticsSummary.builder()
        .totalEvents(0)
        .eventsBySubject(null)
        .eventsByWeekday(null)
        .eventsByWeekIndex(null)
        .eventsByMonth(null)
        .averageEventsPerDay(0.0)
        .build();

    assertNotNull(summary.getEventsBySubject());
    assertNotNull(summary.getEventsByWeekday());
    assertNotNull(summary.getEventsByWeekIndex());
    assertNotNull(summary.getEventsByMonth());
    assertTrue(summary.getEventsBySubject().isEmpty());
    assertTrue(summary.getEventsByWeekday().isEmpty());
    assertTrue(summary.getEventsByWeekIndex().isEmpty());
    assertTrue(summary.getEventsByMonth().isEmpty());
  }

  /**
   * Tests builder with empty maps creates empty unmodifiable maps.
   */
  @Test
  public void testBuilderWithEmptyMaps() {
    CalendarAnalyticsSummary summary = CalendarAnalyticsSummary.builder()
        .totalEvents(0)
        .eventsBySubject(new HashMap<>())
        .eventsByWeekday(new EnumMap<>(DayOfWeek.class))
        .eventsByWeekIndex(new HashMap<>())
        .eventsByMonth(new HashMap<>())
        .averageEventsPerDay(0.0)
        .build();

    assertTrue(summary.getEventsBySubject().isEmpty());
    assertTrue(summary.getEventsByWeekday().isEmpty());
    assertTrue(summary.getEventsByWeekIndex().isEmpty());
    assertTrue(summary.getEventsByMonth().isEmpty());
  }

  /**
   * Tests that returned maps are unmodifiable.
   */
  @Test(expected = UnsupportedOperationException.class)
  public void testEventsBySubjectUnmodifiable() {
    Map<String, Integer> eventsBySubject = new HashMap<>();
    eventsBySubject.put("Meeting", 5);

    CalendarAnalyticsSummary summary = CalendarAnalyticsSummary.builder()
        .eventsBySubject(eventsBySubject)
        .build();

    summary.getEventsBySubject().put("New", 1);
  }

  /**
   * Tests that returned weekday map is unmodifiable.
   */
  @Test(expected = UnsupportedOperationException.class)
  public void testEventsByWeekdayUnmodifiable() {
    Map<DayOfWeek, Integer> eventsByWeekday = new EnumMap<>(DayOfWeek.class);
    eventsByWeekday.put(DayOfWeek.MONDAY, 2);

    CalendarAnalyticsSummary summary = CalendarAnalyticsSummary.builder()
        .eventsByWeekday(eventsByWeekday)
        .build();

    summary.getEventsByWeekday().put(DayOfWeek.TUESDAY, 3);
  }

  /**
   * Tests that returned week index map is unmodifiable.
   */
  @Test(expected = UnsupportedOperationException.class)
  public void testEventsByWeekIndexUnmodifiable() {
    Map<Integer, Integer> eventsByWeekIndex = new HashMap<>();
    eventsByWeekIndex.put(1, 4);

    CalendarAnalyticsSummary summary = CalendarAnalyticsSummary.builder()
        .eventsByWeekIndex(eventsByWeekIndex)
        .build();

    summary.getEventsByWeekIndex().put(2, 5);
  }

  /**
   * Tests that returned month map is unmodifiable.
   */
  @Test(expected = UnsupportedOperationException.class)
  public void testEventsByMonthUnmodifiable() {
    Map<YearMonth, Integer> eventsByMonth = new HashMap<>();
    eventsByMonth.put(YearMonth.of(2024, 1), 8);

    CalendarAnalyticsSummary summary = CalendarAnalyticsSummary.builder()
        .eventsByMonth(eventsByMonth)
        .build();

    summary.getEventsByMonth().put(YearMonth.of(2024, 2), 10);
  }

  /**
   * Tests builder method chaining works correctly.
   */
  @Test
  public void testBuilderChaining() {
    CalendarAnalyticsSummary.Builder builder = CalendarAnalyticsSummary.builder();
    CalendarAnalyticsSummary.Builder result = builder.totalEvents(10);
    assertEquals(builder, result);
  }

  /**
   * Tests builder replaces existing map values when called multiple times.
   */
  @Test
  public void testBuilderReplacesMapValues() {
    Map<String, Integer> map1 = new HashMap<>();
    map1.put("A", 1);

    Map<String, Integer> map2 = new HashMap<>();
    map2.put("B", 2);

    CalendarAnalyticsSummary summary = CalendarAnalyticsSummary.builder()
        .eventsBySubject(map1)
        .eventsBySubject(map2)
        .build();

    assertEquals(1, summary.getEventsBySubject().size());
    assertTrue(summary.getEventsBySubject().containsKey("B"));
  }

  /**
   * Tests builder with null dates for busiest and least busy.
   */
  @Test
  public void testBuilderWithNullDates() {
    CalendarAnalyticsSummary summary = CalendarAnalyticsSummary.builder()
        .busiestDay(null)
        .leastBusyDay(null)
        .build();

    assertEquals(null, summary.getBusiestDay());
    assertEquals(null, summary.getLeastBusyDay());
  }


  /**
   * Tests successful dashboard display with all metrics.
   */
  @Test
  public void testExecuteSuccessful() throws IOException {
    Map<String, Integer> eventsBySubject = new HashMap<>();
    eventsBySubject.put("Meeting", 5);
    eventsBySubject.put("Workshop", 3);

    Map<DayOfWeek, Integer> eventsByWeekday = new EnumMap<>(DayOfWeek.class);
    eventsByWeekday.put(DayOfWeek.MONDAY, 2);
    eventsByWeekday.put(DayOfWeek.FRIDAY, 6);

    Map<Integer, Integer> eventsByWeekIndex = new HashMap<>();
    eventsByWeekIndex.put(1, 4);
    eventsByWeekIndex.put(2, 4);

    Map<YearMonth, Integer> eventsByMonth = new HashMap<>();
    eventsByMonth.put(YearMonth.of(2024, 1), 8);

    CalendarAnalyticsSummary summary = CalendarAnalyticsSummary.builder()
        .totalEvents(8)
        .eventsBySubject(eventsBySubject)
        .eventsByWeekday(eventsByWeekday)
        .eventsByWeekIndex(eventsByWeekIndex)
        .eventsByMonth(eventsByMonth)
        .averageEventsPerDay(2.58)
        .busiestDay(LocalDate.of(2024, 1, 15))
        .leastBusyDay(LocalDate.of(2024, 1, 3))
        .onlineEventsCount(5)
        .offlineEventsCount(3)
        .build();

    model.setSummary(summary);

    ShowDashboardCommand cmd = new ShowDashboardCommand(startDate, endDate);
    cmd.execute(model, view);

    String output = view.getLastMessage();
    assertNotNull(output);
    assertTrue(output.contains("Calendar dashboard from 2024-01-01 to 2024-01-31"));
    assertTrue(output.contains("Total events: 8"));
    assertTrue(output.contains("Average events per day: 2.58"));
    assertTrue(output.contains("Meeting: 5"));
    assertTrue(output.contains("Workshop: 3"));
    assertTrue(output.contains("MONDAY: 2"));
    assertTrue(output.contains("FRIDAY: 6"));
    assertTrue(output.contains("1: 4"));
    assertTrue(output.contains("2: 4"));
    assertTrue(output.contains("2024-01: 8"));
    assertTrue(output.contains("Busiest day: 2024-01-15"));
    assertTrue(output.contains("Least busy day: 2024-01-03"));
    assertTrue(output.contains("Online events: 5 (62.50%)"));
    assertTrue(output.contains("Offline / other location events: 3 (37.50%)"));
  }

  /**
   * Tests dashboard with end date before start date shows error.
   */
  @Test
  public void testExecuteEndDateBeforeStartDate() throws IOException {
    LocalDate start = LocalDate.of(2024, 1, 31);
    LocalDate end = LocalDate.of(2024, 1, 1);

    ShowDashboardCommand cmd = new ShowDashboardCommand(start, end);
    cmd.execute(model, view);

    assertEquals("End date must not be before start date.", view.getLastError());
  }

  /**
   * Tests dashboard with empty summary (no events).
   */
  @Test
  public void testExecuteEmptySummary() throws IOException {
    CalendarAnalyticsSummary summary = CalendarAnalyticsSummary.builder()
        .totalEvents(0)
        .eventsBySubject(new HashMap<>())
        .eventsByWeekday(new EnumMap<>(DayOfWeek.class))
        .eventsByWeekIndex(new HashMap<>())
        .eventsByMonth(new HashMap<>())
        .averageEventsPerDay(0.0)
        .busiestDay(null)
        .leastBusyDay(null)
        .onlineEventsCount(0)
        .offlineEventsCount(0)
        .build();

    model.setSummary(summary);

    ShowDashboardCommand cmd = new ShowDashboardCommand(startDate, endDate);
    cmd.execute(model, view);

    String output = view.getLastMessage();
    assertTrue(output.contains("Total events: 0"));
    assertTrue(output.contains("Average events per day: 0.00"));
    assertTrue(output.contains("Events by subject:"));
    assertTrue(output.contains("  none"));
    assertTrue(output.contains("Events by weekday:"));
    assertTrue(output.contains("Events by week (index within interval):"));
    assertTrue(output.contains("Events by month (YYYY-MM):"));
    assertTrue(output.contains("Busiest day: none"));
    assertTrue(output.contains("Least busy day: none"));
    assertTrue(output.contains("Online events: 0 (0.00%)"));
    assertTrue(output.contains("Offline / other location events: 0 (0.00%)"));
  }

  /**
   * Tests percentage formatting with zero total.
   */
  @Test
  public void testExecuteZeroTotalForPercentage() throws IOException {
    CalendarAnalyticsSummary summary = CalendarAnalyticsSummary.builder()
        .totalEvents(0)
        .onlineEventsCount(0)
        .offlineEventsCount(0)
        .build();

    model.setSummary(summary);

    ShowDashboardCommand cmd = new ShowDashboardCommand(startDate, endDate);
    cmd.execute(model, view);

    String output = view.getLastMessage();
    assertTrue(output.contains("Online events: 0 (0.00%)"));
    assertTrue(output.contains("Offline / other location events: 0 (0.00%)"));
  }

  /**
   * Tests percentage formatting with 100% online.
   */
  @Test
  public void testExecute100PercentOnline() throws IOException {
    CalendarAnalyticsSummary summary = CalendarAnalyticsSummary.builder()
        .totalEvents(10)
        .onlineEventsCount(10)
        .offlineEventsCount(0)
        .build();

    model.setSummary(summary);

    ShowDashboardCommand cmd = new ShowDashboardCommand(startDate, endDate);
    cmd.execute(model, view);

    String output = view.getLastMessage();
    assertTrue(output.contains("Online events: 10 (100.00%)"));
    assertTrue(output.contains("Offline / other location events: 0 (0.00%)"));
  }

  /**
   * Tests percentage formatting with fractional percentages.
   */
  @Test
  public void testExecuteFractionalPercentages() throws IOException {
    CalendarAnalyticsSummary summary = CalendarAnalyticsSummary.builder()
        .totalEvents(7)
        .onlineEventsCount(3)
        .offlineEventsCount(4)
        .build();

    model.setSummary(summary);

    ShowDashboardCommand cmd = new ShowDashboardCommand(startDate, endDate);
    cmd.execute(model, view);

    String output = view.getLastMessage();
    assertTrue(output.contains("Online events: 3 (42.86%)"));
    assertTrue(output.contains("Offline / other location events: 4 (57.14%)"));
  }

  /**
   * Tests dashboard with same start and end date.
   */
  @Test
  public void testExecuteSameDateRange() throws IOException {
    LocalDate sameDate = LocalDate.of(2024, 1, 15);

    CalendarAnalyticsSummary summary = CalendarAnalyticsSummary.builder()
        .totalEvents(3)
        .averageEventsPerDay(3.0)
        .build();

    model.setSummary(summary);

    ShowDashboardCommand cmd = new ShowDashboardCommand(sameDate, sameDate);
    cmd.execute(model, view);

    String output = view.getLastMessage();
    assertTrue(output.contains("Calendar dashboard from 2024-01-15 to 2024-01-15"));
    assertTrue(output.contains("Total events: 3"));
  }

  /**
   * Tests dashboard with multiple months.
   */
  @Test
  public void testExecuteMultipleMonths() throws IOException {
    Map<YearMonth, Integer> eventsByMonth = new HashMap<>();
    eventsByMonth.put(YearMonth.of(2024, 1), 10);
    eventsByMonth.put(YearMonth.of(2024, 2), 8);
    eventsByMonth.put(YearMonth.of(2024, 3), 12);

    CalendarAnalyticsSummary summary = CalendarAnalyticsSummary.builder()
        .totalEvents(30)
        .eventsByMonth(eventsByMonth)
        .build();

    model.setSummary(summary);

    ShowDashboardCommand cmd = new ShowDashboardCommand(
        LocalDate.of(2024, 1, 1),
        LocalDate.of(2024, 3, 31));
    cmd.execute(model, view);

    String output = view.getLastMessage();
    assertTrue(output.contains("2024-01: 10"));
    assertTrue(output.contains("2024-02: 8"));
    assertTrue(output.contains("2024-03: 12"));
  }

  /**
   * Tests dashboard with all weekdays.
   */
  @Test
  public void testExecuteAllWeekdays() throws IOException {
    Map<DayOfWeek, Integer> eventsByWeekday = new EnumMap<>(DayOfWeek.class);
    eventsByWeekday.put(DayOfWeek.MONDAY, 1);
    eventsByWeekday.put(DayOfWeek.TUESDAY, 2);
    eventsByWeekday.put(DayOfWeek.WEDNESDAY, 3);
    eventsByWeekday.put(DayOfWeek.THURSDAY, 4);
    eventsByWeekday.put(DayOfWeek.FRIDAY, 5);
    eventsByWeekday.put(DayOfWeek.SATURDAY, 6);
    eventsByWeekday.put(DayOfWeek.SUNDAY, 7);

    CalendarAnalyticsSummary summary = CalendarAnalyticsSummary.builder()
        .eventsByWeekday(eventsByWeekday)
        .build();

    model.setSummary(summary);

    ShowDashboardCommand cmd = new ShowDashboardCommand(startDate, endDate);
    cmd.execute(model, view);

    String output = view.getLastMessage();
    assertTrue(output.contains("MONDAY: 1"));
    assertTrue(output.contains("TUESDAY: 2"));
    assertTrue(output.contains("WEDNESDAY: 3"));
    assertTrue(output.contains("THURSDAY: 4"));
    assertTrue(output.contains("FRIDAY: 5"));
    assertTrue(output.contains("SATURDAY: 6"));
    assertTrue(output.contains("SUNDAY: 7"));
  }

  /**
   * Tests dashboard with multiple week indices.
   */
  @Test
  public void testExecuteMultipleWeeks() throws IOException {
    Map<Integer, Integer> eventsByWeekIndex = new HashMap<>();
    eventsByWeekIndex.put(1, 5);
    eventsByWeekIndex.put(2, 6);
    eventsByWeekIndex.put(3, 7);
    eventsByWeekIndex.put(4, 8);

    CalendarAnalyticsSummary summary = CalendarAnalyticsSummary.builder()
        .eventsByWeekIndex(eventsByWeekIndex)
        .build();

    model.setSummary(summary);

    ShowDashboardCommand cmd = new ShowDashboardCommand(startDate, endDate);
    cmd.execute(model, view);

    String output = view.getLastMessage();
    assertTrue(output.contains("1: 5"));
    assertTrue(output.contains("2: 6"));
    assertTrue(output.contains("3: 7"));
    assertTrue(output.contains("4: 8"));
  }

  /**
   * Tests dashboard with many subjects.
   */
  @Test
  public void testExecuteManySubjects() throws IOException {
    Map<String, Integer> eventsBySubject = new HashMap<>();
    eventsBySubject.put("Meeting", 10);
    eventsBySubject.put("Workshop", 5);
    eventsBySubject.put("Conference", 3);
    eventsBySubject.put("Training", 7);
    eventsBySubject.put("Review", 2);

    CalendarAnalyticsSummary summary = CalendarAnalyticsSummary.builder()
        .eventsBySubject(eventsBySubject)
        .build();

    model.setSummary(summary);

    ShowDashboardCommand cmd = new ShowDashboardCommand(startDate, endDate);
    cmd.execute(model, view);

    String output = view.getLastMessage();
    assertTrue(output.contains("Meeting: 10"));
    assertTrue(output.contains("Workshop: 5"));
    assertTrue(output.contains("Conference: 3"));
    assertTrue(output.contains("Training: 7"));
    assertTrue(output.contains("Review: 2"));
  }

  /**
   * Tests average events per day with high precision.
   */
  @Test
  public void testExecuteAverageHighPrecision() throws IOException {
    CalendarAnalyticsSummary summary = CalendarAnalyticsSummary.builder()
        .averageEventsPerDay(3.14159)
        .build();

    model.setSummary(summary);

    ShowDashboardCommand cmd = new ShowDashboardCommand(startDate, endDate);
    cmd.execute(model, view);

    String output = view.getLastMessage();
    assertTrue(output.contains("Average events per day: 3.14"));
  }

  /**
   * Tests date formatting in various edge cases.
   */
  @Test
  public void testExecuteDateFormatting() throws IOException {
    CalendarAnalyticsSummary summary = CalendarAnalyticsSummary.builder()
        .busiestDay(LocalDate.of(2024, 12, 31))
        .leastBusyDay(LocalDate.of(2024, 1, 1))
        .build();

    model.setSummary(summary);

    ShowDashboardCommand cmd = new ShowDashboardCommand(
        LocalDate.of(2024, 1, 1),
        LocalDate.of(2024, 12, 31));
    cmd.execute(model, view);

    String output = view.getLastMessage();
    assertTrue(output.contains("Calendar dashboard from 2024-01-01 to 2024-12-31"));
    assertTrue(output.contains("Busiest day: 2024-12-31"));
    assertTrue(output.contains("Least busy day: 2024-01-01"));
  }

  @Test
  public void testGenerateAnalyticsNullStartDate() {
    try {
      calendar.generateAnalytics(null, LocalDate.of(2024, 1, 31));
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertEquals("Start date and end date must not be null", e.getMessage());
    }
  }

  @Test
  public void testGenerateAnalyticsNullEndDate() {
    try {
      calendar.generateAnalytics(LocalDate.of(2024, 1, 1), null);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertEquals("Start date and end date must not be null", e.getMessage());
    }
  }

  @Test
  public void testGenerateAnalyticsBothDatesNull() {
    try {
      calendar.generateAnalytics(null, null);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertEquals("Start date and end date must not be null", e.getMessage());
    }
  }

  @Test
  public void testGenerateAnalyticsEndBeforeStart() {
    try {
      calendar.generateAnalytics(LocalDate.of(2024, 1, 31),
          LocalDate.of(2024, 1, 1));
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertEquals("End date must not be before start date", e.getMessage());
    }
  }

  @Test
  public void testGenerateAnalyticsNoEvents() {
    CalendarAnalyticsSummary summary = calendar.generateAnalytics(
        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
    assertEquals(0, summary.getTotalEvents());
    assertEquals(0.0, summary.getAverageEventsPerDay(), 0.001);
    assertNull(summary.getBusiestDay());
    assertNull(summary.getLeastBusyDay());
    assertEquals(0, summary.getOnlineEventsCount());
    assertEquals(0, summary.getOfflineEventsCount());
  }

  @Test
  public void testGenerateAnalyticsSingleDayRange() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .location("online")
        .build();
    calendar.addEvent(event);
    CalendarAnalyticsSummary summary = calendar.generateAnalytics(
        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 1));
    assertEquals(1, summary.getTotalEvents());
    assertEquals(1.0, summary.getAverageEventsPerDay(), 0.001);
    assertEquals(LocalDate.of(2024, 1, 1), summary.getBusiestDay());
    assertEquals(LocalDate.of(2024, 1, 1), summary.getLeastBusyDay());
  }

  @Test
  public void testGenerateAnalyticsMultipleEventsOnline() {
    Event event1 = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .location("online")
        .build();
    Event event2 = new CalendarEvent.EventBuilder()
        .subject("Workshop")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 14, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 15, 0))
        .location("ONLINE")
        .build();
    calendar.addEvent(event1);
    calendar.addEvent(event2);
    CalendarAnalyticsSummary summary = calendar.generateAnalytics(
        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
    assertEquals(2, summary.getTotalEvents());
    assertEquals(2, summary.getOnlineEventsCount());
    assertEquals(0, summary.getOfflineEventsCount());
  }

  @Test
  public void testGenerateAnalyticsMultipleEventsOffline() {
    Event event1 = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .location("Room A")
        .build();
    Event event2 = new CalendarEvent.EventBuilder()
        .subject("Workshop")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 14, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 15, 0))
        .location("Room B")
        .build();
    calendar.addEvent(event1);
    calendar.addEvent(event2);
    CalendarAnalyticsSummary summary = calendar.generateAnalytics(
        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
    assertEquals(2, summary.getTotalEvents());
    assertEquals(0, summary.getOnlineEventsCount());
    assertEquals(2, summary.getOfflineEventsCount());
  }

  @Test
  public void testGenerateAnalyticsNullLocation() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .location(null)
        .build();
    calendar.addEvent(event);
    CalendarAnalyticsSummary summary = calendar.generateAnalytics(
        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
    assertEquals(1, summary.getTotalEvents());
    assertEquals(0, summary.getOnlineEventsCount());
    assertEquals(1, summary.getOfflineEventsCount());
  }

  @Test
  public void testGenerateAnalyticsEmptyLocation() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .location("")
        .build();
    calendar.addEvent(event);
    CalendarAnalyticsSummary summary = calendar.generateAnalytics(
        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
    assertEquals(1, summary.getTotalEvents());
    assertEquals(0, summary.getOnlineEventsCount());
    assertEquals(1, summary.getOfflineEventsCount());
  }

  @Test
  public void testGenerateAnalyticsOnlineLocationWithWhitespace() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .location("  online  ")
        .build();
    calendar.addEvent(event);
    CalendarAnalyticsSummary summary = calendar.generateAnalytics(
        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
    assertEquals(1, summary.getTotalEvents());
    assertEquals(1, summary.getOnlineEventsCount());
    assertEquals(0, summary.getOfflineEventsCount());
  }

  @Test
  public void testGenerateAnalyticsOnlineLocationCaseInsensitive() {
    Event event1 = new CalendarEvent.EventBuilder()
        .subject("Meeting 1")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .location("OnLiNe")
        .build();
    Event event2 = new CalendarEvent.EventBuilder()
        .subject("Meeting 2")
        .startDateTime(LocalDateTime.of(2024, 1, 2, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 2, 11, 0))
        .location("oNlInE")
        .build();
    calendar.addEvent(event1);
    calendar.addEvent(event2);
    CalendarAnalyticsSummary summary = calendar.generateAnalytics(
        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
    assertEquals(2, summary.getOnlineEventsCount());
    assertEquals(0, summary.getOfflineEventsCount());
  }


  @Test
  public void testGenerateAnalyticsEventsBySubject() {
    Event event1 = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .build();
    Event event2 = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 2, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 2, 11, 0))
        .build();
    Event event3 = new CalendarEvent.EventBuilder()
        .subject("Workshop")
        .startDateTime(LocalDateTime.of(2024, 1, 3, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 3, 11, 0))
        .build();
    calendar.addEvent(event1);
    calendar.addEvent(event2);
    calendar.addEvent(event3);
    CalendarAnalyticsSummary summary = calendar.generateAnalytics(
        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
    assertEquals(2, (int) summary.getEventsBySubject().get("Meeting"));
    assertEquals(1, (int) summary.getEventsBySubject().get("Workshop"));
  }

  @Test
  public void testGenerateAnalyticsEventsByWeekday() {
    Event monday = new CalendarEvent.EventBuilder()
        .subject("Monday Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .build();
    Event tuesday = new CalendarEvent.EventBuilder()
        .subject("Tuesday Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 2, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 2, 11, 0))
        .build();
    Event anotherMonday = new CalendarEvent.EventBuilder()
        .subject("Another Monday")
        .startDateTime(LocalDateTime.of(2024, 1, 8, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 8, 11, 0))
        .build();
    calendar.addEvent(monday);
    calendar.addEvent(tuesday);
    calendar.addEvent(anotherMonday);
    CalendarAnalyticsSummary summary = calendar.generateAnalytics(
        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
    assertEquals(2, (int) summary.getEventsByWeekday().get(DayOfWeek.MONDAY));
    assertEquals(1, (int) summary.getEventsByWeekday().get(DayOfWeek.TUESDAY));
  }

  @Test
  public void testGenerateAnalyticsEventsByWeekIndex() {
    Event week1Event = new CalendarEvent.EventBuilder()
        .subject("Week 1")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .build();
    Event week2Event = new CalendarEvent.EventBuilder()
        .subject("Week 2")
        .startDateTime(LocalDateTime.of(2024, 1, 8, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 8, 11, 0))
        .build();
    Event anotherWeek1 = new CalendarEvent.EventBuilder()
        .subject("Another Week 1")
        .startDateTime(LocalDateTime.of(2024, 1, 3, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 3, 11, 0))
        .build();
    calendar.addEvent(week1Event);
    calendar.addEvent(week2Event);
    calendar.addEvent(anotherWeek1);
    CalendarAnalyticsSummary summary = calendar.generateAnalytics(
        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
    assertEquals(2, (int) summary.getEventsByWeekIndex().get(1));
    assertEquals(1, (int) summary.getEventsByWeekIndex().get(2));
  }

  @Test
  public void testGenerateAnalyticsEventsByMonth() {
    Event jan1 = new CalendarEvent.EventBuilder()
        .subject("January Event 1")
        .startDateTime(LocalDateTime.of(2024, 1, 15, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 15, 11, 0))
        .build();
    Event jan2 = new CalendarEvent.EventBuilder()
        .subject("January Event 2")
        .startDateTime(LocalDateTime.of(2024, 1, 20, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 20, 11, 0))
        .build();
    Event feb = new CalendarEvent.EventBuilder()
        .subject("February Event")
        .startDateTime(LocalDateTime.of(2024, 2, 10, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 2, 10, 11, 0))
        .build();
    calendar.addEvent(jan1);
    calendar.addEvent(jan2);
    calendar.addEvent(feb);
    CalendarAnalyticsSummary summary = calendar.generateAnalytics(
        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 29));
    assertEquals(2, (int) summary.getEventsByMonth().get(YearMonth.of(2024, 1)));
    assertEquals(1, (int) summary.getEventsByMonth().get(YearMonth.of(2024, 2)));
  }

  @Test
  public void testGenerateAnalyticsBusiestDay() {
    Event event1 = new CalendarEvent.EventBuilder()
        .subject("Event 1")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .build();
    Event event2 = new CalendarEvent.EventBuilder()
        .subject("Event 2")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 14, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 15, 0))
        .build();
    Event event3 = new CalendarEvent.EventBuilder()
        .subject("Event 3")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 16, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 17, 0))
        .build();
    Event event4 = new CalendarEvent.EventBuilder()
        .subject("Event 4")
        .startDateTime(LocalDateTime.of(2024, 1, 2, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 2, 11, 0))
        .build();
    calendar.addEvent(event1);
    calendar.addEvent(event2);
    calendar.addEvent(event3);
    calendar.addEvent(event4);
    CalendarAnalyticsSummary summary = calendar.generateAnalytics(
        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
    assertEquals(LocalDate.of(2024, 1, 1), summary.getBusiestDay());
  }

  @Test
  public void testGenerateAnalyticsLeastBusyDay() {
    Event event1 = new CalendarEvent.EventBuilder()
        .subject("Event 1")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .build();
    Event event2 = new CalendarEvent.EventBuilder()
        .subject("Event 2")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 14, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 15, 0))
        .build();
    Event event3 = new CalendarEvent.EventBuilder()
        .subject("Event 3")
        .startDateTime(LocalDateTime.of(2024, 1, 2, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 2, 11, 0))
        .build();
    calendar.addEvent(event1);
    calendar.addEvent(event2);
    calendar.addEvent(event3);
    CalendarAnalyticsSummary summary = calendar.generateAnalytics(
        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
    assertEquals(LocalDate.of(2024, 1, 2), summary.getLeastBusyDay());
  }

  @Test
  public void testGenerateAnalyticsBusiestAndLeastBusySameDay() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Only Event")
        .startDateTime(LocalDateTime.of(2024, 1, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 5, 11, 0))
        .build();
    calendar.addEvent(event);
    CalendarAnalyticsSummary summary = calendar.generateAnalytics(
        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
    assertEquals(LocalDate.of(2024, 1, 5), summary.getBusiestDay());
    assertEquals(LocalDate.of(2024, 1, 5), summary.getLeastBusyDay());
  }

  @Test
  public void testGenerateAnalyticsAverageEventsPerDay() {
    Event event1 = new CalendarEvent.EventBuilder()
        .subject("Event 1")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .build();
    Event event2 = new CalendarEvent.EventBuilder()
        .subject("Event 2")
        .startDateTime(LocalDateTime.of(2024, 1, 2, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 2, 11, 0))
        .build();
    calendar.addEvent(event1);
    calendar.addEvent(event2);
    CalendarAnalyticsSummary summary = calendar.generateAnalytics(
        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 10));
    assertEquals(0.2, summary.getAverageEventsPerDay(), 0.001);
  }

  @Test
  public void testGenerateAnalyticsEventSpanningMultipleDays() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Multi-day Event")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 23, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 2, 2, 0))
        .build();
    calendar.addEvent(event);
    CalendarAnalyticsSummary summary = calendar.generateAnalytics(
        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
    assertEquals(1, summary.getTotalEvents());
    assertEquals(LocalDate.of(2024, 1, 1), summary.getBusiestDay());
  }

  @Test
  public void testGenerateAnalyticsEventsOutsideRange() {
    Event beforeRange = new CalendarEvent.EventBuilder()
        .subject("Before")
        .startDateTime(LocalDateTime.of(2023, 12, 31, 10, 0))
        .endDateTime(LocalDateTime.of(2023, 12, 31, 11, 0))
        .build();
    Event afterRange = new CalendarEvent.EventBuilder()
        .subject("After")
        .startDateTime(LocalDateTime.of(2024, 2, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 2, 1, 11, 0))
        .build();
    Event inRange = new CalendarEvent.EventBuilder()
        .subject("In Range")
        .startDateTime(LocalDateTime.of(2024, 1, 15, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 15, 11, 0))
        .build();
    calendar.addEvent(beforeRange);
    calendar.addEvent(afterRange);
    calendar.addEvent(inRange);
    CalendarAnalyticsSummary summary = calendar.generateAnalytics(
        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
    assertEquals(1, summary.getTotalEvents());
    assertEquals(1, (int) summary.getEventsBySubject().get("In Range"));
  }

  @Test
  public void testGenerateAnalyticsEventAtStartBoundary() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Start Boundary")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 0, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 1, 0))
        .build();
    calendar.addEvent(event);
    CalendarAnalyticsSummary summary = calendar.generateAnalytics(
        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
    assertEquals(1, summary.getTotalEvents());
  }

  @Test
  public void testGenerateAnalyticsEventAtEndBoundary() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("End Boundary")
        .startDateTime(LocalDateTime.of(2024, 1, 31, 23, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 31, 23, 59))
        .build();
    calendar.addEvent(event);
    CalendarAnalyticsSummary summary = calendar.generateAnalytics(
        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
    assertEquals(1, summary.getTotalEvents());
  }

  @Test
  public void testGenerateAnalyticsSameStartAndEndDate() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Same Day")
        .startDateTime(LocalDateTime.of(2024, 1, 15, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 15, 11, 0))
        .build();
    calendar.addEvent(event);
    CalendarAnalyticsSummary summary = calendar.generateAnalytics(
        LocalDate.of(2024, 1, 15), LocalDate.of(2024, 1, 15));
    assertEquals(1, summary.getTotalEvents());
    assertEquals(1.0, summary.getAverageEventsPerDay(), 0.001);
  }

  @Test
  public void testGenerateAnalyticsMultipleEventsPerDayCounting() {
    Event event1 = new CalendarEvent.EventBuilder()
        .subject("Event 1")
        .startDateTime(LocalDateTime.of(2024, 1, 5, 9, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 5, 10, 0))
        .build();
    Event event2 = new CalendarEvent.EventBuilder()
        .subject("Event 2")
        .startDateTime(LocalDateTime.of(2024, 1, 5, 11, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 5, 12, 0))
        .build();
    Event event3 = new CalendarEvent.EventBuilder()
        .subject("Event 3")
        .startDateTime(LocalDateTime.of(2024, 1, 5, 14, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 5, 15, 0))
        .build();
    calendar.addEvent(event1);
    calendar.addEvent(event2);
    calendar.addEvent(event3);
    CalendarAnalyticsSummary summary = calendar.generateAnalytics(
        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
    assertEquals(LocalDate.of(2024, 1, 5), summary.getBusiestDay());
    assertEquals(LocalDate.of(2024, 1, 5), summary.getLeastBusyDay());
  }

  @Test
  public void testGenerateAnalyticsAllDayOfWeeksCovered() {
    Event monday = new CalendarEvent.EventBuilder()
        .subject("Mon").startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0)).build();
    Event tuesday = new CalendarEvent.EventBuilder()
        .subject("Tue").startDateTime(LocalDateTime.of(2024, 1, 2, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 2, 11, 0)).build();
    Event wednesday = new CalendarEvent.EventBuilder()
        .subject("Wed").startDateTime(LocalDateTime.of(2024, 1, 3, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 3, 11, 0)).build();
    Event thursday = new CalendarEvent.EventBuilder()
        .subject("Thu").startDateTime(LocalDateTime.of(2024, 1, 4, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 4, 11, 0)).build();
    Event friday = new CalendarEvent.EventBuilder()
        .subject("Fri").startDateTime(LocalDateTime.of(2024, 1, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 5, 11, 0)).build();
    Event saturday = new CalendarEvent.EventBuilder()
        .subject("Sat").startDateTime(LocalDateTime.of(2024, 1, 6, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 6, 11, 0)).build();
    Event sunday = new CalendarEvent.EventBuilder()
        .subject("Sun").startDateTime(LocalDateTime.of(2024, 1, 7, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 7, 11, 0)).build();
    calendar.addEvent(monday);
    calendar.addEvent(tuesday);
    calendar.addEvent(wednesday);
    calendar.addEvent(thursday);
    calendar.addEvent(friday);
    calendar.addEvent(saturday);
    calendar.addEvent(sunday);
    CalendarAnalyticsSummary summary = calendar.generateAnalytics(
        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
    assertEquals(1, (int) summary.getEventsByWeekday().get(DayOfWeek.MONDAY));
    assertEquals(1, (int) summary.getEventsByWeekday().get(DayOfWeek.TUESDAY));
    assertEquals(1, (int) summary.getEventsByWeekday().get(DayOfWeek.WEDNESDAY));
    assertEquals(1, (int) summary.getEventsByWeekday().get(DayOfWeek.THURSDAY));
    assertEquals(1, (int) summary.getEventsByWeekday().get(DayOfWeek.FRIDAY));
    assertEquals(1, (int) summary.getEventsByWeekday().get(DayOfWeek.SATURDAY));
    assertEquals(1, (int) summary.getEventsByWeekday().get(DayOfWeek.SUNDAY));
  }

  @Test
  public void testGenerateAnalyticsMixedOnlineOfflineAndNull() {
    Event online = new CalendarEvent.EventBuilder()
        .subject("Online").startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .location("online").build();
    Event offline = new CalendarEvent.EventBuilder()
        .subject("Offline").startDateTime(LocalDateTime.of(2024, 1, 2, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 2, 11, 0))
        .location("Room A").build();
    Event nullLocation = new CalendarEvent.EventBuilder()
        .subject("Null").startDateTime(LocalDateTime.of(2024, 1, 3, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 3, 11, 0))
        .location(null).build();
    calendar.addEvent(online);
    calendar.addEvent(offline);
    calendar.addEvent(nullLocation);
    CalendarAnalyticsSummary summary = calendar.generateAnalytics(
        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
    assertEquals(1, summary.getOnlineEventsCount());
    assertEquals(2, summary.getOfflineEventsCount());
  }

  /**
   * Tests generateAnalytics with event having null subject.
   * Uses reflection to bypass builder validation and test the defensive code.
   */
  @Test
  public void testGenerateAnalyticsNullSubject() throws Exception {
    SimpleCalendar calendar = new SimpleCalendar();

    CalendarEvent event = new CalendarEvent.EventBuilder()
        .subject("TempSubject")
        .startDateTime(LocalDateTime.of(2024, 1, 15, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 15, 11, 0))
        .build();

    java.lang.reflect.Field subjectField = CalendarEvent.class.getDeclaredField("subject");
    subjectField.setAccessible(true);
    subjectField.set(event, null);

    calendar.addEvent(event);

    LocalDate startDate = LocalDate.of(2024, 1, 1);
    LocalDate endDate = LocalDate.of(2024, 1, 31);

    CalendarAnalyticsSummary summary = calendar.generateAnalytics(startDate, endDate);

    assertTrue("Should contain '(no subject)' key",
        summary.getEventsBySubject().containsKey("(no subject)"));
    assertEquals("Should have 1 event with no subject", Integer.valueOf(1),
        summary.getEventsBySubject().get("(no subject)"));
  }

  /**
   * Tests generateAnalytics with event having empty subject.
   * Uses reflection to bypass builder validation and test the defensive code.
   */
  @Test
  public void testGenerateAnalyticsEmptySubject() throws Exception {
    SimpleCalendar calendar = new SimpleCalendar();

    CalendarEvent event = new CalendarEvent.EventBuilder()
        .subject("TempSubject")
        .startDateTime(LocalDateTime.of(2024, 1, 15, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 15, 11, 0))
        .build();

    java.lang.reflect.Field subjectField = CalendarEvent.class.getDeclaredField("subject");
    subjectField.setAccessible(true);
    subjectField.set(event, "");

    calendar.addEvent(event);

    LocalDate startDate = LocalDate.of(2024, 1, 1);
    LocalDate endDate = LocalDate.of(2024, 1, 31);

    CalendarAnalyticsSummary summary = calendar.generateAnalytics(startDate, endDate);

    assertTrue("Should contain '(no subject)' key",
        summary.getEventsBySubject().containsKey("(no subject)"));
    assertEquals("Should have 1 event with no subject", Integer.valueOf(1),
        summary.getEventsBySubject().get("(no subject)"));
  }

  /**
   * Tests generateAnalytics with event having whitespace-only subject.
   * Uses reflection to bypass builder validation and test the defensive code.
   */
  @Test
  public void testGenerateAnalyticsWhitespaceSubject() throws Exception {
    SimpleCalendar calendar = new SimpleCalendar();

    CalendarEvent event = new CalendarEvent.EventBuilder()
        .subject("TempSubject")
        .startDateTime(LocalDateTime.of(2024, 1, 15, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 15, 11, 0))
        .build();

    java.lang.reflect.Field subjectField = CalendarEvent.class.getDeclaredField("subject");
    subjectField.setAccessible(true);
    subjectField.set(event, "   ");

    calendar.addEvent(event);

    LocalDate startDate = LocalDate.of(2024, 1, 1);
    LocalDate endDate = LocalDate.of(2024, 1, 31);

    CalendarAnalyticsSummary summary = calendar.generateAnalytics(startDate, endDate);

    assertTrue("Should contain '(no subject)' key",
        summary.getEventsBySubject().containsKey("(no subject)"));
    assertEquals("Should have 1 event with no subject", Integer.valueOf(1),
        summary.getEventsBySubject().get("(no subject)"));
  }

  /**
   * Tests generateAnalytics with event having valid subject.
   */
  @Test
  public void testGenerateAnalyticsValidSubject() {
    SimpleCalendar calendar = new SimpleCalendar();

    CalendarEvent event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 15, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 15, 11, 0))
        .build();

    calendar.addEvent(event);

    LocalDate startDate = LocalDate.of(2024, 1, 1);
    LocalDate endDate = LocalDate.of(2024, 1, 31);

    CalendarAnalyticsSummary summary = calendar.generateAnalytics(startDate, endDate);

    assertTrue("Should contain 'Meeting' key",
        summary.getEventsBySubject().containsKey("Meeting"));
    assertEquals("Should have 1 meeting event", Integer.valueOf(1),
        summary.getEventsBySubject().get("Meeting"));
  }

  /**
   * Tests generateAnalytics average calculation with single day.
   */
  @Test
  public void testGenerateAnalyticsAverageSingleDay() {
    SimpleCalendar calendar = new SimpleCalendar();

    CalendarEvent event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 15, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 15, 11, 0))
        .build();

    calendar.addEvent(event);

    LocalDate date = LocalDate.of(2024, 1, 15);

    CalendarAnalyticsSummary summary = calendar.generateAnalytics(date, date);

    assertEquals("Average should be 1.0 for single day with one event",
        1.0, summary.getAverageEventsPerDay(), 0.001);
  }

  /**
   * Tests generateAnalytics average calculation with multiple events.
   */
  @Test
  public void testGenerateAnalyticsAverageMultipleEvents() {
    SimpleCalendar calendar = new SimpleCalendar();

    CalendarEvent event1 = new CalendarEvent.EventBuilder()
        .subject("Meeting1")
        .startDateTime(LocalDateTime.of(2024, 1, 15, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 15, 11, 0))
        .build();

    CalendarEvent event2 = new CalendarEvent.EventBuilder()
        .subject("Meeting2")
        .startDateTime(LocalDateTime.of(2024, 1, 16, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 16, 11, 0))
        .build();

    calendar.addEvent(event1);
    calendar.addEvent(event2);

    LocalDate startDate = LocalDate.of(2024, 1, 15);
    LocalDate endDate = LocalDate.of(2024, 1, 16);

    CalendarAnalyticsSummary summary = calendar.generateAnalytics(startDate, endDate);

    assertEquals("Average should be 1.0 for 2 events over 2 days",
        1.0, summary.getAverageEventsPerDay(), 0.001);
  }

  /**
   * Tests generateAnalytics with empty calendar having positive days interval.
   */
  @Test
  public void testGenerateAnalyticsEmptyCalendarPositiveDays() {
    SimpleCalendar calendar = new SimpleCalendar();

    LocalDate startDate = LocalDate.of(2024, 1, 1);
    LocalDate endDate = LocalDate.of(2024, 1, 31);

    CalendarAnalyticsSummary summary = calendar.generateAnalytics(startDate, endDate);

    assertEquals("Average should be 0.0 for empty calendar",
        0.0, summary.getAverageEventsPerDay(), 0.001);
  }

  /**
   * Tests that eventsByWeekday clear() is called when setting new values.
   */
  @Test
  public void testEventsByWeekdayClearCalled() {
    Map<DayOfWeek, Integer> initialMap = new EnumMap<>(DayOfWeek.class);
    initialMap.put(DayOfWeek.MONDAY, 5);

    Map<DayOfWeek, Integer> newMap = new EnumMap<>(DayOfWeek.class);
    newMap.put(DayOfWeek.FRIDAY, 3);

    CalendarAnalyticsSummary summary = CalendarAnalyticsSummary.builder()
        .eventsByWeekday(initialMap)
        .eventsByWeekday(newMap)
        .build();

    assertEquals("Should only have FRIDAY after clear", 1,
        summary.getEventsByWeekday().size());
    assertTrue("Should contain FRIDAY",
        summary.getEventsByWeekday().containsKey(DayOfWeek.FRIDAY));
    assertTrue("Should not contain MONDAY from initial map",
        !summary.getEventsByWeekday().containsKey(DayOfWeek.MONDAY));
  }

  /**
   * Tests that eventsByWeekIndex clear() is called when setting new values.
   */
  @Test
  public void testEventsByWeekIndexClearCalled() {
    Map<Integer, Integer> initialMap = new HashMap<>();
    initialMap.put(1, 10);

    Map<Integer, Integer> newMap = new HashMap<>();
    newMap.put(2, 20);

    CalendarAnalyticsSummary summary = CalendarAnalyticsSummary.builder()
        .eventsByWeekIndex(initialMap)
        .eventsByWeekIndex(newMap)
        .build();

    assertEquals("Should only have week 2 after clear", 1,
        summary.getEventsByWeekIndex().size());
    assertTrue("Should contain week 2",
        summary.getEventsByWeekIndex().containsKey(2));
    assertTrue("Should not contain week 1 from initial map",
        !summary.getEventsByWeekIndex().containsKey(1));
  }

  /**
   * Tests that eventsByMonth clear() is called when setting new values.
   */
  @Test
  public void testEventsByMonthClearCalled() {
    Map<YearMonth, Integer> initialMap = new HashMap<>();
    initialMap.put(YearMonth.of(2024, 1), 15);

    Map<YearMonth, Integer> newMap = new HashMap<>();
    newMap.put(YearMonth.of(2024, 2), 25);

    CalendarAnalyticsSummary summary = CalendarAnalyticsSummary.builder()
        .eventsByMonth(initialMap)
        .eventsByMonth(newMap)
        .build();

    assertEquals("Should only have February after clear", 1,
        summary.getEventsByMonth().size());
    assertTrue("Should contain February",
        summary.getEventsByMonth().containsKey(YearMonth.of(2024, 2)));
    assertTrue("Should not contain January from initial map",
        !summary.getEventsByMonth().containsKey(YearMonth.of(2024, 1)));
  }

  /**
   * Tests that maxCount comparison correctly identifies busiest day.
   * Ensures line 514 mutation (> to >=) is killed.
   */
  @Test
  public void testGenerateAnalyticsBusiestDayComparison() {
    Event event1 = new CalendarEvent.EventBuilder()
        .subject("Day1Event1")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .build();
    Event event2 = new CalendarEvent.EventBuilder()
        .subject("Day1Event2")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 14, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 15, 0))
        .build();
    Event event3 = new CalendarEvent.EventBuilder()
        .subject("Day2Event")
        .startDateTime(LocalDateTime.of(2024, 1, 2, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 2, 11, 0))
        .build();

    calendar.addEvent(event1);
    calendar.addEvent(event2);
    calendar.addEvent(event3);

    CalendarAnalyticsSummary summary = calendar.generateAnalytics(
        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));

    assertEquals("Busiest day should be Jan 1 with 2 events",
        LocalDate.of(2024, 1, 1), summary.getBusiestDay());
    assertEquals("Least busy day should be Jan 2 with 1 event",
        LocalDate.of(2024, 1, 2), summary.getLeastBusyDay());
  }

  /**
   * Tests that minCount comparison correctly identifies least busy day.
   * Ensures line 518 mutation (< to <=) is killed.
   */
  @Test
  public void testGenerateAnalyticsLeastBusyDayComparison() {
    Event event1 = new CalendarEvent.EventBuilder()
        .subject("Day1Event")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .build();
    Event event2 = new CalendarEvent.EventBuilder()
        .subject("Day2Event1")
        .startDateTime(LocalDateTime.of(2024, 1, 2, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 2, 11, 0))
        .build();
    Event event3 = new CalendarEvent.EventBuilder()
        .subject("Day2Event2")
        .startDateTime(LocalDateTime.of(2024, 1, 2, 14, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 2, 15, 0))
        .build();

    calendar.addEvent(event1);
    calendar.addEvent(event2);
    calendar.addEvent(event3);

    CalendarAnalyticsSummary summary = calendar.generateAnalytics(
        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));

    assertEquals("Least busy day should be Jan 1 with 1 event",
        LocalDate.of(2024, 1, 1), summary.getLeastBusyDay());
    assertEquals("Busiest day should be Jan 2 with 2 events",
        LocalDate.of(2024, 1, 2), summary.getBusiestDay());
  }

  /**
   * Tests average calculation with positive days interval.
   * Ensures line 525 mutation (> to >=) is killed by testing daysInInterval = 1.
   */
  @Test
  public void testGenerateAnalyticsAverageDaysIntervalBoundary() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("SingleDay")
        .startDateTime(LocalDateTime.of(2024, 1, 15, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 15, 11, 0))
        .build();

    calendar.addEvent(event);

    CalendarAnalyticsSummary summary = calendar.generateAnalytics(
        LocalDate.of(2024, 1, 15), LocalDate.of(2024, 1, 15));

    assertEquals("Average should be 1.0 for 1 event in 1 day",
        1.0, summary.getAverageEventsPerDay(), 0.001);
  }

  @Test
  public void testAverageEventsPerDayOverMultiDayInterval() {
    LocalDate startDate = LocalDate.of(2025, 3, 1);
    LocalDate endDate = startDate.plusDays(2);

    Event event1 = new CalendarEvent.EventBuilder()
        .subject("Day1")
        .startDateTime(startDate.atTime(9, 0))
        .endDateTime(startDate.atTime(10, 0))
        .build();
    Event event2 = new CalendarEvent.EventBuilder()
        .subject("Day2")
        .startDateTime(startDate.plusDays(1).atTime(9, 0))
        .endDateTime(startDate.plusDays(1).atTime(10, 0))
        .build();
    Event event3 = new CalendarEvent.EventBuilder()
        .subject("Day3")
        .startDateTime(startDate.plusDays(2).atTime(9, 0))
        .endDateTime(startDate.plusDays(2).atTime(10, 0))
        .build();

    calendar.addEvent(event1);
    calendar.addEvent(event2);
    calendar.addEvent(event3);

    CalendarAnalyticsSummary summary =
        calendar.generateAnalytics(startDate, endDate);

    assertEquals(1.0, summary.getAverageEventsPerDay(), 0.0001);
  }

  @Test
  public void testBusiestDayNotReplacedWhenCountsAreEqual() {
    LocalDate firstDay = LocalDate.of(2025, 1, 10);
    LocalDate secondDay = firstDay.plusDays(1);

    Event event1 = new CalendarEvent.EventBuilder()
        .subject("Day1Event1")
        .startDateTime(firstDay.atTime(9, 0))
        .endDateTime(firstDay.atTime(10, 0))
        .build();
    Event event2 = new CalendarEvent.EventBuilder()
        .subject("Day1Event2")
        .startDateTime(firstDay.atTime(11, 0))
        .endDateTime(firstDay.atTime(12, 0))
        .build();

    Event event3 = new CalendarEvent.EventBuilder()
        .subject("Day2Event1")
        .startDateTime(secondDay.atTime(9, 0))
        .endDateTime(secondDay.atTime(10, 0))
        .build();
    Event event4 = new CalendarEvent.EventBuilder()
        .subject("Day2Event2")
        .startDateTime(secondDay.atTime(11, 0))
        .endDateTime(secondDay.atTime(12, 0))
        .build();

    calendar.addEvent(event1);
    calendar.addEvent(event2);
    calendar.addEvent(event3);
    calendar.addEvent(event4);

    CalendarAnalyticsSummary summary =
        calendar.generateAnalytics(firstDay, secondDay);

    assertEquals(secondDay, summary.getBusiestDay());
  }

  @Test
  public void testLeastBusyDayNotReplacedWhenCountsAreEqual() {
    LocalDate firstDay = LocalDate.of(2025, 2, 1);
    LocalDate secondDay = firstDay.plusDays(1);

    Event event1 = new CalendarEvent.EventBuilder()
        .subject("Day1Event")
        .startDateTime(firstDay.atTime(10, 0))
        .endDateTime(firstDay.atTime(11, 0))
        .build();
    Event event2 = new CalendarEvent.EventBuilder()
        .subject("Day2Event")
        .startDateTime(secondDay.atTime(10, 0))
        .endDateTime(secondDay.atTime(11, 0))
        .build();

    calendar.addEvent(event1);
    calendar.addEvent(event2);

    CalendarAnalyticsSummary summary =
        calendar.generateAnalytics(firstDay, secondDay);

    assertEquals(secondDay, summary.getLeastBusyDay());
  }

  /**
   * Tests ShowDashboardCommand execution throws exception when no calendar is active.
   */
  @Test(expected = IllegalStateException.class)
  public void testExecuteNoActiveCalendar() throws IOException {
    TestModelStub modelWithoutCalendar = new TestModelStub();

    TestCalendarViewStub mockView = new TestCalendarViewStub();

    ShowDashboardCommand cmd = new ShowDashboardCommand(startDate, endDate);
    cmd.execute(modelWithoutCalendar, mockView);
  }
}
