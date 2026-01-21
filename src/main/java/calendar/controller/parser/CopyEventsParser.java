package calendar.controller.parser;

import calendar.controller.CommandParserUtils;
import calendar.controller.ManagerCommand;
import calendar.controller.commands.CopyEventCommand;
import calendar.controller.commands.CopyEventsBetweenCommand;
import calendar.controller.commands.CopyEventsOnDateCommand;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses copy event commands (copy event, copy events on, copy events between).
 */
public class CopyEventsParser extends AbstractCommandParser {
  private static final String SUBJECT_PATTERN = "(?:\"([^\"]+)\"|(\\S+))";

  @Override
  public ManagerCommand parseManagerCommand(String commandLine) {
    if (startsWith(commandLine, "copy event ") && commandLine.contains(" on ")) {
      return parseCopyEvent(commandLine);
    } else if (startsWith(commandLine, "copy events on ")) {
      return parseCopyEventsOnDate(commandLine);
    } else if (startsWith(commandLine, "copy events between ")) {
      return parseCopyEventsBetween(commandLine);
    }

    return super.parseManagerCommand(commandLine);
  }

  private ManagerCommand parseCopyEvent(String commandLine) {
    Pattern pattern = Pattern.compile(
        "copy event " + SUBJECT_PATTERN + " on (\\S+) --target (\\S+) to (\\S+)$");
    Matcher matcher = pattern.matcher(commandLine);

    if (matcher.matches()) {
      String subject = CommandParserUtils.extractSubject(matcher, 1, 2);
      LocalDateTime sourceDateTime = CommandParserUtils.parseDateTime(matcher.group(3));
      String targetCalendar = matcher.group(4);
      LocalDateTime targetDateTime = CommandParserUtils.parseDateTime(matcher.group(5));
      return new CopyEventCommand(subject, sourceDateTime, targetCalendar, targetDateTime);
    }

    throw new IllegalArgumentException(
        "Invalid copy event syntax. Expected: "
            + "copy event <n> on <datetime> --target <cal> to <datetime>");
  }

  private ManagerCommand parseCopyEventsOnDate(String commandLine) {
    Pattern pattern = Pattern.compile(
        "copy events on (\\S+) --target (\\S+) to (\\S+)$");
    Matcher matcher = pattern.matcher(commandLine);

    if (matcher.matches()) {
      LocalDate sourceDate = CommandParserUtils.parseDate(matcher.group(1));
      String targetCalendar = matcher.group(2);
      LocalDate targetDate = CommandParserUtils.parseDate(matcher.group(3));
      return new CopyEventsOnDateCommand(sourceDate, targetCalendar, targetDate);
    }

    throw new IllegalArgumentException(
        "Invalid copy events on syntax. Expected: "
            + "copy events on <date> --target <cal> to <date>");
  }

  private ManagerCommand parseCopyEventsBetween(String commandLine) {
    Pattern pattern = Pattern.compile(
        "copy events between (\\S+) and (\\S+) --target (\\S+) to (\\S+)$");
    Matcher matcher = pattern.matcher(commandLine);

    if (matcher.matches()) {
      LocalDate startDate = CommandParserUtils.parseDate(matcher.group(1));
      LocalDate endDate = CommandParserUtils.parseDate(matcher.group(2));
      String targetCalendar = matcher.group(3);
      LocalDate targetStartDate = CommandParserUtils.parseDate(matcher.group(4));
      return new CopyEventsBetweenCommand(startDate, endDate, targetCalendar, targetStartDate);
    }

    throw new IllegalArgumentException(
        "Invalid copy events between syntax. Expected: "
            + "copy events between <date> and <date> --target <cal> to <date>");
  }
}