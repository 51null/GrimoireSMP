package noah.grimoireSMP;

import java.util.function.LongSupplier;

public class Ability {
    private final String name;                   // e.g. "Fireball"
    private final String icon;                   // e.g. "\uE000"
    private final LongSupplier cooldownExpiry;   // returns the timestamp when it’s ready

    public Ability(String name, String icon, LongSupplier cooldownExpiry) {
        this.name = name;
        this.icon = icon;
        this.cooldownExpiry = cooldownExpiry;
    }

    public String format() {
        long now    = System.currentTimeMillis();
        long expiry = cooldownExpiry.getAsLong();
        long remMs  = expiry - now;
        String timer;
        if (remMs <= 0) {
            timer = "§a§lREADY§r";
        } else {
            long totalSec = remMs / 1000;
            if (totalSec >= 60) {
                long mins = totalSec / 60;
                long secs = totalSec % 60;
                timer = mins + "m" + secs + "s";
            } else {
                timer = totalSec + "s";
            }
        }
        return icon + " " + name + ": " + timer;
    }
}
