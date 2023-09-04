package iafenvoy.accountswitcher.gui;

import iafenvoy.accountswitcher.config.Account;
import iafenvoy.accountswitcher.config.AccountManager;
import iafenvoy.accountswitcher.utils.ButtonWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public class DeleteScreen extends Screen {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private final AccountScreen parent;
    private final Account account;

    public DeleteScreen(Account account, AccountScreen parent) {
        super(Text.translatable("as.gui.delete.title"));
        this.account = account;
        this.parent = parent;
    }

    public void openParent() {
        client.setScreen(parent);
    }

    @Override
    protected void init() {
        super.init();
        this.addField(new ButtonWidget(this.width / 2 - 100, this.height / 2, 100, 20, Text.translatable("as.gui.Accept"), button -> {
            AccountManager.INSTANCE.deleteAccount(account);
            AccountManager.INSTANCE.save();
            AccountManager.CURRENT = null;
            this.openParent();
        }));
        this.addField(new ButtonWidget(this.width / 2, this.height / 2, 100, 20, Text.translatable("as.gui.Cancel"), button -> this.openParent()));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context);
        context.drawCenteredTextWithShadow(textRenderer, this.title, this.width / 2, this.height / 2 - 50, 16777215);
        super.render(context, mouseX, mouseY, delta);
    }

    public ClickableWidget addField(ClickableWidget drawable) {
        this.addDrawable(drawable);
        this.addSelectableChild(drawable);
        return drawable;
    }
}
