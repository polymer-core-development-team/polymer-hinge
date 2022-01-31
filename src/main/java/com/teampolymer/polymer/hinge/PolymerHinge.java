package com.teampolymer.polymer.hinge;

import com.teampolymer.polymer.core.api.PolymerCoreApi;
import com.teampolymer.polymer.core.api.util.PolymerInternalConstants;
import com.teampolymer.polymer.hinge.common.chunk.CapabilityChunkMultiblockStorage;
import com.teampolymer.polymer.hinge.common.handler.MultiblockRegisterHandler;
import com.teampolymer.polymer.hinge.common.handler.WorldMultiblockTickHandler;
import com.teampolymer.polymer.hinge.common.manager.WorldMultiblockManager;
import com.teampolymer.polymer.hinge.common.network.PolymerHingeNetworking;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(PolymerHinge.MOD_ID)
public class PolymerHinge {
    public static final String MOD_ID = "polymer-hinge";

    public PolymerHinge() {

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus forgeBus = MinecraftForge.EVENT_BUS;
        MultiblockRegisterHandler.MULTIBLOCK_TYPES.register(modBus);

        modBus.addListener(this::preInit);
        modBus.addListener(this::enqueueIMC);
    }

    public void enqueueIMC(InterModEnqueueEvent event) {
        InterModComms.sendTo(PolymerCoreApi.MOD_ID, PolymerInternalConstants.IMC_WORLD_MULTIBLOCK_MANAGER, WorldMultiblockManager::getInstance);
    }


    public void preInit(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            CapabilityChunkMultiblockStorage.register();
            PolymerHingeNetworking.registerMessage();
        });
    }


}
