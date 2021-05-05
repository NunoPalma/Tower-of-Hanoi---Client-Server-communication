package Game;

public class Game {

    private static final int DEFAULT_SIZE_DISK = 3;
    private Tower[] towers;
    private int number_of_rodes;
    private int biggestDisk = 0;
    public static String availablePins = "ABC";
    public static String mOrigin, mTarget;

    public Game(int number_of_rodes, String origin, String target) {
        towers = new Tower[3];
        this.number_of_rodes = number_of_rodes;
        createTowers();
        mOrigin = origin;
        mTarget = target;
    }

    /***
     * Método para criar todas as três torres com um array de number_of_rode discos.
     */
    public void createTowers() {
        for (int i = 0; i < 3; i++) {
            towers[i] = new Tower(i, number_of_rodes);
        }
    }


    /***
     * Atribuição dos valores de cada disco na tore de modo a inicializar o array de discos.
     * Há o tamanho default de um disco mínimo, que é 3. A medida que vai aumentando o seu nível,
     * o tamanho também aumenta de acordo com: tamanho_normal_do_disco + 2* nível.
     * Se for o disco de nível 2, o seu tamanho será: tamanho = 3 + 2*1 = 5
     * Se for o disco de nível 4, o seu tamanho será: tamanho = 3 + 2*3 = 9
     */
    public void initLeftTowers() {
        Integer origin;
        if (mOrigin.equals("A"))
            origin = 0;
        else if (mOrigin.equals("B"))
            origin = 1;
        else
            origin = 2;

        for (int i = 0; i < number_of_rodes; i++) {
            towers[origin].setDisk(new Disk(DEFAULT_SIZE_DISK + (2 * i)));
            biggestDisk = DEFAULT_SIZE_DISK + (2 * i);
            towers[origin].updatePointer();
        }
    }

    /***
     * Método para inicializar a torre do meio e da direita sem nenhum disco.
     */
    public void initTowers() {

        for (int i = 0; i < 3; i++) {
            towers[i].initDisks();
        }

        initLeftTowers();

    }

    /***
     * Método que retorna o array com todas as towers.
     * @return array com as towers
     */
    public Tower[] getTowers() {
        return towers;
    }

    /***
     * Método que retorna uma torre numa posição i.
     * @param i posição do array.
     * @return torre da posição i do array.
     */
    public Tower getTowerAtPosition(int i) {
        return towers[i];
    }

    /***
     * Método que retorna o tamanho do maior disco.
     * @return o tamanho do maior disco da torre.
     */
    public int getBiggestDiskSize() {
        return biggestDisk;
    }

    /***
     * Método para verificar o número de discos introduzidos inicialmente.
     * @return número de discos introduzidos inicialmente.
     */
    public int getNumberOfRodes() {
        return number_of_rodes;
    }

    /***
     * Método para validar se o caracter do topo da piramide possui algum size.
     * Neste caso valida-se se o pointer está a apontar para a base ou para algum disco da torre.
     * @param idTower id da torre
     * @return Se o pointer for igual a capacidade da torre, significa que está a apontar para a base, ou seja
     * para nenhum disco, logo, retorna false; Retorna true se, eventualmente, for menor que a capacidade, logo aponta pelo menos
     * para o disco a seguir a base.
     */
    public boolean checkMove(int idTower) {
        boolean valid = true;

        if (towers[idTower].getPointer() == towers[idTower].getCapacity())
            valid = false;

        return valid;

    }

    /***
     * Método que faz a movimentação, ou não, dos discos entre torres;
     * Primeiramente é verificado se na torre origem possui algum disco; Se sim, é feita a verificação se a torre de destino
     * está vazia (é um movimento simples); caso contrário, é feita a verificação se o disco da torre de destino possui um
     * tamanho maior que o disco da torre origem. Caso o disco da torre de destino seja maior que a torre origem, o movimento é valido;
     * Por im, retira-se o disco da torre source (pop do disco).
     * @param src torre de origem.
     * @param dst torre de destino
     * @return true se o movimento for válido; false, caso contrário.
     */
    public boolean playMove(int src, int dst) {
        boolean valid = false;

        if (checkMove(src)) {
            if (towers[dst].getPointer() == towers[dst].getCapacity()) {
                towers[dst].pushDisk(towers[src].getDiscAtPosition(towers[src].getPointer()).getSize());
                towers[src].popDisk();
                valid = true;
            } else {
                if (towers[dst].getDiscAtPosition(towers[dst].getPointer()).getSize() > towers[src].getDiscAtPosition(towers[src].getPointer()).getSize()) {
                    towers[dst].pushDisk(towers[src].getDiscAtPosition(towers[src].getPointer()).getSize());
                    towers[src].popDisk();
                    valid = true;
                }
            }
        }

        return valid;

    }

    /***
     * Método para verificar se o jogo acaba havendo um vencedor.
     * Significa que a torre C tem de estar completa com dicos, ou seja, o seu pointer tem de apontar para a posição 0;
     * E as torres A e B têm de estar a apontar para a base, neste caso com o valor da capacidade, ou seja, sem nenhum disco.
     * @return true se houver um vencedor.
     */
    public boolean checkWinner() {
        boolean valid = false;

        if (mTarget.equals("C")) {
            if (towers[2].getPointer() == 0 && towers[0].getPointer() == towers[0].getCapacity() && towers[1].getPointer() == towers[1].getCapacity())
                valid = true;
        } else if (mTarget.equals("B")) {
            if (towers[1].getPointer() == 0 && towers[0].getPointer() == towers[0].getCapacity() && towers[2].getPointer() == towers[2].getCapacity())
                valid = true;
        } else {
            if (towers[0].getPointer() == 0 && towers[2].getPointer() == towers[2].getCapacity() && towers[1].getPointer() == towers[1].getCapacity())
                valid = true;
        }

        return valid;
    }


}
