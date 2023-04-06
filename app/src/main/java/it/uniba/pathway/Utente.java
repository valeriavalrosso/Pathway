package it.uniba.pathway;

public class Utente {
    String id;
    String nome;


    public Utente(String id, String nome)
    {
        this.id=id;
        this.nome=nome;

    }

    public String getId()
    {
        return this.id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getNome()
    {
        return this.nome;
    }

    public void setNome(String nome)
    {
        this.nome = nome;
    }
}
