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
import calendar.model.CalendarManager;
import calendar.model.Event;
import calendar.model.SimpleCalendarManager;
import calendar.view.CalendarEditData;
import calendar.view.CalendarGuiView;
import calendar.view.EventDeleteData;
import calendar.view.EventEditData;
import calendar.view.EventFormData;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for GuiController focusing on the showDashboard method.
 */
public class AnalyticsControllerTest {
  private CalendarManager manager;
  private TestGuiViewStub view;
  private GuiController controller;

  /**
   * Sets up the test fixture before each test.
   */
  @Before
  public void setUp() {
    manager = new SimpleCalendarManager();
    view = new TestGuiViewStub();
    controller = new GuiController(manager, view);

    manager.createCalendar("TestCalendar", ZoneId.of("America/New_York"));
    manager.useCalendar("TestCalendar");
  }

  /**
   * Tests showDashboard with valid date range.
   */
  @Test
  public void testShowDashboardValidDateRange() {
    LocalDate startDate = LocalDate.of(2024, 1, 1);
    LocalDate endDate = LocalDate.of(2024, 1, 31);

    controller.showDashboard(startDate, endDate);

    assertTrue("Dashboard should be displayed", view.dashboardDisplayed);
    assertNotNull("Summary should not be null", view.displayedSummary);
    assertEquals("Start date should match", startDate, view.displayedStartDate);
    assertEquals("End date should match", endDate, view.displayedEndDate);
    assertNull("No error should be displayed", view.lastError);
  }

  /**
   * Tests showDashboard with null start date.
   */
  @Test
  public void testShowDashboardNullStartDate() {
    LocalDate endDate = LocalDate.of(2024, 1, 31);

    controller.showDashboard(null, endDate);

    assertEquals("Error message should indicate null dates",
        "Start date and end date must not be null.", view.lastError);
    assertTrue("Dashboard should not be displayed", !view.dashboardDisplayed);
  }

  /**
   * Tests showDashboard with null end date.
   */
  @Test
  public void testShowDashboardNullEndDate() {
    LocalDate startDate = LocalDate.of(2024, 1, 1);

    controller.showDashboard(startDate, null);

    assertEquals("Error message should indicate null dates",
        "Start date and end date must not be null.", view.lastError);
    assertTrue("Dashboard should not be displayed", !view.dashboardDisplayed);
  }

  /**
   * Tests showDashboard with both null dates.
   */
  @Test
  public void testShowDashboardBothDatesNull() {
    controller.showDashboard(null, null);

    assertEquals("Error message should indicate null dates",
        "Start date and end date must not be null.", view.lastError);
    assertTrue("Dashboard should not be displayed", !view.dashboardDisplayed);
  }

  /**
   * Tests showDashboard with end date before start date.
   */
  @Test
  public void testShowDashboardEndDateBeforeStartDate() {
    LocalDate startDate = LocalDate.of(2024, 1, 31);
    LocalDate endDate = LocalDate.of(2024, 1, 1);

    controller.showDashboard(startDate, endDate);

    assertEquals("Error message should indicate invalid date order",
        "End date must not be before start date.", view.lastError);
    assertTrue("Dashboard should not be displayed", !view.dashboardDisplayed);
  }

  /**
   * Tests showDashboard with same start and end date.
   */
  @Test
  public void testShowDashboardSameStartAndEndDate() {
    LocalDate date = LocalDate.of(2024, 1, 15);

    controller.showDashboard(date, date);

    assertTrue("Dashboard should be displayed", view.dashboardDisplayed);
    assertNotNull("Summary should not be null", view.displayedSummary);
    assertEquals("Start date should match", date, view.displayedStartDate);
    assertEquals("End date should match", date, view.displayedEndDate);
    assertNull("No error should be displayed", view.lastError);
  }

  /**
   * Tests showDashboard with dates spanning multiple months.
   */
  @Test
  public void testShowDashboardMultipleMonths() {
    LocalDate startDate = LocalDate.of(2024, 1, 1);
    LocalDate endDate = LocalDate.of(2024, 3, 31);

    controller.showDashboard(startDate, endDate);

    assertTrue("Dashboard should be displayed", view.dashboardDisplayed);
    assertNotNull("Summary should not be null", view.displayedSummary);
    assertEquals("Start date should match", startDate, view.displayedStartDate);
    assertEquals("End date should match", endDate, view.displayedEndDate);
  }

