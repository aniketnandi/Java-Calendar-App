package calendar.controller;

import calendar.model.CalendarManager;
import calendar.model.CalendarModel;
import calendar.model.CalendarModelAdapter;
import calendar.view.CalendarView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Implementation of CalendarController that handles command execution
 * in both interactive and headless modes.
 * Uses a hybrid approach with two types of commands:
 * - Command: Operates on CalendarModel (existing event commands)
 * - ManagerCommand: Operates on CalendarManager (new calendar management commands)
 * An adapter bridges the gap, allowing old commands to work with the new architecture.
 */
public class Calendar implements CalendarController {
  private final CalendarManager manager;
  private final CalendarModel modelAdapter;
  private final CalendarView view;
  private final BufferedReader input;
  private final CommandParser parser;

  /**
   * Creates a Calendar controller with the specified manager, view, and input source.
   *
   * @param manager the calendar manager to operate on
   * @param view the view to display output
   * @param input the input source for reading commands
   */
  public Calendar(CalendarManager manager, CalendarView view, Reader input) {
    this.manager = manager;
    this.modelAdapter = new CalendarModelAdapter(manager);
    this.view = view;
    this.parser = new CommandParser();
    this.input = new BufferedReader(input);
  }

  @Override
  public void runInteractive() throws IOException {
    view.displayMessage("Calendar started. Enter commands (type 'exit' to quit): ");

    String line;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      if (line.isEmpty()) {
        continue;
      }
      if (line.equalsIgnoreCase("exit")) {
        view.displayMessage("Calendar has been terminated.");
        break;
      }
      try {
        executeCommand(line);
      } catch (Exception e) {
        view.displayError(e.getMessage());
      }
    }
  }

  @Override
  public void runHeadless(String commandsFile) throws IOException {
    List<String> commands = Files.readAllLines(Paths.get(commandsFile));
    boolean hasExit = false;
    for (String line : commands) {
      line = line.trim();
      if (line.isEmpty() || line.startsWith("#")) {
        continue;
      }
      if (line.equalsIgnoreCase("exit")) {
        hasExit = true;
        break;
      }
      try {
        executeCommand(line);
      } catch (Exception e) {
        view.displayError(e.getMessage());
      }
    }
    if (!hasExit) {
      view.displayError("Error: Command file must end with 'exit'");
    }
  }

  /**
   * Executes a command by routing it to the appropriate handler.
   * Manager commands work with CalendarManager directly.
   * Model commands work through the adapter.
   *
   * @param commandLine the command string
   * @throws IOException if there's an I/O error
   */
  private void executeCommand(String commandLine) throws IOException {
    if (isManagerCommand(commandLine)) {
      ManagerCommand command = parser.parseManagerCommand(commandLine);
      command.execute(manager, view);
    } else {
      Command command = parser.parseCommand(commandLine);
      command.execute(modelAdapter, view);
    }
  }

  /**
   * Determines if a command is a manager-level command.
   *
   * @param commandLine the command string
   * @return true if it's a manager command, false otherwise
   */
  private boolean isManagerCommand(String commandLine) {
    return commandLine.startsWith("create calendar ")
        || commandLine.startsWith("edit calendar ")
        || commandLine.startsWith("use calendar ")
        || (commandLine.startsWith("copy event ") && commandLine.contains(" on "))
        || commandLine.startsWith("copy events on ")
        || commandLine.startsWith("copy events between ");
  }
}