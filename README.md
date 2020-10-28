# Paradoxos Mod Manager (Rework)
Paradoxos Mod Manager is a java application which can be used to manage your mods in recent Paradox Interactive’s games

I develop this app because i often have lots of mods and games with differents list of mods activated on Stellaris, and enable/disable each mods of a savegame before i launch my game was very boring…

## Important
This repository is for the Java 11 port of [Java 8 Paradoxos Mod Manager](https://github.com/ThibautSF/ParadoxosModManager) and all future updates

## Game supported
* Crusader Kings II
* Europa Universalis IV
* Stellaris
* Hearts of Iron 4
* Imperator Rome
* Crusader Kings III (if you replace [this file](https://raw.githubusercontent.com/ThibautSF/ParadoxosModManagerRework/master/ParadoxosGameModManager/supported_games.json) over PMM, tips : use `ctrl+S` (or equivalent shortcut on MacOS) to download)

## Requirements
* OS : Windows, Linux, MacOS
* Supported game(s) and mods installed (*not really necessary but without, this tool won't be really useful*)

## Usefull links
### Download
* Latest : https://github.com/ThibautSF/ParadoxosModManagerRework/releases/latest
* Indev : https://github.com/ThibautSF/ParadoxosModManagerRework/releases/tag/indev
* All releases : https://github.com/ThibautSF/ParadoxosModManagerRework/releases

For version previous 0.8.0 : [Java 8 Paradoxos Mod Manager](https://github.com/ThibautSF/ParadoxosModManager)

### Documentation
The complete documentation is available on this [Google Doc (https://drive.google.com/open?id=1wThmbZIEGWzDO3rp8-zzJumebXDBE4-q6L6GnzVKmAY)](https://drive.google.com/open?id=1wThmbZIEGWzDO3rp8-zzJumebXDBE4-q6L6GnzVKmAY), the last version (when the app zip was released) of this documentation in pdf is always included in each app archive (for offline read).

### Version Log
On this [Google Doc (https://drive.google.com/open?id=1DFCgmSFUUZ2IRY-ON1bOVZki9LPd-FSTHacR7i2ibUA)](https://drive.google.com/open?id=1DFCgmSFUUZ2IRY-ON1bOVZki9LPd-FSTHacR7i2ibUA)

## How to use it
* Download and extract
* Run `LaunchParadoxosModManager.exe` or `LaunchParadoxosModManager.bat` on windows, or `LaunchParadoxosModManager.sh` on UNIX (also `LaunchParadoxosModManager.command` on macOS)
* Follow the [Documentation](https://drive.google.com/open?id=1wThmbZIEGWzDO3rp8-zzJumebXDBE4-q6L6GnzVKmAY) part III for detailled procedure (a pdf version is in the archive)

## Build latest from source
1. Install OpenJDK 11 and set `JAVA_HOME`
   - I use [AdoptOpenJDK 11 (HotSpot)](https://adoptopenjdk.net/): [installation instructions (installers should set JAVA_HOME)](https://adoptopenjdk.net/installation.html?variant=openjdk11#)
2. Clone repo (download zip and extract or `git clone https://github.com/ThibautSF/ParadoxosModManagerRework.git`)
3. Go with terminal to `/path/to/ParadoxosModManagerRework/ParadoxosGameModManager/`
4. Run gradle
   - Linux/macOS: `./gradlew run`
   - Windows: `gradlew run`
5. If you want to build
   - Linux/macOS: `./gradlew dist`
   - Windows: `gradlew dist`
   - Get the zip `build/distributions/ParadoxosModManager-<distrib>.zip` (after extraction launch PMM with `bin\LaunchParadoxosModManager.bat` (for Windows) or `./bin/LaunchParadoxosModManager` (for Linux/macOS)

See [OpenJFX Documentation](https://openjfx.io/openjfx-docs/) for more details.

## Additional information
### Team
* [SIMON-FINE Thibaut (alias Bisougai)](https://github.com/ThibautSF) : Author
* [GROSJEAN Nicolas (alias Mouchi)](https://github.com/NicolasGrosjean) : Contributor

### Contact (suggestions, reports...)
**For a bug report : Add all informations you can add (OS, java version, image(s), file “DebugLog.txt”...)** 
* Use one of the presentation thread on the paradoxplazza forum
* Open an [issue thread](https://github.com/ThibautSF/ParadoxosModManagerRework/issues)
