package calendar.controller.commands;

import calendar.model.CalendarAnalyticsSummary;
import calendar.model.CalendarModel;
import calendar.view.CalendarView;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Command that computes and displays a calendar analytics dashboard for a given
 * inclusive date interval.
 *
 * <p>When executed, this command:
 * <ol>
 *   <li>validates that the end date is not before the start date,</li>
 *   <li>invokes {@link CalendarModel#generateAnalytics(LocalDate, LocalDate)} on the
 *       currently active calendar model, and</li>
 *   <li>formats the resulting {@link CalendarAnalyticsSummary} into a multi-line textual
 *       dashboard that is rendered via the provided {@link CalendarView}.</li>
 * </ol></p>
 *
 * <p>The dashboard includes total events, average events per day, groupings by subject,
 * weekday, week index, and month, as well as busiest/least busy days and online versus
 * offline event statistics.</p>
 */
public class ShowDashboardCommand extends AbstractCommand {
  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd");

  private final LocalDate startDate;
  private final LocalDate endDate;

  /**
   * Constructs a new {@code ShowDashboardCommand} for the specified inclusive
   * date interval.
   *
   * @param startDate the start date of the interval (inclusive); must not be {@code null}
   * @param endDate the end date of the interval (inclusive); must not be {@code null}
   * @throws IllegalArgumentException if {@code startDate} or {@code endDate} is {@code null}
   */
  public ShowDashboardCommand(LocalDate startDate, LocalDate endDate) {
    this.startDate = startDate;
    this.endDate = endDate;
  }

  /**
   * Executes this command against the given model and view.
   *
   * <p>If the end date occurs before the start date, this method reports an error via
   * {@link CalendarView#displayError(String)} and returns without invoking the model.
   * Otherwise, it requests analytics for the inclusive interval
   * {@code [startDate, endDate]} from the {@link CalendarModel}, formats the
   * resulting {@link CalendarAnalyticsSummary} into a human-readable dashboard, and
   * displays it using {@link CalendarView#displayMessage(String)}.</p>
   *
   * @param model the calendar model used to compute analytics; must not be {@code null}
   * @param view the view used to display the dashboard or error messages; must not be
   *              {@code null}
   * @throws IOException if the underlying view encounters an I/O error while displaying
   *                     the dashboard or an error message
   */
  @Override
  public void execute(CalendarModel model, CalendarView view) throws IOException {
    if (endDate.isBefore(startDate)) {
      view.displayError("End date must not be before start date.");
      return;
    }

    CalendarAnalyticsSummary summary = model.generateAnalytics(startDate, endDate);

    StringBuilder builder = new StringBuilder();
    builder.append("Calendar dashboard from ")
        .append(startDate.format(DATE_FORMATTER))
        .append(" to ")
        .append(endDate.format(DATE_FORMATTER))
        .append(System.lineSeparator());

    builder.append("Total events: ")
        .append(summary.getTotalEvents())
        .append(System.lineSeparator());

    builder.append("Average events per day: ")
        .append(String.format("%.2f", summary.getAverageEventsPerDay()))
        .append(System.lineSeparator());

    builder.append("Events by subject:")
        .append(System.lineSeparator());
    appendIntMap(builder, summary.getEventsBySubject(), "  ");

    builder.append("Events by weekday:")
        .append(System.lineSeparator());
    appendIntMap(builder, summary.getEventsByWeekday(), "  ");

    builder.append("Events by week (index within interval):")
        .append(System.lineSeparator());
    appendIntMap(builder, summary.getEventsByWeekIndex(), "  ");

    builder.append("Events by month (YYYY-MM):")
        .append(System.lineSeparator());
    for (Map.Entry<java.time.YearMonth, Integer> entry
        : summary.getEventsByMonth().entrySet()) {
      builder.append("  ")
          .append(entry.getKey())
          .append(": ")
          .append(entry.getValue())
          .append(System.lineSeparator());
    }

    builder.append("Busiest day: ")
        .append(summary.getBusiestDay() == null
            ? "none"
            : summary.getBusiestDay().format(DATE_FORMATTER))
        .append(System.lineSeparator());

    builder.append("Least busy day: ")
        .append(summary.getLeastBusyDay() == null
            ? "none"
            : summary.getLeastBusyDay().format(DATE_FORMATTER))
        .append(System.lineSeparator());

    int online = summary.getOnlineEventsCount();
    int offline = summary.getOfflineEventsCount();
    int total = online + offline;

    builder.append("Online events: ")
        .append(online)
        .append(" (")
        .append(formatPercentage(online, total))
        .append(")")
        .append(System.lineSeparator());

    builder.append("Offline / other location events: ")
        .append(offline)
        .append(" (")
        .append(formatPercentage(offline, total))
        .append(")")
        .append(System.lineSeparator());

    view.displayMessage(builder.toString());
  }

  /**
   * Appends a formatted view of an integer-valued map to the given string builder.
   *
   * <p>If the map is empty, this method appends a single line containing the word
   * {@code "none"} with the given indentation. Otherwise, each entry is rendered on
   * its own line as {@code "<indent><key>: <value>"}.
   *
   * @param builder the {@link StringBuilder} to append output to; must not be {@code null}
   * @param map the map whose entries should be printed; must not be {@code null}
   * @param indent a prefix string used for each line (for indentation or labeling);
   *                must not be {@code null}
   */
  private void appendIntMap(StringBuilder builder, Map<?, Integer> map, String indent) {
    if (map.isEmpty()) {
      builder.append(indent)
          .append("none")
          .append(System.lineSeparator());
      return;
    }
    for (Map.Entry<?, Integer> entry : map.entrySet()) {
      builder.append(indent)
          .append(entry.getKey())
          .append(": ")
          .append(entry.getValue())
          .append(System.lineSeparator());
    }
  }

  /**
   * Formats a percentage value given a part and a total count.
   *
   * <p>If the {@code total} is less than or equal to zero, the method returns
   * {@code "0.00%"} to avoid division by zero. Otherwise, it computes
   * {@code (part / total) * 100} and formats the result with two decimal places
   * followed by a percent sign.
   *
   * @param part the numerator or subset count
   * @param total the denominator or total count
   * @return a string representation of the percentage with two decimal places,
   *         such as {@code "37.50%"}
   */
  private String formatPercentage(int part, int total) {
    if (total <= 0) {
      return "0.00%";
    }
    double percentage = (double) part * 100.0 / (double) total;
    return String.format("%.2f%%", percentage);
  }
}
