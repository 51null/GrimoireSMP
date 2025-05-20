package noah.grimoireSMP.scroll.types;

import noah.grimoireSMP.Ability;
import noah.grimoireSMP.GrimoireSMP;
import noah.grimoireSMP.PlayerData;
import noah.grimoireSMP.PlayerScrollData;
import noah.grimoireSMP.scroll.ScrollLoreUtil;
import noah.grimoireSMP.scrollmanagers.splash.IceDomeManager;
import noah.grimoireSMP.scroll.Scroll;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static noah.grimoireSMP.AbilityDamageUtil.dealAbilityDamage;

public class SplashScroll extends Scroll {

    private final Map<UUID, Long> fishermansTrapCooldowns = new HashMap<>();
    private final Map<UUID, Long> theFrostBiteCooldowns = new HashMap<>();
    public static final Map<UUID, Long> fallImmunity = new HashMap<>();

    public SplashScroll() {
        super(
                "Splash Scroll",
                105,
                106
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
                    "Chrono Shift",
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
        if (PlayerData.getPlayerScrollData(player).isAbilitiesDisabled()) {
            player.sendMessage("§cYour scroll is silenced!");
            return;
        }
            if (abilityNumber == 1) {
                UUID uuid = player.getUniqueId();
                long now = System.currentTimeMillis();
                Long expiry = fishermansTrapCooldowns.get(uuid);
                if (expiry != null && now < expiry) {
                    return;
                }
                fishermansTrapCooldowns.put(uuid, now + (80 * 1000));
                Block targetBlock = player.getTargetBlockExact(50);
                if (targetBlock == null) {
                    player.sendMessage("§eNo valid block in range for Fisherman's Trap!");
                    return;
                }
                Location loc = targetBlock.getLocation().add(0.5, 1.0, 0.5);
                Location loc1 = targetBlock.getLocation().add(0.5, 10.0, 0.5);
                // spawn a "giant water pillar" => we can do a vertical column of water-like particles
                Particle.DustOptions dust = new Particle.DustOptions(Color.fromRGB(62, 164, 240), 200f);
                Particle.DustOptions dust1 = new Particle.DustOptions(Color.fromRGB(153, 192, 227), 200f);
                Particle.DustOptions dust2 = new Particle.DustOptions(Color.fromRGB(233, 233, 253), 200f);
                player.getWorld().spawnParticle(Particle.DUST, loc1, 800, 1.5f, 10, 1.5f, dust);
                player.getWorld().spawnParticle(Particle.DUST, loc1, 800, 1.5f, 10, 1.5f, dust1);
                player.getWorld().spawnParticle(Particle.DUST, loc, 400, 3f, 0f, 3f, dust2);
                player.getWorld().playSound(loc, Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1.5f, 1.8f);
                player.getWorld().playSound(loc, Sound.ENTITY_PLAYER_SPLASH, 3f, 1.0f);

                // find entities in a small radius at that location => launch them up & deal 20 damage
                double radius = 3.5; // tweak
                for (Entity e : loc.getWorld().getNearbyEntities(loc, radius, 10.0, radius)) {
                    if (e instanceof LivingEntity le) {
                        // launch them
                        player.getWorld().spawnParticle(Particle.DUST, loc1, 600, 1.5f, 10, 1.5f, dust);
                        player.getWorld().spawnParticle(Particle.DUST, loc1, 600, 1.5f, 10, 1.5f, dust1);
                        player.getWorld().spawnParticle(Particle.DUST, loc, 200, 3f, 0f, 3f, dust2);
                        Vector up = new Vector(0, 3.5f, 0); // tweak
                        le.setVelocity(up);
                        // damage them
                        if (!le.equals(player))
                        {dealAbilityDamage(player, le, 7);} else grantFallImmunity(player, 200);
                    }
                }
        }
    }

    public static void grantFallImmunity(Player player, long durationTicks) {
        long endTime = System.currentTimeMillis() + (durationTicks * 50); // 1 tick = 50ms
        fallImmunity.put(player.getUniqueId(), endTime);
    }


    @Override
    public void passiveAbilityStart(Player player) {
        if (PlayerData.getPlayerScrollData(player).getRuneEssence() <= 0) return;
        if (PlayerData.getPlayerScrollData(player).isGrimoire()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 30, 4, true, false, true));
        } else player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 30, 2, true, false, true));

        PlayerScrollData data = PlayerData.getPlayerScrollData(player);
        if (data == null || data.getScroll().isLost()) return;

        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        enchantIfApplicable(mainHand, data);
        enchantIfApplicable(offHand, data);

    }

    private void enchantIfApplicable(ItemStack item, PlayerScrollData data) {
        if (item == null || item.getType().isAir()) return;

        Material mat = item.getType();
        if (mat.name().endsWith("_BOOTS")) {
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return;

            int level = 3;

            int current = meta.getEnchantLevel(Enchantment.DEPTH_STRIDER);
            if (level >current) {
                meta.removeEnchant(Enchantment.DEPTH_STRIDER);
                meta.addEnchant(Enchantment.DEPTH_STRIDER, level, true);
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
            Long expiry = theFrostBiteCooldowns.get(uuid);
            if (expiry != null && now < expiry) {
                return;
            }
            theFrostBiteCooldowns.put(uuid, now + (20 * 10000));
            IceDomeManager.start(player);
    }

    @Override
    public List<Ability> getAbilities(Player player) {
        UUID u = player.getUniqueId();
        if (PlayerData.getPlayerScrollData(player).getRuneEssence() <= 0) return List.of(new Ability("§l----- LOST SCROLL -----", "\uE800", () -> 0L));
        if (PlayerData.getPlayerScrollData(player).isGrimoire()) {
            return List.of(
                    new Ability(
                            "§3§lFisherman's Trap§r",
                            "\uE005",
                            () -> fishermansTrapCooldowns.getOrDefault(u, 0L)
                    ),
                    new Ability(
                            "§3§lThe Frost Bite§r",
                            "\uE006",
                            () -> theFrostBiteCooldowns.getOrDefault(u, 0L)
                    )
            );
        } else {
            return List.of(
                    new Ability(
                            "§3§lFisherman's Trap§r",
                            "\uE005",
                            () -> fishermansTrapCooldowns.getOrDefault(u, 0L)
                    )
            );
        }

    }

    @Override
    public Scroll cloneScroll() {
        return new SplashScroll();
    }
}
