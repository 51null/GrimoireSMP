package noah.grimoireSMP.scrollmanagers.astral;

import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class StoredMobData implements ConfigurationSerializable {
    private final EntityType type;
    private String customName;
    private boolean charged;            // for creepers
    private ItemStack[] armor;          // helmet, chestplate, leggings, boots
    private ItemStack mainHand, offHand;


    // Construct from a live entity
    public StoredMobData(Entity e) {
        this.type = e.getType();
        this.customName = e.getCustomName();
        if (e instanceof Creeper cr) {
            this.charged = cr.isPowered();
        }
        if (e instanceof LivingEntity le) {
            EntityEquipment eq = le.getEquipment();
            this.armor    = eq.getArmorContents();
            this.mainHand = eq.getItemInMainHand();
            this.offHand  = eq.getItemInOffHand();
        }
    }

    /** Deserialize constructor */
    @SuppressWarnings("unchecked")
    public static StoredMobData deserialize(Map<String, Object> map) {
        EntityType type = EntityType.valueOf((String)map.get("type"));
        StoredMobData d = new StoredMobData(type);
        d.customName = (String)map.get("customName");
        d.charged    = Boolean.TRUE.equals(map.get("charged"));
        d.armor      = ((List<ItemStack>)map.get("armor")).toArray(new ItemStack[0]);
        d.mainHand   = (ItemStack)map.get("mainHand");
        d.offHand    = (ItemStack)map.get("offHand");
        return d;
    }

    /** Minimal constructor for deserialization */
    private StoredMobData(EntityType type) {
        this.type = type;
    }

    public EntityType getType() {
        return this.type;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String,Object> m = new LinkedHashMap<>();
        m.put("type", type.name());
        m.put("customName", customName);
        m.put("charged", charged);
        m.put("armor", Arrays.asList(armor));
        m.put("mainHand", mainHand);
        m.put("offHand", offHand);
        return m;
    }

    /** Spawn this snapshot at that location */
    public LivingEntity spawn(Location loc) {
        Entity e = loc.getWorld().spawnEntity(loc, type);
        if (!(e instanceof LivingEntity le)) return null;
        if (customName != null) le.setCustomName(customName);
        if (le instanceof Creeper cr && charged) {
            cr.setPowered(true);
        }
        EntityEquipment eq = le.getEquipment();
        eq.setArmorContents(armor);
        eq.setItemInMainHand(mainHand);
        eq.setItemInOffHand(offHand);
        return le;
    }
}
