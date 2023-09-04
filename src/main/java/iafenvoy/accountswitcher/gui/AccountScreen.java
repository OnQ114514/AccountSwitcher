package iafenvoy.accountswitcher.gui;

import iafenvoy.accountswitcher.config.Account;
import iafenvoy.accountswitcher.config.AccountManager;
import iafenvoy.accountswitcher.login.InjectorLogin;
import iafenvoy.accountswitcher.login.MicrosoftLogin;
import iafenvoy.accountswitcher.utils.ButtonWidget;
import iafenvoy.accountswitcher.utils.IllegalMicrosoftAccountException;
import iafenvoy.accountswitcher.utils.ToastUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public class AccountScreen extends Screen {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    public final MicrosoftLogin microsoftLogin = new MicrosoftLogin();
    public final InjectorLogin injectorLogin = new InjectorLogin();
    private final Screen parent;
    private AccountListWidget widget;

    public AccountScreen(Screen parent) {
        super(Text.translatable("as.gui.title"));
        this.parent = parent;
    }

    public void openParent() {
        client.setScreen(this.parent);
    }

    @Override
    protected void init() {
        super.init();
        this.widget = new AccountListWidget(this, client, 100, this.width - 100, 32, this.height - 64, 36);
        this.widget.setAccount(AccountManager.INSTANCE.getAccounts());
        this.addSelectableChild(this.widget);

        int spacing = 20;
        this.addField(new ButtonWidget(10, spacing, 80, 20, Text.translatable("as.gui.Close"), button -> this.openParent()));

        this.addField(new ButtonWidget(this.width - 90, spacing, 80, 20, Text.translatable("as.gui.AddOffline"), button -> client.setScreen(new AddOfflineAccountScreen(this))));
        this.addField(new ButtonWidget(this.width - 90, 2 * spacing, 80, 20, Text.translatable("as.gui.AddMicrosoft"), button -> new Thread(() -> {
            try {
                Account account = microsoftLogin.doAuth(null);
                if (account != Account.EMPTY)
                    this.addAccount(account);
            } catch (IllegalMicrosoftAccountException e) {
                ToastUtil.showToast("as.toast.error.InvalidAccount", "as.toast.error.InvalidAccount.text");
            } catch (Exception e) {
                ToastUtil.showToast("ERROR", e.getLocalizedMessage());
            }
        }, "Microsoft Login").start()));
        this.addField(new ButtonWidget(this.width - 90, 3 * spacing, 80, 20, Text.translatable("as.gui.AddInjector"), button -> client.setScreen(new AddInjectorAccountScreen(this))));
        this.addField(new ButtonWidget(this.width - 90, 4 * spacing, 80, 20, Text.translatable("as.gui.AddCustom"), button -> client.setScreen(new AddCustomAccountScreen(this))));

        this.addField(new ButtonWidget(this.width / 2 - 90, this.height - 12 - 20, 180, 20, Text.translatable("as.gui.UseAccount"), button -> {
            if (this.widget.getSelectedOrNull() != null && this.widget.getSelectedOrNull() instanceof AccountListWidget.AccountEntry)
                ((AccountListWidget.AccountEntry) this.widget.getSelectedOrNull()).getAccount().use(this);
        }));

        this.addField(new ButtonWidget(10, 2 * spacing, 80, 20, Text.translatable("as.gui.RefreshAccount"), button -> {
            if (this.widget.getSelectedOrNull() != null && this.widget.getSelectedOrNull() instanceof AccountListWidget.AccountEntry)
                ((AccountListWidget.AccountEntry) this.widget.getSelectedOrNull()).getAccount().refresh(this);
        }));
        this.addField(new ButtonWidget(10, 3 * spacing, 80, 20, Text.translatable("as.gui.ModifyAccount"), button -> {
            if (this.widget.getSelectedOrNull() != null && this.widget.getSelectedOrNull() instanceof AccountListWidget.AccountEntry)
                AccountManager.INSTANCE.modifyAccount(((AccountListWidget.AccountEntry) this.widget.getSelectedOrNull()).getAccount(), this);
            AccountManager.INSTANCE.save();
            this.refreshWidget();
        }));
        this.addField(new ButtonWidget(10, 4 * spacing, 80, 20, Text.translatable("as.gui.DeleteAccount"), button -> {
            if (this.widget.getSelectedOrNull() != null && this.widget.getSelectedOrNull() instanceof AccountListWidget.AccountEntry)
                client.setScreen(new DeleteScreen(((AccountListWidget.AccountEntry) this.widget.getSelectedOrNull()).getAccount(), this));
            this.refreshWidget();
        }));
    }

    public void addAccount(Account account) {
        AccountManager.INSTANCE.addAccount(account);
        AccountManager.INSTANCE.save();
        this.refreshWidget();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context);
        this.widget.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(textRenderer, this.title, this.width / 2, 20, 16777215);
        context.drawCenteredTextWithShadow(textRenderer, AccountManager.getAccountInfoText(), this.width / 2, this.height - 56, 16777215);
        if (microsoftLogin.getProcess() != null)
            context.drawCenteredTextWithShadow(textRenderer, Text.of(microsoftLogin.getProcess()), this.width / 2, this.height - 44, 16777215);
        if (injectorLogin.getProcess() != null)
            context.drawCenteredTextWithShadow(textRenderer, Text.of(injectorLogin.getProcess()), this.width / 2, this.height - 44, 16777215);

        super.render(context, mouseX, mouseY, delta);
    }

    public void refreshWidget() {
        this.widget.setAccount(AccountManager.INSTANCE.getAccounts());
    }

    public void select(AccountListWidget.Entry entry) {
        this.widget.setSelected(entry);
    }

    public void addField(ClickableWidget drawable) {
        this.addDrawable(drawable);
        this.addSelectableChild(drawable);
    }
}
