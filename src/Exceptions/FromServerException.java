package src.Exceptions;

/**
 * Exceção utilizada quando as operações do servidor não concluem como deveriam concluir num fluxo normal
 */
public class FromServerException extends Exception{
    public FromServerException(){
        super();
    }

    public FromServerException(String s){
        super(s);
    }
}
