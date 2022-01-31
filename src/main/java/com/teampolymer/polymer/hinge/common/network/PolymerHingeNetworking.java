package com.teampolymer.polymer.hinge.common.network;

import com.teampolymer.polymer.core.api.PolymerCoreApi;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PolymerHingeNetworking {
    public static SimpleChannel INSTANCE;
    public static final String VERSION = "1.0";
    private static int ID = 0;
    private static final Logger LOG = LogManager.getLogger();

    public static int nextID() {
        return ID++;
    }

    public static void registerMessage() {
        INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(PolymerCoreApi.POLYMER_ID, "hinge"),
            () -> VERSION,
            (version) -> version.equals(VERSION),
            (version) -> version.equals(VERSION)
        );


    }
}
