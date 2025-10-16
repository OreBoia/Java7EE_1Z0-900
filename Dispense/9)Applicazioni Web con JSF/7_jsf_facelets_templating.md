# Facelets Templating in JSF

Facelets è il sistema di templating standard di JSF che permette di creare layout di pagina riutilizzabili e componenti custom. Questo sistema è fondamentale per evitare la duplicazione di codice e mantenere un'architettura pulita e manutenibile.

---

## Introduzione ai Template Facelets

I template Facelets permettono di:

- Creare layout comuni riutilizzabili (header, footer, menu)
- Definire la struttura base delle pagine
- Inserire contenuto dinamico in punti specifici del template
- Mantenere la consistenza visiva dell'applicazione

---

## Tag Principali per il Templating

### Namespace UI

```xhtml
xmlns:ui="http://xmlns.jcp.org/jsf/facelets"
```

### Tag Fondamentali

| Tag | Descrizione |
|-----|-------------|
| `<ui:composition>` | Definisce una composizione che usa un template o un frammento riutilizzabile |
| `<ui:define>` | Definisce il contenuto che sostituisce un `<ui:insert>` nel template |
| `<ui:insert>` | Definisce un punto di inserimento nel template con contenuto di default |
| `<ui:include>` | Include un frammento di pagina |
| `<ui:param>` | Passa parametri a frammenti inclusi |
| `<ui:decorate>` | Simile a composition ma include anche il contenuto intorno al tag |
| `<ui:component>` | Definisce un componente che sarà inserito nell'albero dei componenti |
| `<ui:fragment>` | Definisce un frammento che può essere ri-renderizzato via AJAX |
| `<ui:repeat>` | Itera su una collezione (alternativa a `<h:dataTable>`) |
| `<ui:remove>` | Rimuove il contenuto dal rendering (utile per commenti e debug) |

---

## Pattern 1: Template Base e Pagine Client

### Template Base (`template.xhtml`)

```xhtml
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:h="http://xmlns.jcp.org/jsf/html"
      xmlns:ui="http://xmlns.jcp.org/jsf/facelets">
<h:head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    
    <!-- Titolo con valore di default -->
    <title><ui:insert name="title">My Application</ui:insert></title>
    
    <!-- CSS globali -->
    <h:outputStylesheet library="css" name="global.css" />
    
    <!-- CSS specifici della pagina -->
    <ui:insert name="pageStyles" />
</h:head>
<h:body>
    <div id="wrapper">
        <!-- Header -->
        <header id="header">
            <ui:insert name="header">
                <ui:include src="/WEB-INF/templates/includes/header.xhtml" />
            </ui:insert>
        </header>
        
        <!-- Menu di navigazione -->
        <nav id="navigation">
            <ui:insert name="navigation">
                <ui:include src="/WEB-INF/templates/includes/menu.xhtml" />
            </ui:insert>
        </nav>
        
        <!-- Breadcrumb -->
        <div id="breadcrumb">
            <ui:insert name="breadcrumb">
                <h:link outcome="home" value="Home" />
            </ui:insert>
        </div>
        
        <!-- Contenuto principale -->
        <main id="content">
            <ui:insert name="content">
                <p>Contenuto di default - Dovrebbe essere sostituito</p>
            </ui:insert>
        </main>
        
        <!-- Sidebar (opzionale) -->
        <aside id="sidebar">
            <ui:insert name="sidebar">
                <!-- Sidebar di default -->
            </ui:insert>
        </aside>
        
        <!-- Footer -->
        <footer id="footer">
            <ui:insert name="footer">
                <p>&copy; 2025 My Company. Tutti i diritti riservati.</p>
            </ui:insert>
        </footer>
    </div>
    
    <!-- JavaScript globali -->
    <h:outputScript library="js" name="global.js" />
    
    <!-- JavaScript specifici della pagina -->
    <ui:insert name="pageScripts" />
</h:body>
</html>
```

### Pagina Client (`products.xhtml`)

