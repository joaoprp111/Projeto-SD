package src.AlarmeCovidLN;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AlarmeCovidLN {
    private Map<String, Utilizador> users;
    private final int N;
    private final Celula[][] mapa; /* Mapa que guarda quem esteve ou está em cada localização */
    private Lock l;

    public AlarmeCovidLN(int N){
        this.N = N;
        this.users = new HashMap<>();
        this.mapa = new Celula[N][N];
        this.l = new ReentrantLock();
    }

    public boolean celulaVazia(int x, int y){
        l.lock();
            Celula c = mapa[x][y];
            if(c == null)
                mapa[x][y] = new Celula();
            mapa[x][y].lock();
            l.unlock();
        try {
            try {
                while (mapa[x][y].getnPessoasAtual() != 0)
                    mapa[x][y].c.await();
                return true;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
        } finally {
            mapa[x][y].unlock();
        }
    }

    public boolean alertarRisco(String user){
        l.lock();
        Utilizador u = users.get(user);
        u.lock();
        l.unlock();
        try {
            try {
                while (!u.isRisco())
                    u.c.await();
                return true;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
        } finally {
            u.unlock();
        }
    }

    public int getN(){
        return this.N;
    }

    /**
     * Devolve o número de pessoas a ocupar uma localização no momento atual
     * @param x coordenada x
     * @param y coordenada y
     * @return inteiro
     */
    public int getOcupacao(int x, int y){
        l.lock();
        int res = 0;
        Localizacao loc = new Localizacao(x,y);
        
        Collection<Utilizador> us = users.values();
        for(Utilizador u: us)
            u.lock();

        l.unlock();

        for(Utilizador u: us) {
            if (u.getlAtual().equals(loc) && u.getLogged()) {
                res++;
            }
            u.unlock();
        }
        return res;
    }

    public int numElemsIguais(Collection<String> a, Collection<String> b){
        int res = 0;
        for(String s1: a)
            for(String s2: b)
                if(s1.equals(s2))
                    res++;
        return res;
    }

    public Collection<String> getInfetados(){
        Collection<String> res = new TreeSet<>();
        //Não é preciso dar lock porque a função é usada dentro de um bloco que tem lock
        for(Utilizador u: users.values())
            if(u.isInfetado())
                res.add(u.getUsername());
        return res;
    }


    /**
     * Devolve o mapa com as ocupações
     * @return matriz de inteiros (cada localização tem uma quantidade de pessoas associada)
     */
    public int[][][] getOcupacoes(){
        int[][][] res = new int[N][N][2];
        int x,y;

		for(x = 0; x <N; x++)
            for(y = 0; y < N; y++) {
	            res[x][y][0] = 0;
	            res[x][y][1] = 0;
        	}

        l.lock();

            ArrayList<Celula> celulas = new ArrayList<Celula>();
            ArrayList<Integer> xs = new ArrayList<Integer>();
            ArrayList<Integer> ys = new ArrayList<Integer>();
            
            //Dar lock
            for(x = 0; x <N; x++)
                for(y = 0; y < N; y++){
                	if(mapa[x][y] != null){
                		mapa[x][y].lock();
                		celulas.add(mapa[x][y]);
                		xs.add(x);
                		ys.add(y);
                	} 
                }

            Collection<String> infetados = getInfetados();

            l.unlock();

            //Obter ocupacao
            int i;
            for(i=0;i<celulas.size();i++){
            	res[xs.get(i)][ys.get(i)][0] = celulas.get(i).getUsers().size();
	            res[xs.get(i)][ys.get(i)][1] = numElemsIguais(celulas.get(i).getUsers(),infetados);
            }

            
            //Dar unlock
			for(Celula c:celulas){
            	c.unlock();
            }
            return res;
        
    }

    /**
     * Verifica se um utilizador está registado
     * @param username
     * @return true se sim
     */
    public boolean estaRegistado(String username){
        l.lock();
        try {
            return this.users.containsKey(username);
        } finally {
            l.unlock();
        }
    }

    /**
     * Regista um utilizador
     * @param username
     * @param password
     * @return true se for registado com sucesso
     */
    public boolean registar(String username, String password, boolean permissao){
        l.lock();
        try{
        if(estaRegistado(username))
            return false;
        else{
                this.users.put(username, new Utilizador(username, password,
                        permissao,-1,-1));
                return true;
            }
        } finally {
            l.unlock();
        }
    }

    /**
     * Fazer login
     * @param username
     * @param password
     * @return true se for feito com sucesso, false caso contrário
     */
    public boolean[] login(String username, String password) {
        boolean[] res = new boolean[2];
        l.lock();
        
            Utilizador u = users.get(username);
            if(u == null) {
                l.unlock();
                return res;
            }
            u.lock();
            l.unlock();
            try {
            	if(!u.isInfetado()){
	                if (password.equals(u.getPassword()) && !u.isLogged()) {
	                    u.setLogged(true);
	                    res[0] = true;
	                    res[1] = u.isAutorizado();
	                }
            	}
                return res;
            } finally {
                u.unlock();
            }
        
    }

     public void logoff(String username) {
        l.lock();
        
        Utilizador u = users.get(username);
        if(u == null) {
            l.unlock();
        }else{
	        u.lock();
	        l.unlock();
	        try {
	            u.setLogged(false);
	        } finally {
	            u.unlock();
	        }
        }
        
    }

    /**
     * Atualiza a localização de um dado utilizador
     * @param user Utilizador
     * @param x coordenada x
     * @param y coordenada y
     */
    public boolean comunicarLocalizacao(String user, int x, int y){
        Localizacao loc = new Localizacao(x,y);
        
        l.lock();
            Utilizador u = users.get(user);
            if(u != null){
                    // 1.Adicionar pessoas desta localizacao nos contactos do u
                    // 2.Adicionar o u como contacto das restantes pessoas
                    Collection<Utilizador> us = users.values();
                   
                    for(Utilizador ut: us) {
                        ut.lock();
                    }


                    Celula cel = mapa[x][y];
                    if(cel == null)
                        mapa[x][y] = new Celula();
                    mapa[x][y].lock();

                    int coordxAtual = u.getlAtual().getX();
                    int coordyAtual = u.getlAtual().getY();

                    if(coordxAtual >= 0 && coordyAtual >= 0)
                        if(mapa[coordxAtual][coordyAtual]!=null)
                            mapa[coordxAtual][coordyAtual].lock();

                    l.unlock();

                    try{
                    	if(u.isInfetado() || u.getlAtual().equals(loc))
                    	    return false;

                    	u.setlAtual(loc);


	                    for(Utilizador ut: us)
	                        if(ut.getlAtual().equals(loc) && !ut.equals(u) && ut.isLogged()) {
	                            u.addContacto(ut.getUsername());
	                            ut.addContacto(user);
	                        }



	                    // 3.Adicionar este utilizador à nova localização,
                        // e decrementar o nº de pessoas da localização antiga
	                    mapa[x][y].addUser(user);
                        if(coordxAtual >= 0 && coordyAtual >= 0)
                            if(mapa[coordxAtual][coordyAtual]!=null)
                                mapa[coordxAtual][coordyAtual].reduzirN();

                        

                        return true;

                    }finally{
                    	for(Utilizador ut: us)
                        	ut.unlock();	

                    	mapa[x][y].unlock();
                        if(coordxAtual >= 0 && coordyAtual >= 0)
                            if(mapa[coordxAtual][coordyAtual]!=null)
                                mapa[coordxAtual][coordyAtual].unlock();
                    }
            }
            else
                return false;
    }

    /**
     * Marca um utilizador como infetado, e adiciona os seus contactos ao grupo de risco
     * @param username username do infetado
     */
    public boolean estaInfetado (String username) {
        l.lock();
            Collection<Utilizador> us = users.values();
            Utilizador u = users.get(username);
            for(Utilizador ut : us)
                ut.lock();

            l.unlock();

            u.setInfetado(true);
            Collection<String> cs = u.getContactos();
            for(String c: cs) {
                users.get(c).setRisco();
                users.get(c).c.signalAll();
                //System.out.println("Possivel: " + c);
            }

            for(Utilizador ut: us)
                ut.unlock();

            return true;
    }

    /**
     * Verifica se um utilizador esteve em contacto com um infetado
     * @param username
     * @return array de 2 booleanos, o primeiro indica se o método foi executado com sucesso, o segundo indica se o utilizador está em risco ou não
     */
    public boolean[] risco(String username){
        boolean[] res = new boolean[2];
        l.lock();
        Utilizador u = users.get(username);
        if(u == null)
            return res;

        u.lock();
        l.unlock();

        if(u.isInfetado())
            return res;

        res[0] = true;
        res[1] = u.isRisco();
        u.unlock();
        return res;
    }
}
