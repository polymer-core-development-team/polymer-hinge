package com.teampolymer.polymer.hinge.common.manager;

import com.teampolymer.polymer.core.api.manager.IWorldMultiblockManager;
import com.teampolymer.polymer.core.api.multiblock.IAssembledMultiblock;
import com.teampolymer.polymer.core.api.multiblock.assembled.IWorldMultiblock;
import com.teampolymer.polymer.core.api.multiblock.part.IMultiblockUnit;
import com.teampolymer.polymer.hinge.client.manager.ClientMultiblockManager;
import com.teampolymer.polymer.hinge.common.chunk.CapabilityChunkMultiblockStorage;
import com.teampolymer.polymer.hinge.common.world.WorldMultiblockSavedData;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Lazy;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class WorldMultiblockManager implements IWorldMultiblockManager {

    public static final Lazy<WorldMultiblockManager> INSTANCE = Lazy.of(WorldMultiblockManager::new);

    public static WorldMultiblockManager getInstance() {
        return INSTANCE.get();
    }

    private WorldMultiblockManager() {

    }

    @Override
    public Optional<IAssembledMultiblock> getById(World world, UUID multiblockId) {
        if (world.isClientSide) {
            return Optional.ofNullable(ClientMultiblockManager.getInstance().getMultiblock(multiblockId));
        }
        return Optional.ofNullable(WorldMultiblockSavedData.get(world).getAssembledMultiblock(multiblockId));
    }

    @Override
    public Optional<IAssembledMultiblock> getByPosition(World world, BlockPos pos, boolean isCore) {
        if (isCore) {
            if (world.isClientSide) {
                return Optional.ofNullable(ClientMultiblockManager.getInstance().getMultiblock(pos));
            }
            return Optional.ofNullable(WorldMultiblockSavedData.get(world).getAssembledMultiblock(pos));
        }
        if (world.isClientSide) {
            throw new UnsupportedOperationException("Could not do this on client!");
        }
        Tuple<UUID, IMultiblockUnit> part = CapabilityChunkMultiblockStorage.getMultiblockPart(world, pos);
        if (part == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(WorldMultiblockSavedData.get(world).getAssembledMultiblock(part.getA()));
    }

    @Override
    public Collection<? extends IAssembledMultiblock> findAll(World world) {
        if (world.isClientSide) {
            return ClientMultiblockManager.getInstance().getAllMultiblocks();
        }
        return WorldMultiblockSavedData.get(world).getAssembledMultiblocks();
    }

    @Override
    public Collection<? extends IAssembledMultiblock> findByChunks(World world, ChunkPos... poses) {
        if (world.isClientSide) {
            return ClientMultiblockManager.getInstance().getChunkMultiblocks(poses);
        }
        return WorldMultiblockSavedData.get(world).getAssembledMultiblocks(poses);
    }


}
