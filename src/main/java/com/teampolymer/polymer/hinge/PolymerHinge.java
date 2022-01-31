package com.teampolymer.polymer.hinge;

import com.teampolymer.polymer.hinge.common.chunk.CapabilityChunkMultiblockStorage;
import com.teampolymer.polymer.hinge.common.handler.MultiblockRegisterHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(PolymerHinge.MOD_ID)
public class PolymerHinge {
    public static final String MOD_ID = "polymer-hinge";

    PolymerHinge() {

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus forgeBus = MinecraftForge.EVENT_BUS;
        MultiblockRegisterHandler.MULTIBLOCK_TYPES.register(modBus);

        modBus.addListener(this::preInit);
    }


    public void preInit(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            CapabilityChunkMultiblockStorage.register();
        });
    }


}
