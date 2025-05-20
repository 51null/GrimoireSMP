package noah.grimoireSMP.scroll;

import noah.grimoireSMP.PlayerScrollData;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class ScrollLoreUtil {
    // In your Scroll base class (or a ScrollLoreUtil):

    public static class AbilityInfo {
        public final String name;
        public final String description;
        public final int    minEssence;   // required essence to unlock

        public AbilityInfo(String name, String description, int minEssence) {
            this.name = name;
            this.description = description;
            this.minEssence = minEssence;
        }
    }

    private
    static String getScrollStatus(int essence) {
        return switch (essence) {
            case 0 -> "§f§lLOST";
            case 1 -> "§7§lFRACTURED";
            case 2 -> "§7§lFADED";
            case 3 -> "§a§lPOTENT";
            case 4 -> "§c§lEMPOWERED";
            case 5 -> "§e§lGODLY";
            case 6 -> "§e§lGODLY + 1";
            case 7 -> "§e§lGODLY + 2";
            default -> "Unknown";
        };
    }

    /**
     * Call this to build the lore for any scroll + player data.
     */
    public static List<String> buildScrollLore(PlayerScrollData data) {
        Scroll scroll = data.getScroll();
        int essence   = data.getRuneEssence();
        boolean grimoire = data.isGrimoire();

        List<String> lore = new ArrayList<>();
        // Header
        lore.add("§bScroll Status§r " + getScrollStatus(essence));
        if (essence == 0) {
            lore.add("§cThis scroll is lost! Perform a Ritual to revive.");
            return lore;  // early exit for lost scroll
        }
        lore.add(" ");

        // Passives
        lore.add("§bPassives:");
        for (Pair<String, String> p : scroll.getPassiveInfo()) {
            lore.add(" §e" + p.getKey() + "§7 — " + p.getValue());
        }
        lore.add(" ");

        // Actives
        lore.add("§bAbilities:");
        List<AbilityInfo> actives = scroll.getActiveInfo();
        for (int i = 0; i < actives.size(); i++) {
            AbilityInfo ai = actives.get(i);
            boolean unlocked = essence >= ai.minEssence;
            String line = unlocked
                    ? " §a[" + (i+1) + "] " + ai.name + "§7 — " + ai.description
                    : " §c[" + (i+1) + "] " + ai.name + " — Locked (requires " + ai.minEssence + ")";
            lore.add(line);
        }
        lore.add(" ");

        // Grimoire
        lore.add("§bGrimoire:");
        AbilityInfo gi = scroll.getGrimoireInfo();
        if (grimoire) {
            lore.add(" §b[U] " + gi.name + "§7 — " + gi.description);
        } else {
            lore.add(" §cUpgrade to Grimoire to unlock " + gi.name);
        }

        return lore;
    }

}
