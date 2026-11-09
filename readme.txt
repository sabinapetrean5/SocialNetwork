MAP_ToySocialNetwork - Laboratorul 4
Metode Avansate de Programare - 2026-2027

1. Scopul laboratorului

Laboratorul 4 continua proiectul de retea de socializare realizat in laboratoarele anterioare.

Scopul este refactorizarea codului din laboratorul 3 folosind facilitatile Java: interfete functionale, expresii lambda, referinte la metode, metode default in interfete, Optional si Stream API.

Functionalitatile existente sunt pastrate:
- Adaugarea si stergerea utilizatorilor.
- Adaugarea si stergerea prieteniilor.
- Determinarea numarului de comunitati.
- Identificarea celei mai sociabile comunitati pe baza diametrului grafului.

Nu au fost introduse functionalitati de business noi. Modificarile urmaresc inlocuirea buclelor clasice de parcurgere a colectiilor cu operatii functionale, pastrand comportamentul aplicatiei.


2. Refactorizarea buclelor clasice folosind Stream API

2.1. ConsoleUI - displayUsers()

Laboratorul 3:

Utilizatorii erau parcursi folosind o bucla for-each. Pentru fiecare utilizator se construia si se afisa un mesaj care continea ID-ul si informatiile acestuia.

Laboratorul 4:

Bucla a fost inlocuita cu un stream obtinut din Iterable prin StreamSupport.stream().

Operatia map() transforma fiecare utilizator intr-un sir de caractere care contine ID-ul si reprezentarea utilizatorului.

Rezultatele sunt colectate intr-o lista folosind collect(Collectors.toList()), apoi afisate prin forEach(System.out::println).

Operatii Stream utilizate:
- map() - operatie intermediara.
- collect() - operatie terminala.


2.2. ConsoleUI - displayFriendships()

Laboratorul 3:

Prieteniile erau parcurse cu o bucla for-each. Pentru fiecare prietenie se afisau ID-urile utilizatorilor si informatiile relatiei.

Laboratorul 4:

Parcurgerea a fost inlocuita cu StreamSupport.stream().

Prin map(), fiecare prietenie este transformata intr-un sir de caractere. Rezultatele sunt colectate intr-o lista folosind collect(Collectors.toList()) si afisate printr-o referinta la metoda println().

Operatii Stream utilizate:
- map() - operatie intermediara.
- collect() - operatie terminala.


2.3. FriendshipService - removeFriendshipsOfUser()

Laboratorul 3:

Prieteniile erau parcurse folosind o bucla for-each pentru identificarea celor in care participa utilizatorul care urma sa fie sters.

Prieteniile identificate erau memorate si apoi eliminate din repository.

Laboratorul 4:

Parcurgerea a fost inlocuita cu un pipeline Stream.

filter() selecteaza prieteniile in care unul dintre cei doi utilizatori are ID-ul cautat.

map(Friendship::getId) extrage identificatorul fiecarei prietenii selectate.

collect(Collectors.toList()) construieste lista identificatorilor care trebuie eliminati.

Ulterior, lista este parcursa folosind forEach(friendshipRepository::delete).

Colectarea identificatorilor inainte de stergere evita modificarea repository-ului in timpul parcurgerii stream-ului initial.

Operatii Stream utilizate:
- filter() - operatie intermediara.
- map() - operatie intermediara.
- collect() - operatie terminala.


2.4. AbstractFileRepository - loadData()

Laboratorul 3:

Fisierele text erau citite folosind BufferedReader si o bucla while, care prelucra succesiv liniile fisierului.

Fiecare linie era convertita intr-o entitate, iar liniile invalide erau raportate impreuna cu numarul liniei si motivul erorii.

Laboratorul 4:

Citirea se realizeaza folosind Files.lines(), care furnizeaza un Stream de siruri de caractere.

Stream-ul este deschis intr-un bloc try-with-resources, pentru a asigura inchiderea fisierului.

Liniile sunt colectate intr-o lista, iar IntStream.range(0, fileLines.size()) genereaza indicii necesari prelucrarii lor.

