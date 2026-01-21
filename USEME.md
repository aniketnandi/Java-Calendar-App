# Calendar Application - User Guide

## Dashboard Feature
This submission adds a new analytics dashboard feature that summarizes calendar activity over
a user-selected date range. The dashboard is fully supported in the Interactive, GUI, and headless
script mode. No existing commands or workflows were modified.

### Interactive Mode
A new command is available: `show dashboard from <start-date> to <end-date>`

- Dates must follow the YYYY-MM-DD format.
- This command must be entered after selecting a calendar using: `use calendar --name <calendar-name>`
  e.g. `show dashboard from 2025-01-01 to 2025-03-01`
- The console will display:
  - total number of events
  - total number of events by subject
  - total number of events by weekday
  - total number of events by week index
  - total number of events by month
  - average events per day
  - the busiest and least busy date
  - percentage of online vs. offline events
  
If the interval contains no events, the system reports that dashboard data is unavailable.

#### Steps to run the program in Interactive Mode
Run the application with console input:
```bash
java -jar build/libs/calendar-1.0.jar --mode interactive
```

**What happens:**
- The application starts in text mode
- Enter commands one line at a time
- Type `exit` to quit

**Example Session**
```
java -jar build/libs/calendar-1.0.jar --mode interactive
Calendar started. Enter commands (type 'exit' to quit): 

create calendar --name Work --timezone America/New_York
Calendar 'Work' created successfully with timezone America/New_York

use calendar --name Work
Now using calendar: Work

create event "Team Meeting" from 2025-05-05T10:00 to 2025-05-05T11:00
Event created successfully.

show calendar dashboard from 2023-12-01 to 2025-12-31
Calendar dashboard from 2023-12-01 to 2025-12-31
Total events: 1
Average events per day: 0.00
Events by subject:
  Team Meeting: 1
Events by weekday:
  MONDAY: 1
Events by week (index within interval):
  75: 1
Events by month (YYYY-MM):
  2025-05: 1
Busiest day: 2025-05-05
Least busy day: 2025-05-05
Online events: 0 (0.00%)
Offline / other location events: 1 (100.00%)


exit
Calendar has been terminated.
```

### GUI Mode
The Swing interface now includes a "Show Dashboard" button in the top menu bar.
Selecting this option prompts the user for a start and end date (sample values are pre-filled
for convenience). After valid dates are entered, a dialog window displays the analytics summary
containing all required metrics.

The dashboard display is read-only and does not modify any event or calendar data.

#### Steps to run the program in GUI mode
Launch the graphical user interface by running the JAR without arguments:
```bash
java -jar build/libs/calendar-1.0.jar
```

Or simply double-click the JAR file.

**What happens:**
- A calendar window opens showing the current month
- A default calendar is automatically created and selected
- You can immediately start creating and managing events

### Headless Mode
Dashboard commands can also be executed from a script file.

**Example script:**
```
use calendar --name Work
show dashboard from 2023-01-01 to 2026-02-01
exit
```

**Run using:**
`java -jar build/libs/calendar.jar --mode headless <script-file>` from the terminal.

e.g. `java -jar build/libs/calendar-1.0.jar --mode headless commands.txt`

The analytics summary is printed to standard output during execution.

### Additional Notes
- The dashboard feature is fully implemented and supports all required analytics metrics listed in the
  assignment description.
- The dashboard is accessible in all three modes: GUI, interactive text, and headless script mode.
- The text command `show dashboard from <start-date> to <end-date>`
  is fully supported and must be executed after selecting a calendar with use calendar.
- The GUI includes a "Show Dashboard" button, allowing users to enter a date range and view the
  dashboard summary in a dialog window.
- The dashboard always analyzes the currently active calendar and uses the inclusive [start, end]
  date range provided by the user.
- The feature integrates with the existing model–controller–view architecture without requiring
  changes to unrelated components.
- No known bugs in the provider code prevented the completion of the dashboard implementation.
- The dashboard is read-only and does not modify event data.

# --- END OF ASSIGNMENT 7 USEME ---

