package calendar.controller.parser;

import calendar.controller.Command;
import calendar.controller.CommandParserUtils;
import calendar.controller.commands.EditEventCommand;
import calendar.controller.commands.EditEventsCommand;
import calendar.controller.commands.EditSeriesCommand;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses edit event commands (edit event, edit events, edit series).
 * Handles single event edits, series continuation edits, and full series edits.
 */
public class EditEventParser extends AbstractCommandParser {
  private static final String SUBJECT_PATTERN = "(?:\"([^\"]+)\"|(\\S+))";

  @Override
  public Command parseCommand(String commandLine) {
    if (startsWith(commandLine, "edit event ")) {
      return parseEditEvent(commandLine);
    } else if (startsWith(commandLine, "edit events ")) {
      return parseEditEvents(commandLine);
    } else if (startsWith(commandLine, "edit series ")) {
      return parseEditSeries(commandLine);
    }

    return super.parseCommand(commandLine);
  }

  private Command parseEditEvent(String commandLine) {
    Pattern pattern = Pattern.compile(
        "edit event (\\S+) " + SUBJECT_PATTERN
            + " from (\\S+) to (\\S+) with (.+)$");
    Matcher matcher = pattern.matcher(commandLine);

    if (matcher.matches()) {
      String property = matcher.group(1);
      String subject = CommandParserUtils.extractSubject(matcher, 2, 3);
      LocalDateTime start = CommandParserUtils.parseDateTime(matcher.group(4));
      LocalDateTime end = CommandParserUtils.parseDateTime(matcher.group(5));
      String newValue = stripQuotes(matcher.group(6).trim());

      return new EditEventCommand(subject, start, end, property, newValue);
    }

    throw new IllegalArgumentException("Invalid edit event syntax");
  }

  private Command parseEditEvents(String commandLine) {
    Pattern pattern = Pattern.compile(
        "edit events (\\w+) " + SUBJECT_PATTERN + " from (\\S+) with (.+)$");
    Matcher matcher = pattern.matcher(commandLine);

    if (matcher.matches()) {
      String property = matcher.group(1);
      String subject = CommandParserUtils.extractSubject(matcher, 2, 3);
      LocalDateTime start = CommandParserUtils.parseDateTime(matcher.group(4));
      String newValue = stripQuotes(matcher.group(5).trim());

      return new EditEventsCommand(subject, start, property, newValue);
    }

    throw new IllegalArgumentException("Invalid edit events syntax");
  }

  private Command parseEditSeries(String commandLine) {
    Pattern pattern = Pattern.compile(
        "edit series (\\w+) " + SUBJECT_PATTERN + " from (\\S+) with (.+)$");
    Matcher matcher = pattern.matcher(commandLine);

    if (matcher.matches()) {
      String property = matcher.group(1);
      String subject = CommandParserUtils.extractSubject(matcher, 2, 3);
      LocalDateTime start = CommandParserUtils.parseDateTime(matcher.group(4));
      String newValue = stripQuotes(matcher.group(5).trim());

      return new EditSeriesCommand(subject, start, property, newValue);
    }

    throw new IllegalArgumentException("Invalid edit series syntax");
  }

  private String stripQuotes(String value) {
    if (value.startsWith("\"") && value.endsWith("\"") && value.length() > 1) {
      return value.substring(1, value.length() - 1);
    }
    return value;
  }
}