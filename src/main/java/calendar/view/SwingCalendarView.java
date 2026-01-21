package calendar.view;

import calendar.controller.Features;
import calendar.model.CalendarAnalyticsSummary;
import calendar.model.Event;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;


/**
 * Swing-based implementation of the calendar GUI.
 *
 * <p>The view is responsible only for rendering and user interaction:
 * it forwards user actions (e.g., "Show Dashboard") to the controller via
 * the {@link Features} listener and renders results such as the
 * {@link CalendarAnalyticsSummary} in read-only dialogs.</p>
 */
public class SwingCalendarView extends JFrame implements CalendarGuiView {

  private static final Color[] CALENDAR_COLORS = {
      new Color(66, 133, 244),
      new Color(234, 67, 53),
      new Color(52, 168, 83),
      new Color(251, 188, 5),
      new Color(156, 39, 176),
      new Color(255, 109, 0),
      new Color(0, 172, 193),
      new Color(121, 134, 203)
  };

  private Features featuresListener;
  private final Map<String, Color> calendarColors;
  private int colorIndex = 0;

  private YearMonth currentMonth;
  private LocalDate selectedDate;
  private String currentCalendarName;

  private JLabel monthYearLabel;
  private JLabel currentCalendarLabel;
  private JPanel calendarGridPanel;
  private JPanel eventListPanel;
  private JLabel eventDateLabel;

  /**
   * Creates a new SwingCalendarView.
   */
  public SwingCalendarView() {
    super("Calendar Application");

    this.calendarColors = new HashMap<>();
    this.currentMonth = YearMonth.now();
    this.selectedDate = LocalDate.now();
    this.currentCalendarName = "Default";

    initializeUi();
  }

  /**
   * Gets all available timezones sorted alphabetically.
   *
   * @return array of timezone IDs
   */
  private static String[] getAllTimezones() {
    List<String> timezones = new ArrayList<>(ZoneId.getAvailableZoneIds());
    Collections.sort(timezones);
    return timezones.toArray(new String[0]);
  }

  /**
   * Initializes all UI components and layout.
   */
  private void initializeUi() {
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    setSize(1200, 800);
    setLocationRelativeTo(null);
    setLayout(new BorderLayout(10, 10));

    add(createTopPanel(), BorderLayout.NORTH);

    add(createCalendarPanel(), BorderLayout.CENTER);

    add(createSidePanel(), BorderLayout.EAST);
  }

