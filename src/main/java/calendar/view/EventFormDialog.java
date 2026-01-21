package calendar.view;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.Set;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * Dialog for creating a new event.
 */
public class EventFormDialog {
  private final JFrame parent;
  private final LocalDate defaultDate;
  private JDialog dialog;
  private EventFormData result;

  private JTextField subjectField;
  private JTextField startDateField;
  private JTextField endDateField;
  private JCheckBox allDayCheck;
  private JTextField startTimeField;
  private JTextField endTimeField;
  private JTextField locationField;
  private JTextField descriptionField;
  private JCheckBox recurringCheck;
  private JCheckBox[] dayCheckboxes;
  private JRadioButton countRadio;
  private JRadioButton untilRadio;
  private JTextField countField;
  private JTextField untilField;
  private JLabel endDateLabel;
  private JComboBox<String> statusCombo;

  /**
   * Constructs an EventFormDialog.
   *
   * @param parent      the parent frame
   * @param defaultDate the default date to populate in the form
   */
  public EventFormDialog(JFrame parent, LocalDate defaultDate) {
    this.parent = parent;
    this.defaultDate = defaultDate;
  }

  /**
   * Shows the dialog and returns the form data, or null if cancelled.
   */
  public EventFormData showDialog() {
    result = null;
    createDialog();
    dialog.setVisible(true);
    return result;
  }

  private void createDialog() {
    dialog = new JDialog(parent, "Create Event", true);
    dialog.setLayout(new BorderLayout(10, 10));
    dialog.setSize(500, 650);
    dialog.setLocationRelativeTo(parent);

    JPanel formPanel = new JPanel(new GridBagLayout());
    formPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;

    int row = 0;

    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.gridwidth = 1;
    formPanel.add(new JLabel("Event Subject:*"), gbc);

    gbc.gridx = 1;
    gbc.gridwidth = 2;
    subjectField = new JTextField(20);
    formPanel.add(subjectField, gbc);
    row++;

    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.gridwidth = 1;
    formPanel.add(new JLabel("Start Date (YYYY-MM-DD):"), gbc);

    gbc.gridx = 1;
    gbc.gridwidth = 2;
    startDateField = new JTextField(defaultDate.toString());
    formPanel.add(startDateField, gbc);
    row++;

    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.gridwidth = 1;
    endDateLabel = new JLabel("End Date (YYYY-MM-DD):*");
    formPanel.add(endDateLabel, gbc);

    gbc.gridx = 1;
    gbc.gridwidth = 2;
    endDateField = new JTextField(defaultDate.toString());
    formPanel.add(endDateField, gbc);
    row++;

    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.gridwidth = 3;
    allDayCheck = new JCheckBox("All-day event");
    allDayCheck.addActionListener(e -> {
      boolean allDay = allDayCheck.isSelected();
      startTimeField.setEnabled(!allDay);
      endTimeField.setEnabled(!allDay);
    });
    formPanel.add(allDayCheck, gbc);
    row++;

    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.gridwidth = 1;
    formPanel.add(new JLabel("Start Time (HH:mm):*"), gbc);

    gbc.gridx = 1;
    gbc.gridwidth = 2;
    startTimeField = new JTextField("09:00");
    formPanel.add(startTimeField, gbc);
    row++;

    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.gridwidth = 1;
    formPanel.add(new JLabel("End Time (HH:mm):*"), gbc);

    gbc.gridx = 1;
    gbc.gridwidth = 2;
    endTimeField = new JTextField("10:00");
    formPanel.add(endTimeField, gbc);
    row++;

    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.gridwidth = 1;
    formPanel.add(new JLabel("Location:"), gbc);

    gbc.gridx = 1;
    gbc.gridwidth = 2;
    locationField = new JTextField();
    formPanel.add(locationField, gbc);
    row++;

    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.gridwidth = 1;
    formPanel.add(new JLabel("Description:"), gbc);

    gbc.gridx = 1;
    gbc.gridwidth = 2;
    descriptionField = new JTextField();
    formPanel.add(descriptionField, gbc);
    row++;

    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.gridwidth = 1;
    formPanel.add(new JLabel("Status:"), gbc);

    gbc.gridx = 1;
    gbc.gridwidth = 2;
    String[] statuses = {"PUBLIC", "PRIVATE"};
    statusCombo = new JComboBox<>(statuses);
    formPanel.add(statusCombo, gbc);
    row++;

    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.gridwidth = 3;
    recurringCheck = new JCheckBox("Recurring event");
    recurringCheck.addActionListener(e -> toggleRecurringFields());
    formPanel.add(recurringCheck, gbc);
    row++;

    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.gridwidth = 3;
    JPanel weekdaysPanel = createWeekdaysPanel();
    formPanel.add(weekdaysPanel, gbc);
    row++;

    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.gridwidth = 3;
    JPanel recurrencePanel = createRecurrenceEndPanel();
    formPanel.add(recurrencePanel, gbc);

    JPanel buttonPanel = new JPanel();
    JButton createButton = new JButton("Create");
    JButton cancelButton = new JButton("Cancel");

    createButton.addActionListener(e -> handleCreate());
    cancelButton.addActionListener(e -> dialog.dispose());

    buttonPanel.add(createButton);
    buttonPanel.add(cancelButton);

    dialog.add(formPanel, BorderLayout.CENTER);
    dialog.add(buttonPanel, BorderLayout.SOUTH);
  }

