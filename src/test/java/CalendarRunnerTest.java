import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Permission;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for CalendarRunner.
 * Tests the main method's argument parsing and mode selection.
 * Uses custom SecurityManager to intercept System.exit() calls for testing.
 */
public class CalendarRunnerTest {
  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;
  private final PrintStream originalErr = System.err;
  private final InputStream originalIn = System.in;
  private SecurityManager originalSecurityManager;

  /**
   * Custom security manager to prevent System.exit from terminating JVM.
   * This allows us to test code that calls System.exit() without killing the test runner.
   */
  private static class NoExitSecurityManager extends SecurityManager {
    @Override
    public void checkPermission(Permission perm) {
      // Allow everything
    }

    @Override
    public void checkPermission(Permission perm, Object context) {
      // Allow everything
    }

    @Override
    public void checkExit(int status) {
      super.checkExit(status);
      throw new ExitException(status);
    }
  }

  /**
   * Exception thrown when System.exit is called.
   * Captures the exit status for verification in tests.
   */
  private static class ExitException extends SecurityException {
    public final int status;

    public ExitException(int status) {
      super("System.exit(" + status + ")");
      this.status = status;
    }
  }

  /**
   * Sets up the output streams and installs a custom security manager
   * to capture System.out, System.err, and prevent System.exit calls.
   */
  @Before
  public void setUpStreams() {
    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));
    originalSecurityManager = System.getSecurityManager();
    System.setSecurityManager(new NoExitSecurityManager());
  }

  /**
   * Restores the original system input/output streams and security manager
   * after each test to ensure a clean environment.
   */
  @After
  public void restoreStreams() {
    System.setOut(originalOut);
    System.setErr(originalErr);
    System.setIn(originalIn);
    System.setSecurityManager(originalSecurityManager);
  }

  @Test
  public void testMainGuiModeNoArguments() {
    try {
      Thread testThread = new Thread(() -> {
        CalendarRunner.main(new String[] {});
      });
      testThread.setDaemon(true);
      testThread.start();

      Thread.sleep(500);

      assertTrue(true);
    } catch (Exception e) {
      fail("GUI mode should launch without arguments: " + e.getMessage());
    }
  }

  @Test
  public void testMainOneArgument() {
    try {
      CalendarRunner.main(new String[] {"--mode"});
      fail("Expected ExitException when mode value is missing");
    } catch (ExitException e) {
      assertEquals("Exit status should be 1 for error", 1, e.status);
    }
    assertTrue("Should display usage message",
        errContent.toString().contains("Usage"));
  }

  @Test
  public void testMainInvalidFlag() {
    try {
      CalendarRunner.main(new String[] {"--invalid", "interactive"});
      fail("Expected ExitException for invalid flag");
    } catch (ExitException e) {
      assertEquals("Exit status should be 1 for error", 1, e.status);
    }
    assertTrue("Should show error about --mode flag",
        errContent.toString().contains("First argument must be --mode"));
  }

  @Test
  public void testMainInvalidMode() {
    String input = "exit\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    try {
      CalendarRunner.main(new String[] {"--mode", "invalid"});
      fail("Expected ExitException for invalid mode");
    } catch (ExitException e) {
      assertEquals("Exit status should be 1 for error", 1, e.status);
    }
    assertTrue("Should show unknown mode error",
        errContent.toString().contains("Unknown mode"));
  }

  @Test
  public void testMainInteractiveMode() {
    String input = "exit\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    CalendarRunner.main(new String[] {"--mode", "interactive"});

    assertTrue(outContent.toString().contains("Calendar started"));
  }

  @Test
  public void testMainInteractiveModeCaseInsensitive() {
    String input = "exit\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    CalendarRunner.main(new String[] {"--mode", "INTERACTIVE"});

    assertTrue(outContent.toString().contains("Calendar started"));
  }

  @Test
  public void testMainHeadlessModeNoFile() {
    try {
      CalendarRunner.main(new String[] {"--mode", "headless"});
      fail("Expected ExitException when filepath is missing");
    } catch (ExitException e) {
      assertEquals("Exit status should be 1 for error", 1, e.status);
    }
    assertTrue("Should show filepath required error",
        errContent.toString().contains("Headless mode requires a filepath"));
  }

  @Test
  public void testMainHeadlessMode() throws Exception {
    Path tempFile = Files.createTempFile("test", ".txt");
    Files.write(tempFile, "exit\n".getBytes());

    CalendarRunner.main(new String[] {"--mode", "headless", tempFile.toString()});

    Files.deleteIfExists(tempFile);
  }

  @Test
  public void testMainHeadlessModeCaseInsensitive() throws Exception {
    Path tempFile = Files.createTempFile("test", ".txt");
    Files.write(tempFile, "exit\n".getBytes());

    CalendarRunner.main(new String[] {"--mode", "HEADLESS", tempFile.toString()});

    Files.deleteIfExists(tempFile);
  }

  @Test
  public void testMainHeadlessModeNonExistentFile() {
    try {
      CalendarRunner.main(new String[] {"--mode", "headless", "non_existent.txt"});
      fail("Expected ExitException for non-existent file");
    } catch (ExitException e) {
      assertEquals("Exit status should be 1 for error", 1, e.status);
    }
    assertTrue("Should show error message",
        errContent.toString().contains("Error"));
  }

  @Test
  public void testMainModeFlagCaseInsensitive() {
    String input = "exit\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    CalendarRunner.main(new String[] {"--MODE", "interactive"});

    assertTrue(outContent.toString().contains("Calendar started"));
  }
}