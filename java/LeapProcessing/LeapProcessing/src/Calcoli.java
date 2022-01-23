public class Calcoli {

    public static final float LIMITE_X_MIN = -200; //valore originale -130
    public static final float LIMITE_Y_MIN = 140; //valore originale 140
    public static final float LIMITE_Z_MIN = -150; //valore originale -90
    public static final float LIMITE_X_MAX = 200; //valore originale 130
    public static final float LIMITE_Y_MAX = 330; //valore originale 330
    public static final float LIMITE_Z_MAX = 150; //valore originale 90

    public static final int NUM_DITA = 5;
    public static final int NUM_OSSA = 4;

    private float[][] coord_x = new float[NUM_DITA][NUM_OSSA];
    private float[][] coord_y = new float[NUM_DITA][NUM_OSSA];
    private float[][] coord_z = new float[NUM_DITA][NUM_OSSA];
    private float[] palmo = new float[3];

    private float x_schermo;
    private float y_schermo;
    private float z_schermo;

    public Calcoli(float x_schermo, float y_schermo, float z_schermo){
        this.inizializzaCoordinate();

        this.x_schermo = x_schermo;
        this.y_schermo = y_schermo;
        this.z_schermo = z_schermo;
    }

    public float calcolaDistanzaPunti(float[] coord_nodo1, float[] coord_nodo2){

        float result_x = (float) Math.pow(coord_nodo1[0]-coord_nodo2[0],2);
        float result_y = (float) Math.pow(coord_nodo1[1]-coord_nodo2[1],2);
        float result_z = (float) Math.pow(coord_nodo1[2]-coord_nodo2[2],2);
        float somma_coord = result_x + result_y + result_z;

        return (float) Math.sqrt(somma_coord);
    }

    public float mapValori(float x, float in_min, float in_max, float out_min, float out_max) {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }

    public void inizializzaCoordinate(){
        for(int a = 0; a < NUM_DITA; a++){
            for(int i = 0; i < NUM_OSSA; i++){
                this.coord_x[a][i] = LIMITE_X_MIN-1;
                this.coord_y[a][i] = LIMITE_Y_MIN-1;
                this.coord_z[a][i] = LIMITE_Z_MIN-1;
            }
        }
        this.palmo[0] = LIMITE_X_MIN-1;
        this.palmo[1] = LIMITE_Y_MIN-1;
        this.palmo[2] = LIMITE_Z_MIN-1;
    }

    public float[][] convertiValore(float[][] coordinate, float limite_min, float limite_max, float dim){
        float[][] nuove_coord = new float[NUM_DITA][NUM_OSSA];
        //MAP LIMITE*2: valore = WITDH: valore_processing
        for(int a = 0; a < NUM_DITA; a++){
            for(int i = 0; i < NUM_OSSA; i++){
                nuove_coord[a][i] = mapValori(coordinate[a][i], limite_min, limite_max, 0, dim);
            }
        }
        return nuove_coord;
    }

    public float[][] getCoord_x() {
        return this.coord_x;
    }

    public float[][] getCoord_y() {
        return this.coord_y;
    }

    public float[][] getCoord_z() {
        return this.coord_z;
    }

    public float[] getPalm() {
        return this.palmo;
    }

    public float[][] getCoordXProcessing(){return this.convertiValore(coord_x, LIMITE_X_MIN, LIMITE_X_MAX, this.x_schermo);}
    public float[][] getCoordYProcessing(){return this.convertiValore(coord_z, LIMITE_Z_MIN, LIMITE_Z_MAX, this.y_schermo);}
    public float[][] getCoordZProcessing(){return this.convertiValore(coord_y, LIMITE_Y_MIN, LIMITE_Y_MAX, this.z_schermo);}
    public float[] getCoordPalmProcessing(){
        return new float[] {mapValori(this.palmo[0], LIMITE_X_MIN, LIMITE_X_MAX, 0, this.x_schermo),
                mapValori(this.palmo[2], LIMITE_Z_MIN, LIMITE_Z_MAX, 0, this.y_schermo),
                mapValori(this.palmo[1], LIMITE_Y_MIN, LIMITE_Y_MAX, 0, this.z_schermo)};
    }

    public void setDataMano(String dati){
       //# divide le ossa, & divide le dita, , divide le coord, % divide le coord dal palmo £ le coord del palmo
       String[] coord = dati.split(",");

       this.memorizzaCoord(coord[0], this.coord_x);
       this.memorizzaCoord(coord[1], this.coord_y);
       this.memorizzaCoord(coord[2], this.coord_z);

       String[] coord_palmo = coord[3].split("£");

       this.palmo[0] = (Float.parseFloat(coord_palmo[0]));
       this.palmo[1] = (Float.parseFloat(coord_palmo[1]));
       this.palmo[2] = (Float.parseFloat(coord_palmo[2]));

    }
    public void memorizzaCoord(String coord, float[][] coord_mem){
        String[] dita = coord.split("&");
        for(int a = 0; a < NUM_DITA; a++){
            for(int i = 0; i < NUM_OSSA; i++){
                coord_mem[a][i] = Float.parseFloat(dita[a].split("#")[i]);
            }
        }
    }
}