package noah.grimoireSMP.scroll.types;

import noah.grimoireSMP.Ability;
import noah.grimoireSMP.AllyManager;
import noah.grimoireSMP.GrimoireSMP;
import noah.grimoireSMP.PlayerData;
import noah.grimoireSMP.scroll.Scroll;
import noah.grimoireSMP.scroll.ScrollLoreUtil;
import noah.grimoireSMP.scrollmanagers.celestial.CosmicShowerManager;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Location;
import org.bukkit.World;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.*;

import static noah.grimoireSMP.AbilityDamageUtil.dealAbilityDamage;

public class CelestialScroll extends Scroll {

    private final Map<UUID, Long> celestialAllCooldowns = new HashMap<>();
    private final Map<UUID, Long> auraCooldowns = new HashMap<>();
    private final Map<UUID, Long> grimoireCooldowns = new HashMap<>();
    private final Map<UUID, BukkitRunnable> activeHalos = new HashMap<>();

    public CelestialScroll() {
        super(
                "Celestial Scroll",
                107,
                108
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
                    Pair.of("§e§lStarlight§r", "Gain extra health under the daylight's sunlight")
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
                            "§f§lM2§r§7 - Boost allies’ defenses, granting Health, Regeneration and Resistance, and weaken foes in your proximity", 1),
                    new ScrollLoreUtil.AbilityInfo("§e§lCosmic Trail§r",
                            "§f§lQ§r§7 - Leave a dust trail that weakens and slow foes behind you", 1)
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
            if (PlayerData.getPlayerScrollData(player).isAbilitiesDisabled()) {
                player.sendMessage("§cYour scroll is silenced!");
                return;
            }
            UUID uuid = player.getUniqueId();
            long now = System.currentTimeMillis();
            Long expiry = celestialAllCooldowns.get(uuid);
            if (expiry != null && now < expiry) {
                return;
            }
            celestialAllCooldowns.put(uuid, now + (165 * 1000));
            final int radius = 8;

            new BukkitRunnable() {
                int tick = 0;
                @Override
                public void run() {
                    if (tick >= 130) {
                        cancel();
                        return;
                    }
                    Location center = player.getLocation();
                    for (int dis = 0; dis < radius; dis++) {
                        for (int deg = 0; deg < 360; deg += 10) {
                            double rad = Math.toRadians(deg);
                            double x = Math.cos(rad) * dis;
                            double z = Math.sin(rad) * dis;
                            Location spawn = center.clone().add(x, 0, z);
                            // a small burst of starlight
                            /*player.getWorld().spawnParticle(
                                    Particle.END_ROD,
                                    spawn.add(0, 0.1, 0),
                                    2,        // count
                                    0, 0, 0,  // offset
                                    0.01      // extra speed
                            );*/
                            Particle.DustOptions options = new Particle.DustOptions(Color.fromRGB(253, 255, 178 ), 1f); // smaller = faster fade
                            player.getWorld().spawnParticle(Particle.DUST, spawn.add(0, 0.1, 0), 2, 0, 0, 0, 0.01, options);
                        }
                    }


                    // 2) Apply buffs/debuffs
                    player.getWorld()
                            .getNearbyEntities(center, radius, radius, radius)
                            .forEach(ent -> {
                                if (!(ent instanceof Player) || ent == player) return;
                                Player target = (Player) ent;

                                if (AllyManager.isAlly(player.getUniqueId(), target.getUniqueId())
                                        || target.equals(player)) {
                                    if (tick <= 3) target.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 5, 99, false, true, false));
                                    target.addPotionEffect(new PotionEffect(
                                            PotionEffectType.HEALTH_BOOST,  // gives extra hearts
                                            10 * 20,   // 10 seconds
                                            1,         // level 2 (4 hearts)
                                            false, false, false
                                    ));
                                    target.addPotionEffect(new PotionEffect(
                                            PotionEffectType.RESISTANCE,
                                            10 * 20,   // 10 seconds
                                            0,         // Resistance I
                                            false, false, true
                                    ));
                                } else {
                                    // Non‑allies get Wither I and Weakness I
                                    target.addPotionEffect(new PotionEffect(
                                            PotionEffectType.WITHER,
                                            10 * 20,    // 8 seconds
                                            0,         // Wither I
                                            false, false, true
                                    ));
                                    target.addPotionEffect(new PotionEffect(
                                            PotionEffectType.WEAKNESS,
                                            10 * 20,    // 8 seconds
                                            0,         // Weakness I
                                            false, false, true
                                    ));
                                }
                            });

                    tick++;
                }
            }.runTaskTimer(GrimoireSMP.getInstance(), 0L, 1L);


        }
        if (abilityNumber == 2) {
            UUID uuid = player.getUniqueId();
            long now = System.currentTimeMillis();
            Long expiry = auraCooldowns.get(uuid);
            if (expiry != null && now < expiry) {
                return;
            }
            auraCooldowns.put(uuid, now + (135 * 1000));
            player.sendMessage("§eCELESTIAL TRAIL §r§aENABLED!");
            new BukkitRunnable() {
                int tick1 = 0;
                @Override
                public void run() {
                    if (tick1 >= 5) {
                        cancel();
                        return;
                    }
                    new BukkitRunnable() {
                        int tick = 0;
                        Location loc = player.getLocation();
                        @Override
                        public void run() {
                            if (tick >= 60) {
                                cancel();
                                return;
                            }

                            Particle.DustTransition dustTransition = new Particle.DustTransition(Color.fromRGB(241, 227, 164), Color.fromRGB(250, 249, 208), 2.0F);
                            player.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, loc, 1, 0.6, 0.125, 0.6, 0.01, dustTransition);
                            player.getWorld().spawnParticle(Particle.WAX_OFF, loc, 2, 1, 0.1, 1, 0.05);
                            player.getWorld().spawnParticle(Particle.WAX_ON, loc, 1, 1, 0.1, 1, 0.05);
                            player.getWorld().spawnParticle(Particle.END_ROD, loc, 1, 1, 0.1, 1, 0.05);


                            for (Entity entity : player.getWorld().getNearbyEntities(loc, 1, 3, 1)) {
                                // Skip the caster.
                                if (entity.equals(player)) continue;
                                // Only damage living entities.
                                if (!(entity instanceof LivingEntity) || AllyManager.isAlly(player.getUniqueId(), entity.getUniqueId())) continue;

                                LivingEntity target = (LivingEntity) entity;
                                target.damage(1, player);
                                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 30, 1, true, true, true));
                            }
                            tick++;
                        }
                    }.runTaskTimer(GrimoireSMP.getInstance(), 0L, 1L);
                    tick1++;
                }
            }.runTaskTimer(GrimoireSMP.getInstance(), 0L, 30L);
        }
    }

    @Override
    public void passiveAbilityStart(Player player) {
        if (PlayerData.getPlayerScrollData(player).getRuneEssence() <= 0) return;
        if (PlayerData.getPlayerScrollData(player).isAbilitiesDisabled()) {
            return;
        }
        startAnimatedHalo(player);
        World world = player.getWorld();
        long time = world.getTime();
        if (time >= 23000 || time >= 0 && time < 12500) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, 30, 1, true, true, false));
        }
    }

    private void startAnimatedHalo(Player player) {
        UUID uuid = player.getUniqueId();

        if (activeHalos.containsKey(uuid)) {
            return; // Already running
        }

        BukkitRunnable runnable = new BukkitRunnable() {
            double t = 0;

            @Override
            public void run() {
                if (!player.isOnline() || player.isDead()) {
                    this.cancel();
                    activeHalos.remove(uuid); // Cleanup on cancel
                    return;
                }

                Location baseLoc = player.getLocation().clone().add(0, 2.1, 0);
                int points = 25;
                double dynamicRadius = 0.4 + Math.sin(t) * 0.06;
                double yOffset = Math.sin(t * 0.4) * 0.06;

                for (int i = 0; i < points; i++) {
                    double angle = 2 * Math.PI * i / points;
                    double x = Math.cos(angle) * dynamicRadius;
                    double z = Math.sin(angle) * dynamicRadius;

                    Location particleLoc = baseLoc.clone().add(x, yOffset, z);
                    Particle.DustOptions options = new Particle.DustOptions(Color.fromRGB(253, 255, 178 ), 0.5f); // smaller = faster fade
                    particleLoc.getWorld().spawnParticle(Particle.DUST, particleLoc, 2, 0, 0, 0, 1, options);
                    //player.getWorld().spawnParticle(Particle.END_ROD, particleLoc, 0, 0, 0, 0, 0);
                }

                t += 0.2;
            }
        };
        runnable.runTaskTimer(GrimoireSMP.getInstance(), 0L, 1L);
        activeHalos.put(uuid, runnable);
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
                Long expiry = grimoireCooldowns.get(uuid);
                if (expiry != null && now < expiry) {
                    return;
                }
                grimoireCooldowns.put(uuid, now + (500 * 1000));
            var ray = player.rayTraceBlocks(100);
            Location center = ray != null
                    ? ray.getHitPosition().toLocation(player.getWorld())
                    : player.getLocation().add(player.getLocation().getDirection().multiply(30));
            CosmicShowerManager.spawnShower(player, center, 32, 25, 6L, 2);

    }


    @Override
    public List<Ability> getAbilities(Player player) {
        UUID u = player.getUniqueId();
        if (PlayerData.getPlayerScrollData(player).getRuneEssence() <= 0) return List.of(new Ability("§l----- LOST SCROLL -----", " ", () -> 0L));
        if (PlayerData.getPlayerScrollData(player).isGrimoire()) {
            return List.of(
                    new Ability(
                            "§e§lCelestial Alignment§r",
                            "\uE007",
                            () -> celestialAllCooldowns.getOrDefault(u, 0L)
                    ),
                    new Ability(
                            "§e§lCelestial Trail§r",
                            "\uE018",
                            () -> auraCooldowns.getOrDefault(u, 0L)
                    ),
                    new Ability(
                            "§e§lCosmic Shower§r",
                            "\uE008",
                            () -> grimoireCooldowns.getOrDefault(u, 0L)
                    )
            );
        } else {
            return List.of(
                    new Ability(
                            "§e§lCelestial Alignment§r",
                            "\uE007",
                            () -> celestialAllCooldowns.getOrDefault(u, 0L)
                    ),
                    new Ability(
                            "§e§lCelestial Trail§r",
                            "\uE018",
                            () -> auraCooldowns.getOrDefault(u, 0L)
                    )
            );
        }


    }

    @Override
    public Scroll cloneScroll() {
        return new CelestialScroll();
    }
}
