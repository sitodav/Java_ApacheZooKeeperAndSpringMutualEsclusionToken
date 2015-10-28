package wrapper.strutture.sincronizzate;

import java.util.HashMap;
import java.util.concurrent.ScheduledExecutorService;

import org.json.simple.JSONObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import zookeeper.utils.ZkConnectorPerClient;
import database_beans.Utente;


@Component("WrapperStrutturePerUtentiConnessi")
 
@Scope(value="singleton") //necessario poichè deve essere unica istanza condivisa
public class WrapperStruttureAggiuntivePerUtentiConnessi {

	private HashMap<String,Boolean> stati = new HashMap<String, Boolean>();
	private HashMap<String, ScheduledExecutorService> checkers = new HashMap<String, ScheduledExecutorService>();
	private HashMap<String, ZkConnectorPerClient> connettoriClient = new HashMap<String, ZkConnectorPerClient>();
	private HashMap<String, JSONObject> infoClientSide = new HashMap<String, JSONObject>();
	
	public synchronized void addStatoUtente(Utente utente, boolean stato)
	{
		stati.put(utente.getNome(), stato);
	}

	public synchronized void addStatoUtente (String nome, Boolean stato)
	{
		Utente u = new Utente();
		u.setNome(nome);
		addStatoUtente(u,stato);
	}
	
	public synchronized Boolean getStatoUtente(Utente utente)
	{
		return stati.get(utente.getNome());
	}
	
	public synchronized Boolean getStatoUtente(String nome)
	{
		Utente u = new Utente();
		u.setNome(nome);
		return getStatoUtente(u);
	}
	
	public synchronized Boolean removeStato(Utente utente)
	{
		return stati.remove(utente.getNome());
	}
	
	public synchronized Boolean removeStato(String nome)
	{
		Utente u = new Utente();
		u.setNome(nome);
		return removeStato(u);
	}
	
	public synchronized void addChecker(Utente utente, ScheduledExecutorService checker)
	{
		checkers.put(utente.getNome(),checker);
	}
	
	public synchronized ScheduledExecutorService getChecker(Utente utente)
	{
		return checkers.get(utente.getNome());
	}
	
	public synchronized ScheduledExecutorService removeChecker(Utente utente)
	{
		return checkers.remove(utente.getNome());
	}

	public synchronized ScheduledExecutorService removeChecker(String nome)
	{
		Utente u = new Utente();
		u.setNome(nome);
		return removeChecker(u);
	}
	
	public synchronized ZkConnectorPerClient getConnectorUtente(Utente utente)
	{
		return connettoriClient.get(utente.getNome());
	}
	
	public synchronized void addConnectorUtente(Utente utente, ZkConnectorPerClient connector)
	{
		connettoriClient.put(utente.getNome(), connector);
	}
	
	public synchronized ZkConnectorPerClient removeConnectorUtente(Utente utente)
	{
		return connettoriClient.remove(utente.getNome());
	}
	
	public synchronized JSONObject getInfoClientSide(Utente utente)
	{
		return infoClientSide.get(utente.getNome());
	}
	public synchronized void addInfoClientSide(Utente utente, JSONObject infos)
	{
		infoClientSide.put(utente.getNome(), infos);
	}
	public synchronized JSONObject removeInfoClientSide(Utente utente)
	{
		return infoClientSide.remove(utente.getNome());
	}
	
}
