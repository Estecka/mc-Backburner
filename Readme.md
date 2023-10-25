# Back-burner

A fully client-side to-do list, displayed on the side of the screen, and operated through commands.

Each world and server saves its to-do list separately.

## File locations

Backlogs are saved as JSON arrays.

For singleplayer worlds, they are saved at the root of the world's directory as `backlog.json`

For multiplayer, they are saved under `.minecraft/remote_backlogs/`, using the ip/address and port of the server as the file name. LAN worlds are handled the same way.

## Config File

The file `.minecraft/config/backburner.properties` let's you tweak the position and size of the backlog on screen.
Everything can be edited in-game by using **Cloth-Config** and **ModMenu**.

- `hud.x` and `hud.y` controls its position, relative to the top left corner of the screen.
- `hud.width` controls its base size, i.e the amount of text it can hold before wrapping.
- `hud.scale` is a multiplier, that downscales or upscales the backlog in relation to the game's global GUI-scale. Mostly relevant to players who use a large GUI-scale, and thus have room for downscaling.

## Commands

By default, all sub-commands use `/note` as a root. The name of the root can be changed in the config file if needed.
`<text>` arguments are greedy, and do not need to be quoted.

### Adding items
- `push <text>` Adds an item at the top.
- `queue <text>` Adds an item at the bottom.
- `insert <index> <text>` Adds an item at an arbitrary index.

### Removing items
- `pop` Removes the top-most item.
- `pop <index>` Alias for `remove`
- `shift` Removes the bottom-most item.
- `remove <index>` Removes the specified item.

### Editing
- `edit <index> <text>` Overwrites an existing item. Auto-completion will offer you the old value.
- `bump <index>` Moves an item one place upward.
- `bump <index> <offset>` Moves an item the given amount of places. Positive values move upward, negative values move downward.
- `move <from> <to>` Moves an item from one index to another.

### Hud-control
- `hide <bool>` Toogles the backlog hud On or Off. Can be called with no argument.

### Debug
These commands shouldn't ever need to be used for normal usage.
- `save` Forces saving the backlog to its file on the disk. Automatically called whenever you make any change to your backlog.
- `reload` Forces loading the backlog as is saved on-disk. Automatically called upon joining a world.

## Resource Packs

In addition to textures, resource packs can alter some minor aspects of the backlog HUD's layout, by modifying the corresponding texture's `.mcmeta` file.

- `basis`: { `width`, `height` } indicates the base size of the element on screen.  
For 16x-packs, this is strictly equal to the texture's size. For 32x-packs, this is half the texture's size. So on, so forth.
- `basis`: { `fill` } (_boolean_) sets whether the element should take as much horizontal space as allowed (true) or shrink to fit the text (false).
- `ninepatch`: { `top`, `bottom`, `left`, `right` } The margins of the sprite's  9-patch. I.e, the areas that won't be stretched to fit element's size.
- `padding`: { `top`, `bottom`, `left`, `right` } Defines some space that will be added around the element. Negative values can be used to  remove space, and make elements overlap.
- `textarea`: { `top`, `bottom`, `left`, `right` } defines margins inside the texture, which will not be filled with text.
- `text`: {...} defines the different colours of the text for that element, formatted as a `#aarrggbb` hex codes. The alpha component is mandatory.
	- `colour` is the text's main colour
	- `outline` defines a main outline colour.
	- `outerline` adds a thicker outline around the first outline. Requires the former to be defined.
- `text`: { `shadow` } (_boolean_) Whether the text should have a shadow. Incompatible with any outline.
