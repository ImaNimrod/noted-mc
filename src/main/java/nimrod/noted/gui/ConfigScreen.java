package nimrod.noted.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import nimrod.noted.Noted;

public class ConfigScreen extends Screen {
    public ConfigScreen() {
        super(Text.literal("Noted Configuration"));
    }

    @Override
    protected void init() {
        super.init();

        addDrawableChild(
            CheckboxWidget.builder(Text.literal("Swing Hand"), client.textRenderer)
            .callback((widget, checked) -> Noted.CONFIG.swingHand = checked)
            .checked(Noted.CONFIG.swingHand)
            .maxWidth(120)
            .pos(width / 2 - 40, 45)
            .build()
        );

        addDrawableChild(
            ButtonWidget.builder(Text.literal("Open folder"), button -> Util.getOperatingSystem().open(Noted.FOLDER))
            .dimensions(width / 2 - 50, 75, 100, 20)
            .build()
        );
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        context.state.goUpLayer();
        context.drawCenteredTextWithShadow(client.textRenderer, "Noted Configuration:", width / 2, 15, 0xffffffff);
    }
}
