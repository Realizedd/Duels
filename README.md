<h1>Duels | 1.17+ Fork</h1> 

[![](https://jitpack.io/v/Realizedd/Duels.svg)](https://jitpack.io/#Realizedd/Duels)

A 1.17+ Fork of Duels. <a href="https://www.spigotmc.org/resources/duels.20171/">Original Spigot Project Page</a>

---

* **[Original Project](https://github.com/Realizedd/Duels)**


### Getting the dependency

#### Repository
Gradle:
```groovy
maven {
    name 'jitpack-repo'
    url 'https://jitpack.io'
}
```

Maven:
```xml
<repository>
  <id>jitpack-repo</id>
  <url>https://jitpack.io</url>
</repository>
```

#### Dependency
Gradle:
```groovy
compile (group: 'com.github.Realizedd.Duels', name: 'duels-api', version: '3.4.1') {
    transitive = false
}
```  

Maven:
```xml
<dependency>
    <groupId>com.github.Realizedd.Duels</groupId>
    <artifactId>duels-api</artifactId>
    <version>3.4.1</version>
    <exclusions>
        <exclusion>
            <groupId>*</groupId>
            <artifactId>*</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

### plugin.yml
Add Duels as a soft-depend to ensure Duels is fully loaded before your plugin.
```yaml
soft-depend: [Duels]
```

### Getting the API instance

```java
@Override
public void onEnable() {
  Duels api = (Duels) Bukkit.getServer().getPluginManager().getPlugin("Duels");
}
```