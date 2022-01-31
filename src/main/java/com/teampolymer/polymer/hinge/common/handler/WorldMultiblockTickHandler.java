package com.teampolymer.polymer.hinge.common.handler;

import com.teampolymer.polymer.core.api.multiblock.IArchetypeMultiblock;
import com.teampolymer.polymer.core.api.multiblock.assembled.IWorldMultiblock;
import com.teampolymer.polymer.core.common.event.MultiblockChangeEvent;
import com.teampolymer.polymer.hinge.PolymerHinge;
import com.teampolymer.polymer.hinge.common.manager.WorldMultiblockManager;
import com.teampolymer.polymer.hinge.common.utils.MultiblockLoadStatus;
import com.teampolymer.polymer.hinge.common.world.WorldMultiblockSavedData;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

import java.util.Collection;
import java.util.UUID;

public class WorldMultiblockTickHandler {

    @SubscribeEvent
    public static void tickEnd(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.side != LogicalSide.SERVER) {
            return;
        }
        World world = event.world;
        WorldMultiblockSavedData multiblockSavedData = WorldMultiblockSavedData.get(world);
        Collection<IWorldMultiblock> multiblocks = multiblockSavedData.getAssembledMultiblocks();
        for (IWorldMultiblock multiblock : multiblocks) {
            UUID multiblockId = multiblock.getMultiblockId();
            MultiblockLoadStatus status = multiblockSavedData.getMultiblockLoadStatus(multiblockId);
            if (status == MultiblockLoadStatus.LOADED) {
                tickMultiblock(multiblockSavedData, world, multiblock);
            }
        }
    }

    private static void tickMultiblock(WorldMultiblockSavedData manager, World world, IWorldMultiblock multiblock) {


        IArchetypeMultiblock archetype = multiblock.getArchetype();
        if (archetype.isDistributedMachine()) {
            tickMachine();
        }

        //网络同步
    }

    private static void tickMachine() {

    }
}
