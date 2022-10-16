<h1>Duels</h1> 

[![](https://jitpack.io/v/Realizedd/Duels.svg)](https://jitpack.io/#Realizedd/Duels)

A duel plugin for spigot. <a href="https://www.spigotmc.org/resources/duels.20171/">Spigot Project Page</a>

---

* **[Wiki](https://github.com/Realizedd/Duels/wiki)**
* **[Commands](https://github.com/Realizedd/Duels/wiki/commands)**
* **[Permissions](https://github.com/Realizedd/Duels/wiki/permissions)**
* **[Placeholders](https://github.com/Realizedd/Duels/wiki/placeholders)**
* **[Extensions](https://github.com/Realizedd/Duels/wiki/extensions)**
* **[config.yml](https://github.com/Realizedd/Duels/blob/master/duels-plugin/src/main/resources/config.yml)**
* **[lang.yml](https://github.com/Realizedd/Duels/blob/master/duels-plugin/src/main/resources/lang.yml)**
* **[Support Discord](https://discord.gg/RNy45sg)**


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
compile group: 'com.github.Realizedd.Duels', name: 'duels-api', version: '3.5.1'
```  

Maven:
```xml
<dependency>
    <groupId>com.github.Realizedd.Duels</groupId>
    <artifactId>duels-api</artifactId>
    <version>3.5.1</version>
    <scope>provided</scope>
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