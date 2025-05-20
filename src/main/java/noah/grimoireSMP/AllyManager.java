package noah.grimoireSMP;

import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// AllyManager.java
public class AllyManager {
    private static final Set<Pair<UUID,UUID>> allies = ConcurrentHashMap.newKeySet();
    private static final Set<Pair<UUID,UUID>> pending = ConcurrentHashMap.newKeySet();

    public static boolean isAlly(UUID a, UUID b) {
        return allies.contains(Pair.of(a,b)) && allies.contains(Pair.of(b,a));
    }
    public static void addAlly(UUID a, UUID b) {
        allies.add(Pair.of(a,b));
        allies.add(Pair.of(b,a));
    }
    public static void removeAlly(UUID a, UUID b) {
        allies.remove(Pair.of(a,b));
        allies.remove(Pair.of(b,a));
    }

    public static void addPendingRequest(UUID from, UUID to) {
        pending.add(Pair.of(from,to));
    }
    public static boolean hasPendingRequest(UUID from, UUID to) {
        return pending.contains(Pair.of(from,to));
    }
    public static void clearPendingRequest(UUID from, UUID to) {
        pending.remove(Pair.of(from,to));
    }
}

