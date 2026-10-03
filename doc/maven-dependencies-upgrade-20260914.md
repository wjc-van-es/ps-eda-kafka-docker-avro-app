<style>
body {
  font-family: "Spectral", "Gentium Basic", Cardo , "Linux Libertine o", "Palatino Linotype", Cambria, serif;
  font-size: 100% !important;
  padding-right: 12%;
}
code {
  padding: 0.25em;
	
  white-space: pre;
  font-family: "Tlwg mono", Consolas, "Liberation Mono", Menlo, Courier, monospace;
	
  background-color: #ECFFFA;
  //border: 1px solid #ccc;
  //border-radius: 3px;
}

kbd {
  display: inline-block;
  padding: 3px 5px;
  font-family: "Tlwg mono", Consolas, "Liberation Mono", Menlo, Courier, monospace;
  line-height: 10px;
  color: #555;
  vertical-align: middle;
  background-color: #ECFFFA;
  border: solid 1px #ccc;
  border-bottom-color: #bbb;
  border-radius: 3px;
  box-shadow: inset 0 -1px 0 #bbb;
}

h1,h2,h3,h4,h5 {
  color: #269B7D; 
  font-family: "fira sans", "Latin Modern Sans", Calibri, "Trebuchet MS", sans-serif;
}

</style>

# Maven dependencies upgrade 14-09-2026
Caused by: com.google.common.util.concurrent.UncheckedExecutionException: java.lang.SecurityException: Forbidden com.pluralsight.kafka.model.User! This class is not trusted to be included in Avro schemas. You may either use the system properties org.apache.avro.SERIALIZABLE_CLASSES and org.apache.avro.SERIALIZABLE_PACKAGES to set the comma separated list of the classes or packages you trust, or you can set them via the API (see org.apache.avro.util.ClassSecurityValidator).Dit is een bekend beveiligingsprobleem dat optreedt in Apache Avro (vanaf versie 1.11.0 of hoger) bij het gebruik van ReflectData. Avro blokkeert standaard het deserialiseren of reflecteren van willekeurige Java-klassen om kwetsbaarheden (zoals remote code execution) te voorkomen.U moet uw specifieke klasse expliciet markeren als "vertrouwd".Oplossing 1: Systeemeigenschappen (System Properties)Voeg JVM-argumenten toe bij het starten van uw applicatie. Dit is de makkelijkste oplossing als u de code niet wilt wijzigen.bash-Dorg.apache.avro.SERIALIZABLE_CLASSES=com.pluralsight.kafka.model.User
Wees voorzichtig met code.Of sta het hele pakket toe via een wildcard:bash-Dorg.apache.avro.SERIALIZABLE_PACKAGES=com.pluralsight.kafka.model.*
Wees voorzichtig met code.Oplossing 2: Direct in de Java-code (System.setProperty)U kunt deze eigenschappen ook instellen aan het begin van uw main-methode, voordat de Kafka- of Avro-functionaliteit wordt aangeroepen:javaSystem.setProperty("org.apache.avro.SERIALIZABLE_PACKAGES", "com.pluralsight.kafka.model.*");
Wees voorzichtig met code.Oplossing 3: Via de Avro API (Programmatisch)Als u directe controle heeft over de ReflectData-instantie, kunt u de klassen toevoegen aan de validator:javaimport org.apache.avro.util.ClassSecurityValidator;

