import java.io.*;
import java.net.*;
import java.util.Scanner;


public class ClientUDP {
    private Scanner inFromUser;

    private DatagramSocket clientSocket;

    private InetAddress IPAddress;
    private String nome_client;

    private byte[] sendData = new byte[4096];
    private byte[] receiveData = new byte[4096];

    public ClientUDP(String nome_client) throws Exception{
        this.nome_client = nome_client;
        this.inFromUser = new Scanner(System.in);
        this.clientSocket = new DatagramSocket();
        this.IPAddress = InetAddress.getLocalHost();

        this.inviaMessaggio("connect");

    }

    public void inviaMessaggio(String messaggio_da_inviare)throws Exception{
        if(messaggio_da_inviare == null){
            throw new Exception("La stringa non deve essere nulla");
        }

        ByteArrayOutputStream byteSend = new ByteArrayOutputStream();
        DataOutputStream send = new DataOutputStream(byteSend);

        send.writeUTF(this.nome_client+"°"+messaggio_da_inviare);

        sendData = byteSend.toByteArray();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9999);
        clientSocket.send(sendPacket);
    }

    public String riceviMessaggio() throws IOException {
        ByteArrayInputStream byteReceive;

        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        clientSocket.receive(receivePacket);
        byteReceive = new ByteArrayInputStream(receivePacket.getData());
        DataInputStream receive = new DataInputStream(byteReceive);
        //System.out.println("Ho ricevuto il messaggio: "+receive.readUTF());
        return receive.readUTF();
    }

    public void closeClientUDP(){
        this.clientSocket.close();
    }
}