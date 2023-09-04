package iafenvoy.accountswitcher.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import iafenvoy.accountswitcher.gui.AccountScreen;
import iafenvoy.accountswitcher.gui.AddCustomAccountScreen;
import iafenvoy.accountswitcher.gui.AddInjectorAccountScreen;
import iafenvoy.accountswitcher.gui.AddOfflineAccountScreen;
import iafenvoy.accountswitcher.login.AuthRequest;
import iafenvoy.accountswitcher.login.MicrosoftLogin;
import iafenvoy.accountswitcher.login.OfflineLogin;
import iafenvoy.accountswitcher.utils.FileUtil;
import iafenvoy.accountswitcher.utils.IllegalMicrosoftAccountException;
import iafenvoy.accountswitcher.utils.StringUtil;
import iafenvoy.accountswitcher.utils.ToastUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AccountManager {
    public static final AccountManager INSTANCE = new AccountManager();
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static final String FILE_PATH = "./config/accounts.json";
    public static Account CURRENT = null;
    private final List<Account> accounts = new ArrayList<>();

    public AccountManager() {

    }

    public static void setAccountFromClient() {
        CURRENT = AccountManager.INSTANCE.getAccountByUuid(client.getSession().getUuid());
    }

    public static Text getAccountInfoText() {
        String type;
        if (AccountManager.CURRENT == null)
            type = "Error";
        else
            type = AccountManager.CURRENT.getType().getName() + ("".equals(AccountManager.CURRENT.getAlias()) ? ("") : (" - " + AccountManager.CURRENT.getAlias()));
        return Text.translatable("as.titleScreen.nowUse", client.getSession().getUsername(), type);
    }

    public void load() {
        try {
            String data = FileUtil.readFile(FILE_PATH);
            JsonArray json = JsonParser.parseString(data).getAsJsonArray();

            for (JsonElement ele : json) {
                JsonObject obj = ele.getAsJsonObject();
                Account.AccountType type = Account.AccountType.getByName(obj.get("type").getAsString());
                if (type == Account.AccountType.Offline) {
                    AuthRequest request = new AuthRequest();
                    request.name = obj.get("username").getAsString();
                    this.accounts.add(new OfflineLogin().doAuth(request));
                } else if (type == Account.AccountType.Microsoft) {
                    String accessToken = obj.get("accessToken").getAsString();
                    String refreshToken = obj.get("refreshToken").getAsString();
                    String username = obj.get("username").getAsString();
                    String uuid = obj.get("uuid").getAsString();
                    this.accounts.add(new Account(type, accessToken, refreshToken, username, uuid));
                } else {
                    String accessToken = obj.get("accessToken").getAsString();
                    String username = obj.get("username").getAsString();
                    String uuid = obj.get("uuid").getAsString();
                    String injectorServer = obj.get("injectorServer").getAsString();
                    String alias = obj.get("alias").getAsString();
                    Account a = new Account(type, accessToken, "", username, uuid, StringUtil.unicodeToString(alias));
                    a.setInjectorServer(injectorServer);
                    this.accounts.add(a);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void save() {
        try {
            JsonArray array = new JsonArray();
            for (Account account : this.accounts) {
                JsonObject obj = new JsonObject();
                obj.addProperty("type", account.getType().getKey());
                if (account.getType() == Account.AccountType.Offline)
                    obj.addProperty("username", account.getUsername());
                else if (account.getType() == Account.AccountType.Microsoft) {
                    obj.addProperty("accessToken", account.getAccessToken());
                    obj.addProperty("refreshToken", account.getRefreshToken());
                    obj.addProperty("username", account.getUsername());
                    obj.addProperty("uuid", account.getUuid());
                } else {
                    obj.addProperty("accessToken", account.getAccessToken());
                    obj.addProperty("username", account.getUsername());
                    obj.addProperty("uuid", account.getUuid());
                    obj.addProperty("injectorServer", account.getInjectorServer());
                    obj.addProperty("alias", StringUtil.stringToUnicode(account.getAlias()));
                }
                array.add(obj);
            }
            FileUtil.saveFile(FILE_PATH, StringUtil.prettyJson(array));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addAccount(@NotNull Account account) {
        for (int i = 0; i < this.accounts.size(); i++)
            if (this.accounts.get(i).equals(account)) {
                this.accounts.set(i, account);
                return;
            }
        this.accounts.add(account);
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public void deleteAccount(Account account) {
        int index = -1;
        for (int i = 0; i < this.accounts.size(); i++)
            if (this.accounts.get(i).equals(account)) {
                index = i;
                break;
            }
        if (index >= 0)
            this.accounts.remove(index);
    }

    public void modifyAccount(Account account, AccountScreen parent) {
        if (account.getType() == Account.AccountType.Microsoft) {
            new Thread(() -> {
                try {
                    Account acc = new MicrosoftLogin().doAuth(null);
                    if (acc != Account.EMPTY) {
                        this.accounts.remove(account);
                        this.addAccount(acc);
                    }
                } catch (IllegalMicrosoftAccountException e) {
                    ToastUtil.showToast("as.toast.error.InvalidAccount", "as.toast.error.InvalidAccount.text");
                } catch (Exception e) {
                    ToastUtil.showToast("ERROR", e.getLocalizedMessage());
                }
            }, "Microsoft Login").start();
        } else if (account.getType() == Account.AccountType.Injector) {
            client.setScreen(new AddInjectorAccountScreen(parent, account));
        } else if (account.getType() == Account.AccountType.Custom) {
            client.setScreen(new AddCustomAccountScreen(parent, account));
        } else if (account.getType() == Account.AccountType.Offline){
            client.setScreen(new AddOfflineAccountScreen(parent));
        }
    }

    public Account getAccountByUuid(String uuid) {
        for (Account account : this.accounts)
            if (account.getUuid().equals(uuid))
                return account;
        return null;
    }
}
