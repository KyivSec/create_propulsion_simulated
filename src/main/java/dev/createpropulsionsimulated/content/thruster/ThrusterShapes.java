package dev.createpropulsionsimulated.content.thruster;

import net.createmod.catnip.math.VoxelShaper;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.BiFunction;

import static net.minecraft.core.Direction.NORTH;

public final class ThrusterShapes {
    public static final VoxelShaper THRUSTER = ShapeBuilder.shape()
            .add(Block.box(2, 2, 0, 14, 14, 2))
            .add(Block.box(1, 1, 2, 15, 15, 14))
            .add(Block.box(3, 3, 14, 13, 13, 16))
            .forDirectional();

    public static final VoxelShaper CREATIVE_THRUSTER = ShapeBuilder.shape()
            .add(Block.box(3, 3, 0, 13, 13, 10))
            .add(Block.box(4, 4, 10, 12, 12, 12))
            .forDirectional();

    private ThrusterShapes() {
    }

    public static final class ShapeBuilder {
        private VoxelShape shape;

        private ShapeBuilder(final VoxelShape shape) {
            this.shape = shape;
        }

        public static ShapeBuilder shape() {
            return new ShapeBuilder(Shapes.empty());
        }

        public ShapeBuilder add(final VoxelShape shape) {
            this.shape = Shapes.or(this.shape, shape);
            return this;
        }

        public ShapeBuilder erase(final double x1,
                                  final double y1,
                                  final double z1,
                                  final double x2,
                                  final double y2,
                                  final double z2) {
            this.shape = Shapes.join(this.shape, Block.box(x1, y1, z1, x2, y2, z2), BooleanOp.ONLY_FIRST);
            return this;
        }

        public VoxelShape build() {
            return this.shape;
        }

        public VoxelShaper build(final BiFunction<VoxelShape, Direction, VoxelShaper> factory, final Direction direction) {
            return factory.apply(this.shape, direction);
        }

        public VoxelShaper forDirectional(final Direction direction) {
            return this.build(VoxelShaper::forDirectional, direction);
        }

        public VoxelShaper forDirectional() {
            return this.forDirectional(NORTH);
        }
    }
}
