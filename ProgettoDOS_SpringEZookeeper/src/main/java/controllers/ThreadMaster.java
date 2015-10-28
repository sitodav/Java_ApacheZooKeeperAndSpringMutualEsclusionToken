package controllers;

import java.io.IOException;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import zookeeper.utils.ZkConnectorPerMaster;


//questo è il thread che si occupa di interfacciarsi come master con zookeeper
//Viene messo fuori dal ControllerPrincipale (a differenza dei thread come riguardano i client) come classe
//poichè necessita di essere istanziato come bean di spring allo start dell'applicazione, quindi occorre dargli una visibilita
//maggiore, 
//tale thread si occupa di lavorare in zk come coordinatore


@Component("ThreadMasterPerZk")
@ComponentScan(basePackages="zookeeper.utils")
@Scope("singleton")

public class ThreadMaster extends Thread implements InitializingBean //initializing bean in modo tale che quando è stato riempito tutto il bean è possibile startarlo
{
	@Autowired
	ZkConnectorPerMaster zkConnectorPerMaster;
	
	
	
	static boolean toInitialize = true;
	
	
	@Override
	public void run() {
		//creiamo nodo del master in zk
		
		while(true)
		{
			try 
			{	 
				System.out.println("thread di avvio per creazione nodo MASTER su zk");
				System.out.flush();
				 
				zkConnectorPerMaster.creaNodoMaster();
				
				break;
			} 
			catch(NodeExistsException ex)
			{
				break;
			}
			catch (KeeperException e) {
				e.printStackTrace();
			} 
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			catch(IOException ex){
				ex.printStackTrace();
			}
			System.out.println("CIAO DA THREAD MASTER");
			System.out.flush();
		}
		
		
		
		System.out.println("NODO MASTER CREATO SU ZK");
		System.out.flush();
		
		while(true)
		{
			try 
			{	 
				System.out.println("thread di avvio per creazione watcher su token per nodo MASTER su zk");
				System.out.flush();
				
				//la prima volta il watcher è registrato sul cambiamento del valore nel nodo del master
				//quindi partirà quando il master riceverà token
				zkConnectorPerMaster.registraWatcherMaster("/master");
				//e a questo punto, il master si auto da il token (visto che siamo all'avvio, questo equivale a 
				//mettere in circolazioen il token, poichè farà partire il watcher del master sulla ricezione token)
				zkConnectorPerMaster.immettiTokenNelSistema();
				
				break;
			} 
			catch(NodeExistsException ex)
			{
				break;
			}
			catch (KeeperException e) {
				e.printStackTrace();
			} 
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			System.out.println("CIAO DA THREAD MASTER");
			System.out.flush();
		}
		
		while(true); //il thread master è tutto event driven (tramite i suoi watcher) quindi
		//nel run siamo in ciclo continuo
		
	}



	@Override
	public void afterPropertiesSet() throws Exception {
		
		if(toInitialize) //usato per evitare che il restart del contesto (operato da noi quando facciamo injectiondella configurazione distribuita), provochi avvio di 2 master threads
		{
		
			start(); //cioè si avvia il thread quando è terminata l'injection del thread
			toInitialize = false;
		}
		
	}
	
	
	
	
}