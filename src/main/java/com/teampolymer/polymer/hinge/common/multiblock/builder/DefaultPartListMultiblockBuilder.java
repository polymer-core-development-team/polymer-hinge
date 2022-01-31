package com.teampolymer.polymer.hinge.common.multiblock.builder;

import com.teampolymer.polymer.core.api.PolymerCoreApi;
import com.teampolymer.polymer.core.api.multiblock.IArchetypeMultiblock;
import com.teampolymer.polymer.hinge.api.exceptions.MultiblockBuilderException;
import com.teampolymer.polymer.core.api.multiblock.builder.IPartListMultiblockBuilder;
import com.teampolymer.polymer.core.api.multiblock.part.IMultiblockPart;
import com.teampolymer.polymer.core.api.multiblock.part.IPartLimitConfig;
import com.teampolymer.polymer.core.api.manager.PolymerRegistries;
import com.teampolymer.polymer.hinge.common.multiblock.ArchetypeMultiblockImpl;
import com.teampolymer.polymer.hinge.common.multiblock.ExtensibleMultiblockImpl;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3i;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultPartListMultiblockBuilder extends AbstractMultiblockBuilder<IPartListMultiblockBuilder> implements IPartListMultiblockBuilder {
    private final Map<Vector3i, IMultiblockPart> parts = new HashMap<>();

    @Override
    public IArchetypeMultiblock build() {
        if (machine == null) {
            //TODO: Machine
//            throw new MultiblockBuilderException("'Machine' can not be null");
        }
        if (type == null) {
            type = PolymerRegistries.MULTIBLOCK_TYPES.getValue(new ResourceLocation(PolymerCoreApi.MOD_ID, "type_free"));
        }
        if (parts.get(Vector3i.ZERO) == null) {
            throw new MultiblockBuilderException("Could not find a multiblock core in the structure!");
        }

        int maxX = 0, maxY = 0, maxZ = 0;
        int minX = 0, minY = 0, minZ = 0;

        for (Map.Entry<Vector3i, IMultiblockPart> entry : parts.entrySet()) {
            Vector3i off = entry.getKey();
            maxX = Math.max(off.getX(), maxX);
            maxY = Math.max(off.getY(), maxY);
            maxZ = Math.max(off.getZ(), maxZ);
            minX = Math.min(off.getX(), minX);
            minY = Math.min(off.getY(), minY);
            minZ = Math.min(off.getZ(), minZ);
        }

        Vector3i size = new Vector3i(maxX - minX, maxY - minY, maxZ - minZ);
        List<IPartLimitConfig> limitConfigs = buildLimitConfigs();
        //可拓展的版本
        if (extensions.size() > 0) {
            return new ExtensibleMultiblockImpl(
                components,
                machine,
                size,
                parts,
                type,
                canSymmetrical,
                extensions,
                tags,
                limitConfigs
            );
        }
        //不可拓展的版本
        return new ArchetypeMultiblockImpl(
            components,
            machine,
            size,
            parts,
            type,
            canSymmetrical,
            tags,
            limitConfigs);

    }

    @Override
    public IPartListMultiblockBuilder part(Vector3i offset, IMultiblockPart part) {
        parts.put(offset, part);
        return this;
    }

    @Override
    public IPartListMultiblockBuilder part(int x, int y, int z, IMultiblockPart part) {
        return part(new Vector3i(x, y, z), part);
    }

}
