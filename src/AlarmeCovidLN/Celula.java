package src.AlarmeCovidLN;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Celula {
    private Collection<String> users; /* Coleção com toda a gente que passou nesta célula (localização) */
    private int nPessoasAtual;
    private Lock l;
    public Condition c;

    public Celula(){
        this.users = new TreeSet<>();
        this.l = new ReentrantLock();
        this.c = l.newCondition();
        this.nPessoasAtual = 0;
    }

    public void addUser(String user){
        users.add(user);
        nPessoasAtual++;
    }

    public void reduzirN(){
        nPessoasAtual--;

        if(nPessoasAtual == 0)
            c.signalAll();
    }

    public void addAllUsers(Collection<String> users){
        this.users.addAll(users);
    }

    public Collection<String> getUsers(){
        return new ArrayList<>(users);
    }

    public void lock(){
        l.lock();
    }

    public void unlock(){
        l.unlock();
    }

    public int getnPessoasAtual(){
        return this.nPessoasAtual;
    }
}
