package Server;

/**
 * Classe usada para representar um tipo de dificuldade e armazenar dados relativos à mesma.
 */
public class DifficultyType {
    public Integer mAmountOfRods;
    public Integer mAmountOfPlays;
    public Integer mTotalAmountOfMoves;

    public DifficultyType(Integer amountOfRods) {
        mAmountOfRods = amountOfRods;
        mAmountOfPlays = 0;
        mTotalAmountOfMoves = 0;
    }

    public void addPlay() {
        mAmountOfPlays++;
    }

    public void addMoves(Integer moves) {
        mTotalAmountOfMoves += moves;
    }
}