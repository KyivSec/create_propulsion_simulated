package dev.createpropulsionsimulated.content.ponder;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import dev.createpropulsionsimulated.content.wing.CopycatWingBlock;
import dev.createpropulsionsimulated.registry.CPSBlocks;
import net.createmod.catnip.data.Iterate;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public final class CPSSymmetricSailScenes {
    private CPSSymmetricSailScenes() {
    }

    public static void copycatWingWindmill(final SceneBuilder builder, final SceneBuildingUtil util) {
        final CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("copycat_wing_windmill", "Assembling Windmills with Copycat Wings");
        scene.configureBasePlate(0, 0, 5);
        scene.scaleSceneView(0.9f);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(5);

        final BlockPos bearingPos = util.grid().at(2, 1, 2);
        scene.world().showSection(util.select().position(bearingPos), Direction.DOWN);
        scene.idle(5);
        final ElementLink<WorldSectionElement> axle =
                scene.world().showIndependentSection(util.select().position(bearingPos.above()), Direction.DOWN);
        scene.idle(10);

        for (int i = 0; i < 3; i++) {
            for (final Direction d : Iterate.horizontalDirections) {
                final BlockPos location = bearingPos.above(i + 1).relative(d);
                scene.world().showSectionAndMerge(util.select().position(location), d.getOpposite(), axle);
                scene.idle(2);
            }
        }

        scene.overlay().showText(70)
                .text("Copycat Wings attach to blocks and each other to quickly form windmill blades")
                .pointAt(util.vector().blockSurface(util.grid().at(1, 3, 2), Direction.WEST))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.world().configureCenterOfRotation(axle, util.vector().centerOf(bearingPos));
        scene.world().rotateBearing(bearingPos, 180, 75);
        scene.world().rotateSection(axle, 0, 180, 0, 75);
        scene.idle(76);

        final BlockState dyedLikeState = CPSBlocks.COPYCAT_WING.get().defaultBlockState().setValue(CopycatWingBlock.FACING, Direction.EAST);
        scene.world().setBlock(util.grid().at(2, 3, 3), dyedLikeState, false);
        scene.overlay().showText(60)
                .colored(PonderPalette.BLUE)
                .text("Use the same build style as Symmetric Sails for Aeronautics propellers and windmills")
                .pointAt(util.vector().blockSurface(util.grid().at(2, 3, 3), Direction.WEST))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(70);

        scene.world().rotateBearing(bearingPos, 720, 300);
        scene.world().rotateSection(axle, 0, 720, 0, 300);
    }
}
