package pl.polsatgranie.itomsd.deadlyWater;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;

public class DeadlyWaterFlag {
    public static StateFlag DEADLY_WATER = new StateFlag("deadly-water", false);

    public static void registerFlags() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            registry.register(DEADLY_WATER);
        } catch (FlagConflictException e) {
            Flag<?> existing = registry.get("deadly-water");
            if (existing instanceof StateFlag) {
                DEADLY_WATER = (StateFlag) existing;
            } else {
                throw e;
            }
        }
    }
}
