import java.io.*;
import java.util.concurrent.TimeUnit;

import com.leapmotion.leap.*;
import processing.core.PApplet;

class LeapListener extends Listener {

    public static final int NUM_DITA = 5;
    public static final int NUM_OSSA = 4;

    private ClientUDP ck;

    private String[][] coord_x = new String[NUM_DITA][NUM_OSSA];
    private String[][] coord_y = new String[NUM_DITA][NUM_OSSA];
    private String[][] coord_z = new String[NUM_DITA][NUM_OSSA];
    private String[] palmo = new String[3];


    public LeapListener() {
        super();
        try {
            ck = new ClientUDP("LeapListener");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void onInit(Controller controller) {
        System.out.println("Leap is initialized");
    }

    public void onConnect(Controller controller) {
        System.out.println("Leap is connected");
    }

    public void onDisconnect(Controller controller) {
        System.out.println("Leap is disconnected");
    }

    public void onExit(Controller controller) {
        System.out.println("Leap exited");
    }

    public void onFrame(Controller controller) {
        String messaggio_da_inviare = "";
        Frame frame = controller.frame();
        if (frame.hands().isEmpty()) {
            //System.out.println();
        } else {

            //Get hands
            for (Hand hand : frame.hands()) {
                String handType = hand.isLeft() ? "Left hand" : "Right hand";
                Vector handCenter = hand.palmPosition();
                palmo[0] = String.valueOf(handCenter.get(0));
                palmo[1] = String.valueOf(handCenter.get(1));
                palmo[2] = String.valueOf(handCenter.get(2));

                // Get fingers
                int i = 0;
                for (Finger finger : hand.fingers()) {
                    int a = NUM_OSSA - 1;
                    for (Bone.Type boneType : Bone.Type.values()) {
                        Bone bone = finger.bone(boneType);
                        coord_x[i][a] = String.valueOf(bone.nextJoint().get(0));
                        coord_y[i][a] = String.valueOf(bone.nextJoint().get(1));
                        coord_z[i][a] = String.valueOf(bone.nextJoint().get(2));
                        a--;

                    }

                    i++;

                    //Bone bone = finger.bone(Bone.Type.TYPE_DISTAL);
                }
                //this.cl.get().setDataMano(coord_x, coord_y, coord_z, palmo);
                if (handType.equals("Right hand")) {
                    messaggio_da_inviare += "DX_";
                }
                else{messaggio_da_inviare += "SX_";}
                messaggio_da_inviare = this.inviaDati(messaggio_da_inviare);
                messaggio_da_inviare = messaggio_da_inviare+"_";
            }
        }
        try {
            TimeUnit.MILLISECONDS.sleep(50);
            this.ck.inviaMessaggio(messaggio_da_inviare+"_");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public String inviaDati(String messaggio_da_inviare){

        //# divide le ossa, & divide le dita, % divide le coord, divide le coord dal palmo £ le coord del palmo
        messaggio_da_inviare = this.memorizzaDati(this.coord_x, messaggio_da_inviare);
        messaggio_da_inviare = messaggio_da_inviare.concat(",");
        messaggio_da_inviare = this.memorizzaDati(this.coord_y, messaggio_da_inviare);
        messaggio_da_inviare = messaggio_da_inviare.concat(",");
        messaggio_da_inviare = this.memorizzaDati(this.coord_z, messaggio_da_inviare);
        messaggio_da_inviare = messaggio_da_inviare.concat(",");
        for (String s : this.palmo) {
            messaggio_da_inviare = messaggio_da_inviare.concat(s + "£");
        }
        return messaggio_da_inviare;

    }

    public String memorizzaDati(String[][] dati, String messaggio_da_inviare){
        for(int a = 0; a < NUM_DITA; a++){
            for(int i = 0; i < NUM_OSSA; i++){
                messaggio_da_inviare = messaggio_da_inviare.concat(dati[a][i]+"#");
            }
            messaggio_da_inviare = messaggio_da_inviare.concat("&");
        }
        return messaggio_da_inviare;
    }
}

public class LeapProcessing extends PApplet{
    private LeapListener listener;
    private Controller controller;
    private ServerUDP serverUDP;
    private Thread thread_leap;

    private String nome_PAapplet = "PrincipaleProcessing";

    public LeapProcessing(String nome_PAapplet){
        this.nome_PAapplet = nome_PAapplet;
        inizializzazioneLibrerie();
    }

    public static void main(String[] args){}

    public void inizializzazioneLibrerie(){
        try {
            //Inizializzazione del server
            this.serverUDP = new ServerUDP();
            TimeUnit.MILLISECONDS.sleep(1000);

            //Inizializzazione del Leap
            this.listener = new LeapListener();
            this.controller = new Controller();

            //Creazione e avvio del thread leap
            this.thread_leap = new Thread(new Runnable() {
                @Override
                public void run() {
                    controller.addListener(listener);
                    try {
                        System.in.read();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    controller.removeListener(listener);
                }
            });
            this.thread_leap.start();

            //Creazione e avvio del main di Processing
            PApplet.main(this.nome_PAapplet);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
