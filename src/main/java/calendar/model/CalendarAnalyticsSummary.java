package calendar.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Immutable value object representing analytics computed over a calendar for a
 * given inclusive date interval.
 *
 * <p>This summary aggregates several metrics about events in the interval, such as:
 * <ul>
 *   <li>total number of events,</li>
 *   <li>grouped counts by subject, weekday, week index, and month,</li>
 *   <li>average events per day,</li>
 *   <li>busiest and least busy days,</li>
 *   <li>and counts of online versus non-online events.</li>
 * </ul></p>
 *
 * <p>All maps exposed by this class are unmodifiable snapshots of the values at the time the
 * summary was created. Changes to the underlying calendar or builder after construction do
 * not affect instances of this class.</p>
 */
public final class CalendarAnalyticsSummary {
  private final int totalEvents;
  private final Map<String, Integer> eventsBySubject;
  private final Map<DayOfWeek, Integer> eventsByWeekday;
  private final Map<Integer, Integer> eventsByWeekIndex;
  private final Map<YearMonth, Integer> eventsByMonth;
  private final double averageEventsPerDay;
  private final LocalDate busiestDay;
  private final LocalDate leastBusyDay;
  private final int onlineEventsCount;
  private final int offlineEventsCount;

  private CalendarAnalyticsSummary(Builder builder) {
    this.totalEvents = builder.totalEvents;
    this.eventsBySubject = unmodifiableCopy(builder.eventsBySubject);
    this.eventsByWeekday = unmodifiableEnumCopy(builder.eventsByWeekday);
    this.eventsByWeekIndex = unmodifiableCopy(builder.eventsByWeekIndex);
    this.eventsByMonth = unmodifiableCopy(builder.eventsByMonth);
    this.averageEventsPerDay = builder.averageEventsPerDay;
    this.busiestDay = builder.busiestDay;
    this.leastBusyDay = builder.leastBusyDay;
    this.onlineEventsCount = builder.onlineEventsCount;
    this.offlineEventsCount = builder.offlineEventsCount;
  }

  private static <K> Map<K, Integer> unmodifiableCopy(Map<K, Integer> source) {
    if (source == null || source.isEmpty()) {
      return Collections.emptyMap();
    }
    return Collections.unmodifiableMap(new HashMap<>(source));
  }

  private static Map<DayOfWeek, Integer> unmodifiableEnumCopy(
      Map<DayOfWeek, Integer> source) {
    if (source == null || source.isEmpty()) {
      return Collections.emptyMap();
    }
    return Collections.unmodifiableMap(new EnumMap<>(source));
  }

  /**
   * Returns the total number of events considered in this summary for the
   * analyzed date interval.
   *
   * @return the total count of events in the interval
   */
  public int getTotalEvents() {
    return totalEvents;
  }

  /**
   * Returns the event counts grouped by subject.
   *
   * <p>The returned map is unmodifiable. Each key represents a subject string
   * and the corresponding value is the number of events with that subject
   * within the analyzed interval.</p>
   *
   * @return an unmodifiable map from subject name to event count
   */
  public Map<String, Integer> getEventsBySubject() {
    return eventsBySubject;
  }

  /**
   * Returns the event counts grouped by day of the week.
   *
   * <p>The returned map is unmodifiable. Each key represents a {@link DayOfWeek}
   * and the corresponding value is the number of events falling on that day
   * within the analyzed interval.</p>
   *
   * @return an unmodifiable map from {@link DayOfWeek} to event count
   */
  public Map<DayOfWeek, Integer> getEventsByWeekday() {
    return eventsByWeekday;
  }

  /**
   * Returns the event counts grouped by week index within the analyzed interval.
   *
   * <p>The first seven-day block starting at the interval's start date has index
   * {@code 1}, the next block has index {@code 2}, and so on. This allows callers
   * to reason about how events are distributed across successive weeks inside the
   * selected date range.</p>
   *
   * <p>The returned map is unmodifiable.</p>
   *
   * @return an unmodifiable map from week index (1-based) to event count
   */
  public Map<Integer, Integer> getEventsByWeekIndex() {
    return eventsByWeekIndex;
  }

  /**
   * Returns the event counts grouped by calendar month.
   *
   * <p>The keys in the returned map are {@link YearMonth} values, and the
   * corresponding values are the number of events that occur during that month
   * within the analyzed interval. The map is unmodifiable.</p>
   *
   * @return an unmodifiable map from {@link YearMonth} to event count
   */
  public Map<YearMonth, Integer> getEventsByMonth() {
    return eventsByMonth;
  }