## Overview
This is a multi-calendar graphical application that supports creating and managing multiple calendars with different timezones, creating single and recurring events, and viewing events in an interactive monthly calendar interface. The application also supports text-based interactive mode and headless scripting mode.

## Running the Application

### Prerequisites
- JAR file built (located in `build/libs/`)

### Building the JAR
From the project root directory, run:
```bash
./gradlew jar
```

This will create a JAR file in `build/libs/` directory (e.g., `calendar-1.0.jar`).

---

## Three Modes of Operation

### 1. GUI Mode (Default)
Launch the graphical user interface by running the JAR without arguments:
```bash
java -jar build/libs/calendar-1.0.jar
```

Or simply double-click the JAR file.

**What happens:**
- A calendar window opens showing the current month
- A default calendar is automatically created and selected
- You can immediately start creating and managing events

---

### 2. Interactive Text Mode
Run the application with console input:
```bash
java -jar build/libs/calendar-1.0.jar --mode interactive
```

**What happens:**
- The application starts in text mode
- Enter commands one line at a time
- Type `exit` to quit

**Example session:**
```
$ java -jar build/libs/calendar-1.0.jar --mode interactive

Calendar started. Enter commands (type 'exit' to quit): 

create calendar --name Work --timezone America/New_York
Calendar 'Work' created successfully with timezone America/New_York

use calendar --name Work
Now using calendar: Work

create event "Team Meeting" from 2025-05-05T10:00 to 2025-05-05T11:00
Event created successfully.

exit
Calendar has been terminated.
```

---

### 3. Headless Mode
Run commands from a script file:
```bash
java -jar build/libs/calendar-1.0.jar --mode headless res/commands.txt
```

**What happens:**
- Executes all commands in the file sequentially
- Exits automatically when complete
- The script file must end with `exit`

**Example commands.txt:**
```
create calendar --name Work --timezone America/New_York
use calendar --name Work
create event "Project Review" from 2025-05-05T14:00 to 2025-05-05T15:00
print events on 2025-05-05
exit
```

**Run it:**
```bash
java -jar build/libs/calendar-1.0.jar --mode headless res/commands.txt
```

---

## Using the GUI

The graphical interface provides an intuitive way to manage your calendars and events. Below is a bullet-point guide for each operation.

### Calendar Management Operations

#### **Create a New Calendar**
- Click the **"New Calendar"** button in the top-left area
- Enter a calendar name in the dialog box
- Select a timezone from the dropdown list 
- Click **OK** to create the calendar
- The new calendar appears in the current calendar display with a unique color

#### **Switch Between Calendars**
- Click the **"Switch Calendar"** button
- A dialog shows all available calendars
- Select the calendar you want to use
- Click **OK**
- The calendar name updates in the top bar with its assigned color
- The month view refreshes to show events from the selected calendar

#### **Edit Current Calendar**
- Click the **"Edit Calendar"** button
- The dialog shows the current calendar's name and timezone
- Select which property to edit: **name** or **timezone**
    - For **name**: A text field appears to enter the new name
    - For **timezone**: A dropdown appears with all available timezones
- Enter/select the new value
- Click **"Save Changes"**
- The calendar updates immediately

---

### Month Navigation

#### **Navigate to Different Months**
- Click **"< Previous"** to go to the previous month
- Click **"Next >"** to go to the next month
- Click **"Today"** to return to the current month and select today's date
- The month display updates at the top center

#### **Select a Date**
- Click on any date in the calendar grid
- The selected date is highlighted with a blue border
- Today's date has a light yellow background
- Events for the selected date appear in the right sidebar

---

### Event Operations

#### **Create a Single Event**
- Select a date by clicking on it in the calendar grid
- Click the **"Create Event"** button in the right sidebar
- Fill in the event form:
    - **Event Subject** (required): Enter the event name
    - **Start Date**: Pre-filled with selected date (format: YYYY-MM-DD)
    - **End Date**: Pre-filled with selected date (format: YYYY-MM-DD)
    - **All-day event** checkbox: Check for all-day events
    - **Start Time**: Enter time in HH:mm format (24-hour, e.g., 09:00)
    - **End Time**: Enter time in HH:mm format (e.g., 17:00)
    - **Location** (optional): Enter event location
    - **Description** (optional): Enter event details
    - Leave **"Recurring event"** unchecked for single events
