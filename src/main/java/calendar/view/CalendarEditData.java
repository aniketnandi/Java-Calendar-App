package calendar.view;

/**
 * Immutable data class representing calendar edit form input.
 */
public final class CalendarEditData {
  private final String property;
  private final String newValue;

  /**
   * Creates a new immutable edit request for a calendar.
   *
   * @param property the calendar field to update (e.g., "name" or "timezone")
   * @param newValue the new value to assign to that field
   */
  public CalendarEditData(String property, String newValue) {
    this.property = property;
    this.newValue = newValue;
  }

  public String getProperty() {
    return property;
  }

  public String getNewValue() {
    return newValue;
  }
}