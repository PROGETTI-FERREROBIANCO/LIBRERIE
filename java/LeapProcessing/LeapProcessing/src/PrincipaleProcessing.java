import processing.core.PApplet;

public abstract class PrincipaleProcessing extends PApplet{
    private HandLP[] mani = new HandLP[2];
    private ClientUDP ck;

    public void settings() {
        size(displayWidth, displayHeight, P3D);
    }
    public void setup() {

        for(int a = 0; a < mani.length; a++){
            mani[a] = new HandLP(this, displayWidth, displayHeight, 10);
        }
        try {
            ck = new ClientUDP("PrincipaleProcessing");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public void draw() {
        background(0,0,0);
        this.addPAappletComponents();
        try {
            this.analizzaMessaggioRicevuto();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }


    }

    public void analizzaMessaggioRicevuto() throws Exception {
        String[] messaggio = this.ck.riceviMessaggio().split("°");
        if(messaggio[0].equals("LeapListener")){
            this.disegnaMani(messaggio[1]);
        }
    }

    public void disegnaMani(String messaggio_ricevuto){
        try {
            for (HandLP handLP : mani) {
                handLP.getDataHand().inizializzaCoordinate();
            }
            if(!messaggio_ricevuto.equals("_")){
                    String[] messaggio = messaggio_ricevuto.split("_");


                if (messaggio[0].equals("DX")) {
                    mani[0].getDataHand().setDataMano(messaggio[1]);
                }
                if (messaggio[0].equals("SX")) {
                    mani[1].getDataHand().setDataMano(messaggio[1]);
                }
                if (messaggio.length >= 3 && messaggio[2] != null) {
                    if (messaggio[2].equals("DX")) {
                        mani[0].getDataHand().setDataMano(messaggio[3]);
                    }
                    if (messaggio[2].equals("SX")) {
                        mani[1].getDataHand().setDataMano(messaggio[3]);
                    }
                }

            }
        } catch (Exception e) {
            System.out.println("Eccezione: "+e.getMessage());
        }
        for (HandLP handLP : mani) {
            handLP.aggiornaMano();
        }

    }

    public HandLP[] getHands() {
        //getHands()[0] is the right hand, getHands()[1] is the left hand.
        return mani;
    }

    public abstract void addPAappletComponents();

}
