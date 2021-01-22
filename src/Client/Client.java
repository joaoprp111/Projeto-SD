import java.io.IOException;
import java.net.Socket;

public class Client {

    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 12345);

            Thread t1 = new Thread(new WriteWorker(socket));
            //Thread t2 = new Thread(new ReadWorker(socket)); <-- Ainda nao implementado

            t1.start();
            //t2.start();

        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
