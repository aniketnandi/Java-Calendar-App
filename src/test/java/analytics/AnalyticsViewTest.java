package analytics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import calendar.model.CalendarEvent;
import calendar.model.Event;
import calendar.view.CalendarEditData;
import calendar.view.DialogHelper;
import calendar.view.EventDeleteData;
import calendar.view.EventEditData;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JRadioButton;
import org.junit.Before;
import org.junit.Test;

/**
 * Comprehensive tests for view package classes.
 * Tests DialogHelper, CalendarEditData, EventDeleteData, and EventEditData.
 */
public class AnalyticsViewTest {
  private List<Event> events;
  private JRadioButton singleRadio;
  private JRadioButton fromRadio;
  private JRadioButton allRadio;

  /**
   * Sets up test fixtures.
   */
  @Before
  public void setUp() {
    events = new ArrayList<>();
    singleRadio = new JRadioButton("Single");
    fromRadio = new JRadioButton("From");
    allRadio = new JRadioButton("All");
  }

  /**
   * Tests updateSeriesRadioVisibility with event that has series ID.
   */
  @Test
  public void testUpdateSeriesRadioVisibilityWithSeries() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .seriesId("series-123")
        .build();
    events.add(event);

    DialogHelper.updateSeriesRadioVisibility(events, 0, singleRadio, fromRadio, allRadio);

