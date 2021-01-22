package Client;

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

            int tag;
            String input = null;

            while((input = systemin.readLine()) != null){
                tag =  Integer.parseInt(input);
                /* Primeiro inteiro é a etiqueta da operação */
                out.writeInt(tag);
                switch(tag){
                    case 0: /* 0 -> registar utilizador */
                    case 1: /* 1 -> autenticar */
                        out.writeUTF(systemin.readLine()); /* Username */
                        out.writeUTF(systemin.readLine()); /* Password */
                        break;
                }
                out.flush();
            }

            out.writeInt(-1); /* Não vai escrever mais nada */
            out.flush();

            s.shutdownOutput();
            s.shutdownInput();
            s.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }
}
