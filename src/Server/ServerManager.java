package Server;

import Game.Game;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;

import static Server.PROTOCOL_MESSAGES.*;


/**
 * Classe que gere as conexões e controla o fluxo do jogo.
 */
public class ServerManager {

    private final Users mUsers;
    private final GameStatistics mGameStatistics;
    private String mCurrentUser;
    private Integer number_of_rods;
    private Socket mSocket;
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;

    public ServerManager() {
        mUsers = new Users();
        mGameStatistics = new GameStatistics();
    }

    public boolean requestCredentials(DataOutputStream dataOutputStream, DataInputStream dataInputStream, Socket socket) throws IOException {
        mSocket = socket;
        this.dataInputStream = dataInputStream;
        this.dataOutputStream = dataOutputStream;
        dataOutputStream.writeUTF("Input your username:");
        dataOutputStream.flush();

        String userName = dataInputStream.readUTF();

        dataOutputStream.writeUTF("Input your password:");
        String password = dataInputStream.readUTF();
        if (mUsers.isPasswordValid(userName, password)) {
            mCurrentUser = userName;
            mGameStatistics.addPlayer(userName);
            return true;
        } else
            return requestRestartLogin();
    }

    private boolean requestRestartLogin() throws IOException {
        dataOutputStream.writeUTF("Invalid credentials. Do you want to try to login again? [Y/N]");
        dataOutputStream.flush();

        String response = dataInputStream.readUTF();
        if (response.equals("Y")) {
            System.out.println("Requesting credentials again");
            return requestCredentials(dataOutputStream, dataInputStream, mSocket);
        } else if (response.equals("N"))
            return false;
        else
            return requestRestartLogin();
    }

    public String getPin(String message, DataOutputStream dataOutputStream, DataInputStream dataInputStream) throws IOException {
        dataOutputStream.writeUTF(message);
        dataOutputStream.writeUTF(END_MESSAGE);
        String pin = dataInputStream.readUTF();

        if (!Game.availablePins.contains(pin)) {
            dataOutputStream.writeUTF(String.format(INVALID_PIN, pin));
            return getPin(message, dataOutputStream, dataInputStream);
        }

        return pin;
    }

