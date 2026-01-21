# AdventureBGS Unit Testing Plan

This document outlines a comprehensive plan for unit testing the AdventureBGS plugin. The goal is to ensure code quality, prevent regressions, and improve maintainability by testing each component in isolation.

## General Approach

### Testing Strategy: Simulation over Mocking

-   **Environment:** Use MockBukkit as the primary provider for all Bukkit/Spigot objects. Avoid manual Mockito mocks for Player, Server, or Plugin instances when MockBukkit can provide them.
-   **Unit Isolation:** Use Mockito exclusively for internal service classes and utility objects that do not depend on the Bukkit API.
-   **Zero-Lenience Policy:** Avoid `lenient()` stubbing. If a stub is unused, remove it. This ensures the test accurately reflects the code's execution path.
-   **Dynamic Injection:** Leverage Dynamic Injection to swap "Real" services for "Mockito" stubs during tests, while keeping the Plugin object as a MockBukkit controlled entity.

## Further Guidelines for Effective Unit Testing:

### Constructor-based Dependency Injection

-   All dependencies for classes under test (including `AdventureBGS` itself if tested via MockBukkit) should be provided via their constructors. This allows for easy swapping of real implementations with Mockito stubs (for internal service classes) or MockBukkit entities (for Bukkit API components) during testing.
-   When MockBukkit loads the `AdventureBGS` plugin, the resulting `AdventureBGS` instance is treated as a "real" object within the simulated environment. If `AdventureBGS` has its own internal dependencies that need to be mocked for a specific test, ensure `AdventureBGS` itself uses constructor injection to allow these dependencies to be provided as Mockito stubs during the test setup.

### Aligning Test Expectations with Actual Code Behavior and API Contracts

