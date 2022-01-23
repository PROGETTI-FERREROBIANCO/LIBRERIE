import processing.core.PApplet;

public class HandLP {
    public static final int[][] COLORE_DITA = {{249,65,68}, {248,150,30},
            {144,190,109}, {67,170,139}, {87,117,144}};
    public static final int[] COLORE_PALMO = {249,199,79};

    public static final int NUMERO_DITA = 5;

    private PApplet pApplet;
    private Node palmo;
    private FingerLP[] mano = new FingerLP[NUMERO_DITA];
    private Calcoli cl;

    HandLP(PApplet pApplet, float x_schermo, float y_schermo, float z_schermo){
        this.pApplet = pApplet;
        cl = new Calcoli(x_schermo, y_schermo, z_schermo);
        this.creaMano();
    }

    public void creaMano(){
        //Crea e inizializza le dita
        for(int a = 0; a < this.mano.length; a++){
            this.mano[a] = new FingerLP(this.pApplet, COLORE_DITA[a], COLORE_DITA[a]);
        }

        //Crea e inizializza il palmo
        palmo = new Node(this.pApplet, COLORE_PALMO);
        this.cl.inizializzaCoordinate();

    }

    //Il formato dei tre array sono: Metacarpals, Proximal,Intermediate, Distal
    //Formato: [[OSSA_DITO1], [OSSA_DITO2], [OSSA_DITO3], [OSSA_DITO4]]
    public void aggiornaMano(){

        //Aggiorna le dita
        for(int a = 0; a < this.mano.length; a++){
            this.mano[a].aggiornaDito(this.cl.getCoordXProcessing()[a], this.cl.getCoordYProcessing()[a],
                    this.cl.getCoordZProcessing()[a]);

        }

        //Aggiorna il palmo
        this.palmo.aggiornaNodo(this.cl.getCoordPalmProcessing()[0],
                this.cl.getCoordPalmProcessing()[1],this.cl.getCoordPalmProcessing()[2]);
        this.palmo.disegna();

        this.disegna();

    }

    public void disegna(){
        //Unisce il centro del palmo con le dita
        this.pApplet.strokeWeight(3);
        this.pApplet.stroke(COLORE_DITA[0][0], COLORE_DITA[0][1], COLORE_DITA[0][2]);
        this.pApplet.line((this.mano[0].getDito()[2].getXNodo()),
                this.mano[0].getDito()[2].getYNodo(),
                this.mano[0].getDito()[2].getZNodo(),
                this.palmo.getXNodo(), this.palmo.getYNodo(), this.palmo.getZNodo());

        for(int a = 1; a < this.mano.length; a++) {
            this.pApplet.stroke(COLORE_DITA[a][0], COLORE_DITA[a][1], COLORE_DITA[a][2]);
            this.pApplet.line(this.mano[a].getDito()[3].getXNodo(),
                    this.mano[a].getDito()[3].getYNodo(),
                    this.mano[a].getDito()[3].getZNodo(),
                    this.palmo.getXNodo(), this.palmo.getYNodo(), this.palmo.getZNodo());
        }

        //Disegno il resto della mano
        this.pApplet.strokeWeight(3);
        this.pApplet.stroke(COLORE_PALMO[0], COLORE_PALMO[1], COLORE_PALMO[2]);
        this.pApplet.noFill();
        this.pApplet.beginShape();
        this.pApplet.vertex(this.mano[0].getDito()[2].getNodo()[0],
                this.mano[0].getDito()[2].getNodo()[1], this.mano[0].getDito()[2].getNodo()[2]);
        for(int a = 1; a < this.mano.length; a++){
            float[] coord_nodo = this.mano[a].getDito()[3].getNodo();
            this.pApplet.vertex(coord_nodo[0], coord_nodo[1], coord_nodo[2]);
        }
        //Questo punto ha la x del mignolo, la y della distanza da centro del palmo al medio * DUE e la z del centro
        //del palmo
        this.pApplet.vertex(this.mano[4].getDito()[3].getXNodo(), this.mano[0].getDito()[3].getYNodo()
                ,this.palmo.getZNodo());

        //Questo punto ha la x del pollice, la y della distanza da centro del palmo al medio * 2 e la z del centro
        //del palmo
        this.pApplet.vertex(this.mano[0].getDito()[3].getXNodo(), this.mano[0].getDito()[3].getYNodo()
                ,this.mano[0].getDito()[3].getZNodo());

        this.pApplet.endShape();
    }
    public Calcoli getDataHand(){return this.cl;}

    public float thumbIndexDistance(){
        float distanza = this.cl.calcolaDistanzaPunti(new float[]{this.cl.getCoord_x()[0][0], this.cl.getCoord_y()[0][0],
                this.cl.getCoord_z()[0][0]}, new float[]{this.cl.getCoord_x()[1][0], this.cl.getCoord_y()[1][0],
                this.cl.getCoord_z()[1][0]});
        return this.cl.mapValori(distanza, 0, 150, 58, 108);
    }
}