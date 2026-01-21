package analytics;

import calendar.model.CalendarAnalyticsSummary;
import java.time.LocalDate;

/**
 * Configurable mock implementation of CalendarModel for testing purposes.
 * Unlike TestModelStub which always throws an exception for generateAnalytics,
 * this stub allows you to configure what analytics summary should be returned
 * via the setSummary() method. Useful for testing command execution with
 * predetermined analytics results.
 */
public class TestConfigurableModelStub extends TestModelStub {
  private CalendarAnalyticsSummary summary;

  /**
   * Sets the analytics summary that will be returned by generateAnalytics().
   *
   * @param summary the summary to return
   */
  public void setSummary(CalendarAnalyticsSummary summary) {
    this.summary = summary;
  }

  @Override
  public CalendarAnalyticsSummary generateAnalytics(LocalDate start, LocalDate end) {
    return summary;
  }
}