package controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.event.DocumentEvent.EventType;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.runners.model.FrameworkMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import wrapper.strutture.sincronizzate.WrapperStruttureAggiuntivePerUtentiConnessi;
import wrapper.strutture.sincronizzate.WrapperUtentiConnessi;
import zookeeper.utils.ZkConnectorPerClient;
import zookeeper.utils.ZkConnectorPerMaster;
import database_beans.Utente;


@Controller
public class ControllerPrincipale {

	
	@Autowired
	private ApplicationContext applicationContext;
	
	@Autowired
	private WrapperUtentiConnessi utentiConnessi;
	
	@Autowired
	private WrapperStruttureAggiuntivePerUtentiConnessi additionalInfoUtentiConnessi;
	
	@Value("${zookeeper.hostPathPerClients}")
	String hostZkPerClients;
	
	//su questo url arrivano le richieste http usate dal client per implementare il nostro protocollo di heartbeat
	//per far sapere che il client è sempre con la pagine del browser aperta.
	//Siccome per il protocollo di heartbeat, questo metodo è invocato ogni tot ms da tutti i client loggati, occorre distinguere
	//tra i casi in cui è la prima volta che un utente loggato manda richiesta a questo indirizzo, e quindi occorre in tal caso
	//avviargli il protocollo di heartbeat, o se è già attivo e quindi sono richieste successive
	
