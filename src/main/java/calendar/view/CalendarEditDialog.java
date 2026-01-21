package calendar.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;


/**
 * Dialog for editing calendar properties (name or timezone).
 */
public class CalendarEditDialog {
  private final JFrame parent;
  private final String currentName;
  private final String currentTimezone;
  private JDialog dialog;
  private CalendarEditData result;

  private JComboBox<String> propertySelector;
  private JPanel inputPanel;
  private CardLayout cardLayout;

  private JTextField nameField;
  private JComboBox<String> timezoneCombo;

  /**
   * Creates a new dialog with the given calendar name and timezone.
   *
   * @param parent the parent window for display
   * @param currentName the existing calendar name shown to the user
   * @param currentTimezone the existing calendar timezone shown to the user
   */
  public CalendarEditDialog(JFrame parent, String currentName, String currentTimezone) {
    this.parent = parent;
    this.currentName = currentName;
    this.currentTimezone = currentTimezone;
  }

  /**
   * Displays the edit dialog and waits for the user's input.
   *
   * @return a CalendarEditData object with the chosen property and value,
   *         or null if the user cancels the dialog
   */
  public CalendarEditData showDialog() {
    result = null;
    createDialog();
    dialog.setVisible(true);
    return result;
  }

  /**
   * Builds and configures all UI components for the edit dialog.
   */
  private void createDialog() {
    dialog = new JDialog(parent, "Edit Calendar", true);
    dialog.setLayout(new BorderLayout(10, 10));
    dialog.setSize(450, 250);
    dialog.setLocationRelativeTo(parent);

    JPanel formPanel = new JPanel(new GridBagLayout());
    formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(10, 5, 10, 5);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 2;
    JLabel infoLabel = new JLabel("<html><b>Current:</b> " + currentName
        + " (" + currentTimezone + ")</html>");
    formPanel.add(infoLabel, gbc);

    gbc.gridy = 1;
    gbc.gridwidth = 1;
    formPanel.add(new JLabel("Property:"), gbc);

    gbc.gridx = 1;
    String[] properties = {"name", "timezone"};
    propertySelector = new JComboBox<>(properties);
    propertySelector.addActionListener(e -> switchInputField());
    formPanel.add(propertySelector, gbc);

    gbc.gridx = 0;
    gbc.gridy = 2;
    formPanel.add(new JLabel("New Value:"), gbc);

    gbc.gridx = 1;
    cardLayout = new CardLayout();
    inputPanel = new JPanel(cardLayout);

    nameField = new JTextField(20);
    inputPanel.add(nameField, "name");

    timezoneCombo = new JComboBox<>(getAllTimezones());
    timezoneCombo.setSelectedItem(currentTimezone);
    inputPanel.add(timezoneCombo, "timezone");

    formPanel.add(inputPanel, gbc);

    switchInputField();

    JPanel buttonPanel = new JPanel();
    JButton saveButton = new JButton("Save Changes");
    JButton cancelButton = new JButton("Cancel");

    saveButton.addActionListener(e -> handleSave());
    cancelButton.addActionListener(e -> dialog.dispose());

    buttonPanel.add(saveButton);
    buttonPanel.add(cancelButton);

    dialog.add(formPanel, BorderLayout.CENTER);
    dialog.add(buttonPanel, BorderLayout.SOUTH);
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
   * Switches the input field based on selected property.
   * Shows text field for name, dropdown for timezone.
   */
  private void switchInputField() {
    String selectedProperty = (String) propertySelector.getSelectedItem();
    cardLayout.show(inputPanel, selectedProperty);
  }

  /**
   * Handles the save action, validating input and creating result.
   */
  private void handleSave() {
    String property = (String) propertySelector.getSelectedItem();
    String value;

    if ("name".equals(property)) {
      value = nameField.getText().trim();
    } else {
      value = (String) timezoneCombo.getSelectedItem();
    }

    if (value == null || value.isEmpty()) {
      JOptionPane.showMessageDialog(dialog, "Value cannot be empty",
          "Validation Error", JOptionPane.ERROR_MESSAGE);
      return;
    }

    result = new CalendarEditData(property, value);
    dialog.dispose();
  }
}