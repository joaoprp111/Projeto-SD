package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Server {
    private Map<String, String> usersInfo;
    private ReentrantReadWriteLock rwl;
    private Lock rl;
    private Lock wl;

    public Server(){
        rwl = new ReentrantReadWriteLock();
        rl = rwl.readLock();
        wl = rwl.writeLock();
        usersInfo = new HashMap<>();
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
            wl.lock();
            try{
                usersInfo.put(username, password);
            } finally {
                wl.unlock();
            }
            return true;
        }
    }

    public static void main(String[] args){
        Server s = new Server();
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
