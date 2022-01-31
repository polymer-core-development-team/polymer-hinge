package com.teampolymer.polymer.hinge.client.handler;

import com.teampolymer.polymer.hinge.PolymerHinge;
import com.teampolymer.polymer.hinge.client.manager.ClientMultiblockManager;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = PolymerHinge.MOD_ID, value = Dist.CLIENT)
public class ChunkEventHandler {

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        IWorld world = event.getWorld();
        if (!world.isClientSide()) {
            return;
        }

        ChunkPos pos = event.getChunk().getPos();
        ClientMultiblockManager.getInstance().removeInChunk(pos);

    }
}
