package database_beans;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;


@Component("implDaoPerUtente")
@Scope("prototype")
//implementazione del dao per l'utente, che inoltre utilizza la tecnologia jdbc template (di spring per usare jdbc su un db di tipo mysql)
public class UtenteDaoPerJdbcMysql_Impl implements UtenteDao{

	
	private JdbcTemplate jdbcTemplate; //di questo non facciamo l'injection usando il paradigma di spring, in quanto non ce n'e' bisogno
	//ma viene riempito quando viene fatta l'injection del datasource
	
	
	private DataSource dataSource;
	
	

	public DataSource getDataSource() {
		return dataSource;
	}

	@Resource(name="injdatasource")
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		jdbcTemplate = new JdbcTemplate(this.dataSource); //questo fa si che si apra la connessione verso il db
	}

	@Override
	public List<Utente> getAllUtenti() {

		List<Utente> al = jdbcTemplate.query("select * from utenti", new UtenteRowMapper());
		return al;
		
	}

	@Override
	public Utente getUtente(String nome) {
		//hack necessario perchè se usassimo la jdbcTemplate.queryForObject darebbe problemi nel caso di 0 o piu' di un risultato
		String sql = "select password from utenti where nome = ?";
		List<String> dummyList = jdbcTemplate.queryForList(sql, new Object[]{nome}, String.class);
		
		Utente toRet = null;
		if(dummyList.size() > 0)
		{
			toRet = new Utente();
			toRet.setNome(nome);
			toRet.setPassword(dummyList.get(0));
		}
		
		return toRet;
	}

	@Override
	public void inserisciUtente(String nome, String password) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void rimuoviUtente(String nome) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void aggiornaUtente(String nome, String nuovoNome,
			String nuovaPassword) {
		// TODO Auto-generated method stub
		
	}

	public void chiudi()
	{
		//TO DO
	}
	
	public class UtenteRowMapper implements RowMapper<Utente>
	{

		@Override
		public Utente mapRow(ResultSet rs, int arg1) throws SQLException {
			
			Utente toReturn = new Utente();
			
			toReturn.setNome(rs.getString( 1));
			toReturn.setPassword(rs.getString(2));
			
			return toReturn;
		}
		
	}
}
