# Personalizzazioni Avanzate con JAXB

JAXB (Java Architecture for XML Binding) non si limita a un mapping di default, ma offre un ricco set di annotazioni per personalizzare in modo preciso come gli oggetti Java vengono convertiti in XML e viceversa. Questo è fondamentale quando si lavora con schemi XML (XSD) preesistenti o quando si desidera un controllo granulare sull'output XML.

## Annotazioni per la Personalizzazione del Modello

È possibile annotare le classi del modello per definire nomi di elementi, ordine, attributi e molto altro.

### Esempio di Codice: Annotare una Classe `Persona`

```java
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name = "Persona") // Imposta il nome del tag radice a <Persona>
@XmlType(propOrder = { "nome", "eta", "email" }) // Definisce l'ordine degli elementi nel file XML
public class Persona {

    private int id;
    private String nome;
    private int eta;
    private String email; // Questo campo non verrà serializzato
    private String password;

    @XmlAttribute // Mappa l'ID come attributo: <Persona id="123">
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    @XmlElement(name = "NomeCompleto", required = true) // Rinomina il tag e lo rende obbligatorio
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public int getEta() { return eta; }
    public void setEta(int eta) { this.eta = eta; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @XmlTransient // Esclude questo campo dalla serializzazione/deserializzazione
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
```

## Adattatori per Tipi Speciali (`@XmlJavaTypeAdapter`)

A volte, i tipi di dati Java non hanno un mapping diretto e semplice in XML (es. `java.util.Date`, `java.net.URI`). In questi casi, si possono usare degli **adattatori** per definire una logica di conversione personalizzata.

Un adattatore estende `javax.xml.bind.annotation.adapters.XmlAdapter<ValueType, BoundType>`, dove:

- `ValueType` è il tipo di destinazione in XML (es. `String`).
- `BoundType` è il tipo di origine in Java (es. `Date`).

### Esempio di Codice: Adattatore per `Date`

**1. Creare l'adattatore:**

```java
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class DateAdapter extends XmlAdapter<String, Date> {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public String marshal(Date date) throws Exception {
        // Converte da Date a String (formato yyyy-MM-dd)
        return dateFormat.format(date);
    }

    @Override
    public Date unmarshal(String dateString) throws Exception {
        // Converte da String a Date
        return dateFormat.parse(dateString);
    }
}
```

**2. Usare l'adattatore in una classe:**

```java
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Date;

public class Evento {
    private String nome;
    private Date dataEvento;

    @XmlJavaTypeAdapter(DateAdapter.class)
    public Date getDataEvento() { return dataEvento; }
    public void setDataEvento(Date dataEvento) { this.dataEvento = dataEvento; }
    
    // ... altri campi ...
}
```

## Approccio Contract-First con `xjc`

Invece di scrivere le classi Java e generare lo schema (code-first), si può partire da uno schema XSD predefinito e generare le classi Java. Questo approccio, chiamato **contract-first**, è spesso preferito in contesti enterprise perché garantisce l'aderenza a un contratto pre-stabilito.

Lo strumento per fare ciò è `xjc` (XML to Java Compiler), incluso nel JDK.

### Comando `xjc`

```shell
# Genera le classi Java dal file schema.xsd nel package com.example.model
xjc -d src -p com.example.model schema.xsd
```

## Tabella dei Termini e Comandi

| Termine/Comando | Descrizione |
|---|---|
| **`@XmlRootElement`** | Specifica che una classe può essere l'elemento radice di un documento XML. |
| **`@XmlType`** | Personalizza il mapping di una classe a uno schema XML, ad esempio definendo l'ordine degli elementi (`propOrder`). |
| **`@XmlElement`** | Personalizza il mapping di una proprietà Java a un elemento XML (es. nome, obbligatorietà). |
| **`@XmlAttribute`** | Mappa una proprietà Java a un attributo XML invece che a un elemento. |
| **`@XmlTransient`** | Esclude una proprietà dal processo di binding XML. |
| **`@XmlJavaTypeAdapter`** | Applica un adattatore personalizzato a una proprietà per gestire tipi di dati non standard. |
| **`XmlAdapter`** | Classe astratta da estendere per creare un adattatore di tipo personalizzato. |
| **`xjc`** | Strumento a riga di comando (XML to Java Compiler) che genera classi Java a partire da uno schema XSD (approccio contract-first). |
| **Code-First** | Approccio in cui si scrivono prima le classi Java e si lascia che JAXB generi lo schema XML. |
| **Contract-First** | Approccio in cui si definisce prima lo schema XSD ("il contratto") e si generano le classi Java da esso usando `xjc`. |
| **XSD (XML Schema Definition)** | Un linguaggio per definire la struttura, il contenuto e la semantica dei documenti XML. Funge da "contratto" per i dati XML. |