  /**
   * Tests showDashboard with dates spanning a full year.
   */
  @Test
  public void testShowDashboardFullYear() {
    LocalDate startDate = LocalDate.of(2024, 1, 1);
    LocalDate endDate = LocalDate.of(2024, 12, 31);

    controller.showDashboard(startDate, endDate);

    assertTrue("Dashboard should be displayed", view.dashboardDisplayed);
    assertNotNull("Summary should not be null", view.displayedSummary);
  }

  /**
   * Tests showDashboard with consecutive dates.
   */
  @Test
  public void testShowDashboardConsecutiveDates() {
    LocalDate startDate = LocalDate.of(2024, 1, 15);
    LocalDate endDate = LocalDate.of(2024, 1, 16);

    controller.showDashboard(startDate, endDate);

    assertTrue("Dashboard should be displayed", view.dashboardDisplayed);
    assertNotNull("Summary should not be null", view.displayedSummary);
  }

  /**
   * Tests showDashboard with dates in different years.
   */
  @Test
  public void testShowDashboardDifferentYears() {
    LocalDate startDate = LocalDate.of(2023, 12, 1);
    LocalDate endDate = LocalDate.of(2024, 1, 31);

    controller.showDashboard(startDate, endDate);

    assertTrue("Dashboard should be displayed", view.dashboardDisplayed);
    assertNotNull("Summary should not be null", view.displayedSummary);
  }

  /**
   * Tests showDashboard with leap year dates.
   */
  @Test
  public void testShowDashboardLeapYearDates() {
    LocalDate startDate = LocalDate.of(2024, 2, 28);
    LocalDate endDate = LocalDate.of(2024, 2, 29);

    controller.showDashboard(startDate, endDate);

    assertTrue("Dashboard should be displayed", view.dashboardDisplayed);
    assertNotNull("Summary should not be null", view.displayedSummary);
  }

  /**
   * Tests showDashboard with dates at year boundaries.
   */
  @Test
  public void testShowDashboardYearBoundaries() {
    LocalDate startDate = LocalDate.of(2024, 12, 31);
    LocalDate endDate = LocalDate.of(2025, 1, 1);

    controller.showDashboard(startDate, endDate);

    assertTrue("Dashboard should be displayed", view.dashboardDisplayed);
    assertNotNull("Summary should not be null", view.displayedSummary);
  }

  /**
   * Tests showDashboard with dates at month boundaries.
   */
  @Test
  public void testShowDashboardMonthBoundaries() {
    LocalDate startDate = LocalDate.of(2024, 1, 31);
    LocalDate endDate = LocalDate.of(2024, 2, 1);

    controller.showDashboard(startDate, endDate);

    assertTrue("Dashboard should be displayed", view.dashboardDisplayed);
    assertNotNull("Summary should not be null", view.displayedSummary);
  }

  /**
   * Tests showDashboard with long date range.
   */
  @Test
  public void testShowDashboardLongDateRange() {
    LocalDate startDate = LocalDate.of(2020, 1, 1);
    LocalDate endDate = LocalDate.of(2024, 12, 31);

    controller.showDashboard(startDate, endDate);

    assertTrue("Dashboard should be displayed", view.dashboardDisplayed);
    assertNotNull("Summary should not be null", view.displayedSummary);
  }

  /**
   * Tests showDashboard with start date equals end date minus one day scenario.
   */
  @Test
  public void testShowDashboardEndDateOffByOne() {
    LocalDate startDate = LocalDate.of(2024, 1, 1);
    LocalDate endDate = LocalDate.of(2023, 12, 31);

    controller.showDashboard(startDate, endDate);

    assertEquals("Error message should indicate invalid date order",
        "End date must not be before start date.", view.lastError);
    assertTrue("Dashboard should not be displayed", !view.dashboardDisplayed);
  }

