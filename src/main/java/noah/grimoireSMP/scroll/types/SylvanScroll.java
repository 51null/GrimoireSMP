package noah.grimoireSMP.scroll.types;

import noah.grimoireSMP.*;
import noah.grimoireSMP.scroll.Scroll;
import noah.grimoireSMP.scroll.ScrollLoreUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

import static noah.grimoireSMP.AbilityDamageUtil.dealAbilityDamage;

public class SylvanScroll extends Scroll {

    private int flameAuraCountdown = 10;
    private final Map<UUID, Long> fireballCooldowns = new HashMap<>();
    private final Map<UUID, Long> grimoireCooldowns = new HashMap<>();
    private final Map<UUID, Long> flameAuraCooldowns = new HashMap<>();
    private final Map<UUID, BukkitRunnable> infernalTasks = new HashMap<>();
    private static final Map<UUID, Boolean> vineSnare = new HashMap<>();

    public SylvanScroll() {
        super(
            "Sylvan Scroll",
            115,
            116
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

    public static void setvineSnateActive(Player player, boolean isActive) {
        vineSnare.put(player.getUniqueId(), isActive);
    }
    public static boolean vineSnare(Player player) {
        return vineSnare.getOrDefault(player.getUniqueId(), false);
    }
    @Override
    public void useActiveAbility(int abilityNumber, Player player) {
        if (PlayerData.getPlayerScrollData(player).getRuneEssence() <= 0) return;
        if (abilityNumber == 1) {
            if (PlayerData.getPlayerScrollData(player).isAbilitiesDisabled()) {
                player.sendMessage("§cYour scroll is silenced!");
                return;
            }
            UUID uuid = player.getUniqueId();
            /*
            // Check cooldown
                long now = System.currentTimeMillis();
                Long expiry = fireballCooldowns.get(uuid);
                if (expiry != null && now < expiry) {
                    return;
                }
                fireballCooldowns.put(uuid, now + (80 * 1000L));*/

            PlayerScrollData data = PlayerData.getPlayerScrollData(player);
            int essence = Math.min(data.getRuneEssence(), 5);
            int vinesCount = essence * 2;
            boolean sideMode = vineSnare(player); // your boolean

            Vector forward = player.getLocation().getDirection().clone().setY(0).normalize();
            Vector sideways = forward.clone().crossProduct(new Vector(0,1,0)).normalize().multiply(0.5);

            // Center of vine spawn (at chest height)
            Location base = player.getLocation().add(0, 0.5, 0);
            base.getWorld().playSound(base, Sound.ENTITY_SNIFFER_SNIFFING, 3f, 1.0f);
            base.getWorld().playSound(base, Sound.ENTITY_SNIFFER_SNIFFING, 3f, 1.2f);
            // Build all vine configs
            for (int i = 0; i < vinesCount; i++) {
                Location start;
                Vector dir;

                if (sideMode) {
                    double mid = (vinesCount - 1) / 2.0;
                    Vector offset = sideways.clone().multiply(i*2 - mid*2);
                    start = base.clone().add(offset);
                    dir   = forward;
                } else {
                    // circle mode: evenly spaced angles
                    double angle = 2 * Math.PI * i / vinesCount;
                    dir = new Vector(
                            Math.cos(angle) * forward.getX() - Math.sin(angle) * forward.getZ(),
                            0,
                            Math.sin(angle) * forward.getX() + Math.cos(angle) * forward.getZ()
                    ).normalize();
                    start = base.clone();
                }

                // hand off to helper, staggering each by 2 ticks
                int delay = i;
                spawnVine(start, dir, delay, player);
            }
        }
        if (abilityNumber == 2) {
            if (vineSnare(player)) {
                setvineSnateActive(player, false);
            } else setvineSnateActive(player, true);

        }
    }
private void spawnVine(Location start, Vector dir, int initialDelay, Player caster) {
    new BukkitRunnable() {
        double traveled = 0;
        final double maxDist = 25;
        final double step    = 0.5;

        @Override
        public void run() {
            if (traveled > maxDist) {
                cancel();
                return;
            }
            Particle.DustOptions dust1 = new Particle.DustOptions(Color.fromRGB(101,67,33), 2.3f);
            Particle.DustOptions dust2 = new Particle.DustOptions(Color.fromRGB(90, 134, 38), 1.7f);
            // 1) Compute the vine’s current point with a sine‐wave Y
            double wave = Math.sin(traveled * 2) * 0.3;
            Location point = start.clone()
                    .add(dir.clone().multiply(traveled))
                    .add(0, wave, 0);

            // 2) Spawn ground particles
            point.getWorld().spawnParticle(
                    Particle.DUST, point, 6, 0.02,0.02,0.02, 0.03, dust1
            );
            point.getWorld().spawnParticle(
                    Particle.DUST, point, 4, 0.02,0.02,0.02, 0.03, dust2
            );

            // 3) Check for any living entity hit
            for (Entity e : point.getWorld().getNearbyEntities(point, 0.5, 10.0, 0.5)) {
                if (!(e instanceof LivingEntity target)) continue;
                if (target == caster) continue;
                // Snare it
                target.addPotionEffect(new PotionEffect(
                        PotionEffectType.SLOWNESS,
                        70,  // 3.5s * 20
                        9,   // Slowness X → amplifier 9
                        true,false,false
                ));

                // Foot‐particle animation for duration
                new BukkitRunnable() {
                    int t = 0;
                    @Override public void run() {
                        if (t++ > 70) { cancel(); return; }
                        Location foot = target.getLocation().add(0, 0.2, 0);
                        target.getWorld().spawnParticle(
                                Particle.DUST, foot, 3, 0.2,0.6,0.2, 0.02, dust1
                        );
                        target.getWorld().spawnParticle(
                                Particle.DUST, foot, 3, 0.2,0.6,0.2, 0.02, dust2
                        );

                    }
                }.runTaskTimer(GrimoireSMP.getInstance(), 0L, 1L);

                cancel();  // stop this vine
                return;
            }

            traveled += step;
        }
    }
            .runTaskTimer(GrimoireSMP.getInstance(), initialDelay, 1L);
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
                long now = System.currentTimeMillis();
                Long expiry = grimoireCooldowns.get(uuid);
                if (expiry != null && now < expiry) {
                    return;
                }
                grimoireCooldowns.put(uuid, now + (184 * 1000));
        final JavaPlugin plugin = GrimoireSMP.getInstance();

        // 1) Apply a 2s charge with Slowness IV
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 3, false, false, false));
        player.sendMessage("§aChanneling Overgrowth…");
        Particle.DustOptions dust1 = new Particle.DustOptions(Color.fromRGB(101,67,33), 25f);
        Particle.DustOptions dust2 = new Particle.DustOptions(Color.fromRGB(90, 134, 38), 25f);
        player.getWorld().spawnParticle(
                Particle.DUST, player.getLocation(), 300, 5,2,5, 0.03, dust1
        );
        player.getWorld().spawnParticle(
                Particle.DUST, player.getLocation(), 200, 5,3,5, 0.03, dust2
        );

        // Optional: play particles or sounds here
        new BukkitRunnable() {
            @Override
            public void run() {
                Location center = player.getLocation().getBlock().getLocation();
                World world = center.getWorld();

                final int innerR    = 13;
                final int thickness = 3;
                final int height    = 8;
                final int sliceDelay= 2;

                // Helper to store each ring position + its fixed ground Y
                record RingPos(int dx, int dz, int groundY) {}

                // Capture originals and ring positions
                Map<Location, BlockData> original = new HashMap<>();
                List<RingPos> ring = new ArrayList<>();

                for (int dx = -innerR - thickness; dx <= innerR + thickness; dx++) {
                    for (int dz = -innerR - thickness; dz <= innerR + thickness; dz++) {
                        double dist = Math.hypot(dx, dz);
                        if (dist >= innerR && dist < innerR + thickness) {
                            Location base = center.clone().add(dx, 0, dz);
                            int gy = world.getHighestBlockYAt(base);
                            ring.add(new RingPos(dx, dz, gy));

                            for (int dy = 0; dy <= height; dy++) {
                                Location loc = base.clone();
                                loc.setY(gy + dy);
                                original.put(loc.clone(), loc.getBlock().getBlockData());
                            }
                        }
                    }
                }

                // Animate rising wall
                for (int layer = 0; layer <= height; layer++) {
                    final int dy = layer;
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            // play wood-place sound on first slice
                            if (dy == 0) {
                                world.playSound(center, Sound.BLOCK_WOOD_PLACE, 1f, 1f);
                            }
                            for (RingPos rp : ring) {
                                Location loc = center.clone()
                                        .add(rp.dx, 0, rp.dz);
                                loc.setY(rp.groundY + dy);

                                if (dy < height) {
                                    loc.getBlock().setType(Material.OAK_LOG, false);
                                } else {
                                    loc.getBlock().setType(Material.OAK_LEAVES, false);
                                }
                            }
                        }
                    }.runTaskLater(plugin, layer * sliceDelay);
                }

                // Decorate with random extras once fully up
                new BukkitRunnable() {
                    @Override public void run() {
                        Random rnd = new Random();
                        for (RingPos rp : ring) {
                            for (int offX : new int[]{-1, 1, 0, 0}) {
                                for (int offZ : new int[]{0, 0, -1, 1}) {
                                    if (rnd.nextDouble() < 0.2) {
                                        // Build an absolute location at (centerX + dx + offX,
                                        //                                    groundY,
                                        //                                    centerZ + dz + offZ)
                                        Location deco = new Location(
                                                world,
                                                center.getX() + rp.dx + offX,
                                                rp.groundY,        // absolute world‐Y
                                                center.getZ() + rp.dz + offZ
                                        );
                                        // Save original if not already saved
                                        original.putIfAbsent(deco.clone(),
                                                deco.getBlock().getBlockData());
                                        // Randomly place log or leaves
                                        Material m = rnd.nextBoolean()
                                                ? Material.OAK_LOG
                                                : Material.OAK_LEAVES;
                                        deco.getBlock().setType(m, false);
                                    }
                                }
                            }
                        }
                    }
                }.runTaskLater(plugin, height * sliceDelay + 2L);

                // Buff players inside ring
                for (Player p : world.getPlayers()) {
                    if (p.getLocation().distanceSquared(center) < innerR * innerR) {
                        boolean ally = AllyManager.isAlly(
                                player.getUniqueId(), p.getUniqueId()
                        ) || p.equals(player);

                        PotionEffect effect = ally
                                ? new PotionEffect(PotionEffectType.REGENERATION, 200, 0, false, false, false)
                                : new PotionEffect(PotionEffectType.SLOWNESS,         200, 1, false, false, false);

                        p.addPotionEffect(effect);
                    }
                }

                // Revert all blocks after 10s
                new BukkitRunnable() {
                    @Override public void run() {
                        original.forEach((loc, data) -> {
                            Material cur = loc.getBlock().getType();
                            if (cur == Material.OAK_LOG || cur == Material.OAK_LEAVES) {
                                loc.getBlock().setBlockData(data, false);
                            }
                        });
                    }
                }.runTaskLater(plugin, 200L);
            }
        }.runTaskLater(plugin, 40L);
    }

    @Override
    public List<Ability> getAbilities(Player player) {
        UUID u = player.getUniqueId();
        if (PlayerData.getPlayerScrollData(player).getRuneEssence() <= 0) return List.of(new Ability("§l----- LOST SCROLL -----", " ", () -> 0L));
        if (PlayerData.getPlayerScrollData(player).isGrimoire()) {
            return List.of(
                    new Ability(
                            "§6§lFlame Aura§r",
                            "\uE000",
                            () -> flameAuraCooldowns.getOrDefault(u, 0L)
                    ),
                    new Ability(
                            "§6§lFireball§r",
                            "\uE001",
                            () -> fireballCooldowns.getOrDefault(u, 0L)
                    ),
                    new Ability(
                            "§6§lInferno Storm§r",
                            "\uE002",
                            () -> grimoireCooldowns.getOrDefault(u, 0L)
                    )
            );
        } else {
            return List.of(
                    new Ability(
                            "§6§lFlame Aura§r",
                            "\uE000",
                            () -> flameAuraCooldowns.getOrDefault(u, 0L)
                    ),
                    new Ability(
                            "§6§lFireball§r",
                            "\uE001",
                            () -> fireballCooldowns.getOrDefault(u, 0L)
                    )
            );
        }


    }

    @Override
    public Scroll cloneScroll() {
        return new SylvanScroll();
    }
}
