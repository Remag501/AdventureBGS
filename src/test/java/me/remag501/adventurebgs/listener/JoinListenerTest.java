package me.remag501.adventurebgs.listener;



import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import me.remag501.adventurebgs.AdventureBGS;
import me.remag501.adventurebgs.manager.PDCManager;
import me.remag501.adventurebgs.manager.PenaltyManager;
import me.remag501.adventurebgs.manager.RotationManager;
import me.remag501.adventurebgs.model.RotationTrack;
import org.bukkit.World;
import org.bukkit.event.player.PlayerJoinEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

public class JoinListenerTest {

    private ServerMock server;
    private AdventureBGS plugin;
    private PDCManager pdcManager;
    private PenaltyManager penaltyManager;
    private RotationManager rotationManager;
    private JoinListener joinListener;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();

//         Total Mock approach - bypasses all MockBukkit JAR loading

        plugin = MockBukkit.load(AdventureBGS.class);

        pdcManager = mock(PDCManager.class);
        penaltyManager = mock(PenaltyManager.class);
        rotationManager = mock(RotationManager.class);

        joinListener = new JoinListener(pdcManager, rotationManager, penaltyManager);
        server.getPluginManager().registerEvents(joinListener, plugin);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void onPlayerJoin_withOutdatedPlayerInAdventureWorld_shouldApplyPenalty() {
        // Arrange
        World world = server.addSimpleWorld("adventure_world");
        PlayerMock player = server.addPlayer();
        player.setLocation(world.getSpawnLocation());

        when(rotationManager.getTrackByWorld(world)).thenReturn(mock(RotationTrack.class)); // It's an adventure world
        when(pdcManager.isPlayerOutdated(player, world)).thenReturn(true); // Player is outdated
//
//        // Act
//        // The event is triggered by MockBukkit when the player is added, but for clarity
//        // and direct testing, we can also call it manually. Let's create the event.
        PlayerJoinEvent event = new PlayerJoinEvent(player, "test join message");
        joinListener.onPlayerJoin(event);
//
//        // Assert
        verify(penaltyManager, times(1)).penalizePlayer(player);
        verify(pdcManager, times(1)).syncPlayerToWorld(player, world);
    }

    @Test
    void onPlayerJoin_withPlayerInNonAdventureWorld_shouldDoNothing() {
        // Arrange
        World world = server.addSimpleWorld("normal_world");
        PlayerMock player = server.addPlayer();
        player.setLocation(world.getSpawnLocation());

        when(rotationManager.getTrackByWorld(world)).thenReturn(null); // Not an adventure world

        // Act
        PlayerJoinEvent event = new PlayerJoinEvent(player, "test join message");
        joinListener.onPlayerJoin(event);

        // Assert
        verify(penaltyManager, never()).penalizePlayer(player);
        verify(pdcManager, never()).syncPlayerToWorld(player, world);
    }

    @Test
    void onPlayerJoin_withUpToDatePlayerInAdventureWorld_shouldDoNothing() {
        // Arrange
        World world = server.addSimpleWorld("adventure_world");
        PlayerMock player = server.addPlayer();
        player.setLocation(world.getSpawnLocation());

        when(rotationManager.getTrackByWorld(world)).thenReturn(mock(RotationTrack.class)); // It's an adventure world
        when(pdcManager.isPlayerOutdated(player, world)).thenReturn(false); // Player is up-to-date

        // Act
        PlayerJoinEvent event = new PlayerJoinEvent(player, "test join message");
        joinListener.onPlayerJoin(event);

        // Assert
        verify(penaltyManager, never()).penalizePlayer(player);
//        verify(pdcManager, never()).syncPlayerToWorld(player, world); // Not relevant for player joining
    }
}
