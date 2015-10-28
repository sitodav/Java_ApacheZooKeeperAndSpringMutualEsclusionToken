<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>

<!-- sorgenti javascript per usare jquery -->
<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
<!-- fogli css e stili di bootstrap-->
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap.min.css">
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap-theme.min.css">
<style>
	#messaggioErroreForm
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
	
	 
	.contenitore
	{ max-width: 330px;
	  
	  margin: 10px;
	}
	
	.testo
	{
		font-weight: bold;
	}
	.invisibile
	{
	    visibility: hidden;
	}
	
</style>
</head>
<body>
	<div class="contenitore">
		<!-- questi elementi gui vengono aggiornati dallo script javascript quando riceve risposte ajax -->
		<h1 class="titolo">MENU</h1><br>
		<div class="alert alert-success" role="alert">
			<p class="testo">connected as <span id="nomeUtente">${nome}</span></p>
		</div>
		<div id = "coloreConfirmed" class="alert alert-danger" role="alert">
			<p id="msgConfirmed" class="testo">not confirmed su ZK<span class="invisibile" id="confermatoLatoZkDiv">false</span></p>
		</div>
		<div id = "coloreHasToken" class="alert alert-danger" role="alert">
			<p id="msgHasToken"class="testo">has no token<span id="hasTokenDiv" class="invisibile">false</span></p>
		</div>
		<div id = "coloreWantsToken" class="alert alert-danger" role="alert">
			<p class="testo" id="msgWantsToken"  onClick="clickWantsToken()" >doesn't want token <span id="wantsTokenDiv" class="invisibile" >false</span><p>
		</div>
		<%@include file="included/logout.jsp" %>
	</div>
	
	
	<br><br>
	
	 
	
	<!-- script usato per mandare l'heartbeat -->
	<script>
	
		var curLocation = window.location;
		var urlDestinazioneHeartbeat = curLocation.protocol + "//" + curLocation.host + "/sito/heartbeat";
		
		//variabili usate per indicare lo stato del client (es se vuole token etc, se lo ha ricevuto...)
		//vengono salvate in un oggetto e mandate al server come stringa json (rappresentazione dell'oggetto) salvata
		//come parametro post nella richiesta http
		var nomeUtente = $("#nomeUtente").text();
		var objectInfoClientSide = {};
		objectInfoClientSide["hasToken"] = "false";
		objectInfoClientSide["wantsToken"] = "false";
		objectInfoClientSide["confermatoLatoZk"] = "false";  //diventa true quando la wep application è riuscita a creare sul server zk un nodo associato a questo client
		
		
		//funzione che parte quando clicchiamo sul tasto per ottenere token
		function clickWantsToken()
		{
			if(objectInfoClientSide["wantsToken"] == "false")
			{
				objectInfoClientSide["wantsToken"] = "true";
			}
			else if(objectInfoClientSide["wantsToken"] == "true")
			{
				objectInfoClientSide["wantsToken"] = "false";
			}
		}
		
		
		
		
		//si tratta di un semplice script javascript che ogni secondo manda una richiesta di tipo GET (asincrona per quanto riguarda la risposta, quindi 
		//non è altro che ajax, anche se nel nostro caso non ci interessa la risposta) all'indirizzo sul quale
		//è in ascolto il controller spring che si occupa di resettare su "connesso" lo stato di tale client
		window.setInterval(function(){
			var xmlHttp = new XMLHttpRequest();
		    xmlHttp.onreadystatechange = function() //funzione chiamata quando riceviamo risposta dal server
		    {
		    	if (xmlHttp.readyState == 4) 
		    	{
			        if (xmlHttp.status == 200) 
			        {
			        
			            // in xmlHttp.responseText ho la stringa json che rappresenta la risposta del server;
			            var  risposta = JSON.parse(xmlHttp.responseText);
			            console.log(risposta);
			            //aggiorno lo stato delle variabili locali al client e di conseguenza il valore della gui
			            objectInfoClientSide.confermatoLatoZk = risposta.confermatoLatoZk;
			            $("#confermatoLatoZkDiv").text(objectInfoClientSide.confermatoLatoZk); //cambio il valore del testo (anche se l'ho nascosto...)
			            //ed il colore dei messaggi
			            $("#coloreConfirmed").removeClass();
			            if(objectInfoClientSide.confermatoLatoZk == 'true')
			            {
			            	$("#coloreConfirmed").addClass("alert alert-success");
			            	$("#msgConfirmed").text("confirmed on ZK");
			            }
			            else if(objectInfoClientSide.confermatoLatoZk == 'wait')
			            {
			            	$("#coloreConfirmed").addClass("alert alert-warning");
			            	$("#msgConfirmed").text("waiting for confirmation on ZK");
			            }
			            else if(objectInfoClientSide.confermatoLatoZk == 'false')
			            {
		            		$("#coloreConfirmed").addClass("alert alert-danger");
		            		$("#msgConfirmed").text("not confirmed on ZK");
			            }	
			            	
			            
			            objectInfoClientSide.hasToken = risposta.hasToken;
			            $("#hasTokenDiv").text(objectInfoClientSide.hasToken);	
			            $("#coloreHasToken").removeClass();
			            if(objectInfoClientSide.hasToken == 'true')
			            {
			            	$("#coloreHasToken").addClass("alert alert-success");
			            	$("#msgHasToken").text("has token");
			            }
			            else if(objectInfoClientSide.hasToken == 'wait')
			            {
			            	$("#coloreHasToken").addClass("alert alert-warning");
			            	$("#msgHasToken").text("waiting for token");
			            }
			            else if(objectInfoClientSide.hasToken == 'false')
			            {
		            		$("#coloreHasToken").addClass("alert alert-danger");
		            		$("#msgHasToken").text("has no token");
			            }
			            
			            
			            
			            
			            
			            objectInfoClientSide.wantsToken = risposta.wantsToken;
			            $("#wantsTokenDiv").text( objectInfoClientSide.wantsToken);
			            $("#coloreWantsToken").removeClass();
			            if(objectInfoClientSide.wantsToken == 'true')
			            {
			            	$("#coloreWantsToken").addClass("alert alert-success");
			            	$("#msgWantsToken").text("wants token");
			            }
			            else if(objectInfoClientSide.wantsToken == 'wait') //questo non succede mai
			            {
			            	$("#coloreWantsToken").addClass("alert alert-warning");
			            	$("#msgWantsToken").text("--");
			            }
			            else if(objectInfoClientSide.wantsToken == 'false')
			            {
		            		$("#coloreWantsToken").addClass("alert alert-danger");
		            		$("#msgWantsToken").text("does not want token");
			            }
			            
			            
			            
			        } 
			        else 
			        {
			            console.log("Http error " + xmlHttp.status + ":" + xmlHttp.statusText);
						$("#confermatoLatoZkDiv").text("false");
			            
			            objectInfoClientSide.hasToken = risposta.hasToken;
			            $("#hasTokenDiv").text("false");	
			            
			            objectInfoClientSide.wantsToken = risposta.wantsToken;
			            $("#wantsTokenDiv").text( "false");
			        }
		    			
		        }
		    }
		    xmlHttp.open("POST", urlDestinazioneHeartbeat, true);  
		    xmlHttp.setRequestHeader("Content-type","application/x-www-form-urlencoded");
		    
		    //lo alleghiamo come parametro alla http post request che mandiamo al server come heartbeat
		    xmlHttp.send("nome="+nomeUtente+"&infoClientSide="+JSON.stringify(objectInfoClientSide));
		   
		}, 1000); 
		
	</script>

</body>
</html>