package Server;

import java.util.HashMap;
import java.util.Map;

public class Users {
    Map<String, String> mUsers = new HashMap<>(); // Utilizadores que estão "registados" no servidor e podem aceder às suas funcionalidades


    Users() {
        mUsers.put("admin", "admin");
    }

    public boolean userExists(String user) {
        return mUsers.containsKey(user);
    }

    public boolean isPasswordValid(String user, String password) {
        return userExists(user) && mUsers.get(user).equals(password);
    }
}
