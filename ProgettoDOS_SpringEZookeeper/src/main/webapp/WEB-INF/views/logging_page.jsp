<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Log here</title>
<!-- sorgenti javascript per usare jquery -->
<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
<!-- fogli css e stili di bootstrap-->
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap.min.css">
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap-theme.min.css">

<style>
	.messaggioErroreForm
	{
			color: red;
			font-size: 18px;
			font-weight: bolder;
	}
	 body {
	  padding-top: 40px;
	  padding-bottom: 40px;
	  background-color: #eee;
	}
	
	.form-signin {
	  max-width: 330px;
	  padding: 15px;
	  margin: 0 auto;
	}
	 
</style>

</head>
<body>


		<div class="container col-sm-3" >
		<form class="form-signin" action="/sito/checklog" method="post"> <!-- nei sorgenti del framework spring il contesto è automaticamente considerato quello corretto
		mentre nei file jsp, con il simbolo / ci si riferisce al contesto del web container, al quale va aggiunto quello dato dal nome dell'artifact -->
			<p class="messaggioErroreForm">${msgAggiuntivoErrore}</p>
			<form:errors class="messaggioErroreForm"  path="utente.*" /> <!-- questo tag mostra un messaggio quando si arriva a questo form da un model view (ritornato da un controller) che è quello
			per il quale il binder ha dato un errore, questo può capitare o quando sono stati inseriti dati di un formato errato
			o quando non viene soddisfatto un constraint di validazione (dato da annotazione) messo sul bean target
			In questo caso quando il form punta su /sito/checklog ed il binder carica i dati del form nel bean di tipo Utente, esegue la validazione invocata
			dall'annotazione CheckDatiSuDb utilizzata per il bean di tipo Utente. Se fallisce, allora è valorizzato il tag form:errors relativamente a qualunque campo
			del bean, associato al tag utente, possa aver dato errore -->
			<h2 class="form-signin-heading">Inserire dati:</h2>
			<label for="inputName" class="sr-only">Nome</label>
			<input id="inputName" type="text" class="form-control" name="nome" autofocus="" placeholder="nome" required/><br>
			<label for="inputPass" class="sr-only">Password</label>
			<input type="password" id="inputPass" class="form-control" name="password" placeholder="password" required/><br>
			<button class="btn btn-lg btn-info btn-block" type="submit">Log in</button>
		</form>
		</div>
	

</body>
</html>