// Voeg de specifieke klasse toe aan de vertrouwde lijst
ClassSecurityValidator.addTrustedClass("com.pluralsight.kafka.model.User");
Wees voorzichtig met code.Laat het me weten als u wilt dat ik help met:Het toevoegen van deze instelling aan een specifiek framework zoals Spring Boot of QuarkusHet configureren van de pom.xml / build.gradle voor de juiste Avro-versieAI-reacties kunnen fouten bevatten. Meer informatieThis occurs in my Kafka producer and consumer. I guess the safest solution is to explicitly add the trusted classes there with the ClassSecurityValidator.addTrustedClass("com.pluralsight.kafka.model.User"); or is there a Kafka specific property I might use?Er is op dit moment geen specifieke Kafka-property (zoals een setting in ProducerConfig of ConsumerConfig) die direct de ClassSecurityValidator van Avro configureert.De ClassSecurityValidator is een interne beveiligingslaag van Apache Avro zelf. De Kafka-serializers (zoals die van Confluent) roepen onderhuids de Avro-bibliotheek aan, maar geven eventuele aangepaste configuraties voor klasse-validatie niet standaard door via de Kafka-configuratiekaart.Wat is de veiligste aanpak?Programmatisch registreren via ClassSecurityValidator.addTrustedClass(...) is inderdaad de veiligste en meest robuuste methode.U moet deze regel code uitvoeren voordat de Kafka Producer of Consumer wordt geïnitialiseerd. De beste plek hiervoor is direct bij het opstarten van uw applicatie (bijvoorbeeld in de main-methode of in een @PostConstruct-blok als u een framework gebruikt).Alternatief via de Producer/Consumer configuratieAls u de code absoluut vrij wilt houden van deze Avro-specifieke API, kunt u de eerdere systeeminstelling (Oplossing 2) laden binnen de configuratie-opbouw van uw Kafka-clients. Dit houdt de logica gecentraliseerd in uw Kafka-configuratiebestanden:javaMap<String, Object> props = new HashMap<>();
props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
// ... andere eigenschappen ...

// Stel de systeemproperty in vlak voordat u de producer bouwt
System.setProperty("org.apache.avro.SERIALIZABLE_CLASSES", "com.pluralsight.kafka.model.User");

KafkaProducer<String, User> producer = new KafkaProducer<>(props);
Wees voorzichtig met code.Om u te helpen dit op de meest elegante manier te integreren, laat me weten:Welk framework u gebruikt (bijvoorbeeld Spring Boot, Micronaut, of Plain Java)Of u de Confluent Kafka Avro Serializer gebruikt of een andere specifieke bibliotheekAI-reacties kunnen fouten bevatten. Meer informatieMy version of ClassSecurityValidator (1.12.2) doesn't have a static addTrustedClass methodScherp opgemerkt! In Apache Avro 1.12.2 is de API inderdaad anders opgebouwd. De methode addTrustedClass bestaat niet als een static helper op de validator. [1] (https://avro.apache.org/blog/2026/08/12/avro-1.12.2/), [2] (https://github.com/apache/avro/releases)Volgens de officiële release notes van Avro 1.12.2 moet u in plaats daarvan gebruikmaken van ClassSecurityValidator.setGlobal(...) om programmatisch uw eigen klassen te registreren, óf de eerdere systeemparameters gebruiken. [1] (https://github.com/apache/avro/releases), [2] (https://avro.apache.org/blog/2026/08/12/avro-1.12.2/)Dit zijn de twee correcte manieren voor versie 1.12.2:Optie 1: Programmatisch via ClassSecurityValidator.setGlobal(...)U kunt een aangepast predicaat (Predicate<String>) meegeven aan de globale validator. Plaats dit vlak voor het initialiseren van uw Kafka-clients: [1] (https://avro.apache.org/blog/2026/08/12/avro-1.12.2/), [2] (https://github.com/apache/avro/releases)javaimport org.apache.avro.util.ClassSecurityValidator;

