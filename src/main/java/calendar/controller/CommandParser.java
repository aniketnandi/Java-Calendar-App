package calendar.controller;

import calendar.controller.parser.CalendarManagementParser;
import calendar.controller.parser.CommandParserChain;
import calendar.controller.parser.CopyEventsParser;
import calendar.controller.parser.CreateEventParser;
import calendar.controller.parser.EditEventParser;
import calendar.controller.parser.ExportParser;
import calendar.controller.parser.QueryParser;

/**
 * Main command parser that orchestrates a chain of specialized parsers.
 */
public class CommandParser {
  private final CommandParserChain parserChain;

  /**
   * Creates a CommandParser with a chain of specialized parsers.
   */
  public CommandParser() {
    this.parserChain = setupParserChain();
  }

  /**
   * Sets up the chain of responsibility for parsing commands.
   *
   * @return the head of the parser chain
   */
  private CommandParserChain setupParserChain() {
    CommandParserChain createEventParser = new CreateEventParser();
    CommandParserChain editEventParser = new EditEventParser();
    CommandParserChain queryParser = new QueryParser();
    CommandParserChain exportParser = new ExportParser();
    CommandParserChain calendarMgmtParser = new CalendarManagementParser();
    CommandParserChain copyEventsParser = new CopyEventsParser();

    createEventParser
        .setNext(editEventParser)
        .setNext(queryParser)
        .setNext(exportParser)
        .setNext(calendarMgmtParser)
        .setNext(copyEventsParser);

    return createEventParser;
  }

  /**
   * Parses a regular command (event operations).
   *
   * @param commandLine the input command string
   * @return the corresponding Command object
   * @throws IllegalArgumentException if the command format is invalid
   */
  public Command parseCommand(String commandLine) throws IllegalArgumentException {
    if (commandLine == null || commandLine.trim().isEmpty()) {
      throw new IllegalArgumentException("Command cannot be empty");
    }

    Command command = parserChain.parseCommand(commandLine);

    if (command == null) {
      throw new IllegalArgumentException("Unknown command: " + commandLine);
    }

    return command;
  }

  /**
   * Parses manager commands specific to calendar management operations.
   *
   * @param commandLine the input command string
   * @return the corresponding ManagerCommand object
   * @throws IllegalArgumentException if the command format is invalid
   */
  public ManagerCommand parseManagerCommand(String commandLine)
      throws IllegalArgumentException {
    if (commandLine == null || commandLine.trim().isEmpty()) {
      throw new IllegalArgumentException("Command cannot be empty");
    }

    ManagerCommand command = parserChain.parseManagerCommand(commandLine);

    if (command == null) {
      throw new IllegalArgumentException("Unknown manager command: " + commandLine);
    }

    return command;
  }
}