    public void initGame() throws IOException {
        dataOutputStream.writeUTF("Welcome " + mCurrentUser + ", the server accepted you!");

        String cmd = "";
        String op = "";
        int minMoves;
        Game game;
        int nMovements = 0;
        int opMove = 0;
        String originPin, targetPin;

        dataOutputStream.writeUTF(TOWER_OF_HANOI);
        number_of_rods = setNumberOfRodes(); //Tratamento do input.

        originPin = getPin(ORIGIN_PIN, dataOutputStream, dataInputStream);
        targetPin = getPin(TARGET_PIN, dataOutputStream, dataInputStream);

        while (originPin.equals(targetPin)) {
            dataOutputStream.writeUTF(EQUAL_PINS);
            targetPin = getPin(TARGET_PIN, dataOutputStream, dataInputStream);
        }

        minMoves = calculateMinMoves(number_of_rods); //Calcular o número mínimo de jogadas a usar 2^N - 1.
        dataOutputStream.writeUTF(String.format(OPTIMAL_NUMBER_OF_MOVEMENTS, number_of_rods, minMoves));
        dataOutputStream.writeUTF(ASTERISKS);

        System.out.println("Initializing game.");
        game = new Game(number_of_rods, originPin, targetPin);
        game.initTowers();


        //O jogo fica em loop infinito até haver um vencedor ou o utilizador carregar em "y" ou "Y" para encerrar o jogo.
        while (true) {
            printTower(game, nMovements, game.getBiggestDiskSize(), dataOutputStream); //Desenho do topo + torres com ou sem discos + base.
            dataOutputStream.writeUTF(QUESTION_ABORT_GAME);
            dataOutputStream.writeUTF(END_MESSAGE);
            cmd = dataInputStream.readUTF();
            if (cmd.equalsIgnoreCase("Y"))
                break;
            dataOutputStream.writeUTF(CHOSE_A_POSSIBLE_MOVEMENT);
            dataOutputStream.writeUTF(CHOICES);
            dataOutputStream.writeUTF(END_MESSAGE);
            op = dataInputStream.readUTF();
            opMove = parseInputMenu(op, dataOutputStream); //Tratamento do input a ser usado no menu.

            switch (opMove) { //Menu do programa com opções de jogo de 1 a 6.
                case 1:
                    //MOVE A to B
                    if (game.playMove(0, 1))
                        nMovements++;
                    else
                        dataOutputStream.writeUTF(INVALID_MOVEMENT);
                    break;
                case 2:
                    //MOVE A to C
                    if (game.playMove(0, 2))
                        nMovements++;
                    else
                        dataOutputStream.writeUTF(INVALID_MOVEMENT);
                    break;
                case 3:
                    //MOVE B to A
                    if (game.playMove(1, 0))
                        nMovements++;
                    else
                        dataOutputStream.writeUTF(INVALID_MOVEMENT);
                    break;
                case 4:
                    //MOVE B to C
                    if (game.playMove(1, 2))
                        nMovements++;
                    else
                        dataOutputStream.writeUTF(INVALID_MOVEMENT);
                    break;
                case 5:
                    //MOVE C to A
                    if (game.playMove(2, 0))
                        nMovements++;
                    else
                        dataOutputStream.writeUTF(INVALID_MOVEMENT);
                    break;
                case 6:
                    //MOVE C to B
                    if (game.playMove(2, 1))
                        nMovements++;
                    else
                        dataOutputStream.writeUTF(INVALID_MOVEMENT);
                    break;
                default:
                    showMenu();
                    break;

            }

            if (checkWinner(game)) { //Verificação se há vencedores. Se sim, se há o número de movimentos mínimos.
                mGameStatistics.addDifficultyType(mCurrentUser, number_of_rods, nMovements);
                dataOutputStream.writeUTF(String.format(CONGRATULATIONS, nMovements));
                if (nMovements == minMoves)
                    dataOutputStream.writeUTF(String.format(MINIMAL_NUMBER_STEPS, minMoves));
                showMenu();
                break;
            }
        }
    }

    /**
     * Envia a representação do meu de jogo para o cliente e processa a sua escolha.
     * @throws IOException
     */
    private void showMenu() throws IOException {
        dataOutputStream.writeUTF(SELECT_OPTION);
        dataOutputStream.writeUTF(PLAY_AGAIN);
        dataOutputStream.writeUTF(SHOW_STATISTICS);
        dataOutputStream.writeUTF(QUIT);
        dataOutputStream.writeUTF("\n");
        dataOutputStream.writeUTF(END_MESSAGE);

        switch (dataInputStream.readUTF()) {
            case "1":
                initGame();
                break;
            case "2":
                getStats();
                showMenu();
                break;
            case "Q":
                cleanup();
                break;
            default:
                showMenu();
                break;
        }
    }

    /**
     * Envia para o cliente as estatisticas relativas aos seus jogos.
     * @throws IOException
     */
    public void getStats() throws IOException {
        Map<Integer, DifficultyType> stats = mGameStatistics.getUserStats(mCurrentUser);

        dataOutputStream.writeUTF("Statistics:");

        if (stats.isEmpty())
            dataOutputStream.writeUTF("You have no stats.");

        for (Map.Entry<Integer, DifficultyType> entry : stats.entrySet())
            dataOutputStream.writeUTF(String.format(STATS, entry.getValue().mAmountOfRods, entry.getValue().mTotalAmountOfMoves / entry.getValue().mAmountOfPlays, entry.getValue().mAmountOfPlays));
    }

