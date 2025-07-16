# Sebxstt

<div align="center">

<img src="https://github.com/GarcesSebastian/NextInventory/blob/main/src/main/java/com/sebxstt/nextinventory/assets/logo.png?raw=true" width="150"/>

[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21.4-brightgreen.svg)](https://www.minecraft.net/)
[![Version](https://img.shields.io/badge/Version-1.0.0-blue.svg)](https://github.com/GarcesSebastian/Sebxstt)
[![PaperMC](https://img.shields.io/badge/API-PaperMC-yellow.svg)](https://papermc.io/)
[![Java](https://img.shields.io/badge/Java-21-red.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

**An advanced PaperMC plugin that implements checkpoint systems, teams, and collaborative tools**

</div>

---

## Index

1. [Main Features](#main-features)
2. [Installation](#installation)
3. [Configuration](#configuration)
4. [Team System](#team-system)
   - [Roles and Functionality](#roles-and-functionality)
   - [Team Chat](#team-chat)
   - [Team Warps](#team-warps)
5. [Graphical Interface (GUI)](#graphical-interface-gui)
6. [Commands](#commands)
   - [Checkpoint Commands](#checkpoint-commands)
   - [Team Commands](#team-commands)
   - [Warp Commands](#warp-commands)
   - [Player Commands](#player-commands)
7. [Data Storage](#data-storage)
8. [Roadmap and Development](#roadmap-and-development)

---

## Main Features

- **Checkpoint System**: Create personal teleport points with custom names.
- **Team System**: Create and manage teams with different roles and distinctive colors.
- **Team Private Chat**: ✅ Communicate exclusively with your team members through a private channel.
- **Team Warps**: ✅ Establish and share teleport points for the entire team.
- **Team Invitations**: Invitation system with acceptance/rejection by players.
- **Return to Death Point**: Command to return to your last death location (with configurable cooldown).
- **Optimized Storage**: All information is saved in a single JSON file.
- **EXP Sharing System**: Automatic experience distribution among nearby team members.
- **Interactive Graphical Interface**: 🆕 Visual menu for team management using NextInventory.
- **Group Teleportation**: ✅ Teleport all members or members with a specific role to team warps.

---

## Installation

1. Download the `Sebxstt-1.0.0.jar` file from the releases page.
2. Place the `.jar` file in the `plugins/` folder of your PaperMC server.
3. Restart the server. The `plugins/Sebxstt/` folder will be automatically created.
4. Verify that the NextInventory library is correctly integrated for the GUI system.

---

## Configuration

The plugin uses a simple `config.yml` file with the following parameters:
```yaml
CooldownLastDeathCheckPoint: 1  # Cooldown time in minutes for the last death point return command
MaxCheckPoints: 3               # Maximum number of checkpoints a player can have
fileDataSaved: "data"           # Name of the data file
AutoSaveTime: 5                 # Auto-save interval in minutes
```

All persistent data is saved in the JSON file specified in the configuration.

---

## Team System

The plugin implements a team system that allows players to form teams with different distinctive colors. Teams have a role system to manage permissions and features like private chat, shared warps, and member management.

### Roles and Functionality

There are 4 roles in the team system:

- **LEADER**: Team owner with complete control over all functionality.
- **OFFICER**: Role with extensive permissions to manage the team and its members.
- **MEMBER**: Regular member who can use the basic team functionality.
- **GUEST**: Member with limited permissions, mainly viewing permissions.

### Permissions by Role

| Permission | LEADER | OFFICER | MEMBER | GUEST |
|---------|:------:|:-------:|:-------:|:----------:|
| Manage members (invite/kick) | ✅ | ✅ | ❌ | ❌ |
| Change member roles | ✅ | ✅ | ❌ | ❌ |
| Disband team | ✅ | ❌ | ❌ | ❌ |
| Manage warps (create/delete) | ✅ | ✅ | ❌ | ❌ |
| Use warps | ✅ | ✅ | ✅ | ✅ |
| Teleport all members | ✅ | ✅ | ❌ | ❌ |
| Access storage | ✅ | ✅ | ✅ | ✅ |
| Use team chat | ✅ | ✅ | ✅ | ✅ |

### Team Chat

The plugin incorporates a private team chat system that allows members to communicate exclusively with each other:

- It can be activated/deactivated with the `/gchat on|off` command
- Messages are visible only to team members
- The message format includes the team name and color
- Team chat status persists between sessions

### Team Warps

Teams can establish shared teleport points:

- Each team can create multiple warps in different locations
- Warps are accessible to all team members
- Members with advanced permissions can teleport all team members to a warp
- It's also possible to teleport only members with a specific role

---

## Commands

### Checkpoint Commands

| Command | Alias | Description | Permissions |
|---------|-------|-------------|----------|
| `/checkpoint save <name>` | `/cp save` | Saves a checkpoint at your current location | `sebxstt.command.checkpoint` |
| `/checkpoint delete <name>` | `/cp delete` | Deletes a saved checkpoint | `sebxstt.command.checkpoint` |
| `/checkpoint tp <name>` | `/cp tp` | Teleports you to a saved checkpoint | `sebxstt.command.checkpoint` |
| `/checkpoint list` | `/cp list` | Shows all your saved checkpoints | `sebxstt.command.checkpoint` |

### Team Commands

| Command | Alias | Description | Requirements |
|---------|-------|-------------|------------|
| `/gcreate <color> <name>` | `/gc` | Creates a new team with color and name | `sebxstt.command.group` |
| `/ginfo` | `/gi` | Shows current team information | Team member |
| `/gleave` | `/lv` | Leave your current team | Not LEADER |
| `/ginvite <role> <player>` | `/iv` | Invites a player with specific role | LEADER/OFFICER |
| `/gkick <player>` | `/gk` | Kicks a player from the team | LEADER/OFFICER |
| `/gdisband` | `/gd` | Completely dissolves the team | LEADER only |
| `/grole <role> <player>` | `/gr` | Changes a member's role | LEADER/OFFICER |
| `/gstorage` | `/st` | Opens the team's shared storage | Team member |
| `/gchat <on\|off>` | `/ch` | Activates/deactivates team chat | Team member |

### Warp Commands

| Command | Description | Requirements |
|---------|-------------|------------|
| `/gwarp create <name>` | Creates a warp at your current location | LEADER/OFFICER |
| `/gwarp delete <name>` | Deletes an existing warp | LEADER/OFFICER |
| `/gwarp list` | Shows all team warps | Team member |
| `/gwarp tp <name>` | Teleports you to the specified warp | Team member |
| `/gwarp all <name>` | Teleports all members to the warp | LEADER/OFFICER |
| `/gwarp post <role> <name>` | Teleports members with a specific role | LEADER/OFFICER |

### Player Commands

| Command | Alias | Description | Permissions |
|---------|-------|-------------|----------|
| `/stats` | `/est` | Shows player statistics | `sebxstt.command.player` |
| `/clearteams` | - | Clears player teams | `sebxstt.command.player` |
| `/return` | - | Teleports you to your last death point | `sebxstt.command.player` |
| `/invitations` | `/inv` | Manages pending team invitations | `sebxstt.command.player` |

---

## Graphical Interface (GUI)

The plugin incorporates an advanced interactive graphical interface system using the NextInventory library, developed specifically for this project:

### GUI System

- **Team Menu**: Quick visual access to all team functions through custom inventories
- **Visual Member Management**: View and manage team members with a click
- **Page Navigation**: Pagination system to organize large amounts of information
- **Interactive Elements**: Buttons with visual effects and animations to improve user experience
- **Animated Icons**: Elements that alternate or cycle between materials to highlight important actions

### Interface Features

- **Pagination**: Navigate between different menu sections (members, warps, settings)
- **Animations**: Buttons with visual effects like material alternation
- **Callbacks**: Custom events for each interaction
- **Modular Design**: Expandable system to add new functionality

To open the team management menu, use the `/gteam` command or its alias `/gt`.

## Data Storage

All plugin information is stored in a single JSON file defined in the configuration (by default `data.json`). This file contains:

- Player information and settings
- Saved checkpoints
- Created teams and their members
- Member roles in each team
- Team warps
- Team chat configuration

The plugin automatically saves data at the following times:

- When a player disconnects
- When the server shuts down
- Periodically according to the interval defined in the configuration (AutoSaveTime)

## Roadmap and Development

The plugin is under active development with the following features planned for future versions:

| Feature | Status |
|--------------|--------|
| Team Private Chat | ✅ Implemented |
| Team Warps | ✅ Implemented |
| Interactive GUI Menu | 🔄 In development |
| Voting System | 📝 Planned |
| Team Events | 📝 Planned |

### Technologies Used

- **[NextInventory](https://github.com/GarcesSebastian/NextInventory)**: Custom library for creating interactive graphical interfaces
- **Paper API**: For complete integration with PaperMC servers
- **MiniMessage**: For advanced text and message formatting

---

## Contribute

If you find any issues or have suggestions to improve the plugin, please open an issue in the project repository.

## License

This project is licensed under the MIT License. See the LICENSE file for more details.

---

<div align="center">

![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)
![PaperMC](https://img.shields.io/badge/API-PaperMC-yellow.svg)
![Minecraft](https://img.shields.io/badge/Minecraft-1.21.4-green.svg)
![Java](https://img.shields.io/badge/Java-21-red.svg)

&copy; 2025 Sebxstt - Developed for PaperMC servers

</div>
