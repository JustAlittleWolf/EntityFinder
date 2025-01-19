package me.wolfii.playerfinder.render;

import com.mojang.blaze3d.systems.RenderSystem;
import me.wolfii.playerfinder.Config;
import me.wolfii.playerfinder.PlayerFinder;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.GlUsage;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.*;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.util.Set;

public class PlayerfinderRenderer {

    /*
     * Since I wasn't able to reverse engineer how to draw lines in front of world rendering
     * a portion of this code comes from https://github.com/AdvancedXRay/XRay-Fabric
     */
    public static void render(WorldRenderContext context) {
        if (!PlayerFinder.renderingActive) return;

        Set<Entity> playersToRender = EntityHelper.getEntitiesToHighlight();
        if (playersToRender.isEmpty()) return;

        VertexBuffer vertexBuffer = new VertexBuffer(GlUsage.STATIC_WRITE);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        float tickDelta = context.tickCounter().getTickDelta(true);
        Camera camera = context.camera();
        Vec3d cameraPos = camera.getPos();
        for (Entity playerEntity : playersToRender) {
            drawPlayerHitbox(playerEntity, tickDelta, buffer, cameraPos);
            drawPlayerTracer(playerEntity, camera, tickDelta, buffer, cameraPos);
        }

        vertexBuffer.bind();
        vertexBuffer.upload(buffer.end());
        VertexBuffer.unbind();

        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        context.matrixStack().push();

        RenderSystem.setShader(MinecraftClient.getInstance().getShaderLoader().getOrCreateProgram(ShaderProgramKeys.POSITION_COLOR));
        RenderSystem.depthFunc(GL11.GL_ALWAYS);

        Matrix4f matrix4f = new Matrix4f(context.projectionMatrix());

        matrix4f.lookAt(new Vector3f(), new Vector3f().add(camera.getHorizontalPlane()), camera.getVerticalPlane());
        matrix4f.transformPosition(cameraPos.toVector3f());

        vertexBuffer.bind();
        vertexBuffer.draw(context.matrixStack().peek().getPositionMatrix(), matrix4f, RenderSystem.getShader());
        VertexBuffer.unbind();

        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.disableBlend();
        RenderSystem.depthMask(false);

        context.matrixStack().pop();
        vertexBuffer.close();
    }

    private static void drawPlayerHitbox(Entity playerEntity, float tickDelta, BufferBuilder buffer, Vec3d cameraPos) {
        Box box = EntityHelper.getOffsetBoundingBox(playerEntity, tickDelta, cameraPos);

        PlayerfinderRenderer.drawBox(buffer, box, 1.0f, 1.0f, 1.0f, 1.0f);
        if (Config.renderEyeHeight) drawEyeHeight(buffer, box, playerEntity);
        if (Config.renderFacing) drawFacing(buffer, box, playerEntity, tickDelta);
    }

    private static void drawEyeHeight(BufferBuilder buffer, Box box, Entity entity) {
        PlayerfinderRenderer.drawBox(buffer, (float) (box.minX), (float) (entity.getStandingEyeHeight() - 0.01f + box.minY), (float) (box.minZ), (float) (box.maxX), (float) (entity.getStandingEyeHeight() + 0.01f + box.minY), (float) (box.maxZ), 1.0f, 0.0f, 0.0f, 1.0f);
    }

    private static void drawFacing(BufferBuilder buffer, Box box, Entity entity, float tickDelta) {
        Vec3d vec3d = entity.getRotationVec(tickDelta);
        float entityCenterX = (float) (box.minX + (box.maxX - box.minX) / 2.0d);
        float entityCenterZ = (float) (box.minZ + (box.maxZ - box.minZ) / 2.0d);
        buffer.vertex(entityCenterX, (float) (entity.getStandingEyeHeight() + box.minY), entityCenterZ).color(0, 0, 255, 255).normal((float) vec3d.x, (float) vec3d.y, (float) vec3d.z);
        buffer.vertex((float) (vec3d.x * 2.0 + entityCenterX), (float) (entity.getStandingEyeHeight() + vec3d.y * 2.0 + box.minY), (float) (vec3d.z * 2.0 + entityCenterZ)).color(0, 0, 255, 255).normal((float) vec3d.x, (float) vec3d.y, (float) vec3d.z);
    }

    private static void drawPlayerTracer(Entity entity, Camera camera, float tickDelta, BufferBuilder buffer, Vec3d cameraPos) {
        Box box = EntityHelper.getOffsetBoundingBox(entity, tickDelta, cameraPos);
        float entityCenterX = (float) (box.minX + (box.maxX - box.minX) / 2.0d);
        float entityCenterY = (float) (box.minY + (box.maxY - box.minY) / 2.0d);
        float entityCenterZ = (float) (box.minZ + (box.maxZ - box.minZ) / 2.0d);
        Vector3f horizontalPlane = camera.getHorizontalPlane();
        if (horizontalPlane.x == 0) horizontalPlane.x = 0.0000001f;
        if (horizontalPlane.y == 0) horizontalPlane.y = 0.0000001f;
        if (horizontalPlane.z == 0) horizontalPlane.z = 0.0000001f;
        float scaleFactor = Math.min((Math.abs(1f / camera.getHorizontalPlane().x) + Math.abs(1f / camera.getHorizontalPlane().y) + Math.abs(1f / camera.getHorizontalPlane().z)) / 3f, 100f);
        buffer.vertex(entityCenterX, entityCenterY, entityCenterZ).color(255, 255, 255, 255).normal(1f, 0f, 0f);
        buffer.vertex(camera.getHorizontalPlane().x * scaleFactor, camera.getHorizontalPlane().y * scaleFactor, camera.getHorizontalPlane().z * scaleFactor).color(255, 255, 255, 255).normal(1f, 0f, 0f);
    }

