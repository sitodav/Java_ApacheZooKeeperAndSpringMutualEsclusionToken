package database_beans;
import java.util.List;

import beans.*;

//DAO che rappresenta l'astrazione dell'accesso al db per eseguire operazioni CRUD sulla tabella associata
//ai bean utenti
public interface UtenteDao {

	public abstract List<Utente> getAllUtenti();
	public abstract Utente getUtente(String nome);
	public abstract void inserisciUtente(String nome,String password);
	public abstract void rimuoviUtente(String nome);
	public abstract void aggiornaUtente(String nome, String nuovoNome, String nuovaPassword);
	
	
}
