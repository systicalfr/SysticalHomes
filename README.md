# SysticalHomes

A GUI-based homes plugin for Paper 26.2

- `/home` or `/homes` — opens the GUI (top row = orange beds for your homes,
  row below = red dye to delete — click twice within 5 seconds to confirm)
- `/home <name>` — teleport directly to a named home
- `/sethome [name]` — set a home via command
- `/delhome <name>` — delete a home via command

- Each player can only set up to 5 homes.

Clicking an **empty (gray) bed** in the GUI sets a home at your current location.
Clicking a **filled (orange) bed**, or using `/home <name>`, starts a 3-second
teleport countdown: an orange action bar message counts "3... 2... 1..." with
a note block ping each second, then teleports you.

## How to build the .jar (2 commands, ~15 seconds)

You need Java 25+ and Maven installed. Most machines with Java already have
Maven, or you can install it in one line:
- Windows: `winget install Maven.Maven` (or download from maven.apache.org)
- Mac: `brew install maven`
- Linux: `sudo apt install maven`

Then, in this folder, run:

```
mvn package
```

Your finished plugin will appear at `target/HomesGUI.jar`. Drop that into
your server's `/plugins` folder and restart.

## If your server isn't on 26.2

Open `pom.xml` and swap the `paper-api` version near the bottom for the one
matching your server (Paper 26.x uses a `26.x.build.NNN-stable` scheme, no
more `-R0.1-SNAPSHOT`). Also update `api-version` in
`src/main/resources/plugin.yml` to match (e.g. `'26.1'`, `'26.2'`, etc.).
Browse available builds here:
https://repo.papermc.io/#browse/browse:maven-public:io%2Fpapermc%2Fpaper%2Fpaper-api