- Click **"Create"**
- The event appears on the calendar and in the events list

**Note:** If "All-day event" is checked, time fields are disabled and the event runs from 8:00 AM to 5:00 PM.

#### **Create a Recurring Event**
- Select a date and click **"Create Event"**
- Fill in basic event details (subject, start time, end time, etc.)
- Check the **"Recurring event"** checkbox
- Select weekdays when the event should occur:
    - Check boxes for: **Sun, Mon, Tue, Wed, Thu, Fri, Sat**
    - At least one weekday must be selected
- Choose how the recurrence ends:
    - **Repeat count**: Select this and enter a number (e.g., 10 times)
    - **Repeat until**: Select this and enter an end date (YYYY-MM-DD)
- Click **"Create"**
- Multiple event instances are generated across the calendar

**Note:** For recurring events, the end date field is hidden and automatically set to match the start date.

#### **Edit Events**
- Select the date containing the event(s) you want to edit
- Click the **"Edit Events"** button
- A dialog appears:
    - **Select Event**: Choose the event from the dropdown (shows event name and time)
    - **Edit fields**: Modify any of the following:
        - Subject
        - Start Date (YYYY-MM-DD)
        - End Date (YYYY-MM-DD) - only shown for "This event only" scope
        - Start Time (HH:mm)
        - End Time (HH:mm)
        - Location
        - Description
        - Status (PUBLIC or PRIVATE)
    - **Edit Scope** (for recurring events only):
        - **"This event only"**: Changes apply to just this occurrence
        - **"This event and all future events in series"**: Changes apply from this date forward
        - **"All events in series"**: Changes apply to every occurrence
- Click **"Save Changes"**
- The changes are applied and the calendar refreshes

**Note:** For single (non-recurring) events, only "This event only" option is shown.

#### **Delete Events**
- Select the date containing the event(s) you want to delete
- Click the **"Delete Event"** button
- A dialog appears:
    - **Select Event**: Choose the event to delete from the dropdown
    - **Delete Scope** (for recurring events only):
        - **"This event only"**: Deletes just this occurrence
        - **"This event and all future events in series"**: Deletes from this date forward
        - **"All events in series"**: Deletes every occurrence
    - **Warning message**: "This action cannot be undone!"
- Click **"Delete"**
- A confirmation dialog asks: "Are you sure you want to delete this event?"
- Click **"Yes"** to confirm
- The event is removed and the calendar refreshes

**Note:** For single events, only "This event only" option is shown.

---

### Viewing Events

#### **View Events for a Specific Date**
- Click on any date in the calendar grid
- The right sidebar updates to show:
    - Header: "Events on [Month Day, Year]"
    - List of all events on that date
    - Each event shows:
        - Subject (bold)
        - Time or "All Day"
        - Location (if present)
        - "(Recurring)" indicator for recurring events
- If no events exist: "No events scheduled" is displayed

#### **View Multi-Day Events**
- Multi-day events appear on each date they span
- For the start date: Shows start time → end date
- For the end date: Shows start date → end time
- For dates in between: Shows start date → end date
- Example: An event from May 5 to May 7
    - May 5: "2:00 PM → May 7"
    - May 6: "May 5 → May 7"
    - May 7: "May 5 → 4:00 PM"

#### **View Event Previews in Calendar Grid**
- Each date cell shows up to 3 event previews
- Event labels show truncated subject (up to 15 characters)
- Color coding:
    - **Blue labels**: All-day events
    - **Green labels**: Timed events
- If more than 3 events exist: "+N more" indicator appears

---

### Visual Indicators

#### **Calendar Colors**
- Each calendar is assigned a unique color from a palette
- The current calendar name displays in its assigned color at the top
- Event borders in the sidebar match the calendar's color