    /***
     * Leitura do input via utilizador. Utilizacao de uma validacao de input com um tratamento de excepcao;
     * Caso o input nao seja um numero, sera sempre feito um novo pedido ao utilizador para introduzi-lo correctamente;
     * Ainda assim, caso o input seja um numero menor que 3 ou maior que 10, sera feito um novo pedido ao utilizador.
     * @return O numero de rodes introduzidos pelo utilizador.
     */
    public int setNumberOfRodes() throws IOException {
        int auxNumberOfRodes = 0;
        String yourInput = null;
        boolean validInput = false;

        while (!validInput) {
            dataOutputStream.writeUTF(NUMBER_OF_RODS);
            dataOutputStream.writeUTF(END_MESSAGE);
            yourInput = dataInputStream.readUTF();
            try {
                auxNumberOfRodes = Integer.parseInt(yourInput);
                if (auxNumberOfRodes >= 3 && auxNumberOfRodes <= 10)
                    validInput = true;
                else {
                    dataOutputStream.writeUTF(INPUT_RODE_TREATMENT);
                }
            } catch (NumberFormatException ex) {
                dataOutputStream.writeUTF(INPUT_RODE_TREATMENT);
            }
        }

        return auxNumberOfRodes;
    }

    /***
     * Método usado para calcular o número mínimo de movimentos dos discos
     * @param number_of_rods número de discos
     * @return o resultado da fórmula 2^(numero_de_discos) - 1
     */
    public static int calculateMinMoves(int number_of_rods) {

        int resultPow = (int) (Math.pow(2, number_of_rods) - 1);

        return resultPow;
    }

    /***
     * Método para gerar o número de espaços dado o tamanho do maior disco.
     * Basicamente o método é usado para fazer o desenho dos espaços quando não há nenhum disco num dada posição na torre.
     * @param biggestSize tamanho do maior disco.
     * @return string com espaços, em que o número de espaços será o número do maior disco.
     */
    public static String calculateSpaces(int biggestSize) {
        String toReturn = "";
        int nSpaces = biggestSize / 2;

        for (int i = 0; i < nSpaces; i++)
            toReturn = toReturn + " ";

        return toReturn;
    }

    /***
     * Método para gerar uma string com espaços, dado um número de espaços previamente calculado.
     * @param nSpaces número de espaços a ser convertido numa string com espaços.
     * @return string com espaços, em que o número de espaços será o valor introduzido no parâmetro da função.
     */
    public static String calculate_N_Spaces(int nSpaces) {
        String toReturn = "";

        for (int i = 0; i < nSpaces; i++)
            toReturn = toReturn + " ";

        return toReturn;
    }

    /***
     * Método para gerar uma string com o número de asteriscos, dado o tamanho do disco.
     * @param sizeOfDisk tamanho do disco.
     * @return string com asteriscos, em que o número de asteriscos na string será o tamanho do disco.
     */
    public static String printAsterisks(int sizeOfDisk) {
        String toReturn = "";

        for (int i = 0; i < sizeOfDisk; i++)
            toReturn = toReturn + "*";

        return toReturn;
    }

    /***
     * Método auxiliar que faz print do topo da torre, é estático.
     * @param biggestSize o tamanho do maior disco.
     * @return string com espaços e pipes, inicialmente dependente do número do tamanho do maior disco
     * mas que, ao longo do programa, é estático.
     */
    public static String printTop(int biggestSize) {

        return "" + BASE_SPACE + calculateSpaces(biggestSize) + "|" + calculateSpaces(biggestSize) + BASE_SPACE + calculateSpaces(biggestSize) + "|" + calculateSpaces(biggestSize) + BASE_SPACE + calculateSpaces(biggestSize) + "|" + calculateSpaces(biggestSize) + "\n";

    }

    /***
     * Método auxiliar, usado no printTower(...), que faz print das torres com os discos;
     * Inicialmente é usado o método anterior printTop(...) e de seguida o programa chama este;
     * A sua execução baseia-se em: percorrer todos os N discos torre por torre;
     * Acaba quando não houver mais discos nas torres;
     * No fim, é tudo concatenado numa string.
     * @param biggestSize Tamanho do maior disco.
     * @param game objecto do jogo.
     * @return string com o desenho das torres e discos.
     */
    public static String printDisks(int biggestSize, Game game) {
        String toReturn = "";
        int nSpaces = 0;

        for (int i = 0; i < game.getNumberOfRodes(); i++) {
            for (int j = 0; j < 3; j++) {
                if (game.getTowerAtPosition(j).getDiscAtPosition(i).getSize() == 0) {
                    nSpaces = (biggestSize / 2);
                    toReturn = toReturn + BASE_SPACE + calculate_N_Spaces(nSpaces) + "|" + calculate_N_Spaces(nSpaces);
                } else {
                    nSpaces = (biggestSize - game.getTowerAtPosition(j).getDiscAtPosition(i).getSize()) / 2;
                    toReturn = toReturn + BASE_SPACE + calculate_N_Spaces(nSpaces) + printAsterisks(game.getTowerAtPosition(j).getDiscAtPosition(i).getSize()) + calculate_N_Spaces(nSpaces);
                }

                if (j == 2)
                    toReturn = toReturn + "\n";
            }
        }

        return toReturn;
    }

