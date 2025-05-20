package noah.grimoireSMP.ritual;

import noah.grimoireSMP.PlayerData;
import noah.grimoireSMP.PlayerScrollData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.World;
import org.bukkit.block.Biome;

import java.util.Random;
import java.util.Set;

public class RitualCommand implements CommandExecutor {
    private final RitualManager rm = RitualManager.get();

    // All ocean‐type biomes we want to avoid
    private static final Set<Biome> OCEANS = Set.of(
            Biome.OCEAN,
            Biome.DEEP_OCEAN,
            Biome.FROZEN_OCEAN,
            Biome.DEEP_COLD_OCEAN,
            Biome.DEEP_FROZEN_OCEAN,
            Biome.LUKEWARM_OCEAN,
            Biome.DEEP_LUKEWARM_OCEAN,
            Biome.WARM_OCEAN,
            Biome.COLD_OCEAN
    );

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("grimoire.ritual.start")) {
            sender.sendMessage("§cYou need operator to do that.");
            return true;
        }

        World world = Bukkit.getWorlds().get(0);
        Location loc;

        if (args.length == 1 && args[0].equalsIgnoreCase("random")) {
            Random r = new Random();

            // keep trying until we find a spot ≥50 blocks from any ocean biome
            while (true) {
                int x = r.nextInt(10001) - 5000;
                int z = r.nextInt(10001) - 5000;

                // 1) reject if this block is ocean
                int highestY = world.getHighestBlockYAt(x, z);
                Biome centerBiome = world.getBiome(x, highestY, z);
                if (OCEANS.contains(centerBiome)) {
                    continue;
                }

                // 2) sample 12 points on 50-block circle
                boolean tooClose = false;
                for (int deg = 0; deg < 360; deg += 30) {
                    double rad = Math.toRadians(deg);
                    int cx = x + (int)(50 * Math.cos(rad));
                    int cz = z + (int)(50 * Math.sin(rad));
                    int cy = world.getHighestBlockYAt(cx, cz);
                    Biome b = world.getBiome(cx, cy, cz);
                    if (OCEANS.contains(b)) {
                        tooClose = true;
                        break;
                    }
                }
                if (tooClose) {
                    continue;
                }

                // found a valid spot!
                loc = new Location(world, x, highestY, z);
                break;
            }

        } else if (args.length == 3) {
            try {
                double x = Double.parseDouble(args[0]);
                double y = Double.parseDouble(args[1]);
                double z = Double.parseDouble(args[2]);
                loc = new Location(world, x, y, z);
            } catch (NumberFormatException ex) {
                sender.sendMessage("§cUsage: /startritual <x> <y> <z>|random");
                return true;
            }
        } else {
            sender.sendMessage("§cUsage: /startritual <x> <y> <z>|random");
            return true;
        }

        // Set up the site
        rm.setupSite(loc);
        sender.sendMessage("§aRitual site set at " +
                (int)loc.getX()+", "+(int)loc.getY()+", "+(int)loc.getZ());
        Bukkit.broadcastMessage("§6A dark Ritual has begun: head to " +
                (int)loc.getX()+", ~, "+(int)loc.getZ()+" to participate!");
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendTitle("§4RITUAL",
                    (int)loc.getX()+", ~, "+(int)loc.getZ(),
                    5, 40, 0
            );
            player.playSound(player.getLocation(),
                    Sound.ENTITY_ELDER_GUARDIAN_CURSE,
                    1.0f, 0.8f
            );
        }
        return true;
    }
}
