package zookeeper.utils;

import java.io.IOException;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


//siccome questo bean è singleton, è istanziato quando parte il contesto della web application, e ne viene
//fatto dependency injection, cosa che per il setter del serverPath fa anche partire la connessione al server zk
@Component("zkConnectorPerMaster")
@Scope("singleton")
public class ZkConnectorPerMaster extends ZkConnectorAbstract{


	@Value("${zookeeper.hostPathPerClients}") //il server zookeeper ovviamente è lo stesso usato per i client 
	@Override
	public void setServerPath(String serverPath) throws IOException {
		super.setServerPath(serverPath); //questo fa anche connettere zookeeper
	}
	
	public void creaNodoMaster() throws KeeperException, InterruptedException, IOException
	{
		
		//quindi registriamo nella struttura di zk nuovo client
		Thread.sleep(5000); //necessario
		System.out.println(zk);
		zk.create("/master", "stringadummy".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
	}
	
	public void registraWatcherMaster( String target) throws KeeperException, InterruptedException
	{
		//registro quindi il watcher del master thread
		//prende in input target, poichè questo watcher può essere registrato o sul valore del nodo master in zk,
		//che se cambia vuol dire che il master ha ottenuto token, o sulla creazioen di figli per il nodo
		// /token_requests/ ad indicare che è arrivata una nuova richiesta di token da un client
		
		if(target.equals("/master"))
		{
			zk.getData(target, new WatcherPerOttenimentoTokenPerMaster(), new Stat());
		}
		else if(target.equals("/token_requests"))
		{
			zk.getChildren("/token_requests", new WatcherPerOttenimentoTokenPerMaster());
		}
		
	}
	
	private class WatcherPerOttenimentoTokenPerMaster implements Watcher
	{
		//per prima cosa il master registra il watcher sul valore del suo nodo
		//quando parte il watcher vuol dire che è cambiato tale valore (cioè quindi per noi il token è stato mandato al master)
		//se il master riceve il token, cerca in token request e se ci sono richieste, da token al primo client nelle richieste
		//e riregistra stesso watcher su suo ottenimento token
		//altrimenti se non ci sono richieste in /token_requests su zk, registra di nuovo stesso watcher che aveva registrato su
		//ricevimento del suo token, ma questa volta sulla creazione di nodi in /token_requests
		//Tutta questa logica è implementata in WatcherPerOttenimentoTokenPerMaster
		
		@Override
		public void process(WatchedEvent event) { 
			
			//quindi per prima cosa controlliamo se esistono figli per il nodo /token_requests, se si
			//mandiamo token al primo client delle richieste, e riregistriamo questo stesso watcher
			//se non ci sono figli, registriamo questo stesso watcher su /token_requests direttamente
			try 
			{
				List<String> richiesteToken = zk.getChildren("/token_requests", false);
				if(!richiesteToken.isEmpty())
				{
					
					System.out.println("MASTER THREAD WATCHER : ESISTE ALMENO UNA RICHIESTA DI TOKEN DA CLIENT, GLI MANDO TOKEN");					
					System.out.println("RICHIESTA: "+richiesteToken.get(0));
					//estraggo nome client
					String t = richiesteToken.get(0);
					String pathNodoClient =t.substring(t.indexOf("_"));
					//do quindi il token al client, cambiandogli il valore del suo data (questo farà partire il suo watcher)
					zk.setData("/clients/client"+pathNodoClient, "hasToken".getBytes(), -1);
					
					
					//e riregistro il watcher
					registraWatcherMaster("/master");
					
				}
				else
				{
					System.out.println("MASTER THREAD WATCHER : MASTER HA RICEVUTO TOKEN MA NON ESISTONO RICHIESTE DI TOKEN PENDENTI, RIMANGO IN ATTESA CREAZIONE NUOVE RICHIESTE");
					registraWatcherMaster("/token_requests");
				}
				
			} 
			catch (KeeperException e) 
			{
				e.printStackTrace();
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
			
		}
		
	}
	
	
	public void immettiTokenNelSistema() throws KeeperException, InterruptedException
	{
		System.out.println("IMMETTIAMO TOKEN NEL SISTEMA DANDOLO AL MASTER");
		zk.setData("/master", "token".getBytes(),-1);
	}
}
