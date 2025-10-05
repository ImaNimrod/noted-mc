package nimrod.noted.utils;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import static nimrod.noted.Noted.MC;

public class RenderUtils {
    public static void drawString(DrawContext context, String text, int x, int y, int color) {
        context.drawText(MC.textRenderer, text, x, y, color, false);
    }

    public static void draw3DBox(MatrixStack matrices, Camera camera, Box box, int color) {
        VertexConsumerProvider.Immediate vertexConsumerProvider = MC.getBufferBuilders().getEntityVertexConsumers();
        RenderLayer layer = RenderLayer.LINES;
        VertexConsumer bufferBuilder = vertexConsumerProvider.getBuffer(layer);

        build3DLine(matrices, camera, bufferBuilder, box.minX, box.minY, box.minZ, box.maxX, box.minY, box.minZ, color);
        build3DLine(matrices, camera, bufferBuilder, box.maxX, box.minY, box.minZ, box.maxX, box.minY, box.maxZ, color);
        build3DLine(matrices, camera, bufferBuilder, box.maxX, box.minY, box.maxZ, box.minX, box.minY, box.maxZ, color);
        build3DLine(matrices, camera, bufferBuilder, box.minX, box.minY, box.maxZ, box.minX, box.minY, box.minZ, color);
        build3DLine(matrices, camera, bufferBuilder, box.minX, box.minY, box.minZ, box.minX, box.maxY, box.minZ, color);
        build3DLine(matrices, camera, bufferBuilder, box.maxX, box.minY, box.minZ, box.maxX, box.maxY, box.minZ, color);
        build3DLine(matrices, camera, bufferBuilder, box.maxX, box.minY, box.maxZ, box.maxX, box.maxY, box.maxZ, color);
        build3DLine(matrices, camera, bufferBuilder, box.minX, box.minY, box.maxZ, box.minX, box.maxY, box.maxZ, color);
        build3DLine(matrices, camera, bufferBuilder, box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.minZ, color);
        build3DLine(matrices, camera, bufferBuilder, box.maxX, box.maxY, box.minZ, box.maxX, box.maxY, box.maxZ, color);
        build3DLine(matrices, camera, bufferBuilder, box.maxX, box.maxY, box.maxZ, box.minX, box.maxY, box.maxZ, color);
        build3DLine(matrices, camera, bufferBuilder, box.minX, box.maxY, box.maxZ, box.minX, box.maxY, box.minZ, color);

        vertexConsumerProvider.draw(layer);
    }

    private static void build3DLine(MatrixStack matrices, Camera camera, VertexConsumer bufferBuilder, double x1, double y1, double z1, double x2, double y2, double z2, int color) {
        MatrixStack.Entry entry = matrices.peek();
        Matrix4f matrix4f = entry.getPositionMatrix();
        Vec3d cameraPos = camera.getPos();
        Vec3d normalized = new Vec3d(x2 - x1, y2 - y1, z2 - z1).normalize();

        float[] colorComponents = getColorFloats(color);
        float r = colorComponents[0];
        float g = colorComponents[1];
        float b = colorComponents[2];

        bufferBuilder
            .vertex(matrix4f, (float) (x1 - cameraPos.x), (float) (y1 - cameraPos.y), (float) (z1 - cameraPos.z))
            .color(r, g, b, 1.0f).normal(entry, (float) normalized.x, (float) normalized.y, (float) normalized.z);
        bufferBuilder
            .vertex(matrix4f, (float) (x2 - cameraPos.x), (float) (y2 - cameraPos.y), (float) (z2 - cameraPos.z))
            .color(r, g, b, 1.0f).normal(entry, (float) normalized.x, (float) normalized.y, (float) normalized.z);
    }

    private static float[] getColorFloats(int color) {
        float r = ((color >> 16) & 0xff) / 255f;
        float g = ((color >> 8) & 0xff) / 255f;
        float b = (color & 0xff) / 255f;

        return new float[] { r, g, b };
    }
}
