import calendar.controller.Calendar;
import calendar.controller.CalendarController;
import calendar.controller.GuiController;
import calendar.model.CalendarManager;
import calendar.model.SimpleCalendarManager;
import calendar.view.CalendarGuiView;
import calendar.view.CalendarTextView;
import calendar.view.CalendarView;
import calendar.view.SwingCalendarView;
import java.io.InputStreamReader;

/**
 * Main entry point for the Calendar application.
 * Supports three modes of operation:
 * GUI mode, Interactive mode, Headless mode
 */
public class CalendarRunner {

  /**
   * Main method to run the calendar application.
   *
   * @param args command line arguments
   */
  public static void main(String[] args) {
    if (args.length == 0) {
      launchGuI();
      return;
    }

    if (args.length < 2) {
      showUsageAndExit();
      return;
    }

    String modeFlag = args[0].toLowerCase();
    String mode = args[1].toLowerCase();

    if (!modeFlag.equals("--mode")) {
      System.err.println("First argument must be --mode");
      showUsageAndExit();
      return;
    }

    CalendarManager manager = new SimpleCalendarManager();

    try {
      switch (mode) {
        case "interactive":
          launchInteractive(manager);
          break;

        case "headless":
          if (args.length < 3) {
            System.err.println("Headless mode requires a filepath");
            showUsageAndExit();
            return;
          }
          launchHeadless(manager, args[2]);
          break;

        default:
          System.err.println("Unknown mode: " + mode);
          showUsageAndExit();
      }
    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
      System.exit(1);
    }
  }

  /**
   * Launches the GUI mode.
   * Creates a GUI view and GUI controller.
   */
  private static void launchGuI() {
    javax.swing.SwingUtilities.invokeLater(() -> {
      CalendarManager manager = new SimpleCalendarManager();
      CalendarGuiView view = new SwingCalendarView();
      GuiController controller = new GuiController(manager, view);
      controller.start();
    });
  }

  /**
   * Launches interactive text mode.
   * Creates a text view and synchronous controller.
   */
  private static void launchInteractive(CalendarManager manager) {
    try {
      CalendarView view = new CalendarTextView(System.out);
      CalendarController controller = new Calendar(manager, view,
          new InputStreamReader(System.in));
      controller.runInteractive();
    } catch (Exception e) {
      System.err.println("Error in interactive mode: " + e.getMessage());
      System.exit(1);
    }
  }

  /**
   * Launches headless mode with script file.
   * Creates a text view and synchronous controller.
   */
  private static void launchHeadless(CalendarManager manager, String scriptFile) {
    try {
      CalendarView view = new CalendarTextView(System.out);
      CalendarController controller = new Calendar(manager, view,
          new InputStreamReader(System.in));
      controller.runHeadless(scriptFile);
    } catch (Exception e) {
      System.err.println("Error in headless mode: " + e.getMessage());
      System.exit(1);
    }
  }

  /**
   * Displays usage information and exits.
   */
  private static void showUsageAndExit() {
    System.err.println("Usage:");
    System.err.println("  java -jar JARNAME.jar                          (GUI mode)");
    System.err.println("  java -jar JARNAME.jar --mode interactive       (Interactive text mode)");
    System.err.println("  java -jar JARNAME.jar --mode headless <file>   (Headless script mode)");
    System.exit(1);
  }
}