  /**
   * Creates the top panel with calendar management and month navigation.
   *
   * <p>The panel includes a "Show Dashboard" button that opens a date-range dialog and
   *     ultimately triggers {@link Features#showDashboard(LocalDate, LocalDate)}.</p>
   *
   * @return a configured {@link JPanel} for the top section of the window
   */
  private JPanel createTopPanel() {
    JPanel topPanel = new JPanel(new BorderLayout(10, 10));
    topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

    JPanel calendarMgmtPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

    currentCalendarLabel = new JLabel("Calendar: Default");
    currentCalendarLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
    calendarMgmtPanel.add(currentCalendarLabel);

    JButton newCalBtn = new JButton("New Calendar");
    newCalBtn.addActionListener(e -> {
      if (featuresListener != null) {
        featuresListener.createCalendar();
      }
    });
    calendarMgmtPanel.add(newCalBtn);

    JButton switchCalBtn = new JButton("Switch Calendar");
    switchCalBtn.addActionListener(e -> {
      if (featuresListener != null) {
        featuresListener.switchCalendar();
      }
    });
    calendarMgmtPanel.add(switchCalBtn);

    JButton editCalBtn = new JButton("Edit Calendar");
    editCalBtn.addActionListener(e -> {
      if (featuresListener != null) {
        featuresListener.editCalendar();
      }
    });
    calendarMgmtPanel.add(editCalBtn);

    topPanel.add(calendarMgmtPanel, BorderLayout.WEST);

    JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));

    JButton prevBtn = new JButton("< Previous");
    prevBtn.addActionListener(e -> navigateMonth(-1));
    navPanel.add(prevBtn);

    monthYearLabel = new JLabel();
    monthYearLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
    updateMonthYearLabel();
    navPanel.add(monthYearLabel);

    JButton nextBtn = new JButton("Next >");
    nextBtn.addActionListener(e -> navigateMonth(1));
    navPanel.add(nextBtn);

    JButton todayBtn = new JButton("Today");
    todayBtn.addActionListener(e -> goToToday());
    navPanel.add(todayBtn);

    JButton dashboardBtn = new JButton("Show Dashboard");
    dashboardBtn.addActionListener(e -> showDashboardDialog());
    navPanel.add(dashboardBtn);

    topPanel.add(navPanel, BorderLayout.CENTER);

    return topPanel;
  }

  /**
   * Creates the calendar grid panel showing the month view.
   */
  private JPanel createCalendarPanel() {
    JPanel calendarPanel = new JPanel(new BorderLayout(5, 5));
    calendarPanel.setBorder(new EmptyBorder(0, 10, 10, 10));

    JPanel headerPanel = new JPanel(new GridLayout(1, 7, 2, 2));
    String[] dayNames = {"Sunday", "Monday", "Tuesday", "Wednesday",
        "Thursday", "Friday", "Saturday"};
    for (String day : dayNames) {
      JLabel label = new JLabel(day, SwingConstants.CENTER);
      label.setFont(new Font("SansSerif", Font.BOLD, 14));
      label.setBorder(BorderFactory.createLineBorder(Color.GRAY));
      label.setOpaque(true);
      label.setBackground(new Color(240, 240, 240));
      label.setPreferredSize(new Dimension(100, 30));
      headerPanel.add(label);
    }
    calendarPanel.add(headerPanel, BorderLayout.NORTH);

    calendarGridPanel = new JPanel(new GridLayout(6, 7, 2, 2));
    calendarGridPanel.setPreferredSize(new Dimension(700, 500));
    calendarPanel.add(calendarGridPanel, BorderLayout.CENTER);

    refreshCalendarGrid();

    return calendarPanel;
  }

  /**
   * Creates the side panel with event list and action buttons.
   */
  private JPanel createSidePanel() {
    JPanel sidePanel = new JPanel(new BorderLayout(5, 5));
    sidePanel.setPreferredSize(new Dimension(400, 0));
    sidePanel.setBorder(new EmptyBorder(0, 0, 10, 10));

    eventDateLabel = new JLabel("Events on "
        + selectedDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
    eventDateLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
    eventDateLabel.setBorder(new EmptyBorder(5, 5, 10, 5));
    sidePanel.add(eventDateLabel, BorderLayout.NORTH);

    eventListPanel = new JPanel();
    eventListPanel.setLayout(new BoxLayout(eventListPanel, BoxLayout.Y_AXIS));
    eventListPanel.setBackground(Color.WHITE);
    JScrollPane scrollPane = new JScrollPane(eventListPanel);
    scrollPane.getVerticalScrollBar().setUnitIncrement(16);
    sidePanel.add(scrollPane, BorderLayout.CENTER);

    JPanel buttonPanel = getButtonPanel();
    sidePanel.add(buttonPanel, BorderLayout.SOUTH);
    return sidePanel;
  }

  private JPanel getButtonPanel() {
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

    JButton createBtn = new JButton("Create Event");
    createBtn.addActionListener(e -> {
      if (featuresListener != null) {
        featuresListener.createEvent(selectedDate);
      }
    });
    buttonPanel.add(createBtn);

    JButton editBtn = new JButton("Edit Events");
    editBtn.addActionListener(e -> {
      if (featuresListener != null) {
        featuresListener.editEvents(selectedDate);
      }
    });
    buttonPanel.add(editBtn);

    JButton deleteBtn = new JButton("Delete Event");
    deleteBtn.addActionListener(e -> {
      if (featuresListener != null) {
        featuresListener.deleteEvent(selectedDate);
      }
    });
    buttonPanel.add(deleteBtn);

    return buttonPanel;
  }

  /**
   * Refreshes the calendar grid for the current month.
   */
  private void refreshCalendarGrid() {
    LocalDate firstOfMonth = currentMonth.atDay(1);
    int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7;
    int daysInMonth = currentMonth.lengthOfMonth();

    int totalCells = dayOfWeek + daysInMonth;
    int requiredRows = (int) Math.ceil(totalCells / 7.0);

    calendarGridPanel.removeAll();
    calendarGridPanel.setLayout(new GridLayout(requiredRows, 7, 2, 2));

    for (int i = 0; i < dayOfWeek; i++) {
      calendarGridPanel.add(createEmptyDayPanel());
    }

    for (int day = 1; day <= daysInMonth; day++) {
      LocalDate date = currentMonth.atDay(day);
      calendarGridPanel.add(createDayPanel(date));
    }

    int cellsInLastRow = totalCells % 7;
    if (cellsInLastRow > 0) {
      int remainingCells = 7 - cellsInLastRow;
      for (int i = 0; i < remainingCells; i++) {
        calendarGridPanel.add(createEmptyDayPanel());
      }
    }

    updateMonthYearLabel();
    calendarGridPanel.revalidate();
    calendarGridPanel.repaint();

    if (featuresListener != null) {
      featuresListener.monthChanged(currentMonth.getYear(), currentMonth.getMonthValue());
    }
  }

  /**
   * Creates an empty day panel for spacing.
   */
  private JPanel createEmptyDayPanel() {
    JPanel panel = new JPanel();
    panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
    panel.setBackground(Color.WHITE);
    return panel;
  }

  /**
   * Prompts the user for a date interval and triggers the dashboard computation.
   *
   * <p>This method:
   * <ul>
   *   <li>Asks the user for a start date and an end date in {@code yyyy-MM-dd} format,
   *       pre-populating the input with the currently selected date.</li>
   *   <li>Validates that both inputs are non-empty and parseable as {@link LocalDate}.</li>
   *   <li>If validation succeeds, forwards the request to the controller via
   *       {@link Features#showDashboard(LocalDate, LocalDate)}.</li>
   *   <li>If parsing fails, shows an error dialog and does not call the controller.</li>
   * </ul>
   *
   * <p>No analytics is computed directly in the view; it only gathers user input
   * and delegates to the {@link Features} listener.
   */
  private void showDashboardDialog() {
    if (featuresListener == null) {
      return;
    }

    String startStr = (String) JOptionPane.showInputDialog(
        this, "Enter start date (YYYY-MM-DD):", "Dashboard Start Date",
        JOptionPane.PLAIN_MESSAGE, null, null, selectedDate.toString());
    if (startStr == null || startStr.trim().isEmpty()) {
      return;
    }

    String endStr = (String) JOptionPane.showInputDialog(
        this, "Enter end date (YYYY-MM-DD):", "Dashboard End Date",
        JOptionPane.PLAIN_MESSAGE, null, null, selectedDate.toString());
    if (endStr == null || endStr.trim().isEmpty()) {
      return;
    }

    try {
      LocalDate startDate = LocalDate.parse(startStr.trim());
      LocalDate endDate = LocalDate.parse(endStr.trim());
      featuresListener.showDashboard(startDate, endDate);
    } catch (DateTimeParseException e) {
      displayError("Invalid date format. Please use YYYY-MM-DD.");
    }
  }

  /**
   * Creates a day panel for a specific date.
   */
  private JPanel createDayPanel(LocalDate date) {
    JPanel dayPanel = new JPanel(new BorderLayout());
    dayPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

    if (date.equals(LocalDate.now())) {
      dayPanel.setBackground(new Color(255, 250, 205));
    } else {
      dayPanel.setBackground(Color.WHITE);
    }

    if (date.equals(selectedDate)) {
      dayPanel.setBorder(BorderFactory.createLineBorder(Color.BLUE, 3));
    }

    JLabel dayLabel = new JLabel(String.valueOf(date.getDayOfMonth()));
    dayLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
    dayLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
    dayPanel.add(dayLabel, BorderLayout.NORTH);

    JPanel eventPreviewPanel = new JPanel();
    eventPreviewPanel.setLayout(new BoxLayout(eventPreviewPanel, BoxLayout.Y_AXIS));
    eventPreviewPanel.setOpaque(false);
    dayPanel.add(eventPreviewPanel, BorderLayout.CENTER);

    dayPanel.addMouseListener(new java.awt.event.MouseAdapter() {
      @Override
      public void mouseClicked(java.awt.event.MouseEvent e) {
        selectedDate = date;
        refreshCalendarGrid();
        if (featuresListener != null) {
          featuresListener.dateSelected(date);
        }
      }

      @Override
      public void mouseEntered(java.awt.event.MouseEvent e) {
        if (!date.equals(selectedDate)) {
          dayPanel.setBackground(new Color(230, 240, 255));
        }
      }

      @Override
      public void mouseExited(java.awt.event.MouseEvent e) {
        if (!date.equals(selectedDate)) {
          if (date.equals(LocalDate.now())) {
            dayPanel.setBackground(new Color(255, 250, 205));
          } else {
            dayPanel.setBackground(Color.WHITE);
          }
        }
      }
    });

    return dayPanel;
  }

  /**
   * Updates the month/year label.
   */
  private void updateMonthYearLabel() {
    monthYearLabel.setText(currentMonth.format(
        DateTimeFormatter.ofPattern("MMMM yyyy")));
  }

  /**
   * Navigates to a different month.
   */
  private void navigateMonth(int offset) {
    currentMonth = currentMonth.plusMonths(offset);
    refreshCalendarGrid();
  }

  /**
   * Navigates to current month and today's date.
   */
  private void goToToday() {
    currentMonth = YearMonth.now();
    selectedDate = LocalDate.now();
    refreshCalendarGrid();
    if (featuresListener != null) {
      featuresListener.dateSelected(selectedDate);
    }
  }


  @Override
  public void addFeaturesListener(Features features) {
    this.featuresListener = features;
  }

  @Override
  public void setCurrentCalendar(String calendarName) {
    this.currentCalendarName = calendarName;
    currentCalendarLabel.setText("Calendar: " + calendarName);

    if (!calendarColors.containsKey(calendarName)) {
      calendarColors.put(calendarName,
          CALENDAR_COLORS[colorIndex % CALENDAR_COLORS.length]);
      colorIndex++;
    }

    Color calColor = calendarColors.get(calendarName);
    currentCalendarLabel.setForeground(calColor);
  }

  @Override
  public void setEventsForDate(LocalDate date, List<Event> events) {
    eventListPanel.removeAll();

    eventDateLabel.setText("Events on "
        + date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));

    if (events.isEmpty()) {
      JLabel noEvents = new JLabel("No events scheduled");
      noEvents.setAlignmentX(LEFT_ALIGNMENT);
      noEvents.setBorder(new EmptyBorder(10, 10, 10, 10));
      eventListPanel.add(noEvents);
    } else {
      for (Event event : events) {
        JPanel eventPanel = createEventDisplayPanel(event);
        eventPanel.setAlignmentX(LEFT_ALIGNMENT);
        eventListPanel.add(eventPanel);
        eventListPanel.add(Box.createRigidArea(new Dimension(0, 5)));
      }
    }

    eventListPanel.revalidate();
    eventListPanel.repaint();
  }

  /**
   * Creates a panel to display a single event.
   * For multi-day events, shows appropriate context (start/end indicators).
   */
  private JPanel createEventDisplayPanel(Event event) {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(
            calendarColors.getOrDefault(currentCalendarName, Color.BLUE), 2),
        new EmptyBorder(8, 8, 8, 8)
    ));
    panel.setBackground(Color.WHITE);
    panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

    JLabel subjectLabel = new JLabel(event.getSubject());
    subjectLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
    subjectLabel.setAlignmentX(LEFT_ALIGNMENT);
    panel.add(subjectLabel);

    String timeStr = formatEventTimeForDate(event, selectedDate);

    JLabel timeLabel = new JLabel(timeStr);
    timeLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
    timeLabel.setAlignmentX(LEFT_ALIGNMENT);
    panel.add(timeLabel);

    if (event.getLocation() != null && !event.getLocation().trim().isEmpty()) {
      JLabel locationLabel = new JLabel("Location: " + event.getLocation());
      locationLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
      locationLabel.setAlignmentX(LEFT_ALIGNMENT);
      panel.add(locationLabel);
    }

    if (event.getSeriesId() != null) {
      JLabel seriesLabel = new JLabel("(Recurring)");
      seriesLabel.setFont(new Font("SansSerif", Font.ITALIC, 10));
      seriesLabel.setForeground(Color.GRAY);
      seriesLabel.setAlignmentX(LEFT_ALIGNMENT);
      panel.add(seriesLabel);
    }

    return panel;
  }

  /**
   * Formats event time display for a specific date.
   * Handles multi-day events by showing continuation indicators.
   *
   * @param event       the event to format
   * @param displayDate the date being displayed
   * @return formatted time string with context for multi-day events
   */
  private String formatEventTimeForDate(Event event, LocalDate displayDate) {
    LocalDate startDate = event.getStartDateTime().toLocalDate();
    LocalDate endDate = event.getEndDateTime().toLocalDate();
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM d");

    if (startDate.equals(endDate)) {
      return event.isAllDay() ? "All Day" : event.getStartDateTime().format(timeFormatter)
          + " - " + event.getEndDateTime().format(timeFormatter);
    }

    if (displayDate.equals(startDate)) {
      return event.getStartDateTime().format(timeFormatter)
          + " → "
          + endDate.format(dateFormatter);
    } else if (displayDate.equals(endDate)) {
      return startDate.format(dateFormatter)
          + " → "
          + event.getEndDateTime().format(timeFormatter);
    } else {
      return startDate.format(dateFormatter)
          + " → "
          + endDate.format(dateFormatter);
    }
  }

  @Override
  public void setMonthEvents(Map<LocalDate, List<Event>> eventsByDate) {
    for (int i = 0; i < calendarGridPanel.getComponentCount(); i++) {
      if (calendarGridPanel.getComponent(i) instanceof JPanel) {
        JPanel dayPanel = (JPanel) calendarGridPanel.getComponent(i);
        updateDayPanelEvents(dayPanel, eventsByDate);
      }
    }
  }

  /**
   * Updates the event preview for a single day panel.
   */
  private void updateDayPanelEvents(JPanel dayPanel, Map<LocalDate, List<Event>> eventsByDate) {
    if (dayPanel.getComponentCount() < 2 || !(dayPanel.getComponent(0) instanceof JLabel)) {
      return;
    }

    JLabel dayLabel = (JLabel) dayPanel.getComponent(0);
    try {
      int day = Integer.parseInt(dayLabel.getText().trim());
      LocalDate date = currentMonth.atDay(day);

      if (dayPanel.getComponent(1) instanceof JPanel) {
        JPanel previewPanel = (JPanel) dayPanel.getComponent(1);
        updateEventPreviewPanel(previewPanel, eventsByDate.get(date));
      }
    } catch (NumberFormatException ignored) {
      // Ignore empty cells
    }
  }

  /**
   * Updates the preview panel with event labels.
   */
  private void updateEventPreviewPanel(JPanel previewPanel, List<Event> events) {
    previewPanel.removeAll();

    if (events == null || events.isEmpty()) {
      previewPanel.revalidate();
      previewPanel.repaint();
      return;
    }

    int displayCount = Math.min(events.size(), 3);

    for (int j = 0; j < displayCount; j++) {
      Event event = events.get(j);
      JLabel eventLabel = createEventPreviewLabel(event);
      previewPanel.add(eventLabel);

      if (j < displayCount - 1) {
        previewPanel.add(Box.createRigidArea(new Dimension(0, 2)));
      }
    }

    if (events.size() > 3) {
      previewPanel.add(Box.createRigidArea(new Dimension(0, 2)));
      JLabel moreLabel = new JLabel("+" + (events.size() - 3) + " more");
      moreLabel.setFont(new Font("SansSerif", Font.ITALIC, 9));
      moreLabel.setForeground(Color.GRAY);
      moreLabel.setAlignmentX(LEFT_ALIGNMENT);
      previewPanel.add(moreLabel);
    }

    previewPanel.revalidate();
    previewPanel.repaint();
  }


  /**
   * Creates a label for event preview in day cells.
   * All-day events are shown in blue, timed events in green.
   */
  private JLabel createEventPreviewLabel(Event event) {
    String displayText = event.getSubject();

    if (displayText.length() > 15) {
      displayText = displayText.substring(0, 12) + "...";
    }

    JLabel label = new JLabel(displayText);
    label.setFont(new Font("SansSerif", Font.PLAIN, 10));
    label.setAlignmentX(LEFT_ALIGNMENT);

    label.setMaximumSize(new Dimension(Integer.MAX_VALUE, 16));
    label.setBorder(new EmptyBorder(2, 4, 2, 4));

    if (event.isAllDay()) {
      label.setForeground(Color.WHITE);
      label.setOpaque(true);
      label.setBackground(new Color(66, 133, 244));
    } else {
      label.setForeground(Color.WHITE);
      label.setOpaque(true);
      label.setBackground(new Color(52, 168, 83));
    }

    return label;
  }

  @Override
  public void display() {
    setVisible(true);
  }

  @Override
  public String promptCalendarName() {
    return JOptionPane.showInputDialog(this,
        "Enter calendar name:",
        "New Calendar",
        JOptionPane.PLAIN_MESSAGE);
  }

  @Override
  public String promptTimezone() {
    String[] allTimezones = getAllTimezones();

    return (String) JOptionPane.showInputDialog(
        this,
        "Select timezone:",
        "Timezone Selection",
        JOptionPane.QUESTION_MESSAGE,
        null,
        allTimezones,
        "America/New_York");
  }

  @Override
  public String promptSelectCalendar(List<String> calendarNames) {
    if (calendarNames.isEmpty()) {
      return null;
    }

    return (String) JOptionPane.showInputDialog(
        this,
        "Select a calendar:",
        "Switch Calendar",
        JOptionPane.QUESTION_MESSAGE,
        null,
        calendarNames.toArray(),
        calendarNames.get(0));
  }

  @Override
  public EventFormData showEventCreationDialog(LocalDate date) {
    EventFormDialog dialog = new EventFormDialog(this, date);
    return dialog.showDialog();
  }

  @Override
  public EventEditData showEventEditDialog(LocalDate date, List<Event> events) {
    EventEditDialog dialog = new EventEditDialog(this, events);
    return dialog.showDialog();
  }

  @Override
  public CalendarEditData showCalendarEditDialog(String currentName, String currentTimezone) {
    CalendarEditDialog dialog = new CalendarEditDialog(this, currentName, currentTimezone);
    return dialog.showDialog();
  }

  @Override
  public EventDeleteData showEventDeleteDialog(LocalDate date, List<Event> events) {
    EventDeleteDialog dialog = new EventDeleteDialog(this, events);
    return dialog.showDialog();
  }

  @Override
  public void displayMessage(String message) {
    JOptionPane.showMessageDialog(this, message, "Information",
        JOptionPane.INFORMATION_MESSAGE);
  }

  @Override
  public void displayError(String error) {
    JOptionPane.showMessageDialog(this, error, "Error",
        JOptionPane.ERROR_MESSAGE);
  }

  @Override
  public void displayEvents(List<Event> events) {
    setEventsForDate(selectedDate, events);
  }

  @Override
  public void displayStatus(boolean isBusy) {
    String status = isBusy ? "Busy" : "Available";
    displayMessage("Status: " + status);
  }

  /**
   * Renders the analytics dashboard for a given date interval in a read-only dialog.
   *
   * <p>The summary produced by the model is formatted into a textual report that includes:
   * <ul>
   *   <li>Total number of events and average events per day.</li>
   *   <li>Breakdowns by subject, weekday, week index within the interval, and month.</li>
   *   <li>Busiest and least busy days (by number of events).</li>
   *   <li>Counts and percentages of online vs. offline events.</li>
   * </ul>
   *
   * <p>This method does not modify any model state or events; it only displays
   * the aggregated information to the user.
   *
   * @param summary the {@link CalendarAnalyticsSummary} containing all pre-computed
   *                  metrics for the interval; must not be {@code null}
   * @param startDate the first date (inclusive) of the analytics interval, used
   *                  for labeling; must not be {@code null}
   * @param endDate the last date (inclusive) of the analytics interval, used
   *                  for labeling; must not be {@code null}
   */
  @Override
  public void displayDashboard(CalendarAnalyticsSummary summary,
                               LocalDate startDate,
                               LocalDate endDate) {
    if (summary == null) {
      displayError("No dashboard data available.");
      return;
    }

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    StringBuilder builder = new StringBuilder();

    builder.append("Dashboard for ")
        .append(startDate.format(formatter))
        .append(" to ")
        .append(endDate.format(formatter))
        .append(System.lineSeparator());

    builder.append("Total events: ")
        .append(summary.getTotalEvents())
        .append(System.lineSeparator());

    builder.append("Average events per day: ")
        .append(String.format("%.2f", summary.getAverageEventsPerDay()))
        .append(System.lineSeparator());

    builder.append(System.lineSeparator())
        .append("Events by subject:")
        .append(System.lineSeparator());
    appendIntMap(builder, summary.getEventsBySubject());

    builder.append(System.lineSeparator())
        .append("Events by weekday:")
        .append(System.lineSeparator());
    appendIntMap(builder, summary.getEventsByWeekday());

    builder.append(System.lineSeparator())
        .append("Events by week (index within interval):")
        .append(System.lineSeparator());
    appendIntMap(builder, summary.getEventsByWeekIndex());

    builder.append(System.lineSeparator())
        .append("Events by month (YYYY-MM):")
        .append(System.lineSeparator());
    for (Map.Entry<java.time.YearMonth, Integer> entry
        : summary.getEventsByMonth().entrySet()) {
      builder.append("  ")
          .append(entry.getKey())
          .append(": ")
          .append(entry.getValue())
          .append(System.lineSeparator());
    }

    builder.append(System.lineSeparator())
        .append("Busiest day: ")
        .append(summary.getBusiestDay() == null
            ? "none"
            : summary.getBusiestDay().format(formatter))
        .append(System.lineSeparator());

    builder.append("Least busy day: ")
        .append(summary.getLeastBusyDay() == null
            ? "none"
            : summary.getLeastBusyDay().format(formatter))
        .append(System.lineSeparator());

    int online = summary.getOnlineEventsCount();
    int offline = summary.getOfflineEventsCount();
    int total = online + offline;

    builder.append(System.lineSeparator())
        .append("Online events: ")
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

    JOptionPane.showMessageDialog(
        this,
        builder.toString(),
        "Calendar Dashboard",
        JOptionPane.INFORMATION_MESSAGE);
  }

  private void appendIntMap(StringBuilder builder, Map<?, Integer> map) {
    if (map == null || map.isEmpty()) {
      builder.append("  none")
          .append(System.lineSeparator());
      return;
    }
    for (Map.Entry<?, Integer> entry : map.entrySet()) {
      builder.append("  ")
          .append(entry.getKey())
          .append(": ")
          .append(entry.getValue())
          .append(System.lineSeparator());
    }
  }

  private String formatPercentage(int part, int total) {
    if (total <= 0) {
      return "0.00%";
    }
    double percentage = (double) part * 100.0 / (double) total;
    return String.format("%.2f%%", percentage);
  }
}