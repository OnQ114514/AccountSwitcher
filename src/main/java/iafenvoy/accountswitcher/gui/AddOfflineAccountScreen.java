package iafenvoy.accountswitcher.gui;

import iafenvoy.accountswitcher.config.Account;
import iafenvoy.accountswitcher.login.AuthRequest;
import iafenvoy.accountswitcher.login.OfflineLogin;
import iafenvoy.accountswitcher.utils.ButtonWidget;
import iafenvoy.accountswitcher.utils.IllegalMicrosoftAccountException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class AddOfflineAccountScreen extends Screen {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private final AccountScreen parent;
    private TextFieldWidget usernameField;

    public AddOfflineAccountScreen(AccountScreen parent) {
        super(Text.translatable("as.gui.offline.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        this.usernameField = (TextFieldWidget) this.addField(new TextFieldWidget(client.textRenderer, this.width / 2 - 100, this.height / 2 - 30, 200, 20, Text.empty()));
        this.addField(new ButtonWidget(this.width / 2 - 100, this.height / 2 + 10, 100, 20, Text.translatable("as.gui.Accept"), button -> {
            if (this.usernameField.getText().equals("")) return;
            AuthRequest request=new AuthRequest();
            request.name=this.usernameField.getText();
            Account account;
            try {
                account = new OfflineLogin().doAuth(request);
            } catch (IllegalMicrosoftAccountException e) {
                throw new RuntimeException(e);
            }
            parent.addAccount(account);
            this.openParent();
        }));
        this.addField(new ButtonWidget(this.width / 2, this.height / 2 + 10, 100, 20, Text.translatable("as.gui.Cancel"), button -> this.openParent()));
    }

    public void openParent() {
        client.setScreen(this.parent);
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
