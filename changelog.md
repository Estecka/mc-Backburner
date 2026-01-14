# v1
## 1.0
- Initial Release

## 1.1
- Added the `edit` sub-command. The old value of the edited item can be auto-filled.
- `remove` no longer falls back to the closest item when the index is out of bounds.
- `hide` can be called with no arguments
- Fixed the icon offset when hiding the hud.

## 1.2
### 1.2.0
- The root command's name can be changed in a config file.
- The position, size and scale of the backlog HUD can be tweaked in the config file.
- Resource packs now have more controls over the HUD's appearance, including text colour, header text, and some minor aspects of the layout.
- Added built-in resource packs: "Questlog" and "Notebook".
- The previous theme was moved to the "Questlog" pack. The new default resources are more simplistic
### 1.2.1
- Fixed the "incompatible" warning on built-in resource packs in MC 1.20.2

## 1.3
### 1.3.0
- Added Cloth-Config screen with ModMenu integration
- Added an option to allow fractional GUI Scales
- Excluded image sources from the jar, dramatically reducing file size.
### 1.3.1
- Fixed the "incompatible" warning on built-in resource packs in MC 1.20.3
### 1.3.2
- Fixed a crash that could occur upon loading a replay in ReplayMod.
### 1.3.3
- Updated for MC 1.20.5
### 1.3.4
- Updated for MC 1.21
### 1.3.5
- Fixed incompatibility with Fabric-Loader 0.16

## 1.4
### 1.4.0
- Added an option to change which side of the screen the hud is anchored to.
- Added options to disable some command feedbacks.
- Added `first` and `last` as supported values for indices.
- Added `add` as an alias to `insert`.
- Added the commmand `clear`
### 1.4.1
- Updated for MC 1.21.2
### 1.4.2
- Updated for MC 1.21.4
- Fixed a minor inconsistency in resource mcmeta default values.
## 1.5
### 1.5.0
- Added a configurable `write` command
- The autocomplete for `remove`/`pop` will now show the entry's text.
### 1.5.1
- Marked embedded resource packs as compatible with all upcoming versions of minecraft.
## 1.6
- Backlog now accepts SNBT texts as entries.
