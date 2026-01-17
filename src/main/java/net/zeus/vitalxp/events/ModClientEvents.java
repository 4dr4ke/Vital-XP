package net.zeus.vitalxp.events;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zeus.vitalxp.Config;
import net.zeus.vitalxp.VitalXP;

@Mod.EventBusSubscriber(modid = VitalXP.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ModClientEvents {

    private static final ResourceLocation HEART_LOCKED = new ResourceLocation("vitalxp", "textures/gui/locked_heart.png");
    private static final ResourceLocation HALF_HEART_LOCKED = new ResourceLocation("vitalxp", "textures/gui/locked_heart_half.png");

    private static int tick = 0;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        tick++;
    }

    @SubscribeEvent
    public static void onRenderHealthPost(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.PLAYER_HEALTH.type()) return;

        Player player = Minecraft.getInstance().player;
        if (player == null || player.isCreative()) return;
        if (!Config.lockedHeartsEnabled) return;

        double lockedHearts = ModServerEvents.lastLockedHearts.getOrDefault(player, 0.0);
        if (lockedHearts <= 0) return;

        renderLockedHeartsOverlay(event.getGuiGraphics(), player, lockedHearts);
    }

    private static void renderLockedHeartsOverlay(GuiGraphics gui, Player player, double lockedHearts) {

        Minecraft mc = Minecraft.getInstance();

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int left = screenWidth / 2 - 91;
        int top = screenHeight - 39;

        float maxHealth = player.getMaxHealth();
        float health = player.getHealth();
        int absorption = Mth.ceil(player.getAbsorptionAmount());

        int totalHearts = Mth.ceil(maxHealth / 2f);
        int lockedHeartCount = (int) Math.floor(lockedHearts / 2.0);

        HeartType type = HeartType.forPlayer(player);

        gui.pose().pushPose();

        for (int i = totalHearts - lockedHeartCount; i < totalHearts; i++) {

            int x = left + (i % 10) * 8;
            int y = top - (i / 10) * 10;

            int healthWithAbsorption = Mth.ceil(health) + absorption;

            if (healthWithAbsorption <= 6) {
                y += ((tick + i * 2) / 5) % 2 == 0 ? 1 : 0;
            }

            int heartHealthIndex = i * 2;
            int heartHealth = Mth.ceil(health) - heartHealthIndex;

            if (heartHealth >= 2) {
                renderLockedHeart(gui, type, x, y, false);
            } else if (heartHealth == 1) {
                renderLockedHeart(gui, type, x, y, true);
            }
        }

        gui.pose().popPose();
    }

    private static void renderLockedHeart(GuiGraphics gui, HeartType type, int x, int y, boolean half) {

        if (type != HeartType.NORMAL) return;

        ResourceLocation tex = half ? HALF_HEART_LOCKED : HEART_LOCKED;
        gui.blit(tex, x, y, 0, 0, 9, 9, 9, 9);
    }

    private enum HeartType {
        NORMAL, POISONED, WITHERED, FROZEN;

        public static HeartType forPlayer(Player player) {
            if (player.hasEffect(MobEffects.WITHER)) return WITHERED;
            if (player.hasEffect(MobEffects.POISON)) return POISONED;
            if (player.isFullyFrozen()) return FROZEN;
            return NORMAL;
        }
    }
}