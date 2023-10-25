# v1
## 1.0
- Initial Release

## 1.1
- Added the `edit` sub-command. The old value of the edited item can be auto-filled.
- `remove` no longer falls back to the closest item when the index is out of bounds.
- `hide` can be called with no arguments
- Fixed the icon offset when hiding the hud.

## 1.2
## 1.2.0
- The root command's name can be changed in a config file.
- The position, size and scale of the backlog HUD can be tweaked in the config file.
- Resource packs now have more controls over the HUD's appearance, including text colour, header text, and some minor aspects of the layout.
- Added built-in resource packs: "Questlog" and "Notebook".
- The previous theme was moved to the "Questlog" pack. The new default resources are more simplistic

## 1.2.1
- Fixed the "incompatible" warning on built-in resource packs in 1.20.2

## 1.3.0
- Added Cloth-Config screen with ModMenu integration
- Added an option to allow fractional GUI Scales
- Excluded image sources from the jar, dramatically reducing file size.
