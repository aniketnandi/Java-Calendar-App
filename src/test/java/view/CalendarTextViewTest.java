package view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import calendar.model.CalendarEvent;
import calendar.model.Event;
import calendar.model.Status;
import calendar.view.CalendarTextView;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for CalendarTextView.
 */
public class CalendarTextViewTest {
  private StringBuilder output;
  private CalendarTextView view;

  /**
   * Prepares a fresh output buffer and view before each test run.
   */
  @Before
  public void setUp() {
    output = new StringBuilder();
    view = new CalendarTextView(output);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorNullOutput() {
    new CalendarTextView(null);
  }

  @Test
  public void testDisplayMessage() {
    view.displayMessage("Test message");
    assertEquals("Test message\n\n", output.toString());
  }

  @Test
  public void testDisplayMessageMultiple() {
    view.displayMessage("First");
    view.displayMessage("Second");
    assertEquals("First\n\nSecond\n\n", output.toString());
  }

  @Test
  public void testDisplayError() {
    view.displayError("Error message");
    assertEquals("Error: Error message\n\n", output.toString());
  }

  @Test
  public void testDisplayEventsEmpty() {
    view.displayEvents(new ArrayList<>());
    assertEquals("No events found\n\n", output.toString());
  }

  @Test
  public void testDisplayEventsNull() {
    view.displayEvents(null);
    assertEquals("No events found\n\n", output.toString());
  }

  @Test
  public void testDisplayEventsSingle() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();

    List<Event> events = new ArrayList<>();
    events.add(event);

    view.displayEvents(events);
    String result = output.toString();
    assertTrue(result.contains("Meeting"));
    assertTrue(result.contains("2025-05-05"));
    assertTrue(result.contains("10:00"));
    assertTrue(result.contains("11:00"));
  }

  @Test
  public void testDisplayEventsWithDescription() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .description("Important meeting")
        .build();

    List<Event> events = new ArrayList<>();
    events.add(event);

    view.displayEvents(events);
    String result = output.toString();
    assertTrue(result.contains("Description: Important meeting"));
  }

  @Test
  public void testDisplayEventsWithLocation() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .location("Room A")
        .build();

    List<Event> events = new ArrayList<>();
    events.add(event);

    view.displayEvents(events);
    String result = output.toString();
    assertTrue(result.contains("Location: Room A"));
  }

  @Test
  public void testDisplayEventsWithStatus() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .status(Status.PRIVATE)
        .build();

    List<Event> events = new ArrayList<>();
    events.add(event);

    view.displayEvents(events);
    String result = output.toString();
    assertTrue(result.contains("Status: PRIVATE"));
  }

  @Test
  public void testDisplayEventsWithAllProperties() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .description("Important")
        .location("Room A")
        .status(Status.PRIVATE)
        .build();

    List<Event> events = new ArrayList<>();
    events.add(event);

    view.displayEvents(events);
    String result = output.toString();
    assertTrue(result.contains("Location: Room A"));
    assertTrue(result.contains("Status: PRIVATE"));
    assertTrue(result.contains("Description: Important"));
  }

  @Test
  public void testDisplayEventsMultiple() {
    Event event1 = new CalendarEvent.EventBuilder()
        .subject("First")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();

    Event event2 = new CalendarEvent.EventBuilder()
        .subject("Second")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 14, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 15, 0))
        .build();

    List<Event> events = new ArrayList<>();
    events.add(event1);
    events.add(event2);

    view.displayEvents(events);
    String result = output.toString();
    assertTrue(result.contains("First"));
    assertTrue(result.contains("Second"));
  }

  @Test
  public void testDisplayStatusBusy() {
    view.displayStatus(true);
    assertEquals("Busy\n\n", output.toString());
  }

  @Test
  public void testDisplayStatusAvailable() {
    view.displayStatus(false);
    assertEquals("Available\n\n", output.toString());
  }

  @Test
  public void testDisplayEventFormatting() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .build();

    List<Event> events = new ArrayList<>();
    events.add(event);

    view.displayEvents(events);
    String result = output.toString();

    assertTrue(result.contains("starting on"));
    assertTrue(result.contains("at"));
    assertTrue(result.contains("ending on"));
  }

  @Test
  public void testDisplayEventsEmptyDescription() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .description("")
        .build();

    List<Event> events = new ArrayList<>();
    events.add(event);

    view.displayEvents(events);
    String result = output.toString();
    assertFalse(result.contains("Description:"));
  }

  @Test
  public void testDisplayEventsEmptyLocation() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .location("")
        .build();

    List<Event> events = new ArrayList<>();
    events.add(event);

    view.displayEvents(events);
    String result = output.toString();
    assertFalse(result.contains("Location:"));
  }

  @Test(expected = RuntimeException.class)
  public void testDisplayMessageWithIoException() {
    Appendable failingAppendable = new Appendable() {
      @Override
      public Appendable append(CharSequence csq) throws IOException {
        throw new IOException("Test exception");
      }

      @Override
      public Appendable append(CharSequence csq, int start, int end) throws IOException {
        throw new IOException("Test exception");
      }

      @Override
      public Appendable append(char c) throws IOException {
        throw new IOException("Test exception");
      }
    };

    CalendarTextView failingView = new CalendarTextView(failingAppendable);
    failingView.displayMessage("Test");
  }

  @Test
  public void testDisplayEventsWithNullLocation() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .location(null)
        .build();

    List<Event> events = new ArrayList<>();
    events.add(event);

    view.displayEvents(events);
    String result = output.toString();
    assertFalse(result.contains("Location:"));
  }

  @Test
  public void testDisplayEventsWithNullDescription() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .description(null)
        .build();

    List<Event> events = new ArrayList<>();
    events.add(event);

    view.displayEvents(events);
    String result = output.toString();
    assertFalse(result.contains("Description:"));
  }

  @Test
  public void testDisplayEventsLocationNotNullNotEmpty() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .location("Conference Room")
        .build();

    List<Event> events = new ArrayList<>();
    events.add(event);

    view.displayEvents(events);
    String result = output.toString();
    assertTrue(result.contains("Location: Conference Room"));
  }

  @Test
  public void testDisplayEventsDescriptionNotNullNotEmpty() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2025, 5, 5, 10, 0))
        .endDateTime(LocalDateTime.of(2025, 5, 5, 11, 0))
        .description("Important meeting")
        .build();

    List<Event> events = new ArrayList<>();
    events.add(event);

    view.displayEvents(events);
    String result = output.toString();
    assertTrue(result.contains("Description: Important meeting"));
  }
}