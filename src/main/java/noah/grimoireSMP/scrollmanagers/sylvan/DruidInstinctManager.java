package noah.grimoireSMP.scrollmanagers.sylvan;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import noah.grimoireSMP.GrimoireSMP;
import noah.grimoireSMP.PlayerData;
import noah.grimoireSMP.PlayerScrollData;
import noah.grimoireSMP.scroll.types.SylvanScroll;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DruidInstinctManager {
    private static final Set<Biome> FOREST_BIOMES = Set.of(
            Biome.FOREST,
            Biome.FLOWER_FOREST,
            Biome.OLD_GROWTH_BIRCH_FOREST,
            Biome.DARK_FOREST,
            Biome.OLD_GROWTH_PINE_TAIGA
    );

    public static void init(JavaPlugin plugin) {
        ProtocolLibrary.getProtocolManager().addPacketListener(
                new PacketAdapter(plugin, ListenerPriority.NORMAL,
                        PacketType.Play.Server.NAMED_ENTITY_SPAWN,
                        PacketType.Play.Server.ENTITY_METADATA
                ) {
                    @Override
                    public void onPacketSending(PacketEvent event) {
                        modifyGlow(event);
                    }
                }
        );
    }

    private static void modifyGlow(PacketEvent event) {
        Player viewer = event.getPlayer();
        // 1) Only if viewer has an active Sylvan scroll and is in a forest biome:
        PlayerScrollData data = PlayerData.getPlayerScrollData(viewer);
        if (data == null
                || !(data.getScroll() instanceof SylvanScroll)
                || data.getScroll().getRuneEssence() <= 0
                || !FOREST_BIOMES.contains(viewer.getLocation().getBlock().getBiome())
        ) return;

        PacketContainer packet = event.getPacket();
        int entityId = packet.getIntegers().read(0);
        Entity e = getEntityById(entityId);
        if (!(e instanceof Player target) || target.equals(viewer)) return;

        // (Optional) require target in same forest:
        // if (!FOREST_BIOMES.contains(target.getLocation().getBlock().getBiome())) return;

        // If it’s a metadata packet, flip the glow bit
        if (event.getPacketType() == PacketType.Play.Server.ENTITY_METADATA) {
            List<WrappedDataValue> metaList =
                    packet.getDataValueCollectionModifier().read(0);

            for (int i = 0; i < metaList.size(); i++) {
                WrappedDataValue wdv = metaList.get(i);
                if (wdv.getIndex() == 0 && wdv.getValue() instanceof Byte) {
                    byte oldFlags = (Byte) wdv.getValue();
                    byte newFlags = (byte)(oldFlags | 0x40);

                    WrappedDataValue updated = new WrappedDataValue(
                            wdv.getIndex(),
                            WrappedDataWatcher.Registry.get(Byte.class),
                            newFlags
                    );

                    metaList.set(i, updated);
                    packet.getDataValueCollectionModifier().write(0, metaList);
                    break;
                }
            }
        }
        // No further action needed for NAMED_ENTITY_SPAWN: entity metadata will follow soon.
    }

    private static Entity getEntityById(int id) {
        for (World w : Bukkit.getWorlds()) {
            for (Entity e : w.getEntities()) {
                if (e.getEntityId() == id) return e;
            }
        }
        return null;
    }
}

