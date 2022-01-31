package com.teampolymer.polymer.hinge.client.manager;

import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.teampolymer.polymer.core.api.multiblock.IAssembledMultiblock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.common.util.Lazy;

import java.util.*;

public class ClientMultiblockManager {
    private static final Lazy<ClientMultiblockManager> instance = Lazy.of(ClientMultiblockManager::new);

    private ClientMultiblockManager() {
    }

    public static ClientMultiblockManager getInstance() {
        return instance.get();
    }

    private final HashMap<UUID, IAssembledMultiblock> multiblocks = new HashMap<>();
    private final HashBiMap<UUID, BlockPos> positions = HashBiMap.create();
    private final HashMultimap<ChunkPos, UUID> chunksMultiblocks = HashMultimap.create();

    public void addMultiblock(IAssembledMultiblock multiblock) {
        if (multiblock.tryInitialize()) {
            UUID uuid = multiblock.getMultiblockId();
            BlockPos pos = multiblock.getOffset();
            ChunkPos chunkPos = new ChunkPos(pos);
            multiblocks.put(uuid, multiblock);
            positions.put(uuid, pos);
            chunksMultiblocks.put(chunkPos, uuid);
        }
    }

    public void removeMultiblock(UUID multiblockId) {
        IAssembledMultiblock multiblock = multiblocks.remove(multiblockId);
        BlockPos remove = positions.remove(multiblockId);
        if (remove != null) {
            chunksMultiblocks.remove(new ChunkPos(remove), multiblockId);
        }
        if (multiblock != null) {
            chunksMultiblocks.remove(new ChunkPos(multiblock.getOffset()), multiblockId);
        }
    }

    public void removeInChunk(ChunkPos pos) {
        for (UUID uuid : chunksMultiblocks.get(pos)) {
            removeMultiblock(uuid);
        }
    }

    public IAssembledMultiblock getMultiblock(UUID multiblockId) {
        return multiblocks.get(multiblockId);
    }


    public IAssembledMultiblock getMultiblock(BlockPos corePos) {
        return getMultiblock(positions.inverse().get(corePos));
    }

    public Collection<IAssembledMultiblock> getAllMultiblocks() {
        return Collections.unmodifiableCollection(multiblocks.values());
    }

    public Collection<IAssembledMultiblock> getChunkMultiblocks(ChunkPos... poses) {
        ArrayList<IAssembledMultiblock> result = new ArrayList<>();
        for (ChunkPos pos : poses) {
            for (UUID uuid : chunksMultiblocks.get(pos)) {
                result.add(getMultiblock(uuid));
            }
        }
        return result;
    }


}
