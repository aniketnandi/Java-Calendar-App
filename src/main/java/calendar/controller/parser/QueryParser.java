package calendar.controller.parser;

import calendar.controller.Command;
import calendar.controller.CommandParserUtils;
import calendar.controller.commands.PrintEventsBetweenCommand;
import calendar.controller.commands.PrintEventsOnCommand;
import calendar.controller.commands.ShowDashboardCommand;
import calendar.controller.commands.ShowStatusCommand;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses query and export commands (print events, show status, export).
 */
public class QueryParser extends AbstractCommandParser {

  @Override
  public Command parseCommand(String commandLine) {
    if (startsWith(commandLine, "print events on ")) {
      return parsePrintEventsOn(commandLine);
    } else if (startsWith(commandLine, "print events from ")) {
      return parsePrintEventsFrom(commandLine);
    } else if (startsWith(commandLine, "show status on ")) {
      return parseShowStatus(commandLine);
    } else if (commandLine.startsWith("show calendar dashboard from ")) {
      return parseDashboard(commandLine);
    }

    return super.parseCommand(commandLine);
  }

  private Command parsePrintEventsOn(String commandLine) {
    Pattern pattern = Pattern.compile("print events on (\\S+)$");
    Matcher matcher = pattern.matcher(commandLine);

    if (matcher.matches()) {
      LocalDate date = CommandParserUtils.parseDate(matcher.group(1));
      return new PrintEventsOnCommand(date);
    }

    throw new IllegalArgumentException("Invalid print events on syntax");
  }

  private Command parsePrintEventsFrom(String commandLine) {
    Pattern pattern = Pattern.compile("print events from (\\S+) to (\\S+)$");
    Matcher matcher = pattern.matcher(commandLine);

    if (matcher.matches()) {
      LocalDateTime start = CommandParserUtils.parseDateTime(matcher.group(1));
      LocalDateTime end = CommandParserUtils.parseDateTime(matcher.group(2));
      return new PrintEventsBetweenCommand(start, end);
    }

    throw new IllegalArgumentException("Invalid print events from syntax");
  }

  private Command parseShowStatus(String commandLine) {
    Pattern pattern = Pattern.compile("show status on (\\S+)$");
    Matcher matcher = pattern.matcher(commandLine);

    if (matcher.matches()) {
      LocalDateTime dateTime = CommandParserUtils.parseDateTime(matcher.group(1));
      return new ShowStatusCommand(dateTime);
    }

    throw new IllegalArgumentException("Invalid show status syntax");
  }

  private Command parseDashboard(String commandLine) {
    Pattern pattern = Pattern.compile(
        "show calendar dashboard from (\\d{4}-\\d{2}-\\d{2}) to (\\d{4}-\\d{2}-\\d{2})$");
    Matcher matcher = pattern.matcher(commandLine);

    if (!matcher.matches()) {
      throw new IllegalArgumentException(
          "Invalid dashboard syntax. Expected: "
              + "show calendar dashboard from YYYY-MM-DD to YYYY-MM-DD");
    }

    try {
      LocalDate startDate = LocalDate.parse(matcher.group(1));
      LocalDate endDate = LocalDate.parse(matcher.group(2));
      return new ShowDashboardCommand(startDate, endDate);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid date format. Expected YYYY-MM-DD", e);
    }
  }
}