## Changes to Design

### 1. **Updated CalendarManager Interface**
- **Files Modified**: `CalendarManager.java`, `SimpleCalendarManager.java`
- **Changes**:
    - Added `getAllCalendarNames()` method to support GUI calendar selection
    - Added delegation methods for event operations (addEvent, getEventsOn, removeEvent, etc.)
- **Justification**: The GUI needed to list available calendars and perform operations on the current calendar without directly accessing the Calendar object, maintaining encapsulation

### 2. **Fixed Method Signatures for Series Editing**
- **Files Modified**: `CalendarModel.java`, `Calendar.java`, `SimpleCalendar.java`, `CalendarModelAdapter.java`
- **Changes**:
    - `editEventsFrom()` now includes `end` parameter: `editEventsFrom(String subject, LocalDateTime start, LocalDateTime end, String property, Object value)`
    - `editAllEventsInSeries()` now includes `end` parameter: `editAllEventsInSeries(String subject, LocalDateTime start, LocalDateTime end, String property, Object value)`
- **Justification**: In the previous iteration, we discovered ambiguity when multiple events have the same subject and start time. The `end` parameter helps disambiguate events, improving reliability. The GUI implementation revealed this edge case when testing event editing functionality

### 3. **Added Series Deletion Methods**
- **Files Modified**: `CalendarModel.java`, `Calendar.java`, `SimpleCalendar.java`, `CalendarModelAdapter.java`, `CalendarManager.java`, `SimpleCalendarManager.java`
- **Changes**:
    - Added `removeEventFromSeries(Event event)` - removes event and all future occurrences
    - Added `removeAllEventsInSeries(Event event)` - removes all occurrences in series
- **Justification**: The GUI required different deletion scopes (single event, this and future, all in series). Previous design did not require delete functionality at all, we added this now, so that it is easier for testing in GUI.

### 4. **Improved RecurringEventSeries Generation Logic**
- **Files Modified**: `RecurringEventSeries.java`
- **Changes**:
    - Modified `generateEventsByCount()` to use `previousOrSame(DayOfWeek.MONDAY)` for week alignment
    - Fixed the week iteration logic to properly handle events spanning multiple weeks
- **Justification**: The previous implementation had a bug where events wouldn't generate correctly if the start date wasn't on one of the specified weekdays. The new approach aligns to week boundaries, ensuring all specified weekdays are properly considered

### 5. **Enhanced SimpleCalendar Event Editing**
- **Files Modified**: `SimpleCalendar.java`
- **Changes**:
    - Added `isPartOfSeriesEdit` parameter to `updateStartDateTime()` and `updateEndDateTime()`
    - Series edits now only modify time, not date (date remains fixed to each event's original date)
    - Single event edits can modify both date and time
- **Justification**: When editing a recurring series, users expect to change the time of day (e.g., move meeting from 2pm to 3pm) while keeping each occurrence on its original date. The previous design changed both date and time

### 6. **CalendarRunner Mode Detection**
- **Files Modified**: `CalendarRunner.java`
- **Changes**:
    - No arguments → GUI mode
    - `--mode interactive` → Interactive text mode
    - `--mode headless <file>` → Headless mode
- **Justification**: The assignment specified these three modes. The no-argument case defaulting to GUI allows users to double-click the JAR file for easy access
---

## Features Status

### Working Features 

**GUI Mode:**
- Create, edit, and switch between multiple calendars
- Edit calendar properties (name, timezone)
- Month view with calendar grid navigation
- Create single events (all-day and timed)
- Create recurring events with weekday selection and repeat count/until date
- Edit events with three scopes:
    - Single event only
    - This event and all future events in series
    - All events in series
- Delete events with three scopes (same as edit)
- View events for selected dates
- Visual indicators for dates with events
- Multi-day event display with continuation indicators
- Color-coded calendars
- Default calendar initialization

### Not working Features

**GUI Mode:**
- Event copying between calendars with timezone conversion
- Export to CSV and iCal formats

### Working Features

**Interactive Mode:**
- All commands from previous iteration
- Calendar management commands (create, edit, use)
- Event copying between calendars with timezone conversion
- Export to CSV and iCal formats

**Headless Mode:**
- Script file execution
- All commands supported in interactive mode
- Proper error handling and exit command validation

