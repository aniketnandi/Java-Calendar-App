package calendar.controller.parser;

import calendar.controller.Command;
import calendar.controller.ManagerCommand;

/**
 * Abstract base class for command parsers implementing Chain of Responsibility.
 * Provides common functionality for delegating to the next parser in the chain.
 */
public abstract class AbstractCommandParser implements CommandParserChain {
  protected CommandParserChain next;

  @Override
  public CommandParserChain setNext(CommandParserChain next) {
    this.next = next;
    return next;
  }

  @Override
  public Command parseCommand(String commandLine) {
    return next != null ? next.parseCommand(commandLine) : null;
  }

  @Override
  public ManagerCommand parseManagerCommand(String commandLine) {
    return next != null ? next.parseManagerCommand(commandLine) : null;
  }

  /**
   * Utility method to check if command starts with a specific prefix.
   *
   * @param commandLine the command to check
   * @param prefix the prefix to look for
   * @return true if command starts with prefix
   */
  protected boolean startsWith(String commandLine, String prefix) {
    if (commandLine == null) {
      return false;
    }
    return commandLine.trim().startsWith(prefix);
  }
}