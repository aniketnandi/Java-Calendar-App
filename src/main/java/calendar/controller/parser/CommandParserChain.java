package calendar.controller.parser;

import calendar.controller.Command;
import calendar.controller.ManagerCommand;

/**
 * Interface for command parsers using Chain of Responsibility pattern.
 * Each parser tries to parse a command, and if it can't, passes to the next parser.
 */
public interface CommandParserChain {

  /**
   * Attempts to parse a command string into a Command object.
   * Returns null if this parser cannot handle the command.
   *
   * @param commandLine the command string to parse
   * @return the parsed Command, or null if this parser can't handle it
   */
  Command parseCommand(String commandLine);

  /**
   * Attempts to parse a command string into a ManagerCommand object.
   * Returns null if this parser cannot handle the command.
   *
   * @param commandLine the command string to parse
   * @return the parsed ManagerCommand, or null if this parser can't handle it
   */
  ManagerCommand parseManagerCommand(String commandLine);

  /**
   * Sets the next parser in the chain.
   *
   * @param next the next parser to try if this one can't handle the command
   * @return the next parser (for fluent chaining)
   */
  CommandParserChain setNext(CommandParserChain next);
}