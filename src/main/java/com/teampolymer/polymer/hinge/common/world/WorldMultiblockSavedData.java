package com.teampolymer.polymer.hinge.common.world;

import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.teampolymer.polymer.core.api.multiblock.assembled.IWorldMultiblock;
import com.teampolymer.polymer.core.api.util.MultiblockUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WorldMultiblockSavedData extends WorldSavedData {
    private static final Logger LOG = LogManager.getLogger();
    private static final String NAME = "polymer_core_multiblock";
    private final World world;

    public WorldMultiblockSavedData(World world) {
        super(NAME);
        this.world = world;
        this.assembledMultiblockMap = new ConcurrentHashMap<>();
    }

    private final ConcurrentHashMap<UUID, IWorldMultiblock> assembledMultiblockMap;
    private final HashBiMap<UUID, BlockPos> positions = HashBiMap.create();
    private final HashMultimap<ChunkPos, UUID> chunksMultiblocks = HashMultimap.create();

    public IWorldMultiblock getAssembledMultiblock(UUID multiblockId) {
        return assembledMultiblockMap.get(multiblockId);
    }

    public IWorldMultiblock getAssembledMultiblock(BlockPos corePos) {
        return assembledMultiblockMap.get(positions.inverse().get(corePos));
    }

    public Collection<IWorldMultiblock> getAssembledMultiblocks() {
        return Collections.unmodifiableCollection(assembledMultiblockMap.values());
    }

    public Collection<IWorldMultiblock> getAssembledMultiblocks(ChunkPos... poses) {
        ArrayList<IWorldMultiblock> result = new ArrayList<>();
        for (ChunkPos pos : poses) {
            for (UUID uuid : chunksMultiblocks.get(pos)) {
                result.add(getAssembledMultiblock(uuid));
            }
        }
        return result;
    }

    public void validateMultiblocksInChunk(ChunkPos pos, Set<UUID> multiblocks) {

        for (UUID uuid : chunksMultiblocks.get(pos)) {
            BlockPos corePos = positions.get(uuid);
            IWorldMultiblock assembledMultiblock = getAssembledMultiblock(uuid);
            if (assembledMultiblock == null) {
                LOG.error("Multiblock {} in {} has no entry, this should not happen!", uuid, corePos);
                removeAssembledMultiblock(uuid);
            } else if (!multiblocks.contains(uuid)) {
                LOG.error("Found invalidate multiblock {} in {}", uuid, corePos);
                assembledMultiblock.disassemble(world);
            } else {
                boolean invalid = assembledMultiblock.validate(world, false);
                if (invalid) {
                    LOG.error("Found invalidate multiblock {} in {}", uuid, corePos);
                    assembledMultiblock.disassemble(world);
                }
            }
        }

    }

    public void addAssembledMultiblock(IWorldMultiblock multiblock) {
        IWorldMultiblock result = assembledMultiblockMap.put(multiblock.getMultiblockId(), multiblock);
        if (result != null) {
            LOG.error("Attempting to add an multiblock with existing id: {}", multiblock.getMultiblockId());
            result.disassemble(world);
            removeAssembledMultiblock(result.getMultiblockId());
        }
        if (positions.containsValue(multiblock.getOffset())) {
            UUID uuid = positions.inverse().get(multiblock.getOffset());
            LOG.error("Attempting to add an multiblock {} to position {} where there are another multiblock {}",
                multiblock.getMultiblockId(), multiblock.getOffset(), uuid);
            IWorldMultiblock assembledMultiblock = getAssembledMultiblock(uuid);
            if (assembledMultiblock != null) {
                assembledMultiblock.disassemble(world);
            }
            removeAssembledMultiblock(uuid);
        }
        positions.put(multiblock.getMultiblockId(), multiblock.getOffset());
        chunksMultiblocks.put(new ChunkPos(multiblock.getOffset()), multiblock.getMultiblockId());
        setDirty();
    }

    public void removeAssembledMultiblock(UUID multiblockId) {
        assembledMultiblockMap.remove(multiblockId);
        BlockPos remove = positions.remove(multiblockId);
        if (remove != null) {
            chunksMultiblocks.remove(new ChunkPos(remove), multiblockId);
        }
        setDirty();
    }

    @Override
    public void load(CompoundNBT nbt) {
        assembledMultiblockMap.clear();
        ListNBT multiblocks = nbt.getList("assembled_multiblocks", 10);
        for (INBT multiblock : multiblocks) {
            IWorldMultiblock assembledMultiblock = (IWorldMultiblock) MultiblockUtils.deserializeNBT(world, multiblock);
            if (assembledMultiblock == null) {
                continue;
            }
            assembledMultiblockMap.put(assembledMultiblock.getMultiblockId(), assembledMultiblock);
            positions.put(assembledMultiblock.getMultiblockId(), assembledMultiblock.getOffset().immutable());
            chunksMultiblocks.put(new ChunkPos(assembledMultiblock.getOffset()), assembledMultiblock.getMultiblockId());
        }
        if (LOG.isDebugEnabled() && assembledMultiblockMap.size() > 0) {
            LOG.debug("Loaded {} machines in world {}", assembledMultiblockMap.size(), world.dimension());
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        ListNBT listNBT = new ListNBT();
        for (IWorldMultiblock value : assembledMultiblockMap.values()) {
            listNBT.add(value.serializeNBT());
        }
        compound.put("assembled_multiblocks", listNBT);
        if (LOG.isDebugEnabled() && listNBT.size() > 0) {
            LOG.debug("Saving {} machines in world {}", listNBT.size(), world.dimension());
        }
        return compound;
    }

    public static WorldMultiblockSavedData get(World worldIn) {
        if (!(worldIn instanceof ServerWorld)) {
            throw new RuntimeException("Attempted to get the data from a client world. This is wrong.");
        }
        ServerWorld world = (ServerWorld) worldIn;
        DimensionSavedDataManager storage = world.getDataStorage();
        return storage.computeIfAbsent(() -> new WorldMultiblockSavedData(world), NAME);
    }
}