Aceasta abordare permite pastrarea numerotarii liniilor, inclusiv pentru raportarea celor corupte.

Operatii utilizate:
- Files.lines() - creeaza stream-ul de linii.
- collect() - operatie terminala.
- IntStream.range() - genereaza indicii.
- filter() - operatie intermediara.
- forEach() - operatie terminala.


2.5. AbstractFileRepository - writeAllToFile()

Laboratorul 3:

Entitatile erau parcurse folosind o bucla for-each si convertite in siruri de caractere pentru scrierea in fisier cu BufferedWriter.

Laboratorul 4:

Entitatile sunt prelucrate prin entities.values().stream().

map(this::createEntityAsString) transforma fiecare entitate in reprezentarea sa textuala.

collect(Collectors.toList()) construieste lista liniilor care trebuie scrise.

Scrierea se realizeaza prin Files.write(), folosind codificarea UTF-8.

Operatii Stream utilizate:
- map() - operatie intermediara.
- collect() - operatie terminala.


2.6. CommunityService - buildGraph()

Laboratorul 3:

Graful retelei sociale era construit folosind doua bucle for-each.

Prima bucla parcurgea utilizatorii si initializa pentru fiecare o multime de vecini.

A doua bucla parcurgea prieteniile si adauga muchiile in ambele sensuri, deoarece graful este neorientat.

Laboratorul 4:

Construirea grafului se realizeaza folosind stream-uri si Collectors.toMap().

map(User::getId) extrage ID-urile utilizatorilor.

Pentru fiecare utilizator, stream-ul prieteniilor este filtrat cu filter() pentru a identifica relatiile in care acesta participa.

Prin map() se obtine ID-ul celuilalt utilizator din fiecare prietenie.

Vecinii sunt colectati intr-un LinkedHashSet folosind Collectors.toCollection(LinkedHashSet::new).

Harta de adiacenta este construita folosind Collectors.toMap(), cu Function.identity() pentru chei.

Operatii Stream utilizate:
- map() - operatie intermediara.
- filter() - operatie intermediara.
- collect() - operatie terminala.


2.7. CommunityService - calculateDiameter()

Laboratorul 3:

Diametrul unei comunitati era calculat prin parcurgerea utilizatorilor din componenta conexa.

Pentru fiecare utilizator se efectua o parcurgere BFS, folosind o coada si bucle while si for-each.

Se determinau distantele minime fata de ceilalti utilizatori, iar diametrul reprezenta distanta maxima obtinuta.

Laboratorul 4:

Parcurgerea iterativa a fost inlocuita cu o explorare recursiva pe niveluri, implementata in metoda exploreDistances().

Pentru fiecare utilizator de pornire, mapToInt() calculeaza cea mai mare distanta minima pana la ceilalti utilizatori din componenta.

In exploreDistances(), flatMap() reuneste vecinii utilizatorilor aflati pe nivelul curent, iar filter() selecteaza vecinii care apartin componentei si nu au fost inca vizitati.

collect() construieste urmatorul nivel de explorare.

max() determina cea mai mare distanta, iar apoi diametrul componentei.

Operatii Stream utilizate:
- mapToInt() - operatie intermediara.
- flatMap() - operatie intermediara.
- filter() - operatie intermediara.
- collect() - operatie terminala.
- max() - operatie terminala.


2.8. CommunityService - getMostSociableCommunity()

Laboratorul 3:

Comunitatile erau parcurse folosind o bucla for-each.

Pentru fiecare comunitate se calcula diametrul, iar comunitatea cu diametrul maxim era retinuta.

Ulterior, o alta bucla for-each transforma ID-urile utilizatorilor din comunitatea selectata in obiecte User.

Laboratorul 4:

Comunitatile sunt prelucrate prin components.stream().

max(Comparator.comparingInt(...)) selecteaza comunitatea cu diametrul cel mai mare.

Rezultatul este tratat folosind Optional si orElseGet().

Apoi, map(userService::findUserById) cauta utilizatorii corespunzatori identificatorilor.

