package com.teampolymer.polymer.hinge.common.world;

import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.teampolymer.polymer.core.api.multiblock.assembled.IWorldMultiblock;
import com.teampolymer.polymer.core.api.util.MultiblockUtils;
import com.teampolymer.polymer.hinge.common.utils.MultiblockLoadRef;
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

public class WorldMultiblockSavedData extends WorldSavedData {
    private static final Logger LOG = LogManager.getLogger();
    private static final String NAME = "polymer_core_multiblock";
    private final World world;

    public WorldMultiblockSavedData(World world) {
        super(NAME);
        this.world = world;
        this.assembledMultiblockMap = new HashMap<>();
    }

    private final HashMap<UUID, IWorldMultiblock> assembledMultiblockMap;
    private final HashMap<UUID, MultiblockLoadRef> multiblockLoadRef = new HashMap<>();
    private final HashBiMap<UUID, BlockPos> positions = HashBiMap.create();
    private final HashMultimap<ChunkPos, UUID> chunksMultiblocks = HashMultimap.create();

    public IWorldMultiblock getAssembledMultiblock(UUID multiblockId) {
        IWorldMultiblock result = assembledMultiblockMap.get(multiblockId);
        if (!result.isInitialized()) {
            if (!result.tryInitialize()) {
                removeAssembledMultiblock(multiblockId);
                return null;
            }
            getMultiblockLoadRef(multiblockId).setTotal(result.getCrossedChunks().size());
        }
        return result;
    }

    private IWorldMultiblock getRawMultiblock(UUID multiblockId) {
        return assembledMultiblockMap.get(multiblockId);
    }


    public IWorldMultiblock getAssembledMultiblock(BlockPos corePos) {
        return getAssembledMultiblock(positions.inverse().get(corePos));
    }

    public Collection<IWorldMultiblock> getAssembledMultiblocks() {
        return Collections.unmodifiableCollection(assembledMultiblockMap.values());
    }

    public Collection<IWorldMultiblock> getAssembledMultiblocks(ChunkPos... poses) {
        ArrayList<IWorldMultiblock> result = new ArrayList<>();
        for (ChunkPos pos : poses) {
            for (UUID uuid : chunksMultiblocks.get(pos)) {
                result.add(getRawMultiblock(uuid));
            }
        }
        return result;
    }

    public void onChunkLoad(ChunkPos pos) {
        for (UUID uuid : chunksMultiblocks.get(pos)) {
            getMultiblockLoadRef(uuid).incRef();
        }
    }

    public void onChunkUnload(ChunkPos pos) {
        for (UUID uuid : chunksMultiblocks.get(pos)) {
            int ref = getMultiblockLoadRef(uuid).decRef();
            if (ref == 0) {
                LOG.debug("Invalidating multiblock {}", uuid);
                getRawMultiblock(uuid).invalidate();
            }
        }
    }

    public void validateMultiblocksInChunk(ChunkPos pos, Set<UUID> multiblocks) {
        for (UUID uuid : chunksMultiblocks.get(pos)) {
            BlockPos corePos = positions.get(uuid);
            IWorldMultiblock assembledMultiblock = getRawMultiblock(uuid);
            if (assembledMultiblock == null) {
                LOG.error("Multiblock {} in {} has no entry, this should not happen!", uuid, corePos);
                removeAssembledMultiblock(uuid);
            } else if (!multiblocks.contains(uuid)) {
                LOG.error("Found invalidate multiblock {} in {}", uuid, corePos);
                if (assembledMultiblock.tryInitialize()) {
                    assembledMultiblock.disassemble(world);
                } else {
                    removeAssembledMultiblock(uuid);
                }
            } else {
                if (assembledMultiblock.tryInitialize()) {
                    boolean isValid = getMultiblockLoadRef(uuid).getStatus() != MultiblockLoadRef.Status.ERROR;
                    isValid = isValid && assembledMultiblock.validate(world, false);
                    if (!isValid) {
                        LOG.error("Found invalidate multiblock {} in {}", uuid, corePos);
                        assembledMultiblock.disassemble(world);
                    }
                } else {
                    removeAssembledMultiblock(uuid);
                }
            }
        }

    }

    public MultiblockLoadRef getMultiblockLoadRef(UUID uuid) {
        if (assembledMultiblockMap.containsKey(uuid)) {
            return multiblockLoadRef.computeIfAbsent(uuid, id -> new MultiblockLoadRef());
        }
        return null;
    }

    public MultiblockLoadRef.Status getMultiblockLoadStatus(UUID uuid) {
        MultiblockLoadRef ref = getMultiblockLoadRef(uuid);
        if (ref == null) {
            return MultiblockLoadRef.Status.REMOVED;
        }
        return ref.getStatus();
    }

    public boolean placeNewMultiblock(IWorldMultiblock multiblock) {
        if (!multiblock.tryInitialize()) {
            return false;
        }
        addWorldMultiblock(multiblock);
        multiblockLoadRef.put(multiblock.getMultiblockId(), MultiblockLoadRef.createLoaded(multiblock.getCrossedChunks().size()));
        return true;
    }

    public void addWorldMultiblock(IWorldMultiblock multiblock) {
        IWorldMultiblock result = assembledMultiblockMap.put(multiblock.getMultiblockId(), multiblock);
        if (result != null) {
            LOG.error("Attempting to add an multiblock with existing id: {}", multiblock.getMultiblockId());
            if (result.tryInitialize()) {
                result.disassemble(world);
            }
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
        multiblockLoadRef.remove(multiblockId);
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
