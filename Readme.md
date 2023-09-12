# The Back-burner

## Overview
A fully client-side to-do list, displayed on the side of the screen, and operated through commands.

Each world and server has its own to-do list.

## Commands

All sub-commands use `/note` as a root. 
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

## File locations

Backlogs are saved as JSON arrays.

For singleplayer worlds, they are saved at the root of the world's directory as `backlog.json`

For multiplayer, they are saved under `.minecraft/remote_backlogs/`, using the ip/address and port of the server as the file name. LAN worlds are handled the same way.
