package calendar.view;

import calendar.model.Event;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * Dialog for editing existing events.
 * Allows user to select event and edit its properties with separate fields.
 */
public class EventEditDialog {
  private final JFrame parent;
  private final List<Event> events;
  private JDialog dialog;
  private EventEditData result;

  private JComboBox<String> eventSelector;
  private JTextField subjectField;
  private JTextField startDateField;
  private JTextField endDateField;
  private JTextField startTimeField;
  private JTextField endTimeField;
  private JTextField locationField;
  private JTextArea descriptionArea;
  private JComboBox<String> statusCombo;
  private JRadioButton singleRadio;
  private JRadioButton fromRadio;
  private JRadioButton allRadio;
  private JLabel endDateLabel;


  /**
   * Constructs an EventEditDialog.
   *
   * @param parent the parent frame
   * @param events the list of events on the specified date
   */
  public EventEditDialog(JFrame parent, List<Event> events) {
    this.parent = parent;
    this.events = events;
  }

  /**
   * Shows the dialog and returns the edit data.
   * If there are no events to edit, displays an info message and returns null.
   * If the user cancels, returns null.
   *
   * @return EventEditData containing edit information, or null if cancelled
   */
  public EventEditData showDialog() {
    if (events.isEmpty()) {
      JOptionPane.showMessageDialog(parent, "No events on this date to edit.",
          "No Events", JOptionPane.INFORMATION_MESSAGE);
      return null;
    }

    result = null;
    createDialog();
    dialog.setVisible(true);
    return result;
  }

  private void createDialog() {
    dialog = new JDialog(parent, "Edit Event", true);
    dialog.setLayout(new BorderLayout(10, 10));
    dialog.setSize(600, 650);
    dialog.setLocationRelativeTo(parent);

    JPanel formPanel = new JPanel(new GridBagLayout());
    formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;

    int row = 0;

    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.gridwidth = 1;
    gbc.weightx = 0.3;
    formPanel.add(new JLabel("Select Event:*"), gbc);

    gbc.gridx = 1;
    gbc.weightx = 0.7;
    String[] eventNames = events.stream()
        .map(this::formatEventForDisplay)
        .toArray(String[]::new);
    eventSelector = new JComboBox<>(eventNames);
    eventSelector.addActionListener(e -> {
      loadEventData();
      updateVisibility();
    });
    formPanel.add(eventSelector, gbc);
    row++;

    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.weightx = 0.3;
    formPanel.add(new JLabel("Subject:*"), gbc);

    gbc.gridx = 1;
    gbc.weightx = 0.7;
    subjectField = new JTextField(20);
    formPanel.add(subjectField, gbc);
    row++;

    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.weightx = 0.3;
    formPanel.add(new JLabel("Start Date (yyyy-MM-dd):*"), gbc);

    gbc.gridx = 1;
    gbc.weightx = 0.7;
    startDateField = new JTextField(20);
    formPanel.add(startDateField, gbc);
    row++;

    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.weightx = 0.3;
    endDateLabel = new JLabel("End Date (yyyy-MM-dd):*");
    formPanel.add(endDateLabel, gbc);

    gbc.gridx = 1;
    gbc.weightx = 0.7;
    endDateField = new JTextField(20);
    formPanel.add(endDateField, gbc);
    row++;

    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.weightx = 0.3;
    formPanel.add(new JLabel("Start Time (HH:mm):*"), gbc);

    gbc.gridx = 1;
    gbc.weightx = 0.7;
    startTimeField = new JTextField(20);
    formPanel.add(startTimeField, gbc);
    row++;

    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.weightx = 0.3;
    formPanel.add(new JLabel("End Time (HH:mm):*"), gbc);

    gbc.gridx = 1;
    gbc.weightx = 0.7;
    endTimeField = new JTextField(20);
    formPanel.add(endTimeField, gbc);
    row++;

    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.weightx = 0.3;
    formPanel.add(new JLabel("Location:"), gbc);

    gbc.gridx = 1;
    gbc.weightx = 0.7;
    locationField = new JTextField(20);
    formPanel.add(locationField, gbc);
    row++;

    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.weightx = 0.3;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    formPanel.add(new JLabel("Description:"), gbc);

    gbc.gridx = 1;
    gbc.weightx = 0.7;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weighty = 0.3;
    descriptionArea = new JTextArea(4, 20);
    descriptionArea.setLineWrap(true);
    descriptionArea.setWrapStyleWord(true);
    JScrollPane descScroll = new JScrollPane(descriptionArea);
    formPanel.add(descScroll, gbc);
    row++;

    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.weightx = 0.3;
    gbc.weighty = 0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.WEST;
    formPanel.add(new JLabel("Status:"), gbc);

    gbc.gridx = 1;
    gbc.weightx = 0.7;
    String[] statuses = {"PUBLIC", "PRIVATE"};
    statusCombo = new JComboBox<>(statuses);
    formPanel.add(statusCombo, gbc);
    row++;

    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.gridwidth = 2;
    gbc.weightx = 1.0;
    JLabel scopeLabel = new JLabel("Edit Scope:");
    scopeLabel.setBorder(new EmptyBorder(10, 0, 5, 0));
    formPanel.add(scopeLabel, gbc);
    row++;

    final ButtonGroup scopeGroup = new ButtonGroup();

    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.gridwidth = 2;
    singleRadio = new JRadioButton("This event only");
    singleRadio.setSelected(true);
    singleRadio.addActionListener(e -> updateVisibility());
    scopeGroup.add(singleRadio);
    formPanel.add(singleRadio, gbc);
    row++;

    gbc.gridy = row;
    fromRadio = new JRadioButton("This event and all future events in series");
    fromRadio.addActionListener(e -> updateVisibility());
    scopeGroup.add(fromRadio);
    formPanel.add(fromRadio, gbc);
    row++;

    gbc.gridy = row;
    allRadio = new JRadioButton("All events in series");
    allRadio.addActionListener(e -> updateVisibility());
    scopeGroup.add(allRadio);
    formPanel.add(allRadio, gbc);

    JPanel buttonPanel = new JPanel();
    JButton saveButton = new JButton("Save Changes");
    JButton cancelButton = new JButton("Cancel");

    saveButton.addActionListener(e -> handleSave());
    cancelButton.addActionListener(e -> dialog.dispose());

    buttonPanel.add(saveButton);
    buttonPanel.add(cancelButton);

    dialog.add(formPanel, BorderLayout.CENTER);
    dialog.add(buttonPanel, BorderLayout.SOUTH);

    loadEventData();
    updateVisibility();
  }

