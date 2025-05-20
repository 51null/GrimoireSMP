package noah.grimoireSMP.scroll.types;

import noah.grimoireSMP.Ability;
import noah.grimoireSMP.AllyManager;
import noah.grimoireSMP.GrimoireSMP;
import noah.grimoireSMP.PlayerData;
import noah.grimoireSMP.scroll.Scroll;
import noah.grimoireSMP.scroll.ScrollLoreUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

import static noah.grimoireSMP.AbilityDamageUtil.dealAbilityDamage;

public class InfernalScroll extends Scroll {

    private int flameAuraCountdown = 10;
    private final Map<UUID, Long> fireballCooldowns = new HashMap<>();
    private final Map<UUID, Long> grimoireCooldowns = new HashMap<>();
    private final Map<UUID, Long> flameAuraCooldowns = new HashMap<>();
    private final Map<UUID, BukkitRunnable> infernalTasks = new HashMap<>();
    private static final Map<UUID, Boolean> auraStates = new HashMap<>();

    public InfernalScroll() {
        super(
            "Infernal Scroll",
            101,
            102
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
                    Pair.of("§6§lFlame Aura§r", "Scorch nearby hostile players continuously - Deactivate with §f§lQ§r§7 - Minimum 3 Essence"),
                    Pair.of("§6§lEmber Resilience§r", "Fire occasionally heals you instead of harming you + Permanent Fire Resistance")
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
                    "§f§lSHIFT§r§7 - When you kill a player, theres a chance you recieve a §4BLOOD MARKED§r§7 armor piece, use this armor piece in the menu to track them",
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
        if (PlayerData.getPlayerScrollData(player).getRuneEssence() <= 0) return;
        if (abilityNumber == 1) {
            if (PlayerData.getPlayerScrollData(player).isAbilitiesDisabled()) {
                player.sendMessage("§cYour scroll is silenced!");
                return;
            }
            UUID uuid = player.getUniqueId();

            // Check cooldown
                long now = System.currentTimeMillis();
                Long expiry = fireballCooldowns.get(uuid);
                if (expiry != null && now < expiry) {
                    return;
                }
                fireballCooldowns.put(uuid, now + (80 * 1000L));



            // Begin charge effect
            player.sendMessage("§6Charging fireball...");
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 30, 9, false, true, false)); // Slowness 10 for 1.5 sec

            // Spawn armor stand holding a fireball
            Location spawnLoc = player.getEyeLocation().add(player.getLocation().getDirection().normalize().multiply(0.5));
            ArmorStand stand = (ArmorStand) player.getWorld().spawn(spawnLoc, ArmorStand.class);
            stand.setVisible(false);
            stand.setInvisible(true);
            stand.setMarker(true);
            stand.setGravity(false);
            stand.setCustomNameVisible(false);
            stand.setHelmet(new ItemStack(Material.FIRE_CHARGE)); // Display fireball
            stand.setRotation(player.getLocation().getYaw(), 0);


            BukkitRunnable task = new BukkitRunnable() {
                int ticks = 0;
                @Override
                public void run() {
                    Location Loc = player.getEyeLocation().add(player.getLocation().getDirection().normalize().multiply(1));
                    stand.teleport(Loc.add(0.0, -2.0, 0.0));
                    Location loc = player.getLocation();
                    Vector dir = loc.getDirection().normalize();

                    if (ticks == 0) player.getWorld().playSound(player.getLocation(), Sound.ITEM_FLINTANDSTEEL_USE, 80.0f, 0.5f);
                    player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BLASTFURNACE_FIRE_CRACKLE, 15.0f, 1.0f + (ticks / 15f));

                    World w = player.getWorld();
                    // We'll spawn some particles in a cone shape
                    int segments = 2; // how many lines
                    double angleSpread = Math.toRadians(25); // 30° cone half-angle
                    double step = angleSpread / segments;

                    for (int i = -segments; i <= segments; i++) {
                        double angle = i * step;
                        // rotate dir by angle around the Y-axis
                        Vector rotated = rotateVectorY(dir.clone(), angle).multiply(-1);
                        for (double dist = 0; dist <= 3; dist += 0.5) {
                            Location point = loc.clone().add(rotated.clone().multiply(dist));

                            w.spawnParticle(Particle.FLAME, point, 2, 0, 1, 0, 0.01);
                            w.spawnParticle(Particle.SOUL_FIRE_FLAME, point, 2, 0, 1, 0, 0.005);
                        }
                    }
                    ticks++;
                    if (ticks >= 15) {
                        this.cancel();
                    }
                }
            };
            task.runTaskTimer(GrimoireSMP.getInstance(), 0L, 2L);
            infernalTasks.put(uuid, task);


