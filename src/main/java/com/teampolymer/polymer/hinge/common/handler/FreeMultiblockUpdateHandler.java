package com.teampolymer.polymer.hinge.common.handler;

import com.teampolymer.polymer.core.api.capability.IChunkMultiblockStorage;
import com.teampolymer.polymer.core.api.multiblock.IAssembledMultiblock;
import com.teampolymer.polymer.core.api.multiblock.part.IMultiblockUnit;
import com.teampolymer.polymer.core.common.capability.chunk.CapabilityChunkMultiblockStorage;
import com.teampolymer.polymer.core.common.capability.chunk.ChunkMultiblockCapabilityProvider;
import com.teampolymer.polymer.core.common.world.FreeMultiblockWorldSavedData;
import com.teampolymer.polymer.hinge.PolymerHinge;
import net.minecraft.block.BlockState;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunk;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = PolymerHinge.MOD_ID)
public class FreeMultiblockUpdateHandler {
    private static final Logger LOG = LogManager.getLogger();

    public static void handleBlockChange(World world, Chunk chunk, BlockPos pos, BlockState newBlock) {
        chunk.getCapability(CapabilityChunkMultiblockStorage.MULTIBLOCK_STORAGE).ifPresent(it -> {
            Tuple<UUID, IMultiblockUnit> part = it.getMultiblockPart(pos);
            if (part == null) {
                return;
            }
            boolean test = part.getB().test(newBlock);
            if (!test) {
                //TODO: 这里可以继续优化一下性能
                IAssembledMultiblock assembledMultiblock = FreeMultiblockWorldSavedData.get(world).getAssembledMultiblock(part.getA());
                if (assembledMultiblock == null) {
                    LOG.warn("The multiblock '{}' 's block in {} is invalid!", part.getA(), pos);
                    it.removeMultiblock(part.getA());
                    return;
                }
                //解除组装
                assembledMultiblock.disassemble(world);
            }
        });
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<?> event) {
        if (!(event.getObject() instanceof Chunk)) {
            return;
        }
        Chunk chunk = (Chunk) event.getObject();
        ChunkMultiblockCapabilityProvider provider = new ChunkMultiblockCapabilityProvider(() -> chunk);
        event.addCapability(ChunkMultiblockCapabilityProvider.CAPABILITY_PROVIDER_CHUNK_MULTIBLOCK, provider);
        event.addListener(provider::invalidate);
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load load) {
        IChunk chunk = load.getChunk();
        IWorld world = load.getWorld();
        if (world.isClientSide()) {
            return;
        }
        Set<UUID> multiblocks = new HashSet<>();
        if (chunk instanceof ICapabilityProvider) {
            ((ICapabilityProvider) chunk).getCapability(CapabilityChunkMultiblockStorage.MULTIBLOCK_STORAGE)
                .ifPresent(it -> {
                    it.initialize((World) world);
                    multiblocks.addAll(it.getContainingMultiblocks());
                });
        }
        FreeMultiblockWorldSavedData.get((World) world).validateMultiblocksInChunk(chunk.getPos(), multiblocks);

    }

    @SubscribeEvent
    public static void onChunkUnLoad(ChunkEvent.Unload unload) {
        IChunk chunk = unload.getChunk();
        if (chunk instanceof ICapabilityProvider) {
            ((ICapabilityProvider) chunk).getCapability(CapabilityChunkMultiblockStorage.MULTIBLOCK_STORAGE)
                .ifPresent(IChunkMultiblockStorage::invalidate);
        }
    }
}
