package noah.grimoireSMP.scroll;

import noah.grimoireSMP.Ability;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.ArrayList;

public abstract class Scroll {

    private final String name;
    protected int runeEssence;
    public static final int MAX_RUNE_ESSENCE = 7;
    public static final int START_RUNE_ESSENCE = 5;

    // Base lore for the scroll.
    protected List<String> baseLore;

    // Custom model data for this scroll.
    private final int normalModelData;
    private final int grimoireModelData;
    public static final int LOST_MODEL_DATA = 9999;

    protected Scroll(String name, int normalModelData, int grimoireModelData) {
        this.name = name;
        this.normalModelData = normalModelData;
        this.grimoireModelData = grimoireModelData;
        this.runeEssence = START_RUNE_ESSENCE;
        this.baseLore = new ArrayList<>();
        this.baseLore.add("Scroll of " + name);
    }

    public String getName() {
        return name;
    }

    public int getRuneEssence() {
        return runeEssence;
    }

    public void setRuneEssence(int amount) {
        this.runeEssence = amount;
    }

    public void addRuneEssence(int amount) {
        if (!isLost()) {
            runeEssence = Math.min(runeEssence + amount, MAX_RUNE_ESSENCE);
        }
    }

    public void reduceRuneEssence(int amount) {
        if (!isLost()) {
            runeEssence = Math.max(runeEssence - amount, 0);
        }
    }

    public boolean isLost() {
        return runeEssence == 0;
    }

    public List<String> getBaseLore() {
        return baseLore;
    }

    public int getModelData(boolean isGrimoire, boolean isLost) {
        if (isLost) {
            return LOST_MODEL_DATA;
        }
        if (isGrimoire) return grimoireModelData;
        return normalModelData;
    }


    // Default method to display an action bar message.
    // This can be overridden by subclasses if they need custom formatting.
    public void updateActionBar(Player player, String message) {
        // In newer versions of Spigot/Paper, you can do:
        player.sendActionBar(message);
    }
    // Example: a method to display a cooldown timer.
    public void displayCooldown(Player player, int secondsRemaining) {
        updateActionBar(player, "Ability cooldown: " + secondsRemaining + "s");
    }

    public abstract void useActiveAbility(int abilityNumber, Player player);
    public abstract void passiveAbilityStart(Player player);
    public abstract void useGrimoireAbility(Player player);
    public abstract List<Ability> getAbilities(Player player);

    public abstract List<Pair<String,String>> getPassiveInfo();
    public abstract List<ScrollLoreUtil.AbilityInfo> getActiveInfo();
    public abstract ScrollLoreUtil.AbilityInfo getGrimoireInfo();

    // Cloning method for returning new instances.
    public abstract Scroll cloneScroll();
}
