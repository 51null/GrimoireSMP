package noah.grimoireSMP.scroll.types;

import noah.grimoireSMP.scroll.ScrollLoreUtil;
import noah.grimoireSMP.scrollmanagers.berserker.BloodMarkManager;
import noah.grimoireSMP.Ability;
import noah.grimoireSMP.GrimoireSMP;
import noah.grimoireSMP.PlayerData;
import noah.grimoireSMP.PlayerScrollData;
import noah.grimoireSMP.scroll.Scroll;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.bukkit.enchantments.Enchantment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BerserkerScroll extends Scroll {

    private final Map<UUID, Long> fangsOfDemiseCooldowns = new HashMap<>();
    private final Map<UUID, Long> blank = new HashMap<>();

    public BerserkerScroll() {
        super(
                "Berserker Scroll",
                103,
                104
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
        if (abilityNumber == 1) {
            UUID uuid = player.getUniqueId();
            if (PlayerData.getPlayerScrollData(player).isAbilitiesDisabled()) {
                player.sendMessage("§cYour scroll is silenced!");
                return;
            }
            // Check cooldown
            long now = System.currentTimeMillis();
            Long expiry = fangsOfDemiseCooldowns.get(uuid);
            if (expiry != null && now < expiry) {
                return;
            }
            fangsOfDemiseCooldowns.put(uuid, now + (70 * 1000L));


            Location startLoc = player.getLocation().clone();
            startLoc.add(0, 0.1, 0); // keep it near the ground
            Vector dir = player.getLocation().getDirection().setY(0).normalize();

            final int maxSteps = 20; // 20 blocks
            final double stepSize = 1.0; // 1 block per iteration
            final int[] steps = {0};

            // Use an int[] for the scheduled task ID
            final int[] projectileTaskId = new int[1];

            player.getWorld().playSound(startLoc, Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1.5f, 1f);

            // 2) Schedule a repeating task that moves the "blob" each tick
            projectileTaskId[0] = Bukkit.getScheduler().scheduleSyncRepeatingTask(
                    GrimoireSMP.getInstance(), // or your plugin instance if you're not in the main class
                    new Runnable() {
                        @Override
                        public void run() {
                            // If we've traveled 20 steps, stop
                            if (steps[0] >= maxSteps) {
                                Bukkit.getScheduler().cancelTask(projectileTaskId[0]);
                                return;
                            }

                            // Current location
                            Location currentLoc = startLoc.clone().add(dir.clone().multiply(stepSize * steps[0]));
                            steps[0]++;

                            // Spawn "dark" particles, e.g. Particle.SMOKE_LARGE or ASH
                            currentLoc.getWorld().spawnParticle(
                                    Particle.LARGE_SMOKE,
                                    currentLoc.getX(), currentLoc.getY(), currentLoc.getZ(),
                                    5,
                                    0.2, 0.2, 0.2,
                                    0.01
                            );

                            // Check if we hit a living entity (radius ~0.7 around the blob)
                            for (Entity e : currentLoc.getWorld().getNearbyEntities(currentLoc, 0.7, 5.0, 0.7)) {
                                if (e instanceof LivingEntity target && !target.equals(player)) {
                                    // We "hit" this target => stop the projectile
                                    Bukkit.getScheduler().cancelTask(projectileTaskId[0]);

                                    // 3) Inflict Wither I for 10s => 200 ticks
                                    target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 200, 0, true, false));

                                    Location loc = target.getLocation();
                                    target.getWorld().playSound(loc, Sound.ENTITY_EVOKER_PREPARE_ATTACK, 1.5f, 1f);

                                    // 4) Pull the target back to the caster for ~1 second
                                    startPullingTarget(player, target);

                                    player.sendMessage("§6Your Fangs ensnare§r §c§l" + target.getName() + "!");
                                    if (target instanceof Player p) {
                                        p.sendMessage("§6You are pulled by§r §c§l" + player.getName() + "'s§r §mFangs of Demise..");
                                    }
                                    return;
                                }
                            }
                        }
                    },
                    0L, 1L // run every tick
            );
        }
    }

    private void startPullingTarget(Player caster, LivingEntity target) {
        final int pullDuration = 30; // ticks => 1 second
        final int[] ticks = {0};

        final int[] pullTaskId = new int[1];
        pullTaskId[0] = Bukkit.getScheduler().scheduleSyncRepeatingTask(GrimoireSMP.getInstance(), new Runnable() {
            @Override
            public void run() {
                if (!target.isValid() || !caster.isOnline()) {
                    Bukkit.getScheduler().cancelTask(pullTaskId[0]);
                    return;
                }
                if (ticks[0] >= pullDuration) {
                    Bukkit.getScheduler().cancelTask(pullTaskId[0]);
                    return;
                }

                // Pull effect => set velocity toward caster
                Vector pull = caster.getLocation().toVector().subtract(target.getLocation().toVector());
                pull.normalize().multiply(0.45); // tweak to adjust pulling speed
                target.setVelocity(pull);

                // Darkish particles at their feet
                Location feet = target.getLocation().clone().add(0, 0.1, 0);
                feet.getWorld().spawnParticle(
                        Particle.SQUID_INK, // or SMOKE_LARGE, etc.
                        feet.getX(), feet.getY(), feet.getZ(),
                        10, 0.3, 0.1, 0.3, 0.01
                );
                Location feet1 = target.getLocation().clone().add(0, 0.35, 0);
                feet.getWorld().spawnParticle(
                        Particle.SOUL_FIRE_FLAME, // or SMOKE_LARGE, etc.
                        feet.getX(), feet.getY(), feet.getZ(),
                        25, 0.3, 0.1, 0.3, 0.01
                );
                Particle.DustOptions dust = new Particle.DustOptions(Color.fromRGB(136, 8, 8), 1f);
                feet1.getWorld().spawnParticle(Particle.DUST, feet1, 5, 0.4, 0.2, 0.4, 0.01, dust);

                ticks[0]++;
            }
        }, 0L, 1L);
    }


    @Override
    public void passiveAbilityStart(Player player) {
        if (PlayerData.getPlayerScrollData(player).getRuneEssence() <= 0) return;
        if (PlayerData.getPlayerScrollData(player).isAbilitiesDisabled()) {
            return;
        }
        if (PlayerData.getPlayerScrollData(player).isGrimoire()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 30, 1, true, false, true));
        } else player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 30, 0, true, false, true));

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
        if (mat.name().endsWith("_SWORD") || mat.name().endsWith("_AXE")) {
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return;

            int level;
            if (data.isGrimoire()) {
                level = 5;
            } else if (data.getRuneEssence() <= 5) { // ==5
                level = 3;
            } else {
                level = 2;
            }

            int current = meta.getEnchantLevel(Enchantment.SHARPNESS);
            if (level >current) {
                meta.removeEnchant(Enchantment.SHARPNESS);
                meta.addEnchant(Enchantment.SHARPNESS, level, true);
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
        BloodMarkManager mgr = GrimoireSMP.getInstance().getBloodMarkManager();
        Long expiry = mgr.trackingCooldowns.get(uuid);
        if (expiry != null && now < expiry) {
            long secsLeft = (expiry - now + 999) / 1000;
            player.sendMessage("§cBerserker's Instinct cooldown: " + secsLeft + "s");
            return;
        }
        Inventory gui = Bukkit.createInventory(null, InventoryType.HOPPER, "Berserker's Instinct");
        player.openInventory(gui);
    }

    @Override
    public List<Ability> getAbilities(Player player) {
        UUID u = player.getUniqueId();
        if (PlayerData.getPlayerScrollData(player).getRuneEssence() <= 0) return List.of(new Ability("§l----- LOST SCROLL -----", "\uE800", () -> 0L));
        if (PlayerData.getPlayerScrollData(player).isGrimoire()) {
            return List.of(
                    new Ability(
                            "§4§lFangs Of Demise§r",
                            "\uE003",
                            () -> fangsOfDemiseCooldowns.getOrDefault(u, 0L)
                    ),
                    new Ability(
                            "§4§lBerserker's Instinct§r",
                            "\uE004",
                            () -> GrimoireSMP.getInstance().getBloodMarkManager().trackingCooldowns.getOrDefault(u, 0L)
                    )
            );
        } else {
            return List.of(
                    new Ability(
                            "§4§lFangs Of Demise§r",
                            "\uE003",
                            () -> fangsOfDemiseCooldowns.getOrDefault(u, 0L)
                    )
            );
        }

    }

    @Override
    public Scroll cloneScroll() {
        return new BerserkerScroll();
    }
}
