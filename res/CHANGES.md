# DASHBOARD STATUS
- Analytics implemented in model: YES
- Text command for dashboard: YES (`show calendar dashboard from <date> to <date>`)
- Dashboard exposed through GUI: YES
- Known limitations: None observed beyond existing behavior of the provider code

## Overview

The calendar analytics feature was added in a way that respects the existing MVC + Command design of 
the provider's codebase. Instead of pushing analytics logic into the controller or the views, I 
introduced a small analytics service API in the **model layer** and reused the existing adapter and 
command infrastructure to expose it through both the **text interface** and the **GUI**.

At a high level:

- The **model** now exposes a `generateAnalytics(startDate, endDate)` method via `CalendarModel`, 
implemented in `SimpleCalendar` and delegated through `CalendarModelAdapter`.
- A new immutable value object, `CalendarAnalyticsSummary`, represents all computed metrics for a 
given date interval.
- The **text interface** gains a new command `show calendar dashboard from <date> to <date>` wired 
through the existing command parsing infrastructure and implemented as `ShowDashboardCommand`.
- The **GUI** exposes the same analytics via the `Features` and `CalendarGuiView` abstractions, 
implemented in `GuiController` and `SwingCalendarView` using a new "Show Dashboard" button and dialog.

This keeps analytics logic centralized in the model and reuses the existing patterns for user interaction.

## Model Layer Changes

### 1. `CalendarAnalyticsSummary` (new class)

I introduced a new immutable value type:

- **File:** `calendar/model/CalendarAnalyticsSummary.java`
- **Responsibility:** Encapsulate all analytics metrics for a calendar over a given date interval.

It is built using a nested `Builder` class and contains:

- `totalEvents`
- `eventsBySubject` (subject -> count)
- `eventsByWeekday` (`DayOfWeek` -> count)
- `eventsByWeekIndex` (week index within interval -> count)
- `eventsByMonth` (`YearMonth` -> count)
- `averageEventsPerDay`
- `busiestDay` and `leastBusyDay` (`LocalDate`)
- `onlineEventsCount` and `offlineEventsCount`

The builder pattern avoids long parameter lists and makes the object easy to construct without 
cluttering controller or view code.

### 2. `CalendarModel` interface

I extended `CalendarModel` with a single new method:

- `CalendarAnalyticsSummary generateAnalytics(LocalDate startDate, LocalDate endDate);`

This gives a single, cohesive entry point for all dashboard metrics. All consumers (text controller, 
GUI controller) use this method instead of reimplementing analytics in multiple places.

### 3. `SimpleCalendar` implementation

`SimpleCalendar` now implements `generateAnalytics`:

- Computes all required metrics over events in the inclusive date range `[startDate, endDate]`.
- Uses existing event retrieval (`getEventsInRange(...)`) and then groups events by:
    - subject
    - weekday (`DayOfWeek`)
    - "week index" within the selected interval (computed via `ChronoUnit.WEEKS.between(startDate, eventDate)` + 1)
    - month (`YearMonth`)
    - per-day counts to determine busiest and least busy day
- Identifies "online" events by checking if the event location equals `"online"` (case-insensitive, 
- ignoring surrounding spaces); everything else is considered "not online".

The method returns a `CalendarAnalyticsSummary` constructed through the builder, keeping the core 
analytics logic firmly in the model.

### 4. `CalendarModelAdapter`

`CalendarModelAdapter` was updated to delegate:

- `generateAnalytics(startDate, endDate)` simply calls the same method on the currently selected 
calendar (`getCurrentCalendar()`).

This is consistent with its existing role as an adapter over `CalendarManager` and hides the details 
of which calendar is active from the controllers.

## Text Interface Changes

### 5. `ShowDashboardCommand` (new command)

- **File:** `calendar/controller/commands/ShowDashboardCommand.java`

This command:

1. Stores the inclusive start and end `LocalDate`s.
2. Calls `model.generateAnalytics(startDate, endDate)`.
3. Formats all metrics from `CalendarAnalyticsSummary` into a readable multi-line string:
    - Total events
    - Average events per day
    - Events by subject
    - Events by weekday
    - Events by week index
    - Events by month
    - Busiest day and least busy day
    - Online vs offline event counts and percentages
4. Uses `CalendarView.displayMessage(...)` so it naturally works with `CalendarTextView`.

By keeping formatting in the command and data in the model, the `ShowDashboardCommand` fits nicely 
with the existing command pattern.

### 6. `QueryParser` (text command parsing)

`QueryParser` (within `calendar.controller.parser`) was extended to recognize:

```text
show calendar dashboard from <YYYY-MM-DD> to <YYYY-MM-DD>
```

Concretely:
- Checks commandLine.startsWith("show calendar dashboard from ").
- Uses a regex to capture the two dates and validates them.
- Parses them into LocalDate objects.
- Creates a ShowDashboardCommand(startDate, endDate) instance.
- This follows the same style as existing query commands and keeps the parsing logic localized.

## GUI Changes
### 7. Features interface

Features was extended with: `void showDashboard(LocalDate start, LocalDate end);`

This gives the GUI view a type-safe way to ask the controller to show analytics, consistent with the 
existing pattern for creating, editing, and deleting events.

### 8. CalendarGuiView interface

CalendarGuiView was extended with:

`void displayDashboard(CalendarAnalyticsSummary summary,
                      LocalDate startDate,
                      LocalDate endDate);`

This keeps the view responsible for deciding how to render the dashboard data (e.g., dialog, panel, 
labels), while the controller only orchestrates between model and view.

### 9. GuiController implementation

GuiController implements the new Features.showDashboard method:

- Validates that dates are non-null and end is not before start.
- Creates a CalendarModelAdapter from the shared CalendarManager.
- Calls `generateAnalytics(start, end)` on the model.
- Passes the resulting CalendarAnalyticsSummary to the view via
  `view.displayDashboard(summary, start, end)`.
- Uses `view.displayError(...)` for invalid ranges or unexpected problems.

This mirrors how other features are handled (controller coordinates, model computes, view displays) 
and does not mix presentation logic into the controller.

### 10. SwingCalendarView implementation

SwingCalendarView was updated in two ways:
- New "Show Dashboard" button in the side panel:
- A JButton labeled "Show Dashboard".

On click, it prompts the user twice for start and end dates (YYYY-MM-DD) via JOptionPane.
It parses the dates to LocalDate and calls `featuresListener.showDashboard(start, end)`.

Implementation of `displayDashboard(...)`:
- Assembles a multi-line textual summary of the analytics fields from CalendarAnalyticsSummary.
- Displays that summary in a `JOptionPane.showMessageDialog(...)` with the title "Calendar Dashboard".

This approach reuses the existing Swing style and keeps the dashboard UI minimal but clear, without 
introducing new complex layouts or reinforcing any code smells such as long, overly coupled methods 
in the controller.