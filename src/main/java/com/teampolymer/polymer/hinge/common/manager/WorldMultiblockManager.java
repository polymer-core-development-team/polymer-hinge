package com.teampolymer.polymer.hinge.common.manager;

import com.teampolymer.polymer.core.api.manager.IWorldMultiblockManager;
import com.teampolymer.polymer.core.api.multiblock.IAssembledMultiblock;
import com.teampolymer.polymer.core.api.multiblock.assembled.IWorldMultiblock;
import com.teampolymer.polymer.core.api.multiblock.part.IMultiblockUnit;
import com.teampolymer.polymer.hinge.common.chunk.CapabilityChunkMultiblockStorage;
import com.teampolymer.polymer.hinge.common.world.WorldMultiblockSavedData;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

public class WorldMultiblockManager implements IWorldMultiblockManager {
    @Override
    public Optional<IWorldMultiblock> getById(World world, UUID multiblockId) {
        return Optional.ofNullable(WorldMultiblockSavedData.get(world).getAssembledMultiblock(multiblockId));
    }

    @Override
    public Optional<IWorldMultiblock> getByPosition(World world, BlockPos pos, boolean isCore) {
        if (isCore) {
            return Optional.ofNullable(WorldMultiblockSavedData.get(world).getAssembledMultiblock(pos));
        }

        Tuple<UUID, IMultiblockUnit> part = CapabilityChunkMultiblockStorage.getMultiblockPart(world, pos);
        if (part == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(WorldMultiblockSavedData.get(world).getAssembledMultiblock(part.getA()));
    }

    @Override
    public Collection<IWorldMultiblock> findAll(World world) {
        return WorldMultiblockSavedData.get(world).getAssembledMultiblocks();
    }

    @Override
    public Collection<IWorldMultiblock> findByChunks(World world, ChunkPos... poses) {
        return WorldMultiblockSavedData.get(world).getAssembledMultiblocks(poses);
    }


}
