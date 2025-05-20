package noah.grimoireSMP;

import net.md_5.bungee.api.ChatMessageType;
import noah.grimoireSMP.ritual.RitualCommand;
import noah.grimoireSMP.ritual.RitualManager;
import noah.grimoireSMP.scrollmanagers.astral.AstralPhaseListener;
import noah.grimoireSMP.scrollmanagers.astral.AstralProjectionManager;
import noah.grimoireSMP.scrollmanagers.astral.StoredMobData;
import noah.grimoireSMP.scrollmanagers.berserker.BerserkerStrengthBoostListener;
import noah.grimoireSMP.scrollmanagers.berserker.BloodMarkManager;
import noah.grimoireSMP.listener.*;
import noah.grimoireSMP.scroll.ScrollRegistry;
import noah.grimoireSMP.commands.*;
import noah.grimoireSMP.scrollmanagers.celestial.CosmicShowerManager;
import noah.grimoireSMP.scrollmanagers.sylvan.DruidInstinctManager;
import noah.grimoireSMP.scrollmanagers.sylvan.OneWithNatureListener;
import noah.grimoireSMP.scrollmanagers.temporal.ClaritySurgeListener;
import noah.grimoireSMP.scrollmanagers.temporal.TemporalAwarenessManager;
import noah.grimoireSMP.scrollmanagers.temporal.TemporalHiatusManager;
import noah.grimoireSMP.scrollmanagers.temporal.TemporalHistoryManager;
import noah.grimoireSMP.scrollmanagers.treasure.TreasureMineListener;
import noah.grimoireSMP.scrollmanagers.treasure.WealthHoardListener;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.scheduler.BukkitRunnable;

public class GrimoireSMP extends JavaPlugin {

    private static GrimoireSMP instance;
    private BloodMarkManager bloodMarkMgr;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("GrimoireSMP enabled on Minecraft 1.21.3!");
        saveDefaultConfig();

        getCommand("scrollstart")
                .setExecutor(new ScrollStartCommand(this));

