package ServerFiles;

import java.util.Collection;
import java.util.HashSet;

public class Utilizador {
    private String username;
    private Localizacao l;
    private Collection<Integer> idsContacts;

    public Utilizador(String username, int x, int y){
        this.username = username;
        this.l = new Localizacao(x, y);
        this.idsContacts = new HashSet<>();
    }
}
