package calendar.view;

import calendar.model.Event;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Immutable data class representing event edit form input.
 * Contains all edited properties from the event edit dialog.
 * Uses the actual Event object for precise identification.
 */
public final class EventEditData {
  private final Event originalEvent;
  private final String newSubject;
  private final LocalDateTime newStartDateTime;
  private final LocalDateTime newEndDateTime;
  private final String newLocation;
  private final String newDescription;
  private final String newStatus;
  private final String editScope;

  /**
   * Creates an immutable object holding all the user's event edit details.
   *
   * @param originalEvent the event being edited
   * @param newSubject the new subject
   * @param newStartDateTime the new start date and time
   * @param newEndDateTime the new end date and time
   * @param newLocation the new location
   * @param newDescription the new description
   * @param newStatus the new status
   * @param editScope the edit scope ("single", "from", or "all")
   */
  public EventEditData(Event originalEvent, String newSubject,
                       LocalDateTime newStartDateTime, LocalDateTime newEndDateTime,
                       String newLocation, String newDescription,
                       String newStatus, String editScope) {
    this.originalEvent = originalEvent;
    this.newSubject = newSubject;
    this.newStartDateTime = newStartDateTime;
    this.newEndDateTime = newEndDateTime;
    this.newLocation = newLocation;
    this.newDescription = newDescription;
    this.newStatus = newStatus;
    this.editScope = editScope;
  }

  /**
   * Gets the original event being edited.
   * This provides unambiguous identification even when multiple events
   * have the same subject.
   */
  public Event getOriginalEvent() {
    return originalEvent;
  }

  public String getNewSubject() {
    return newSubject;
  }

  public LocalDateTime getNewStartDateTime() {
    return newStartDateTime;
  }

  public LocalDateTime getNewEndDateTime() {
    return newEndDateTime;
  }

  public String getNewLocation() {
    return newLocation;
  }

  public String getNewDescription() {
    return newDescription;
  }

  public String getNewStatus() {
    return newStatus;
  }

  public String getEditScope() {
    return editScope;
  }

  @Override
  public String toString() {
    return String.format("EventEditData{event='%s' at %s, scope='%s'}",
        originalEvent.getSubject(),
        originalEvent.getStartDateTime(),
        editScope);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof EventEditData)) {
      return false;
    }
    EventEditData that = (EventEditData) o;
    return Objects.equals(originalEvent, that.originalEvent)
        && Objects.equals(newSubject, that.newSubject)
        && Objects.equals(newStartDateTime, that.newStartDateTime)
        && Objects.equals(newEndDateTime, that.newEndDateTime)
        && Objects.equals(newLocation, that.newLocation)
        && Objects.equals(newDescription, that.newDescription)
        && Objects.equals(newStatus, that.newStatus)
        && Objects.equals(editScope, that.editScope);
  }

  @Override
  public int hashCode() {
    return Objects.hash(originalEvent, newSubject, newStartDateTime,
        newEndDateTime, newLocation, newDescription, newStatus, editScope);
  }
}