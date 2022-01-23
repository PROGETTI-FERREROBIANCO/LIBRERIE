import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Hashtable;

public class ServerUDP implements Runnable{
    private Thread thread_server;
    private DatagramSocket serverSocket;
    private InetAddress IPAddress;
    //Le porte sono memorizzate in questo modo: LeapProcessing, PrincipaleProcessing, LeapListener
    private Hashtable<String, Integer> dict_port = new Hashtable<String, Integer>();

    private int porta_iniziale;

    private byte[] receiveData = new byte[4096];
    private byte[] sendData = new byte[4096];

    ServerUDP() throws Exception{
        this.serverSocket =  new DatagramSocket(9999);
        this.IPAddress = serverSocket.getLocalAddress();
        this.thread_server = new Thread(this);
        this.thread_server.start();
    }

    public String riceviMessaggio() throws Exception{
        ByteArrayInputStream byteReceive;

        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        serverSocket.receive(receivePacket);
        byteReceive = new ByteArrayInputStream(receivePacket.getData());
        DataInputStream receive = new DataInputStream(byteReceive);
        this.porta_iniziale = receivePacket.getPort();
        String messaggio = receive.readUTF();
        receive.close();
        return messaggio;
    }

    public void inviaMessaggio(String messaggio_da_inviare, int porta)throws Exception{

        if(messaggio_da_inviare == null){
            throw new Exception("La stringa non deve essere nulla");
        }
        ByteArrayOutputStream byteSend = new ByteArrayOutputStream();
        DataOutputStream send = new DataOutputStream(byteSend);
        send.writeUTF(messaggio_da_inviare);
        sendData = byteSend.toByteArray();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, porta);
        serverSocket.send(sendPacket);

        byteSend.reset();
        send.close();
    }

    @Override
    public void run() {
        //Messaggio: nome_thread°messaggio
        while (true){
            String messaggio_ricevuto = "";
            try {
                messaggio_ricevuto = riceviMessaggio();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            String nome_thread = messaggio_ricevuto.split("°")[0];
            if(messaggio_ricevuto.split("°").length>1){
                String contenuto_messaggio = messaggio_ricevuto.split("°")[1];
                if(contenuto_messaggio.equals("connect")){
                    this.dict_port.put(nome_thread, this.porta_iniziale);
                }
                if(dict_port.size() == 2){
                    if(nome_thread.equals("LeapListener")){
                        try {
                            this.inviaMessaggio(messaggio_ricevuto, this.dict_port.get("PrincipaleProcessing"));
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }
                    }
                }
            }

        }
    }
}
