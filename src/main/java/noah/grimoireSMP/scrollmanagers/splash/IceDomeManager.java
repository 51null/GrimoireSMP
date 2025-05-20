package noah.grimoireSMP.scrollmanagers.splash;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import noah.grimoireSMP.GrimoireSMP;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class IceDomeManager {
    private static final int RADIUS = 16;
    private static final long DURATION_TICKS = 20 * 20; // 20 seconds

    // For storing original blocks
    private final List<Original> originals = new ArrayList<>();
    private final Player player;
    private final World world;
    private final int groundY;

    // ProtocolLib manager & spoof task
    private final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
    private BukkitRunnable spoofTask;

    private IceDomeManager(Player player) {
        this.player  = player;
        this.world   = player.getWorld();
        this.groundY = player.getLocation().getBlockY();

    }

    public static void start(Player player) {
        IceDomeManager mgr = new IceDomeManager(player);
        mgr.buildDome();
        mgr.startBootSpoof();     // begin client‑side boot spoofing
        mgr.scheduleRevert();     // schedule revert of blocks & spoof task
    }

    private void buildDome() {
        Location center = player.getLocation();
        int cx = center.getBlockX(), cy = groundY, cz = center.getBlockZ();

        double rSquared = RADIUS * RADIUS;
        double innerSquared = (RADIUS - 1) * (RADIUS - 1);




        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                if (dx*dx + dz*dz <= RADIUS*RADIUS) {
                    Block g = world.getHighestBlockAt(cx+dx, cz+dz);
                    saveAndSet(g, Material.POWDER_SNOW.createBlockData());
                }
            }
        }

        // ICE shell
        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                for (int dy = -RADIUS; dy <= RADIUS; dy++) {
                    double distanceSquared = dx * dx + dy * dy + dz * dz; // remove if using older ->
                    if (distanceSquared <= rSquared && distanceSquared >= (innerSquared-2)) { // dx*dx + dy*dy + dz*dz <= RADIUS*RADIUS
                        Block b = world.getBlockAt(cx+dx, cy+dy, cz+dz);
                        saveAndSet(b, Material.ICE.createBlockData());
                    }
                }
            }
        }
        // POWDER_SNOW ground layer

    }

    private void saveAndSet(Block block, BlockData data) {
        originals.add(new Original(block.getLocation().clone(), block.getBlockData()));
        block.setBlockData(data, false);
    }

    /** Starts sending fake leather‑boots packets each tick so the client treats you as wearing boots. */
    private void startBootSpoof() {
        spoofTask = new BukkitRunnable() {
            int tick = 0;
            @Override
            public void run() {
                if (tick >= 400) {
                    cancel();
                    return;
                }
                if (!player.isOnline()) {
                    cancel();
                    return;
                }
                try {
                    PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);
                    packet.getIntegers().write(0, player.getEntityId());
                    // FEET slot = leather boots
                    Pair<EnumWrappers.ItemSlot, ItemStack> slot =
                            new Pair<>(EnumWrappers.ItemSlot.FEET, new ItemStack(Material.LEATHER_BOOTS));
                    packet.getSlotStackPairLists().write(0, Collections.singletonList(slot));
                    protocolManager.sendServerPacket(player, packet);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        // run every tick
        spoofTask.runTaskTimer(GrimoireSMP.getInstance(), 0L, 1L);
    }

    /** After the duration, revert all blocks and stop spoofing boots. */
    private void scheduleRevert() {
        new BukkitRunnable() {
            @Override
            public void run() {
                // 1) Cancel the boot‑spoof task so the client sees your real boots again.
                if (spoofTask != null) spoofTask.cancel();

                // 2) Restore all original blocks.
                for (Original o : originals) {
                    Block b = world.getBlockAt(o.loc);
                    b.setBlockData(o.data, false);
                }

                //originals = new ArrayList<>();
            }
        }.runTaskLater(GrimoireSMP.getInstance(), DURATION_TICKS);
    }

    private static class Original {
        final Location loc;
        final BlockData data;
        Original(Location loc, BlockData data) {
            this.loc  = loc;
            this.data = data;
        }
    }



}
