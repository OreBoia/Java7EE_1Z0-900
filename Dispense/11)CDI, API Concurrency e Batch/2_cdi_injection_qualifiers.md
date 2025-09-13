# Iniezione e Qualificatori in CDI

Contexts and Dependency Injection (CDI) è una specifica fondamentale in Java EE che fornisce un potente framework per l'inversione di controllo (IoC) e la dependency injection. Uno dei meccanismi centrali di CDI è l'iniezione di dipendenze, che viene ulteriormente potenziata attraverso l'uso dei qualificatori.

## Iniezione con `@Inject`

L'annotazione `@Inject` è il modo standard in CDI per richiedere che una dipendenza venga iniettata in un bean. Quando il container CDI istanzia un bean, cerca un'implementazione appropriata per ogni punto di iniezione marcato con `@Inject` e la fornisce automaticamente.

Se esiste una sola implementazione del tipo richiesto, il meccanismo è semplice e diretto. Tuttavia, sorgono problemi di ambiguità quando più implementazioni sono disponibili per la stessa interfaccia.

## Risolvere l'Ambiguità con i Qualificatori

I qualificatori (qualifiers) sono annotazioni personalizzate che servono a distinguere diverse implementazioni di uno stesso tipo di bean. Questo permette al container CDI di risolvere le ambiguità e iniettare l'implementazione corretta richiesta dal client.

Per usare un qualificatore, si seguono questi passaggi:

1. **Definire l'annotazione del qualificatore**: Si crea una nuova annotazione e la si annota con `@Qualifier`.
2. **Annotare le implementazioni**: Si applica l'annotazione del qualificatore alle diverse implementazioni del bean per differenziarle.
3. **Usare il qualificatore nel punto di iniezione**: Si usa l'annotazione del qualificatore insieme a `@Inject` per specificare quale implementazione si desidera iniettare.

### Esempi di Codice

Vediamo un esempio pratico con un servizio di notifica.

**1. Definizione dell'interfaccia**

```java
public interface NotificationService {
    String send(String message);
}
```

**2. Creazione dei qualificatori**

Definiamo due qualificatori, `@Email` e `@SMS`, per le diverse modalità di notifica.

```java
import javax.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Qualifier
@Retention(RUNTIME)
@Target({FIELD, TYPE, METHOD})
public @interface Email {}
```

```java
import javax.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Qualifier
@Retention(RUNTIME)
@Target({FIELD, TYPE, METHOD})
public @interface SMS {}
```

**3. Implementazioni del servizio**

Creiamo due implementazioni del servizio di notifica, ognuna annotata con il rispettivo qualificatore.

```java
@Email
public class EmailNotificationService implements NotificationService {
    @Override
    public String send(String message) {
        return "Email inviata: " + message;
    }
}
```

```java
@SMS
public class SmsNotificationService implements NotificationService {
    @Override
    public String send(String message) {
        return "SMS inviato: " + message;
    }
}
```

**4. Iniezione con qualificatori**

Ora possiamo iniettare l'implementazione desiderata in un altro bean.

```java
import javax.inject.Inject;

public class NotificationManager {

    @Inject
    @Email
    private NotificationService emailService;

    @Inject
    @SMS
    private NotificationService smsService;

    public void sendNotifications(String message) {
        System.out.println(emailService.send(message));
        System.out.println(smsService.send(message));
    }
}
```

## Ottenere Bean Programmaticamente

Oltre all'iniezione, CDI permette di ottenere istanze di bean in modo programmatico. Questo può essere utile in scenari dove non è possibile usare l'iniezione diretta.

L'API `CDI.current()` fornisce un punto di accesso al container.

```java
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.util.AnnotationLiteral;

public class ProgrammaticLookupExample {

    public void sendUrgentNotification(String message) {
        // Ottiene un'istanza del servizio SMS
        NotificationService service = CDI.current()
                                         .select(NotificationService.class, new AnnotationLiteral<SMS>(){})
                                         .get();
        System.out.println(service.send(message));
    }
}
```

## Tabella dei Termini e Comandi Principali

| Termine/Comando | Descrizione |
| --- | --- |
| `@Inject` | Annotazione standard per richiedere l'iniezione di una dipendenza. |
| `@Qualifier` | Meta-annotazione usata per creare annotazioni di qualificazione personalizzate. |
| `@Named` | Un qualificatore predefinito che associa un nome a un bean, utile per l'integrazione con EL (Expression Language). |
| `@Default` | Qualificatore di default applicato a tutti i bean che non specificano esplicitamente un altro qualificatore. |
| `@Any` | Qualificatore speciale che permette di iniettare tutte le implementazioni di un'interfaccia. |
| `CDI.current()` | Fornisce l'accesso programmatico al container CDI per ottenere istanze di bean. |
| `select()` | Metodo dell'API programmatica per selezionare un bean in base al tipo e ai qualificatori. |
| Ambiguità di dipendenza | Errore che si verifica in fase di deploy se CDI trova più implementazioni per un punto di iniezione e non sa quale scegliere. |
| Type-safe resolution | Il processo con cui CDI seleziona il bean corretto da iniettare, basandosi sul tipo e sui qualificatori, garantendo la sicurezza a livello di tipo. |
