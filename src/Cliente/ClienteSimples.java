package src.Cliente;

import java.net.Socket;
import java.util.Random;
import java.io.*;

import java.util.Scanner;

public class ClienteSimples {
	
	private static Socket s;
    private static Stub stub;
    private static boolean permissao;

	private static void apresentarMenuRL(){
		System.out.println("--------Menu RL--------");
		System.out.println("0. Sair");
		System.out.println("1. Login");
		System.out.println("2. Register");
	}
    private static void apresentarMenuLog(){
        System.out.println("--------Menu Login--------");
        System.out.println("0. Sair");
        System.out.println("1. Comunicar Localização Atual");
        System.out.println("2. Verificar ocupação de uma Localizacao");
        if(permissao){
        	System.out.println("3. Imprimir Mapa de ocupaçoes e doentes");
        }

        System.out.println("4. Comunicar que está infetado");
        System.out.println("5. Notificar se uma localização está vazia");
    }

	private static int readOption() {
        int op;
        Scanner is = new Scanner(System.in);
        System.out.print("Opção: ");
        try {
            String line = is.nextLine();
            op = Integer.parseInt(line);
        }
        catch (NumberFormatException e) { // Não foi inscrito um int
            op = -1;
        }
        return op;
    }
    
    private static String lerString(String texto) {
        Scanner is = new Scanner(System.in);
        System.out.print(texto);
        return is.nextLine();
    }

    private static int lerInt(String text) {
        int op;
        Scanner is = new Scanner(System.in);
        System.out.print(text);
        try {
            String line = is.nextLine();
            op = Integer.parseInt(line);
        }
        catch (NumberFormatException e) { // Não foi inscrito um int
            op = -1;
        }
        return op;
    }

    private static boolean login(){
    	//System.out.println("Nao implementado");
        String user=lerString("Insira o username: ");
        String pass=lerString("Insira a password: ");
        boolean b;

        try{
		    // send request
		    b = stub.login(user,pass);

            System.out.println();
            System.out.println("Login bem sucedido");
            permissao=b;
            
	       return true;
	    }catch(Exception e){
        	System.out.println(e);
        	return false;
        }

    }

    private static void register(){
    	//System.out.println("Nao implementado");
    	String user=lerString("Escolha o username: ");
        String pass=lerString("Escolha a password: ");
        boolean b;

        try{
		    // send request
		    stub.register(user,pass);
		    
		    System.out.println("Registo bem sucedido");

        }catch(Exception e){
         	System.out.println("Erro: "+e);
        }
    }

    private static void comunicarLocalizacao(){
    	//System.out.println("Nao implementado");
    	int x = lerInt("Insira a coordenada x: ");
    	int y = lerInt("Insira a coordenada y: ");

        boolean b;

        try{
		    // send request
		   	stub.comunicarLocalizacao(x,y);
 
            //System.out.println("Comunicacao bem sucedida");
             

        }catch(Exception e){
         	System.out.println("Erro: "+e);
        }
    }

    private static void verificarOcupacao(){
    	//System.out.println("Nao implementado");
    	int x = lerInt("Insira a coordenada x: ");
    	int y = lerInt("Insira a coordenada y: ");

        try{
		    // send request
		    stub.verificarOcupacao(x,y);

        }catch(Exception e){
         	System.out.println("Erro: "+e);
        }

    }
    public static void imprimirMapa(){
        try{
            stub.imprimirMapa();            

        } catch(Exception e){
            System.out.println("Erro " +e);
        }
    }

    public static void imprimirMapa(DataInputStream in){
	    try{
	    	StringBuilder sb = new StringBuilder();
	        System.out.println("Mapa de ocupacoes/infecoes");

            int l=in.readInt();
	       	
            sb.append("  ");
	        for(int i = 0; i < l; i++){
	        	sb.append(i).append("     ");
	        }
            
            sb.append("\n");
            
            for(int i = 0; i < l; i++){
	        	sb.append(i).append(" ");
                for(int j = 0; j < l; j++){
                	sb.append("(").append(in.readInt());
                	sb.append("/").append(in.readInt());
                	sb.append(") ");
                }
            	sb.append("\n");
            }
      
	        System.out.println(sb.toString());
        } catch(Exception e){
	        System.out.println("Erro " +e);
        }
    }

    public static void comunicarInfecao(){
	    try{
	        stub.comunicarInfecao();
        } catch(Exception e){
	        System.out.println("Erro " +e);
        }
    }


