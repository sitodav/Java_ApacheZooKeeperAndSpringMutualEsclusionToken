package zookeeper.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


//questo bean non è ha scope prototype, ma scope singleton, quindi viene automaticamente 
//istanziato quando viene caricato il contesto della web application
@Component(value="connettorePerConfigurazioni")
public class ZkConnectorPerConfigurazioni extends ZkConnectorAbstract{
	
	
	@Value("${zookeeper.hostpath}") //facciamo dependency injection della stringa serverPath usando il contenuto del file .properties
	@Override
	public void setServerPath(String serverPath) throws IOException {
		super.setServerPath(serverPath);
	}
	
	//metodo che visita il server zookeeper (all'indirizzo ottenuto dal file locale .properties)
	//e ottiene tutta la gerarchia (visitando ricorsivamente) della struttura mappata in zookeeper, 
	//portando tutto in una hashmap dove per ogni nodo, è memorizzato nell'hash il percorso a quel nodo, e come valore  il valore memorizzato in quel nodo zk
	//(si noti che il percorso ad un nodo zk è memorizzato non nella forma /nodoa/nodob/nodoc  ma come nodoa.nodob.nodoc in modo tale da poter utilizzare
	//la struttura di un "filesystem" di zk come se fosse un file di configurazione di tipo properties per spring)
	public void risolviGerarchiaZooKeeperComeFormaDocumentoProperties(String actualPathInZk, HashMap<String,String> toFill) throws Exception
	{
		
		if(zk == null) throw new Exception("connettore (client) non connesso al server");
		
		String t = (actualPathInZk.length() > 1) ? actualPathInZk.substring(0, actualPathInZk.length()-1) : actualPathInZk ;
		List<String> children =zk.getChildren(t, this);
		String prefixPropertiesStyle = (actualPathInZk.length() > 1 ) ? actualPathInZk.substring(1).replace("/", ".") : "";
		for(String child : children)
		{
			String dataInChild = new String(zk.getData(actualPathInZk+child, this, new Stat()));
			toFill.put(prefixPropertiesStyle+child, dataInChild);
			risolviGerarchiaZooKeeperComeFormaDocumentoProperties(actualPathInZk+child+"/",toFill);
		}
		
	}
	
	
//	//metodo per registrare un watcher su tutti i nodi figli da un nodo attuale a scendere (in input vuole nome nodo con / finale aggiunto)
//	public void addWatcherPerModificaNodoZkProprietaEFigli(String pathActualNode, Watcher watcher) throws Exception
//	{
//		if(zk == null) throw new Exception("connettore (client) non connesso al server");
//		
//		//ricorsivamente visito tutti i nodi da nodo root usato per le configurazioni (il cui valore è salvato
//		//nelle proprieta del file .properties LOCALE) e i figli, settando su tutti un watcher
//		String t = pathActualNode.substring(0,pathActualNode.length()-1);
//		List<String> figli = zk.getChildren(t, null);
//		for(String nomeFiglio : figli)
//		{
//			addWatcherPerModificaNodoZkProprietaEFigli(pathActualNode+nomeFiglio+"/", watcher);
//			
//		}
//		//registro sul nodo attuale il watcher
//		zk.getData(t, watcher, new Stat());
//		System.out.println("messo watcher sul cambio del valore nodo di configurazione "+t);
//		
//	}
//	
//	//stesso di sopra ma senza metterlo sui figli (in input vuole nome nodo SENZA / finale)
//	public void addWatcherPerModificaNodoZkProprietaSingolo(String path, Watcher watcher) throws KeeperException, InterruptedException
//	{
//		zk.getData(path, watcher, new Stat());
//		System.out.println("messo watcher sul cambio del valore nodo di configurazione "+path);
//		
//	}
	
}
