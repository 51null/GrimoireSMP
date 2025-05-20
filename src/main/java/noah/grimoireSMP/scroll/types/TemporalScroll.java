package noah.grimoireSMP.scroll.types;

import noah.grimoireSMP.Ability;
import noah.grimoireSMP.GrimoireSMP;
import noah.grimoireSMP.PlayerData;
import noah.grimoireSMP.PlayerScrollData;
import noah.grimoireSMP.scroll.Scroll;
import noah.grimoireSMP.scroll.ScrollLoreUtil;
import noah.grimoireSMP.scrollmanagers.berserker.BloodMarkManager;
import noah.grimoireSMP.scrollmanagers.temporal.TemporalHiatusManager;
import noah.grimoireSMP.scrollmanagers.temporal.TemporalHistoryManager;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TemporalScroll extends Scroll {

    private final Map<UUID, Long> hiatusCooldowns = new HashMap<>();
    private final Map<UUID, Long> chronoCooldowns = new HashMap<>();
    private static final Map<UUID, Boolean> auraStates = new HashMap<>();

    public TemporalScroll() {
        super(
                "Temporal Scroll",
                117,
                118
        );
    }

    @Override
    public List<Pair<String,String>> getPassiveInfo() {
        return switch(getName()) {
            case "Astral Scroll" -> List.of(
                    Pair.of("§5§lAstral Phase§r","% chance to phase through incoming damage. Based on Rune Essence")
            );
            case "Berserker Scroll" -> List.of(
                    Pair.of("§4§lConqueror Blood§r", "Grants permanent Strength I, Strength II if Grimoire'd."),
                    Pair.of("§4§lBlood Spill§r", "Auto-Enchants your swords and axes with Sharpness. Scales with Rune Essence / Grimoire")
            );
            case "Celestial Scroll" -> List.of(
                    Pair.of("§e§lStarlight§r", "Gain extra health under the daylight's sunlight"),
                    Pair.of("§e§lCosmic Trail§r", "Leave a dust trail that weakens and slow foes behind you")
            );
            case "Infernal Scroll" -> List.of(
                    Pair.of("§6§lFlame Aura§r", "Scorch nearby hostile players continuously - Deactivate with §f§lQ§r - Minimum 3 Essence"),
                    Pair.of("§6§lEmber Resilience§r", "Fire occasionally heals you instead of harming you + Permanent Fire Aspect")
            );
            case "Splash Scroll" -> List.of(
                    Pair.of("§3§lOcean's Grace§r", "Permanent Dolphin's Grace. Multiplied with Grimoire"),
                    Pair.of("§3§lFamiliar Fins§r", "Auto-Enchants your boots with Depth Strider. Scales with Rune Essence / Grimoire")
            );
            case "Steel Scroll" -> List.of(
                    Pair.of("§7§lSteel Reinforcements§r", "Gain Resistance I from your metallic skin"),
                    Pair.of("§7§lHeavy Body§r", "Permanent Slowness II due to your heavy skin")
            );
            case "Sylvan Scroll" -> List.of(
                    Pair.of("§2§lDruid's Instinct§r", "See all players glowing in forest biomes"),
                    Pair.of("§2§lOne With Nature§r", "Consume a wood log for Regeneration, requires you to have your scroll in offhand/hand")
            );
            case "Temporal Scroll" -> List.of(
                    Pair.of("§e§lTemporal Awareness§r", "A subtle slowing of incoming projectiles or damage gives you a moment to react"),
                    Pair.of("§e§lClarity Surge§r", "When hurt, you bend time, staying clear of incoming hazards. Increased power with Grimoire - Deactivate with §f§lQ§r§7 - Minimum 5 Essence")
            );
            case "Treasure Scroll" -> List.of(
                    Pair.of("§a§lRich Connections§r", "Permanent Hero of the Village status. Scales with Rune Essence"),
                    Pair.of("§a§lLucky Fella§r", "Permanent Luck"),
                    Pair.of("§a§lMINE!§r", "Drain armor durability from foes on hit, mending your own and hurting theirs further")
            );
            default -> List.of();
        };
    }

    @Override
    public List<ScrollLoreUtil.AbilityInfo> getActiveInfo() {
        return switch(getName()) {
            case "Astral Scroll" -> List.of(
                    new ScrollLoreUtil.AbilityInfo("§5§lDagger Summon§r", "§f§lM2§r§7 - Launch a volley of ethereal daggers, silencing other player's abilities. Based on Rune Essence", 1),
                    new ScrollLoreUtil.AbilityInfo("§5§lAstral Pocket§r", "§f§lQ§r§7 - Store and release mobs on demand! Storage scales with Rune Essence / Grimoire", 1)
            );
            case "Berserker Scroll" -> List.of(
                    new ScrollLoreUtil.AbilityInfo("§4§lFang of Demise§r",
                            "§f§lM2§r§7 - Summon an demonic hand that pulls enemies to you", 1)
            );
            case "Celestial Scroll" -> List.of(
                    new ScrollLoreUtil.AbilityInfo("§e§lCelestial Alignment§r",
                            "§f§lM2§r§7 - Boost allies’ defenses, granting Health, Regeneration and Resistance, and weaken foes in your proximity", 1)
            );
            case "Infernal Scroll" -> List.of(
                    new ScrollLoreUtil.AbilityInfo("§6§lFireball§r",
                            "§f§lM2§r§7 - Charge and hurl an explosive, incendiary fireball. Scales with Rune Essence", 1)
            );
            case "Splash Scroll" -> List.of(
                    new ScrollLoreUtil.AbilityInfo("§3§lFisherman's Trap§r",
                            "§f§lM2§r§7 - Erupt a giant water column that flings and damages enemies. Can be used to boost yourself!", 1)
            );
            case "Steel Scroll" -> List.of(
                    new ScrollLoreUtil.AbilityInfo("§7§lCyborg Arm",
                            "§f§lQ§r§7 - Grapple to blocks or pull players with a mechanical arm. Range scales with Rune Essence", 1),
                    new ScrollLoreUtil.AbilityInfo("§7§lBullet Storm§r",
                            "§f§lM2§r§7 - Spawn and fire rapid energy steel projectiles in succession. Scales with Rune Essence", 1)
            );
            case "Sylvan Scroll" -> List.of(
                    new ScrollLoreUtil.AbilityInfo("§2§lVine Snare§r",
                            "§f§lM2§r§7 - Send animated vines that root and slow enemies", 1)
            );
            case "Temporal Scroll" -> List.of(
                    new ScrollLoreUtil.AbilityInfo("§e§lHiatus§r",
                            "§f§lM2§r§7 - Freeze all nearby entities in time for 2.5+ seconds. Scales with Rune Essence", 1)
            );
            case "Treasure Scroll" -> List.of(
                    new ScrollLoreUtil.AbilityInfo("§a§lWealth Hoarding§r",
                            "§f§lM2§r§7 - Double all ore, XP, and mob drops for 5 minutes", 1)
            );
            default -> List.of();
        };
    }

    @Override
    public ScrollLoreUtil.AbilityInfo getGrimoireInfo() {
        return switch(getName()) {
            case "Astral Scroll" -> new ScrollLoreUtil.AbilityInfo(
                    "§5§lAstral Projection§r",
                    "§f§lSHIFT§r§7 - Leave your body and scout the area in spirit form, if players touch your original body, you return",
                    5
            );
            case "Berserker Scroll" -> new ScrollLoreUtil.AbilityInfo(
                    "§4§lBerserker's Instinct§r",
                    "§f§lSHIFT§r§7 - When you kill a player, theres a chance you recieve a §4BLOOD MARKED§r armor piece, use this armor piece in the menu to track them",
                    5
            );
            case "Celestial Scroll" -> new ScrollLoreUtil.AbilityInfo(
                    "§e§lCosmic Shower§r",
                    "§f§lSHIFT§r§7 - Call down a barrage of meteors on a targeted zone",
                    5
            );
            case "Infernal Scroll" -> new ScrollLoreUtil.AbilityInfo(
                    "§6§lInferno Storm§r",
                    "§f§lSHIFT§r§7 - Charge up in a spiral, to then unleash a raging tempest of flame around you, dealing massive damage",
                    5
            );
            case "Splash Scroll" -> new ScrollLoreUtil.AbilityInfo(
                    "§3§lThe Frost Bite§r",
                    "§f§lSHIFT§r§7 - Encase enemies in ice within a massive dome, whilst trapping them in snow",
                    5
            );
            case "Steel Scroll" -> new ScrollLoreUtil.AbilityInfo(
                    "§7§lFlash Cannon§r",
                    "§f§lSHIFT§r§7 - Fire a devastating beam that knocks back and FLASHBANGS foes!",
                    5
            );
            case "Sylvan Scroll" -> new ScrollLoreUtil.AbilityInfo(
                    "§2§lOvergrowth",
                    "§f§lSHIFT§r§7 - Erect living tree walls and buff allies inside",
                    5
            );
            case "Temporal Scroll" -> new ScrollLoreUtil.AbilityInfo(
                    "§e§lChrono Shift",
                    "§f§lSHIFT§r§7 - Rewind yourself 10 seconds back in time, reverting your old position, health and hunger",
                    5
            );
            case "Treasure Scroll" -> new ScrollLoreUtil.AbilityInfo(
                    "Debt Collector",
                    "§f§lSHIFT§r§7 - Fire a giant beam, stealing HP, XP & valuables from anyone hit",
                    5
            );
            default -> new ScrollLoreUtil.AbilityInfo("", "", Integer.MAX_VALUE);
        };
    }

    @Override
    public void useActiveAbility(int abilityNumber, Player player) {
        if (PlayerData.getPlayerScrollData(player).getRuneEssence() <= 0) return;
        if (abilityNumber == 1) {
            UUID uuid = player.getUniqueId();
            if (PlayerData.getPlayerScrollData(player).isAbilitiesDisabled()) {
                player.sendMessage("§cYour scroll is silenced!");
                return;
            }
            // Check cooldown
            long now = System.currentTimeMillis();
            Long expiry = hiatusCooldowns.get(uuid);
            if (expiry != null && now < expiry) {
                return;
            }
            hiatusCooldowns.put(uuid, now + (130 * 1000L));

            int essence = PlayerData.getPlayerScrollData(player).getRuneEssence();
            if (essence >= 5) essence = 5;
            long duration = 40L + (essence * 8L);
            TemporalHiatusManager.triggerHiatus(player, duration);
        }
        if (abilityNumber == 2) {
            if (isAuraActive(player))
            {
                setAuraActive(player, false);
                player.sendMessage("§eCLARITY SURGE §r§cDISABLED!");
            } else {
                setAuraActive(player, true);
                player.sendMessage("§eCLARITY SURGE §r§aENABLED!");
            }
        }
    }
    public static void setAuraActive(Player player, boolean isActive) {
        auraStates.put(player.getUniqueId(), isActive);
    }
    public static boolean isAuraActive(Player player) {
        return auraStates.getOrDefault(player.getUniqueId(), false);
    }


    @Override
    public void passiveAbilityStart(Player player) {
        if (PlayerData.getPlayerScrollData(player).getRuneEssence() <= 0) return;


    }


    @Override
    public void useGrimoireAbility(Player player) {
            if (!PlayerData.getPlayerScrollData(player).isGrimoire()) return;
            if (PlayerData.getPlayerScrollData(player).getRuneEssence() <= 0) return;
        if (PlayerData.getPlayerScrollData(player).isAbilitiesDisabled()) {
            player.sendMessage("§cYour scroll is silenced!");
            return;
        }
        UUID uuid = player.getUniqueId();
        // Check cooldown
        long now = System.currentTimeMillis();
        Long expiry = chronoCooldowns.get(uuid);
        if (expiry != null && now < expiry) {
            return;
        }

        boolean success = TemporalHistoryManager.restore10Sec(player);
        if (success) {
            chronoCooldowns.put(uuid, now + (145 * 1000L));
            player.sendMessage("§e§lChrono Shift!§r§e You’ve been rolled back 10 seconds.");
        } else {
            player.sendMessage("§cNot enough history yet—wait a few more seconds before shifting.");
        }

    }

    @Override
    public List<Ability> getAbilities(Player player) {
        UUID u = player.getUniqueId();
        if (PlayerData.getPlayerScrollData(player).getRuneEssence() <= 0) return List.of(new Ability("§l----- LOST SCROLL -----", "\uE800", () -> 0L));
        if (PlayerData.getPlayerScrollData(player).isGrimoire()) {
            return List.of(
                    new Ability(
                            "§e§lHiatus§r",
                            "\uE016",
                            () -> hiatusCooldowns.getOrDefault(u, 0L)
                    ),
                    new Ability(
                            "§e§lChrono Shift§r",
                            "\uE017",
                            () -> chronoCooldowns.getOrDefault(u, 0L)
                    )
            );
        } else {
            return List.of(
                    new Ability(
                            "§e§lHiatus§r",
                            "\uE016",
                            () -> hiatusCooldowns.getOrDefault(u, 0L)
                    )
            );
        }

    }

    @Override
    public Scroll cloneScroll() {
        return new TemporalScroll();
    }
}
