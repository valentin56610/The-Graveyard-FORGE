package com.finallion.graveyard.world.biomes.features.trees;

import com.finallion.graveyard.world.biomes.features.features.FeatureHelper;
import com.finallion.graveyard.world.biomes.features.trees.config.TGTreeFeatureConfig;
import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;

import java.util.Random;

public class SmallSpruceTree06 extends BaseSpruceTree {
    private final int trunkHeight = 14;


    public SmallSpruceTree06(Codec<TGTreeFeatureConfig> configCodec) {
        super(configCodec);
    }

    @Override
    public boolean place(ISeedReader world, ChunkGenerator generator, Random random, BlockPos blockPos, TGTreeFeatureConfig config) {
        BlockPos.Mutable mutable = new BlockPos.Mutable().set(blockPos);
        BlockState wood = config.woodState;
        BlockState leaf = config.leafState;
        int offsetTrunk = random.nextInt(2);

        if (!FeatureHelper.canBePlaced(world, blockPos.below(), world.getBlockState(blockPos.below()))) {
            return false;
        }

        if (!FeatureHelper.canGenerate(world, blockPos, 14)) {
            return false;
        }

        for (int i = 0; i < trunkHeight + offsetTrunk; i++) {
            world.setBlock(blockPos.above(i), wood, 2);
            mutable.move(0, 1, 0);
        }

        // single tree top
        setLeaves(world, mutable.offset(0, 2, 0), leaf);
        setLeaves(world, mutable.offset(0, 1, 0), leaf);
        setLeaves(world, mutable.offset(0, 0, 0), leaf);
        randomSpreadOne(world, mutable.offset(0, 0, 0), false, 3, config);
        generateOneStar(world, mutable.offset(0, -1, 0), false, config);
        generateTwoStar(world, mutable.offset(0, -2, 0), false, config);

        generateOneStar(world, mutable.offset(0, -4, 0), false, config);
        randomSpreadOne(world, mutable.offset(0, -5, 0), false, 2, config);
        generateTwoStar(world, mutable.offset(0, -6, 0), false, config);

        generateOneStar(world, mutable.offset(0, -8, 0), false, config);
        generateTwoStar(world, mutable.offset(0, -9, 0), false, config);
        randomSpreadThree(world, mutable.offset(0, -10, 0), false, 2, config);
        generateThreeStar(world, mutable.offset(0, -11, 0), false, config);


        return false;
    }
}