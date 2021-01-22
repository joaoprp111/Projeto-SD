import java.io.*;
import java.net.Socket;

public class WriteWorker implements Runnable {
    private Socket s;

    public WriteWorker(Socket s){
        this.s = s;
    }

    public void run(){
        try {
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
            BufferedReader systemin = new BufferedReader(new InputStreamReader(System.in));

            String userInput;

            while((userInput =  systemin.readLine()) != null){
                out.writeUTF(userInput); //Username
                out.writeUTF(userInput); //Password
                out.flush();
            }
            s.shutdownOutput();
            s.shutdownInput();
            s.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }
}
