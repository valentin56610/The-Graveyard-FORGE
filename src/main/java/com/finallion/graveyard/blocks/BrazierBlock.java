package com.finallion.graveyard.blocks;

import com.finallion.graveyard.blockentities.BrazierBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.client.audio.SoundSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.function.ToIntFunction;


public class BrazierBlock extends Block implements ContainerBlock, IWaterLoggable {
    public static final BooleanProperty LIT;
    public static final BooleanProperty WATERLOGGED;
    public static final ToIntFunction<BlockState> STATE_TO_LUMINANCE;
    protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 15.0D, 16.0D, 15.0D);
    private SimpleParticleType fireType;

    public BrazierBlock(Properties settings, SimpleParticleType fireType) {
        super(settings);
        this.fireType = fireType;
        this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false).setValue(WATERLOGGED, false));
    }

    public ActionResultType use(BlockState p_152822_, World p_152823_, BlockPos p_152824_, PlayerEntity p_152825_, Hand p_152826_, BlockRayTraceResult p_152827_) {
        if (p_152825_.getAbilities().mayBuild && p_152825_.getItemInHand(p_152826_).isEmpty() && p_152822_.getValue(LIT)) {
            extinguish(p_152825_, p_152822_, p_152823_, p_152824_);
            return ActionResultType.sidedSuccess(p_152823_.isClientSide);
        } else {
            return ActionResultType.PASS;
        }
    }

    public FluidState getFluidState(BlockState p_152844_) {
        return p_152844_.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(p_152844_);
    }

    @Override
    public boolean isPathfindable(BlockState p_60475_, IBlockReader p_60476_, BlockPos p_60477_, PathType p_60478_) {
        return false;
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext p_51240_) {
        LevelAccessor levelaccessor = p_51240_.getLevel();
        BlockPos blockpos = p_51240_.getClickedPos();
        boolean flag = levelaccessor.getFluidState(blockpos).getType() == Fluids.WATER;
        return this.defaultBlockState().setValue(WATERLOGGED, flag).setValue(LIT, false);
    }

    public BlockState updateShape(BlockState p_51298_, Direction p_51299_, BlockState p_51300_, LevelAccessor p_51301_, BlockPos p_51302_, BlockPos p_51303_) {
        if (p_51298_.getValue(WATERLOGGED)) {
            p_51301_.scheduleTick(p_51302_, Fluids.WATER, Fluids.WATER.getTickDelay(p_51301_));
        }

        return p_51299_ == Direction.DOWN ? p_51298_ : super.updateShape(p_51298_, p_51299_, p_51300_, p_51301_, p_51302_, p_51303_);
    }

    public boolean placeLiquid(LevelAccessor p_152805_, BlockPos p_152806_, BlockState p_152807_, FluidState p_152808_) {
        if (!p_152807_.getValue(WATERLOGGED) && p_152808_.getType() == Fluids.WATER) {
            BlockState blockstate = p_152807_.setValue(WATERLOGGED, Boolean.valueOf(true));
            if (p_152807_.getValue(LIT)) {
                extinguish((Player)null, blockstate, p_152805_, p_152806_);
            } else {
                p_152805_.setBlock(p_152806_, blockstate, 3);
            }

            p_152805_.scheduleTick(p_152806_, p_152808_.getType(), p_152808_.getType().getTickDelay(p_152805_));
            return true;
        } else {
            return false;
        }
    }


    public VoxelShape getShape(BlockState p_51309_, BlockGetter p_51310_, BlockPos p_51311_, CollisionContext p_51312_) {
        return SHAPE;
    }


    public void animateTick(BlockState p_51287_, Level p_51288_, BlockPos pos, RandomSource p_51290_) {
        if (p_51287_.getValue(LIT)) {
            if (p_51290_.nextInt(20) == 0) {
                p_51288_.playLocalSound((double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS, 0.5F + p_51290_.nextFloat(), p_51290_.nextFloat() * 0.7F + 0.6F, false);
            }

            for (int i = 0; i < 20; ++i) {
                p_51288_.addParticle(fireType,  (double)pos.getX() + 0.25 + Math.abs(p_51290_.nextDouble() - 0.5D), (double)pos.getY() + 1.1D, (double)pos.getZ() + 0.25 + Math.abs(p_51290_.nextDouble() - 0.5D), 0, 0.01, 0);
            }


        }
    }


    public void onProjectileHit(Level p_51244_, BlockState p_51245_, BlockHitResult p_51246_, Projectile p_51247_) {
        BlockPos blockpos = p_51246_.getBlockPos();
        if (!p_51244_.isClientSide && p_51247_.isOnFire() && p_51247_.mayInteract(p_51244_, blockpos) && !p_51245_.getValue(LIT) && !p_51245_.getValue(WATERLOGGED)) {
            p_51244_.setBlock(blockpos, p_51245_.setValue(BlockStateProperties.LIT, Boolean.valueOf(true)), 11);
        }
    }

    public BlockEntity newBlockEntity(BlockPos p_153064_, BlockState p_153065_) {
        return new BrazierBlockEntity(p_153064_, p_153065_);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_51305_) {
        p_51305_.add(LIT, WATERLOGGED);
    }


    public static void extinguish(@javax.annotation.Nullable Player p_151900_, BlockState p_151901_, LevelAccessor p_151902_, BlockPos p_151903_) {
        setLit(p_151902_, p_151901_, p_151903_, false);
        if (p_151901_.getBlock() instanceof BrazierBlock) {
            RandomSource random = p_151902_.getRandom();
            p_151902_.addParticle(ParticleTypes.SMOKE, (double)p_151903_.getX() + 0.5D + random.nextDouble() / 4.0D * (double)(random.nextBoolean() ? 1 : -1), (double)p_151903_.getY() + 0.4D, (double)p_151903_.getZ() + 0.5D + random.nextDouble() / 4.0D * (double)(random.nextBoolean() ? 1 : -1), 0.0D, 0.005D, 0.0D);
            p_151902_.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, (double)p_151903_.getX() + 0.5D + random.nextDouble() / 3.0D * (double)(random.nextBoolean() ? 1 : -1), (double)p_151903_.getY() + random.nextDouble() + random.nextDouble(), (double)p_151903_.getZ() + 0.5D + random.nextDouble() / 3.0D * (double)(random.nextBoolean() ? 1 : -1), 0.0D, 0.07D, 0.0D);

        }

        p_151902_.playSound((Player)null, p_151903_, SoundEvents.CANDLE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
        p_151902_.gameEvent(p_151900_, GameEvent.BLOCK_CHANGE, p_151903_);
    }


    public static void spawnSmokeParticle(Level p_51252_, BlockPos p_51253_, boolean p_51255_) {
        RandomSource random = p_51252_.getRandom();
        SimpleParticleType simpleparticletype = ParticleTypes.CAMPFIRE_COSY_SMOKE;
        p_51252_.addAlwaysVisibleParticle(simpleparticletype, true, (double)p_51253_.getX() + 0.5D + random.nextDouble() / 3.0D * (double)(random.nextBoolean() ? 1 : -1), (double)p_51253_.getY() + random.nextDouble() + random.nextDouble(), (double)p_51253_.getZ() + 0.5D + random.nextDouble() / 3.0D * (double)(random.nextBoolean() ? 1 : -1), 0.0D, 0.07D, 0.0D);
        if (p_51255_) {
            p_51252_.addParticle(ParticleTypes.SMOKE, (double)p_51253_.getX() + 0.5D + random.nextDouble() / 4.0D * (double)(random.nextBoolean() ? 1 : -1), (double)p_51253_.getY() + 0.4D, (double)p_51253_.getZ() + 0.5D + random.nextDouble() / 4.0D * (double)(random.nextBoolean() ? 1 : -1), 0.0D, 0.005D, 0.0D);
        }

    }


    private static void setLit(LevelAccessor p_151919_, BlockState p_151920_, BlockPos p_151921_, boolean p_151922_) {
        p_151919_.setBlock(p_151921_, p_151920_.setValue(LIT, Boolean.valueOf(p_151922_)), 11);
    }

    static {
        LIT = BlockStateProperties.LIT;
        WATERLOGGED = BlockStateProperties.WATERLOGGED;
        STATE_TO_LUMINANCE = (state) -> {
            return (Boolean)state.getValue(LIT) ? 11 : 0;
        };
    }
}
