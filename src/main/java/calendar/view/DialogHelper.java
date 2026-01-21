package calendar.view;

import calendar.model.Event;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.JRadioButton;

/**
 * Utility methods for common dialog operations.
 */
public class DialogHelper {

  /**
   * Updates visibility of series-related radio buttons based on whether
   * the selected event is part of a recurring series.
   *
   * @param events        the list of events
   * @param selectedIndex the index of the selected event
   * @param singleRadio   radio button for single event action
   * @param fromRadio     radio button for "this and future" action
   * @param allRadio      radio button for "all in series" action
   */
  public static void updateSeriesRadioVisibility(
      List<Event> events,
      int selectedIndex,
      JRadioButton singleRadio,
      JRadioButton fromRadio,
      JRadioButton allRadio) {

    if (selectedIndex < 0 || selectedIndex >= events.size()) {
      return;
    }

    Event selectedEvent = events.get(selectedIndex);
    boolean isPartOfSeries = selectedEvent.getSeriesId() != null
        && !selectedEvent.getSeriesId().isEmpty();

    fromRadio.setVisible(isPartOfSeries);
    allRadio.setVisible(isPartOfSeries);

    if (!isPartOfSeries) {
      singleRadio.setSelected(true);
    }
  }

  /**
   * Formats an event for display in a dropdown or list.
   *
   * @param event the event to format
   * @return formatted string
   */
  public static String formatEventForDisplay(Event event) {
    LocalDate startDate = event.getStartDateTime().toLocalDate();
    LocalDate endDate = event.getEndDateTime().toLocalDate();

    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM d");
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");

    if (startDate.equals(endDate)) {
      String time = event.isAllDay() ? "All Day" :
          event.getStartDateTime().format(timeFormatter) + " - "
              + event.getEndDateTime().format(timeFormatter);
      return String.format("%s (%s)", event.getSubject(), time);
    }

    String time = startDate.format(dateFormatter) + " "
        + event.getStartDateTime().format(timeFormatter) + " - "
        + endDate.format(dateFormatter) + " "
        + event.getEndDateTime().format(timeFormatter);
    return String.format("%s (%s)", event.getSubject(), time);
  }
}