```xhtml
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:ui="http://xmlns.jcp.org/jsf/facelets"
      xmlns:h="http://xmlns.jcp.org/jsf/html"
      xmlns:f="http://xmlns.jcp.org/jsf/core">

<!-- Usa il template base -->
<ui:composition template="/WEB-INF/templates/template.xhtml">
    
    <!-- Definisce il titolo della pagina -->
    <ui:define name="title">
        Lista Prodotti - My Shop
    </ui:define>
    
    <!-- CSS specifici per questa pagina -->
    <ui:define name="pageStyles">
        <h:outputStylesheet library="css" name="products.css" />
    </ui:define>
    
    <!-- Override del breadcrumb -->
    <ui:define name="breadcrumb">
        <h:link outcome="home" value="Home" /> &gt;
        <h:link outcome="catalog" value="Catalogo" /> &gt;
        <span>Prodotti</span>
    </ui:define>
    
    <!-- Contenuto principale della pagina -->
    <ui:define name="content">
        <h1>I Nostri Prodotti</h1>
        
        <h:form>
            <!-- Filtri -->
            <h:panelGrid columns="2">
                <h:outputLabel for="category" value="Categoria:" />
                <h:selectOneMenu id="category" value="#{productBean.selectedCategory}">
                    <f:selectItems value="#{productBean.categories}" />
                    <f:ajax listener="#{productBean.filterByCategory}" render="productTable" />
                </h:selectOneMenu>
            </h:panelGrid>
            
            <!-- Tabella prodotti -->
            <h:dataTable id="productTable" value="#{productBean.products}" var="product"
                         styleClass="product-table">
                <h:column>
                    <f:facet name="header">Nome</f:facet>
                    #{product.name}
                </h:column>
                <h:column>
                    <f:facet name="header">Prezzo</f:facet>
                    <h:outputText value="#{product.price}">
                        <f:convertNumber type="currency" currencySymbol="€" />
                    </h:outputText>
                </h:column>
                <h:column>
                    <h:commandButton value="Dettagli" 
                                     action="#{productBean.viewDetails(product)}" />
                </h:column>
            </h:dataTable>
        </h:form>
    </ui:define>
    
    <!-- Sidebar con informazioni aggiuntive -->
    <ui:define name="sidebar">
        <h3>Info</h3>
        <p>Catalogo aggiornato quotidianamente</p>
        <p>Spedizione gratuita sopra i 50€</p>
    </ui:define>
    
    <!-- JavaScript specifici -->
    <ui:define name="pageScripts">
        <h:outputScript library="js" name="products.js" />
    </ui:define>
    
</ui:composition>
</html>
```

**Importante:** Tutto il contenuto **fuori** da `<ui:composition>` viene ignorato! Solo il contenuto all'interno dei tag `<ui:define>` viene inserito nel template.

---

## Pattern 2: Frammenti Riutilizzabili con `<ui:include>`

### Creazione di un Header Riutilizzabile

**`/WEB-INF/templates/includes/header.xhtml`**

```xhtml
<ui:composition xmlns="http://www.w3.org/1999/xhtml"
                xmlns:h="http://xmlns.jcp.org/jsf/html"
                xmlns:ui="http://xmlns.jcp.org/jsf/facelets">
    
    <div class="header-content">
        <h:graphicImage library="images" name="logo.png" alt="Logo" />
        
        <nav class="main-menu">
            <h:link outcome="home" value="Home" />
            <h:link outcome="products" value="Prodotti" />
            <h:link outcome="about" value="Chi Siamo" />
            <h:link outcome="contact" value="Contatti" />
        </nav>
        
        <div class="user-menu">
            <h:panelGroup rendered="#{not empty userBean.currentUser}">
                Benvenuto, #{userBean.currentUser.name}!
                <h:link outcome="profile" value="Profilo" />
                <h:commandLink value="Logout" action="#{userBean.logout}" />
            </h:panelGroup>
            
            <h:panelGroup rendered="#{empty userBean.currentUser}">
                <h:link outcome="login" value="Login" />
                <h:link outcome="register" value="Registrati" />
            </h:panelGroup>
        </div>
    </div>
    
</ui:composition>
```

### Inclusione nel Template

```xhtml
<header id="header">
    <ui:include src="/WEB-INF/templates/includes/header.xhtml" />
</header>
```

---

## Pattern 3: Passaggio di Parametri con `<ui:param>`

### Frammento Parametrizzato

**`/WEB-INF/includes/panel.xhtml`**

```xhtml
<ui:composition xmlns="http://www.w3.org/1999/xhtml"
                xmlns:h="http://xmlns.jcp.org/jsf/html"
                xmlns:ui="http://xmlns.jcp.org/jsf/facelets">
    
    <div class="panel" style="border-color: #{panelColor};">
        <div class="panel-header" style="background-color: #{panelColor};">
            <h3>#{panelTitle}</h3>
        </div>
        <div class="panel-body">
            <ui:insert name="panelContent">
                Contenuto del pannello
            </ui:insert>
        </div>
        <div class="panel-footer">
            <ui:insert name="panelFooter" />
        </div>
    </div>
    
</ui:composition>
```

