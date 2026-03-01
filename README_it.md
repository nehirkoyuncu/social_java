# Social Network

(la versione Inglese è disponibile nel file [`README.md`](README.md)).

Sviluppare un'applicazione per supportare un social network. Tutte le
classi si devono trovare nel package `social`.

Il sistema deve utilizzare Hibernate ORM per garantire la persistenza delle informazioni.
Per consentire il test, è necessario usare la classe `JPAUtil` per ottenere gli oggetti 
`EntityManager` tramite il metodo `getEntityManager()`.


## R1 - Sottoscrizione

L'interazione con il sistema avviene tramite la classe facade `Social`.

È possibile registrare un nuovo account tramite il metodo
`addPerson()` che accetta come parametri un codice univoco, nome e
cognome.

Il metodo lancia un'eccezione di `PersonExistsException` se il codice
univoco è già associato a un account.

Il metodo `getPerson()` restituisce una stringa contenente codice, nome
e cognome della persona, separati da spazi. Se il codice passato come
parametro non corrisponde a nessun account, il metodo lancia
un'eccezione di `NoSuchCodeException`.

**💡 Suggerimento**:

- usare la classe `Person` (già fornita) per rappresentare le persone
- usare il pattern *repository* (già fornito)
    - una classe `PersonRepository` che fornisce le operazioni ORM base
    - un oggetto `personRepository` dentro classe facade che incapsula la collezione if oggetti persona


## R2 - Amicizia

Una persona, registrata sul social network, può aggiungere degli amici.
L'amicizia è bi-direzionale: se la persona Tizio è amico della persona
Caio, questo significa che la persona Caio è amico di Tizio.

L'amicizia viene stabilita con il metodo `addFriendship()` che
accetta come parametri il codice di entrambe le persone. Il metodo
lancia un'eccezione di `NoSuchCodeException` se almeno uno dei due
codici non esiste.

Il metodo `listOfFriends()` riceve come parametro il codice di una
persona e restituisce la collezione dei suoi amici. Viene lanciata una
`NoSuchCodeException` se il codice non esiste.

Se la persona non ha amici viene restituita una collezione vuota.

## R3 - Gruppi

È possibile registrare un gruppo tramite il metodo `addGroup()`. Il nome del gruppo deve essere unico. Viene lanciata una `GroupExistsException` se il nome del gruppo esiste già.

Il metodo `updateGroupName()` permette di modificare il nome di un gruppo esistente. 
Riceve come parametro il nome attuale del gruppo e il nuovo nome desiderato. 
Viene lanciata una `GroupExistsException` se il nuovo nome del gruppo esiste già, viene lanciata una `NoSuchCodeException` se il nome attuale del gruppo non esiste.

Il metodo `deleteGroup()` permette di cancellare un gruppo esistente. 
Riceve come parametro il nome del gruppo. 
Viene lanciata una `NoSuchCodeException` se il nome del gruppo non esiste.

Il metodo `listOfGroups()` restituisce la lista dei nomi di tutti i gruppi registrati o la collezione vuota se non ce ne sono.

Una persona può essere iscritta a un gruppo tramite il metodo
`addPersonToGroup()` che riceve come parametri il codice della persona
e il nome del gruppo. Viene lanciata una `NoSuchCodeException` se il
codice della persona o il nome del gruppo non esiste.

Quindi il metodo `listOfPeopleInGroup()` restituisce la collezione dei
codici delle persone iscritte al gruppo dato. Restituisce una collezione vuota se il gruppo non esiste.

## R4 - Statistiche

Il metodo `personWithLargestNumberOfFriends()` restituisce il codice
della persona che ha il maggior numero di amici (di primo livello). Si
supponga che non esistano casi di parità.

Il metodo `largestGroup()` restituisce il nome del gruppo con il
maggior numero di membri. Si supponga che non esistano casi di parità.

## R5 - Post

È possibile aggiungere un nuovo post da un dato account utilizzando il metodo `post()` che accetta come argomenti il codice unico della persona che posta e il contenuto del testo. 
Il metodo restituisce un id univoco per il post formato esclusivamente da lettere e numeri.

Dato l'id del post è possibile ottenere:

- il timestamp con `getTimestamp()`,
- il contenuto del testo con `getPostContent()`.

Il timestamp del post è l'ora corrente del sistema al momento della creazione del post (recuperato tramite `System.currentTimeMillis()`).

La lista paginata di tutti i post di _un dato utente_, esclusi i post dell'utente stesso, può essere recuperata utilizzando il metodo `getPaginatedUserPosts()` che accetta l'id dell'utente, il numero della pagina (1 è la prima) e la lunghezza della pagina. Il metodo restituisce gli id dei post ordinati per timestamp decrescente. La lista è divisa in pagine, ciascuna contenente un numero di post specificato dalla lunghezza della pagina. Ad esempio, se la lunghezza della pagina è 5 e la pagina è 2, vengono restituiti i post con posizione da 6 a 10. I post sono ordinati per timestamp decrescente, cioè i più recenti per primi.

La lista paginata di tutti i post degli _amici di un dato utente_ può essere recuperata utilizzando il metodo `getPaginatedFriendPosts()` che accetta l'id dell'utente, il numero della pagina (1 è la prima) e la lunghezza della pagina. Il metodo funziona come il precedente ma restituisce il codice dell'autore e l'id del post separati da `":"`, es. `"elon:123wtf"`.
