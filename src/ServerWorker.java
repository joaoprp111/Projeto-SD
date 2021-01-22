import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ServerWorker implements Runnable{
    private Socket socket;
    private Server s;

    public ServerWorker(Socket socket, Server s) {
        this.socket = socket;
        this.s = s;
    }

    public void run(){
        try {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            int tag = -1;
            boolean isOpen = true;
            while(isOpen){
                tag = in.readInt();
                switch(tag){
                    case 0:
                        String user = in.readUTF();
                        if(s.registerUser(user, in.readUTF()))
                            System.out.println("Utilizador " +user+ " registado!");
                        else
                            System.out.println("Já existe!");
                        break;
                    case -1:
                        System.out.println("Registos efetuados com sucesso!");
                        isOpen = false;
                        break;
                    default:
                        System.out.println("Tag errada!");
                        break;
                }
            }

            socket.shutdownInput();
            socket.shutdownOutput();
            socket.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }
}