### Utilizzo con Parametri

```xhtml
<ui:include src="/WEB-INF/includes/panel.xhtml">
    <ui:param name="panelTitle" value="Informazioni Utente" />
    <ui:param name="panelColor" value="#3498db" />
    <ui:define name="panelContent">
        <p>Nome: #{userBean.name}</p>
        <p>Email: #{userBean.email}</p>
    </ui:define>
    <ui:define name="panelFooter">
        <h:commandButton value="Modifica" action="#{userBean.edit}" />
    </ui:define>
</ui:include>

<ui:include src="/WEB-INF/includes/panel.xhtml">
    <ui:param name="panelTitle" value="Statistiche" />
    <ui:param name="panelColor" value="#e74c3c" />
    <ui:define name="panelContent">
        <p>Visite: #{statsBean.visits}</p>
        <p>Ordini: #{statsBean.orders}</p>
    </ui:define>
</ui:include>
```

---

## Pattern 4: Template Nidificati

Puoi creare una gerarchia di template per organizzazioni complesse.

### Template Base (`base-template.xhtml`)

```xhtml
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:h="http://xmlns.jcp.org/jsf/html"
      xmlns:ui="http://xmlns.jcp.org/jsf/facelets">
<h:head>
    <title><ui:insert name="title">Default</ui:insert></title>
    <ui:insert name="head" />
</h:head>
<h:body>
    <ui:insert name="body">
        <p>Body di default</p>
    </ui:insert>
</h:body>
</html>
```

### Template Intermedio (`main-template.xhtml`)

```xhtml
<ui:composition template="/WEB-INF/templates/base-template.xhtml"
                xmlns="http://www.w3.org/1999/xhtml"
                xmlns:h="http://xmlns.jcp.org/jsf/html"
                xmlns:ui="http://xmlns.jcp.org/jsf/facelets">
    
    <ui:define name="title">
        <ui:insert name="pageTitle">My Application</ui:insert>
    </ui:define>
    
    <ui:define name="head">
        <h:outputStylesheet library="css" name="main.css" />
        <ui:insert name="pageHead" />
    </ui:define>
    
    <ui:define name="body">
        <div id="header">
            <ui:include src="/WEB-INF/templates/includes/header.xhtml" />
        </div>
        
        <div id="content">
            <ui:insert name="content">
                Default content
            </ui:insert>
        </div>
        
        <div id="footer">
            <ui:include src="/WEB-INF/templates/includes/footer.xhtml" />
        </div>
    </ui:define>
    
</ui:composition>
```

### Pagina Finale

```xhtml
<ui:composition template="/WEB-INF/templates/main-template.xhtml"
                xmlns:ui="http://xmlns.jcp.org/jsf/facelets"
                xmlns:h="http://xmlns.jcp.org/jsf/html">
    
    <ui:define name="pageTitle">
        Home Page
    </ui:define>
    
    <ui:define name="content">
        <h1>Benvenuti!</h1>
        <p>Questo è il contenuto della home page.</p>
    </ui:define>
    
</ui:composition>
```

---

## Pattern 5: `<ui:repeat>` per Iterazioni

`<ui:repeat>` è un'alternativa più leggera a `<h:dataTable>` per iterare su collezioni.

### Esempio Base

```xhtml
<ui:repeat value="#{productBean.products}" var="product">
    <div class="product-card">
        <h3>#{product.name}</h3>
        <p>Prezzo: 
            <h:outputText value="#{product.price}">
                <f:convertNumber type="currency" currencySymbol="€" />
            </h:outputText>
        </p>
        <h:commandButton value="Aggiungi al carrello" 
                         action="#{cartBean.add(product)}" />
    </div>
</ui:repeat>
```

### Con `varStatus` per Informazioni sull'Iterazione

```xhtml
<ui:repeat value="#{bean.items}" var="item" varStatus="status">
    <div class="item #{status.even ? 'even' : 'odd'}">
        <span>Item #{status.index + 1}</span>
        <span>#{item.name}</span>
        
        <h:panelGroup rendered="#{status.first}">
            <strong>Primo!</strong>
        </h:panelGroup>
        
        <h:panelGroup rendered="#{status.last}">
            <strong>Ultimo!</strong>
        </h:panelGroup>
    </div>
</ui:repeat>
```

