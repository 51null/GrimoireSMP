package noah.grimoireSMP.scroll.types;

import noah.grimoireSMP.Ability;
import noah.grimoireSMP.GrimoireSMP;
import noah.grimoireSMP.PlayerData;
import noah.grimoireSMP.PlayerScrollData;
import noah.grimoireSMP.scroll.Scroll;
import noah.grimoireSMP.scroll.ScrollLoreUtil;
import noah.grimoireSMP.scrollmanagers.astral.AstralProjectionManager;
import noah.grimoireSMP.scrollmanagers.astral.StoredMobData;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;

import static noah.grimoireSMP.AbilityDamageUtil.dealAbilityDamage;

public class AstralScroll extends Scroll {

    private static final NamespacedKey KEY_DAGGER =
            new NamespacedKey(GrimoireSMP.getInstance(), "astral_dagger");

    private final Map<UUID, Long> daggerCooldowns = new HashMap<>();
    private final Map<UUID, Long> grimoireCooldowns = new HashMap<>();

    public AstralScroll() {
        super(
            "Astral Scroll",
            111,
            112
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
            Long expiry = daggerCooldowns.get(uuid);
            if (expiry != null && now < expiry) {
                return;
            }
            daggerCooldowns.put(uuid, now + (55 * 1000));
            PlayerScrollData data = PlayerData.getPlayerScrollData(player);
            if (data == null || data.getScroll().isLost()) return;


            int count     = Math.min(5, data.getRuneEssence());
            long interval = 4L;    // ticks between each dagger spawn
            double speed  = 1.2;   // blocks per tick

            for (int i = 0; i < count; i++) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        spawnFlyingDagger(player, speed);
                    }
                }.runTaskLater(GrimoireSMP.getInstance(), i * interval);
            }
        }
        if (abilityNumber == 2) {
            PlayerScrollData data = PlayerData.getPlayerScrollData(player);
            if (data == null || data.getScroll().isLost()) return;

            // Compute capacity
            int essence = data.getRuneEssence();
            int maxStore = data.isGrimoire()
                    ? 3
                    : (essence >= 4 ? 2 : 1);

            // Ray-trace for a mob in front (up to 7 blocks)
            Location eye = player.getEyeLocation();
            Vector dir   = eye.getDirection();
            RayTraceResult entHit = player.getWorld().rayTraceEntities(
                    eye, dir, 7,
                    e -> e instanceof LivingEntity && !(e instanceof Player)
            );

            if (entHit != null && entHit.getHitEntity() instanceof LivingEntity mob) {
                // --- STORE ---
                EntityType type = mob.getType();

                // NEW: prevent certain bosses from being stored
                if (BLOCKED.contains(type)) {
                    player.sendMessage("§cYou cannot store a "
                            + type.name().toLowerCase().replace('_',' ') + " in your Astral Pocket!");
                    return;
                }

                StoredMobData snap = new StoredMobData(mob);
                if (data.storeMob(snap, maxStore)) {
                    mob.remove();
                    player.sendMessage("§bStored “"
                            + snap.getType().name().toLowerCase().replace('_',' ')
                            + "” in your Astral Pocket! ("
                            + data.getPocket().size() + "/" + maxStore + ")");
                } else {
                    player.sendMessage("§cYour pocket is full ("
                            + maxStore + "). Spawn or drop one first.");
                }
            } else {
                // --- RETRIEVE & SPAWN ---
                StoredMobData snap = data.retrieveLastMob();
                if (snap == null) {
                    player.sendMessage("§cNo stored mob to summon!");
                    return;
                }

                // Find spawn location: ray-trace to block (7 blocks), else 5 out
                RayTraceResult blockHit = player.rayTraceBlocks(7);
                Location spawnLoc = (blockHit != null)
                        ? blockHit.getHitPosition().toLocation(player.getWorld())
                        : eye.clone().add(dir.multiply(5));

                // Spawn using our snapshot
                LivingEntity spawned = snap.spawn(spawnLoc);
                if (spawned != null) {
                    player.sendMessage("§aSummoned your stored “"
                            + snap.getType().name().toLowerCase().replace('_',' ')
                            + "”!");
                }
            }
        }
    }

    private static final Set<EntityType> BLOCKED = Set.of(
            EntityType.WARDEN,
            EntityType.WITHER,
            EntityType.ENDER_DRAGON,
            EntityType.ELDER_GUARDIAN
    );

    private void spawnFlyingDagger(Player player, double speed) {
        Location start = player.getEyeLocation();
        Vector dir     = start.getDirection().normalize();

        // 1) spawn the stand
        ArmorStand dagger = start.getWorld().spawn(start.add(0, -1, 0), ArmorStand.class, as -> {
            as.setVisible(false);
            as.setMarker(true);
            as.setGravity(false);
            as.setSmall(true);
            as.getEquipment().setHelmet(createDaggerStack());
            as.setHeadPose(new EulerAngle(-Math.PI / 0, 1.5, 0));
        });

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.0f, 1.5f);

        // 2) move it every tick
        new BukkitRunnable() {
            double traveled = 0;

            @Override
            public void run() {
                if (!dagger.isValid() || traveled > 50) {
                    dagger.remove();
                    cancel();
                    return;
                }

                // move along
                traveled += speed;
                Location loc = start.clone().add(dir.clone().multiply(traveled));
                dagger.teleport(loc);

                // collision vs blocks?
                if (loc.getBlock().getType().isSolid()) {
                    explodeDagger(loc, player);
                    dagger.remove();
                    cancel();
                    return;
                }

                // collision vs entities?
                for (Entity e : loc.getWorld().getNearbyEntities(loc, 0.5, 0.5, 0.5)) {
                    if (e instanceof LivingEntity target && !target.equals(player) && !e.getUniqueId().equals(dagger.getUniqueId())) {
                        // apply 5 damage bypassing invuln
                        dealAbilityDamage(player, target, 1.5);

                        // disable abilities for 8s
                        if (target instanceof Player p) {
                            PlayerScrollData tData = PlayerData.getPlayerScrollData(p);
                            if (tData != null) {
                                tData.extendAbilitiesDisabledBy(12_000L);
                                p.sendMessage("§cYour scroll is silenced for 12s!");
                            }
                        }

                        explodeDagger(loc, player);
                        dagger.remove();
                        cancel();
                        return;
                    }
                }
            }
        }.runTaskTimer(GrimoireSMP.getInstance(), 0, 1);

        // 3) auto-despawn after 3s in case it never hits anything
        new BukkitRunnable() {
            @Override
            public void run() {
                if (dagger.isValid()) dagger.remove();
            }
        }.runTaskLater(GrimoireSMP.getInstance(), 60L);
    }

    private ItemStack createDaggerStack() {
        ItemStack stk = new ItemStack(Material.ARROW);
        ItemMeta m    = stk.getItemMeta();
        m.setCustomModelData( 1001 );
        stk.setItemMeta(m);
        return stk;
    }

    private void explodeDagger(Location loc, Player shooter) {
        World w = loc.getWorld();
        w.spawnParticle(
                Particle.DUST,
                loc,
                30,     // count
                0.6, 0.6, 0.6, // offsets
                0.02,       // speed
                new Particle.DustOptions(Color.fromRGB(68, 30, 75), 2.5f)
        );

        for (Entity e : w.getNearbyEntities(loc, 2, 2, 2)) {
            if (e instanceof LivingEntity tgt && !tgt.equals(shooter)) {
                // small knockback
                Vector kb = tgt.getLocation().toVector()
                        .subtract(shooter.getLocation().toVector())
                        .normalize()
                        .multiply(0.3);
                tgt.setVelocity(kb);
            }
        }
    }

    @Override
    public void passiveAbilityStart(Player player) {
        if (PlayerData.getPlayerScrollData(player).getRuneEssence() <= 0) return;



    }
    @Override
    public void useGrimoireAbility(Player player) {
            if (!PlayerData.getPlayerScrollData(player).isGrimoire()) return;
            if (PlayerData.getPlayerScrollData(player).getRuneEssence() <= 0) return;
            /*
            UUID uuid = player.getUniqueId();
            long now = System.currentTimeMillis();
            Long expiry = grimoireCooldowns.get(uuid);
            if (expiry != null && now < expiry) {
                return;
            }
            grimoireCooldowns.put(uuid, now + (13 * 60000));
            AstralProjectionManager.startProjection(player);*/
    }

    @Override
    public List<Ability> getAbilities(Player player) {
        UUID u = player.getUniqueId();
        if (PlayerData.getPlayerScrollData(player).getRuneEssence() <= 0) return List.of(new Ability("§l----- LOST SCROLL -----", " ", () -> 0L));
        if (PlayerData.getPlayerScrollData(player).isGrimoire()) {
            return List.of(
                    new Ability(
                            "§5§lDagger Summon§r",
                            "\uE012",
                            () -> daggerCooldowns.getOrDefault(u, 0L)
                    ),
                    new Ability(
                            "§5§lAstral Projection§r",
                            "\uE013",
                            () -> grimoireCooldowns.getOrDefault(u, 0L)
                    )
            );
        } else {
            return List.of(
                    new Ability(
                            "§5§lDagger Summon§r",
                                "\uE012",
                            () -> daggerCooldowns.getOrDefault(u, 0L)
                    )
            );
        }


    }

    @Override
    public Scroll cloneScroll() {
        return new AstralScroll();
    }


}