  /**
   * Tests showDashboard with minimum valid date range.
   */
  @Test
  public void testShowDashboardMinimumValidRange() {
    LocalDate date = LocalDate.of(2024, 6, 15);

    controller.showDashboard(date, date);

    assertTrue("Dashboard should be displayed", view.dashboardDisplayed);
    assertNotNull("Summary should not be null", view.displayedSummary);
  }

  /**
   * Tests showDashboard with dates in reverse chronological order.
   */
  @Test
  public void testShowDashboardReverseDateOrder() {
    LocalDate startDate = LocalDate.of(2024, 12, 31);
    LocalDate endDate = LocalDate.of(2024, 1, 1);

    controller.showDashboard(startDate, endDate);

    assertEquals("Error message should indicate invalid date order",
        "End date must not be before start date.", view.lastError);
    assertTrue("Dashboard should not be displayed", !view.dashboardDisplayed);
  }

  /**
   * Tests showDashboard validates null start date before checking date order.
   */
  @Test
  public void testShowDashboardNullStartDatePriority() {
    controller.showDashboard(null, LocalDate.of(2024, 1, 1));

    assertEquals("Error message should indicate null dates",
        "Start date and end date must not be null.", view.lastError);
  }

  /**
   * Tests showDashboard with both dates null takes precedence.
   */
  @Test
  public void testShowDashboardBothNullPrecedence() {
    controller.showDashboard(null, null);

    assertEquals("Error message should indicate null dates",
        "Start date and end date must not be null.", view.lastError);
  }

  /**
   * Tests showDashboard with valid dates across month with 30 days.
   */
  @Test
  public void testShowDashboardMonth30Days() {
    LocalDate startDate = LocalDate.of(2024, 4, 1);
    LocalDate endDate = LocalDate.of(2024, 4, 30);

    controller.showDashboard(startDate, endDate);

    assertTrue("Dashboard should be displayed", view.dashboardDisplayed);
    assertNotNull("Summary should not be null", view.displayedSummary);
  }

  /**
   * Tests showDashboard with valid dates across month with 31 days.
   */
  @Test
  public void testShowDashboardMonth31Days() {
    LocalDate startDate = LocalDate.of(2024, 1, 1);
    LocalDate endDate = LocalDate.of(2024, 1, 31);

    controller.showDashboard(startDate, endDate);

    assertTrue("Dashboard should be displayed", view.dashboardDisplayed);
    assertNotNull("Summary should not be null", view.displayedSummary);
  }

  /**
   * Tests parseDashboard with valid command syntax.
   */
  @Test
  public void testParseDashboardValidCommand() {
    QueryParser parser = new QueryParser();
    String command = "show calendar dashboard from 2024-01-01 to 2024-01-31";

    Command result = parser.parseCommand(command);

    assertNotNull("Command should not be null", result);
    assertTrue("Should return ShowDashboardCommand",
        result instanceof ShowDashboardCommand);
  }

  /**
   * Tests parseDashboard with same start and end date.
   */
  @Test
  public void testParseDashboardSameDate() {
    QueryParser parser = new QueryParser();
    String command = "show calendar dashboard from 2024-06-15 to 2024-06-15";

    Command result = parser.parseCommand(command);

    assertNotNull("Command should not be null", result);
    assertTrue("Should return ShowDashboardCommand",
        result instanceof ShowDashboardCommand);
  }

  /**
   * Tests parseDashboard with leap year date.
   */
  @Test
  public void testParseDashboardLeapYear() {
    QueryParser parser = new QueryParser();
    String command = "show calendar dashboard from 2024-02-28 to 2024-02-29";

    Command result = parser.parseCommand(command);

    assertNotNull("Command should not be null", result);
    assertTrue("Should return ShowDashboardCommand",
        result instanceof ShowDashboardCommand);
  }

  /**
   * Tests parseDashboard with regex not matching.
   */
  @Test
  public void testParseDashboardRegexNoMatch() {
    QueryParser parser = new QueryParser();
    String command = "show calendar dashboard from 2024-01-01 2024-01-31";

    try {
      parser.parseCommand(command);
      fail("Should throw IllegalArgumentException for missing 'to'");
    } catch (IllegalArgumentException e) {
      assertTrue("Exception message should mention invalid syntax",
          e.getMessage().contains("Invalid dashboard syntax"));
    }
  }