**Proprietà di `varStatus`:**

- `index` - Indice corrente (base 0)
- `count` - Contatore (base 1)
- `first` - `true` se è il primo elemento
- `last` - `true` se è l'ultimo elemento
- `even` - `true` se l'indice è pari
- `odd` - `true` se l'indice è dispari

---

## Pattern 6: `<ui:fragment>` per Aggiornamenti AJAX

`<ui:fragment>` crea un contenitore che può essere riferito per aggiornamenti AJAX.

```xhtml
<h:form>
    <h:inputText value="#{bean.query}">
        <f:ajax render="searchResults" />
    </h:inputText>
    
    <ui:fragment id="searchResults">
        <h:panelGroup rendered="#{not empty bean.results}">
            <ui:repeat value="#{bean.results}" var="result">
                <div>#{result.name}</div>
            </ui:repeat>
        </h:panelGroup>
        
        <h:outputText value="Nessun risultato" 
                      rendered="#{empty bean.results}" />
    </ui:fragment>
</h:form>
```

---

## Pattern 7: `<ui:remove>` per Commenti e Debug

Il contenuto dentro `<ui:remove>` viene completamente rimosso dal processing.

```xhtml
<ui:remove>
    Questo è un commento che non appare nel sorgente HTML finale.
    Utile per note agli sviluppatori o per disabilitare temporaneamente codice.
    
    <h:outputText value="Questo non verrà renderizzato" />
</ui:remove>

<!-- Questo commento HTML appare nel sorgente della pagina -->

<%-- Questo commento JSP non appare (se supportato) --%>
```

---

## Pattern 8: Componenti Compositi

Facelets permette di creare componenti riutilizzabili complessi.

### Definizione del Componente

**`/resources/components/userCard.xhtml`**

```xhtml
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:h="http://xmlns.jcp.org/jsf/html"
      xmlns:f="http://xmlns.jcp.org/jsf/core"
      xmlns:composite="http://xmlns.jcp.org/jsf/composite">

<composite:interface>
    <composite:attribute name="user" required="true" type="com.example.User" />
    <composite:attribute name="showEmail" required="false" default="true" type="java.lang.Boolean" />
</composite:interface>

<composite:implementation>
    <div class="user-card">
        <h:graphicImage value="#{cc.attrs.user.avatar}" alt="Avatar" />
        
        <div class="user-info">
            <h3>#{cc.attrs.user.name}</h3>
            
            <h:panelGroup rendered="#{cc.attrs.showEmail}">
                <p>Email: #{cc.attrs.user.email}</p>
            </h:panelGroup>
            
            <p>Membro dal: 
                <h:outputText value="#{cc.attrs.user.registrationDate}">
                    <f:convertDateTime pattern="dd/MM/yyyy" />
                </h:outputText>
            </p>
        </div>
    </div>
</composite:implementation>

</html>
```

### Utilizzo del Componente

```xhtml
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:h="http://xmlns.jcp.org/jsf/html"
      xmlns:comp="http://xmlns.jcp.org/jsf/composite/components">
<h:head>
    <title>Team</title>
</h:head>
<h:body>
    <h1>Il Nostro Team</h1>
    
    <!-- Usa il componente composito -->
    <comp:userCard user="#{teamBean.manager}" showEmail="true" />
    
    <h2>Membri del Team</h2>
    <ui:repeat value="#{teamBean.members}" var="member">
        <comp:userCard user="#{member}" showEmail="false" />
    </ui:repeat>
    
</h:body>
</html>
```

---

## Struttura Raccomandata dei File

```
webapp/
├── WEB-INF/
│   ├── templates/
│   │   ├── template.xhtml              # Template principale
│   │   ├── admin-template.xhtml        # Template per area admin
│   │   ├── simple-template.xhtml       # Template semplificato
│   │   └── includes/
│   │       ├── header.xhtml            # Header comune
│   │       ├── footer.xhtml            # Footer comune
│   │       ├── menu.xhtml              # Menu di navigazione
│   │       └── sidebar.xhtml           # Sidebar
│   ├── faces-config.xml
│   └── web.xml
├── resources/
│   ├── css/
│   │   ├── global.css
│   │   └── products.css
│   ├── js/
│   │   └── global.js
│   ├── images/
│   │   └── logo.png
│   └── components/                     # Componenti compositi
│       └── userCard.xhtml
├── index.xhtml
├── products.xhtml
└── about.xhtml
```

