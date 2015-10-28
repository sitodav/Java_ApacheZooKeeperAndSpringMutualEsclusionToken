package beans;

import java.util.HashMap;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import zookeeper.utils.ZkConnectorPerConfigurazioni;


//questo bean ha scope singleton, quindi viene istanziato quando si carica il contesto
//questo bean è utilizzato per configurare l'environment, caricandovi le proprietà ottenute
//elaborando la struttura del filesystem dell'ensemble di zk, passando per il connettore
//siccome alcune operazioni di aggiornamento dell'environment, richiedono il refresh dell'application context, refresh che porta quest'oggetto ad essere reistanziato
//utilizziamo una variabile booleana per evitare di entrare in un loop di riavvi del contesto

//NB: questo bean lascia anche un watcher sul cambiamento di uno dei valori dei nodi di zookeeper usati per la configurazione remota distribuita
//in modo tale che quando cambia uno dei valori, restarta la web application

@Component("aggiornatore")
@ComponentScan(basePackages="zookeeper.utils") //necessario affinchè possa ottenere il bean che sta in un altro package
public class ImporterZookeeperComeProperties implements ApplicationContextAware, InitializingBean/*, Watcher*/
{

	private static boolean firstTime = true;
	
	@Autowired
	WebApplicationContext webApplicationContext; 
	
	ApplicationContext rootContext; //questo viene ottenuto grazie all'interfaccia ApplicationContextAware
	
//	@Resource(name="connettore") //injection del bean zkConnector
	@Autowired
	ZkConnectorPerConfigurazioni connettorePerConfigurazioni;
	
	@Autowired 
	Environment environment; //l'environment generale dell'applicazione, nel quale vogliamo inserire la struttura dei nodi
	//letti da zookeeper come insieme di proprietà (come se le stessimo leggendo da un file.properties)
	
	@Value("${zookeeper.rootPerProperties}") //il path da cui partire (nel fs zookeeper) per considerare i nodi come elementi-proprietà
	String startingPathPerPropertiesOnZk;

	public void aggiornaEnvironment(){
		HashMap<String,String> proprietaDaZk = new HashMap<String,String>();
		try
		{
			connettorePerConfigurazioni.risolviGerarchiaZooKeeperComeFormaDocumentoProperties(startingPathPerPropertiesOnZk, proprietaDaZk);
			
			
			Properties propertiesDaAggiungereAlContestoSpring = new Properties();
			
			for(String nomeProprieta : proprietaDaZk.keySet())
			{
				
				//per il nome proprietà creato concatenando i padri-figli della gerarchia di zk, e trasformandoli nella forma
				//di proprietà come contenute in un file.properties, eliminiamo quella che è la parte iniziale di startingPathPerProperties
				String nomeProprietaDopoStartingPathPerProperties = nomeProprieta;
				if(!startingPathPerPropertiesOnZk.equals("/"))
					nomeProprietaDopoStartingPathPerProperties = nomeProprieta.substring( nomeProprieta.indexOf(startingPathPerPropertiesOnZk.substring(1))+startingPathPerPropertiesOnZk.length());
				
				
				propertiesDaAggiungereAlContestoSpring.setProperty( nomeProprietaDopoStartingPathPerProperties, proprietaDaZk.get(nomeProprieta));
				System.out.println(nomeProprietaDopoStartingPathPerProperties+ " "+ proprietaDaZk.get(nomeProprieta));
			}
			PropertySourcesPlaceholderConfigurer conf = new PropertySourcesPlaceholderConfigurer();
			conf.setProperties(propertiesDaAggiungereAlContestoSpring);
			conf.setIgnoreUnresolvablePlaceholders(true);

			
			
			//dobbiamo aggiungere il nuovo propertysourceplaceholderconfigurer nel contesto dell'applicazione generale
			//che otteniamo da quell
			
			
			((AbstractApplicationContext)rootContext).addBeanFactoryPostProcessor(conf);   
			((AbstractApplicationContext)rootContext).refresh();
			
			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	
	@Override
	public void setApplicationContext(ApplicationContext ac)
			throws BeansException {
			
			rootContext = ac;
		
	}

	

	@Override
	public void afterPropertiesSet() throws Exception {
		
		if(firstTime)
		{ 
//			System.out.println("AFTER SET PROPERTIES***********AGGIORNO ENVIRONMENT**********************************+");
			firstTime = false;
			this.aggiornaEnvironment();
			
//			//mettiamo il watcher per far si che venga restartata l'applicazione nel caso in cui cambi qualche proprietà (valore del nodo di zookeeper) remota
//			connettorePerConfigurazioni.addWatcherPerModificaNodoZkProprietaEFigli(startingPathPerPropertiesOnZk,this);
			
		}
//		else
//		{
//			System.out.println("AFTER SET PROPERTIES***********NON AGGIORNO ENVIRONMENT**********************************+");
//		}
		return;
	}


//	//questo è il metodo che parte quando uno dei nodi (/config e figli) zk usati per le conf distribuite, cambia di valore 
//	@Override
//	public void process(WatchedEvent event) {
//		String nodeSourcePath = event.getPath();
//		
//		System.out.println("WATCHER PER NODO CONFIGURAZIONE "+nodeSourcePath);
//		//riregistro watcher sul nodo che ha generato evento
//		try 
//		{
//			connettorePerConfigurazioni.addWatcherPerModificaNodoZkProprietaSingolo(nodeSourcePath, this);
//		} 
//		catch (KeeperException e) 
//		{
//			e.printStackTrace();
//		} 
//		catch (InterruptedException e) 
//		{
//			e.printStackTrace();
//		}
//		
//		//e riavviamo il contesto dell'applicazione (e anche quello della web application in quanto è lì che c'e' il dao che utilizza le proprieta')
////		
//		firstTime = true; //dopo aver settato la variabile statica a true, in modo tale che venga reinstanziato anche questo stesso oggetto importerzookeepercomeproperties, che si occuperà di ricaricare
//		//i valori delle proprietà dai nodi di zk
//		
//		System.out.println("E' CAMBIATO UN NODO DI CONFIGURAZIONE DISTRIBUITA, RIAVVIO CONTESTO APPLICAZIONE");
//		
//		((AbstractApplicationContext)webApplicationContext).refresh();
//		((AbstractApplicationContext)rootContext).refresh();
//		
//		
//	}
	
	

}
