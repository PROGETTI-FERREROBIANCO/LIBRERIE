import processing.core.PApplet;

public class FingerLP {
    public static final int NUMERO_NODI = 4;
    public static final float SPESSORE_LINEA = 5;
    private PApplet pApplet;
    private Node[] dito = new Node[NUMERO_NODI];
    private int[] colore_dito;

    FingerLP(PApplet pApplet, int[] colore_nodi, int[] colore_dito){
        //Inizializzazione dell'oggetto pApplet
        this.pApplet = pApplet;

        //Inizializzazione delle coordinate del nodo;
        for(int a = 0; a < this.dito.length; a++){
            dito[a] = new Node(this.pApplet, colore_nodi);
        }
        this.colore_dito = colore_dito;
    }

    public void aggiornaDito(float[] coord_x, float[] coord_y, float[] coord_z){
        //Aggiorna i nodi
        for(int a = 0; a < this.dito.length; a++){
            this.dito[a].setXNodo(coord_x[a]);
            this.dito[a].setYNodo(coord_y[a]);
            this.dito[a].setZNodo(coord_z[a]);
        }
        this.disegna();
    }


    public void disegna(){
        //Disegna i nodi
        for (Node node : this.dito) {
            node.disegna();
        }

        //disegna linea
        this.disegnaArco();
    }

    public void disegnaArco(){
        this.pApplet.strokeWeight(SPESSORE_LINEA);
        this.pApplet.stroke(colore_dito[0], colore_dito[1], colore_dito[2]);
        for(int a = 0; a < this.dito.length-1; a++){
            this.pApplet.line(this.dito[a].getXNodo(), this.dito[a].getYNodo(), this.dito[a].getZNodo(),
                    this.dito[a+1].getXNodo(), this.dito[a+1].getYNodo(), this.dito[a+1].getZNodo());
        }

    }

    public Node[] getDito(){return this.dito;}


}