        if (getConfig().getBoolean("scroll-system-enabled", false)) {
            startScrollSystem();
        } else {
            getLogger().info("Scroll system is disabled; waiting for /scrollstart");
        }

    }

    public void startScrollSystem() {
        // Register scroll types.
        ScrollRegistry.registerScrolls();
        // Reload the persistent config.
        ScrollDataManager.getInstance(this).reloadConfig();
        // Clear in-memory data from any previous session
        PlayerData.clearPlayerData();

        // For each online player, load their data from the YAML file.
        for (Player player : getServer().getOnlinePlayers()) {
            PlayerData.loadPlayerData(player);
            // Optionally, update their inventory after a short delay.
            getServer().getScheduler().runTaskLater(this, () -> PlayerData.updateScrollItem(player), 2L);
        }


        // Register event listeners.
        getServer().getPluginManager().registerEvents(new RuneManager(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(new ScrollProtectionListener(), this);
        getServer().getPluginManager().registerEvents(new RuneConsumeListener(), this);
        getServer().getPluginManager().registerEvents(new GrimoireHandleListener(), this);
        getServer().getPluginManager().registerEvents(new ContainerCheckListener(), this);
        getServer().getPluginManager().registerEvents(new ScrollRightClickListener(this), this);
        getServer().getPluginManager().registerEvents(new SelfDamageCancelListener(), this);
        getServer().getPluginManager().registerEvents(new CosmicShowerManager(), this);
        getServer().getPluginManager().registerEvents(new AstralPhaseListener(), this);
        getServer().getPluginManager().registerEvents(new AstralProjectionManager(), this);
        getServer().getPluginManager().registerEvents(new TreasureMineListener(), this);
        getServer().getPluginManager().registerEvents(new WealthHoardListener(), this);
        getServer().getPluginManager().registerEvents(new ClaritySurgeListener(), this);
        getServer().getPluginManager().registerEvents(new OneWithNatureListener(), this);
        getServer().getPluginManager().registerEvents(new SwitchCaseListener(), this);
        DruidInstinctManager.init(this);
        // getServer().getPluginManager().registerEvents(new BerserkerStrengthBoostListener(), this); this is disabled for now




        ConfigurationSerialization.registerClass(StoredMobData.class, "StoredMobData");

        TemporalAwarenessManager.start(this);
        TemporalHiatusManager.init(this);
        TemporalHistoryManager.start(this);

        // Register commands.
        getCommand("resetall").setExecutor(new ResetAllCommand());
        getCommand("givehandle").setExecutor(new GiveHandleCommand());
        getCommand("giverune").setExecutor(new GiveRuneCommand());
        AllyCommand allyCmd = new AllyCommand();
        getCommand("ally").setExecutor(allyCmd);
        getCommand("ally").setTabCompleter(allyCmd);
        GiveScrollCommand giveCmd = new GiveScrollCommand(this);
        getCommand("givescroll").setExecutor(giveCmd);
        getCommand("givescroll").setTabCompleter(giveCmd);
        this.getCommand("giveswitchcase").setExecutor(new GiveSwitchCaseCommand());

        bloodMarkMgr = new BloodMarkManager();
        getServer().getPluginManager().registerEvents(bloodMarkMgr, this);
        // pass it to your scroll registry or wherever you need it

        // ritual
        RitualManager.init(this);
        getCommand("startritual").setExecutor(new RitualCommand());



        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                // Retrieve the player's scroll data and activate its passive abilities.
                PlayerScrollData data = PlayerData.getPlayerScrollData(player);
                if (data != null) {
                    data.getScroll().passiveAbilityStart(player);
                }
            }

            for (Player player : Bukkit.getOnlinePlayers()) {
                PlayerScrollData data = PlayerData.getPlayerScrollData(player);
                if (data == null) continue;

                long now = System.currentTimeMillis();
                // 1) Check for silence
                if (data.isAbilitiesDisabled()) {
                    long remMs = data.getAbilitiesDisabledUntil() - now;
                    long remSec = Math.max(0, (remMs + 999) / 1000);  // ceil to seconds

                    String msg = "§8§lSilenced: §r§f" + remSec + "s";
                    // Using Spigot API to send ActionBar:
                    player.spigot().sendMessage(
                            ChatMessageType.ACTION_BAR,
                            new TextComponent(msg)
                    );
                    return;
                }
                    List<Ability> abilities = data.getScroll().getAbilities(player);

                // Build a single line by joining each formatted ability
                String line = abilities.stream()
                        .map(Ability::format)
                        .collect(Collectors.joining("   "));

                // Send to action bar
                player.spigot().sendMessage(
                        ChatMessageType.ACTION_BAR,
                        new TextComponent(line)
                );
            }
        }, 0L, 20L); // 20 ticks (1 second) interval
    }

    public BloodMarkManager getBloodMarkManager() {
        return bloodMarkMgr;
    }

    public static GrimoireSMP getInstance() {
        return instance;
    }

    /**
     * Shows a slot-machine–style roll of all registered scroll names
     * to the given player, then invokes the callback with the selected key.
     */
    public void showScrollRoll(Player player, Consumer<String> resultCallback) {
        List<String> scrollKeys = ScrollRegistry.getAllScrollNames();
        int totalCycles = scrollKeys.size() * 2;  // number of visual spins
        final int interval = 5;                   // ticks between titles

        // Choose the final scroll at random, BEFORE animation finishes
        String finalScroll = scrollKeys.get(new Random().nextInt(scrollKeys.size()));
        String finalDisplay = finalScroll.substring(0, 1).toUpperCase() + finalScroll.substring(1);

        new BukkitRunnable() {
            int count = 0;

            @Override
            public void run() {
                // Show the "rolling" scroll name
                int idx = count % scrollKeys.size();
                String display = scrollKeys.get(idx).substring(0, 1).toUpperCase()
                        + scrollKeys.get(idx).substring(1);

                player.sendTitle(
                        "§6" + display,
                        "§7Choosing your scroll...",
                        3, interval, 3
                );
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.12f);
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.02f);

                count++;
                if (count > totalCycles) {
                    cancel();
                    // Final landing
                    player.sendTitle(
                            "§aYour scroll: §l" + finalDisplay,
                            "",
                            5, 40, 5
                    );
                    player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.1f);
                    getLogger().info("Assigned scroll: " + finalScroll);
                    resultCallback.accept(finalScroll);
                }
            }
        }.runTaskTimer(this, 0L, interval);
    }


    @Override
    public void onDisable() {
        // Save the scroll data persistently.
        ScrollDataManager.getInstance(this).saveConfig();
        getLogger().info("GrimoireSMP disabled.");

    }

}
