# ![logo](src/main/resources/icons/icon_64x64.png) Changelist Sorter
> Automated changelist organizing for IntelliJ-based IDEs

## About
This IntelliJ plugin helps you organize your changelists with a single click.
Define the changelists you want and assign files to them via simple wildcard patterns,
either on demand or automatically as files are modified.

Requires IntelliJ IDEA (or another IntelliJ-based IDE) **2025.2 or newer**.

## Fork notice

**Changelist Sorter is a fork and continuation of
[4ch1m/ChangelistOrganizer](https://github.com/4ch1m/ChangelistOrganizer)
by [Achim Seufert](https://www.achimonline.de).**

All credit for the original design and implementation goes to him. This fork exists to keep
the plugin working on current IntelliJ Platform releases and is maintained independently at
[AFA-Simoes/changelist-sorter](https://github.com/AFA-Simoes/changelist-sorter).

It is published as a **separate plugin** with its own ID and name — it is not an update to,
and is not endorsed by, the original. The original plugin remains available as
[Changelist Organizer](https://plugins.jetbrains.com/plugin/9216) on the JetBrains Marketplace.

Because both plugins store their settings in the same project file, existing Changelist
Organizer rules are picked up automatically when you switch. Do not run both plugins at the
same time.

##  Screenshots

> ![screenshot1](screenshots/settings.png)

> ![screenshot2](screenshots/changelist-before.png)

> ![screenshot3](screenshots/changelist-after.png)

## Installation

Use the IDE's built-in plugin system:

* `File` --> `Settings` --> `Plugins` --> `Marketplace`
* search for "Changelist Sorter"
* click the `Install`-button

Alternatively, download a release archive from the
[Releases page](https://github.com/AFA-Simoes/changelist-sorter/releases) and install it via
`File` --> `Settings` --> `Plugins` --> `⚙` --> `Install Plugin from Disk...`.

## Development

Requires a JDK 21 toolchain.

```bash
./gradlew build         # compile and run the unit tests
./gradlew runIde        # launch a sandbox IDE with the plugin installed
./gradlew verifyPlugin  # check binary compatibility against recommended IDE releases
./gradlew buildPlugin   # produce the installable ZIP in build/distributions
```

## License

MIT — see the [LICENSE](LICENSE) file. The original copyright notice is retained in full,
as required; this fork is distributed under the same terms.
