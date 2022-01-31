package com.teampolymer.polymer.hinge.common.network;

import com.teampolymer.polymer.core.api.multiblock.IAssembledMultiblock;
import com.teampolymer.polymer.core.api.util.MultiblockUtils;
import com.teampolymer.polymer.hinge.client.manager.ClientMultiblockManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class PacketSyncWorldMultiblock {
    private Collection<IAssembledMultiblock> multiblocks;

    public PacketSyncWorldMultiblock(PacketBuffer packet) {
        int size = packet.readVarInt();
        multiblocks = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            CompoundNBT nbt = packet.readNbt();
            IAssembledMultiblock multiblock = MultiblockUtils.deserializeNBT(nbt);
            multiblocks.add(multiblock);
        }
    }

    public PacketSyncWorldMultiblock(IAssembledMultiblock multiblock) {
        this.multiblocks = Collections.singletonList(multiblock);
    }

    public PacketSyncWorldMultiblock(Collection<? extends IAssembledMultiblock> multiblocks) {
        this.multiblocks = multiblocks.stream().filter(IAssembledMultiblock::tryInitialize).collect(Collectors.toList());
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeVarInt(multiblocks.size());
        for (IAssembledMultiblock multiblock : multiblocks) {
            buffer.writeNbt(multiblock.serializeNBT());
        }
    }

    public void handler(Supplier<NetworkEvent.Context> ctx) {
        if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            ctx.get().enqueueWork(() -> {
                for (IAssembledMultiblock multiblock : this.multiblocks) {
                    ClientMultiblockManager.getInstance().addMultiblock(multiblock);
                }
            });
        }
        ctx.get().setPacketHandled(true);
    }
}
