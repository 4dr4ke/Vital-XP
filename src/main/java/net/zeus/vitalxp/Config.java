package net.zeus.vitalxp;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = VitalXP.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    // Health XP config
    private static final ForgeConfigSpec.DoubleValue MIN_HEALTH = BUILDER
            .comment("Minimum health when your XP level is 0 (in half-hearts * 2), e.g. 8 = 4 hearts")
            .defineInRange("minHealth", 8.0, 1.0, 100.0);

    private static final ForgeConfigSpec.DoubleValue MAX_HEALTH = BUILDER
            .comment("Maximum health (in half-hearts * 2), e.g. 20 = 10 hearts")
            .defineInRange("maxHealth", 20.0, 1.0, 200.0);

    private static final ForgeConfigSpec.IntValue LEVELS_PER_HEART = BUILDER
            .comment("Number of XP levels required to gain one heart")
            .defineInRange("levelsPerHeart", 4, 1, 100);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static double minHealth;
    public static double maxHealth;
    public static int levelsPerHeart;
    public static boolean lockedHeartsEnabled = true;

    private static boolean validateItemName(final Object obj) {
        return obj instanceof final String itemName && ForgeRegistries.ITEMS.containsKey(new ResourceLocation(itemName));
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        minHealth = MIN_HEALTH.get();
        maxHealth = MAX_HEALTH.get();
        levelsPerHeart = LEVELS_PER_HEART.get();
    }
}
