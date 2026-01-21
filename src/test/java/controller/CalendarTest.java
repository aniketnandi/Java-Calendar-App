package controller;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import calendar.controller.Calendar;
import calendar.model.CalendarManager;
import calendar.model.SimpleCalendarManager;
import calendar.view.CalendarTextView;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for Calendar controller.
 */
public class CalendarTest {
  private CalendarManager manager;
  private StringBuilder output;
  private CalendarTextView view;

  /**
   * Prepares a fresh manager, output buffer, and view before each test.
   */
  @Before
  public void setUp() {
    manager = new SimpleCalendarManager();
    output = new StringBuilder();
    view = new CalendarTextView(output);
  }

  @Test
  public void testRunInteractiveExit() throws IOException {
    StringReader input = new StringReader("exit\n");
    Calendar controller = new Calendar(manager, view, input);

    controller.runInteractive();

    assertTrue(output.toString().contains("Calendar started"));
    assertTrue(output.toString().contains("terminated"));
  }

  @Test
  public void testRunInteractiveCreateCalendarAndEvent() throws IOException {
    String commands = "create calendar --name Work --timezone America/New_York\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-05-05T10:00 to 2025-05-05T11:00\n"
        + "exit\n";
    StringReader input = new StringReader(commands);
    Calendar controller = new Calendar(manager, view, input);

    controller.runInteractive();
    assertTrue(output.toString()
        .contains("Calendar 'Work' created successfully with timezone America/New_York"));
    assertTrue(output.toString().contains("Now using calendar: Work"));
    assertTrue(output.toString().contains("Event created successfully"));
  }

  @Test
  public void testRunInteractiveEmptyLines() throws IOException {
    StringReader input = new StringReader("\n\n\nexit\n");
    Calendar controller = new Calendar(manager, view, input);

    controller.runInteractive();

    assertTrue(output.toString().contains("terminated"));
  }

  @Test
  public void testRunInteractiveInvalidCommand() throws IOException {
    StringReader input = new StringReader("invalid command\nexit\n");
    Calendar controller = new Calendar(manager, view, input);

    controller.runInteractive();

    assertTrue(output.toString().contains("Error"));
  }

  @Test
  public void testRunInteractiveMultipleCommands() throws IOException {
    String commands = "create calendar --name Work --timezone America/New_York\n"
        + "use calendar --name Work\n"
        + "create event First from 2025-05-05T10:00 to 2025-05-05T11:00\n"
        + "create event Second from 2025-05-05T14:00 to 2025-05-05T15:00\n"
        + "print events on 2025-05-05\n"
        + "exit\n";
    StringReader input = new StringReader(commands);
    Calendar controller = new Calendar(manager, view, input);

    controller.runInteractive();

    String result = output.toString();
    assertTrue(result.contains("First"));
    assertTrue(result.contains("Second"));
  }

  @Test
  public void testRunInteractiveCaseInsensitiveExit() throws IOException {
    StringReader input = new StringReader("EXIT\n");
    Calendar controller = new Calendar(manager, view, input);

    controller.runInteractive();

    assertTrue(output.toString().contains("terminated"));
  }

  @Test
  public void testRunHeadlessWithValidFile() throws IOException {
    Path tempFile = Files.createTempFile("test", ".txt");
    String commands = "create calendar --name Work --timezone America/New_York\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-05-05T10:00 to 2025-05-05T11:00\n"
        + "print events on 2025-05-05\n"
        + "exit\n";
    Files.write(tempFile, commands.getBytes());

    StringReader input = new StringReader("");
    Calendar controller = new Calendar(manager, view, input);

    controller.runHeadless(tempFile.toString());

    String result = output.toString();
    assertTrue(result.contains("Event created successfully"));
    assertTrue(result.contains("Meeting"));

    Files.deleteIfExists(tempFile);
  }

  @Test
  public void testRunHeadlessWithEmptyLines() throws IOException {
    Path tempFile = Files.createTempFile("test", ".txt");
    String commands = "\n\ncreate calendar --name Work --timezone America/New_York\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-05-05T10:00 to 2025-05-05T11:00\n"
        + "exit\n";
    Files.write(tempFile, commands.getBytes());

    StringReader input = new StringReader("");
    Calendar controller = new Calendar(manager, view, input);

    controller.runHeadless(tempFile.toString());

    assertTrue(output.toString().contains("Event created successfully"));

    Files.deleteIfExists(tempFile);
  }

  @Test
  public void testRunHeadlessWithComments() throws IOException {
    Path tempFile = Files.createTempFile("test", ".txt");
    String commands = "# This is a comment\n"
        + "create calendar --name Work --timezone America/New_York\n"
        + "use calendar --name Work\n"
        + "# Another comment\n"
        + "create event Meeting from 2025-05-05T10:00 to 2025-05-05T11:00\n"
        + "exit\n";
    Files.write(tempFile, commands.getBytes());

    StringReader input = new StringReader("");
    Calendar controller = new Calendar(manager, view, input);

    controller.runHeadless(tempFile.toString());

    assertTrue(output.toString().contains("Event created successfully"));

    Files.deleteIfExists(tempFile);
  }

  @Test
  public void testRunHeadlessNoExit() throws IOException {
    Path tempFile = Files.createTempFile("test", ".txt");
    String commands = "create calendar --name Work --timezone America/New_York\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-05-05T10:00 to 2025-05-05T11:00\n";
    Files.write(tempFile, commands.getBytes());

    StringReader input = new StringReader("");
    Calendar controller = new Calendar(manager, view, input);

    controller.runHeadless(tempFile.toString());

    assertTrue(output.toString().contains("Error: Command file must end with 'exit'"));

    Files.deleteIfExists(tempFile);
  }

