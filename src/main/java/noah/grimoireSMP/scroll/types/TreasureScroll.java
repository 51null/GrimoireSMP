package noah.grimoireSMP.scroll.types;

import noah.grimoireSMP.Ability;
import noah.grimoireSMP.GrimoireSMP;
import noah.grimoireSMP.PlayerData;
import noah.grimoireSMP.PlayerScrollData;
import noah.grimoireSMP.scroll.Scroll;
import noah.grimoireSMP.scroll.ScrollLoreUtil;
import noah.grimoireSMP.scrollmanagers.berserker.BloodMarkManager;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class TreasureScroll extends Scroll {

    private final Map<UUID, Long> wHoardingCooldowns = new HashMap<>();
    private final Map<UUID, Long> debtCooldowns = new HashMap<>();

    public TreasureScroll() {
        super(
                "Treasure Scroll",
                113,
                114
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
                    Pair.of("§e§lClarity Surge§r", "When hurt, you bend time, staying clear of incoming hazards. Increased power with Grimoire - Minimum 5 Essence")
            );
            case "Treasure Scroll" -> List.of(
                    Pair.of("§a§lRich Connections§r", "Permanent Hero of the Village status. Scales with Rune Essence"),
                    Pair.of("§a§lLucky Fella§r", "Permanent Luck"),
                    Pair.of("§a§lRefined Hands§r", "Auto-Enchant items with looting and fortune. Scales with Grimoire - Minimum 5 Essence"),
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
                    "§a§lDebt Collector",
                    "§f§lSHIFT§r§7 - Fire a giant beam, stealing HP, XP & valuables from anyone hit",
                    5
            );
            default -> new ScrollLoreUtil.AbilityInfo("", "", Integer.MAX_VALUE);
        };
    }


    @Override
    public void useActiveAbility(int abilityNumber, Player player) {
        PlayerScrollData data = PlayerData.getPlayerScrollData(player);
        if (data.getRuneEssence() <= 0) return;
        if (abilityNumber == 1) {
            UUID uuid = player.getUniqueId();
            if (PlayerData.getPlayerScrollData(player).isAbilitiesDisabled()) {
                player.sendMessage("§cYour scroll is silenced!");
                return;
            }
            if (PlayerData.getPlayerScrollData(player).isWealthHoardingActive()) {
                player.sendMessage("§eWealth Hoarding is already active!");
                return;
            }
            // Check cooldown
            long now = System.currentTimeMillis();
            Long expiry = wHoardingCooldowns.get(uuid);
            if (expiry != null && now < expiry) {
                return;
            }
            wHoardingCooldowns.put(uuid, now + (15 * 60 * 1000L));

            data.activateWealthHoarding(5 * 60 * 1000L);
            player.sendMessage("§aYou feel the tides of fortune! Wealth Hoarding active for 5 minutes.");


        }
    }




    @Override
    public void passiveAbilityStart(Player player) {
        if (PlayerData.getPlayerScrollData(player).getRuneEssence() <= 0) return;

        player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 30, 0, true, false, true));

        int essence = PlayerData.getPlayerScrollData(player).getRuneEssence();   // 1–5 (max)
        if (essence >= 5) essence = 5;
        essence = essence - 3;
        if (essence <= 0) essence = 0;

        player.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, 30, essence, true, false, true));


        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();
        PlayerScrollData data = PlayerData.getPlayerScrollData(player);

        enchantIfApplicable(mainHand, data);
        enchantIfApplicable(offHand, data);
    }


    private void enchantIfApplicable(ItemStack item, PlayerScrollData data) {
        if (item == null || item.getType().isAir()) return;
        if (data.getRuneEssence() <= 4) return;
        Material mat = item.getType();
        if (mat.name().endsWith("_SWORD")) {
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return;

            int level;
            if (data.isGrimoire()) {
                level = 3;
            } else if (data.getRuneEssence() <= 5) { // ==5
                level = 2;
            } else {
                return;
            }

            int current = meta.getEnchantLevel(Enchantment.LOOTING);
            if (level >current) {
                meta.removeEnchant(Enchantment.LOOTING);
                meta.addEnchant(Enchantment.LOOTING, level, true);
                item.setItemMeta(meta);
            }
        }
        if (mat.name().endsWith("_PICKAXE") || mat.name().endsWith("_AXE") || mat.name().endsWith("_HOE") || mat.name().endsWith("_SHOVEL")) {
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return;

            int level;
            if (data.isGrimoire()) {
                level = 3;
            } else if (data.getRuneEssence() <= 5) { // ==5
                level = 2;
            } else {
                return;
            }

            int current = meta.getEnchantLevel(Enchantment.FORTUNE);
            if (level >current) {
                meta.removeEnchant(Enchantment.FORTUNE);
                meta.addEnchant(Enchantment.FORTUNE, level, true);
                item.setItemMeta(meta);
            }
        }
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
        long now = System.currentTimeMillis();
        Long expiry = debtCooldowns.get(uuid);
        if (expiry != null && now < expiry) {
            return;
        }
        debtCooldowns.put(uuid, now + (180 * 1000L));

        final int DURATION_TICKS = 200;       // 10 seconds
        final double   MAX_DIST      = 20.0;
        final double   WIDTH         = 0.5;   // 1-block wide laser
        final double   HEALTH_STEAL  = 0.2;   // ½ heart
        final int      XP_STEAL      = 5;
        final double   KNOCKBACK     = 0.5;

        // Define which materials count as “valuable”
        final List<Material> VALUABLES = List.of(
                Material.DIAMOND,
                Material.GOLD_INGOT,
                Material.EMERALD,
                Material.NETHERITE_INGOT,
                Material.IRON_INGOT,
                Material.NETHERITE_SCRAP,
                Material.ANCIENT_DEBRIS,
                Material.IRON_ORE,
                Material.GOLD_ORE
        );

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (!player.isOnline() || ticks++ > DURATION_TICKS) {
                    cancel();
                    return;
                }

                // 1) Compute laser origin & direction
                Location eye = player.getEyeLocation();
                Vector dir   = eye.getDirection().normalize();
                // 2) Draw the beam (particles)
                for (double d = 2; d < MAX_DIST; d += 0.5) {
                    Location p = eye.clone().add(dir.clone().multiply(d));
                    player.getWorld().spawnParticle(
                            Particle.DUST,
                            p,
                            60,     // count
                            0.16, 0.16, 0.16, // offsets
                            0.02,       // speed
                            new Particle.DustOptions(Color.fromRGB(80, 200, 120), 1.5f)
                    );
                    player.getWorld().spawnParticle(
                            Particle.DUST,
                            p,
                            12,     // count
                            0.2, 0.2, 0.2, // offsets
                            0.03,       // speed
                            new Particle.DustOptions(Color.RED, 1.5f)
                    );
                }

                // 3) Affect any players in the beam
                //    We'll scan all nearby players within a 1-block radius of the 20-block line.
                for (Player victim : player.getWorld().getPlayers()) {
                    if (victim.equals(player)) continue;

                    // Vector from eye → victim eye
                    Vector toVictim = victim.getEyeLocation()
                            .toVector()
                            .subtract(eye.toVector());
                    // Project onto dir
                    double proj = toVictim.dot(dir);
                    if (proj < 0 || proj > MAX_DIST) continue;
                    // Perpendicular distance
                    double perpSq = toVictim.lengthSquared() - proj*proj;
                    if (perpSq > WIDTH*WIDTH) continue;

                    // --- Steal health ---
                    double victimHp = victim.getHealth();

                    double newVictimHp = Math.max(victimHp - HEALTH_STEAL, 1);
                    victim.setHealth(newVictimHp);
                    // Heal caster (clamp at max health)
                    double maxHp = player.getAttribute(Attribute.MAX_HEALTH)
                            .getValue();
                    player.setHealth(Math.min(player.getHealth() + HEALTH_STEAL, maxHp));

                    // --- Steal XP ---
                    // NOTE: getTotalExperience is a bit involved; we'll use giveExp:
                    victim.giveExp(-XP_STEAL);    // remove 5 XP
                    player.giveExp(XP_STEAL);     // give 5 XP

                    // --- Steal items ---
                    for (Material mat : VALUABLES) {
                        ItemStack one = new ItemStack(mat, 1);
                        if (victim.getInventory().contains(mat, 1)) {
                            victim.getInventory().removeItem(one);
                            player.getInventory().addItem(one);
                        }
                    }

                    // --- Knockback ---
                    victim.setVelocity(dir.clone().multiply(KNOCKBACK));
                }
            }
        }.runTaskTimer(GrimoireSMP.getInstance(), 0L, 1L);
    }

    @Override
    public List<Ability> getAbilities(Player player) {
        UUID u = player.getUniqueId();
        if (PlayerData.getPlayerScrollData(player).getRuneEssence() <= 0) return List.of(new Ability("§l----- LOST SCROLL -----", "\uE800", () -> 0L));
        if (PlayerData.getPlayerScrollData(player).isGrimoire()) {
            return List.of(
                    new Ability(
                            "§a§lWealth Hoarding§r",
                            "\uE014",
                            () -> wHoardingCooldowns.getOrDefault(u, 0L)
                    ),
                    new Ability(
                            "§a§lDebt Collector§r",
                            "\uE015",
                            () -> debtCooldowns.getOrDefault(u, 0L)
                    )
            );
        } else {
            return List.of(
                    new Ability(
                            "§a§lWealth Hoarding§r",
                            "\uE014",
                            () -> wHoardingCooldowns.getOrDefault(u, 0L)
                    )
            );
        }

    }

    @Override
    public Scroll cloneScroll() {
        return new TreasureScroll();
    }
}