-   Ensure that test expectations (e.g., method calls, message content) precisely match the actual behavior of the code under test, especially when interacting with the Bukkit API.
-   Be aware of external API contracts (like Bukkit's) and design tests to either adhere to them or explicitly handle expected behaviors (e.g., exceptions for invalid input).


---

## Test Plan by File

### `AdventureBGS.java`

The main plugin class is difficult to unit test as it handles lifecycle and coordinates other components. Its testing is better suited for integration tests.

- **Testing Strategy:** Limited to no unit tests. Focus on integration testing.

### `command/AdventureCommand.java`

- **Dependencies:** `AdventureBGS`, `PDCManager`, `RotationManager`, `GuiManager`, `CommandSender`, `Command`.
- **Testing Strategy:** Test the `onCommand` method by simulating various command inputs and sender types.
- **Test Cases:**
    1.  **Non-Player Sender:**
        - Verify that the command fails gracefully when executed by the console.
    2.  **`/bgs` command (no arguments):**
        - Verify that the main GUI is opened for the player (`guiManager.openAdventureGUI`).
    3.  **`/bgs reload` command:**
        - **With permission (`adventure.admin`):**
            - Verify that `plugin.reloadPluginConfig()` is called.
            - Verify that a success message is sent to the player.
        - **Without permission:**
            - Verify that no reload methods are called.
            - Verify that an error/permission message is sent.
    4.  **`/bgs version` command:**
        - **With permission (`adventure.admin`):**
            - For a specific world, verify that `pdcManager.incrementWorldVersion` is called.
            - Verify success message is sent.
        - **Without permission:**
            - Verify `pdcManager.incrementWorldVersion` is not called.
            - Verify error message is sent.
        - **With invalid arguments (e.g., no world name):**
            - Verify an error message about incorrect usage is sent.
    5.  **Unknown subcommand:**
        - Verify that a "command not found" or help message is sent to the player.

### `listener/JoinListener.java`

- **Dependencies:** `PDCManager`, `RotationManager`, `PenaltyManager`, `PlayerJoinEvent`.
- **Testing Strategy:** Simulate `PlayerJoinEvent` and verify interactions with manager classes based on the player's world and data.
- **Test Cases:**
    1.  **Player joins a non-adventure world:**
        - Mock `rotationManager.getTrackByWorld` to return `null`.
        - Verify that no other methods on `pdcManager` or `penaltyManager` are called.
    2.  **Player joins an adventure world (up-to-date):**
        - Mock `rotationManager.getTrackByWorld` to return a valid track.
        - Mock `pdcManager.isPlayerOutdated` to return `false`.
        - Verify that `pdcManager.syncPlayerToWorld` is called.
        - Verify that `penaltyManager.penalizePlayer` is **not** called.
    3.  **Player joins an adventure world (outdated):**
        - Mock `rotationManager.getTrackByWorld` to return a valid track.
        - Mock `pdcManager.isPlayerOutdated` to return `true`.
        - Verify that `penaltyManager.penalizePlayer` is called.
        - Verify that `pdcManager.syncPlayerToWorld` is also called.
    4.  **Event handling with null player or world:**
        - Verify robust handling if the event provides null values, preventing `NullPointerException`.

### `manager/DeathManager.java`

- **Dependencies:** `AdventureBGS` (for logging/plugin context).
- **Testing Strategy:** Test the logic of marking, unmarking, and checking players. This class appears to be a simple in-memory store.
- **Test Cases:**
    1.  **Mark a player:**
        - Call `markPlayer` with a UUID.
        - Verify `isMarked` returns `true` for that UUID.
    2.  **Unmark a player:**
        - First, mark a player.
        - Call `unmarkPlayer` with the same UUID.
        - Verify `isMarked` returns `false`.
    3.  **Check a non-marked player:**
        - Verify `isMarked` returns `false` for a UUID that was never marked.
    4.  **Multiple players:**
        - Mark several players and verify the state of each is tracked correctly.

### `manager/PDCManager.java`

- **Dependencies:** `AdventureBGS`, `PersistentDataContainer` (mocked from `Player` and `World`).
- **Testing Strategy:** This is a critical class. Test all interactions with the `PersistentDataContainer` to ensure data is read, written, and compared correctly.
- **Test Cases:**
    1.  **`getWorldVersion`:**
        - **World has version:** Mock `worldPDC.get` to return a version number. Verify the method returns that number.
        - **World has no version:** Mock `worldPDC.has` to return `false`. Verify the method returns `0`.
    2.  **`incrementWorldVersion`:**
        - **From 0:** Verify `worldPDC.set` is called with the value `1`.
        - **From > 0:** Get an existing version (e.g., 5), and verify `worldPDC.set` is called with `6`.
    3.  **`getPlayerVersionForWorld`:**
        - **Player has version:** Mock `playerPDC.get` to return a version. Verify the correct version is returned.
        - **Player has no version:** Mock `playerPDC.has` to be `false`. Verify `0` is returned.
        - **NamespacedKey:** Verify the `NamespacedKey` used is correct for the given world.
    4.  **`syncPlayerToWorld`:**
        - Verify that the player's PDC is updated with the world's current version.
    5.  **`isPlayerOutdated`:**
        - **Outdated:** Player version < world version. Verify returns `true`.
        - **Up-to-date:** Player version == world version. Verify returns `false`.
        - **Ahead (edge case):** Player version > world version. Verify returns `false`.

### `manager/PenaltyManager.java`

- **Dependencies:** `AdventureBGS`, `PDCManager`, `RotationManager`, `BroadcastTask`, `AdventureSettings`.
- **Testing Strategy:** Test the logic that decides if and how a player is penalized.
- **Test Cases:**
    1.  **`penalizePlayer`:**
        - **Player online:**
            - Mock `Bukkit.getPlayer(uuid)` to return a mock player.
            - Verify `applyPenalty` is called with the player's name.
        - **Player offline:**
            - Mock `Bukkit.getPlayer(uuid)` to return `null`.
            - Verify `applyPenalty` is **not** called.
    2.  **`applyPenalty`:**
        - Mock `settings` to provide a penalty message and sound.
        - Verify that `player.sendMessage` and `player.playSound` are called with the correct formatted message and sound from settings.

### `util/MessageUtil.java`

- **Dependencies:** None.
- **Testing Strategy:** Pure utility class. Test the string manipulation methods.
- **Test Cases:**
    1.  **`color` method:**
        - **With color codes:** Verify `&` is translated to `§`.
        - **Without color codes:** Verify the string is unchanged.
        - **With null input:** Verify it returns an empty string or handles it gracefully.
    2.  **`format` method:**
        - Test with various placeholders and values to ensure correct replacement.
        - Test time formatting logic (e.g., `(mm:ss)`).

---
This initial plan covers the most critical components. The remaining files will be analyzed and added in subsequent phases. This provides a solid foundation to start writing effective unit tests.

### `listener/BroadcastListener.java`

- **Dependencies:** `RotationManager`, `BroadcastTask`, `PlayerChangedWorldEvent`.
- **Testing Strategy:** Simulate a `PlayerChangedWorldEvent` and verify that the `BroadcastTask` is interacted with correctly based on the world the player is entering.
- **Test Cases:**
    1.  **Player enters a world that is the start of a new map cycle:**
        - Mock a `RotationTrack` where `isNewCycle()` returns `true` for the destination world.
        - Mock `rotationManager.getTrackByWorld()` to return this track.
        - Verify that `broadcastTask.run()` is called.
    2.  **Player enters a world that is *not* the start of a new cycle:**
        - Mock a `RotationTrack` where `isNewCycle()` returns `false`.
        - Verify that `broadcastTask.run()` is **not** called.
    3.  **Player enters a non-adventure world:**
        - Mock `rotationManager.getTrackByWorld()` to return `null`.
        - Verify that `broadcastTask.run()` is **not** called.

### `listener/ExtractionListener.java`

- **Dependencies:** `AdventureBGS`, `ExtractionManager`, `RotationManager`, `SettingsProvider`, `PlayerMoveEvent`.
- **Testing Strategy:** This is a complex listener. The primary goal is to test the `onMove` event and the chain of logic it triggers (entering/leaving zones, starting/canceling extraction). State changes within `ExtractionZone` will need to be verified.
- **Test Cases for `onMove`:**
    1.  **Player moves but does not cross a zone boundary:**
        - Mock `from` and `to` locations within the same zone status (both inside or both outside).
        - Mock `extractionManager.getZone()` to return the same zone (or `null`) for both locations.
        - Verify no extraction logic is triggered.
    2.  **Player enters an extraction zone:**
        - Mock `from` (outside) and `to` (inside) a zone.
        - Mock `extractionManager.getZone()` to return `null` for `from` and a valid `ExtractionZone` for `to`.
        - Verify `zone.addExtractingPlayer()` is called.
        - Verify `startExtraction()` is called on the listener.
    3.  **Player leaves an extraction zone:**
        - Mock `from` (inside) and `to` (outside) a zone.
        - Verify `zone.removeExtractingPlayer()` is called.
        - Verify `zone.cancelExtraction()` is called.
    4.  **Extraction start and success:**
        - In `startExtraction()`, verify a `BukkitRunnable` is created and started.
        - Verify the zone's boss bar is updated and `extractionBossBar.addPlayer()` is called.
        - In the runnable's `run()` method, simulate the timer counting down.
        - When timer hits zero, verify `completeExtraction()` is called.
        - In `completeExtraction()`, verify player is teleported (`player.teleport()`) and success messages/sounds are played.
    5.  **Extraction cancellation:**
        - When a player leaves a zone, verify `zone.cancelExtraction()` is called, which should cancel the runnable and reset the boss bar.
    6.  **Zone is on cooldown (`down`):**
        - If a player enters a zone where `zone.isDown()` is `true`, verify that no extraction logic is triggered.

### `manager/GuiManager.java`

- **Dependencies:** `AdventureBGS`, `RotationManager`, `AdventureSettings`.
- **Testing Strategy:** Test the logic for creating and populating the GUI inventory. Verify item stacks are created with the correct meta-data based on the `RotationTrack` and `WorldInfo`.
- **Test Cases for `openAdventureGUI`:**
    1.  **GUI Title and Size:**
        - Verify `Bukkit.createInventory()` is called with the title from `settings.getGuiTitle()` and a calculated size.
    2.  **Item Generation:**
        - For each `RotationTrack`, verify that an `ItemStack` is created.
        - Mock the `TrackGuiConfig` and `WorldInfo` associated with the track.
        - Verify the item's display name and lore are set correctly based on the GUI config and world info.
        - Verify `SkullUtil.fromTexture()` is called with the correct texture.
    3.  **Teleport Item:**
        - Verify the "teleport" item is created with the correct name, lore, and slot from the `TrackGuiConfig`.
    4.  **Player GUI Opening:**
        - Verify `player.openInventory()` is called with the created inventory.

### `placeholder/BGSExpansion.java`

- **Dependencies:** `RotationManager`, `OfflinePlayer`.
- **Testing Strategy:** Test the `onRequest` method with various placeholder identifiers.
- **Test Cases:**
    1.  **Identifier `track_time_left_<trackId>`:**
        - **Valid track:** Mock `rotationManager.getTrackById()` to return a track. Verify the formatted time `(mm:ss)` is returned.
        - **Invalid track:** Mock `rotationManager.getTrackById()` to return `null`. Verify `null` or an error string is returned.
    2.  **Identifier `track_next_map_<trackId>`:**
        - **Valid track with next world:** Verify returns the `chatName` of the `nextWorld`.
        - **Valid track, no next world:** Verify returns a default/placeholder string.
    3.  **Identifier `track_current_map_<trackId>`:**
        - Verify returns the `chatName` of the `currentWorld`.
    4.  **Unknown identifier:**
        - Verify the method returns `null`.

### `setting/AdventureSettings.java`

- **Dependencies:** `FileConfiguration`.
- **Testing Strategy:** This is a crucial class for configuration parsing. Test it with a heavily mocked `FileConfiguration` to ensure all values are read correctly, defaults are applied, and data structures are built properly.
- **Test Cases:**
    1.  **Primitive Types:**
        - For each `get...` method (e.g., `getString`, `getInt`, `getBoolean`), verify it reads the correct value from the mock config.
        - Test with missing paths to ensure default values are used.
    2.  **`Location` Parsing:**
        - Provide a valid location string in the config. Verify `getAlertFixedLocation()` returns a correct `Location` object.
        - Test with an invalid or incomplete string to ensure it handles the error gracefully (e.g., returns null or a default).
    3.  **`worlds` List Parsing (`WorldInfo`):**
        - Mock a configuration section for `worlds`.
        - Verify that a list of `WorldInfo` objects is created with the correct data loaded from the config.
    4.  **`tracks` Map Parsing (`RotationTrack`):**
        - Mock a configuration section for `tracks`.
        - Verify that a map of `RotationTrack` objects is created, and that they are linked to the correct `WorldInfo` objects.
    5.  **`extraction` Map Parsing (`ExtractionZone`):**
        - Mock a configuration section for `extraction-zones`.
        - Verify that a map of `ExtractionZone` lists is created correctly.
        - Test the private `parseZone` method's logic indirectly by checking the output.

### `task/BroadcastTask.java`

- **Dependencies:** `AdventureBGS`, `RotationManager`, `AdventureSettings`. This is a `BukkitRunnable`.
- **Testing Strategy:** Test the `run()` method's logic. Since it's time-based, tests will focus on the state of the `RotationTrack` and verifying that the correct actions (messages, sounds, commands) are triggered at the right times.
- **Test Cases:**
    1.  **`run()` method - Main Loop:**
        - For each track from `rotationManager`, verify `track.tick()` is called.
    2.  **New Map Cycle Broadcast (`isNewCycle`):**
        - If a track's `isNewCycle()` is true, verify that the `newMapMessage` is broadcast to all players.
        - Verify the `hasBroadcasted` flag on the track is set to `true`.
    3.  **Warning Countdown (`startWarningCountdown`):**
        - When a track's `secondsUntilNextCycle` falls below `warnMinutes`, verify a new `BukkitRunnable` (the warning task) is started.
        - **Inside the warning task:**
            - Verify the boss bar is created and shown to players in the relevant worlds.
            - Verify the boss bar's progress is updated correctly on each tick.
            - Verify alert messages/sounds/particles are triggered at the configured `alertSeconds`.
            - When the timer hits 0, verify the `onTimeUp` consumer is executed and the task is cancelled.

### `weather/BlizzardWeather.java`

- **Dependencies:** `World`, `Player`.
- **Testing Strategy:** Test the `tick()` method to verify that weather effects are correctly applied to players based on their conditions (e.g., location, armor).
- **Test Cases:**
    1.  **Player freezing:**
        - Simulate a player in a cold biome, not near a heat source, without leather armor.
        - Call `tick()` repeatedly.
        - Verify the player's freeze ticks increase (`player.setFreezeTicks()`).
        - Verify damage is applied when max freeze ticks are reached (`player.damage()`).
    2.  **Player warming:**
        - Simulate a player near a heat source (e.g., torch, as determined by block type at location).
        - Verify the `isWarming` check works correctly.
        - Verify freeze ticks decrease.
    3.  **Player is immune (wearing leather armor):**
        - Mock `player.getInventory().getArmorContents()` to include leather armor.
        - Verify the player does not accumulate freeze ticks.
    4.  **`start()` and `stop()`:**
        - Verify that `start()` sets the world's weather to `DOWNFALL`.
        - Verify that `stop()` clears the weather.

### Classes Requiring Minimal/No Unit Tests

- **`model/TrackGuiConfig.java`**: Simple data holder, populated by `AdventureSettings`.
- **`model/WeatherModel.java`**: Simple data holder. `random...` methods could be tested, but are low priority.
- **`model/WorldInfo.java`**: Simple data holder.
- **`setting/SettingsProvider.java`**: Simple getter/setter.
- **`util/BeaconColorUtil.java`**: Directly manipulates the world, making it very difficult to unit test. Best for integration tests.
- **`util/SkullUtil.java`**: Relies on `ItemMeta` and `Bukkit.getUnsafe()` which can be difficult to mock reliably. Low priority.
- **`weather/WeatherEffect.java`**: An interface with no concrete methods. No tests needed.