  @Test
  public void testRunHeadlessExitInMiddle() throws IOException {
    Path tempFile = Files.createTempFile("test", ".txt");
    String commands = "create calendar --name Work --timezone America/New_York\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-05-05T10:00 to 2025-05-05T11:00\n"
        + "exit\n"
        + "print events on 2025-05-05\n";
    Files.write(tempFile, commands.getBytes());

    StringReader input = new StringReader("");
    Calendar controller = new Calendar(manager, view, input);

    controller.runHeadless(tempFile.toString());

    String result = output.toString();
    assertTrue(result.contains("Event created successfully"));
    assertFalse(result.contains("Meeting starting on"));

    Files.deleteIfExists(tempFile);
  }

  @Test
  public void testRunHeadlessInvalidCommand() throws IOException {
    Path tempFile = Files.createTempFile("test", ".txt");
    Files.write(tempFile, "invalid command\nexit\n".getBytes());

    StringReader input = new StringReader("");
    Calendar controller = new Calendar(manager, view, input);

    controller.runHeadless(tempFile.toString());

    assertTrue(output.toString().contains("Error"));

    Files.deleteIfExists(tempFile);
  }

  @Test
  public void testRunHeadlessMultipleErrors() throws IOException {
    Path tempFile = Files.createTempFile("test", ".txt");
    Files.write(tempFile, "invalid1\ninvalid2\nexit\n".getBytes());

    StringReader input = new StringReader("");
    Calendar controller = new Calendar(manager, view, input);

    controller.runHeadless(tempFile.toString());

    String result = output.toString();
    int errorCount = 0;
    int index = 0;
    while ((index = result.indexOf("Error", index)) != -1) {
      errorCount++;
      index++;
    }
    assertTrue(errorCount >= 2);

    Files.deleteIfExists(tempFile);
  }

  @Test(expected = IOException.class)
  public void testRunHeadlessNonExistentFile() throws IOException {
    StringReader input = new StringReader("");
    Calendar controller = new Calendar(manager, view, input);

    controller.runHeadless("non_existent_file.txt");
  }

  @Test
  public void testRunHeadlessExportCommand() throws IOException {
    Path tempFile = Files.createTempFile("test", ".txt");
    String commands = "create calendar --name Work --timezone America/New_York\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-05-05T10:00 to 2025-05-05T11:00\n"
        + "export cal test_output.csv\n"
        + "exit\n";
    Files.write(tempFile, commands.getBytes());

    StringReader input = new StringReader("");
    Calendar controller = new Calendar(manager, view, input);

    controller.runHeadless(tempFile.toString());

    String result = output.toString();
    assertTrue(result.contains("Calendar exported successfully"));

    Files.deleteIfExists(tempFile);
    Files.deleteIfExists(Paths.get("exports/test_output.csv"));
  }

  @Test
  public void testRunInteractiveWithWhitespace() throws IOException {
    StringReader input = new StringReader("  exit  \n");
    Calendar controller = new Calendar(manager, view, input);

    controller.runInteractive();

    assertTrue(output.toString().contains("terminated"));
  }

  @Test
  public void testRunInteractiveNoCalendarInUse() throws IOException {
    String commands = "create event Meeting from 2025-05-05T10:00 to 2025-05-05T11:00\nexit\n";
    StringReader input = new StringReader(commands);
    Calendar controller = new Calendar(manager, view, input);

    controller.runInteractive();

    assertTrue(output.toString().contains("No calendar is currently in use"));
  }

  @Test
  public void testRunHeadlessWithManagerCommands() throws IOException {
    Path tempFile = Files.createTempFile("test", ".txt");
    String commands = "create calendar --name Work --timezone America/New_York\n"
        + "create calendar --name Personal --timezone America/Chicago\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-05-05T10:00 to 2025-05-05T11:00\n"
        + "copy event Meeting on 2025-05-05T10:00 --target Personal to 2025-05-06T10:00\n"
        + "exit\n";
    Files.write(tempFile, commands.getBytes());

    StringReader input = new StringReader("");
    Calendar controller = new Calendar(manager, view, input);
    controller.runHeadless(tempFile.toString());

    assertTrue(output.toString()
        .contains("Calendar 'Work' created successfully with timezone America/New_York"));
    assertTrue(output.toString()
        .contains("Calendar 'Personal' created successfully with timezone America/Chicago"));

    assertTrue(
        output.toString().contains("Event 'Meeting' copied successfully to calendar: Personal"));

    Files.deleteIfExists(tempFile);
  }

  @Test
  public void testRunInteractiveEditCalendar() throws IOException {
    String commands = "create calendar --name Work --timezone America/New_York\n"
        + "edit calendar --name Work --property name Office\n"
        + "exit\n";
    StringReader input = new StringReader(commands);
    Calendar controller = new Calendar(manager, view, input);

    controller.runInteractive();

    assertTrue(output.toString().contains("updated successfully"));
  }

  @Test
  public void testRunInteractiveCopyCommands() throws IOException {
    String commands = "create calendar --name Work --timezone America/New_York\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-05-05T10:00 to 2025-05-05T11:00\n"
        + "copy events on 2025-05-05 --target Work to 2025-05-10\n"
        + "exit\n";
    StringReader input = new StringReader(commands);
    Calendar controller = new Calendar(manager, view, input);

    controller.runInteractive();

    assertTrue(output.toString().contains("copied successfully"));
  }
}