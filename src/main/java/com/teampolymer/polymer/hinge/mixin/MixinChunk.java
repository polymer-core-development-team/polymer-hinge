package com.teampolymer.polymer.hinge.mixin;

import com.teampolymer.polymer.hinge.common.handler.WorldMultiblockUpdateHandler;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Chunk.class)
public abstract class MixinChunk {

    @Final
    @Shadow
    private World level;

    @Inject(
        method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Z)Lnet/minecraft/block/BlockState;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/BlockState;onRemove(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Z)V"
        )
    )
    private void injectBlockRemove(BlockPos pos, BlockState newBlock, boolean flag, CallbackInfoReturnable<BlockState> cir) {
        WorldMultiblockUpdateHandler.handleBlockChange(level, (Chunk)(Object)this, pos, newBlock);
    }
}
