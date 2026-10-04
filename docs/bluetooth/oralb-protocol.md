# Oral-B BLE Protocol Notes

## Original Characteristic Reference

Source:
https://github.com/wise86-android/OralBlue_python/blob/15e1a03bcb3350574d438e4593bcff59608a77a7/Protocol.md
[OralBlue_python - Protocol.md](https://github.com/wise86-android/OralBlue_python/blob/15e1a03bcb3350574d438e4593bcff59608a77a7/Protocol.md)
```python
# Pulled from here:
https://github.com/wise86-android/OralBlue_python/blob/15e1a03bcb3350574d438e4593bcff59608a77a7/Protocol.md

# In Use
CHARACTERISTIC_BATTERY = "a0f0ff05-5047-4d53-8208-4f72616c2d42"
CHARACTERISTIC_MODEL = "a0f0ff02-5047-4d53-8208-4f72616c2d42"

# Not from above link:
CHARACTERISTIC_PRESSURE = "a0f0ff0b-5047-4d53-8208-4f72616c2d42"

# Known but not in use
CHARACTERISTIC_SECTOR = "a0f0ff09-5047-4d53-8208-4f72616c2d42"
CHARACTERISTIC_CONTROL = "a0f0ff21-5047-4d53-8208-4f72616c2d42"
CHARACTERISTIC_SECTOR_TIMER = "a0f0ff26-5047-4d53-8208-4f72616c2d42"
CHARACTERISTIC_SESSION_INFO = "a0f0ff29-5047-4d53-8208-4f72616c2d42"
CHARACTERISTIC_TOOTHBRUSH_ID = "a0f0ff01-5047-4d53-8208-4f72616c2d42"
CHARACTERISTIC_USER_ID = "a0f0ff03-5047-4d53-8208-4f72616c2d42"
CHARACTERISTIC_STATUS = "a0f0ff04-5047-4d53-8208-4f72616c2d42"
CHARACTERISTIC_BUTTON = "a0f0ff06-5047-4d53-8208-4f72616c2d42"
CHARACTERISTIC_MODE = "a0f0ff07-5047-4d53-8208-4f72616c2d42"
CHARACTERISTIC_BRUSHING_TIME = "a0f0ff08-5047-4d53-8208-4f72616c2d42"
CHARACTERISTIC_CURRENT_TIME = "a0f0ff22-5047-4d53-8208-4f72616c2d42"
CHARACTERISTIC_AVAILABLE_MODES = "a0f0ff25-5047-4d53-8208-4f72616c2d42"

# This seems to be giving positional data, but I have not quite nailed it down.
CHARACTERISTIC_POSITION = "a0f0ff0d-5047-4d53-8208-4f72616c2d42"

# Unknown
CHARACTERISTIC_UNKNOWN_1 = "a0f0ff84-5047-4d53-8208-4f72616c2d42"
CHARACTERISTIC_UNKNOWN_2 = "a0f0ff85-5047-4d53-8208-4f72616c2d42"
CHARACTERISTIC_UNKNOWN_3 = "a0f0ff83-5047-4d53-8208-4f72616c2d42"
CHARACTERISTIC_UNKNOWN_4 = "a0f0ff81-5047-4d53-8208-4f72616c2d42"
CHARACTERISTIC_UNKNOWN_5 = "a0f0ff82-5047-4d53-8208-4f72616c2d42"
# CHARACTERISTIC_UNKNOWN_6 = "a0f0ff0c-5047-4d53-8208-4f72616c2d42" # Failure when trying to grab
CHARACTERISTIC_UNKNOWN_7 = "a0f0ff0a-5047-4d53-8208-4f72616c2d42"
CHARACTERISTIC_UNKNOWN_8 = "a0f0ff2c-5047-4d53-8208-4f72616c2d42"
CHARACTERISTIC_UNKNOWN_9 = "a0f0ff2b-5047-4d53-8208-4f72616c2d42"
CHARACTERISTIC_UNKNOWN_10 = "a0f0ff23-5047-4d53-8208-4f72616c2d42"
CHARACTERISTIC_UNKNOWN_11 = "a0f0ff2d-5047-4d53-8208-4f72616c2d42"
CHARACTERISTIC_UNKNOWN_12 = "a0f0ff2a-5047-4d53-8208-4f72616c2d42"
```
## Characteristic Quick Reference

| Characteristic | Short UUID | Purpose |
|---|---|---|
| Toothbrush ID | FF01 | Toothbrush identifier |
| Model | FF02 | Toothbrush model |
| User ID | FF03 | User identifier |
| Status | FF04 | Toothbrush status |
| Battery | FF05 | Battery information |
| Button | FF06 | Button state/event |
| Mode | FF07 | Brushing mode |
| Brushing Time | FF08 | Brushing time |
| Sector | FF09 | Brushing sector |
| Unknown 7 | FF0A | Unknown |
| Pressure | FF0B | Pressure information |
| Position | FF0D | Appears to contain positional data |
| Control | FF21 | Control |
| Current Time | FF22 | Current time |
| Available Modes | FF25 | Available brushing modes |
| Sector Timer | FF26 | Sector timer |
| Session Info | FF29 | Session information |

## BrushBuddy GATT Characteristic Strategy

| Characteristic | Short UUID | Meaning | BrushBuddy Use | GATT Strategy |
|---|---|---|---|---|
| Model | FF02 | Toothbrush model | Identify the toothbrush/model | Read once |
| Status | FF04 | Toothbrush status (Idle, Run, Charge, etc.) | Detect whether brushing is running | Subscribe |
| Battery | FF05 | Battery level | Display/record battery percentage | Read / Subscribe |
| Button | FF06 | Button state/event | Detect button activity if needed | Optional |
| Mode | FF07 | Brushing mode | Determine selected brushing mode | Subscribe |
| Brushing Time | FF08 | Brushing duration `[minutes, seconds]` | Track session duration | Subscribe |
| Sector | FF09 | Current brushing sector | Future brushing coverage feature | Optional / Future |
| Pressure | FF0B | Brushing pressure | Detect excessive/normal pressure | Subscribe / Future UI |
| Position | FF0D | Positional/motion data | Not required for current MVP | Do not subscribe |
| Current Time | FF22 | Toothbrush current time | Time synchronization / session timestamps | Read once if needed |
| Available Modes | FF25 | Supported brushing modes | Determine capabilities of toothbrush | Read once / Future |
| Sector Timer | FF26 | Sector brushing timer | Future brushing coverage feature | Future |
| Session Info | FF29 | Stored brushing session information | Retrieve historical sessions | Future |

### Current MVP GATT Plan

Dynamic characteristics to subscribe to:

- **FF04 - Status**
- **FF05 - Battery**
- **FF07 - Mode**
- **FF08 - Brushing Time**
- **FF0B - Pressure**

Characteristics to read once when needed:

- **FF02 - Model**
- **FF22 - Current Time**
- **FF25 - Available Modes**

Characteristics not required for the current MVP:

- **FF06 - Button**
- **FF09 - Sector**
- **FF0D - Position**
- **FF26 - Sector Timer**
- **FF29 - Session Info**

The current implementation subscribes to FF04-FF0D. This will be changed to subscribe only to the characteristics required by the current BrushBuddy MVP:

- **FF04 - Status:** detect whether the toothbrush is running/brushing.
- **FF05 - Battery:** obtain the toothbrush battery level.
- **FF08 - Brushing Time:** track the duration of the brushing session.

Other characteristics will be kept as future options but will not be subscribed to for the current MVP.