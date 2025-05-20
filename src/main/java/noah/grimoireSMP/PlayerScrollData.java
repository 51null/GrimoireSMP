package noah.grimoireSMP;

import noah.grimoireSMP.scroll.Scroll;
import noah.grimoireSMP.scroll.ScrollLoreUtil;
import noah.grimoireSMP.scrollmanagers.astral.StoredMobData;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Material;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.ArrayList;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;

public class PlayerScrollData {
    private final Scroll scroll;
    private int runeEssence;
    private boolean grimoire; // false by default
    private long abilitiesDisabledUntil = 0;
    private final Deque<StoredMobData> pocket = new ArrayDeque<>();

    public PlayerScrollData(Scroll scroll, int runeEssence) {
        this.scroll = scroll;
        this.runeEssence = runeEssence;
        this.grimoire = false;
    }

    public Scroll getScroll() {
        return scroll;
    }

    public int getRuneEssence() {
        return runeEssence;
    }

    public void setRuneEssence(int runeEssence) {
        this.runeEssence = runeEssence;
    }

    public void addRuneEssence(int amount) {
        runeEssence = Math.min(runeEssence + amount, Scroll.MAX_RUNE_ESSENCE);
    }

    public void reduceRuneEssence(int amount) {
        runeEssence = Math.max(runeEssence - amount, 0);
    }

    public boolean isGrimoire() {
        return grimoire;
    }

    public void setGrimoire(boolean grimoire) {
        this.grimoire = grimoire;
    }

    private long wealthHoardingExpiry = 0;

    public void activateWealthHoarding(long durationMs) {
        this.wealthHoardingExpiry = System.currentTimeMillis() + durationMs;
    }

    /** Returns true if Wealth Hoarding is currently active. */
    public boolean isWealthHoardingActive() {
        return System.currentTimeMillis() < this.wealthHoardingExpiry;
    }

    public List<StoredMobData> getPocket() {
        return List.copyOf(pocket);
    }

    public boolean storeMob(StoredMobData data, int maxSize) {
        if (pocket.size() >= maxSize) return false;
        pocket.addLast(data);
        return true;
    }

    public StoredMobData retrieveLastMob() {
        return pocket.pollLast();
    }

    /** Call before firing any ability. */
    public boolean isAbilitiesDisabled() {
        return System.currentTimeMillis() < abilitiesDisabledUntil;
    }

    /** Silences abilities for at least `durationMs` more. */
    public void extendAbilitiesDisabledBy(long durationMs) {
        long now = System.currentTimeMillis();
        // If already silenced, extend; else start new
        abilitiesDisabledUntil = Math.max(abilitiesDisabledUntil, now) + durationMs;
    }

    public long getAbilitiesDisabledUntil() {
        return abilitiesDisabledUntil;
    }

    public ItemStack toItemStack(String playerName) {
        // 1) Create the base paper item
        ItemStack scrollItem = new ItemStack(Material.PAPER);
        ItemMeta meta = scrollItem.getItemMeta();
        if (meta == null) return scrollItem;

        // 2) Determine state flags
        boolean isLost     = (runeEssence == 0);
        boolean isGrimoire = this.grimoire;

        // 3) Display name
        String displayName = scroll.getName();
        if (isLost) {
            displayName = "Lost " + displayName;
        } else if (isGrimoire) {
            displayName += " [Grimoire]";
        }
        meta.setDisplayName(displayName);

        // 4) Custom Model Data
        int cmd = scroll.getModelData(isGrimoire, isLost);
        meta.setCustomModelData(cmd);

        // 5) PersistentDataContainer tags


        NamespacedKey key = new NamespacedKey(GrimoireSMP.getInstance(), "Scroll");
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        pdc.set(key, PersistentDataType.STRING, scroll.getName());

        // 6) Lore
        List<String> dynamicLore = ScrollLoreUtil.buildScrollLore(this);
        meta.setLore(dynamicLore);

        // 7) Apply back to the item
        scrollItem.setItemMeta(meta);
        return scrollItem;
    }

}
