package zookeeper.utils;

import java.io.IOException;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.beans.factory.annotation.Value;

public abstract class ZkConnectorAbstract implements Watcher {
	protected ZooKeeper zk;
	protected String serverPath;
	
	public void setServerPath(String serverPath) throws IOException
	{
		this.serverPath = serverPath;
		zk = new ZooKeeper(serverPath,10000,this); //questo fa partire automaticamente il costruttore dell'oggetto ZooKeeper per il quale non è fatta dependency injection con l'IoC
	}
	
	public boolean isConnected() throws KeeperException, InterruptedException
	{
		return (zk.exists("/", false) != null );
	}
	
	public String getServerPath()
	{
		return this.serverPath;
	}

	@Override
	public void process(WatchedEvent event) {
		System.out.println("Evento su connector (client) :"+event);
	}
	
	public void disconnect() throws InterruptedException
	{
		zk.close();
	}
}