flatMap(Optional::stream) elimina rezultatele fara utilizator si extrage valorile existente.

collect(Collectors.toList()) construieste lista finala.

Operatii Stream utilizate:
- max() - operatie terminala.
- map() - operatie intermediara.
- flatMap() - operatie intermediara.
- collect() - operatie terminala.


3. Interfete functionale si expresii lambda

3.1. Validator<T>

Interfata Validator<T> este adnotata cu @FunctionalInterface si contine o singura metoda abstracta de validare.

A fost adaugata metoda default andThen(Validator<T> other), care permite compunerea a doua reguli de validare.

In Main.java a fost definita o regula suplimentara prin expresie lambda, nameNoDigitsValidator.

Aceasta verifica daca prenumele sau numele utilizatorului contine cifre, folosind chars() si anyMatch(Character::isDigit).

Daca este identificata o cifra, se arunca ValidationException.

Noua regula este combinata cu UserValidator prin andThen(), fara crearea unei clase suplimentare si fara modificarea validatorului existent.


3.2. Predicate<T>

Interfata Repository<ID, E> contine metoda default findAll(Predicate<E> filter).

Aceasta permite filtrarea entitatilor pe baza unui criteriu transmis ca parametru.

In UserCriteria.java, metodele statice hasFirstName() si hasLastName() intorc Predicate<User>.

In UserSearchDemo.java sunt demonstrate combinatiile:
- and() - ambele conditii trebuie indeplinite.
- or() - cel putin una dintre conditii trebuie indeplinita.
- negate() - inverseaza rezultatul unui predicat.
- Predicate.not() - neaga un predicat.

Un criteriu nou poate fi definit direct prin lambda, de exemplu:

Predicate<User> startsWithA = user -> user.getFirstName().startsWith("A");

Acesta poate fi transmis metodei findAll(), fara modificarea repository-ului si fara crearea unei clase noi.


3.3. Function<T, R>

RepositoryFactory utilizeaza Function pentru asocierea fiecarui RepositoryType cu o functie care construieste repository-ul corespunzator.

Functiile primesc validatorul si intorc un Repository.

Sunt definite mecanisme separate pentru repository-urile utilizatorilor si pentru cele ale prieteniilor.

Este executata doar functia corespunzatoare tipului de persistenta selectat.

Function.identity() este utilizat si in CommunityService.buildGraph(), pentru construirea cheilor hartii de adiacenta.


3.4. Comparator<T>

Comparator.comparingInt() este folosit in CommunityService pentru compararea comunitatilor dupa diametru.

Comunitatea cu diametrul maxim este selectata prin max().

Se evita implementarea manuala a comparatiei prin scadere.


4. Referinte la metode

In laboratorul 4 sunt utilizate cele patru categorii de referinte la metode:

1. Referinta la metoda statica: Character::isDigit, in Main.java, pentru identificarea cifrelor din numele utilizatorului.

2. Referinta la metoda unui obiect existent: System.out::println, pentru afisarea rezultatelor in ConsoleUI si UserSearchDemo.

3. Referinta la metoda de instanta a unui obiect oarecare: Friendship::getId, in FriendshipService, pentru extragerea ID-urilor prieteniilor.

4. Referinta la constructor: InMemoryRepository::new, in RepositoryFactory, si LinkedHashSet::new, in CommunityService.

Referintele la metode inlocuiesc expresiile lambda care ar apela direct o metoda sau un constructor existent, reducand codul repetitiv.


5. Alegerea pentru Optional in Repository

S-a decis pastrarea contractului existent al interfetei Repository.

Metoda findOne(ID) intoarce entitatea identificata sau null daca aceasta nu exista.

Aceasta alegere mentine compatibilitatea cu implementarile repository-urilor din laboratorul 3 si evita modificarea tuturor claselor care utilizeaza interfata.

La nivelul service-ului, UserService.findUserById() foloseste Optional.ofNullable() pentru a transforma rezultatul cautarii intr-un Optional<User>.

Optional este utilizat si pentru tratarea rezultatelor operatiilor care pot sa nu gaseasca o valoare.

