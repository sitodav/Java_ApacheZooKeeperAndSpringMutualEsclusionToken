package annotazionicustom;

import java.util.ArrayList;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;

import database_beans.Utente;
import database_beans.UtenteDaoPerJdbcMysql_Impl;


public class ValidatoreCheckDatiSuDb implements ConstraintValidator<CheckDatiSuDb,Utente>{

	@Autowired
	WebApplicationContext webApplicationContext;
	
	@Override
	public void initialize(CheckDatiSuDb arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isValid(Utente utenteBindato, ConstraintValidatorContext arg1) {
		
//		//Per fare validazione, occorre ottenere connessione al db
//		//chiedo quindi istanza del bean di classe UtenteDaoPerJdbcMysql_Impl che non è altro che 
//		//un'implementazione di un dao per la classe utente, che usa tecnologia jdbc per db di tipo mysql
//		//ottenendo informazioni (relativamente al db a cui collegarsi) dalle proprietà precedentemente ottenute 
//		//dall'ensemble zookeeper
		UtenteDaoPerJdbcMysql_Impl dao = (UtenteDaoPerJdbcMysql_Impl) webApplicationContext.getBean("implDaoPerUtente");
		Utente ritornato = dao.getUtente(utenteBindato.getNome());
		dao.chiudi();
		
		if(ritornato != null && ritornato.getPassword().equals(utenteBindato.getPassword()))
		{
			//la password corrisponde
			return true;
		}
		return false;
		
		
		
	}

	
	 

}
