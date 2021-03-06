package p455w0rd.endermanevo.blocks;

import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import p455w0rd.endermanevo.init.ModCreativeTab;
import p455w0rd.endermanevo.init.ModGlobals;
import p455w0rd.endermanevo.init.ModItems;
import p455w0rd.endermanevo.util.EnumParticles;
import p455w0rd.endermanevo.util.ParticleUtil;

/**
 * @author p455w0rd
 *
 */
public class BlockEnderFlower extends BlockCrops {

	private static final String NAME = "ender_flower";
	public static final AxisAlignedBB[] FLOWER_AABB = new AxisAlignedBB[] {
			new AxisAlignedBB(0.33D, 0.0D, 0.33D, 0.67D, 0.063D, 0.67D),
			new AxisAlignedBB(0.33D, 0.0D, 0.33D, 0.67D, 0.126D, 0.67D),
			new AxisAlignedBB(0.33D, 0.0D, 0.33D, 0.67D, 0.189D, 0.67D),
			new AxisAlignedBB(0.33D, 0.0D, 0.33D, 0.67D, 0.252D, 0.67D),
			new AxisAlignedBB(0.33D, 0.0D, 0.33D, 0.67D, 0.315D, 0.67D),
			new AxisAlignedBB(0.33D, 0.0D, 0.33D, 0.67D, 0.378D, 0.67D),
			new AxisAlignedBB(0.33D, 0.0D, 0.33D, 0.67D, 0.441D, 0.67D),
			new AxisAlignedBB(0.33D, 0.0D, 0.33D, 0.67D, 0.75D, 0.67D)
	};
	public static final List<Block> VALID_SOILS = Lists.newArrayList(Blocks.NETHERRACK, Blocks.DIRT, Blocks.GRASS, Blocks.FARMLAND);
	public static final List<Block> VALID_BONEMEAL_SOILS = Lists.newArrayList(Blocks.END_STONE, Blocks.END_BRICKS, Blocks.END_PORTAL_FRAME);

	public BlockEnderFlower() {
		setUnlocalizedName(NAME);
		setRegistryName(NAME);
		setDefaultState(blockState.getBaseState().withProperty(getAgeProperty(), Integer.valueOf(0)));
		setTickRandomly(true);
		setCreativeTab(ModCreativeTab.TAB);
		setHardness(0.0F);
		setSoundType(SoundType.PLANT);
		disableStats();
	}

	public static boolean isValidSoil(Block block) {
		return BlockEnderFlower.VALID_SOILS.contains(block) || BlockEnderFlower.VALID_BONEMEAL_SOILS.contains(block);
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return FLOWER_AABB[state.getValue(getAgeProperty()).intValue()];
	}

	@Override
	protected boolean canSustainBush(IBlockState state) {
		return isValidSoil(state.getBlock());
	}

	@Override
	protected int getBonemealAgeIncrease(World world) {
		return world.rand.nextInt(8);//MathHelper.getInt(worldIn.rand, 2, 5);
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		checkAndDropBlock(world, pos, state);
		float f = getGrowChance(this, world, pos);
		if (ForgeHooks.onCropsGrowPre(world, pos, state, rand.nextInt((int) (25.0F / f) + 1) == 0)) {
			boolean bonusSoil = VALID_BONEMEAL_SOILS.contains(world.getBlockState(pos.down()).getBlock());
			if (bonusSoil || world.getLightFromNeighbors(pos.up()) <= 8) {
				int i = getAge(state);
				if (i < getMaxAge()) {
					int newAge = bonusSoil ? i + 2 : i + 1;
					if (newAge > getMaxAge()) {
						newAge = getMaxAge();
					}
					world.setBlockState(pos, withAge(newAge), 2);
					ForgeHooks.onCropsGrowPost(world, pos, state, world.getBlockState(pos));
				}
			}
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
		if (getAge(state) >= getMaxAge()) {
			for (int i = 0; i < 2; ++i) {
				double x = (pos.getX() + 0.5D) + (rand.nextDouble() - 0.5D) * 0.34D;
				double y = (pos.getY() + 0.5D) + rand.nextDouble() * 0.55D - 0.25D;
				double z = (pos.getZ() + 0.5D) + (rand.nextDouble() - 0.5D) * 0.34D;
				double sx = (rand.nextDouble() - 0.5D) * 2.0D;
				double sy = -rand.nextDouble();
				double sz = (rand.nextDouble() - 0.5D) * 2.0D;
				ParticleUtil.spawn(EnumParticles.PORTAL_GREEN, world, x, y, z, sx, sy, sz);
			}
		}
	}

	protected static float getGrowChance(Block block, World world, BlockPos pos) {
		float chance = 0.0f;
		if (VALID_BONEMEAL_SOILS.contains(world.getBlockState(pos.down()).getBlock())) {
			chance = 0.5f + world.rand.nextFloat();
			if (chance > 1.0f) {
				chance = 1.0f;
			}
		}
		else {
			chance = BlockCrops.getGrowthChance(block, world, pos);
		}
		return chance;
	}

	@Override
	public boolean canBlockStay(World worldIn, BlockPos pos, IBlockState state) {
		IBlockState soil = worldIn.getBlockState(pos.down());
		return isValidSoil(soil.getBlock()) || soil.getBlock().canSustainPlant(soil, worldIn, pos.down(), EnumFacing.UP, this);
	}

	@Override
	protected Item getSeed() {
		return ModItems.ENDER_FLOWER;
	}

	@Override
	protected Item getCrop() {
		return ModItems.ENDER_FLOWER;
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return getSeed();
	}

	@Override
	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		int age = getAge(state);
		Random rand = world instanceof World ? ((World) world).rand : ModGlobals.RNG;
		drops.clear();
		int numFlowers = 1 + (rand.nextFloat() > 0.97 ? 1 : 0);
		drops.add(new ItemStack(getSeed(), numFlowers));
		if (age >= getMaxAge()) {
			drops.add(new ItemStack(ModItems.ENDER_FRAGMENT, 1 + rand.nextInt(fortune + 1)));
		}
	}

	@Override
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
		IBlockState soil = worldIn.getBlockState(pos.down());
		return super.canPlaceBlockAt(worldIn, pos) && (soil.getBlock().canSustainPlant(soil, worldIn, pos.down(), EnumFacing.UP, this) || isValidSoil(soil.getBlock()));
	}

	@Override
	public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean isClient) {
		return VALID_BONEMEAL_SOILS.contains(world.getBlockState(pos.down()).getBlock()) ? !isMaxAge(state) : false;
	}

	@Override
	public boolean canUseBonemeal(World world, Random rand, BlockPos pos, IBlockState state) {
		Block block = world.getBlockState(pos.down()).getBlock();
		return VALID_BONEMEAL_SOILS.contains(block);
	}

}