    /***
     * Método principal que faz todo o desenho do jogo: topo das torres + discos (ou não) + base (os asteriscos).
     * @param game objecto do jogo.
     * @param nMovements número de movimentos.
     * @param biggestSize tamanho do maior disco.
     */
    public static void printTower(Game game, int nMovements, int biggestSize, DataOutputStream dataOutputStream) throws IOException {
        String toPrint = "";
        int totalSize = 4 + (game.getBiggestDiskSize() * 3);
        dataOutputStream.writeUTF(String.format(NUMBER_OF_MOVEMENTS, nMovements));
        //System.out.println();

        toPrint = toPrint + printTop(biggestSize);
        toPrint = toPrint + printDisks(biggestSize, game);
        for (int i = 0; i < totalSize; i++)
            toPrint = toPrint + HASH;
        toPrint = toPrint + '\n';

        if (Game.mTarget.equals("A"))
            toPrint = toPrint + BASE_SPACE + calculateSpaces(biggestSize) + "A*" + calculateSpaces(biggestSize) + BASE_SPACE + calculateSpaces(biggestSize) + "B" + calculateSpaces(biggestSize) + BASE_SPACE + calculateSpaces(biggestSize) + "C" + calculateSpaces(biggestSize) + BASE_SPACE;
        else if (Game.mTarget.equals("B"))
            toPrint = toPrint + BASE_SPACE + calculateSpaces(biggestSize) + "A" + calculateSpaces(biggestSize) + BASE_SPACE + calculateSpaces(biggestSize) + "B*" + calculateSpaces(biggestSize) + BASE_SPACE + calculateSpaces(biggestSize) + "C" + calculateSpaces(biggestSize) + BASE_SPACE;
        else
            toPrint = toPrint + BASE_SPACE + calculateSpaces(biggestSize) + "A" + calculateSpaces(biggestSize) + BASE_SPACE + calculateSpaces(biggestSize) + "B" + calculateSpaces(biggestSize) + BASE_SPACE + calculateSpaces(biggestSize) + "C*" + calculateSpaces(biggestSize) + BASE_SPACE;

        dataOutputStream.writeUTF(toPrint);

    }

    /***
     * Método que faz tratamento do input usado no menu do programa;
     * Para haver sucesso, o input do utilizador tem, necessariamente, que ser um número inteiro para depois ser convertido em INT;
     * Caso não seja um número, é lançada uma excepção e feito o devido tratamento.
     * @param op input do utilizador a ser tratado para utilização no menu.
     * @return valor inteiro a ser utilizado no menu do programa.
     */
    public static int parseInputMenu(String op, DataOutputStream dataOutputStream) {
        int validInput = -1;
        int myOp;

        try {
            myOp = Integer.parseInt(op);
            validInput = myOp;
        } catch (NumberFormatException ex) {
            return -1;
        }

        return validInput;
    }

    /***
     * Método que verifica se existe um vencedor do jogo.
     * @param game objecto do jogo, que chama o seu método checkWinner(). Ver comentários do método na classe Game.
     * @return true se existe um vencedor; caso contrário false.
     */
    public static boolean checkWinner(Game game) {
        return game.checkWinner();
    }


    /**
     * Fecha as streams de dados e quebra a ligação com o cliente.
     * @throws IOException
     */
    public void cleanup() throws IOException {
        dataOutputStream.close();
        dataInputStream.close();
        mSocket.close();
    }
}
