package Game;

public class Tower {
	
	private int id;
	private Disk[] disks;
	private int capacity;
	private int counter;
	private int pointer;
	
	public Tower(int id, int capacity) {
		this.id = id;
		this.capacity = capacity;
		this.disks = new Disk[capacity];
		this.counter = 0;
		updatePointer();
	}

	/**
	 * Método para obter o valor do id da torre.
	 * @return id da torre.
	 */
	public int getId() {
		return id;
	}
	
	/***
	 * Método para introduzir o id da torre.
	 * @param id da torre.
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	/***
	 * Método para obter o array com todos os discos da torre.
	 * @return array com todos os discos da torre.
	 */
	public Disk[] getDisks() {
		return disks;
	}
	
	/***
	 * Método para introduzir discos na torre.
	 * @param disk Objecto Disk a ser introduzido no array de discos.
	 */
	public void setDisk(Disk disk) {
		disks[counter++] = disk;
	}
	
	/***
	 * Método para inicializar todos os discos no array de discos na torre. Por default, todos os discos, inicialmente
	 * são inicializados com tamanho zero.
	 */
	public void initDisks() {
		for (int i = 0; i < capacity; i++) {
			disks[i] = new Disk(0);
		}
	}
	
	/***
	 * Método para obter a capacidade total de discos que uma torre pode obter.
	 * @return capacidade total de discos que uma torre pode obter
	 */
	public int getCapacity() {
		return capacity;
	}
	
	/***
	 * Método que retorna o contador par-ordenado do array de discos.
	 * @return do número do contador do array de discos.
	 */
	public int getCounter() {
		return counter;
	}
	
	/***
	 * Método para incrementar o valor do contador do array de discos.
	 */
	public void incCounter() {
		counter++;
	}
	
	/***
	 * Método para obter um disco numa dada posição no array de discos.
	 * @param i posição do array de discos.
	 * @return objecto Disk.
	 */
	public Disk getDiscAtPosition(int i) {
		return disks[i];
	}
	
	/***
	 * Método para obter o "ponteiro" da torre. Este ponteiro é apenas uma abstração em que indica a posição actual do disco
	 * com maior disponibilidade na torre.
	 * @return index do disco com maior disponibilidade na torre.
	 */
	public int getPointer() {
		return pointer;
	}
	
	/***
	 * Método que faz o cálculo automático da posição do ponteiro, em que varia com o valor do contador do array de discos.
	 */
	public void updatePointer() {
		pointer = capacity - counter;
	}
	
	/***
	 * Método que faz pop do disco no array de discos.
	 * Se houver apenas um disco na torre, é retirado o disco que fica mais próximo da base;
	 * Caso contrário, executa-se um ciclo iterativo a procura do disco com maior disponibilidade na torre.
	 * Uma vez encontrado, é retirado da torre, decrementa-se o contador e actualiza-se o pointer.
	 */
	public void popDisk() {
		
		if(counter == 1) {
			disks[capacity-1].setSize(0);
		}else {
			for(int i = 0; i < capacity; i++) {
				if(disks[i].getSize() != 0) {
					disks[i].setSize(0);
					break;
				}
			}
		}
		
		counter--;
		updatePointer();
	}
	
	/***
	 * Método que faz push dos discos na torre.
	 * Se a torre estiver vazia, é adicionado um disco na posição mais próxima da base;
	 * Caso contrário, é feito um ciclo iteractvo, down to top, a fim de se verificar a próxima posição disponível para
	 * colocar o disco.
	 * @param size tamanho do disco a ser colocado na torre.
	 */
	public void pushDisk(int size) {
		
		if(counter == 0) {
			disks[capacity-1].setSize(size);
		}else {
			for (int i = capacity-1; i >=0; i--) {
				if(disks[i].getSize() == 0) {
					disks[i].setSize(size);
					break;
				}
			}
		}
		
		counter++;
		updatePointer();
		
	}
	
	
}
