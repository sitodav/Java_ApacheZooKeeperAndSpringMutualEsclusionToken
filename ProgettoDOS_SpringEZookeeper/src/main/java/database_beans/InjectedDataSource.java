package database_beans;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;


/*questo bean contiene le informazioni del db a cui connettersi (username, pw e url db) e le tira fuori dalle proprietà
caricate nell'environment utilizzando il connettore a zookeeper. Per questo motivo può essere istanziato soltanto
dopo che queste proprietà sono state caricate nell'enviroment, cioe' dopo che la classe ImporterZookeeperComeProperties ha fatto il suo dovere 
ecco perchè quindi è di tipo prototyupe*/


@Component("injdatasource")
@Scope("prototype")
public class InjectedDataSource extends DriverManagerDataSource{

	
	
	//queste 3 proprietà sono caricate tramite l'importer (e quindi il connettore zk) dall'ensemble di zookeeper
	
	@Override
	@Value("${database.url_db}")
	public void setUrl(String url) {
		System.out.println("--------------------------------------------------------------------------------------------"+url);
		super.setUrl(url);
	}
	
	@Override
	@Value("${database.username}")
	public void setUsername(String username) {
		super.setUsername(username);
	}
	
	@Override
	@Value("${database.password}")
	public void setPassword(String password) {
		super.setPassword(password);
	}
	
	@Override
	@Value("${database.driverClassName}")
	public void setDriverClassName(String driverClassName) 
	{
		super.setDriverClassName(driverClassName);
	}
	
	public String getUsername() { return super.getUsername(); }
	public String getPassword() { return super.getPassword(); }
	public String getUrl() { return super.getUrl(); }
													
}
