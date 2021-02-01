package src.Cliente;


import src.Exceptions.FromServerException;

import java.io.*;
import java.net.Socket;


public class Stub{
    private Socket s;
    private DataOutputStream out;
    private DataInputStream in;

    public Stub(Socket s) throws Exception{
        this.s = s;
        this.out = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
        this.in = new DataInputStream(new BufferedInputStream(s.getInputStream()));
           
    }


    public boolean login(String user, String pass) throws Exception{
    	try{
	    	out.writeInt(1);

	        out.writeUTF(user); /* Username */
	        out.writeUTF(pass); /* Password */
	   
	        out.flush();

            boolean b;
	        b = in.readBoolean();

            if(b){
                b = in.readBoolean();
            }else{
                throw new FromServerException("Stub error - Login inválido!");
            } 

	       	return b;
    	}catch(Exception e){
    		throw e;
    	}
    }

    public void register(String user, String pass) throws Exception{
    	try{
	    	out.writeInt(2);

	        out.writeUTF(user); 
	        out.writeUTF(pass); 
	   
	        out.flush();

	        if(!in.readBoolean()) throw new FromServerException("Stub error - Registo inválido (já existe?)");

    	}catch(Exception e){
    		throw e;
    	}
    }

    public void comunicarLocalizacao(int x, int y) throws Exception{
        try{
            out.writeInt(3);

            out.writeInt(x); 
            out.writeInt(y); 
       
            out.flush();

        }catch(Exception e){
            throw e;
        }
    }

    public void verificarOcupacao(int x, int y) throws Exception{
        try{
            out.writeInt(4);

            out.writeInt(x); 
            out.writeInt(y); 
       
            out.flush();

        }catch(Exception e){
            throw e;
        }
    }
    
    public void imprimirMapa() throws Exception{
    	try{
            out.writeInt(5);
            out.flush();
            
        } catch(Exception e){
            throw e;
        }
    }

    public void verificarRiscoInfecao() throws Exception{
        try{
            out.writeInt(7);
            out.flush();

        } catch(Exception e){
            throw e;
        }
    }

    public void comunicarInfecao() throws Exception{
        try{
            out.writeInt(6);
            out.flush();

        } catch(Exception e){
            throw e;
        }
    }


    public void notificarLocalVazio(int x, int y) throws Exception{
        try{
            out.writeInt(8);

            out.writeInt(x); 
            out.writeInt(y); 
       
            out.flush();

        }catch(Exception e){
            throw e;
        }
    }

}