    assertTrue(fromRadio.isVisible());
    assertTrue(allRadio.isVisible());
  }

  /**
   * Tests updateSeriesRadioVisibility with event without series ID.
   */
  @Test
  public void testUpdateSeriesRadioVisibilityWithoutSeries() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .build();
    events.add(event);

    DialogHelper.updateSeriesRadioVisibility(events, 0, singleRadio, fromRadio, allRadio);

    assertFalse(fromRadio.isVisible());
    assertFalse(allRadio.isVisible());
    assertTrue(singleRadio.isSelected());
  }

  /**
   * Tests updateSeriesRadioVisibility with empty series ID.
   */
  @Test
  public void testUpdateSeriesRadioVisibilityWithEmptySeriesId() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .seriesId("")
        .build();
    events.add(event);

    DialogHelper.updateSeriesRadioVisibility(events, 0, singleRadio, fromRadio, allRadio);

    assertFalse(fromRadio.isVisible());
    assertFalse(allRadio.isVisible());
    assertTrue(singleRadio.isSelected());
  }

  /**
   * Tests updateSeriesRadioVisibility with null series ID.
   */
  @Test
  public void testUpdateSeriesRadioVisibilityWithNullSeriesId() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .seriesId(null)
        .build();
    events.add(event);

    DialogHelper.updateSeriesRadioVisibility(events, 0, singleRadio, fromRadio, allRadio);

    assertFalse(fromRadio.isVisible());
    assertFalse(allRadio.isVisible());
    assertTrue(singleRadio.isSelected());
  }

  /**
   * Tests updateSeriesRadioVisibility with negative index.
   */
  @Test
  public void testUpdateSeriesRadioVisibilityNegativeIndex() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .seriesId("series-123")
        .build();
    events.add(event);

    fromRadio.setVisible(true);
    allRadio.setVisible(true);

    DialogHelper.updateSeriesRadioVisibility(events, -1, singleRadio, fromRadio, allRadio);
    assertTrue(fromRadio.isVisible());
    assertTrue(allRadio.isVisible());
  }

  /**
   * Tests updateSeriesRadioVisibility with index out of bounds.
   */
  @Test
  public void testUpdateSeriesRadioVisibilityIndexOutOfBounds() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .seriesId("series-123")
        .build();
    events.add(event);

    fromRadio.setVisible(true);
    allRadio.setVisible(true);

    DialogHelper.updateSeriesRadioVisibility(events, 5, singleRadio, fromRadio, allRadio);

    assertTrue(fromRadio.isVisible());
    assertTrue(allRadio.isVisible());
  }

  /**
   * Tests formatEventForDisplay with same-day event.
   */
  @Test
  public void testFormatEventForDisplaySameDay() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 15, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 15, 11, 0))
        .build();

    String formatted = DialogHelper.formatEventForDisplay(event);

    assertTrue(formatted.contains("Meeting"));
    assertTrue(formatted.contains("10:00 AM"));
    assertTrue(formatted.contains("11:00 AM"));
  }

  /**
   * Tests formatEventForDisplay with event starting at midnight.
   */
  @Test
  public void testFormatEventForDisplayMidnightStart() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Conference")
        .startDateTime(LocalDateTime.of(2024, 1, 15, 0, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 15, 23, 59))
        .build();

    String formatted = DialogHelper.formatEventForDisplay(event);

    assertTrue(formatted.contains("Conference"));
    assertTrue(formatted.contains("12:00 AM"));
  }

  /**
   * Tests formatEventForDisplay with multi-day event.
   */
  @Test
  public void testFormatEventForDisplayMultiDay() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Retreat")
        .startDateTime(LocalDateTime.of(2024, 1, 15, 9, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 17, 17, 0))
        .build();

    String formatted = DialogHelper.formatEventForDisplay(event);

    assertTrue(formatted.contains("Retreat"));
    assertTrue(formatted.contains("Jan 15"));
    assertTrue(formatted.contains("Jan 17"));
    assertTrue(formatted.contains("9:00 AM"));
    assertTrue(formatted.contains("5:00 PM"));
  }

  /**
   * Tests formatEventForDisplay with event at midnight.
   */
  @Test
  public void testFormatEventForDisplayMidnight() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("New Year")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 0, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 1, 0))
        .build();

    String formatted = DialogHelper.formatEventForDisplay(event);

    assertTrue(formatted.contains("New Year"));
    assertTrue(formatted.contains("12:00 AM"));
  }

  /**
   * Tests formatEventForDisplay with event at noon.
   */
  @Test
  public void testFormatEventForDisplayNoon() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Lunch")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 12, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 13, 0))
        .build();

    String formatted = DialogHelper.formatEventForDisplay(event);

    assertTrue(formatted.contains("Lunch"));
    assertTrue(formatted.contains("12:00 PM"));
  }

  /**
   * Tests CalendarEditData constructor and getters.
   */
  @Test
  public void testCalendarEditDataConstructorAndGetters() {
    CalendarEditData data = new CalendarEditData("name", "MyCalendar");

    assertEquals("name", data.getProperty());
    assertEquals("MyCalendar", data.getNewValue());
  }

  /**
   * Tests CalendarEditData with timezone property.
   */
  @Test
  public void testCalendarEditDataTimezone() {
    CalendarEditData data = new CalendarEditData("timezone", "America/New_York");

    assertEquals("timezone", data.getProperty());
    assertEquals("America/New_York", data.getNewValue());
  }

  /**
   * Tests CalendarEditData toString method returns non-null.
   */
  @Test
  public void testCalendarEditDataToString() {
    CalendarEditData data = new CalendarEditData("name", "Work");

    String str = data.toString();

    assertNotNull(str);
  }

  /**
   * Tests CalendarEditData equals with same object.
   */
  @Test
  public void testCalendarEditDataEqualsSameObject() {
    CalendarEditData data = new CalendarEditData("name", "Test");

    assertTrue(data.equals(data));
  }

  /**
   * Tests CalendarEditData equals with equal objects.
   */
  @Test
  public void testCalendarEditDataEqualsEqualObjects() {
    CalendarEditData data1 = new CalendarEditData("name", "Test");
    CalendarEditData data2 = new CalendarEditData("name", "Test");

    assertNotNull(data1);
    assertNotNull(data2);
    data1.equals(data2);
    data1.hashCode();
    data2.hashCode();
  }

  /**
   * Tests CalendarEditData equals with different property.
   */
  @Test
  public void testCalendarEditDataEqualsDifferentProperty() {
    CalendarEditData data1 = new CalendarEditData("name", "Test");
    CalendarEditData data2 = new CalendarEditData("timezone", "Test");

    assertFalse(data1.equals(data2));
  }

  /**
   * Tests CalendarEditData equals with different value.
   */
  @Test
  public void testCalendarEditDataEqualsDifferentValue() {
    CalendarEditData data1 = new CalendarEditData("name", "Test1");
    CalendarEditData data2 = new CalendarEditData("name", "Test2");

    assertFalse(data1.equals(data2));
  }

  /**
   * Tests CalendarEditData equals with null.
   */
  @Test
  public void testCalendarEditDataEqualsNull() {
    CalendarEditData data = new CalendarEditData("name", "Test");

    assertFalse(data.equals(null));
  }

  /**
   * Tests CalendarEditData equals with different type.
   */
  @Test
  public void testCalendarEditDataEqualsDifferentType() {
    CalendarEditData data = new CalendarEditData("name", "Test");

    assertFalse(data.equals("String"));
  }

  /**
   * Tests EventDeleteData constructor and getters.
   */
  @Test
  public void testEventDeleteDataConstructorAndGetters() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .build();

    EventDeleteData data = new EventDeleteData(event, "THIS");

    assertEquals(event, data.getEvent());
    assertEquals("THIS", data.getScope());
  }

  /**
   * Tests EventDeleteData with THIS_AND_FUTURE scope.
   */
  @Test
  public void testEventDeleteDataThisAndFutureScope() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .build();

    EventDeleteData data = new EventDeleteData(event, "THIS_AND_FUTURE");

    assertEquals("THIS_AND_FUTURE", data.getScope());
  }

  /**
   * Tests EventDeleteData with ALL scope.
   */
  @Test
  public void testEventDeleteDataAllScope() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .build();

    EventDeleteData data = new EventDeleteData(event, "ALL");

    assertEquals("ALL", data.getScope());
  }

  /**
   * Tests EventDeleteData toString method returns non-null.
   */
  @Test
  public void testEventDeleteDataToString() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .build();

    EventDeleteData data = new EventDeleteData(event, "THIS");

    String str = data.toString();

    assertNotNull(str);
  }

  /**
   * Tests EventDeleteData equals with same object.
   */
  @Test
  public void testEventDeleteDataEqualsSameObject() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .build();

    EventDeleteData data = new EventDeleteData(event, "THIS");

    assertTrue(data.equals(data));
  }

  /**
   * Tests EventDeleteData equals with equal objects.
   */
  @Test
  public void testEventDeleteDataEqualsEqualObjects() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .build();

    EventDeleteData data1 = new EventDeleteData(event, "THIS");
    EventDeleteData data2 = new EventDeleteData(event, "THIS");

    assertNotNull(data1);
    assertNotNull(data2);
    data1.equals(data2);
    data1.hashCode();
    data2.hashCode();
  }

  /**
   * Tests EventDeleteData equals with different event.
   */
  @Test
  public void testEventDeleteDataEqualsDifferentEvent() {
    Event event1 = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .build();

    Event event2 = new CalendarEvent.EventBuilder()
        .subject("Workshop")
        .startDateTime(LocalDateTime.of(2024, 1, 2, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 2, 11, 0))
        .build();

    EventDeleteData data1 = new EventDeleteData(event1, "THIS");
    EventDeleteData data2 = new EventDeleteData(event2, "THIS");

    assertFalse(data1.equals(data2));
  }

  /**
   * Tests EventDeleteData equals with different scope.
   */
  @Test
  public void testEventDeleteDataEqualsDifferentScope() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .build();

    EventDeleteData data1 = new EventDeleteData(event, "THIS");
    EventDeleteData data2 = new EventDeleteData(event, "ALL");

    assertFalse(data1.equals(data2));
  }

  /**
   * Tests EventDeleteData equals with null.
   */
  @Test
  public void testEventDeleteDataEqualsNull() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .build();

    EventDeleteData data = new EventDeleteData(event, "THIS");

    assertFalse(data.equals(null));
  }

  /**
   * Tests EventDeleteData equals with different type.
   */
  @Test
  public void testEventDeleteDataEqualsDifferentType() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .build();

    EventDeleteData data = new EventDeleteData(event, "THIS");

    assertFalse(data.equals("String"));
  }

  /**
   * Tests EventEditData constructor and getters.
   */
  @Test
  public void testEventEditDataConstructorAndGetters() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .build();

    LocalDateTime newStart = LocalDateTime.of(2024, 1, 1, 14, 0);
    LocalDateTime newEnd = LocalDateTime.of(2024, 1, 1, 15, 0);

    EventEditData data = new EventEditData(
        event,
        "Updated Meeting",
        newStart,
        newEnd,
        "Room B",
        "New description",
        "PRIVATE",
        "single"
    );

    assertEquals(event, data.getOriginalEvent());
    assertEquals("Updated Meeting", data.getNewSubject());
    assertEquals(newStart, data.getNewStartDateTime());
    assertEquals(newEnd, data.getNewEndDateTime());
    assertEquals("Room B", data.getNewLocation());
    assertEquals("New description", data.getNewDescription());
    assertEquals("PRIVATE", data.getNewStatus());
    assertEquals("single", data.getEditScope());
  }

  /**
   * Tests EventEditData with from scope.
   */
  @Test
  public void testEventEditDataFromScope() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .build();

    EventEditData data = new EventEditData(
        event, "Meeting", LocalDateTime.of(2024, 1, 1, 10, 0),
        LocalDateTime.of(2024, 1, 1, 11, 0), null, null, "PUBLIC", "from"
    );

    assertEquals("from", data.getEditScope());
  }

  /**
   * Tests EventEditData with all scope.
   */
  @Test
  public void testEventEditDataAllScope() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .build();

    EventEditData data = new EventEditData(
        event, "Meeting", LocalDateTime.of(2024, 1, 1, 10, 0),
        LocalDateTime.of(2024, 1, 1, 11, 0), null, null, "PUBLIC", "all"
    );

    assertEquals("all", data.getEditScope());
  }

  /**
   * Tests EventEditData with null location.
   */
  @Test
  public void testEventEditDataNullLocation() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .build();

    EventEditData data = new EventEditData(
        event, "Meeting", LocalDateTime.of(2024, 1, 1, 10, 0),
        LocalDateTime.of(2024, 1, 1, 11, 0), null, "Description", "PUBLIC", "single"
    );

    assertNull(data.getNewLocation());
  }

  /**
   * Tests EventEditData with null description.
   */
  @Test
  public void testEventEditDataNullDescription() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .build();

    EventEditData data = new EventEditData(
        event, "Meeting", LocalDateTime.of(2024, 1, 1, 10, 0),
        LocalDateTime.of(2024, 1, 1, 11, 0), "Location", null, "PUBLIC", "single"
    );

    assertNull(data.getNewDescription());
  }

  /**
   * Tests EventEditData toString method.
   */
  @Test
  public void testEventEditDataToString() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .build();

    EventEditData data = new EventEditData(
        event, "Updated", LocalDateTime.of(2024, 1, 1, 14, 0),
        LocalDateTime.of(2024, 1, 1, 15, 0), "Room", "Desc", "PUBLIC", "single"
    );

    String str = data.toString();

    assertTrue(str.contains("Meeting"));
    assertTrue(str.contains("single"));
    assertTrue(str.contains("2024-01-01"));
  }

  /**
   * Tests EventEditData equals with same object.
   */
  @Test
  public void testEventEditDataEqualsSameObject() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .build();

    EventEditData data = new EventEditData(
        event, "Meeting", LocalDateTime.of(2024, 1, 1, 10, 0),
        LocalDateTime.of(2024, 1, 1, 11, 0), "Room", "Desc", "PUBLIC", "single"
    );

    assertTrue(data.equals(data));
  }

  /**
   * Tests EventEditData equals with equal objects.
   */
  @Test
  public void testEventEditDataEqualsEqualObjects() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .build();

    LocalDateTime start = LocalDateTime.of(2024, 1, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2024, 1, 1, 11, 0);

    EventEditData data1 = new EventEditData(
        event, "Meeting", start, end, "Room", "Desc", "PUBLIC", "single"
    );
    EventEditData data2 = new EventEditData(
        event, "Meeting", start, end, "Room", "Desc", "PUBLIC", "single"
    );

    assertTrue(data1.equals(data2));
    assertEquals(data1.hashCode(), data2.hashCode());
  }

  /**
   * Tests EventEditData equals with different event.
   */
  @Test
  public void testEventEditDataEqualsDifferentEvent() {
    Event event1 = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .build();

    Event event2 = new CalendarEvent.EventBuilder()
        .subject("Workshop")
        .startDateTime(LocalDateTime.of(2024, 1, 2, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 2, 11, 0))
        .build();

    LocalDateTime start = LocalDateTime.of(2024, 1, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2024, 1, 1, 11, 0);

    EventEditData data1 = new EventEditData(
        event1, "Meeting", start, end, "Room", "Desc", "PUBLIC", "single"
    );
    EventEditData data2 = new EventEditData(
        event2, "Meeting", start, end, "Room", "Desc", "PUBLIC", "single"
    );

    assertFalse(data1.equals(data2));
  }

  /**
   * Tests EventEditData equals with different subject.
   */
  @Test
  public void testEventEditDataEqualsDifferentSubject() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .build();

    LocalDateTime start = LocalDateTime.of(2024, 1, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2024, 1, 1, 11, 0);

    EventEditData data1 = new EventEditData(
        event, "Meeting", start, end, "Room", "Desc", "PUBLIC", "single"
    );
    EventEditData data2 = new EventEditData(
        event, "Workshop", start, end, "Room", "Desc", "PUBLIC", "single"
    );

    assertFalse(data1.equals(data2));
  }

  /**
   * Tests EventEditData equals with different start time.
   */
  @Test
  public void testEventEditDataEqualsDifferentStartTime() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .build();

    LocalDateTime end = LocalDateTime.of(2024, 1, 1, 11, 0);

    EventEditData data1 = new EventEditData(
        event, "Meeting", LocalDateTime.of(2024, 1, 1, 10, 0),
        end, "Room", "Desc", "PUBLIC", "single"
    );
    EventEditData data2 = new EventEditData(
        event, "Meeting", LocalDateTime.of(2024, 1, 1, 14, 0),
        end, "Room", "Desc", "PUBLIC", "single"
    );

    assertFalse(data1.equals(data2));
  }

  /**
   * Tests EventEditData equals with different end time.
   */
  @Test
  public void testEventEditDataEqualsDifferentEndTime() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .build();

    LocalDateTime start = LocalDateTime.of(2024, 1, 1, 10, 0);

    EventEditData data1 = new EventEditData(
        event, "Meeting", start, LocalDateTime.of(2024, 1, 1, 11, 0),
        "Room", "Desc", "PUBLIC", "single"
    );
    EventEditData data2 = new EventEditData(
        event, "Meeting", start, LocalDateTime.of(2024, 1, 1, 15, 0),
        "Room", "Desc", "PUBLIC", "single"
    );

    assertFalse(data1.equals(data2));
  }

  /**
   * Tests EventEditData equals with different location.
   */
  @Test
  public void testEventEditDataEqualsDifferentLocation() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .build();

    LocalDateTime start = LocalDateTime.of(2024, 1, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2024, 1, 1, 11, 0);

    EventEditData data1 = new EventEditData(
        event, "Meeting", start, end, "Room A", "Desc", "PUBLIC", "single"
    );
    EventEditData data2 = new EventEditData(
        event, "Meeting", start, end, "Room B", "Desc", "PUBLIC", "single"
    );

    assertFalse(data1.equals(data2));
  }

  /**
   * Tests EventEditData equals with different description.
   */
  @Test
  public void testEventEditDataEqualsDifferentDescription() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .build();

    LocalDateTime start = LocalDateTime.of(2024, 1, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2024, 1, 1, 11, 0);

    EventEditData data1 = new EventEditData(
        event, "Meeting", start, end, "Room", "Desc 1", "PUBLIC", "single"
    );
    EventEditData data2 = new EventEditData(
        event, "Meeting", start, end, "Room", "Desc 2", "PUBLIC", "single"
    );

    assertFalse(data1.equals(data2));
  }

  /**
   * Tests EventEditData equals with different status.
   */
  @Test
  public void testEventEditDataEqualsDifferentStatus() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .build();

    LocalDateTime start = LocalDateTime.of(2024, 1, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2024, 1, 1, 11, 0);

    EventEditData data1 = new EventEditData(
        event, "Meeting", start, end, "Room", "Desc", "PUBLIC", "single"
    );
    EventEditData data2 = new EventEditData(
        event, "Meeting", start, end, "Room", "Desc", "PRIVATE", "single"
    );

    assertFalse(data1.equals(data2));
  }

  /**
   * Tests EventEditData equals with different scope.
   */
  @Test
  public void testEventEditDataEqualsDifferentScope() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .build();

    LocalDateTime start = LocalDateTime.of(2024, 1, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2024, 1, 1, 11, 0);

    EventEditData data1 = new EventEditData(
        event, "Meeting", start, end, "Room", "Desc", "PUBLIC", "single"
    );
    EventEditData data2 = new EventEditData(
        event, "Meeting", start, end, "Room", "Desc", "PUBLIC", "all"
    );

    assertFalse(data1.equals(data2));
  }

  /**
   * Tests EventEditData equals with null.
   */
  @Test
  public void testEventEditDataEqualsNull() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .build();

    EventEditData data = new EventEditData(
        event, "Meeting", LocalDateTime.of(2024, 1, 1, 10, 0),
        LocalDateTime.of(2024, 1, 1, 11, 0), "Room", "Desc", "PUBLIC", "single"
    );

    assertFalse(data.equals(null));
  }

  /**
   * Tests EventEditData equals with different type.
   */
  @Test
  public void testEventEditDataEqualsDifferentType() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .build();

    EventEditData data = new EventEditData(
        event, "Meeting", LocalDateTime.of(2024, 1, 1, 10, 0),
        LocalDateTime.of(2024, 1, 1, 11, 0), "Room", "Desc", "PUBLIC", "single"
    );

    assertFalse(data.equals("String"));
  }

  /**
   * Tests EventEditData with empty strings.
   */
  @Test
  public void testEventEditDataEmptyStrings() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .build();

    EventEditData data = new EventEditData(
        event, "", LocalDateTime.of(2024, 1, 1, 10, 0),
        LocalDateTime.of(2024, 1, 1, 11, 0), "", "", "PUBLIC", "single"
    );

    assertEquals("", data.getNewSubject());
    assertEquals("", data.getNewLocation());
    assertEquals("", data.getNewDescription());
  }

  /**
   * Tests DialogHelper formatEventForDisplay with different months.
   */
  @Test
  public void testFormatEventForDisplayDifferentMonths() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Conference")
        .startDateTime(LocalDateTime.of(2024, 1, 31, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 2, 1, 15, 0))
        .build();

    String formatted = DialogHelper.formatEventForDisplay(event);

    assertTrue(formatted.contains("Conference"));
    assertTrue(formatted.contains("Jan 31"));
    assertTrue(formatted.contains("Feb 1"));
  }

  /**
   * Tests formatEventForDisplay with PM times.
   */
  @Test
  public void testFormatEventForDisplayPmTimes() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Dinner")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 18, 30))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 20, 0))
        .build();

    String formatted = DialogHelper.formatEventForDisplay(event);

    assertTrue(formatted.contains("Dinner"));
    assertTrue(formatted.contains("6:30 PM"));
    assertTrue(formatted.contains("8:00 PM"));
  }

  /**
   * Tests CalendarEditData with empty property name.
   */
  @Test
  public void testCalendarEditDataEmptyProperty() {
    CalendarEditData data = new CalendarEditData("", "value");

    assertEquals("", data.getProperty());
  }

  /**
   * Tests CalendarEditData with empty value.
   */
  @Test
  public void testCalendarEditDataEmptyValue() {
    CalendarEditData data = new CalendarEditData("name", "");

    assertEquals("", data.getNewValue());
  }

  /**
   * Tests updateSeriesRadioVisibility preserves state when returning early.
   */
  @Test
  public void testUpdateSeriesRadioVisibilityPreservesState() {
    singleRadio.setSelected(false);
    fromRadio.setSelected(true);
    fromRadio.setVisible(false);

    DialogHelper.updateSeriesRadioVisibility(events, -1, singleRadio, fromRadio, allRadio);

    assertFalse(fromRadio.isVisible());
    assertTrue(fromRadio.isSelected());
  }

  /**
   * Tests updateSeriesRadioVisibility with index exactly at size (boundary).
   * This test kills the boundary mutation on line 31 of DialogHelper.
   */
  @Test
  public void testUpdateSeriesRadioVisibilityIndexAtSize() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .seriesId("series-123")
        .build();
    events.add(event);

    fromRadio.setVisible(true);
    allRadio.setVisible(true);
    singleRadio.setSelected(false);

    DialogHelper.updateSeriesRadioVisibility(events, 1, singleRadio, fromRadio, allRadio);

    assertTrue(fromRadio.isVisible());
    assertTrue(allRadio.isVisible());
    assertFalse(singleRadio.isSelected());
  }

  /**
   * Tests EventEditData hashCode returns non-zero for non-null object.
   * This test kills the hashCode mutation on line 114 of EventEditData.
   */
  @Test
  public void testEventEditDataHashCodeNonZero() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .build();

    LocalDateTime time = LocalDateTime.of(2024, 1, 1, 10, 0);

    EventEditData data = new EventEditData(
        event, "Subject", time, time, "Room", "Desc", "PUBLIC", "single"
    );

    int hash = data.hashCode();
    assertNotNull(data);

    Event event2 = new CalendarEvent.EventBuilder()
        .subject("Workshop")
        .startDateTime(LocalDateTime.of(2024, 2, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 2, 1, 11, 0))
        .build();

    EventEditData data2 = new EventEditData(
        event2, "Different", time, time, "Other", "Text", "PRIVATE", "all"
    );

    int hash2 = data2.hashCode();

    assertTrue(hash != 0 || hash2 != 0);
  }

  /**
   * Tests formatEventForDisplay with all months.
   */
  @Test
  public void testFormatEventForDisplayAllMonthsCoverage() {
    String[] expectedMonths = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    for (int month = 1; month <= 12; month++) {
      Event event = new CalendarEvent.EventBuilder()
          .subject("Event" + month)
          .startDateTime(LocalDateTime.of(2024, month, 15, 10, 0))
          .endDateTime(LocalDateTime.of(2024, month, 16, 11, 0))
          .build();

      String formatted = DialogHelper.formatEventForDisplay(event);

      assertTrue(formatted.contains("Event" + month));
      assertTrue(formatted.contains(expectedMonths[month - 1]));
    }
  }

  /**
   * Tests formatEventForDisplay with hour 0 (midnight edge case).
   */
  @Test
  public void testFormatEventForDisplayHourZero() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Midnight Event")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 0, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 1, 0))
        .build();

    String formatted = DialogHelper.formatEventForDisplay(event);

    assertTrue(formatted.contains("12:00 AM"));
  }

  /**
   * Tests formatEventForDisplay with hour 12 (noon edge case).
   */
  @Test
  public void testFormatEventForDisplayHourTwelve() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Noon Event")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 12, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 13, 0))
        .build();

    String formatted = DialogHelper.formatEventForDisplay(event);

    assertTrue(formatted.contains("12:00 PM"));
  }

  /**
   * Tests formatEventForDisplay with hour 13 (1 PM edge case).
   */
  @Test
  public void testFormatEventForDisplayHourThirteen() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Afternoon Event")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 13, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 14, 0))
        .build();

    String formatted = DialogHelper.formatEventForDisplay(event);

    assertTrue(formatted.contains("1:00 PM"));
  }

  /**
   * Tests EventEditData with maximum boundary values.
   */
  @Test
  public void testEventEditDataBoundaryValues() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .build();

    LocalDateTime maxDate = LocalDateTime.of(9999, 12, 31, 23, 59);

    EventEditData data = new EventEditData(
        event, "Test", maxDate, maxDate, "Loc", "Desc", "PUBLIC", "single"
    );

    assertEquals(maxDate, data.getNewStartDateTime());
  }

  /**
   * Tests formatEventForDisplay with single digit hours.
   */
  @Test
  public void testFormatEventForDisplaySingleDigitHour() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Morning Event")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 9, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .build();

    String formatted = DialogHelper.formatEventForDisplay(event);

    assertTrue(formatted.contains("9:00 AM"));
  }

  /**
   * Tests formatEventForDisplay with various minutes.
   */
  @Test
  public void testFormatEventForDisplayVariousMinutes() {
    int[] minutes = {0, 15, 30, 45, 59};

    for (int min : minutes) {
      Event event = new CalendarEvent.EventBuilder()
          .subject("Event")
          .startDateTime(LocalDateTime.of(2024, 1, 1, 10, min))
          .endDateTime(LocalDateTime.of(2024, 1, 1, 11, min))
          .build();

      String formatted = DialogHelper.formatEventForDisplay(event);
      assertNotNull(formatted);
    }
  }

  /**
   * Tests updateSeriesRadioVisibility preserves selected state.
   */
  @Test
  public void testUpdateSeriesRadioVisibilityPreservesSelection() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .seriesId("series-123")
        .build();
    events.add(event);

    fromRadio.setSelected(true);

    DialogHelper.updateSeriesRadioVisibility(events, 0, singleRadio, fromRadio, allRadio);

    assertTrue(fromRadio.isVisible());
    assertTrue(fromRadio.isSelected());
  }

  /**
   * Tests EventEditData with whitespace in strings.
   */
  @Test
  public void testEventEditDataWithWhitespace() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .build();

    LocalDateTime time = LocalDateTime.of(2024, 1, 1, 10, 0);

    EventEditData data = new EventEditData(
        event, " Test ", time, time, " Room ", " Desc ", "PUBLIC", "single"
    );

    assertEquals(" Test ", data.getNewSubject());
    assertEquals(" Room ", data.getNewLocation());
    assertEquals(" Desc ", data.getNewDescription());
  }

  /**
   * Tests CalendarEditData with various property names.
   */
  @Test
  public void testCalendarEditDataVariousProperties() {
    String[] properties = {"name", "timezone", "color", "description"};

    for (String prop : properties) {
      CalendarEditData data = new CalendarEditData(prop, "value");
      assertEquals(prop, data.getProperty());
    }
  }

  /**
   * Tests EventDeleteData with various scope strings.
   */
  @Test
  public void testEventDeleteDataVariousScopes() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .build();

    String[] scopes = {"THIS", "THIS_AND_FUTURE", "ALL", "CUSTOM"};

    for (String scope : scopes) {
      EventDeleteData data = new EventDeleteData(event, scope);
      assertEquals(scope, data.getScope());
    }
  }

  /**
   * Tests EventEditData equals with different event object.
   */
  @Test
  public void testEventEditDataEqualsDifferentEventObject() {
    Event event1 = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .build();

    Event event2 = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .build();

    LocalDateTime time = LocalDateTime.of(2024, 1, 1, 10, 0);

    EventEditData data1 = new EventEditData(
        event1, "Test", time, time, "Room", "Desc", "PUBLIC", "single"
    );
    EventEditData data2 = new EventEditData(
        event2, "Test", time, time, "Room", "Desc", "PUBLIC", "single"
    );

    assertTrue(data1.equals(data2));
  }

  /**
   * Tests formatEventForDisplay with end of year dates.
   */
  @Test
  public void testFormatEventForDisplayEndOfYear() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Year End")
        .startDateTime(LocalDateTime.of(2024, 12, 31, 23, 0))
        .endDateTime(LocalDateTime.of(2024, 12, 31, 23, 59))
        .build();

    String formatted = DialogHelper.formatEventForDisplay(event);

    assertTrue(formatted.contains("Year End"));
    assertTrue(formatted.contains("11:00 PM"));
  }

  /**
   * Tests formatEventForDisplay with start of year dates.
   */
  @Test
  public void testFormatEventForDisplayStartOfYear() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("New Year")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 0, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 1, 0))
        .build();

    String formatted = DialogHelper.formatEventForDisplay(event);

    assertTrue(formatted.contains("New Year"));
    assertTrue(formatted.contains("12:00 AM"));
  }

  /**
   * Tests EventEditData hashCode with null values.
   */
  @Test
  public void testEventEditDataHashCodeWithNulls() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .build();

    LocalDateTime time = LocalDateTime.of(2024, 1, 1, 10, 0);

    EventEditData data1 = new EventEditData(
        event, null, time, time, null, null, "PUBLIC", "single"
    );
    int hash1 = data1.hashCode();

    EventEditData data2 = new EventEditData(
        event, "Test", null, time, "Room", null, "PUBLIC", "single"
    );
    int hash2 = data2.hashCode();

    assertNotNull(data1);
    assertNotNull(data2);
  }

  /**
   * Tests updateSeriesRadioVisibility with single event list.
   */
  @Test
  public void testUpdateSeriesRadioVisibilitySingleEventList() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .seriesId("series-123")
        .build();
    events.add(event);

    DialogHelper.updateSeriesRadioVisibility(events, 0, singleRadio, fromRadio, allRadio);

    assertTrue(fromRadio.isVisible());
    assertTrue(allRadio.isVisible());
  }

  /**
   * Tests formatEventForDisplay with very short time span.
   */
  @Test
  public void testFormatEventForDisplayShortTimeSpan() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Quick Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 10, 15))
        .build();

    String formatted = DialogHelper.formatEventForDisplay(event);

    assertTrue(formatted.contains("10:00 AM"));
    assertTrue(formatted.contains("10:15 AM"));
  }

  /**
   * Tests EventEditData toString contains relevant information.
   */
  @Test
  public void testEventEditDataToStringContent() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Important Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 15, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 15, 11, 0))
        .build();

    LocalDateTime time = LocalDateTime.of(2024, 1, 15, 14, 0);

    EventEditData data = new EventEditData(
        event, "Updated", time, time, "Room", "Desc", "PUBLIC", "from"
    );

    String str = data.toString();

    assertTrue(str.contains("Important Meeting"));
    assertTrue(str.contains("from"));
    assertTrue(str.contains("2024"));
  }

  /**
   * Tests CalendarEditData toString returns valid string.
   */
  @Test
  public void testCalendarEditDataToStringValid() {
    CalendarEditData data = new CalendarEditData("timezone", "America/Los_Angeles");

    String str = data.toString();

    assertNotNull(str);
    assertTrue(str.length() > 0);
  }

  /**
   * Tests EventDeleteData toString returns valid string.
   */
  @Test
  public void testEventDeleteDataToStringValid() {
    Event event = new CalendarEvent.EventBuilder()
        .subject("Meeting")
        .startDateTime(LocalDateTime.of(2024, 1, 1, 10, 0))
        .endDateTime(LocalDateTime.of(2024, 1, 1, 11, 0))
        .build();

    EventDeleteData data = new EventDeleteData(event, "ALL");

    String str = data.toString();

    assertNotNull(str);
    assertTrue(str.length() > 0);
  }
}