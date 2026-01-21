package controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import calendar.controller.Command;
import calendar.controller.CommandParser;
import calendar.controller.CommandParserUtils;
import calendar.controller.ManagerCommand;
import calendar.controller.commands.CopyEventCommand;
import calendar.controller.commands.CopyEventsBetweenCommand;
import calendar.controller.commands.CopyEventsOnDateCommand;
import calendar.controller.commands.CreateCalendarCommand;
import calendar.controller.commands.CreateEventCommand;
import calendar.controller.commands.EditCalendarCommand;
import calendar.controller.commands.EditEventCommand;
import calendar.controller.commands.EditEventsCommand;
import calendar.controller.commands.EditSeriesCommand;
import calendar.controller.commands.EventSeriesCommand;
import calendar.controller.commands.ExportCommand;
import calendar.controller.commands.PrintEventsBetweenCommand;
import calendar.controller.commands.PrintEventsOnCommand;
import calendar.controller.commands.ShowStatusCommand;
import calendar.controller.commands.UseCalendarCommand;
import java.time.DayOfWeek;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for CommandParser.
 */
public class CommandParserTest {
  private CommandParser parser;

  /**
   * Initializes a new CommandParser instance before each test.
   */
  @Before
  public void setUp() {
    parser = new CommandParser();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseNullCommand() {
    parser.parseCommand(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseEmptyCommand() {
    parser.parseCommand("");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseWhitespaceCommand() {
    parser.parseCommand("   ");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseUnknownCommand() {
    parser.parseCommand("invalid command");
  }

  @Test
  public void testParseSingleTimedEvent() {
    String cmd = "create event Meeting from 2025-05-05T10:00 to 2025-05-05T11:00";
    Command result = parser.parseCommand(cmd);
    assertTrue(result instanceof CreateEventCommand);
  }

  @Test
  public void testParseSingleTimedEventQuoted() {
    String cmd = "create event \"Team Meeting\" from 2025-05-05T10:00 "
        + "to 2025-05-05T11:00";
    Command result = parser.parseCommand(cmd);
    assertTrue(result instanceof CreateEventCommand);
  }

  @Test
  public void testParseSingleAllDayEvent() {
    Command cmd = parser.parseCommand("create event Lunch on 2025-05-05");
    assertTrue(cmd instanceof CreateEventCommand);
  }

  @Test
  public void testParseSingleAllDayEventQuoted() {
    Command cmd = parser.parseCommand("create event \"Team Lunch\" on 2025-05-05");
    assertTrue(cmd instanceof CreateEventCommand);
  }

  @Test
  public void testParseTimedSeriesWithCount() {
    String cmd = "create event Standup from 2025-05-05T09:00 to 2025-05-05T09:30 "
        + "repeats MTW for 6 times";
    Command result = parser.parseCommand(cmd);
    assertTrue(result instanceof EventSeriesCommand);
  }

  @Test
  public void testParseTimedSeriesWithCountQuoted() {
    String cmd = "create event \"Daily Standup\" from 2025-05-05T09:00 "
        + "to 2025-05-05T09:30 repeats MTW for 6 times";
    Command result = parser.parseCommand(cmd);
    assertTrue(result instanceof EventSeriesCommand);
  }

  @Test
  public void testParseTimedSeriesWithUntil() {
    String cmd = "create event Standup from 2025-05-05T09:00 to 2025-05-05T09:30 "
        + "repeats MTWRF until 2025-05-20";
    Command result = parser.parseCommand(cmd);
    assertTrue(result instanceof EventSeriesCommand);
  }

  @Test
  public void testParseTimedSeriesWithUntilQuoted() {
    String cmd = "create event \"Daily Standup\" from 2025-05-05T09:00 "
        + "to 2025-05-05T09:30 repeats MTWRF until 2025-05-20";
    Command result = parser.parseCommand(cmd);
    assertTrue(result instanceof EventSeriesCommand);
  }

  @Test
  public void testParseAllDaySeriesWithCount() {
    Command cmd = parser.parseCommand("create event Gym on 2025-05-06 repeats MWF for 5 times");
    assertTrue(cmd instanceof EventSeriesCommand);
  }

  @Test
  public void testParseAllDaySeriesWithCountQuoted() {
    String cmd = "create event \"Gym Session\" on 2025-05-06 repeats MWF for 5 times";
    Command result = parser.parseCommand(cmd);
    assertTrue(result instanceof EventSeriesCommand);
  }

  @Test
  public void testParseAllDaySeriesWithUntil() {
    Command cmd = parser.parseCommand("create event Gym on 2025-05-06 repeats MW until 2025-05-20");
    assertTrue(cmd instanceof EventSeriesCommand);
  }

  @Test
  public void testParseAllDaySeriesWithUntilQuoted() {
    String cmd = "create event \"Gym Session\" on 2025-05-06 repeats MWF until 2025-05-20";
    Command result = parser.parseCommand(cmd);
    assertTrue(result instanceof EventSeriesCommand);
  }

  @Test
  public void testParseAllWeekdays() {
    String cmd = "create event Test on 2025-05-06 repeats MTWRFSU for 1 times";
    Command result = parser.parseCommand(cmd);
    assertTrue(result instanceof EventSeriesCommand);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseInvalidWeekdayCharacter() {
    parser.parseCommand("create event Test on 2025-05-06 repeats XYZ for 1 times");
  }

  @Test
  public void testParseEditEvent() {
    String cmd = "edit event subject Meeting from 2025-05-05T10:00 "
        + "to 2025-05-05T11:00 with \"New Meeting\"";
    Command result = parser.parseCommand(cmd);
    assertTrue(result instanceof EditEventCommand);
  }

  @Test
  public void testParseEditEventQuotedSubject() {
    String cmd = "edit event subject \"Team Meeting\" from 2025-05-05T10:00 "
        + "to 2025-05-05T11:00 with \"New Meeting\"";
    Command result = parser.parseCommand(cmd);
    assertTrue(result instanceof EditEventCommand);
  }

  @Test
  public void testParseEditEventAllProperties() {
    String cmd1 = "edit event subject Meeting from 2025-05-05T10:00 "
        + "to 2025-05-05T11:00 with NewSubject";
    parser.parseCommand(cmd1);
    String cmd2 = "edit event description Meeting from 2025-05-05T10:00 "
        + "to 2025-05-05T11:00 with NewDesc";
    parser.parseCommand(cmd2);
    String cmd3 = "edit event location Meeting from 2025-05-05T10:00 "
        + "to 2025-05-05T11:00 with NewLoc";
    parser.parseCommand(cmd3);
    String cmd4 = "edit event status Meeting from 2025-05-05T10:00 "
        + "to 2025-05-05T11:00 with PRIVATE";
    parser.parseCommand(cmd4);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseEditEventInvalid() {
    parser.parseCommand("edit event invalid syntax");
  }

  @Test
  public void testParseEditEvents() {
    String cmd = "edit events subject Meeting from 2025-05-05T10:00 with \"New Meeting\"";
    Command result = parser.parseCommand(cmd);
    assertTrue(result instanceof EditEventsCommand);
  }

  @Test
  public void testParseEditEventsQuotedSubject() {
    String cmd = "edit events subject \"Team Meeting\" from 2025-05-05T10:00 "
        + "with \"New Meeting\"";
    Command result = parser.parseCommand(cmd);
    assertTrue(result instanceof EditEventsCommand);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseEditEventsInvalid() {
    parser.parseCommand("edit events invalid syntax");
  }

  @Test
  public void testParseEditSeries() {
    String cmd = "edit series subject Meeting from 2025-05-05T10:00 with \"New Meeting\"";
    Command result = parser.parseCommand(cmd);
    assertTrue(result instanceof EditSeriesCommand);
  }

  @Test
  public void testParseEditSeriesQuotedSubject() {
    String cmd = "edit series subject \"Team Meeting\" from 2025-05-05T10:00 "
        + "with \"New Meeting\"";
    Command result = parser.parseCommand(cmd);
    assertTrue(result instanceof EditSeriesCommand);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseEditSeriesInvalid() {
    parser.parseCommand("edit series invalid syntax");
  }

  @Test
  public void testParsePrintEventsOn() {
    Command cmd = parser.parseCommand("print events on 2025-05-05");
    assertTrue(cmd instanceof PrintEventsOnCommand);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParsePrintEventsOnInvalid() {
    parser.parseCommand("print events on invalid");
  }

  @Test
  public void testParsePrintEventsFrom() {
    String cmd = "print events from 2025-05-05T10:00 to 2025-05-05T11:00";
    Command result = parser.parseCommand(cmd);
    assertTrue(result instanceof PrintEventsBetweenCommand);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParsePrintEventsFromInvalid() {
    parser.parseCommand("print events from invalid");
  }

  @Test
  public void testParseShowStatus() {
    Command cmd = parser.parseCommand("show status on 2025-05-05T10:00");
    assertTrue(cmd instanceof ShowStatusCommand);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseShowStatusInvalid() {
    parser.parseCommand("show status on invalid");
  }

  @Test
  public void testParseExportCal() {
    Command cmd = parser.parseCommand("export cal calendar.csv");
    assertTrue(cmd instanceof ExportCommand);
  }

  @Test
  public void testParseExportCalWithPath() {
    Command cmd = parser.parseCommand("export cal exports/calendar.csv");
    assertTrue(cmd instanceof ExportCommand);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseExportCalInvalid() {
    parser.parseCommand("export cal");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseInvalidDateFormat() {
    parser.parseCommand("create event Test on 05-05-2025");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseInvalidDateTimeFormat() {
    parser.parseCommand("create event Test from 2025-05-05 10:00 to 2025-05-05T11:00");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseInvalidDate() {
    parser.parseCommand("create event Test on 2025-13-45");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseInvalidDateTime() {
    parser.parseCommand("create event Test from 2025-05-05T25:99 to 2025-05-05T11:00");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCreateEventIncomplete() {
    parser.parseCommand("create event");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParsePrintEventsIncomplete() {
    parser.parseCommand("print events");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseEditEventIncomplete() {
    parser.parseCommand("edit event subject");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseShowStatusIncomplete() {
    parser.parseCommand("show status");
  }

  @Test
  public void testParseCreateCalendar() {
    ManagerCommand cmd = parser.parseManagerCommand(
        "create calendar --name Work --timezone America/New_York");
    assertTrue(cmd instanceof CreateCalendarCommand);
  }

  @Test
  public void testParseEditCalendar() {
    ManagerCommand cmd = parser.parseManagerCommand(
        "edit calendar --name Work --property name Office");
    assertTrue(cmd instanceof EditCalendarCommand);
  }

  @Test
  public void testParseUseCalendar() {
    ManagerCommand cmd = parser.parseManagerCommand(
        "use calendar --name Work");
    assertTrue(cmd instanceof UseCalendarCommand);
  }

  @Test
  public void testParseCopyEvent() {
    ManagerCommand cmd = parser.parseManagerCommand(
        "copy event Meeting on 2025-05-05T10:00 --target Personal to 2025-05-06T10:00");
    assertTrue(cmd instanceof CopyEventCommand);
  }

  @Test
  public void testParseCopyEventQuoted() {
    ManagerCommand cmd = parser.parseManagerCommand(
        "copy event \"Team Meeting\" on 2025-05-05T10:00 --target Personal to 2025-05-06T10:00");
    assertTrue(cmd instanceof CopyEventCommand);
  }

  @Test
  public void testParseCopyEventsOnDate() {
    ManagerCommand cmd = parser.parseManagerCommand(
        "copy events on 2025-05-05 --target Personal to 2025-05-10");
    assertTrue(cmd instanceof CopyEventsOnDateCommand);
  }

  @Test
  public void testParseCopyEventsBetween() {
    ManagerCommand cmd = parser.parseManagerCommand(
        "copy events between 2025-05-05 and 2025-05-10 --target Personal to 2025-06-01");
    assertTrue(cmd instanceof CopyEventsBetweenCommand);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCreateCalendarInvalid() {
    parser.parseManagerCommand("create calendar invalid");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseEditCalendarInvalid() {
    parser.parseManagerCommand("edit calendar invalid");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseUseCalendarInvalid() {
    parser.parseManagerCommand("use calendar");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCopyEventInvalid() {
    parser.parseManagerCommand("copy event invalid");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseManagerCommandNull() {
    parser.parseManagerCommand(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseManagerCommandEmpty() {
    parser.parseManagerCommand("");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseManagerCommandUnknown() {
    parser.parseManagerCommand("unknown manager command");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseUseCalendarInvalidFormat() {
    parser.parseManagerCommand("use calendar --name");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseUseCalendarMissingDashes() {
    parser.parseManagerCommand("use calendar name Work");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCopyEventInvalidFormat() {
    parser.parseManagerCommand("copy event Meeting on 2025-05-05T10:00 to 2025-05-06T10:00");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCopyEventMissingTarget() {
    parser.parseManagerCommand(
        "copy event Meeting on 2025-05-05T10:00 target Work to 2025-05-06T10:00");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCopyEventsOnDateInvalidFormat() {
    parser.parseManagerCommand("copy events on 2025-05-05 to 2025-05-10");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCopyEventsOnDateMissingTarget() {
    parser.parseManagerCommand("copy events on 2025-05-05 target Work to 2025-05-10");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCopyEventsBetweenInvalidFormat() {
    parser.parseManagerCommand("copy events between 2025-05-05 to 2025-05-10");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCopyEventsBetweenMissingAnd() {
    parser.parseManagerCommand(
        "copy events between 2025-05-05 2025-05-10 --target Work to 2025-06-01");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCopyEventsBetweenMissingTarget() {
    parser.parseManagerCommand("copy events between 2025-05-05 and 2025-05-10 to 2025-06-01");
  }

  @Test
  public void testEditEventWithQuotedValue() {
    CommandParser parser = new CommandParser();

    Command cmd = parser.parseCommand(
        "edit event subject Meeting from "
            + "2025-05-05T10:00 to 2025-05-05T11:00 with \"New Meeting\"");

    assertTrue(cmd instanceof EditEventCommand);
  }

  @Test
  public void testEditEventWithUnquotedValue() {
    CommandParser parser = new CommandParser();

    Command cmd = parser.parseCommand(
        "edit event subject Meeting from "
            + "2025-05-05T10:00 to 2025-05-05T11:00 with NewMeeting");

    assertTrue(cmd instanceof EditEventCommand);
  }

  @Test
  public void testEditEventWithSingleQuote() {
    CommandParser parser = new CommandParser();

    Command cmd = parser.parseCommand(
        "edit event subject Meeting from "
            + "2025-05-05T10:00 to 2025-05-05T11:00 with \"");

    assertTrue(cmd instanceof EditEventCommand);
  }

  @Test
  public void testEditEventWithEmptyQuotes() {
    CommandParser parser = new CommandParser();

    Command cmd = parser.parseCommand(
        "edit event subject Meeting from 2025-05-05T10:00 "
            + "to 2025-05-05T11:00 with \"\"");

    assertTrue(cmd instanceof EditEventCommand);
  }

  @Test
  public void testEditEventsWithQuotedValue() {
    CommandParser parser = new CommandParser();

    Command cmd = parser.parseCommand(
        "edit events subject Meeting from 2025-05-05T10:00 with \"Updated Meeting\"");

    assertTrue(cmd instanceof EditEventsCommand);
  }

  @Test
  public void testEditEventsWithUnquotedValue() {
    CommandParser parser = new CommandParser();

    Command cmd = parser.parseCommand(
        "edit events subject Meeting from 2025-05-05T10:00 with UpdatedMeeting");

    assertTrue(cmd instanceof EditEventsCommand);
  }

  @Test
  public void testEditSeriesWithQuotedValue() {
    CommandParser parser = new CommandParser();

    Command cmd = parser.parseCommand(
        "edit series subject Meeting from 2025-05-05T10:00 with \"Series Update\"");

    assertTrue(cmd instanceof EditSeriesCommand);
  }

  @Test
  public void testEditSeriesWithUnquotedValue() {
    CommandParser parser = new CommandParser();

    Command cmd = parser.parseCommand(
        "edit series subject Meeting from 2025-05-05T10:00 with SeriesUpdate");

    assertTrue(cmd instanceof EditSeriesCommand);
  }

  @Test
  public void testStripQuotesWithValueStartingWithQuote() {
    CommandParser parser = new CommandParser();

    Command cmd = parser.parseCommand(
        "edit event subject Meeting from 2025-05-05T10:00 to 2025-05-05T11:00 with \"NoEndQuote");

    assertTrue(cmd instanceof EditEventCommand);
  }

  @Test
  public void testStripQuotesWithValueEndingWithQuote() {
    CommandParser parser = new CommandParser();

    Command cmd = parser.parseCommand(
        "edit event subject Meeting from 2025-05-05T10:00 "
            + "to 2025-05-05T11:00 with NoStartQuote\"");

    assertTrue(cmd instanceof EditEventCommand);
  }

  @Test
  public void testStripQuotesWithMultipleWords() {
    CommandParser parser = new CommandParser();

    Command cmd = parser.parseCommand(
        "edit event subject Meeting from 2025-05-05T10:00"
            + " to 2025-05-05T11:00 with \"This is a long description\"");

    assertTrue(cmd instanceof EditEventCommand);
  }

  @Test
  public void testStripQuotesWithSingleCharacterQuoted() {
    CommandParser parser = new CommandParser();

    Command cmd = parser.parseCommand(
        "edit event subject Meeting from 2025-05-05T10:00 to 2025-05-05T11:00 with \"A\"");

    assertTrue(cmd instanceof EditEventCommand);
  }

  @Test
  public void testStripQuotesPreservesInternalQuotes() {
    CommandParser parser = new CommandParser();

    Command cmd = parser.parseCommand(
        "edit event subject Meeting from 2025-05-05T10:00"
            + " to 2025-05-05T11:00 with \"Say \\\"hello\\\"\"");

    assertTrue(cmd instanceof EditEventCommand);
  }

  @Test
  public void testParseDaysReturnsNonEmptySet() {
    Set<DayOfWeek> days = CommandParserUtils.parseDays("M");

    assertNotNull(days);
    assertFalse(days.isEmpty());
    assertEquals(1, days.size());
    assertTrue(days.contains(DayOfWeek.MONDAY));
  }

  @Test
  public void testParseDaysWithMultipleDays() {
    Set<DayOfWeek> days = CommandParserUtils.parseDays("MTW");

    assertNotNull(days);
    assertFalse(days.isEmpty());
    assertEquals(3, days.size());
    assertTrue(days.contains(DayOfWeek.MONDAY));
    assertTrue(days.contains(DayOfWeek.TUESDAY));
    assertTrue(days.contains(DayOfWeek.WEDNESDAY));
  }

  @Test
  public void testParseDaysWithAllDays() {
    Set<DayOfWeek> days = CommandParserUtils.parseDays("MTWRFSU");

    assertNotNull(days);
    assertFalse(days.isEmpty());
    assertEquals(7, days.size());
    assertTrue(days.contains(DayOfWeek.MONDAY));
    assertTrue(days.contains(DayOfWeek.TUESDAY));
    assertTrue(days.contains(DayOfWeek.WEDNESDAY));
    assertTrue(days.contains(DayOfWeek.THURSDAY));
    assertTrue(days.contains(DayOfWeek.FRIDAY));
    assertTrue(days.contains(DayOfWeek.SATURDAY));
    assertTrue(days.contains(DayOfWeek.SUNDAY));
  }

  @Test
  public void testParseDaysSingleDayThursday() {
    Set<DayOfWeek> days = CommandParserUtils.parseDays("R");

    assertNotNull(days);
    assertEquals(1, days.size());
    assertTrue(days.contains(DayOfWeek.THURSDAY));
  }

  @Test
  public void testParseDaysSingleDayFriday() {
    Set<DayOfWeek> days = CommandParserUtils.parseDays("F");

    assertNotNull(days);
    assertEquals(1, days.size());
    assertTrue(days.contains(DayOfWeek.FRIDAY));
  }

  @Test
  public void testParseDaysSingleDaySaturday() {
    Set<DayOfWeek> days = CommandParserUtils.parseDays("S");

    assertNotNull(days);
    assertEquals(1, days.size());
    assertTrue(days.contains(DayOfWeek.SATURDAY));
  }

  @Test
  public void testParseDaysSingleDaySunday() {
    Set<DayOfWeek> days = CommandParserUtils.parseDays("U");

    assertNotNull(days);
    assertEquals(1, days.size());
    assertTrue(days.contains(DayOfWeek.SUNDAY));
  }

  @Test
  public void testParseDaysWeekendDays() {
    Set<DayOfWeek> days = CommandParserUtils.parseDays("SU");

    assertNotNull(days);
    assertEquals(2, days.size());
    assertTrue(days.contains(DayOfWeek.SATURDAY));
    assertTrue(days.contains(DayOfWeek.SUNDAY));
  }

  @Test
  public void testParseDaysWeekdays() {
    Set<DayOfWeek> days = CommandParserUtils.parseDays("MTWRF");

    assertNotNull(days);
    assertEquals(5, days.size());
    assertTrue(days.contains(DayOfWeek.MONDAY));
    assertTrue(days.contains(DayOfWeek.TUESDAY));
    assertTrue(days.contains(DayOfWeek.WEDNESDAY));
    assertTrue(days.contains(DayOfWeek.THURSDAY));
    assertTrue(days.contains(DayOfWeek.FRIDAY));
  }
}