package calendar.controller.parser;

import calendar.controller.ManagerCommand;
import calendar.controller.commands.CreateCalendarCommand;
import calendar.controller.commands.EditCalendarCommand;
import calendar.controller.commands.UseCalendarCommand;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses calendar management commands (create, edit, use calendar).
 */
public class CalendarManagementParser extends AbstractCommandParser {

  /**
   * Parses copy event commands including single event, events on date, and events between dates.
   * Handles timezone conversion during the copy operation.
   *
   * @param commandLine the input command string
   * @return the corresponding ManagerCommand, or null if not a copy event command
   */
  @Override
  public ManagerCommand parseManagerCommand(String commandLine) {
    if (startsWith(commandLine, "create calendar ")) {
      return parseCreateCalendar(commandLine);
    } else if (startsWith(commandLine, "edit calendar ")) {
      return parseEditCalendar(commandLine);
    } else if (startsWith(commandLine, "use calendar ")) {
      return parseUseCalendar(commandLine);
    }

    return super.parseManagerCommand(commandLine);
  }

  private ManagerCommand parseCreateCalendar(String commandLine) {
    Pattern pattern = Pattern.compile(
        "create calendar --name (\\S+) --timezone ([\\w/]+)$");
    Matcher matcher = pattern.matcher(commandLine);

    if (matcher.matches()) {
      String name = matcher.group(1);
      String timezone = matcher.group(2);
      return new CreateCalendarCommand(name, timezone);
    }

    throw new IllegalArgumentException(
        "Invalid create calendar syntax. Expected: "
            + "create calendar --name <n> --timezone <timezone>");
  }

  private ManagerCommand parseEditCalendar(String commandLine) {
    Pattern pattern = Pattern.compile(
        "edit calendar --name (\\S+) --property (\\w+) (.+)$");
    Matcher matcher = pattern.matcher(commandLine);

    if (matcher.matches()) {
      String name = matcher.group(1);
      String property = matcher.group(2);
      String value = matcher.group(3).trim();
      return new EditCalendarCommand(name, property, value);
    }

    throw new IllegalArgumentException(
        "Invalid edit calendar syntax. Expected: "
            + "edit calendar --name <n> --property <property> <value>");
  }

  private ManagerCommand parseUseCalendar(String commandLine) {
    Pattern pattern = Pattern.compile("use calendar --name (\\S+)$");
    Matcher matcher = pattern.matcher(commandLine);

    if (matcher.matches()) {
      String name = matcher.group(1);
      return new UseCalendarCommand(name);
    }

    throw new IllegalArgumentException(
        "Invalid use calendar syntax. Expected: use calendar --name <n>");
  }
}