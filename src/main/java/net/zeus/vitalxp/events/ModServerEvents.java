package net.zeus.vitalxp.events;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.zeus.vitalxp.Config;

import java.util.HashMap;
import java.util.Map;

public class ModServerEvents {

    public static final Map<Player, Double> lastLockedHearts = new HashMap<>();
    public static final String LOCKED_HEARTS_TAG = "VitalXpLockedHearts";

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {

        if (event.side.isClient()) return;

        Player player = event.player;

        CompoundTag persisted = player.getPersistentData();
        if (!persisted.contains(Player.PERSISTED_NBT_TAG)) {
            persisted.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        }
        CompoundTag playerTag = persisted.getCompound(Player.PERSISTED_NBT_TAG);

        if (!Config.lockedHeartsEnabled) {
            if (lastLockedHearts.get(player) != null) {
                lastLockedHearts.remove(player);
            }

            if (playerTag.contains(LOCKED_HEARTS_TAG)) {
                playerTag.putDouble(LOCKED_HEARTS_TAG, 0d);
            }
        }

        int level = player.experienceLevel;
        int heartsGained = level / Config.levelsPerHeart;

        double baseMaxHealth = Config.minHealth + heartsGained * 2.0;
        double savedLocked = playerTag.contains(LOCKED_HEARTS_TAG) ? playerTag.getDouble(LOCKED_HEARTS_TAG) : 0d;

        double overflow = Math.max(0, baseMaxHealth - Config.maxHealth);
        double lockedHearts = Math.max(savedLocked, overflow);
        playerTag.putDouble(LOCKED_HEARTS_TAG, lockedHearts);

        double last = lastLockedHearts.getOrDefault(player, -1.0);
        if (lockedHearts != last) {
            lastLockedHearts.put(player, lockedHearts);
        }

        double finalMaxHealth = Math.min(Config.maxHealth, baseMaxHealth + lockedHearts);

        AttributeInstance maxHealthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttr == null) return;

        double current = maxHealthAttr.getBaseValue();

        if (current != finalMaxHealth) {
            maxHealthAttr.setBaseValue(finalMaxHealth);

            if (player.getHealth() > finalMaxHealth) {
                player.setHealth((float) finalMaxHealth);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (lastLockedHearts.get(event.getEntity()) != null) {
            lastLockedHearts.remove(event.getEntity());
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;

        Player newPlayer = event.getEntity();
        CompoundTag persisted = newPlayer.getPersistentData();

        if (!persisted.contains(Player.PERSISTED_NBT_TAG)) {
            persisted.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        }

        CompoundTag playerTag = persisted.getCompound(Player.PERSISTED_NBT_TAG);

        playerTag.putDouble(LOCKED_HEARTS_TAG, 0d);

        lastLockedHearts.remove(event.getOriginal());
        lastLockedHearts.remove(newPlayer);
    }
}
