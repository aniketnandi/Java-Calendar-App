package calendar.view;

import calendar.model.Event;

/**
 * Data class holding information about an event deletion request.
 */
public class EventDeleteData {
  private final Event event;
  private final String scope;

  /**
   * Creates an EventDeleteData instance.
   *
   * @param event the event to delete
   * @param scope the scope of deletion ("THIS", "THIS_AND_FUTURE", or "ALL")
   */
  public EventDeleteData(Event event, String scope) {
    this.event = event;
    this.scope = scope;
  }

  public Event getEvent() {
    return event;
  }

  public String getScope() {
    return scope;
  }
}