  /**
   * Returns the average number of events per day over the analyzed date interval.
   *
   * <p>This value is typically computed as the total number of events divided by
   * the number of calendar days in the inclusive interval. The exact computation
   * is determined by the model that created this summary.</p>
   *
   * @return the average events per day as a {@code double}
   */
  public double getAverageEventsPerDay() {
    return averageEventsPerDay;
  }

  /**
   * Returns the busiest day within the analyzed date interval.
   *
   * <p>A day is considered "busiest" if it has the highest number of events in the
   * interval. If multiple days have the same maximum event count, the earliest such
   * {@link LocalDate} in the interval should be used when populating this field.</p>
   *
   * @return the busiest day, or {@code null} if the interval contains no events
   */
  public LocalDate getBusiestDay() {
    return busiestDay;
  }

  /**
   * Returns the least busy day within the analyzed date interval.
   *
   * <p>A day is considered "least busy" if it has the fewest events, including days
   * with zero events. If multiple days share the minimum event count, the earliest
   * such {@link LocalDate} in the interval should be used when populating this field.</p>
   *
   * @return the least busy day, or {@code null} if the interval itself has no days
   *         or no analytics could be computed
   */
  public LocalDate getLeastBusyDay() {
    return leastBusyDay;
  }

  /**
   * Returns the number of events classified as online within the analyzed interval.
   *
   * <p>The exact criteria for an event to be considered "online" are determined by
   * the model that created this summary (for example, based on the event location).</p>
   *
   * @return the count of online events
   */
  public int getOnlineEventsCount() {
    return onlineEventsCount;
  }

  /**
   * Returns the number of events that are not classified as online within the
   * analyzed interval.
   *
   * <p>This typically includes all events whose location does not match the
   * "online" criterion used by the model when computing this summary.</p>
   *
   * @return the count of non-online (offline or other) events
   */
  public int getOfflineEventsCount() {
    return offlineEventsCount;
  }

  /**
   * Creates a new {@link Builder} instance for constructing a
   * {@code CalendarAnalyticsSummary}.
   *
   * <p>The returned builder starts with default values (zero counts, empty maps,
   * and {@code null} dates) and can be populated using the fluent setter methods
   * before calling {@link Builder#build()}.</p>
   *
   * @return a new {@link Builder} for this summary type
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Fluent builder for creating immutable {@link CalendarAnalyticsSummary}
   * instances.
   *
   * <p>The builder holds mutable state while metrics are being assembled. Once
   * {@link #build()} is called, an immutable snapshot of the current state is
   * taken and stored in a new {@code CalendarAnalyticsSummary}. Subsequent
   * modifications to the builder do not affect already-built summary instances.</p>
   */
  public static final class Builder {
    private int totalEvents;
    private Map<String, Integer> eventsBySubject = new HashMap<>();
    private Map<DayOfWeek, Integer> eventsByWeekday = new EnumMap<>(DayOfWeek.class);
    private Map<Integer, Integer> eventsByWeekIndex = new HashMap<>();
    private Map<YearMonth, Integer> eventsByMonth = new HashMap<>();
    private double averageEventsPerDay;
    private LocalDate busiestDay;
    private LocalDate leastBusyDay;
    private int onlineEventsCount;
    private int offlineEventsCount;

    /**
     * Creates a new builder with empty maps and default values. All fields can
     * be configured using the provided setter methods before calling
     * {@link #build()}.
     */
    private Builder() {
    }

    /**
     * Sets the total number of events used to compute this summary.
     *
     * @param totalEvents the total count of events in the interval
     * @return this {@code Builder} instance for method chaining
     */
    public Builder totalEvents(int totalEvents) {
      this.totalEvents = totalEvents;
      return this;
    }

    /**
     * Sets the mapping of event counts grouped by subject.
     *
     * <p>Any existing subject-to-count mappings in this builder are cleared
     * before the provided entries are copied in.</p>
     *
     * @param eventsBySubject a map where each key is a subject name and each
     *                        value is the number of events with that subject;
     *                        if {@code null}, the builder keeps the current
     *                        (possibly empty) mapping
     * @return this {@code Builder} instance for method chaining
     */
    public Builder eventsBySubject(Map<String, Integer> eventsBySubject) {
      if (eventsBySubject != null) {
        this.eventsBySubject.clear();
        this.eventsBySubject.putAll(eventsBySubject);
      }
      return this;
    }

    /**
     * Sets the mapping of event counts grouped by weekday.
     *
     * <p>Any existing weekday-to-count mappings in this builder are cleared
     * before the provided entries are copied in.</p>
     *
     * @param eventsByWeekday a map where each key is a {@link DayOfWeek} and
     *                        each value is the number of events occurring on
     *                        that day; if {@code null}, the builder keeps the
     *                        current (possibly empty) mapping
     * @return this {@code Builder} instance for method chaining
     */
    public Builder eventsByWeekday(Map<DayOfWeek, Integer> eventsByWeekday) {
      if (eventsByWeekday != null) {
        this.eventsByWeekday.clear();
        this.eventsByWeekday.putAll(eventsByWeekday);
      }
      return this;
    }

