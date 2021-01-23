package ServerFiles;

import UserFiles.Utilizador;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Server {
    private Map<String, String> usersInfo; /* Armazenamento dos usernames e passwords */
    private int[][] locationsMap; /* Mapa que indica que na localização (x,y) estão z utilizadores, no momento */
    private int N; /* Tamanho da matriz */
    private Map<String, Utilizador> users; /* Map que contém todos os utilizadores logados */
    private ReentrantReadWriteLock rwl;
    private Lock rl;
    private Lock wl;

    public Server(int N){
        rwl = new ReentrantReadWriteLock();
        rl = rwl.readLock();
        wl = rwl.writeLock();
        this.N = N;
        usersInfo = new HashMap<>();
        locationsMap = new int[N][N];
        users = new HashMap<>();
    }

    public boolean matches(String user, String password){
        rl.lock();
        try{
            return(password.equals(usersInfo.get(user)));
        } finally {
            rl.unlock();
        }
    }

    public boolean exists(String user){
        rl.lock();
        try {
            return usersInfo.containsKey(user);
        } finally {
            rl.unlock();
        }
    }

    public boolean registerUser(String username, String password){
        if(exists(username))
            return false;
        else{
            // (1) Guardar username e password, para já com localização não definida
            //@TODO definir se a localização é passada logo no registo, e decidir se usamos id como chave ou o username
            wl.lock();
            try{
                usersInfo.put(username, password);
            } finally {
                wl.unlock();
            }
            return true;
        }
    }

    public boolean isLoggedIn(String user){
        rl.lock();
        try{
            return users.containsKey(user);
        } finally {
            rl.unlock();
        }
    }

    public void addLogin(String user){
        /* Só neste método é que adicionamos ao Map dos utilizadores, para saber que já está autenticado */
        //@TODO definir outra vez como é iniciada a localização
        wl.lock();
        try{
            users.put(user, new Utilizador(user, -1, -1));
        } finally {
            wl.unlock();
        }
    }

    public static void main(String[] args){
        Server s = new Server(15); /* Mapa 15 por 15 (???) */
        try {
            ServerSocket ss = new ServerSocket(12345);
            while(true){
                Socket sck = ss.accept();
                Thread t = new Thread(new ServerWorker(sck,s));
                t.start();
            }
        } catch(IOException e){
            e.printStackTrace();
        }
    }
}