    private static void drawBox(VertexConsumer vertexConsumer, Box box, float red, float green, float blue, float alpha) {
        PlayerfinderRenderer.drawBox(vertexConsumer, (float) (box.minX), (float) (box.minY), (float) (box.minZ), (float) (box.maxX), (float) (box.maxY), (float) (box.maxZ), red, green, blue, alpha, red, green, blue);
    }

    private static void drawBox(VertexConsumer vertexConsumer, float x1, float y1, float z1, float x2, float y2, float z2, float red, float green, float blue, float alpha) {
        PlayerfinderRenderer.drawBox(vertexConsumer, x1, y1, z1, x2, y2, z2, red, green, blue, alpha, red, green, blue);
    }

    @SuppressWarnings("DuplicatedCode")
    private static void drawBox(VertexConsumer vertexConsumer, float x1, float y1, float z1, float x2, float y2, float z2, float red, float green, float blue, float alpha, float xAxisRed, float yAxisGreen, float zAxisBlue) {
        vertexConsumer.vertex(x1, y1, z1).color(red, yAxisGreen, zAxisBlue, alpha).normal(1.0f, 0.0f, 0.0f);
        vertexConsumer.vertex(x2, y1, z1).color(red, yAxisGreen, zAxisBlue, alpha).normal(1.0f, 0.0f, 0.0f);
        vertexConsumer.vertex(x1, y1, z1).color(xAxisRed, green, zAxisBlue, alpha).normal(0.0f, 1.0f, 0.0f);
        vertexConsumer.vertex(x1, y2, z1).color(xAxisRed, green, zAxisBlue, alpha).normal(0.0f, 1.0f, 0.0f);
        vertexConsumer.vertex(x1, y1, z1).color(xAxisRed, yAxisGreen, blue, alpha).normal(0.0f, 0.0f, 1.0f);
        vertexConsumer.vertex(x1, y1, z2).color(xAxisRed, yAxisGreen, blue, alpha).normal(0.0f, 0.0f, 1.0f);
        vertexConsumer.vertex(x2, y1, z1).color(red, green, blue, alpha).normal(0.0f, 1.0f, 0.0f);
        vertexConsumer.vertex(x2, y2, z1).color(red, green, blue, alpha).normal(0.0f, 1.0f, 0.0f);
        vertexConsumer.vertex(x2, y2, z1).color(red, green, blue, alpha).normal(-1.0f, 0.0f, 0.0f);
        vertexConsumer.vertex(x1, y2, z1).color(red, green, blue, alpha).normal(-1.0f, 0.0f, 0.0f);
        vertexConsumer.vertex(x1, y2, z1).color(red, green, blue, alpha).normal(0.0f, 0.0f, 1.0f);
        vertexConsumer.vertex(x1, y2, z2).color(red, green, blue, alpha).normal(0.0f, 0.0f, 1.0f);
        vertexConsumer.vertex(x1, y2, z2).color(red, green, blue, alpha).normal(0.0f, -1.0f, 0.0f);
        vertexConsumer.vertex(x1, y1, z2).color(red, green, blue, alpha).normal(0.0f, -1.0f, 0.0f);
        vertexConsumer.vertex(x1, y1, z2).color(red, green, blue, alpha).normal(1.0f, 0.0f, 0.0f);
        vertexConsumer.vertex(x2, y1, z2).color(red, green, blue, alpha).normal(1.0f, 0.0f, 0.0f);
        vertexConsumer.vertex(x2, y1, z2).color(red, green, blue, alpha).normal(0.0f, 0.0f, -1.0f);
        vertexConsumer.vertex(x2, y1, z1).color(red, green, blue, alpha).normal(0.0f, 0.0f, -1.0f);
        vertexConsumer.vertex(x1, y2, z2).color(red, green, blue, alpha).normal(1.0f, 0.0f, 0.0f);
        vertexConsumer.vertex(x2, y2, z2).color(red, green, blue, alpha).normal(1.0f, 0.0f, 0.0f);
        vertexConsumer.vertex(x2, y1, z2).color(red, green, blue, alpha).normal(0.0f, 1.0f, 0.0f);
        vertexConsumer.vertex(x2, y2, z2).color(red, green, blue, alpha).normal(0.0f, 1.0f, 0.0f);
        vertexConsumer.vertex(x2, y2, z1).color(red, green, blue, alpha).normal(0.0f, 0.0f, 1.0f);
        vertexConsumer.vertex(x2, y2, z2).color(red, green, blue, alpha).normal(0.0f, 0.0f, 1.0f);
    }
}
