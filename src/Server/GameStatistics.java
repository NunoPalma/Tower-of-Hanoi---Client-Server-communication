package Server;

import java.util.HashMap;
import java.util.Map;

/**
 * Classe usada para armazenar dados registados durante a execução de uma instância de um cliente.
 */
public class GameStatistics {
    private final Map<String, Map<Integer, DifficultyType>> mStats;

    public GameStatistics() {
        mStats = new HashMap<>();
    }

    public void addPlayer(String player) {
        mStats.putIfAbsent(player, new HashMap<>());
    }

    public void addDifficultyType(String player, Integer amountOfRods,Integer amountOfMoves) {
        if (mStats.get(player).containsKey(amountOfRods)) {
            mStats.get(player).get(amountOfRods).addPlay();
            mStats.get(player).get(amountOfRods).addMoves(amountOfMoves);
        } else {
            DifficultyType difficultyType = new DifficultyType(amountOfRods);
            difficultyType.addMoves(amountOfMoves);
            difficultyType.addPlay();
            mStats.get(player).put(amountOfRods, difficultyType);
        }
    }

    public Map<Integer, DifficultyType> getUserStats(String player) {
        return mStats.get(player);
    }
}