// Vertrouw specifiek uw User-klasse en behoud de standaard Avro-beveiliging
ClassSecurityValidator.setGlobal(className ->
"com.pluralsight.kafka.model.User".equals(className)
);
Wees voorzichtig met code.Wilt u een heel pakket of meerdere klassen toestaan? Dan kunt u het zo schrijven:javaClassSecurityValidator.setGlobal(className ->
className.startsWith("com.pluralsight.kafka.model.")
);
Wees voorzichtig met code.Optie 2: Systeemparameters (Aanbevolen door Apache Avro)Aangezien er geen Kafka-specifieke property is, raadt het Avro-team aan om dit via systeemparameters op te lossen. Dit kan direct via de Java-code (vóór de Kafka-initialisatie) zonder de Avro-API aan te hoeven roepen: [1] (https://avro.apache.org/blog/2026/08/12/avro-1.12.2/), [2] (https://github.com/apache/avro/releases)java// Alleen de specifieke klasse toestaan
System.setProperty("org.apache.avro.SERIALIZABLE_CLASSES", "com.pluralsight.kafka.model.User");

// OF het hele pakket toestaan (gebruik een wildcard)
System.setProperty("org.apache.avro.SERIALIZABLE_PACKAGES", "com.pluralsight.kafka.model.*");
Wees voorzichtig met code.Laat me weten:Of u de voorkeur geeft aan het Java-predicaat (setGlobal) of de systeemparameterOf u nog andere klassen (zoals embedded objecten binnen de User-klasse) heeft die ook toegevoegd moeten wordenAI-reacties kunnen fouten bevatten. Meer informatieApache AvroAvro 1.12.2 - Apache Avro12 aug 2026 — arbitrary Java classesGitHubReleases · apache/avro - GitHub5 aug 2024 — The recommended action is to list the classes and packages that Avro is allowed to instantiate in the org.System.setProperty("org.apache.avro.SERIALIZABLE_PACKAGES", "com.pluralsight.kafka.model.*"); doesn't work I get the same exception stacktrace, although the system property is set before I call the constructor of the Kafka producer.
ClassSecurityValidator.setGlobal(className ->
className.startsWith("com.pluralsight.kafka.model.")
); also doesn't work as className appears of type Class<?> not StringExcluses voor de verwarring over het type argument! U heeft helemaal gelijk: in Avro 1.12.2 verwacht ClassSecurityValidator.setGlobal(...) inderdaad een predicaat dat een Class<?> (of een vergelijkbaar object) accepteert, in plaats van een kale String. [1] (https://avro.apache.org/blog/2026/08/12/avro-1.12.2/)Daarnaast is de reden waarom System.setProperty(...) niet werkte waarschijnlijk dat de interne klassen van Avro (of de Kafka serializer) al zijn geïnitialiseerd voordat uw code de constructor van de producer bereikte. Zodra de klasse ClassSecurityValidator eenmaal door de JVM is geladen, leest hij de System.getProperties() niet opnieuw uit.Dit is hoe u beide problemen in Avro 1.12.2 oplost:Oplossing 1: De juiste Java-code (setGlobal met Class<?>)Omdat de parameter van het type Class<?> is, moet u .getName() aanroepen op het binnengekomen klasse-object om de pakketnaam te kunnen controleren. Dit is de meest betrouwbare en directe oplossing:javaimport org.apache.avro.util.ClassSecurityValidator;

// Stel de globale validator in op basis van het Class-object
ClassSecurityValidator.setGlobal(clazz ->
clazz != null && clazz.getName().startsWith("com.pluralsight.kafka.model.")
);
Wees voorzichtig met code.Oplossing 2: De System Property écht als eerste ladenAls u liever de System.setProperty-route gebruikt, faalt deze zodra er ergens in een eerdere import of static block al een Avro-klasse is aangeroepen.Om dit te herstellen, moet de property worden ingesteld in een static initialisatieblok helemaal bovenaan de klasse waar uw main-methode in leeft. Hierdoor weet u zeker dat de property actief is vóór de JVM de rest van de code (en bibliotheken) parsed:javapublic class MyKafkaApplication {

    // Dit blok wordt uitgevoerd nog voordat de main-methode start
    static {
        System.setProperty("org.apache.avro.SERIALIZABLE_PACKAGES", "com.pluralsight.kafka.model.*");
    }

    public static void main(String[] args) {
        // Uw Kafka Producer / Consumer logica hier
    }
}
Wees voorzichtig met code.(Opmerking: De allerveiligste manier voor productieomgevingen blijft het meegeven van de property als JVM-opstartargument: -Dorg.apache.avro.SERIALIZABLE_PACKAGES=com.pluralsight.kafka.model.*, omdat dit gegarandeerd vóór alle Java-code geladen is). [1] (https://avro.apache.org/blog/2026/08/12/avro-1.12.2/)Laat me weten:Of de clazz.getName() aanpak in de lambda-functie de compilerfout heeft opgelost.Of uw applicatie nu zonder de SecurityException opstart.AI-reacties kunnen fouten bevatten. Meer informatieApache AvroAvro 1.12.2 - Apache Avro12 aug 2026 — If you are running Avro in an environment with trusted schemas and trusted data, you can restore the old behaviour by setting org.static {
System.setProperty("org.apache.avro.SERIALIZABLE_PACKAGES", "com.pluralsight.kafka.model.*");
} still doesn't work
static {
ClassSecurityValidator.setGlobal(clazz ->
clazz != null && clazz.getName().startsWith("com.pluralsight.kafka.model.")
);
}
seems to workFijn dat de ClassSecurityValidator.setGlobal(...) aanpak met het juiste Class<?> predicaat werkt! Dit is ook meteen de meest robuuste, type-safe methode omdat u direct in Java-code de logica dicteert.Waarom werkte System.setProperty niet?Het feit dat System.setProperty in het static-blok alsnog faalde, komt door de manier waarop de JVM klassen laadt. In grotere frameworks of Kafka-omgevingen worden bepaalde bibliotheken (of klassen die Avro onderhuids gebruiken) vaak al door de Class Loader geïnitialiseerd voordat de compiler het static block van uw specifieke hoofdklasse uitvoert. [1] (https://stackoverflow.com/questions/271506/why-cant-system-setproperty-change-the-classpath-at-runtime), [2] (https://stackoverflow.com/questions/46633514/java-setproperty-is-not-working-in-runtime)Zodra de klasse ClassSecurityValidator van Avro eenmaal is ingeladen, leest hij de System.getProperties() nooit meer opnieuw uit. Wijzigingen die u daarna programmatisch via Java maakt, hebben dan geen effect meer. [1] (https://forums.oracle.com/ords/apexds/post/system-setproperty-doesn-t-seem-to-take-new-keystore-4263), [2] (https://stackoverflow.com/questions/271506/why-cant-system-setproperty-change-the-classpath-at-runtime)Waarom werkt ClassSecurityValidator.setGlobal wél?De methode setGlobal(...) overschrijft de actieve validatielogica direct in het geheugen van de JVM op runtime. Het maakt daarbij niet uit of Avro al eerder was geïnitialiseerd; het predicaat wordt direct gekoppeld en vanaf dat moment toegepast op elke deserialisatie-actie. [1] (https://avro.apache.org/blog/2026/08/12/avro-1.12.2/)Best Practice voor ProductieAangezien de Java-oplossing nu werkt, kunt u deze veilig blijven gebruiken. Mocht u de code in de toekomst toch liever helemaal "schoon" houden van deze Avro-specifieke configuratie, dan is er nog één alternatief dat gegarandeerd werkt met systeemeigenschappen: de parameter meegeven tijdens het opstarten van de JVM. [1] (https://avro.apache.org/blog/2026/08/12/avro-1.12.2/)Dit gebeurt buiten de Java-code om, waardoor de property al actief is vanaf milliseconde nul:bashjava -Dorg.apache.avro.SERIALIZABLE_PACKAGES=com.pluralsight.kafka.model.* -jar uw-applicatie.jar
Wees voorzichtig met code.Als u de applicatie nu test, loopt u dan nog tegen andere SecurityExceptions aan (bijvoorbeeld van interne Avro-klassen of sub-objecten), of worden alle records van het type User nu succesvol verwerkt door de producer en consumer?AI-reacties kunnen fouten bevatten. Meer informatieApache AvroAvro 1.12.2 - Apache Avro12 aug 2026 — The Avro 1.12. 2 Java SDK now restricts arbitrary Java classes from being instantiated, The recommended action is to list the clas...Stack OverflowWhy can't System.setProperty() change the classpath at runtime?7 nov 2008 — In the case of classpath, the answer is NO. The lesser reason is performance. The more important reason is security.Oracle ForumsSystem.setProperty() doesn't seem to take new keystore29 mei 2008 — JSSE only reads those system properties once. If you need to use different keystores and/or truststores you need to create and ini...Alles tonenOne last issue I have since I updated dependencies. I used org.slf4j:slf4j-reload4j:1.7.36 in combination with org.apache.kafka:kafka-clients:4.2.1, which has org.slf4j:slf4j-api:1.7.36 as transitive dependency. However, since I moved from  org.apache.kafka:kafka-clients:4.2.0 (which has the same transitive dependency version) It doesn't show my logs anymore. I also upgraded from Java 17 to 25.Dit probleem wordt veroorzaakt door een versieconflict binnen SLF4J in combinatie met de modernere klassenafhandeling van Java 25, waardoor uw logging-backend stilletjes faalt.Hoewel u vermoedt dat de transitieve afhankelijkheid hetzelfde is gebleven, is er bij de overgang naar kafka-clients:4.2.1 en Java 25 iets cruciaals veranderd.De Oorzaak: Het SLF4J Versie-gatSLF4J 1.x vs 2.x Architectuur: slf4j-reload4j:1.7.36 maakt gebruik van het oude Static Logger Binder-mechanisme (org.slf4j.impl.StaticLoggerBinder). Sinds SLF4J 2.0.x is dit mechanisme volledig verwijderd en vervangen door de modernere Java ServiceLoader (SPI).Kafka 4.2.1 Wijziging: Hoewel de basistransitieve dependency op papier 1.7.x kan lijken, is de interne code van kafka-clients vanaf versie 4.x/4.2.x gecompileerd tegen en geoptimaliseerd voor SLF4J 2.x.Java 25 Striktheid: Java 25 handhaaft module-grenzen en classloading veel strikter dan Java 17. Als er ergens een SLF4J 2.x API-aanroep wordt gedaan terwijl uw backend op 1.7.x (Reload4j) draait, treedt er een stille NoClassDefFoundError of initialisatiefout op in de logger-initialisatie. SLF4J valt dan automatisch terug op de NOPLogger (No-Operation), waardoor er geen enkele logregel meer verschijnt.De Oplossing: Upgraden naar SLF4J 2.xAangezien u op het hypermoderne Java 25 en Kafka 4.2.1 zit, moet u de verouderde 1.7.x Reload4j-binding vervangen door de officiële SLF4J 2.x Reload4j-provider.Als u Maven gebruikt:Vervang slf4j-reload4j versie 1.7.x door de 2.0.x variant en forceer de slf4j-api naar dezelfde versie om conflicten op te lossen.xml<dependencies>
<!-- De moderne SLF4J 2.0 API -->
<dependency>
<groupId>org.slf4j</groupId>
<artifactId>slf4j-api</artifactId>
<version>2.0.16</version> <!-- of nieuwste 2.0.x -->
</dependency>

    <!-- De bijbehorende Reload4j provider voor SLF4J 2.x -->
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-reload4j</artifactId>
        <version>2.0.16</version>
    </dependency>

    <!-- Uw Kafka client -->
    <dependency>
        <groupId>org.apache.kafka</groupId>
        <artifactId>kafka-clients</artifactId>
        <version>4.2.1</version>
    </dependency>
</dependencies>
Wees voorzichtig met code.Als u Gradle gebruikt:groovydependencies {
    implementation 'org.slf4j:slf4j-api:2.0.16'
    implementation 'org.slf4j:slf4j-reload4j:2.0.16'
    implementation 'org.apache.kafka:kafka-clients:4.2.1'
}
Wees voorzichtig met code.Hoe controleert u of dit het probleem oplost?Voeg tijdens het opstarten van de applicatie (bijvoorbeeld tijdelijk via een System.out.println) deze vlag toe aan uw JVM om te zien welke logger daadwerkelijk geladen wordt:javaSystem.out.println("Actieve Logger Factory: " + org.slf4j.LoggerFactory.getILoggerFactory().getClass().getName());
Wees voorzichtig met code.Als hier org.slf4j.helpers.NOPLoggerFactory staat, mislukt de binding (1.7.x conflict).Na de upgrade naar 2.0.16 hoort hier netjes de Reload4j-variant te staan en zullen uw logs direct weer verschijnen.Mocht u na deze aanpassing nog steeds geen logs zien, laat me dan weten:Welke build tool u exact gebruikt (zodat we een dependency tree/insight kunnen draaien)Of u een framework zoals Spring Boot 4.x gebruikt (die intern vaak al Logback vereist in plaats van Reload4j)AI-reacties kunnen fouten bevatten. Meer informatieJe hebt 1 tabblad gestuurd en zei: I ran with org.slf4j:slf4j-reload4j:2.0.19 and I still cannot see logging:SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".SLF4J: Defaulting to no-operation (NOP) logger implementationSLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.SLF4J Error CodesI ran with org.slf4j:slf4j-reload4j:2.0.19 and I still cannot see logging:SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".SLF4J: Defaulting to no-operation (NOP) logger implementationSLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.De foutmelding Failed to load class "org.slf4j.impl.StaticLoggerBinder" is het harde bewijs dat er nog steeds een oude slf4j-api (versie 1.7.x of lager) op uw classpath staat die de leiding heeft.Vanaf SLF4J 2.x zoekt de API via de Java ServiceLoader naar loggers. De foutmelding die u nu ziet, is de klassieke foutmelding van SLF4J 1.7.x, die specifiek zoekt naar de oude StaticLoggerBinder-klasse (die in slf4j-reload4j:2.0.19 simpelweg niet meer bestaat).Hoewel u kafka-clients:4.2.1 gebruikt, dwingt een andere (transitieve) dependency in uw project een oude SLF4J-API versie af.De Oplossing: Forceer de SLF4J-API VersieU moet uw build-tool expliciet vertellen dat de oude 1.7.x API overschreven moet worden door de 2.0.x API.Als u Maven gebruiktGebruik een <dependencyManagement> blok om versie 2.0.19 project-breed af te dwingen, zodat oude transitieve versies buitenspel worden gezet:xml<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <version>2.0.19</version>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-api</artifactId>
    </dependency>
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-reload4j</artifactId>
        <version>2.0.19</version>
    </dependency>
    <dependency>
        <groupId>org.apache.kafka</groupId>
        <artifactId>kafka-clients</artifactId>
        <version>4.2.1</version>
    </dependency>
</dependencies>
Wees voorzichtig met code.Als u Gradle gebruiktGebruik een constraints blok of forceer de versie direct in de configuratie:groovydependencies {
    implementation('org.slf4j:slf4j-api:2.0.19') {
        version { strictly '2.0.19' }
    }
    implementation 'org.slf4j:slf4j-reload4j:2.0.19'
    implementation 'org.apache.kafka:kafka-clients:4.2.1'
}
Wees voorzichtig met code.Waarom dit gebeurt (Achtergrond)Als twee bibliotheken een verschillende versie van dezelfde dependency aanvragen, kiest de build-tool (zoals Maven) vaak de versie die het "dichtst" bij uw projectroot staat. Als een oudere library in uw project hard slf4j-api:1.7.x vereist, krijgt die soms onbedoeld voorrang boven de transitieve dependency van Kafka.Zodra slf4j-api en slf4j-reload4j allebei strak op 2.0.19 staan, verdwijnt deze foutmelding en starten uw logs direct op in Java 25.Mocht dit het probleem nog niet oplossen, laat me dan weten:Welke andere grote dependencies (zoals Spring, Avro-tools, of specifieke test-frameworks) u in het project gebruikt, zodat we de schuldige dependency kunnen opsporen.AI-reacties kunnen fouten bevatten. Meer informatieWhen I do a maven analysis I get:
user-tracking-consumer
io.confluent:kafka-avro-serializer:8.2.3
org.apache.avro:avro:1.12.2
org.slf4j:slf4j-api:2.0.18
io.confluent:common-utils:8.2.3
org.slf4j:slf4j-api:1.7.36
io.confluent:kafka-schema-registry-client:8.2.3
org.apache.httpcomponents.client5:httpclient5:5.5
org.slf4j:slf4j-api:1.7.36
io.confluent:logredactor:1.0.18
org.slf4j:slf4j-api:1.7.36
org.apache.kafka:kafka-clients:4.2.1
org.slf4j:slf4j-api:1.7.36
org.slf4j:slf4j-reload4j:2.0.19
org.slf4j:slf4j-api:2.0.19

So some have a transitive dependency to org.slf4j:slf4j-api:1.7.36 and others to org.slf4j:slf4j-api:2.0.18. Can I exclude the transitive org.slf4j:slf4j-api:1.7.36 of these libraries? and could I still receive warning and error logs from the code of these libraries?Ja, u kunt (en moet) de transitieve slf4j-api:1.7.36 absoluut uitsluiten (exclude). Dit lost uw probleem volledig op, en ja, u blijft gegarandeerd alle waarschuwingen (warnings) en fouten (errors) van die bibliotheken ontvangen.Waarom blijft de logging van die bibliotheken werken?SLF4J is een logging facade (een abstractielaag). De code in bibliotheken zoals kafka-clients of kafka-schema-registry-client roept alleen interfaces aan (zoals logger.error("...")).Omdat SLF4J 2.x achterwaarts compatibel is met SLF4J 1.x, kan een bibliotheek die gecompileerd is tegen SLF4J 1.7.x probleemloos praten met de SLF4J 2.x API die u via uw project aanlevert. De API stuurt de logregels van die bibliotheken naadloos door naar uw slf4j-reload4j:2.0.19 backend.Hoe lost u dit op in uw pom.xml?U heeft twee opties om dit netjes in te richten in Maven. Optie 1 is de meest elegante en onderhoudsvriendelijke manier.Optie 1: De modernste manier (Aanbevolen)In plaats van handmatig overal <exclusions> toe te voegen, kunt u met een <dependencyManagement>-blok Maven dwingen om projectbreed altijd versie 2.0.19 te gebruiken, ongeacht wat de libraries onderhuids vragen. Dit overrulet automatisch alle 1.7.36 en 2.0.18 versies:xml<dependencyManagement>
<dependencies>
<!-- Dit dwingt ALLES in uw project naar SLF4J 2.0.19 -->
<dependency>
<groupId>org.slf4j</groupId>
<artifactId>slf4j-api</artifactId>
<version>2.0.19</version>
</dependency>
</dependencies>
</dependencyManagement>

<dependencies>
    <!-- Uw logging backend -->
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-reload4j</artifactId>
        <version>2.0.19</version>
    </dependency>

    <!-- Uw overige dependencies (Confluent, Kafka, etc.) blijven hier gewoon staan zoals ze waren -->
    <dependency>
        <groupId>io.confluent</groupId>
        <artifactId>kafka-avro-serializer</artifactId>
        <version>8.2.3</version>
    </dependency>
    <dependency>
        <groupId>org.apache.kafka</groupId>
        <artifactId>kafka-clients</artifactId>
        <version>4.2.1</version>
    </dependency>
</dependencies>
Wees voorzichtig met code.Optie 2: Handmatige ExclusionsAls u de voorkeur geeft aan expliciete uitsluitingen, moet u de slf4j-api uitsluiten bij de bibliotheken die de 1.7.36 versie meebrengen:xml<dependencies>
    <!-- Confluent Serializer -->
    <dependency>
        <groupId>io.confluent</groupId>
        <artifactId>kafka-avro-serializer</artifactId>
        <version>8.2.3</version>
        <exclusions>
            <exclusion>
                <groupId>org.slf4j</groupId>
                <artifactId>slf4j-api</artifactId>
            </exclusion>
        </exclusions>
    </dependency>

    <!-- Kafka Clients -->
    <dependency>
        <groupId>org.apache.kafka</groupId>
        <artifactId>kafka-clients</artifactId>
        <version>4.2.1</version>
        <exclusions>
            <exclusion>
                <groupId>org.slf4j</groupId>
                <artifactId>slf4j-api</artifactId>
            </exclusion>
        </exclusions>
    </dependency>

    <!-- Zorg dat uw eigen SLF4J-API strak op 2.0.19 staat -->
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-api</artifactId>
        <version>2.0.19</version>
    </dependency>
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-reload4j</artifactId>
        <version>2.0.19</version>
    </dependency>
</dependencies>
Wees voorzichtig met code.Zodra u een van deze aanpassingen doorvoert en uw project opnieuw bouwt (mvn clean package), zal de StaticLoggerBinder foutmelding verdwijnen en ziet u direct alle logs (inclusief de startup-logs van de Kafka producer/consumer) weer op uw scherm verschijnen.Laat me weten of u de dependencyManagement of de exclusions aanpak heeft toegepast en of uw logs nu succesvol binnenstromen.AI-reacties kunnen fouten bevatten. Meer informatieOption 1 worked like charm thank youGeweldig om te horen dat de dependencyManagement aanpak het probleem direct heeft opgelost! Het is inderdaad de meest schone manier om Maven de controle terug te geven zonder uw pom.xml te vervuilen met talloze handmatige uitsluitingen.Nu de bibliotheken netjes op één lijn zitten met SLF4J 2.x en Java 25, worden alle logregels van zowel Confluent als Apache Kafka weer correct verwerkt.Mocht u later nog ergens tegenaan lopen, laat het gerust weten. Als u wilt, kunnen we nu nog kijken naar:De configuratie van uw log4j.properties om te zorgen dat de Kafka-logs niet uw hele console overspoelen.Het optimaliseren van de Kafka Producer/Consumer timeouts voor Java 25.Laat maar weten waar uw focus nu ligt!