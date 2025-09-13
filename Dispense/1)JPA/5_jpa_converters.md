# Convertitori di Attributi (Attribute Converters) in JPA 2.1

JPA 2.1 ha introdotto una potente funzionalità chiamata **Attribute Converters**, che permette di definire una logica di conversione personalizzata per gli attributi di un'entità. Questo consente di mappare tipi di dati complessi o non standard del modello a oggetti su tipi di dati supportati dal database (e viceversa).

## A Cosa Servono i Converter?

I converter sono utili in molti scenari, tra cui:

- **Mappare tipi non supportati**: Memorizzare un tipo custom (es. `java.time.YearMonth`) in una colonna standard del DB (es. `VARCHAR` o `DATE`).
- **Trasformare dati**: Salvare una lista di stringhe (`List<String>`) in un'unica colonna di testo, separando i valori con una virgola.
- **Cifratura/Decifratura**: Cifrare automaticamente i dati di un campo prima di salvarli nel database e decifrarli quando vengono letti.
- **Compressione**: Comprimere dati di grandi dimensioni prima della persistenza e decomprimerli durante la lettura.

## Come si crea un Attribute Converter

Creare un converter è semplice e richiede due passaggi:

1. **Implementare l'interfaccia `AttributeConverter<X, Y>`**:
    - `X`: Il tipo dell'attributo nell'entità Java.
    - `Y`: Il tipo della colonna nel database.
    - Si devono implementare due metodi:
        - `convertToDatabaseColumn(X attribute)`: Converte il tipo dell'entità nel tipo del database.
        - `convertToEntityAttribute(Y dbData)`: Converte il tipo del database nel tipo dell'entità.

2. **Annotare la classe con `@Converter`**:
    - Questa annotazione registra il converter con il provider JPA.
    - L'attributo `autoApply = true` può essere usato per applicare il converter automaticamente a tutti gli attributi del tipo specificato, senza doverlo dichiarare esplicitamente su ogni campo.

### Esempio 1: Salvare una `List<String>` come Stringa

Supponiamo di voler salvare una lista di tag per un prodotto in un'unica colonna `VARCHAR`, separando i tag con una virgola.

**1. Creazione del Converter:**

```java
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {

    private static final String SEPARATOR = ",";

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        // Converte la lista in una stringa (es. "tag1,tag2,tag3")
        return attribute == null || attribute.isEmpty() ? null : String.join(SEPARATOR, attribute);
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        // Converte la stringa del DB di nuovo in una lista
        if (dbData == null || dbData.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(dbData.split(SEPARATOR));
    }
}
```

**2. Applicazione del Converter all'Entità:**
Per applicare il converter a un campo specifico, si usa l'annotazione `@Convert`.

```java
@Entity
public class Prodotto {

    @Id
    @GeneratedValue
    private Long id;

    private String nome;

    @Convert(converter = StringListConverter.class)
    @Column(name = "tags") // La colonna nel DB sarà di tipo VARCHAR o TEXT
    private List<String> tagList;

    // Getters e Setters...
}
```

Quando si salva o si legge un'entità `Prodotto`, JPA invocherà automaticamente il `StringListConverter` per gestire l'attributo `tagList`.

### Esempio 2: Converter con `autoApply = true`

Immaginiamo di voler gestire sempre il tipo `java.net.URL` come una `String` nel database, senza dover specificare `@Convert` ogni volta.

```java
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.net.MalformedURLException;
import java.net.URL;

@Converter(autoApply = true) // Applica automaticamente a tutti i campi di tipo URL
public class UrlConverter implements AttributeConverter<URL, String> {

    @Override
    public String convertToDatabaseColumn(URL attribute) {
        return attribute == null ? null : attribute.toExternalForm();
    }

    @Override
    public URL convertToEntityAttribute(String dbData) {
        try {
            return dbData == null ? null : new URL(dbData);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("URL non valida nel database", e);
        }
    }
}
```

Ora, in qualsiasi entità, un campo di tipo `URL` verrà automaticamente convertito.

```java
@Entity
public class SitoWeb {
    // ...
    private URL indirizzoHomepage; // Non serve @Convert, viene applicato in automatico
}
```

## Lista dei Comandi e API

| Elemento | Tipo | Descrizione |
|---|---|---|
| `@Converter` | Annotazione | Marca una classe come un Attribute Converter. Può avere l'attributo `autoApply` per l'applicazione globale. |
| `AttributeConverter<X, Y>` | Interfaccia | L'interfaccia da implementare per creare un converter. `X` è il tipo dell'entità, `Y` è il tipo del database. |
| `convertToDatabaseColumn(X)` | Metodo | Metodo dell'interfaccia che definisce la logica di conversione dal tipo dell'entità a quello del database. |
| `convertToEntityAttribute(Y)` | Metodo | Metodo dell'interfaccia che definisce la logica di conversione dal tipo del database a quello dell'entità. |
| `@Convert` | Annotazione | Applica esplicitamente un converter a un campo di un'entità. Si usa quando `autoApply` è `false` o per sovrascrivere un converter globale. |
