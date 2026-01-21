package calendar.controller.parser;

import calendar.controller.Command;
import calendar.controller.commands.ExportCommand;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses query and export commands (print events, show status, export).
 */
public class ExportParser extends AbstractCommandParser {

  @Override
  public Command parseCommand(String commandLine) {
    if (startsWith(commandLine, "export cal ")) {
      return parseExportCal(commandLine);
    }

    return super.parseCommand(commandLine);
  }

  private Command parseExportCal(String commandLine) {
    Pattern pattern = Pattern.compile("export cal (.+)$");
    Matcher matcher = pattern.matcher(commandLine);

    if (matcher.matches()) {
      String fileName = matcher.group(1).trim();
      return new ExportCommand(fileName);
    }

    throw new IllegalArgumentException("Invalid export syntax");
  }
}