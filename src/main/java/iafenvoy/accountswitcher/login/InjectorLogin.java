package iafenvoy.accountswitcher.login;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import iafenvoy.accountswitcher.config.Account;
import iafenvoy.accountswitcher.utils.NetworkUtil;

public class InjectorLogin implements ILogin{
    private String stats = "";

    public String getProcess() {
        return stats;
    }

    public boolean doLogin(Account account, String server, String name, String password, String alias) {
        try {
            stats = "Login...";
            String url = server + "/authserver/authenticate";
            JsonObject agent = new JsonObject();
            agent.addProperty("name", "Minecraft");
            agent.addProperty("version", 1);

            JsonObject root = new JsonObject();
            root.add("agent", agent);
            root.addProperty("username", name);
            root.addProperty("password", password);

            String data = NetworkUtil.getDataWithJson(url, root);
            JsonObject json = JsonParser.parseString(data).getAsJsonObject();
            if (json.has("error")) {
                stats = json.get("errorMessage").getAsString();
                return false;
            }
            String mcToken = json.get("accessToken").getAsString();
            String uuid = json.get("selectedProfile").getAsJsonObject().get("id").getAsString();
            String username = json.get("selectedProfile").getAsJsonObject().get("name").getAsString();

            account.setAccessToken(password);
            account.setUsername(username);
            account.setUuid(uuid);
            account.setMcToken(mcToken);
            account.setInjectorServer(server);
            account.setAlias(alias);

            stats = "";
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Account doAuth(AuthRequest request) {
        return null;
    }

    @Override
    public void useAccount(Account account) {

    }

    @Override
    public void refreshAccessToken(Account account) {

    }
}
