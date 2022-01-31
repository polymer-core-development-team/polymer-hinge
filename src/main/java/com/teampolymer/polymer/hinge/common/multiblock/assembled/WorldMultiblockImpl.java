package com.teampolymer.polymer.hinge.common.multiblock.assembled;

import com.teampolymer.polymer.core.api.PolymerCoreApi;
import com.teampolymer.polymer.core.api.multiblock.IArchetypeMultiblock;
import com.teampolymer.polymer.core.api.multiblock.assembled.IWorldMultiblock;
import com.teampolymer.polymer.core.api.multiblock.assembled.IMultiblockAssembleRule;
import com.teampolymer.polymer.core.api.multiblock.part.IMultiblockUnit;
import com.teampolymer.polymer.hinge.common.chunk.CapabilityChunkMultiblockStorage;
import com.teampolymer.polymer.hinge.common.world.WorldMultiblockSavedData;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

public class WorldMultiblockImpl implements IWorldMultiblock {
    private static final Logger LOG = LogManager.getLogger();
    private UUID multiblockId;
    private IMultiblockAssembleRule assembleRule;
    private IArchetypeMultiblock definedMultiblock;
    private Map<BlockPos, IMultiblockUnit> unitsMap;
    private Collection<ChunkPos> crossedChunks;
    private boolean initialized = false;

    @Override
    public UUID getMultiblockId() {
        return multiblockId;
    }

    public WorldMultiblockImpl() {
    }

    public WorldMultiblockImpl(UUID multiblockId, IMultiblockAssembleRule assembleRule, IArchetypeMultiblock definedMultiblock) {
        this.multiblockId = multiblockId;
        this.assembleRule = assembleRule;
        this.definedMultiblock = definedMultiblock;

    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void invalidate() {
        if (!initialized) {
            return;
        }
        unitsMap = null;
        crossedChunks = null;
        initialized = false;
    }

    @Override
    public boolean tryInitialize() {
        if (initialized) {
            return true;
        }
        LOG.debug("Initializing multiblock {}", getMultiblockId());
        Map<BlockPos, IMultiblockUnit> units = assembleRule.mapParts(getOriginalMultiblock());
        if (units == null || units.isEmpty()) {
            return false;
        }
        unitsMap = Collections.unmodifiableMap(units);
        Set<ChunkPos> posSet = units.keySet().stream()
            .map(ChunkPos::new)
            .collect(Collectors.toSet());
        if (posSet.isEmpty()) {
            return false;
        }
        crossedChunks = Collections.unmodifiableSet(
            posSet
        );
        initialized = true;
        return true;

    }


    @Override
    public void disassemble(World world) {
        if (!initialized) {
            LOG.error("The multiblock '{}' not initialized", multiblockId);
            throw new IllegalStateException("Multiblock not initialized");
        }
        for (ChunkPos crossedChunk : getCrossedChunks()) {
            world.getChunk(crossedChunk.x, crossedChunk.z).getCapability(CapabilityChunkMultiblockStorage.MULTIBLOCK_STORAGE)
                .ifPresent(it -> it.removeMultiblock(getMultiblockId()));
        }
        WorldMultiblockSavedData.get(world).removeAssembledMultiblock(multiblockId);
        LOG.debug("The multiblock '{}' disassembled", multiblockId);
    }


    @Override
    public IArchetypeMultiblock getOriginalMultiblock() {
        return definedMultiblock;
    }

    @Override
    public BlockPos getOffset() {
        return assembleRule.getOffset();
    }

    @Override
    public boolean isSymmetrical() {
        return assembleRule.isSymmetrical();
    }

    @Override
    public Rotation getRotation() {
        return assembleRule.getRotation();
    }

    @Override
    public Map<BlockPos, IMultiblockUnit> getUnits() {
        if (!initialized) {
            LOG.error("Multiblock {} not initialized", getMultiblockId());
            return Collections.emptyMap();
        }
        return unitsMap;
    }

    @Override
    public boolean validate(World world, boolean disassemble) {
        if (!initialized) {
            LOG.error("The multiblock '{}' not initialized", multiblockId);
            throw new IllegalStateException("Multiblock not initialized");
        }
        boolean result = true;
        Map<BlockPos, IMultiblockUnit> parts = getUnits();
        for (Map.Entry<BlockPos, IMultiblockUnit> entry : parts.entrySet()) {
            BlockPos testPos = entry.getKey();
            BlockState block = world.getBlockState(testPos);
            if (entry.getValue().test(block)) {
                result = false;
                break;
            }
        }
        if (disassemble && !result) {
            disassemble(world);
        }
        return result;
    }

    @Override
    public IMultiblockAssembleRule getAssembleRule() {
        return assembleRule;
    }

    @Override
    public Collection<ChunkPos> getCrossedChunks() {
        if (!initialized) {
            LOG.error("Multiblock {} not initialized", getMultiblockId());
            return Collections.singleton(new ChunkPos(getOffset()));
        }
        return crossedChunks;
    }


    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putUUID("uuid", multiblockId);
        nbt.putString("define", definedMultiblock.getRegistryName().toString());
        nbt.put("rule", assembleRule.serializeNBT());
        nbt.putString("type", definedMultiblock.getType().getRegistryName().toString());
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        this.multiblockId = nbt.getUUID("uuid");
        String define = nbt.getString("define");
        this.definedMultiblock = PolymerCoreApi.getInstance().getArchetypeManager().findById(new ResourceLocation(define))
            .orElseThrow(() -> new IllegalStateException(String.format("Could not get multiblock %s from NBT", define)));

        this.assembleRule = this.definedMultiblock.getType().createRuleFromNBT(nbt.getCompound("rule"));
    }
}
