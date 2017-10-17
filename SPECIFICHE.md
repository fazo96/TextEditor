# Specifiche Tecniche TextEditor Java

### Riassunto delle tecnologie e tecniche utilizzate

- __Java__ per il codice sorgente
- __Swing__ per l'interfaccia grafica
- __Stringhe__ serializzate da Java su __TCP__ per il protocollo di rete 
(usando le funzionalità Socket e ServerSocket di Java)
- __MD5__ come hashing per identificare lo stato del documento
- Porta di default _1234_ ma personalizzabile dall'utente 
nell'interfaccia di hosting del server

### Funzionalità

Il programma è simile al blocco note di Windows (editing base del testo, 
salvataggio e caricamento di file) con aggiunta la possibilità di 
trasformare un'istanza del programma in un server in grado di accettare 
connessioni da altre istanze. L'istanza server rimane in sola lettura 
mentre le altre possono alterare il documento in tempo reale, che viene 
tenuto in sincronia tra i client dal server.

### Protocollo di comunicazione

Il software fa uso di connessioni TCP sulle quali vengono scambiate 
stringhe serializzate da Java.

Il protocollo supporta due messaggi:

- __SYNC__ che ha come parametro la stringa del testo del documento e 
serve come prima sincronizzazione per i nuovi client. Viene usata anche 
in altri due casi:
    - conflitto tra operazioni: se il server non riesce a ribasare due 
operazioni che arrivano contemporaneamente da due client diversi, una 
delle due "vince" e il client che ha inviato l'altra riceve una SYNC
    - errore nell'applicazione di un aggiornamento dal server: se il 
client per qualche motivo non riesce a rimanere aggiornato con i 
messaggi del server, invia una richiesta di SYNC
- __SYNCREQ__ rappresenta una richiesta di SYNC e non ha parametri
- __Operazione__: una operazione rappresenta una modifica al documento 
effettuata da un client. Ha il seguente formato: 
`HASH|OPERAZIONE|ARGOMENTI` dove l'operazione può essere `ADD` o `DEL` 
(aggiunta o cancellazione).
    - __ADD__ ha come argomenti la posizione in cui c'è stata una 
aggiunta e il testo da aggiungere, separati dal simbolo `|`
    - __DEL__ ha come argomenti le due posizioni tra le quali va 
cancellato il testo
    - __HASH__ rappresenta l'HASH MD5 dell'operazione sulla quale va 
applicata questa. Serve a capire se l'operazione è valida

### Message flow

Ogni __client__ riceve operazioni da due sorgenti: l'utente che modifica 
il documento e gli aggiornamenti del server. Le modifiche dell'utente 
sono prima convertite in operazioni, poi applicate localmente ed inviate 
al server. Gli aggiornamenti del server sono invece semplicemente 
applicati localmente. In caso di SYNC, lo stato locale viene azzerato e 
viene creato un nuovo documento dal messaggio di SYNC.

Il __server__ riceve operazioni da tutti gli utenti e cerca di 
applicarle nell'ordine in cui sono state ricevute. Risponde ai SYNC 
inviando lo stato attuale del documento. In caso di errori, 
semplicemente non applica l'operazione e invia una SYNC al client che 
l'ha inviata: saranno gli altri client, in caso necessario, a inviare 
richiesta di SYNC.