    public static void verificarRiscoInfecao(){
	    try{
	       stub.verificarRiscoInfecao();
        } catch(Exception e){
	        System.out.println("Erro " +e);
        }
    }

    public static void notificarLocalVazio(){
    	int x = lerInt("Insira a coordenada x: ");
    	int y = lerInt("Insira a coordenada y: ");

        try{
		    // send request
		   	stub.notificarLocalVazio(x,y);
 
        }catch(Exception e){
         	System.out.println("Erro: "+e);
        }
    }
    

    public static void main(String[] args) throws Exception {
        try{
        	s = new Socket("localhost", 12345);
        }catch(Exception e){
        	System.out.println("Não foi possivel connectar-se ao server");
        	return;
        }

        try {
        	stub = new Stub(s);
        }catch(Exception e){
        	System.out.println("Erro a criar Stub");
        }


    	Scanner scin = new Scanner(System.in);

        // menu login ou register
        int op=-1;
        boolean loggedin=false;

        while(op!=0 && !loggedin){
        	apresentarMenuRL();
        	op=readOption();
            System.out.println();

        	switch(op){
        		case 0:
        			System.out.println("A sair...");
        			break;
        		case 1:
        			System.out.println("Login:");
        			loggedin=login();
        			break;
        		case 2:
                    System.out.println("Register:");
        			register();
        			break;
        		default:
        			System.out.println("Erro na escolha");
        			break;
        	}

        	System.out.println();
        }

        // Até aqui ser sequencial! 

        if(loggedin){
        	//fazer cenas
            
            //Uma thread para cada pedido? -> erro de sincronizacao de mais que um a ler
            Barreira bar = new Barreira();

            Runnable escrita = () -> {
                try{
                    DataInputStream in = new DataInputStream(new BufferedInputStream(s.getInputStream()));
                    int tag;
                    while(true){
                        tag = in.readInt();
                        boolean b = in.readBoolean();
                    	System.out.println();
                        if(b){
                            switch(tag){
                                case 3:
                                    System.out.println("Localizacao foi alterada com sucesso");
                                    break;
                                case 4:
                                    System.out.println("Estao " + in.readInt() + " pessoas nesse local");
                                    break;
                                case 5:
                                    imprimirMapa(in);
                                    break;
                                case 6:
                                    System.out.println("Utilizador foi registado como infetado");
                                    break;
                                case 7:
                                    StringBuilder sb = new StringBuilder();
                                    sb.append("Você ");
                                    if(!(in.readBoolean())) sb.append("não ");
                                    sb.append("está em risco de estar infetado(a)");

                                    System.out.println(sb.toString());

                                    break;
                                case 8:
                                	System.out.println("Local (" +in.readInt()+ "," +in.readInt()+ ") está livre");
                                	break;
                            }
                        }else{
                            System.out.println("Erro na opcao");
                        }
                        
                    	System.out.println();

                        if(tag!=7 && tag!=8){
                            bar.await(1);
                        }

                    }
                }catch(IOException e){
                    System.out.println("Socket closed");
                    try{
                    	bar.await(1);
                    }catch(Exception ex){
                    	System.out.println("erro" + ex);
                	}
                }catch(Exception e){
                    System.out.println("erro" + e);
                }

            };
            new Thread(escrita).start();

            //pedido de estar infetado (wait no sv se é 0 e esta login)
            verificarRiscoInfecao();

            while(op!=0){
                try{
                    apresentarMenuLog();
                    op=readOption();
                    System.out.println();

                    switch(op){
                        case 0:
                            System.out.println("A sair...");
                            s.close();
                            break;
                        case 1:
                            System.out.println("Comunicar Localização Atual:");
                            comunicarLocalizacao();
                            bar.await(0);
                            break;
                        case 2:
                            System.out.println("Verificar ocupação de uma Localizacao");
                            verificarOcupacao();
                            bar.await(0);
                            break;
                        case 3:
                            if(permissao){
                            	System.out.println("Imprimir Mapa de ocupaçoes e doentes");
                            	imprimirMapa();
                            	bar.await(0);
                            }
                            else{
                               System.out.println("Não tem permissao");
                            }
                            break;
                        case 4:
                            System.out.println("Comunicar infeção");
                            comunicarInfecao();
                            bar.await(0);
                            break;
                        case 5:
                            System.out.println("Notificar quando local está vazio");
                            notificarLocalVazio();
                            System.out.println();
                            //bar.await(0);
                            break;
                        default:
                            System.out.println("Erro na escolha");
                            break;
                    }
                }catch(Exception e){
                    s.close();

                }
            }
        }
    }
}