#### **Date Highlighting**
- **Today's date**: Light yellow background
- **Selected date**: Bold blue border (3px)
- **Hover effect**: Light blue background when hovering over unselected dates

#### **Event Status**
- Events can be marked as PUBLIC or PRIVATE
- Status is shown in event edit/create dialogs
- Status affects event visibility/privacy (as per application logic)

---

## Date and Time Formats

When entering dates and times in the GUI:
- **Date format:** `YYYY-MM-DD` (e.g., 2025-05-05)
- **Time format:** `HH:mm` in 24-hour format (e.g., 14:30 for 2:30 PM, 09:00 for 9:00 AM)
- **DateTime format (for text mode):** `YYYY-MM-DDTHH:mm` (e.g., 2025-05-05T14:30)

---

## Error Handling

The GUI handles errors gracefully:
- **Invalid input**: Error dialogs appear with specific messages explaining the issue
- **Validation errors**: Form validation prevents invalid dates/times
    - End date cannot be before start date
    - End time must be after start time for same-day events
    - Subject cannot be empty
    - At least one weekday required for recurring events
- **Calendar operations**: Error messages display if calendar operations fail
- The application remains functional after any error

---

## Example Workflow: Complete GUI Usage

1. **Launch the application**
```bash
   java -jar build/libs/calendar-1.0.jar
```

2. **A default calendar opens automatically** - you can start using it immediately

3. **Create additional calendars** (optional):
    - Click "New Calendar"
    - Name: "Work", Timezone: "America/New_York"
    - Click "New Calendar" again
    - Name: "Personal", Timezone: "America/Los_Angeles"

4. **Create some events**:
    - Select May 5, 2025
    - Click "Create Event"
    - Subject: "Team Meeting"
    - Start: 10:00, End: 11:00
    - Click "Create"

    - Click "Create Event" again
    - Subject: "Daily Standup"
    - Check "Recurring event"
    - Check: Mon, Tue, Wed, Thu, Fri
    - Start: 09:00, End: 09:30
    - Repeat count: 20 times
    - Click "Create"

5. **Navigate the calendar**:
    - Click "Next >" to view June
    - Click "Today" to return to current month

6. **Edit an event**:
    - Select a date with events
    - Click "Edit Events"
    - Select event from dropdown
    - Change location to "Conference Room A"
    - For recurring events, choose edit scope
    - Click "Save Changes"

7. **Delete an event**:
    - Select the date
    - Click "Delete Event"
    - Choose the event
    - Choose deletion scope (for recurring events)
    - Click "Delete", then "Yes" to confirm

8. **Switch calendars**:
    - Click "Switch Calendar"
    - Select "Personal"
    - View and manage events in the Personal calendar
---

## Text Mode Command Reference

For complete text-based command reference (interactive and headless modes), all commands from the previous version remain fully supported:

### Calendar Management Commands
- `create calendar --name <name> --timezone <timezone>`
- `use calendar --name <name>`
- `edit calendar --name <name> --property <property> <value>`

### Event Commands
- `create event <subject> from <datetime> to <datetime>`
- `create event <subject> on <date>`
- `create event <subject> from <datetime> to <datetime> repeats <weekdays> for <count> times`
- `create event <subject> from <datetime> to <datetime> repeats <weekdays> until <date>`
- `edit event <property> <subject> from <datetime> to <datetime> with <value>`
- `edit events <property> <subject> from <datetime> with <value>`
- `edit series <property> <subject> from <datetime> with <value>`

### Query Commands
- `print events on <date>`
- `print events from <datetime> to <datetime>`
- `show status on <datetime>`

### Export Commands
- `export cal <filename>` (supports .csv and .ical formats)

### Copy Commands
- `copy event <subject> on <datetime> --target <calendar> to <datetime>`
- `copy events on <date> --target <calendar> to <date>`
- `copy events between <date> and <date> --target <calendar> to <date>`

For detailed command syntax and examples, refer to `res/commands.txt`.

---