Sunt folosite metode precum orElseThrow(), orElse(), orElseGet() si Optional::stream.

Nu se utilizeaza Optional.get() fara verificarea prealabila a existentei valorii.


6. Alegerea Template Method vs. Function

Pentru repository-urile care lucreaza cu fisiere text s-a pastrat sablonul de proiectare Template Method din laboratorul 3.

AbstractFileRepository contine logica comuna pentru incarcarea si rescrierea datelor.

UserFileRepository si FriendshipFileRepository furnizeaza implementarile specifice pentru conversia dintre liniile fisierelor si entitatile aplicatiei, prin metodele extractEntity() si createEntityAsString().

Aceasta alegere a fost pastrata deoarece:
- Evita duplicarea codului de citire si scriere.
- Separa operatiile comune de conversiile specifice entitatilor.
- Permite reutilizarea structurii deja existente.
- Nu necesita modificarea arhitecturii de persistenta.

Alternativa ar fi utilizarea unui FileRepository generic care primeste doua functii, Function<String, E> si Function<E, String>, pentru conversia datelor.

Pentru acest proiect, Template Method ofera deja separarea necesara intre algoritmul comun si comportamentul specific fiecarei entitati.


7. Persistenta datelor si configurarea

Aplicatia pastreaza suportul pentru trei tipuri de repository:
- MEMORY - stocarea datelor in memorie.
- FILE - stocarea datelor in fisiere text.
- DATABASE - implementare rezervata laboratorului urmator.

Configurarea se realizeaza prin fisierul:

src/main/resources/config.properties

In configuratia actuala, tipul de repository este FILE.

Fisierele utilizate sunt:
- data/users.txt
- data/friendships.txt

Formatul utilizatorilor este:

id;prenume;nume

Formatul prieteniilor este:

idUtilizator1;idUtilizator2;dataPrieteniei

Data prieteniei este reprezentata prin LocalDateTime, in format ISO.

Formatul inregistrarilor nu a fost modificat in laboratorul 4.


7.1. Citirea si scrierea fisierelor

AbstractFileRepository utilizeaza Files.lines() pentru citirea datelor si Files.write() pentru scriere.

Citirea se efectueaza intr-un bloc try-with-resources, deoarece stream-ul asociat fisierului trebuie inchis dupa utilizare.

Pentru scriere, entitatile sunt convertite in linii text folosind Stream API.

Se utilizeaza codificarea UTF-8.


7.2. Tratarea fisierelor lipsa si a liniilor corupte

Daca fisierul de date nu exista, repository-ul porneste fara inregistrari.

Fisierul si directoarele necesare sunt create la prima scriere.

Liniile invalide sunt raportate in System.err, impreuna cu numarul liniei si motivul erorii.

Liniile corupte sunt ignorate, iar incarcarea continua cu cele valide.

Pentru demonstrarea acestui comportament sunt pastrate fisierele:
- data/test_linii_corupte.txt
- data/test_prietenii_corupte.txt

Exceptiile de I/O sunt tratate prin RepositoryException, pastrand cauza originala prin exception chaining.


8. RepositoryFactory

In laboratorul 3, RepositoryFactory utiliza un switch clasic pentru alegerea implementarii repository-ului.

In laboratorul 4, switch-ul a fost inlocuit cu un Map care asociaza fiecare RepositoryType cu o functie de creare.

Sunt utilizate functii de tip Function, care primesc validatorul si construiesc repository-ul corespunzator.

Aceasta abordare permite selectarea implementarii prin configuratie si centralizeaza logica de creare a obiectelor.

Sunt tratate separat repository-urile utilizatorilor si cele ale prieteniilor.

Clasele UserDbRepository si FriendshipDbRepository raman schelete si arunca UnsupportedOperationException.

Nu a fost implementata persistenta JDBC/SQL, aceasta fiind rezervata laboratorului 5.


9. Algoritmii pentru comunitati

CommunityService reprezinta reteaua sociala printr-un graf neorientat.