            new BukkitRunnable() {
                @Override
                public void run() {
            stand.remove();
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.3f, 2.5f);
            float fireballPower = PlayerData.getPlayerScrollData(player).getRuneEssence();
            if (fireballPower >= 5 ) {
                fireballPower = 3.5f;
            } else fireballPower = PlayerData.getPlayerScrollData(player).getRuneEssence() - 1.5f;
            // Launch fireball
            Fireball fireball = player.launchProjectile(Fireball.class);
            fireball.setYield(fireballPower); // Explosion strength
            fireball.setIsIncendiary(true);
            fireball.setShooter(player);


            }
        }.runTaskLater(GrimoireSMP.getInstance(), 30L); // 1.5 seconds later
        }
            if (abilityNumber == 2) {
                if (isAuraActive(player))
                {
                    setAuraActive(player, false);
                } else setAuraActive(player, true);

            }
    }
    public static void setAuraActive(Player player, boolean isActive) {
        auraStates.put(player.getUniqueId(), isActive);
    }
    public static boolean isAuraActive(Player player) {
        return auraStates.getOrDefault(player.getUniqueId(), false);
    }

    private Vector rotateVectorY(Vector v, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double x = v.getX() * cos - v.getZ() * sin;
        double z = v.getX() * sin + v.getZ() * cos;
        return new Vector(x, v.getY(), z);
    }

    @Override
    public void passiveAbilityStart(Player player) {
        if (PlayerData.getPlayerScrollData(player).getRuneEssence() <= 0) return;

        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 30, 0, true, false, true));

        if (PlayerData.getPlayerScrollData(player).getRuneEssence() <= 1) return;
        if (player.getFireTicks() > 0) { // player is on fire
            if (player.getHealth() >= 20) return;
            if (Math.random() < 0.1) { // 10% chance
                double currentHealth = player.getHealth();
                double maxHealth = player.getMaxHealth();
                double healAmount = 4.0; // healing 2 HP (1 heart)
                double newHealth = Math.min(currentHealth + healAmount, maxHealth);
                player.setHealth(newHealth);
                player.sendMessage("You feel rejuvenated while on fire!");
            }
        }
        if (PlayerData.getPlayerScrollData(player).getRuneEssence() < 3) return;
        if (!(isAuraActive(player))) return;
        flameAuraCountdown--;
        if (flameAuraCountdown <= 0) {
            Location loc = player.getLocation();
            World world = loc.getWorld();
            if (world == null) return;

            // Cosmetic effect: spawn flame particles around player
            world.spawnParticle(
                    Particle.FLAME,
                    loc.getX(), loc.getY() + 1.0, loc.getZ(),
                    400,  // count
                    4.0,  // offsetX
                    0,  // offsetY
                    4.0,  // offsetZ
                    0.2  // extra speed/spread
            );

            player.getWorld().playSound(loc, Sound.ENTITY_GHAST_SHOOT, 2f, 2f);

            // Damage + set fire to entities within 8-block radius
            for (Entity e : world.getNearbyEntities(loc, 8, 8, 8)) {
                if (e instanceof LivingEntity target && e != player && !AllyManager.isAlly(player.getUniqueId(), target.getUniqueId())) {
                    dealAbilityDamage(player, target, 2);
                    target.setFireTicks(200);     // set on fire for 5 seconds
                }
            }
            flameAuraCountdown = 10;
            UUID uuid = player.getUniqueId();
            flameAuraCooldowns.put(uuid, System.currentTimeMillis() + (10000));
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
                Long expiry = grimoireCooldowns.get(uuid);
                if (expiry != null && now < expiry) {
                    return;
                }
                grimoireCooldowns.put(uuid, now + (184 * 1000));
            // First, give the player Slowness V so that they are immobilized
            int abilityDurationTicks = 40; // e.g. ~4 seconds of immobilization
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, abilityDurationTicks, 4, false, true, true)); // Slowness level 5 (amplifier 4)
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, abilityDurationTicks, 4, false, true, true));
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GHAST_SHOOT, 5f, 0.5f);
            // Create a repeating task to form a spiral of flame particles and lift the player.
            final int spiralDurationTicks = 40;  // 2 seconds for spiral + lift effect
            new BukkitRunnable() {
                int tick = 0;
                @Override
                public void run() {
                    if (tick >= spiralDurationTicks) {
                        cancel();
                        // After the spiral/lift phase, execute the ground burst and expanding circle.
                        launchExpandingCircle(player);
                        return;
                    }

                    // Compute spiral parameters:
                    double angle = Math.toRadians((tick * 10) % 360); // Increase angle over time
                    double radius = 1.0; // fixed radius for the spiral
                    double offsetX = Math.cos(angle) * radius;
                    double offsetZ = Math.sin(angle) * radius;
                    double offsetY = (double) tick / spiralDurationTicks * 1.5; // gradually lift player 3 blocks

                    // Define the spiral location in front of the player's eye location
                    Location spiralLoc = player.getEyeLocation().clone().add(offsetX, offsetY, offsetZ);

                    // Spawn flame particles at spiralLoc
                    player.getWorld().spawnParticle(Particle.FLAME, spiralLoc, 10, 0.1, 0.1, 0.1, 0.01);
                    Particle.DustTransition dustTransition = new Particle.DustTransition(Color.RED, Color.ORANGE, 1.0F);
                    player.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, spiralLoc.add(0, -1, 0), 10, 0.3, 0.3, 0.3, 0.01, dustTransition);

                    // Gradually lift the player (set their Y coordinate) if desired.
                    Location playerLoc = player.getLocation();
                    playerLoc.setY(playerLoc.getY() + (3.0 / spiralDurationTicks));
                    player.teleport(playerLoc);
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.1f + ((tick / 2f) / 40f), 2 - (tick / 40f));
                    tick++;

                }
            }.runTaskTimer(GrimoireSMP.getInstance(), 0L, 1L);

            // Set the ability cooldown (not shown here; you need to store it as in your fireball ability).
            // For example: setCooldown(player, 80*1000 + 20*1000);

    }
    private void launchExpandingCircle(Player player) {
        World world = player.getWorld();
        Location origin = player.getLocation().add(0, 0.1, 0);

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 2.0f, 0.6f);
        // Now, launch an expanding circle. We'll have it expand over 2 seconds.
        final int circleDurationTicks = 20;
        new BukkitRunnable() {
            int tick = 0;
            @Override
            public void run() {
                if (tick >= circleDurationTicks) {
                    cancel();
                    return;
                }
                // The radius expands over time – starting at 1 and reaching about 10 blocks.
                double radius = 1 + (14.0 * ((double) tick / circleDurationTicks));
                for (double phi = Math.PI / 4; phi <= (3 * Math.PI / 4); phi += Math.toRadians(25)) {
                    // And theta covers the full circle.
                    for (double theta = 0; theta < 2 * Math.PI; theta += Math.toRadians(30)) {
                        double x = origin.getX() + radius * Math.sin(phi) * Math.cos(theta);
                        double y = origin.getY() + radius * Math.cos(phi);
                        double z = origin.getZ() + radius * Math.sin(phi) * Math.sin(theta);
                        Location particleLoc = new Location(world, x, y, z);
                        double spread = 0.1 * radius;
                        world.spawnParticle(Particle.FLAME, particleLoc, 6 + (tick / 4), spread, spread, spread, 0.05);
                        Particle.DustTransition dustTransition = new Particle.DustTransition(Color.RED, Color.ORANGE, 6.0F);
                        world.spawnParticle(Particle.DUST_COLOR_TRANSITION, particleLoc, 3 + (tick / 4), spread, spread, spread, 0.1, dustTransition);
                    }
                }

                for (Entity entity : world.getNearbyEntities(origin, radius, 30, radius)) {
                    // Skip the caster.
                    if (entity.equals(player)) continue;
                    // Only damage living entities.
                    if (!(entity instanceof LivingEntity)) continue;

                    LivingEntity target = (LivingEntity) entity;
                    //target.damage(45.0, player);
                    dealAbilityDamage(player, target, 1);
                    target.setFireTicks(130); // Sets target on fire for around 6.5 seconds (130 ticks)
                }
                tick++;
                if (tick == 10) player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 2.0f, 0.6f);
            }
        }.runTaskTimer(GrimoireSMP.getInstance(), 0L, 1L);
    }

    @Override
    public List<Ability> getAbilities(Player player) {
        UUID u = player.getUniqueId();
        if (PlayerData.getPlayerScrollData(player).getRuneEssence() <= 0) return List.of(new Ability("§l----- LOST SCROLL -----", " ", () -> 0L));
        if (PlayerData.getPlayerScrollData(player).isGrimoire() && isAuraActive(player)) {
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
        } else if (isAuraActive(player)) {
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
        } else if (PlayerData.getPlayerScrollData(player).isGrimoire()) {
            return List.of(
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
                            "§6§lFireball§r",
                            "\uE001",
                            () -> fireballCooldowns.getOrDefault(u, 0L)
                    )
            );
        }


    }

    @Override
    public Scroll cloneScroll() {
        return new InfernalScroll();
    }
}
