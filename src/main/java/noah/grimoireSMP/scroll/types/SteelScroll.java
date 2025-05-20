package noah.grimoireSMP.scroll.types;

import noah.grimoireSMP.*;
import noah.grimoireSMP.scroll.Scroll;
import noah.grimoireSMP.scroll.ScrollLoreUtil;
import noah.grimoireSMP.scrollmanagers.berserker.BloodMarkManager;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;

import static noah.grimoireSMP.AbilityDamageUtil.dealAbilityDamage;

public class SteelScroll extends Scroll {

    private final Map<UUID, Long> grappleCooldowns = new HashMap<>();
    private final Map<UUID, Long> bulletStormCooldowns = new HashMap<>();
    private final Map<UUID, Long> flashCanonCooldowns = new HashMap<>();

    public SteelScroll() {
        super(
                "Steel Scroll",
                109,
                110
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
            Long expiry = bulletStormCooldowns.get(uuid);
            if (expiry != null && now < expiry) {
                return;
            }
            bulletStormCooldowns.put(uuid, now + (80 * 1000L));
            PlayerScrollData data = PlayerData.getPlayerScrollData(player);
            int count = Math.min(5, data.getRuneEssence());
            if (count <= 0) return;

            final double radius     = 1.5;
            final double yOffset    = 1.5;
            final long   spawnInt   = 4L;  // ticks between blob spawns
            final long   shootInt   = 3L;  // ticks between shots
            final long   buffer     = 3L;  // small pause before shooting

            // Store each blob’s offset & its repeating particle‑task
            List<Vector>          offsets = new ArrayList<>();
            List<BukkitRunnable>  tasks   = new ArrayList<>();

            World world = player.getWorld();
            NamespacedKey key = new NamespacedKey(GrimoireSMP.getInstance(), "bullet_storm"); // if you still need PDC

            // PHASE A: Spawn blobs in a vertical circle around the player
            for (int i = 0; i < count; i++) {
                final int idx = i;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        // 1) Compute forward & right in horizontal plane
                        Vector forward = player.getEyeLocation()
                                .getDirection()
                                .clone()
                                .setY(0)
                                .normalize();
                        Vector up      = new Vector(0, 1, 0);
                        Vector right   = forward.clone().crossProduct(up).normalize();

                        // 2) Compute angle and offsets
                        double angle = 2 * Math.PI * idx / count;
                        double xOff  = Math.cos(angle) * radius;
                        double yOff  = Math.sin(angle) * radius;

                        // 3) Build final offset: (right * xOff) + (up * yOff)
                        Vector offset = right.clone().multiply(xOff)
                                .add(up.clone().multiply(yOff)).add(new Vector (0, 2, 0));

                        // 4) Store and start the follow‐task

                        offsets.add(offset);
                        BukkitRunnable task = new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (!player.isOnline()) {
                                    cancel();
                                    return;
                                }
                                Location loc = player.getLocation().add(offset);
                                    player.getWorld().playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_BREAK, 4f, 0.8f);
                                    player.getWorld().playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_BREAK, 4f, 1.8f);
                                world.spawnParticle(
                                        Particle.DUST,
                                        loc, 12,
                                        0.1, 0.1, 0.1, 0.02,
                                        new Particle.DustOptions(Color.GRAY, 2.2f)
                                );
                            }
                        };
                        task.runTaskTimer(GrimoireSMP.getInstance(), 0L, 1L);
                        tasks.add(task);
                    }
                }.runTaskLater(GrimoireSMP.getInstance(), idx * spawnInt);
            }


            // PHASE B: Shoot them at the *current* aim, one by one
            long shootStart = count * spawnInt + buffer;
            for (int i = 0; i < count; i++) {
                final int idx = i;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        // 1) Stop the blob’s particle task
                        tasks.get(idx).cancel();

                        // 2) Compute the blob’s current world position
                        Vector offset = offsets.get(idx);
                        Location start = player.getLocation().add(offset);

                        // 3) Re‑raytrace to get current target direction
                        RayTraceResult ray = player.rayTraceBlocks(50);
                        Location tgt = ray != null
                                ? ray.getHitPosition().toLocation(world)
                                : player.getEyeLocation().add(player.getLocation().getDirection().multiply(30));
                        Vector dir = tgt.toVector().subtract(start.toVector()).normalize();

                        // 4) Fire a fast particle beam
                        launchParticleBeam(
                                player, start, dir,
                                /*speed*/   6.0,
                                /*maxDist*/ 200.0,
                                /*damage*/  1.0,
                                /*explRad*/ 3.0
                        );
                    }
                }.runTaskLater(GrimoireSMP.getInstance(), shootStart + i * shootInt);
            }
        }

        if (abilityNumber == 2) {
            UUID uuid = player.getUniqueId();
            long now = System.currentTimeMillis();
            Long expiry = grappleCooldowns.get(uuid);
            if (expiry != null && now < expiry) {
                return;
            }
            grappleCooldowns.put(uuid, now + (12 * 1000L));

            Location eye = player.getEyeLocation();
            Vector dir = eye.getDirection();
            int rayrange;
            if (PlayerData.getPlayerScrollData(player).getRuneEssence() >= 5) {
                rayrange = 50;
            } else rayrange = (PlayerData.getPlayerScrollData(player).getRuneEssence() * 10);

            // 1) Ray‑trace blocks
            RayTraceResult blockHit = player.rayTraceBlocks(rayrange);
            double blockDist = (blockHit != null)
                    ? blockHit.getHitPosition().distance(eye.toVector())
                    : Double.MAX_VALUE;

            // 2) Ray‑trace entities (players only)
            RayTraceResult entHit = player.getWorld().rayTraceEntities(
                    eye, dir, rayrange,
                    e -> e instanceof Player && !e.equals(player)
            );
            double entDist = (entHit != null)
                    ? entHit.getHitPosition().distance(eye.toVector())
                    : Double.MAX_VALUE;

            // 3) Pick the nearest
            RayTraceResult hit = (blockDist <= entDist ? blockHit : entHit);
            if (hit == null) {
                player.sendMessage("§cNo valid target for Cyborg Arm!");
                return;
            }

            Vector hitVec = hit.getHitPosition();
            Player target = (entHit != null && hit == entHit)
                    ? (Player) entHit.getHitEntity()
                    : null;

            // 4) Spawn a quick particle beam
            double maxDist = hitVec.distance(eye.toVector());
            for (double d = 0; d <= maxDist; d += 0.2) {
                Location loc = eye.clone().add(dir.clone().multiply(d));
                Particle.DustOptions options = new Particle.DustOptions(Color.fromRGB(61, 61, 61 ), 1.5f); // smaller = faster fade
                player.getWorld().spawnParticle(Particle.DUST, loc, 2, 0, 0, 0, 1, options);
            }

            // 5) Handle each case
            if (target != null) {
                boolean ally = AllyManager.isAlly(player.getUniqueId(), target.getUniqueId());
                if (ally) {
                    // Pull caster *to* the ally
                    continuousPull(player, target.getLocation().toVector(), 1.2, 1.5);
                } else {
                    double peakHeight;
                    if (PlayerData.getPlayerScrollData(player).getRuneEssence() >= 5) {
                        peakHeight = 10;
                    } else peakHeight = (PlayerData.getPlayerScrollData(player).getRuneEssence() * 2);
                    syncGrappleEnemy(player, target, peakHeight);
                }
            } else {
                Location dest = hitVec.toLocation(player.getWorld());
                continuousPull(player, hitVec, 1.2, 1.5);
            }
        }
    }

    private void continuousPull(Player who,
                                Vector dest,
                                double speed,
                                double stopDistance) {
        final boolean[] cancel = { false };
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!who.isOnline()) { cancel(); return; }


                Vector current = who.getLocation().toVector();
                Vector diff    = dest.clone().subtract(current);
                double dist    = diff.length();

                if (dist <= stopDistance || cancel[0]) {
                    who.setVelocity(new Vector(0,0,0));
                    cancel();
                    return;
                }

                // Normalize diff and multiply by speed to get velocity
                Vector vel = diff.normalize().multiply(speed);
                who.setVelocity(vel);
            }
        }.runTaskTimer(GrimoireSMP.getInstance(), 0L, 1L);

        new BukkitRunnable() {
            @Override
            public void run() {
                cancel[0] = true;
            }
        }.runTaskLater(GrimoireSMP.getInstance(), 50L);
    }



    private void syncGrappleEnemy(Player caster, Player target, double peakHeight) {
        // 1) Physics constants & compute time to apex
        double g = 0.08;                              // server gravity
        double vY = Math.sqrt(2 * g * peakHeight);    // initial vertical speed
        long   tApex = Math.round(vY / g);            // ticks to reach peak

        // 2) Launch target upward
        target.setVelocity(new Vector(0, vY, 0));

        // 3) Compute apex location (straight above current target position)
        Vector tPos   = target.getLocation().toVector();
        Vector apex   = tPos.clone().add(new Vector(0, peakHeight, 0));

        // 4) Calculate caster pull speed so they arrive in tApex ticks
        Vector cPos   = caster.getLocation().toVector();
        double dist   = apex.clone().subtract(cPos).length();
        double speed  = dist / tApex;   // blocks per tick

        // 5) Start pulling caster toward apex immediately
        continuousPull(caster, apex, speed, /*stopDist*/1.5);

        // 6) After apex time, teleport both into place and zero motion
        new BukkitRunnable() {
            @Override
            public void run() {

                caster.setVelocity(new Vector(0,0,0));
                target.setVelocity(new Vector(0,0,0));
            }
        }.runTaskLater(GrimoireSMP.getInstance(), tApex);
    }

    /** Shoots a single particle bullet from `start` along `dir`. */
    private void launchParticleBeam(Player caster,
                                    Location start,
                                    Vector dir,
                                    double speed,
                                    double maxDist,
                                    double damage,
                                    double explosionRadius) {
        start.getWorld().playSound(start, Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 4f, 1.15f);
        new BukkitRunnable() {
            double traveled = 0;

            @Override
            public void run() {
                if (!caster.isOnline() || traveled > maxDist) {
                    cancel();
                    return;
                }
                traveled += speed;
                Location pos = start.clone().add(dir.clone().multiply(traveled));
                World world = pos.getWorld();


                // 2) Loop along the line
                for (double d = -speed; d <= 0; d += 0.2) {
                    // 3) Compute the point at distance 'd'
                    Location point = pos.clone().add(dir.clone().multiply(d));
                    // 4) Spawn a particle (count=1, no offset)
                    world.spawnParticle(
                            Particle.DUST,
                            point, 3, 0,0,0,0,
                            new Particle.DustOptions(Color.GRAY, 2f)
                    );
                }

                // draw bullet
                world.spawnParticle(
                        Particle.DUST,
                        pos, 3, 0,0,0,0,
                        new Particle.DustOptions(Color.GRAY, 2f)
                );

                // block collision?
                if (pos.getBlock().getType().isSolid()) {
                    explodeAt(world, pos, caster, damage, explosionRadius);
                    cancel();
                    return;
                }
                // entity collision?
                for (Entity e : world.getNearbyEntities(pos, 0.5,0.5,0.5)) {
                    if (e instanceof LivingEntity tgt && !tgt.equals(caster)) {
                        explodeAt(world, pos, caster, damage, explosionRadius);
                        cancel();
                        return;
                    }
                }
            }
        }.runTaskTimer(GrimoireSMP.getInstance(), 0L, 1L);
    }

    private void explodeAt(World world,
                           Location loc,
                           Player caster,
                           double damage,
                           double radius) {
        world.spawnParticle(Particle.EXPLOSION, loc, 1, 0,0,0,0);
        world.spawnParticle(
                Particle.DUST,
                loc, 120, 2.2,2.2,2.2,0.06,
                new Particle.DustOptions(Color.GRAY, 30f)
        );
        world.spawnParticle(
                Particle.DUST,
                loc, 40, 2.2,2.2,2.2,0.06,
                new Particle.DustOptions(Color.RED, 30f)
        );
        double ting = radius + 4.0;
        for (Entity e : world.getNearbyEntities(loc, ting, ting, ting)) {
            if (e instanceof LivingEntity tgt && !tgt.equals(caster)) {
                dealAbilityDamage(caster, tgt, damage + 0.7);
                Vector kb = tgt.getLocation().toVector()
                        .subtract(caster.getLocation().toVector())
                        .normalize()
                        .multiply(0.3);
                tgt.setVelocity(kb);
            }
        }
    }



    @Override
    public void passiveAbilityStart(Player player) {
        if (PlayerData.getPlayerScrollData(player).getRuneEssence() <= 0) return;
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 30, 0, true, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 30, 0, true, false, true));
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
        Long expiry = flashCanonCooldowns.get(uuid);
        if (expiry != null && now < expiry) {
            return;
        }
        flashCanonCooldowns.put(uuid, now + (180 * 1000L));

        World world = player.getWorld();
        Location eye = player.getEyeLocation();
        Vector dir   = eye.getDirection().normalize();
        double maxDist = 30;
        double step    = 0.5;

        // Track who we’ve already hit
        Set<LivingEntity> hit = new HashSet<>();

        // 1) Beam sweep
        for (double d = 0; d <= maxDist; d += step) {
            Location loc = eye.clone().add(dir.clone().multiply(d));

            // Particle beam
            world.spawnParticle(
                    Particle.END_ROD,
                    loc, 120,        // count
                    1,1,1,   // spread
                    0.01           // speed
            );
            world.spawnParticle(
                    Particle.DUST,
                    loc,
                    60,     // count
                    0.4, 0.4, 0.4, // offsets
                    0.01,       // speed
                    new Particle.DustOptions(Color.fromRGB(211, 211, 211), 2f)
            );

            // Stop on solid block
            if (loc.getBlock().getType().isSolid()) break;

            // Check for entities at this point
            for (Entity e : world.getNearbyEntities(loc, 1.5, 2, 1.5)) {
                if (!(e instanceof LivingEntity tgt) || tgt.equals(player)) continue;
                if (hit.add(tgt)) {
                    // First time we hit this target
                    applyFlashCanonEffects(player, tgt);
                }
            }
        }

        // 2) Optionally, play a sound at the end of the beam
        world.playSound(eye, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.3f);
    }

    private void applyFlashCanonEffects(Player shooter, LivingEntity target) {
        // Heavy damage (8 hearts)
        dealAbilityDamage(shooter, target, 7);

        // Knockback directly away from the shooter
        Vector kb = target.getLocation().toVector()
                .subtract(shooter.getLocation().toVector())
                .normalize()
                .multiply(1.2);
        // give a little vertical lift so they fly back
        kb.setY(0.5);
        target.setVelocity(kb);

        // Slowness V for 2 seconds (40 ticks)
        target.addPotionEffect(new PotionEffect(
                PotionEffectType.SLOWNESS,
                40,    // ticks
                4,     // amplifier 4 = Slowness V
                false, false, true
        ));
        if (target instanceof Player p) {
            p.sendTitle("\uE999", "\uE999", 0, 100, 0);
            p.sendMessage("§lYou've been FLASHBANGED!");
            p.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.5f, 1.3f);

            new BukkitRunnable() {
                int t = 0;
                @Override
                public void run() {
                    if (t++ >= 100 || !target.isValid()) {
                        cancel();
                        return;
                    }
                    Location eye = target.getEyeLocation();
                    p.playSound(p.getLocation(), Sound.BLOCK_BELL_USE, 1.0f, 1.5f);
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.7f);
                    // spawn tons of white dust so their screen fills
                    p.getWorld().spawnParticle(
                            Particle.DUST,
                            eye,
                            60,     // count
                            0.2, 0.2, 0.2, // offsets
                            0,       // speed
                            new Particle.DustOptions(Color.WHITE, 3f)
                    );
                }
            }.runTaskTimer(GrimoireSMP.getInstance(), 0L, 1L);
        }
        // Flash‑bang effect: spawn a white cloud around their eyes for 20 ticks

    }

    @Override
    public List<Ability> getAbilities(Player player) {
        UUID u = player.getUniqueId();
        if (PlayerData.getPlayerScrollData(player).getRuneEssence() <= 0) return List.of(new Ability("§l----- LOST SCROLL -----", "\uE800", () -> 0L));
        if (PlayerData.getPlayerScrollData(player).isGrimoire()) {
            return List.of(
                    new Ability(
                            "§7§lCyborg Arm§r",
                            "\uE009",
                            () -> grappleCooldowns.getOrDefault(u, 0L)
                    ),
                    new Ability(
                            "§7§lBullet Storm§r",
                            "\uE010",
                            () -> bulletStormCooldowns.getOrDefault(u, 0L)
                    ),
                    new Ability(
                            "§7§lFlash Cannon§r",
                            "\uE011",
                            () -> flashCanonCooldowns.getOrDefault(u, 0L)
                    )
            );
        } else {
            return List.of(
                    new Ability(
                            "§7§lCyborg Arm§r",
                            "\uE009",
                            () -> grappleCooldowns.getOrDefault(u, 0L)
                    ),
                    new Ability(
                            "§7§lBullet Storm§r",
                            "\uE010",
                            () -> bulletStormCooldowns.getOrDefault(u, 0L)
                    )
            );
        }

    }
    @Override
    public Scroll cloneScroll() {
        return new SteelScroll();
    }
}


