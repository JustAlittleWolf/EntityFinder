package me.wolfii.entityfinder.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.wolfii.entityfinder.EntityFinder;
import me.wolfii.entityfinder.EntityFinderSettings;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@SuppressWarnings("SameParameterValue")
public class EntityFinderRenderer {

    public static final RenderPipeline LINES_THROUGH_WALLS = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath("entityfinder", "pipeline/debug_lines_through_walls"))
            .withDepthStencilState(Optional.empty())
            .build()
    );

    public static final RenderType LINES_THROUGH_WALLS_TYPE = RenderType.create(
        "lines_through_walls",
        RenderSetup.builder(LINES_THROUGH_WALLS).createRenderSetup()
    );

    // Immutable representation of the rendering data extracted from each entity
    private record ExtractedEntityState(
        AABB box,
        float eyeHeight,
        Vec3 viewVector,
        boolean renderEyeHeight,
        boolean renderFacing,
        boolean renderTracers
    ) {
    }

    @SuppressWarnings("resource")
    public static void render(LevelRenderContext context) {
        if (!EntityFinder.shouldRender) return;

        Set<Entity> entitiesToRender = EntityFinder.getHighlightedEntities();
        if (entitiesToRender.isEmpty()) return;

        PoseStack poseStack = context.poseStack();
        SubmitNodeCollector collector = context.submitNodeCollector();
        Vec3 cameraPos = context.gameRenderer().mainCamera().position();
        Vector3fc cameraLook = context.gameRenderer().mainCamera().forwardVector();
        float tickDelta = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);

        boolean renderTracers = EntityFinderSettings.renderTracers;
        boolean renderEyeHeight = EntityFinderSettings.renderEyeHeight;
        boolean renderFacing = EntityFinderSettings.renderFacing;

        List<ExtractedEntityState> extractedStates = new ArrayList<>(entitiesToRender.size());
        for (Entity entity : entitiesToRender) {
            AABB box = getOffsetBoundingBox(entity, tickDelta, cameraPos);
            float eyeHeight = (float) (entity.getEyeHeight() + box.minY);
            Vec3 viewVector = entity.getViewVector(tickDelta);
            extractedStates.add(new ExtractedEntityState(
                box,
                eyeHeight,
                viewVector,
                renderEyeHeight,
                renderFacing,
                renderTracers
            ));
        }

        collector.submitCustomGeometry(poseStack, LINES_THROUGH_WALLS_TYPE, (pose, buffer) -> {
            Matrix4f poseMatrix = pose.pose();
            for (ExtractedEntityState state : extractedStates) {
                drawHitbox(buffer, poseMatrix, state);
                if (state.renderTracers) {
                    drawTracer(buffer, poseMatrix, state.box, cameraLook);
                }
            }
        });
    }

    private static AABB getOffsetBoundingBox(Entity entity, float tickDelta, Vec3 cameraPos) {
        double offsetX = Mth.lerp(tickDelta, entity.xo, entity.getX()) - entity.getX();
        double offsetY = Mth.lerp(tickDelta, entity.yo, entity.getY()) - entity.getY();
        double offsetZ = Mth.lerp(tickDelta, entity.zo, entity.getZ()) - entity.getZ();
        return entity.getBoundingBox().move(offsetX, offsetY, offsetZ).move(cameraPos.reverse());
    }

    private static void drawHitbox(VertexConsumer buffer, Matrix4f pose, ExtractedEntityState state) {
        drawBox(buffer, pose, state.box, 1.0f, 1.0f, 1.0f, 1.0f);
        if (state.renderEyeHeight) {
            drawEyeHeight(buffer, pose, state.box, state.eyeHeight);
        }
        if (state.renderFacing) {
            drawFacing(buffer, pose, state.box, state.eyeHeight, state.viewVector);
        }
    }

    private static void drawEyeHeight(VertexConsumer buffer, Matrix4f pose, AABB box, float eyeHeight) {
        drawBox(buffer, pose, (float) (box.minX), eyeHeight - 0.01f, (float) (box.minZ), (float) (box.maxX), eyeHeight + 0.01f, (float) (box.maxZ), 1.0f, 0.0f, 0.0f, 1.0f);
    }

    private static void drawFacing(VertexConsumer buffer, Matrix4f pose, AABB box, float eyeHeight, Vec3 viewVector) {
        float entityCenterX = (float) (box.minX + (box.maxX - box.minX) / 2.0d);
        float entityCenterZ = (float) (box.minZ + (box.maxZ - box.minZ) / 2.0d);
        addVertex(buffer, pose, entityCenterX, eyeHeight, entityCenterZ, 0.0f, 0.0f, 1.0f, 1.0f, (float) viewVector.x, (float) viewVector.y, (float) viewVector.z);
        addVertex(buffer, pose, (float) (viewVector.x * 2.0 + entityCenterX), (float) (viewVector.y * 2.0 + eyeHeight), (float) (viewVector.z * 2.0 + entityCenterZ), 0.0f, 0.0f, 1.0f, 1.0f, (float) viewVector.x, (float) viewVector.y, (float) viewVector.z);
    }

    private static void drawTracer(VertexConsumer buffer, Matrix4f pose, AABB box, Vector3fc lookVector) {
        float entityCenterX = (float) (box.minX + (box.maxX - box.minX) / 2.0d);
        float entityCenterY = (float) (box.minY + (box.maxY - box.minY) / 2.0d);
        float entityCenterZ = (float) (box.minZ + (box.maxZ - box.minZ) / 2.0d);
        float startX = lookVector.x() * 5.0f;
        float startY = lookVector.y() * 5.0f;
        float startZ = lookVector.z() * 5.0f;
        addVertex(buffer, pose, startX, startY, startZ, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f);
        addVertex(buffer, pose, entityCenterX, entityCenterY, entityCenterZ, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f);
    }

    private static void addVertex(VertexConsumer consumer, Matrix4f pose, float x, float y, float z, float r, float g, float b, float a, float nx, float ny, float nz) {
        consumer.addVertex(pose, x, y, z)
            .setColor(r, g, b, a)
            .setNormal(nx, ny, nz)
            .setLineWidth(2.0f);
    }

    private static void drawBox(VertexConsumer vertexConsumer, Matrix4f pose, AABB box, float red, float green, float blue, float alpha) {
        drawBox(vertexConsumer, pose, (float) (box.minX), (float) (box.minY), (float) (box.minZ), (float) (box.maxX), (float) (box.maxY), (float) (box.maxZ), red, green, blue, alpha, red, green, blue);
    }

    private static void drawBox(VertexConsumer vertexConsumer, Matrix4f pose, float x1, float y1, float z1, float x2, float y2, float z2, float red, float green, float blue, float alpha) {
        drawBox(vertexConsumer, pose, x1, y1, z1, x2, y2, z2, red, green, blue, alpha, red, green, blue);
    }

    @SuppressWarnings("DuplicatedCode")
    private static void drawBox(VertexConsumer vertexConsumer, Matrix4f pose, float x1, float y1, float z1, float x2, float y2, float z2, float red, float green, float blue, float alpha, float xAxisRed, float yAxisGreen, float zAxisBlue) {
        addVertex(vertexConsumer, pose, x1, y1, z1, red, yAxisGreen, zAxisBlue, alpha, 1.0f, 0.0f, 0.0f);
        addVertex(vertexConsumer, pose, x2, y1, z1, red, yAxisGreen, zAxisBlue, alpha, 1.0f, 0.0f, 0.0f);
        addVertex(vertexConsumer, pose, x1, y1, z1, xAxisRed, green, zAxisBlue, alpha, 0.0f, 1.0f, 0.0f);
        addVertex(vertexConsumer, pose, x1, y2, z1, xAxisRed, green, zAxisBlue, alpha, 0.0f, 1.0f, 0.0f);
        addVertex(vertexConsumer, pose, x1, y1, z1, xAxisRed, yAxisGreen, blue, alpha, 0.0f, 0.0f, 1.0f);
        addVertex(vertexConsumer, pose, x1, y1, z2, xAxisRed, yAxisGreen, blue, alpha, 0.0f, 0.0f, 1.0f);
        addVertex(vertexConsumer, pose, x2, y1, z1, red, green, blue, alpha, 0.0f, 1.0f, 0.0f);
        addVertex(vertexConsumer, pose, x2, y2, z1, red, green, blue, alpha, 0.0f, 1.0f, 0.0f);
        addVertex(vertexConsumer, pose, x2, y2, z1, red, green, blue, alpha, -1.0f, 0.0f, 0.0f);
        addVertex(vertexConsumer, pose, x1, y2, z1, red, green, blue, alpha, -1.0f, 0.0f, 0.0f);
        addVertex(vertexConsumer, pose, x1, y2, z1, red, green, blue, alpha, 0.0f, 0.0f, 1.0f);
        addVertex(vertexConsumer, pose, x1, y2, z2, red, green, blue, alpha, 0.0f, 0.0f, 1.0f);
        addVertex(vertexConsumer, pose, x1, y2, z2, red, green, blue, alpha, 0.0f, -1.0f, 0.0f);
        addVertex(vertexConsumer, pose, x1, y1, z2, red, green, blue, alpha, 0.0f, -1.0f, 0.0f);
        addVertex(vertexConsumer, pose, x1, y1, z2, red, green, blue, alpha, 1.0f, 0.0f, 0.0f);
        addVertex(vertexConsumer, pose, x2, y1, z2, red, green, blue, alpha, 1.0f, 0.0f, 0.0f);
        addVertex(vertexConsumer, pose, x2, y1, z2, red, green, blue, alpha, 0.0f, 0.0f, -1.0f);
        addVertex(vertexConsumer, pose, x2, y1, z1, red, green, blue, alpha, 0.0f, 0.0f, -1.0f);
        addVertex(vertexConsumer, pose, x1, y2, z2, red, green, blue, alpha, 1.0f, 0.0f, 0.0f);
        addVertex(vertexConsumer, pose, x2, y2, z2, red, green, blue, alpha, 1.0f, 0.0f, 0.0f);
        addVertex(vertexConsumer, pose, x2, y1, z2, red, green, blue, alpha, 0.0f, 1.0f, 0.0f);
        addVertex(vertexConsumer, pose, x2, y2, z2, red, green, blue, alpha, 0.0f, 1.0f, 0.0f);
        addVertex(vertexConsumer, pose, x2, y2, z1, red, green, blue, alpha, 0.0f, 0.0f, 1.0f);
        addVertex(vertexConsumer, pose, x2, y2, z2, red, green, blue, alpha, 0.0f, 0.0f, 1.0f);
    }
}
