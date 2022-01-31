package com.teampolymer.polymer.hinge.common.handler;

import com.teampolymer.polymer.core.api.PolymerCoreApi;
import com.teampolymer.polymer.core.api.multiblock.IAssembledMultiblock;
import com.teampolymer.polymer.core.common.event.MultiblockChangeEvent;
import com.teampolymer.polymer.hinge.PolymerHinge;
import com.teampolymer.polymer.hinge.common.network.PacketRemoveWorldMultiblock;
import com.teampolymer.polymer.hinge.common.network.PacketSyncWorldMultiblock;
import com.teampolymer.polymer.hinge.common.network.PolymerHingeNetworking;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.Collection;

@Mod.EventBusSubscriber(modid = PolymerHinge.MOD_ID)
public class WorldMultiblockSyncHandler {

    @SubscribeEvent
    public static void onChunkWatch(ChunkWatchEvent.Watch event) {
        Collection<? extends IAssembledMultiblock> multiblocks = PolymerCoreApi.getInstance()
            .getWorldMultiblockManager()
            .findByChunks(event.getWorld(), event.getPos());
        PolymerHingeNetworking.INSTANCE.send(
            PacketDistributor.TRACKING_CHUNK.with(() -> event.getWorld().getChunk(event.getPos().x, event.getPos().z)),
            new PacketSyncWorldMultiblock(multiblocks)
        );
    }

    @SubscribeEvent
    public static void onMultiblockAssembled(MultiblockChangeEvent.Assembled event) {
        PolymerHingeNetworking.INSTANCE.send(
            PacketDistributor.TRACKING_CHUNK.with(() -> (Chunk) event.getWorld().getChunk(event.getCorePos())),
            new PacketSyncWorldMultiblock(event.getMultiblock())
        );
    }

    @SubscribeEvent
    public static void onMultiblockDisassembled(MultiblockChangeEvent.Disassembled event) {
        PolymerHingeNetworking.INSTANCE.send(
            PacketDistributor.TRACKING_CHUNK.with(() -> (Chunk) event.getWorld().getChunk(event.getCorePos())),
            new PacketRemoveWorldMultiblock(event.getMultiblock().getMultiblockId())
        );
    }

}
