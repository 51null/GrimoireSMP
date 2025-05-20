package noah.grimoireSMP.scroll;

import noah.grimoireSMP.GrimoireSMP;
import noah.grimoireSMP.scroll.types.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;

public class ScrollRegistry {

    private static final Map<String, Scroll> scrolls = new HashMap<>();

    public static void registerScroll(String id, Scroll scroll) {
        scrolls.put(id.toLowerCase(), scroll);
    }

    public static Scroll getScroll(String id) {
        return scrolls.get(id.toLowerCase());
    }

    // Returns a random new instance of a scroll (by cloning).
    public static Scroll getRandomScroll() {
        List<Scroll> list = new ArrayList<>(scrolls.values());
        if (list.isEmpty()) return null;
        Random random = new Random();
        Scroll template = list.get(random.nextInt(list.size()));
        return template.cloneScroll();
    }

    public static List<String> getAllScrollNames() {
        return new ArrayList<>(scrolls.keySet());
    }

    public static void registerScrolls() {
        registerScroll("infernal", new InfernalScroll());
        registerScroll("berserker", new BerserkerScroll());
        registerScroll("splash", new SplashScroll());
        registerScroll("celestial", new CelestialScroll());
        registerScroll("steel", new SteelScroll());
        registerScroll("astral", new AstralScroll());
        registerScroll("treasure", new TreasureScroll());
        registerScroll("temporal", new TemporalScroll());
        //registerScroll("sylvan", new SylvanScroll());
        // register more scrolls...
        // Log which keys were registered:
        for (String key : scrolls.keySet()) {
            GrimoireSMP.getInstance().getLogger().info("Registered scroll: " + key);
        }
    }
}
