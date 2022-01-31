package com.teampolymer.polymer.hinge.common.handler;

import com.teampolymer.polymer.core.api.PolymerCoreApi;
import com.teampolymer.polymer.core.api.manager.PolymerRegistries;
import com.teampolymer.polymer.core.api.multiblock.IMultiblockType;
import com.teampolymer.polymer.hinge.common.multiblock.world.MultiblockTypeWorld;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public class MultiblockRegisterHandler {
    public static final DeferredRegister<IMultiblockType> MULTIBLOCK_TYPES = DeferredRegister.create(PolymerRegistries.MULTIBLOCK_TYPES, PolymerCoreApi.POLYMER_ID);

    public static final RegistryObject<IMultiblockType> TYPE_FREE = MULTIBLOCK_TYPES.register("world", MultiblockTypeWorld::new);

}