  /**
   * Loads the selected event's data into the form fields.
   */
  private void loadEventData() {
    int selectedIndex = eventSelector.getSelectedIndex();
    if (selectedIndex >= 0 && selectedIndex < events.size()) {
      Event event = events.get(selectedIndex);

      subjectField.setText(event.getSubject());

      LocalDate startDate = event.getStartDateTime().toLocalDate();
      LocalDate endDate = event.getEndDateTime().toLocalDate();

      startDateField.setText(startDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
      endDateField.setText(endDate.format(DateTimeFormatter.ISO_LOCAL_DATE));

      if (event.isAllDay()) {
        startTimeField.setText("08:00");
        endTimeField.setText("17:00");
      } else {
        LocalTime startTime = event.getStartDateTime().toLocalTime();
        LocalTime endTime = event.getEndDateTime().toLocalTime();
        startTimeField.setText(startTime.format(DateTimeFormatter.ofPattern("HH:mm")));
        endTimeField.setText(endTime.format(DateTimeFormatter.ofPattern("HH:mm")));
      }

      locationField.setText(event.getLocation() != null ? event.getLocation() : "");
      descriptionArea.setText(event.getDescription() != null ? event.getDescription() : "");
      statusCombo.setSelectedItem(event.getStatus().toString());
    }
  }

  /**
   * Updates field visibility based on edit scope.
   * End date field only shown for single event edits.
   */
  private void updateVisibility() {
    int selectedIndex = eventSelector.getSelectedIndex();

    DialogHelper.updateSeriesRadioVisibility(
        events, selectedIndex, singleRadio, fromRadio, allRadio);

    if (selectedIndex >= 0 && selectedIndex < events.size()) {
      boolean isSingleEdit = singleRadio.isSelected();
      endDateLabel.setVisible(isSingleEdit);
      endDateField.setVisible(isSingleEdit);
    }

    dialog.revalidate();
    dialog.repaint();
  }

  private String formatEventForDisplay(Event event) {
    return DialogHelper.formatEventForDisplay(event);
  }

  private void handleSave() {
    try {
      int selectedIndex = eventSelector.getSelectedIndex();
      if (selectedIndex < 0) {
        showError("Please select an event");
        return;
      }

      final Event selectedEvent = events.get(selectedIndex);

      String subject = subjectField.getText().trim();

      String startDateStr = startDateField.getText().trim();
      String endDateStr = endDateField.getText().trim();
      String startTimeStr = startTimeField.getText().trim();
      String endTimeStr = endTimeField.getText().trim();

      LocalDate startDate;
      LocalDate endDate;
      LocalTime startTime;
      LocalTime endTime;

      try {
        startDate = LocalDate.parse(startDateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        startTime = LocalTime.parse(startTimeStr, DateTimeFormatter.ofPattern("HH:mm"));
        endTime = LocalTime.parse(endTimeStr, DateTimeFormatter.ofPattern("HH:mm"));

        if (fromRadio.isSelected() || allRadio.isSelected()) {
          endDate = startDate;
        } else {
          if (endDateStr.isEmpty()) {
            endDate = startDate;
          } else {
            endDate = LocalDate.parse(endDateStr, DateTimeFormatter.ISO_LOCAL_DATE);
          }
        }
      } catch (DateTimeParseException e) {
        showError("Invalid date or time format. Use yyyy-MM-dd for date and HH:mm for time");
        return;
      }

      LocalDateTime startDateTime = LocalDateTime.of(startDate, startTime);
      LocalDateTime endDateTime = LocalDateTime.of(endDate, endTime);

      String location = locationField.getText().trim();
      String description = descriptionArea.getText().trim();
      String status = (String) statusCombo.getSelectedItem();

      String scope;
      if (singleRadio.isSelected()) {
        scope = "single";
      } else if (fromRadio.isSelected()) {
        scope = "from";
      } else {
        scope = "all";
      }

      result = new EventEditData(
          selectedEvent,
          subject,
          startDateTime,
          endDateTime,
          location,
          description,
          status,
          scope
      );

      dialog.dispose();

    } catch (Exception e) {
      showError("Error: " + e.getMessage());
    }
  }

  private void showError(String message) {
    JOptionPane.showMessageDialog(dialog, message, "Validation Error",
        JOptionPane.ERROR_MESSAGE);
  }
}