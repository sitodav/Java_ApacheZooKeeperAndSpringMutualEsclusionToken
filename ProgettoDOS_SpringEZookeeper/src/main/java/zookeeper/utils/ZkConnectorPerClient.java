package zookeeper.utils;

import java.io.IOException;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import database_beans.Utente;

//viene creato un connettore di questo tipo per ciascun utente connesso
@Component("connettorePerClient")
@Scope("prototype")
public class ZkConnectorPerClient extends ZkConnectorAbstract {

	Utente utente;
	
	@Value("${zookeeper.hostPathPerClients}")
	String hostPath; 
	
	//questo injection non fa partire connessione a zk
	
	@Override
	public void setServerPath(String serverPath) throws IOException {
		//ne facciamo override perchè in questo ci serve che la connessione non avvenga nel setter
		//come avviene dalla classe madre da cui ereditiamo
		//quindi niente qui
		hostPath = serverPath;
		
	}
	
	public ZkConnectorPerClient(Utente utente, String hostPath) 
	{
		this.utente = utente;
		this.hostPath = hostPath;
		
	}
	
	public void connetti() throws IOException
	{
		zk = new ZooKeeper(hostPath, 15000,this);
	}
	
	public void creaNodoInClients() throws KeeperException, InterruptedException, IOException
	{
		
		//quindi registriamo nella struttura di zk nuovo client
		Thread.sleep(10000); //necessario
		System.out.println(zk);
		zk.create("/clients/client_"+utente, "stringadummy".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
	}
	
	//aggiungiamo richiesta token : mettiamo richiesta in token_requests e mettiamo un watcher sul nodo del client (alla ricezione)
	public void aggiungiRichiestaToken(Utente utente, Watcher watcher) throws KeeperException, InterruptedException
	{
		
		zk.create("/token_requests/richiesta_"+utente.getNome(), new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
		zk.getData("/clients/client_"+utente.getNome(), watcher, new Stat()); //registro il watcher usl valore del nodo associato all'utente (assume valore "hasToken" quando il  client ha ricevuto token"
	}
	
	public void rimuoviRichiestaToken(Utente utente) throws InterruptedException, KeeperException
	{
		zk.delete("/token_requests/richiesta_"+utente.getNome(), -1);
	}
	
	public void ridaiTokenAMaster() throws KeeperException, InterruptedException
	{
		zk.setData("/master", "token".getBytes(), -1); //e questo farà partire il suo watcher
	}
	
	
	
	
	
	
	
	
	

}
