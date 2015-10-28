package annotazionicustom;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

//Qui dichiariamo la nostra notazione @ personalizzata (che si basa su di un validatore associato)
//per validare i bean che vengono mappati dal binder quando si utilizza il form di login

@Documented
@Constraint(validatedBy = ValidatoreCheckDatiSuDb.class)	//classe di validazione associata
@Target (ElementType.TYPE) //indica che l'annotazione non è su singolo campo ma su classe
@Retention(RetentionPolicy.RUNTIME)

public @interface CheckDatiSuDb {

	//lista degli attributi utilizzabili nel tag
	public abstract String message() default "username/password errati"; 
	
	//necessari da framework
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};
	
}