Fiecare utilizator reprezinta un nod, iar fiecare prietenie reprezinta o muchie.

In laboratorul 3, componentele conexe erau identificate prin BFS, folosind cozi si bucle clasice.

In laboratorul 4, identificarea componentelor se realizeaza prin metode recursive si operatii pe colectii si stream-uri.

Metoda findComponent() determina componenta conexa a unui utilizator, iar findAllComponents() obtine componentele distincte.

Numarul comunitatilor este determinat prin dimensiunea listei componentelor conexe.

Cea mai sociabila comunitate este definita ca acea componenta conexa care are diametrul maxim.

Diametrul reprezinta cea mai mare distanta minima dintre doua noduri ale componentei.

Pentru determinarea sa, exploreDistances() realizeaza explorarea recursiva pe niveluri, iar calculateDiameter() utilizeaza mapToInt() si max().

Selectarea comunitatii cu diametrul maxim se realizeaza prin max(Comparator.comparingInt(...)).


10. Bucle clasice pastrate si justificare

In ConsoleUI.run() a fost pastrata bucla:

while (running)

Aceasta este necesara pentru repetarea meniului aplicatiei pana cand utilizatorul selecteaza optiunea de iesire.

Bucla nu este utilizata pentru parcurgerea unei colectii, ci pentru controlul interactiunii cu utilizatorul.

Prin urmare, nu incalca restrictia laboratorului 4.

In pachetele domain, service, repository si ui nu sunt pastrate bucle clasice pentru parcurgerea colectiilor.

In algoritmii de graf, parcurgerile iterative au fost inlocuite cu recursivitate si operatii functionale.


11. Restrictii de implementare

In cadrul refactorizarii au fost respectate urmatoarele restrictii:

- Nu se folosesc bucle clasice pentru parcurgerea colectiilor in domain, service, repository si UI.
- Nu se folosesc clase anonime acolo unde sunt suficiente expresiile lambda sau referintele la metode.
- Nu se folosesc biblioteci externe pentru prelucrarea colectiilor.
- Nu se utilizeaza parallelStream().
- Nu se modifica formatul fisierelor de date.
- Nu se implementeaza inca persistenta in baza de date.
- Se pastreaza tratarea fisierelor lipsa, a liniilor corupte si a exceptiilor.
- Codul nou este documentat prin Javadoc in limba engleza.


12. Demonstrarea functionalitatilor la predare

La predare se pot demonstra urmatoarele:

1. Rularea aplicatiei si executarea operatiilor de adaugare si stergere a utilizatorilor si prieteniilor.

2. Determinarea numarului de comunitati si identificarea celei mai sociabile comunitati.

3. Compararea implementarilor din laboratorul 3 cu cele din laboratorul 4 pentru metodele displayUsers(), removeFriendshipsOfUser() si buildGraph().

4. Explicarea operatiilor intermediare, precum filter(), map() si flatMap(), si a operatiilor terminale, precum collect() si max().

5. Demonstrarea regulii de validare nameNoDigitsValidator, definita prin lambda si compusa cu validatorul existent prin andThen().

6. Adaugarea unui criteriu nou de cautare prin Predicate<User>, fara modificarea repository-ului si fara crearea unei clase noi.

7. Verificarea comportamentului aplicatiei in cazul fisierelor lipsa si al liniilor corupte.


13. Rularea proiectului si livrabile

Proiectul este realizat in IntelliJ IDEA, fara Gradle.

Pentru rulare se deschide directorul proiectului in IntelliJ IDEA, se configureaza un JDK compatibil si se executa clasa Main.

Este necesara cel putin versiunea Java 11, deoarece sunt utilizate Optional.stream() si Predicate.not().

Livrabilele includ:
- Codul sursa Java.
- Fisierul config.properties.
- Directorul data/, inclusiv fisierele de test cu linii corupte.
- Fisierul readme.txt.
- Versionarea proiectului folosind Git.

Laboratorul 4 pastreaza functionalitatile proiectului anterior si introduce o implementare bazata pe interfete functionale, expresii lambda, referinte la metode, Optional si Stream API.