    /**
     * Sets the mapping of event counts grouped by week index within the
     * analyzed interval.
     *
     * <p>The first seven-day block starting at the interval's start date is
     * assigned index {@code 1}, the next block index {@code 2}, and so on.
     * Any existing mappings in this builder are cleared before the provided
     * entries are copied in.</p>
     *
     * @param eventsByWeekIndex a map where each key is the 1-based week index
     *                          within the interval and each value is the
     *                          number of events in that week; if {@code null},
     *                          the builder keeps the current (possibly empty)
     *                          mapping
     * @return this {@code Builder} instance for method chaining
     */
    public Builder eventsByWeekIndex(Map<Integer, Integer> eventsByWeekIndex) {
      if (eventsByWeekIndex != null) {
        this.eventsByWeekIndex.clear();
        this.eventsByWeekIndex.putAll(eventsByWeekIndex);
      }
      return this;
    }

    /**
     * Sets the mapping of event counts grouped by calendar month.
     *
     * <p>Any existing month-to-count mappings in this builder are cleared
     * before the provided entries are copied in.</p>
     *
     * @param eventsByMonth a map where each key is a {@link YearMonth} and
     *                      each value is the number of events in that month;
     *                      if {@code null}, the builder keeps the current
     *                      (possibly empty) mapping
     * @return this {@code Builder} instance for method chaining
     */
    public Builder eventsByMonth(Map<YearMonth, Integer> eventsByMonth) {
      if (eventsByMonth != null) {
        this.eventsByMonth.clear();
        this.eventsByMonth.putAll(eventsByMonth);
      }
      return this;
    }

    /**
     * Sets the precomputed average number of events per day for the interval.
     *
     * <p>This value is typically the total number of events divided by the
     * number of days in the interval, but the exact computation is left to the
     * model constructing the summary.</p>
     *
     * @param averageEventsPerDay the average events per day as a {@code double}
     * @return this {@code Builder} instance for method chaining
     */
    public Builder averageEventsPerDay(double averageEventsPerDay) {
      this.averageEventsPerDay = averageEventsPerDay;
      return this;
    }

    /**
     * Sets the busiest day in the interval.
     *
     * <p>The supplied date should represent the day with the highest number of
     * events in the analyzed interval. In case of a tie, the earliest such date
     * should be provided by the caller.</p>
     *
     * @param busiestDay the {@link LocalDate} representing the busiest day, or
     *                   {@code null} if there are no events
     * @return this {@code Builder} instance for method chaining
     */
    public Builder busiestDay(LocalDate busiestDay) {
      this.busiestDay = busiestDay;
      return this;
    }

    /**
     * Sets the least busy day in the interval.
     *
     * <p>The supplied date should represent the day with the lowest number of
     * events, including days with zero events. In case of a tie, the earliest
     * such date in the interval should be provided by the caller.</p>
     *
     * @param leastBusyDay the {@link LocalDate} representing the least busy day,
     *                     or {@code null} if there are no days to analyze
     * @return this {@code Builder} instance for method chaining
     */
    public Builder leastBusyDay(LocalDate leastBusyDay) {
      this.leastBusyDay = leastBusyDay;
      return this;
    }

    /**
     * Sets the count of events classified as online.
     *
     * <p>The criteria for an event being considered online are determined by
     * the code constructing this summary (for example, based on its location).</p>
     *
     * @param onlineEventsCount the number of online events in the interval
     * @return this {@code Builder} instance for method chaining
     */
    public Builder onlineEventsCount(int onlineEventsCount) {
      this.onlineEventsCount = onlineEventsCount;
      return this;
    }

    /**
     * Sets the count of events that are not classified as online.
     *
     * <p>This typically includes offline events and any other events that do
     * not meet the "online" criteria used by the model.</p>
     *
     * @param offlineEventsCount the number of non-online events in the interval
     * @return this {@code Builder} instance for method chaining
     */
    public Builder offlineEventsCount(int offlineEventsCount) {
      this.offlineEventsCount = offlineEventsCount;
      return this;
    }

    /**
     * Builds a new immutable {@link CalendarAnalyticsSummary} instance using
     * the current state of this builder.
     *
     * <p>Subsequent changes to the builder do not affect the returned summary.</p>
     *
     * @return a new {@code CalendarAnalyticsSummary} containing the configured metrics
     */
    public CalendarAnalyticsSummary build() {
      return new CalendarAnalyticsSummary(this);
    }
  }
}
