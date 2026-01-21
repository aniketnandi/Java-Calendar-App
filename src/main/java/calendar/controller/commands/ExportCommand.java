package calendar.controller.commands;

import calendar.model.CalendarModel;
import calendar.view.CalendarView;
import java.io.IOException;
import java.time.ZoneId;

/**
 * Command to export the current calendar to a file.
 */
public class ExportCommand extends AbstractCommand {
  private final String fileName;

  /**
   * Constructs an ExportCommand to export the current calendar.
   *
   * @param filename the name of the file to export to (must include valid extension)
   */
  public ExportCommand(String filename) {
    this.fileName = filename;
  }

  @Override
  public void execute(CalendarModel model, CalendarView view) throws IOException {
    try {
      String trimmedFileName = fileName.trim();

      if (!trimmedFileName.contains(".")) {
        view.displayError("No file extension provided. Use .csv or .ical extension");
        return;
      }

      int lastDotIndex = trimmedFileName.lastIndexOf('.');
      String nameWithoutExt = trimmedFileName.substring(0, lastDotIndex);
      String extension = trimmedFileName.substring(lastDotIndex).toLowerCase();

      if (nameWithoutExt.isEmpty()) {
        view.displayError("Filename cannot be empty. Please provide a name before the extension");
        return;
      }

      String absolutePath;
      if (extension.equals(".csv")) {
        absolutePath = model.exportToCsv(trimmedFileName);
        view.displayMessage("Calendar exported successfully to: " + absolutePath);
      } else if (extension.equals(".ical")) {
        absolutePath = model.exportToIcal(trimmedFileName, ZoneId.systemDefault());
        view.displayMessage("Calendar exported successfully to: " + absolutePath);
      } else {
        view.displayError("Unsupported file format: '" + extension
            + "'. Supported formats: .csv, .ical");
      }
    } catch (IOException e) {
      view.displayError("Failed to export calendar: " + e.getMessage());
    } catch (IllegalArgumentException e) {
      view.displayError(e.getMessage());
    }
  }
}