---

## Best Practices

### 1. Usa Template per Consistenza

Mantieni un aspetto coerente usando template base per tutte le pagine.

### 2. Organizza i Frammenti

Raggruppa header, footer, menu in file separati per facilitare la manutenzione.

### 3. Parametrizza i Componenti

Usa `<ui:param>` per rendere i frammenti riutilizzabili in contesti diversi.

### 4. Evita Logica Complessa nei Template

Mantieni i template semplici; sposta la logica complessa nei backing bean.

### 5. Usa `<ui:composition>` per Pagine Client

Le pagine che usano template dovrebbero sempre usare `<ui:composition>` come root element.

### 6. Naming Convention

Usa nomi descrittivi per i punti di inserimento:

- `title` - Per il titolo della pagina
- `content` - Per il contenuto principale
- `sidebar` - Per la sidebar
- `breadcrumb` - Per il breadcrumb
- `pageStyles` - Per CSS specifici
- `pageScripts` - Per JavaScript specifici

### 7. Documentazione

Aggiungi commenti nei template per spiegare l'uso dei vari `<ui:insert>`.

```xhtml
<!-- Inserisci qui il contenuto principale della pagina -->
<ui:insert name="content">
    <p>Contenuto di default</p>
</ui:insert>
```

---

## Differenze tra Tag Simili

### `<ui:composition>` vs `<ui:decorate>`

| `<ui:composition>` | `<ui:decorate>` |
|--------------------|-----------------|
| Ignora tutto il contenuto fuori dal tag | Include il contenuto intorno al tag |
| Usato per pagine complete | Usato per decorare frammenti inline |

### `<ui:include>` vs `<ui:composition>`

| `<ui:include>` | `<ui:composition>` |
|----------------|-------------------|
| Include un frammento | Usa un template completo |
| Non può definire `<ui:define>` | Richiede `<ui:define>` per popolare il template |

### `<ui:insert>` vs `<ui:define>`

| `<ui:insert>` | `<ui:define>` |
|---------------|---------------|
| Nel template: definisce dove va il contenuto | Nella pagina client: fornisce il contenuto |
| Può avere contenuto di default | Sostituisce completamente il contenuto di default |

---

## Risoluzione dei Problemi Comuni

### Problema: Il contenuto non appare

**Causa:** Hai dimenticato di usare `<ui:composition>` o i nomi in `<ui:define>` non corrispondono a quelli in `<ui:insert>`.

**Soluzione:**

```xhtml
<!-- ✅ Corretto -->
<ui:composition template="/template.xhtml">
    <ui:define name="content">
        Mio contenuto
    </ui:define>
</ui:composition>

<!-- ❌ Errato - manca ui:composition -->
<ui:define name="content">
    Mio contenuto
</ui:define>
```

### Problema: Parametri non passati correttamente

**Causa:** Hai usato `value` invece di passare direttamente il valore.

**Soluzione:**

```xhtml
<!-- ✅ Corretto -->
<ui:param name="title" value="My Title" />

<!-- ❌ Errato -->
<ui:param name="title">My Title</ui:param>
```

### Problema: AJAX non aggiorna il contenuto incluso

**Causa:** L'include non ha un ID o non è wrappato in un componente con ID.

**Soluzione:**

```xhtml
<!-- ✅ Corretto -->
<h:panelGroup id="includedContent">
    <ui:include src="/includes/fragment.xhtml" />
</h:panelGroup>

<f:ajax render="includedContent" />
```

---

## Glossario

| Termine | Descrizione |
|---------|-------------|
| **Facelets** | Sistema di templating standard di JSF basato su XHTML |
| **Template** | Pagina che definisce la struttura base riutilizzabile |
| **Composition** | Pagina client che usa un template |
| **Insertion Point** | Punto nel template dove può essere inserito contenuto |
| **Fragment** | Porzione di pagina riutilizzabile |
| **Composite Component** | Componente JSF custom creato con Facelets |
| **`<ui:composition>`** | Tag che definisce una pagina che usa un template |
| **`<ui:define>`** | Tag che fornisce contenuto per un punto di inserimento |
| **`<ui:insert>`** | Tag che definisce un punto di inserimento nel template |
| **`<ui:include>`** | Tag per includere frammenti riutilizzabili |
| **`<ui:param>`** | Tag per passare parametri ai frammenti |

---

Con Facelets Templating, puoi costruire applicazioni JSF ben organizzate, manutenibili e con un design consistente!
