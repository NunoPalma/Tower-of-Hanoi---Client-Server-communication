package Server;

import java.util.HashMap;
import java.util.Map;

public class Users {
    Map<String, String> mUsers = new HashMap<>(); // Utilizadores que estão "registados" no servidor e podem aceder às suas funcionalidades


    Users() {
        mUsers.put("admin", "admin");
        mUsers.put("user", "user");
    }

    /**
     *
     * @return true se o utilizador estiver "registado" no servidor, false caso contrário
     */
    public boolean userExists(String user) {
        return mUsers.containsKey(user);
    }

    /**
     * Processa a validação das credênciais do utilizador.
     * @param user Utilizador cuja password vai ser validada
     * @param password Password a ser validada
     * @return true se as credênciais forem válidas, false caso contrário
     */
    public boolean isPasswordValid(String user, String password) {
        return userExists(user) && mUsers.get(user).equals(password);
    }
}
