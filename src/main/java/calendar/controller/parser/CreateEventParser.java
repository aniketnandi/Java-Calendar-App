package calendar.controller.parser;

import calendar.controller.Command;
import calendar.controller.CommandParserUtils;
import calendar.controller.commands.CreateEventCommand;
import calendar.controller.commands.EventSeriesCommand;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses "create event" commands.
 * Handles both single events and event series.
 */
public class CreateEventParser extends AbstractCommandParser {
  private static final String SUBJECT_PATTERN = "(?:\"([^\"]+)\"|(\\S+))";

  @Override
  public Command parseCommand(String commandLine) {
    if (!startsWith(commandLine, "create event ")) {
      return super.parseCommand(commandLine);
    }

    Command command;

    command = tryParseTimedSeries(commandLine);
    if (command != null) {
      return command;
    }

    command = tryParseSingleTimedEvent(commandLine);
    if (command != null) {
      return command;
    }

    command = tryParseAllDaySeries(commandLine);
    if (command != null) {
      return command;
    }

    command = tryParseSingleAllDayEvent(commandLine);
    if (command != null) {
      return command;
    }

    throw new IllegalArgumentException("Invalid create event syntax");
  }

  private Command tryParseTimedSeries(String commandLine) {
    Pattern seriesWithCount = Pattern.compile(
        "create event " + SUBJECT_PATTERN
            + " from (\\S+) to (\\S+) repeats ([MTWRFSU]+) for (\\d+) times$");
    Matcher matcher = seriesWithCount.matcher(commandLine);

    if (matcher.matches()) {
      String subject = CommandParserUtils.extractSubject(matcher, 1, 2);
      LocalDateTime start = CommandParserUtils.parseDateTime(matcher.group(3));
      LocalDateTime end = CommandParserUtils.parseDateTime(matcher.group(4));
      Set<DayOfWeek> days = CommandParserUtils.parseDays(matcher.group(5));
      int count = Integer.parseInt(matcher.group(6));
      return new EventSeriesCommand(subject, start, end, days, count, null);
    }

    Pattern seriesWithUntil = Pattern.compile(
        "create event " + SUBJECT_PATTERN
            + " from (\\S+) to (\\S+) repeats ([MTWRFSU]+) until (\\S+)$");
    matcher = seriesWithUntil.matcher(commandLine);

    if (matcher.matches()) {
      String subject = CommandParserUtils.extractSubject(matcher, 1, 2);
      LocalDateTime start = CommandParserUtils.parseDateTime(matcher.group(3));
      LocalDateTime end = CommandParserUtils.parseDateTime(matcher.group(4));
      Set<DayOfWeek> days = CommandParserUtils.parseDays(matcher.group(5));
      LocalDate until = CommandParserUtils.parseDate(matcher.group(6));
      return new EventSeriesCommand(subject, start, end, days, null, until);
    }

    return null;
  }

  private Command tryParseSingleTimedEvent(String commandLine) {
    Pattern pattern = Pattern.compile(
        "create event " + SUBJECT_PATTERN + " from (\\S+) to (\\S+)$");
    Matcher matcher = pattern.matcher(commandLine);

    if (matcher.matches()) {
      String subject = CommandParserUtils.extractSubject(matcher, 1, 2);
      LocalDateTime start = CommandParserUtils.parseDateTime(matcher.group(3));
      LocalDateTime end = CommandParserUtils.parseDateTime(matcher.group(4));
      return new CreateEventCommand(subject, start, end);
    }

    return null;
  }

  private Command tryParseAllDaySeries(String commandLine) {
    Pattern seriesWithCount = Pattern.compile(
        "create event " + SUBJECT_PATTERN
            + " on (\\S+) repeats ([MTWRFSU]+) for (\\d+) times$");
    Matcher matcher = seriesWithCount.matcher(commandLine);

    if (matcher.matches()) {
      String subject = CommandParserUtils.extractSubject(matcher, 1, 2);
      LocalDate date = CommandParserUtils.parseDate(matcher.group(3));
      Set<DayOfWeek> days = CommandParserUtils.parseDays(matcher.group(4));
      int count = Integer.parseInt(matcher.group(5));

      LocalDateTime start = date.atTime(8, 0);
      LocalDateTime end = date.atTime(17, 0);
      return new EventSeriesCommand(subject, start, end, days, count, null);
    }

    Pattern seriesWithUntil = Pattern.compile(
        "create event " + SUBJECT_PATTERN
            + " on (\\S+) repeats ([MTWRFSU]+) until (\\S+)$");
    matcher = seriesWithUntil.matcher(commandLine);

    if (matcher.matches()) {
      String subject = CommandParserUtils.extractSubject(matcher, 1, 2);
      LocalDate date = CommandParserUtils.parseDate(matcher.group(3));
      Set<DayOfWeek> days = CommandParserUtils.parseDays(matcher.group(4));
      LocalDate until = CommandParserUtils.parseDate(matcher.group(5));

      LocalDateTime start = date.atTime(8, 0);
      LocalDateTime end = date.atTime(17, 0);
      return new EventSeriesCommand(subject, start, end, days, null, until);
    }

    return null;
  }

  private Command tryParseSingleAllDayEvent(String commandLine) {
    Pattern pattern = Pattern.compile(
        "create event " + SUBJECT_PATTERN + " on (\\S+)$");
    Matcher matcher = pattern.matcher(commandLine);

    if (matcher.matches()) {
      String subject = CommandParserUtils.extractSubject(matcher, 1, 2);
      LocalDate date = CommandParserUtils.parseDate(matcher.group(3));

      LocalDateTime start = date.atTime(8, 0);
      LocalDateTime end = date.atTime(17, 0);
      return new CreateEventCommand(subject, start, end);
    }

    return null;
  }
}