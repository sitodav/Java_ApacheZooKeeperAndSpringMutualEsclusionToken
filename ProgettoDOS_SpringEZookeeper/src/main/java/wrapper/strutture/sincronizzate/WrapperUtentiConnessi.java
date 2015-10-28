package wrapper.strutture.sincronizzate;

import java.util.HashMap;
import java.util.List;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import database_beans.Utente;


//questa classe rappresenta un contenitore di client connessi, con proprietà classiche di 
//insert e get 

@Component(value="WrapperUtentiConnessi")
@ComponentScan(basePackages="database_beans")
@Scope(value="singleton") //necessario poichè deve essere unica istanza condivisa
public class WrapperUtentiConnessi {
	
	//il nome utente è la chiave della mappa
	HashMap<String,Utente> utentiConnessi = new HashMap<String,Utente>(); 
	
	public synchronized void addUtente(Utente toAdd)
	{
		utentiConnessi.put( toAdd.getNome(), toAdd  ); 
	}
	
	public synchronized void removeUtente(String nomeToRemove)
	{
		utentiConnessi.remove(nomeToRemove);
	}
	
	public synchronized HashMap<String,Utente> getAllUtentiConnessi() 
	{
		return utentiConnessi; 
	}
	
	public synchronized boolean isUtenteConnesso(Utente toSearch)
	{
//		System.out.println("il controllo dice che "+utentiConnessi.containsKey(toSearch.getNome()));
		return utentiConnessi.containsKey(toSearch.getNome());
	}
}