  private JPanel createWeekdaysPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBorder(new EmptyBorder(5, 20, 5, 5));

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(2, 5, 2, 5);
    gbc.anchor = GridBagConstraints.WEST;

    String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    dayCheckboxes = new JCheckBox[7];

    for (int i = 0; i < 7; i++) {
      dayCheckboxes[i] = new JCheckBox(dayNames[i]);
      dayCheckboxes[i].setEnabled(false);
      gbc.gridx = i % 4;
      gbc.gridy = i / 4;
      panel.add(dayCheckboxes[i], gbc);
    }

    return panel;
  }

  private JPanel createRecurrenceEndPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBorder(new EmptyBorder(5, 20, 5, 5));

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(3, 5, 3, 5);
    gbc.fill = GridBagConstraints.HORIZONTAL;

    ButtonGroup group = new ButtonGroup();
    countRadio = new JRadioButton("Repeat count:");
    untilRadio = new JRadioButton("Repeat until:");
    group.add(countRadio);
    group.add(untilRadio);

    countRadio.setEnabled(false);
    untilRadio.setEnabled(false);
    countRadio.setSelected(true);

    countField = new JTextField("5", 8);
    countField.setEnabled(false);

    untilField = new JTextField(defaultDate.plusMonths(1).toString(), 12);
    untilField.setEnabled(false);

    countRadio.addActionListener(e -> {
      countField.setEnabled(true);
      untilField.setEnabled(false);
    });

    untilRadio.addActionListener(e -> {
      countField.setEnabled(false);
      untilField.setEnabled(true);
    });

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 1;
    panel.add(countRadio, gbc);

    gbc.gridx = 1;
    panel.add(countField, gbc);

    gbc.gridx = 0;
    gbc.gridy = 1;
    panel.add(untilRadio, gbc);

    gbc.gridx = 1;
    panel.add(untilField, gbc);

    return panel;
  }

  /**
   * Toggles recurring-related fields based on recurring checkbox state.
   * When recurring is checked, end date is hidden and forced to equal start date.
   * When unchecked, end date is shown for multi-day events.
   */

  private void toggleRecurringFields() {
    boolean recurring = recurringCheck.isSelected();
    endDateLabel.setVisible(!recurring);
    endDateField.setVisible(!recurring);

    for (JCheckBox cb : dayCheckboxes) {
      cb.setEnabled(recurring);
    }

    countRadio.setEnabled(recurring);
    untilRadio.setEnabled(recurring);

    if (recurring) {
      countField.setEnabled(true);
      untilField.setEnabled(false);
    } else {
      countField.setEnabled(false);
      untilField.setEnabled(false);
    }
    dialog.revalidate();
    dialog.repaint();
  }

  private void handleCreate() {
    try {
      final String subject = subjectField.getText().trim();
      LocalDate startDate = LocalDate.parse(startDateField.getText().trim());
      boolean recurring = recurringCheck.isSelected();
      LocalDate endDate;

      if (recurring) {
        endDate = startDate;
      } else {
        endDate = LocalDate.parse(endDateField.getText().trim());
      }

      boolean allDay = allDayCheck.isSelected();

      LocalTime startTime = null;
      LocalTime endTime = null;

      if (!allDay) {
        startTime = LocalTime.parse(startTimeField.getText().trim());
        endTime = LocalTime.parse(endTimeField.getText().trim());
      }

      String location = locationField.getText().trim();
      String description = descriptionField.getText().trim();
      String status = (String) statusCombo.getSelectedItem();

      Set<DayOfWeek> weekdays = null;
      boolean useCount = false;
      int repeatCount = 0;
      LocalDate repeatUntil = null;

      if (recurring) {
        weekdays = getSelectedWeekdays();
        useCount = countRadio.isSelected();
        if (useCount) {
          repeatCount = Integer.parseInt(countField.getText().trim());
        } else {
          repeatUntil = LocalDate.parse(untilField.getText().trim());
        }
      }

      result = new EventFormData(subject, startDate, endDate, allDay, startTime, endTime,
          location, description, status, recurring, weekdays, useCount, repeatCount, repeatUntil);

      dialog.dispose();

    } catch (DateTimeParseException e) {
      showError("Invalid date or time format. Use YYYY-MM-DD for dates and HH:mm for times.");
    } catch (NumberFormatException e) {
      showError("Invalid repeat count. Please enter a number.");
    } catch (Exception e) {
      showError("Error: " + e.getMessage());
    }
  }

  private Set<DayOfWeek> getSelectedWeekdays() {
    Set<DayOfWeek> weekdays = new HashSet<>();
    DayOfWeek[] days = {DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY, DayOfWeek.SATURDAY};

    for (int i = 0; i < dayCheckboxes.length; i++) {
      if (dayCheckboxes[i].isSelected()) {
        weekdays.add(days[i]);
      }
    }

    return weekdays;
  }

  private void showError(String message) {
    javax.swing.JOptionPane.showMessageDialog(dialog, message, "Validation Error",
        javax.swing.JOptionPane.ERROR_MESSAGE);
  }
}