	@RequestMapping(value="/heartbeat", method=RequestMethod.POST)
//	@ResponseBody //questo fa si che quanto ritorniamo, al client che manda richiesta HTTP a questo url, venga tradotto direttamente in rappresentazione JSON
	public void heartBeat(HttpServletRequest request, HttpServletResponse response)
	{
//		System.out.println("heart beat method nel controller principale");
//		System.out.flush();
		if(request.getParameter("nome") == null)
			return; //in realtà non è possibile poichè vorrebbe dire che si è arrivati a questo link senza essere loggato ed in post
		
		Utente utente = new Utente();
		utente.setNome(request.getParameter("nome")+"");
		
		//tiro fuori la stringa (ricevuta come parametro della richiesta http) che è la rappresentazione json dell'oggetto
		//che nello script client side javascript mantiene info sullo stato del client
		//e ottengo un oggetto json del framework associato
		JSONObject infoClientSideInJSON = null; //e la userò anche per la risposta
		try
		{
			infoClientSideInJSON = (JSONObject) new JSONParser().parse(request.getParameter("infoClientSide"));
		}
		catch(Exception ex) {ex.printStackTrace(); return;}
		
		//Quindi a questo punto abbiamo come parametro di request il nome utente
		//e tutte le altre variabili locali al client nello script javascript
		 
		
		//in ogni caso quindi, SE NON ESISTE GIA' (perchè è la primissima connessione dopo il log) creo entry per la struttura che mantiene le informazioni relative alle variabili locali del client.
		if(additionalInfoUtentiConnessi.getInfoClientSide(utente) == null)
		{
			//in tal caso mettiamo come nuova entry direttamente quella ottenuta dal client, per semplicità, poichè non va a sovrascrivere nulla
			additionalInfoUtentiConnessi.addInfoClientSide(utente, infoClientSideInJSON);
		}
			
		//Questa entry (modificata qui dal controller) sarà rimandata al client come risposta (in json), e rappresenta il nuovo stato delle sue variabili.
		//Quindi dopo aver creato questa entry nuova, a seeconda dell'interazioen col client, ne riempio i campi (con nuovi valori), senza
		//modificare la struttura ricevuta dal client (poichè in alcuni casi mi serve mantenere il valore delle variabili locali al client,)
		//quindi le teniamo separate
		
		
		
		//vediamo se quell'utente, che è sicuramente loggato, già apparteneva alla lista degli utenti connessi
		if(utentiConnessi.isUtenteConnesso(utente))
		{ //allora in tal caso vuol dire che questa non è la prima volta che l'utente loggato manda richiesta a questa pagina
		  
			System.out.println("controller: "+utente.getNome()+"dice di essere ancora connesso");
			System.out.flush();
			additionalInfoUtentiConnessi.addStatoUtente(utente, new Boolean(true)); //quindi da protocollo di heartbeat sicuramente risulta vivo per ora
			
			//aggiorno lo stato della variabile nella stringa json che poi reinvio al client
			additionalInfoUtentiConnessi.getInfoClientSide(utente).put("confermatoLatoZk", "true");
//			System.out.println(additionalInfoUtentiConnessi.getInfoClientSide(utente));
			
			//e ora vediamo se vuole il token
			if(infoClientSideInJSON.get("wantsToken").equals("true"))
			{
					//il client vuole il token
					//quindi aggiorno la struttura simmetrica qui sul server
					additionalInfoUtentiConnessi.getInfoClientSide(utente).put("wantsToken", "true");
					
					if(infoClientSideInJSON.get("hasToken").equals("false"))
					{ //e non lo ha già
					  //e la volta precedente non lo voleva ancora, quindi è la prima volta che entriamo nel metodo del controller col client che manifesta volontà di avere token
			
						System.out.println("AVVIO OTTENIMENTO TOKEN PER "+utente);
						additionalInfoUtentiConnessi.getInfoClientSide(utente).put("hasToken", "wait"); 
						//lancio il thread, al quale passo il connettore già connesso, l'utente, ed il watcher 
						//che verrà registrato sull'evento della ricezione del token dal nodo che rappresenta in zookeeper
						
						ThreadRegistrazioneRichiestaTokenSuZk thread2 = new ThreadRegistrazioneRichiestaTokenSuZk(utente,additionalInfoUtentiConnessi.getConnectorUtente(utente), new WatcherPerOttenimentoTokenPerClient(utente,additionalInfoUtentiConnessi));	
						thread2.start();
							
					}
					else if(additionalInfoUtentiConnessi.getInfoClientSide(utente).get("hasToken").equals("wait"))
					{	//allora ha già manifestato volonta di avere token, ma non l'ha ancora ricevuto
						System.out.println(utente+" ASPETTA DI OTTENERE TOKEN");
					}
					else
					{
						System.out.println(utente+" HA TOKEN");
					}
					
					
					additionalInfoUtentiConnessi.getInfoClientSide(utente).put("wantsToken", "true");
			}
			else //non vuole il token
			{ 
				additionalInfoUtentiConnessi.getInfoClientSide(utente).put("wantsToken", "false");

				
				if(additionalInfoUtentiConnessi.getInfoClientSide(utente).get("hasToken").equals("true")) //e lo aveva
				{
					System.out.println(utente+" rilascia token");
					//dobbiamo ridare token al master
					
					try 
					{
						additionalInfoUtentiConnessi.getConnectorUtente(utente).ridaiTokenAMaster();
						additionalInfoUtentiConnessi.getInfoClientSide(utente).put("hasToken","false");
					} 
					catch (KeeperException e) 
					{
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
				}
				else if(additionalInfoUtentiConnessi.getInfoClientSide(utente).get("hasToken").equals("wait")) //e lo stava attendendo
				{//in questo caso dobbiamo eliminare la richiesta pendente in /token_requests
					System.out.println(utente+" rinuncia alla richiesta di token");
					
					
					try 
					{
						additionalInfoUtentiConnessi.getConnectorUtente(utente).rimuoviRichiestaToken(utente);
						additionalInfoUtentiConnessi.getInfoClientSide(utente).put("hasToken","false");
					} 
					catch (KeeperException e) 
					{
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					
				}
				

				
			}
		}
		
		else //allora l'utente non è stato ancora aggiunto alla lista degli utenti "connessi"
		{ 
			if(additionalInfoUtentiConnessi.getStatoUtente(utente) == null) 
			{
				//allora è la primissima volta che il client ci manda heartbeat, in tal caso prima di avviare il protocollo di heartbeat, dobbiamo 
				//assicurarci che lato server zookeeper avvenga correttamente la creazione del nodo corrispondente.
				//Non aggiungiamo quindi ancora l'utente alle strutture dati, tranne che allo stato, in modo tale che si possa distinguere
				//la primissima connessione, da una connessione in cui l'utente non figura ancora tra quelli connessi, ma un thread associato sta aspettando che veng creato un nodo su zookeeper
				additionalInfoUtentiConnessi.addStatoUtente(utente, new Boolean(true)); //in questo modo , finchè il thread che ora lanciamo non ha finito, i successivi heartbeat mandati dal client non faranno rientrare in questa sezione
				//e creo quindi il thred che si occupa di iniziare, tramite un nuovo connettore la connessione al client
				additionalInfoUtentiConnessi.getInfoClientSide(utente).put("confermatoLatoZk", "wait");
				new ThreadPerAvvioCreazioneNodoClientSuZk(utente).start();
			}
			
			else
			{	//entriamo qui quando già per un heartbeat precedente siamo entrati nell'if sopra
				//non fare niente, il client continua a mandarci heartbeat, e noi non lo disconnettiamo perchè il checker non è stato ancora avviato
				System.out.println("inizializzazione lato server zk per client "+ utente +" non terminata");
				additionalInfoUtentiConnessi.getInfoClientSide(utente).put("confermatoLatoZk", "wait");
			}
		
		}
		
		
		//quello che ritorniamo qui è la risposta che riceve in ajax il client nello script javascript 
		//e lo ritorniamo come stringa rappresentante json, gli inviamo quindi lo stato aggiornato di quelle che sono le sue variabili locali
		try 
		{
			response.getWriter().println( additionalInfoUtentiConnessi.getInfoClientSide(utente).toJSONString());
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	
	
	
	private class WatcherPerOttenimentoTokenPerClient implements Watcher
	{
		Utente utente;
		WrapperStruttureAggiuntivePerUtentiConnessi additionalInfos;

		public WatcherPerOttenimentoTokenPerClient(Utente utente, WrapperStruttureAggiuntivePerUtentiConnessi additionalInfoUtentiConnessi) {
			this.utente = utente;
			additionalInfos = additionalInfoUtentiConnessi;
			
		}
		@Override
		public void process(WatchedEvent event) {
			//per definizione il watcher sul nodo in /clients/client_nome viene messo solo la prima volta che un client manifesta la volonta di ottenere token
			//quindi non c'e' bisogno di fare controllo sul tipo di evento o sui dati messi
			
//			if(event.getType() != org.apache.zookeeper.Watcher.Event.EventType.NodeDataChanged)
//				return;
			
			System.out.println(event+" per client "+utente);
			System.out.println("TOKEN RICEVUTO PER "+utente);
			System.out.flush();
			
			additionalInfos.getInfoClientSide(utente).put("hasToken", "true");
			//devo cancellare ora la richiesta obsoleta da /token_requests
			try 
			{
				additionalInfos.getConnectorUtente(utente).rimuoviRichiestaToken(utente);
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			} catch (KeeperException e) {
				e.printStackTrace();
			}
			
		}
		
	}

	private class ThreadPerAvvioCreazioneNodoClientSuZk extends Thread
	{
		private static final int MAX_TENTATIVI = 3;
		
		ZkConnectorPerClient connettorePerClient = null;
		Utente utente;
		
		public ThreadPerAvvioCreazioneNodoClientSuZk(Utente utente) {
			super(utente.getNome());
			this.utente = utente;
			
			
		}
		@Override
		public void run() 
		{
			int nTentativi = 0;
			System.out.println("AVVIO THREAD PER CREAZIONE NODO CLIENT "+utente+"SU ZK");
			boolean success = false;
			while(++nTentativi <= MAX_TENTATIVI)
			{	
				try 
				{	 //questo fa partire non solo la connessione al server ma anche la creazione del nodo associato al client
					System.out.println("thred di avvio per "+utente+ " cerco di creare il connettore");
					System.out.flush();
					connettorePerClient = new ZkConnectorPerClient(utente,hostZkPerClients);
					connettorePerClient.connetti();
					connettorePerClient.creaNodoInClients();
					success = true;
					break;
				} 
				catch(NodeExistsException ex)
				{
					success = true;
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
			}
			if(success)
			{
				System.out.println("SUCCESSO CREAZIONE NODO PER UTENTE "+getName()+ " SU ZK, AGGIUNTA IN STRUTTURE DATI E CREAZIONE CHECKER");
				//allora in tal caso dobbiamo avviare il protocollo di heartbeat
				//aggiungo entry nella struttura degli stati
				System.out.println(utente.getNome() + " connesso");
				System.out.flush();
				
				utentiConnessi.addUtente(utente);
				additionalInfoUtentiConnessi.addStatoUtente(utente, new Boolean(true)); //in realtà di questo non c'è bisogno
				additionalInfoUtentiConnessi.addConnectorUtente(utente, connettorePerClient); //aggiungo alla lista dei connettori, quello già creato ed usato per il client
				
				//e gli creo il thread che lo controlla
				ScheduledExecutorService checker = Executors.newSingleThreadScheduledExecutor();
				checker.scheduleAtFixedRate(new Thread( utente.getNome() ) //il nome dell'utente è passato come input al costruttore della classe che estende la classe thread, usata dall'executor
				{

					@Override
					public void run() 
					{
						//controllo se il client ha mandato l'heartbeat
						if(additionalInfoUtentiConnessi.getStatoUtente( getName() )) //allora l'ha mandato
						{
							System.out.println("checker : "+ getName() +"ancora connesso");
							//quindi lo risettiao a falso in modo tale che necessita, per rimanere vivo dal nostro
							//punto di vista, di rimandarne un altro prima del prossimo controllo
							additionalInfoUtentiConnessi.addStatoUtente(getName(), new Boolean(false));
						}
						else
						{ //allora non ha fatto in tempo a mandarci l'heartbeat, quindi lo consideriamo disconnesso
					      //quindi eseguiamo la pulizia delle informazioni relative al client
							
							try
							{
								additionalInfoUtentiConnessi.removeConnectorUtente(utente).disconnect(); //elimino il connettore e lo disconnetto dal server zk
								additionalInfoUtentiConnessi.removeChecker(getName()).shutdown(); //rimuovo e disabilito il checker
								additionalInfoUtentiConnessi.removeStato(getName());
								utentiConnessi.removeUtente(getName());
								//se il client che si disconnette ha token, occorre rimettere token nel sistema (dandolo al master)
								if(additionalInfoUtentiConnessi.getInfoClientSide(utente).get("hasToken").equals("true")) //non c'e' bisogno di metterlo a false tanto la sua pagina ed il suo script javascript si resettano
								{
									//invoco il connettore usato dal master thread (bean singleton) per lanciare metodo di immissione token nel sistema
									ZkConnectorPerMaster conn = applicationContext.getBean("zkConnectorPerMaster",ZkConnectorPerMaster.class);
									conn.immettiTokenNelSistema();
								}
								
								additionalInfoUtentiConnessi.removeInfoClientSide(utente);
								
								System.out.println("checker : "+ getName()+ " disconnesso");
								
								
							}
							catch(Exception ex)
							{
								ex.printStackTrace();
							}
							
						}
						
					}
					
				}, 5000, 5000, TimeUnit.MILLISECONDS);
				//e aggiungo il checker
				additionalInfoUtentiConnessi.addChecker(utente, checker);
			}
			else
			{
				System.out.println("NUMERO TENTATIVI ESAURITO, NODO CLIENT NON CREATO SU ZK");
			}
			
		}
	
	}

	private class ThreadRegistrazioneRichiestaTokenSuZk extends Thread
	{ //questo thread avvia in maniera asincrona (rispetto al creatore del thread) le operazioni per la registrazione (nel nodo apposito di zk) della richiesta di token
		private Utente utente;
		private ZkConnectorPerClient connettorePerClient = null;
		private Watcher watcherEventoRicezioneToken = null; 
		
		//il costruttore di questo thread prende un connettore già connesso, poichè si suppone
		//che per richiedere token già dobbiamo essere connessi
		//il watcher che si passa è quello richiamato quando viene inserito token come nodo nel nodo padre relativo al client nel fs di zookeeper
		public ThreadRegistrazioneRichiestaTokenSuZk(Utente utente, ZkConnectorPerClient connectorGiaConnesso, Watcher watcherEvento) {
			this.utente = utente;
			this.connettorePerClient = connectorGiaConnesso;
			watcherEventoRicezioneToken  = watcherEvento;	
		}
		
		
		@Override
		public void run() {
			final int MAX_TENTATIVI = 3;
			System.out.println("AVVIO THREAD PER CREAZIONE NODO ZK PER RICHIESTA TOKEN DA CLIENT "+utente.getNome());
			boolean success = false;
			int nTentativi = 0;
			while( ++nTentativi <= MAX_TENTATIVI )
			{
				try 
				{	 //questo fa partire non solo la connessione al server ma anche la creazione del nodo associato al client
					if(!connettorePerClient.isConnected()){
						break;
					}
					//ora questo metodo aggiunge nodo della richiesta in zk, e registra watcher sul cambio dei dati del client
					//perchè è lì che ci viene indicato che abbiamo token
					connettorePerClient.aggiungiRichiestaToken(utente,watcherEventoRicezioneToken);
					success = true;
					break;
				} 
				catch(NodeExistsException ex)
				{
					success = true;
					break;
				}
				catch (KeeperException e) {
					e.printStackTrace();
				} 
				catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			if(success)
			{
				System.out.println("RICHIESTA TOKEN PER "+utente+ " REGISTRATA CON SUCCESSO");
			}
			else
			{
				System.out.println("RICHIESTA TOKEN PER "+utente+ "NON REGISTRATA");
			}
			
		}
	}
	
}