  /**
   * Tests parseDashboard with invalid date format triggering DateTimeParseException.
   */
  @Test
  public void testParseDashboardInvalidDateFormat() {
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
   * Tests parseDashboard with invalid day causing DateTimeParseException.
   */
  @Test
  public void testParseDashboardInvalidDay() {
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
   * View stub that throws IllegalArgumentException when displaying the dashboard.
   * Used to exercise the IllegalArgumentException catch block in showDashboard.
   */
  private static class IllegalArgumentDashboardView extends TestGuiViewStub {

    @Override
    public void displayDashboard(CalendarAnalyticsSummary summary,
                                 LocalDate startDate, LocalDate endDate) {
      throw new IllegalArgumentException("Dashboard failed");
    }
  }

  /**
   * View stub that throws a generic RuntimeException when displaying the dashboard.
   * Used to exercise the generic Exception catch block in showDashboard.
   */
  private static class ExceptionDashboardView extends TestGuiViewStub {

    @Override
    public void displayDashboard(CalendarAnalyticsSummary summary,
                                 LocalDate startDate, LocalDate endDate) {
      throw new RuntimeException("Boom");
    }
  }

  /**
   * Tests that showDashboard handles IllegalArgumentException thrown by the view
   * and forwards the message to displayError.
   */
  @Test
  public void testShowDashboardHandlesIllegalArgumentFromView() {
    CalendarManager localManager = new SimpleCalendarManager();
    IllegalArgumentDashboardView errorView = new IllegalArgumentDashboardView();
    GuiController localController = new GuiController(localManager, errorView);

    localManager.createCalendar("TestCalendar", ZoneId.of("America/New_York"));
    localManager.useCalendar("TestCalendar");

    LocalDate startDate = LocalDate.of(2024, 1, 1);
    LocalDate endDate = LocalDate.of(2024, 1, 31);

    localController.showDashboard(startDate, endDate);

    assertEquals("Exception message should be forwarded to displayError",
        "Dashboard failed", errorView.lastError);
    assertTrue("Dashboard should not be marked as displayed after error",
        !errorView.dashboardDisplayed);
  }

  /**
   * Tests that showDashboard handles a generic Exception thrown by the view
   * and prepends the correct prefix in displayError.
   */
  @Test
  public void testShowDashboardHandlesGenericExceptionFromView() {
    CalendarManager localManager = new SimpleCalendarManager();
    ExceptionDashboardView errorView = new ExceptionDashboardView();
    GuiController localController = new GuiController(localManager, errorView);

    localManager.createCalendar("TestCalendar", ZoneId.of("America/New_York"));
    localManager.useCalendar("TestCalendar");

    LocalDate startDate = LocalDate.of(2024, 2, 1);
    LocalDate endDate = LocalDate.of(2024, 2, 28);

    localController.showDashboard(startDate, endDate);

    assertEquals("Generic exception should be wrapped with dashboard prefix",
        "Error generating dashboard: Boom", errorView.lastError);
    assertTrue("Dashboard should not be marked as displayed after error",
        !errorView.dashboardDisplayed);
  }

  /**
   * Tests that parsing dashboard command succeeds but execution fails when no calendar is active.
   * This explicitly tests the parser-level behavior followed by command execution.
   */
  @Test(expected = IllegalStateException.class)
  public void testParseDashboardCommandExecutionWithNoActiveCalendar() throws Exception {
    TestModelStub emptyModel = new TestModelStub();

    TestGuiViewStub mockView = new TestGuiViewStub();

    QueryParser parser = new QueryParser();
    String command = "show calendar dashboard from 2024-01-01 to 2024-01-31";
    Command cmd = parser.parseCommand(command);

    assertNotNull("Command should be successfully parsed", cmd);
    assertTrue("Should return ShowDashboardCommand",
        cmd instanceof ShowDashboardCommand);

    cmd.execute(emptyModel, mockView);
  }
}