import processing.core.PApplet;

public class Node {

    public static final float RAGGIO_NODO = 10;
    private PApplet pApplet;
    private float[] coordinate_nodo = new float[3];
    private int[] colore;

    Node(PApplet pApplet, int[] colore){
        //Inizializzazione dell'oggetto pApplet
        this.pApplet = pApplet;
        this.colore = colore;

        //Inizializzazione delle coordinate del nodo;
        for(int a = 0; a < this.coordinate_nodo.length; a++){
            coordinate_nodo[a] = 0;
        }
    }

    public void aggiornaNodo(float x_nodo, float y_nodo, float z_nodo){
        this.setXNodo(x_nodo);
        this.setYNodo(y_nodo);
        this.setZNodo(z_nodo);
    }


    public void disegna(){
        this.pApplet.pushMatrix();
        this.pApplet.fill((float)this.colore[0], (float)this.colore[1], (float)this.colore[2]);
        this.pApplet.translate(this.coordinate_nodo[0], this.coordinate_nodo[1], this.coordinate_nodo[2]);
        this.pApplet.lights();
        this.pApplet.noStroke();
        this.pApplet.sphere(RAGGIO_NODO);
        this.pApplet.popMatrix();
    }

    public float getXNodo(){return this.coordinate_nodo[0];}
    public float getYNodo(){return this.coordinate_nodo[1];}
    public float getZNodo(){return this.coordinate_nodo[2];}
    public float[] getNodo(){return this.coordinate_nodo;}

    public void setXNodo(float x_nodo){this.coordinate_nodo[0]= x_nodo;}
    public void setYNodo(float y_nodo){this.coordinate_nodo[1] = y_nodo;}
    public void setZNodo(float z_nodo){this.coordinate_nodo[2] = z_nodo;}

}