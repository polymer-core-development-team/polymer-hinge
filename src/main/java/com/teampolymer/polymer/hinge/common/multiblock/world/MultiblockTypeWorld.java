package com.teampolymer.polymer.hinge.common.multiblock.world;

import com.teampolymer.polymer.core.api.capability.IChunkMultiblockStorage;
import com.teampolymer.polymer.core.api.multiblock.IAssembledMultiblock;
import com.teampolymer.polymer.core.api.multiblock.IArchetypeMultiblock;
import com.teampolymer.polymer.core.api.multiblock.IMultiblockType;
import com.teampolymer.polymer.core.api.multiblock.assembled.IMultiblockAssembleRule;
import com.teampolymer.polymer.hinge.common.chunk.CapabilityChunkMultiblockStorage;
import com.teampolymer.polymer.hinge.common.multiblock.assembled.WorldMultiblockImpl;
import com.teampolymer.polymer.hinge.common.world.WorldMultiblockSavedData;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.UUID;

public class MultiblockTypeWorld extends ForgeRegistryEntry<IMultiblockType> implements IMultiblockType {
    private static final Logger LOG = LogManager.getLogger();
    @Override
    public IAssembledMultiblock createMultiblockIn(IArchetypeMultiblock definition, World world, IMultiblockAssembleRule assembleRule) {

        if (world.isClientSide) {
            return null;
        }
        UUID uuid = UUID.randomUUID();
        WorldMultiblockImpl multiblock = new WorldMultiblockImpl(
            uuid,
            assembleRule,
            definition
        );

        WorldMultiblockSavedData.get(world).addAssembledMultiblock(multiblock);
        if (!multiblock.initialize()) {
            multiblock.disassemble(world);
            return null;
        }
        Collection<ChunkPos> crossedChunks = multiblock.getCrossedChunks();
        for (ChunkPos chunkPos : crossedChunks) {
            Chunk chunk = world.getChunk(chunkPos.x, chunkPos.z);
            LazyOptional<IChunkMultiblockStorage> capability = chunk.getCapability(CapabilityChunkMultiblockStorage.MULTIBLOCK_STORAGE);
            capability.ifPresent(it -> it.addMultiblock(uuid, multiblock.getUnits()));
        }

        return multiblock;
    }


    @Override
    public IAssembledMultiblock createFromNBT(World world, CompoundNBT nbt) {
        //TODO: 拓展
        WorldMultiblockImpl multiblock = new WorldMultiblockImpl();
        try {
            multiblock.deserializeNBT(nbt);
        } catch (IllegalStateException e) {
            LOG.error(e.getMessage());
            return null;
        }
        return multiblock;
    }

    @Override
    public IMultiblockAssembleRule createEmptyRule(BlockPos coreOffset, Rotation rotation, boolean isSymmetrical) {
        return new WorldMultiblockAssembleRule(coreOffset, isSymmetrical, rotation);
    }

    @Override
    public IMultiblockAssembleRule createRuleFromNBT(CompoundNBT nbt) {
        IMultiblockAssembleRule rule = new WorldMultiblockAssembleRule();
        rule.deserializeNBT(nbt);
        return rule;
    }

}
