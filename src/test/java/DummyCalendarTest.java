import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import calendar.DummyCalendar;
import org.junit.Test;

/**
 * Dummy Calendar Test Class.
 */
public class DummyCalendarTest {

  @Test
  public void testGetName() {
    DummyCalendar calendar = new DummyCalendar();
    String name = calendar.getName();

    assertNotNull(name);
    assertEquals("DummyCalendar", name);
  }

  @Test
  public void testConstructor() {
    DummyCalendar calendar = new DummyCalendar();
    assertNotNull(calendar);
  }
}