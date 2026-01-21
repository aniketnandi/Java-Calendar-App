package calendar.view;

import calendar.model.Event;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.format.DateTimeFormatter;
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

/**
 * Dialog for deleting existing events.
 * Allows user to select event and scope of deletion.
 */
public class EventDeleteDialog {
  private final JFrame parent;
  private final List<Event> events;
  private JDialog dialog;
  private EventDeleteData result;

  private JComboBox<String> eventSelector;
  private JRadioButton singleRadio;
  private JRadioButton fromRadio;
  private JRadioButton allRadio;

  /**
   * Constructs an EventDeleteDialog.
   *
   * @param parent the parent frame
   * @param events the list of events on the specified date
   */
  public EventDeleteDialog(JFrame parent, List<Event> events) {
    this.parent = parent;
    this.events = events;
  }

  /**
   * Shows the dialog and returns the delete data.
   * If there are no events to delete, displays an info message and returns null.
   * If the user cancels, returns null.
   *
   * @return EventDeleteData containing delete information, or null if cancelled
   */
  public EventDeleteData showDialog() {
    if (events.isEmpty()) {
      JOptionPane.showMessageDialog(parent, "No events on this date to delete.",
          "No Events", JOptionPane.INFORMATION_MESSAGE);
      return null;
    }

    result = null;
    createDialog();
    dialog.setVisible(true);
    return result;
  }

  /**
   * Creates and configures the dialog components.
   */
  private void createDialog() {
    dialog = new JDialog(parent, "Delete Event", true);
    dialog.setSize(600, 400);
    dialog.setLocationRelativeTo(parent);
    dialog.setLayout(new GridBagLayout());

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(10, 10, 10, 10);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.WEST;

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 2;
    JLabel titleLabel = new JLabel("🗑️ Delete Event");
    titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
    dialog.add(titleLabel, gbc);

    gbc.gridy = 1;
    gbc.gridwidth = 1;
    gbc.weightx = 0.3;
    JLabel selectLabel = new JLabel("Select Event:");
    selectLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
    dialog.add(selectLabel, gbc);

    gbc.gridx = 1;
    gbc.weightx = 0.7;
    String[] eventDescriptions = events.stream()
        .map(DialogHelper::formatEventForDisplay)
        .toArray(String[]::new);
    eventSelector = new JComboBox<>(eventDescriptions);
    eventSelector.addActionListener(e -> updateScopeVisibility());
    dialog.add(eventSelector, gbc);

    gbc.gridx = 0;
    gbc.gridy = 2;
    gbc.gridwidth = 2;
    JLabel scopeLabel = new JLabel("Delete Scope:");
    scopeLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
    dialog.add(scopeLabel, gbc);

    final ButtonGroup scopeGroup = new ButtonGroup();

    gbc.gridy = 3;
    singleRadio = new JRadioButton("This event only");
    singleRadio.setSelected(true);
    scopeGroup.add(singleRadio);
    dialog.add(singleRadio, gbc);

    gbc.gridy = 4;
    fromRadio = new JRadioButton("This event and all future events in series");
    scopeGroup.add(fromRadio);
    dialog.add(fromRadio, gbc);

    gbc.gridy = 5;
    allRadio = new JRadioButton("All events in series");
    scopeGroup.add(allRadio);
    dialog.add(allRadio, gbc);

    gbc.gridy = 6;
    JLabel warningLabel = new JLabel("Warning: This action cannot be undone!");
    warningLabel.setForeground(Color.RED);
    warningLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
    dialog.add(warningLabel, gbc);

    gbc.gridy = 7;
    JLabel hintLabel = new JLabel("Hint: Use YYYY-MM-DDTHH:mm for dates");
    hintLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
    hintLabel.setForeground(Color.GRAY);
    dialog.add(hintLabel, gbc);

    gbc.gridy = 8;
    gbc.gridwidth = 2;
    final JPanel buttonPanel = new JPanel();

    JButton deleteButton = new JButton("Delete");
    deleteButton.setBackground(new Color(220, 53, 69));
    deleteButton.setForeground(Color.BLACK);
    deleteButton.addActionListener(e -> handleDelete());
    buttonPanel.add(deleteButton);

    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(e -> dialog.dispose());
    buttonPanel.add(cancelButton);

    dialog.add(buttonPanel, gbc);

    updateScopeVisibility();
  }

  /**
   * Updates the visibility of scope options based on whether the selected event
   * is part of a series.
   */
  private void updateScopeVisibility() {
    int selectedIndex = eventSelector.getSelectedIndex();
    DialogHelper.updateSeriesRadioVisibility(
        events, selectedIndex, singleRadio, fromRadio, allRadio);
  }

  /**
   * Handles the delete button action.
   * Confirms deletion with user before proceeding.
   */
  private void handleDelete() {
    int confirm = JOptionPane.showConfirmDialog(
        dialog,
        "Are you sure you want to delete this event?",
        "Confirm Deletion",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.WARNING_MESSAGE
    );

    if (confirm != JOptionPane.YES_OPTION) {
      return;
    }

    int selectedIndex = eventSelector.getSelectedIndex();
    if (selectedIndex < 0 || selectedIndex >= events.size()) {
      JOptionPane.showMessageDialog(dialog, "Invalid event selection.",
          "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }

    Event selectedEvent = events.get(selectedIndex);

    String scope;
    if (singleRadio.isSelected()) {
      scope = "THIS";
    } else if (fromRadio.isSelected()) {
      scope = "THIS_AND_FUTURE";
    } else {
      scope = "ALL";
    }

    result = new EventDeleteData(selectedEvent, scope);
    dialog.dispose();
  }
}