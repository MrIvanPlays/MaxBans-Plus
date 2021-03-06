package org.maxgamer.maxbans.service;

import org.junit.Assert;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.junit.Test;
import org.maxgamer.maxbans.exception.RejectedException;
import org.maxgamer.maxbans.locale.Locale;
import org.maxgamer.maxbans.orm.User;
import org.maxgamer.maxbans.test.UnitTest;
import org.maxgamer.maxbans.util.Lockdown;

import java.io.File;
import java.time.Instant;
import java.util.Collections;

import static org.mockito.Mockito.*;

/**
 * @author netherfoam
 */
public class LockdownServiceTest implements UnitTest {
    @Test
    public void testLockdown() throws RejectedException {
        Server server = mock(Server.class);
        UserService users = mock(UserService.class);
        BroadcastService broadcast = mock(BroadcastService.class);
        FileConfiguration lockdownYml = YamlConfiguration.loadConfiguration(new File("lockdown.yml"));
        Locale locale = new Locale();
        locale.put("lockdown.broadcast", "The server was locked down by {{name|Console}} for {{reason}}");

        LockdownService service = new LockdownService(server, users, broadcast, lockdownYml);

        service.lockdown(null, "all", "maintenance", locale);
        Assert.assertEquals("expect ALL", Lockdown.ALL, service.getState());
    }

    @Test
    public void testKickPlayers() throws RejectedException {
        Server server = mock(Server.class);
        UserService users = mock(UserService.class);
        BroadcastService broadcast = mock(BroadcastService.class);
        FileConfiguration lockdownYml = YamlConfiguration.loadConfiguration(new File("lockdown.yml"));
        Locale locale = new Locale();
        locale.put("lockdown.broadcast", "The server was locked down by {{name|Console}} for {{reason}}");
        locale.put("lockdown.message", "The server was locked down by {{name|Console}} for {{reason}}");

        Player player = mock(Player.class);
        doReturn(Collections.singletonList(player)).when(server).getOnlinePlayers();

        LockdownService service = new LockdownService(server, users, broadcast, lockdownYml);

        service.lockdown(null, "all", "maintenance", locale);
        Assert.assertEquals("expect ALL", Lockdown.ALL, service.getState());

        // Assert we kicked the player
        verify(player, times(1)).kickPlayer(any());
    }

    @Test
    public void testNotKickOldPlayers() throws RejectedException {
        Server server = mock(Server.class);
        UserService users = mock(UserService.class);
        BroadcastService broadcast = mock(BroadcastService.class);
        FileConfiguration lockdownYml = YamlConfiguration.loadConfiguration(new File("lockdown.yml"));
        User user = mock(User.class);
        Locale locale = new Locale();
        locale.put("lockdown.broadcast", "The server was locked down by {{name|Console}} for {{reason}}");
        locale.put("lockdown.message", "The server was locked down by {{name|Console}} for {{reason}}");

        Player player = mock(Player.class);
        doReturn(Collections.singletonList(player)).when(server).getOnlinePlayers();
        doReturn(user).when(users).getOrCreate(any());
        doReturn(Instant.EPOCH).when(user).getFirstActive();

        LockdownService service = new LockdownService(server, users, broadcast, lockdownYml);

        service.lockdown(null, "recent", "maintenance", locale);
        Assert.assertEquals("expect NEW", Lockdown.NEW, service.getState());

        // Assert we never kicked the old player
        verify(player, never()).kickPlayer(any());
    }

    @Test
    public void testKickNewPlayers() throws RejectedException {
        Server server = mock(Server.class);
        UserService users = mock(UserService.class);
        BroadcastService broadcast = mock(BroadcastService.class);
        FileConfiguration lockdownYml = YamlConfiguration.loadConfiguration(new File("lockdown.yml"));
        User user = mock(User.class);
        Locale locale = new Locale();
        locale.put("lockdown.broadcast", "The server was locked down by {{name|Console}} for {{reason}}");
        locale.put("lockdown.message", "The server was locked down by {{name|Console}} for {{reason}}");

        Player player = mock(Player.class);
        doReturn(Collections.singletonList(player)).when(server).getOnlinePlayers();
        doReturn(user).when(users).getOrCreate(any());
        doReturn(Instant.now()).when(user).getFirstActive();

        LockdownService service = new LockdownService(server, users, broadcast, lockdownYml);

        service.lockdown(null, "recent", "maintenance", locale);
        Assert.assertEquals("expect NEW", Lockdown.NEW, service.getState());

        // Assert we kicked the new player
        verify(player, times(1)).kickPlayer(any());
    }

    @Test(expected = RejectedException.class)
    public void testJoin() throws RejectedException {
        Server server = mock(Server.class);
        UserService users = mock(UserService.class);
        BroadcastService broadcast = mock(BroadcastService.class);
        FileConfiguration lockdownYml = YamlConfiguration.loadConfiguration(new File("lockdown.yml"));
        User user = mock(User.class);
        Locale locale = new Locale();
        locale.put("lockdown.broadcast", "The server was locked down by {{name|Console}} for {{reason}}");
        locale.put("lockdown.message", "The server was locked down by {{name|Console}} for {{reason}}");

        LockdownService service = new LockdownService(server, users, broadcast, lockdownYml);

        service.lockdown(null, "join", "maintenance", locale);
        Assert.assertEquals("expect JOIN", Lockdown.JOIN, service.getState());

        // Should throw RejectedException
        service.onJoin(user);
    }

    @Test
    public void testPersistence() throws RejectedException {
        Server server = mock(Server.class);
        UserService users = mock(UserService.class);
        BroadcastService broadcast = mock(BroadcastService.class);
        FileConfiguration lockdownYml = YamlConfiguration.loadConfiguration(new File("lockdown.yml"));

        Locale locale = new Locale();
        locale.put("lockdown.broadcast", "The server was locked down by {{name|Console}} for {{reason}}");

        LockdownService service = new LockdownService(server, users, broadcast, lockdownYml);
        service.lockdown(null, "join", "maintenance", locale);

        service = new LockdownService(server, users, broadcast, lockdownYml);
        Assert.assertEquals("expect persistent state", Lockdown.JOIN, service.getState());
    }
}
