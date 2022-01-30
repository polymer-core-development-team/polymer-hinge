package com.teampolymer.polymer.hinge.common.handler;

import com.teampolymer.polymer.hinge.PolymerHinge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = PolymerHinge.MOD_ID)
public class ReloadListenerHandler {
    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent e) {
        e.addListener(new MultiblockReloadListener());
    }

}
