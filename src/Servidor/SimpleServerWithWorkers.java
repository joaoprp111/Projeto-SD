package src.Servidor;

import src.AlarmeCovidLN.AlarmeCovidLN;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.*;
import java.io.*;

public class SimpleServerWithWorkers {

    public static void main(String[] args) throws Exception {
        AlarmeCovidLN ac = new AlarmeCovidLN(10); /* Damos como argumento o tamanho do mapa (NxN) */
        ServerSocket ss = new ServerSocket(12345);


        /* Administrador da aplicação, que possui a autorização especial para descarregar o mapa */
        System.out.println("Admin criado: " + ac.registar("1", "1",true));

        while(true) {
            Socket s = ss.accept();
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
            DataInputStream in = new DataInputStream(new BufferedInputStream(s.getInputStream()));
            Lock writer = new ReentrantLock();

            Runnable worker = () -> {
                String uniqueUser = null; /* Esta string contém o username do utilizador que 'invocou' este worker */
                
                try{
                    for (;;) {
                        int tag = in.readInt();
                        String user, pass;
                        int x,y;
                        switch(tag){
                            case 1:
                                boolean[] bs;
                                //System.out.println("Login");
                                user = in.readUTF();
                                pass = in.readUTF();
                                bs = ac.login(user, pass);

                                writer.lock();
                                out.writeBoolean(bs[0]);
                                if(bs[0]){
                                    uniqueUser = user;
                                    out.writeBoolean(bs[1]);
                                }

                                out.flush();

                                writer.unlock();
                                
                                break;
                            case 2:
                                //System.out.println("Registar");
                                user = in.readUTF();
                                pass = in.readUTF();
                                boolean a=ac.registar(user, pass, false);

                                writer.lock();
                                out.writeBoolean(a);
                                out.flush();

                                writer.unlock();
                                break;
                            case 3:
                                //System.out.println("Comunicar localização");
                                //user = in.readUTF();
                                x = in.readInt();
                                y = in.readInt();

                                writer.lock();
                                out.writeInt(tag);
                                if(x < 0 || y < 0 || x >= ac.getN() || y >= ac.getN() || ac.getInfetados().contains(uniqueUser))
                                    out.writeBoolean(false);
                                else {
                                    boolean b = ac.comunicarLocalizacao(uniqueUser, x, y);
                                    out.writeBoolean(b);
                                }
                                out.flush();

                                writer.unlock();
                                break;
                            case 4:
                                //System.out.println("Quantidade de pessoas numa localização");
                                x = in.readInt();
                                y = in.readInt();

                                writer.lock();
                                out.writeInt(tag);
                                if(x < 0 || y < 0 || x >= ac.getN() || y >= ac.getN() || ac.getInfetados().contains(uniqueUser))
                                    out.writeBoolean(false);
                                else {
                                    out.writeBoolean(true);
                                    out.writeInt(ac.getOcupacao(x, y));
                                }
                                out.flush();
                                writer.unlock();
                                break;
                            case 5:
                                //System.out.println("Mapa com o nº de pessoas em cada localização");
                                int[][][] res = ac.getOcupacoes();
                                int l = res.length;

                                writer.lock();
                                out.writeInt(tag);
                                out.writeBoolean(true);
                                out.writeInt(l);
                                for(int i = 0; i < l; i++)
                                    for(int j = 0; j < l; j++){
                                        out.writeInt(res[i][j][0]);
                                        out.writeInt(res[i][j][1]);
                                    }
                                    
                                out.flush();
                                writer.unlock();
                                break;
                            case 6:
                                //System.out.println("Comunicar que está infetado");
                                
                                writer.lock();
                                out.writeInt(tag);
                                if(uniqueUser!= null) {
                                    out.writeBoolean(ac.estaInfetado(uniqueUser));
                                    //out.writeUTF(uniqueUser);

                                    //Close socket?
                                
                                }
                                else out.writeBoolean(false);

                                out.flush();
                                writer.unlock();
                                break;
                            case 7:
                                //System.out.println("Verificar se está em risco de contaminação");
                                final String uniqueUser1 = uniqueUser;
                                Runnable infetado = () -> {
                                    try{
                                        boolean r = ac.alertarRisco(uniqueUser1);
                                        writer.lock();
                                        
                                        out.writeInt(tag);
                                        out.writeBoolean(r);
                                        if(r)out.writeBoolean(r);
                                        out.flush();

                                        writer.unlock();
                                    }catch(Exception e){
                                        System.out.println(e);
                                    }
                                };
                                new Thread(infetado).start();


                                //funcao risco faz wait na variavel de condicao
                                break;
                            case 8:
                                //System.out.println("Verificar se uma localização está vazia");
                                int cx = in.readInt();
                                int cy = in.readInt();
                                Runnable vazia = () -> {
                                    try{
                                        boolean r = ac.celulaVazia(cx,cy);
                                        writer.lock();

                                        out.writeInt(tag);
                                        out.writeBoolean(r);
                                        if(r) {
                                            out.writeInt(cx);
                                            out.writeInt(cy);
                                        }
                                        out.flush();

                                        writer.unlock();
                                    }catch(Exception e){
                                        System.out.println(e);
                                    }
                                };
                                new Thread(vazia).start();
                                break;
                        }

                        //System.out.println("___________");
                    }

                }catch(EOFException e){
                    // Try Log off unique user!!
                    ac.logoff(uniqueUser);
                    //if(uniqueUser!=null)System.out.println(uniqueUser + " is now offline");
                }catch(SocketException e){
                    // Try Log off unique user!!
                    ac.logoff(uniqueUser);
                    //if(uniqueUser!=null)System.out.println(uniqueUser + " is now offline (closed socket)");
                }catch(Exception e){
                    // Try Log off unique user!!
                    ac.logoff(uniqueUser);
                    //if(uniqueUser!=null)System.out.println(uniqueUser + " is now offline");
                    e.printStackTrace();
                }
                
            };
            new Thread(worker).start();
        }

    }
}

