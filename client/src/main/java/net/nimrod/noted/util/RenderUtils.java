package net.nimrod.noted.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.awt.Color;

public class RenderUtils {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static void drawString(MatrixStack matrixStack, String text, float x, float y, Color color) {
        mc.textRenderer.drawWithShadow(matrixStack, text, x, y, color.getRGB(), false);
    }

    public static void drawBoxOutline(MatrixStack matrixStack, Box box, Color color) {
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_DEPTH_TEST);

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        matrixStack.push();
        translateMatrixStack(matrixStack);

        float[] colorFloats = getColorFloats(color);

        Matrix4f matrix = matrixStack.peek().getPositionMatrix();
        BufferBuilder bufferBuilder = RenderSystem.renderThreadTesselator().getBuffer();

        bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        {
            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).color(colorFloats[0], colorFloats[1], colorFloats[2], 1.0f).next();
            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).color(colorFloats[0], colorFloats[1], colorFloats[2], 1.0f).next();

            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).color(colorFloats[0], colorFloats[1], colorFloats[2], 1.0f).next();
            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).color(colorFloats[0], colorFloats[1], colorFloats[2], 1.0f).next();

            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).color(colorFloats[0], colorFloats[1], colorFloats[2], 1.0f).next();
            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ).color(colorFloats[0], colorFloats[1], colorFloats[2], 1.0f).next();

            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ).color(colorFloats[0], colorFloats[1], colorFloats[2], 1.0f).next();
            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).color(colorFloats[0], colorFloats[1], colorFloats[2], 1.0f).next();

            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).color(colorFloats[0], colorFloats[1], colorFloats[2], 1.0f).next();
            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ).color(colorFloats[0], colorFloats[1], colorFloats[2], 1.0f).next();

            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).color(colorFloats[0], colorFloats[1], colorFloats[2], 1.0f).next();
            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).color(colorFloats[0], colorFloats[1], colorFloats[2], 1.0f).next();

            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).color(colorFloats[0], colorFloats[1], colorFloats[2], 1.0f).next();
            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ).color(colorFloats[0], colorFloats[1], colorFloats[2], 1.0f).next();

            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ).color(colorFloats[0], colorFloats[1], colorFloats[2], 1.0f).next();
            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ).color(colorFloats[0], colorFloats[1], colorFloats[2], 1.0f).next();

            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ).color(colorFloats[0], colorFloats[1], colorFloats[2], 1.0f).next();
            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).color(colorFloats[0], colorFloats[1], colorFloats[2], 1.0f).next();

            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).color(colorFloats[0], colorFloats[1], colorFloats[2], 1.0f).next();
            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ).color(colorFloats[0], colorFloats[1], colorFloats[2], 1.0f).next();

            bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ).color(colorFloats[0], colorFloats[1], colorFloats[2], 1.0f).next();
            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ).color(colorFloats[0], colorFloats[1], colorFloats[2], 1.0f).next();

            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ).color(colorFloats[0], colorFloats[1], colorFloats[2], 1.0f).next();
            bufferBuilder.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ).color(colorFloats[0], colorFloats[1], colorFloats[2], 1.0f).next();
        }
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());

        matrixStack.pop();

		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_BLEND);
    }

    private static float[] getColorFloats(Color color) {
        return new float[] { color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f };
    }

    private static void translateMatrixStack(MatrixStack matrixStack) {
        Vec3d cameraPos = mc.getBlockEntityRenderDispatcher().camera.getPos();
        matrixStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
    }

}
