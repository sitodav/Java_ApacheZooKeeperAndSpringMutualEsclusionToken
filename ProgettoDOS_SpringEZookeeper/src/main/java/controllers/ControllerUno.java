package controllers;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;

import wrapper.strutture.sincronizzate.WrapperUtentiConnessi;
import database_beans.Utente;
import database_beans.UtenteDaoPerJdbcMysql_Impl;


//questo è il controller che riguarda gli url e le operazioni di login

@Controller
public class ControllerUno  { 

	
	//questa struttura è riempita dall'altro controller 
	@Autowired
	private WrapperUtentiConnessi utentiConnessi;
	
	@Value("${zookeeper.hostPathPerClients}")
	String urlServerZkPerClient; //qui viene iniettato il valore 
	//dell'url di un server zk usato per interagire col client (per i lock etc...)
	
	
	
	//mappa sull'url base del contesto della web application, che essendo di tipo spring mvc è l'indirizzo
	//del server seguito da /artifact del pom.xml di maven
	@RequestMapping(value="/",method=RequestMethod.GET) 
	public ModelAndView root(HttpServletRequest request) //spring automaticamente fa injection dell'httpservletRequest
	{
		
		
		ModelAndView toRet = null;
		
		
		//DECOMMENTARE QUESTO (E COMMENTARE LA VERSIONE DI SOTTO) 
		//PER UTILIZZARE LE SESSIONI HTTP
//		if(request.getSession().getAttribute("logged") != null) //in tal caso l'utente che si è connesso a questo url è già loggato
//		{
//			toRet = new ModelAndView("logged_page");
//		}
//		else //l'utente non è ancora loggato quindi viene mandato alla login page
//		{
//			toRet = new ModelAndView("logging_page");
//		}
		
//		if(request.getAttribute("nome") != null) //in tal caso l'utente che si è connesso a questo url è già loggato
//		{
//			toRet = new ModelAndView("logged_page");
//		}
//		else //l'utente non è ancora loggato quindi viene mandato alla login page
//		{
			toRet = new ModelAndView("logging_page");
//		}
		
	
		return toRet;
	}
	
	
	
	
	@RequestMapping(value="/checklog", method=RequestMethod.POST)
	//usando la tecnica del @ModelAttribute otteniamo un binding dei dati del form che ci ha indirizzati a questo url
	//questa tecnica fa si che se si inseriscono dei dati in un form (quello che punta a questo url) questi vengono
	//inseriti in un bean che li mappa (in questo caso utenteToCheck).
	//Inoltre è possibile creare annotazioni custom per indicare quali campi di tale bean devono superare
	//quali vincoli. Nel nostro caso creiamo un'annotazione per controllare che i dati nome/password esistano nel db
	//e solo in questo caso il binding result non conterrà errori
	public ModelAndView checkLog(@Validated @ModelAttribute("utente") Utente utenteToCheck, BindingResult res ,HttpServletRequest request)
	{
		System.out.println(" checklog method nel controller uno");
		System.out.flush();
		
		ModelAndView toRet = null;
		
		if(res.hasErrors()) // il binder non ha trovato(tra i dati inseriti nel form della pagina di logging, inseriti nell'oggetto utenteToCheck)
		{					//e i dati presenti nel database.
			toRet = new ModelAndView("logging_page");
		}
		else if(utentiConnessi.isUtenteConnesso(utenteToCheck))
		{
			toRet = new ModelAndView("logging_page");
			toRet.addObject("msgAggiuntivoErrore","Client "+utenteToCheck+" già connesso");
		}
		else //l'utente con quella password esiste, poichè il binder nell'iniettare i dati dal form all'oggetto utenteToCheck ha trovato una corrispondenza nel db
			//settiamo inoltre l'attributo nella sessione "http" in modo tale che venga ricordato come registrato 
		{
			request.getSession().setAttribute("logged", "logged");
			request.getSession().setAttribute("nome",utenteToCheck.getNome());
			
			toRet = new ModelAndView("logged_page");
			toRet.addObject("messaggio","riusciti a loggare"); //istruzioni equivalenti al setParameter nell'http servlet request
			toRet.addObject("nome",utenteToCheck.getNome()); 
		}
		
		
		return toRet;
	}

	
	
	@RequestMapping(value="/invalidaSessione", method=RequestMethod.GET)
	public ModelAndView invalida(HttpServletRequest request)
	{
		System.out.println("RICHIESTA DI LOGOUT");
		request.getSession().invalidate();
		return new ModelAndView("logging_page");
		
	}
	
}
