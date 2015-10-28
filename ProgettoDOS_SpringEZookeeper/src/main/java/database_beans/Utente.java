package database_beans;

import annotazionicustom.CheckDatiSuDb;

@CheckDatiSuDb(message="inserire nome/password corretti") //usiamo una nostra annotazione custom che fa si che parta un validatore (associato a quest'annotazione custom)
//quando al bean è associato il tag @Valid, ad esempio quando c'e' l'uso di un form e quindi dell'oggetto ModelAttribute per farne il binding
public class Utente {
	
	private String nome;
	private String password;
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	@Override
	public String toString() {
		return this.nome;
	}
	
}
