# Mapping da Java a WSDL con JAX-WS e JAXB

Quando si crea un web service SOAP con JAX-WS, i tipi di dati Java (parametri e valori di ritorno dei metodi) devono essere convertiti in XML per essere inseriti nei messaggi SOAP. Questo processo di mapping è gestito in gran parte da JAXB (Java Architecture for XML Binding).

## Mapping di Tipi di Dati

### Tipi Semplici

Per i tipi di dati primitivi e le classi wrapper comuni (come `String`, `int`, `Integer`, `boolean`, `double`, ecc.), JAX-WS ha un mapping predefinito verso i tipi di schema XML (XSD).

- `String` -> `xsd:string`
- `int`, `Integer` -> `xsd:int`
- `boolean`, `Boolean` -> `xsd:boolean`
- `double`, `Double` -> `xsd:double`

### Tipi Complessi (Oggetti)

Per le classi complesse (i tuoi POJO), JAX-WS si affida a JAXB per la serializzazione e deserializzazione. JAXB può funzionare in due modi:

1. **Convenzione su Configurazione**: Per default, JAXB tenta di mappare una classe Java generando uno schema XML basato sui nomi dei campi o delle proprietà (getter/setter). Perché questo funzioni, la classe deve avere un costruttore senza argomenti.
2. **Annotazioni Esplicite**: Per un controllo più fine, si possono usare le annotazioni JAXB per personalizzare il mapping.

#### Esempio di Codice: Classe Complessa con JAXB

Supponiamo di voler usare un oggetto `Utente` nel nostro web service.

```java
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Classe Utente, annotata per il mapping JAXB.
 */
@XmlRootElement(name = "User") // Definisce il nome dell'elemento radice XML
@XmlType(propOrder = { "id", "nome", "email" }) // Definisce l'ordine degli elementi XML
public class Utente {

    private int id;
    private String nome;
    private String email;
    
    // JAXB richiede un costruttore senza argomenti
    public Utente() {}

    public Utente(int id, String nome, String email) {
        this.id = id;
        this.nome = nome;
        this.email = email;
    }

    // Getter e Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    @XmlElement(name = "FullName", required = true) // Personalizza il nome del tag e lo rende obbligatorio
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
```

### Collezioni

Le collezioni Java (come `List<T>` o `T[]`) vengono generalmente tradotte in una sequenza di elementi XML ripetuti. Ad esempio, `List<Utente>` diventerebbe una serie di tag `<User>`.

## Gestione delle Eccezioni (SOAP Fault)

Se un metodo del web service lancia un'eccezione, questa può essere comunicata al client come un `SOAP Fault`.

- Le eccezioni che estendono `RuntimeException` (unchecked) causano un fault generico.
- Le eccezioni che estendono `Exception` (checked) e sono dichiarate nella clausola `throws` del metodo vengono mappate in un `SOAP Fault` specifico e diventano parte del contratto WSDL.

Per personalizzare il `SOAP Fault`, si può creare una classe di eccezione annotata con `@WebFault`.

#### Esempio di Codice: Eccezione Personalizzata

```java
import javax.xml.ws.WebFault;

/**
 * Eccezione personalizzata per il web service.
 */
@WebFault(name = "UtenteNonTrovatoFault")
public class UtenteNonTrovatoException extends Exception {

    private static final long serialVersionUID = 1L;

    public UtenteNonTrovatoException(String message) {
        super(message);
    }
}
```

#### Esempio di Codice: Metodo che lancia l'eccezione

```java
import javax.jws.WebService;
import javax.jws.WebMethod;

@WebService
public class UserService {

    @WebMethod
    public Utente getUtenteById(int id) throws UtenteNonTrovatoException {
        if (id <= 0) {
            throw new UtenteNonTrovatoException("L'ID utente " + id + " non è valido.");
        }
        // Logica per trovare l'utente...
        return new Utente(id, "Nome Utente", "email@example.com");
    }
}
```

## Tabella dei Termini e Annotazioni JAXB

| Annotazione | Descrizione |
|---|---|
| **`@XmlRootElement`** | Designa una classe come elemento radice di un documento XML. Utile per la classe principale che si vuole serializzare. |
| **`@XmlType`** | Permette di personalizzare il mapping di una classe a uno schema XML, ad esempio definendo l'ordine degli elementi con `propOrder`. |
| **`@XmlElement`** | Applica a un campo o a un metodo getter/setter per personalizzare il tag XML corrispondente (es. nome, obbligatorietà). |
| **`@XmlAttribute`** | Mappa un campo o una proprietà come un attributo XML invece che come un elemento. |
| **`@XmlTransient`** | Esclude un campo o una proprietà dalla serializzazione XML. |
| **`@XmlAccessorType`** | Definisce se JAXB deve accedere ai campi (`FIELD`), alle proprietà (`PROPERTY`), o a nessuno (`NONE`) per il binding. |
| **`@WebFault`** | Annotazione usata su una classe di eccezione per personalizzare la rappresentazione del `SOAP Fault` nel WSDL. |
