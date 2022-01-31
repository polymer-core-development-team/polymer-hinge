package com.teampolymer.polymer.hinge.common.network;

import com.teampolymer.polymer.hinge.client.manager.ClientMultiblockManager;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class PacketRemoveWorldMultiblock {
    private UUID multiblockId;

    public PacketRemoveWorldMultiblock(PacketBuffer buffer) {
        this.multiblockId = buffer.readUUID();
    }

    public PacketRemoveWorldMultiblock(UUID multiblockId) {
        this.multiblockId = multiblockId;
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeUUID(multiblockId);
    }

    public void handler(Supplier<NetworkEvent.Context> ctx) {
        if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            ctx.get().enqueueWork(() -> {
                ClientMultiblockManager.getInstance().removeMultiblock(this.multiblockId);
            });
        }
        ctx.get().setPacketHandled(true);
    }
}
