package net.zeus.vitalxp.commands;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class CommandRegister {

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        VitalXPCommand.register(event.getDispatcher());
    }
}
