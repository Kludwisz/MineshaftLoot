# MineshaftLoot

Repo for generating mineshafts and simulating mineshaft decoration features (pre 1.18 only) such as chest loot, cobwebs, etc.  
The code doesn't take lava lakes, water lakes, and caves into account, so the results aren't always accurate.
Requires seedfinding libraries by Neil and Kaptainwutax to run (https://github.com/SeedFinding).

Mineshaft generator by Gaider10  
Theoretical support: DylanDC14  

# Installation
`build.gradle`:
```
repositories {
    mavenCentral()
    maven { url "https://maven.seedfinding.com" }
    maven { url "https://www.jitpack.io" }
}

dependencies {
    implementation('com.seedfinding:mc_math:1.171.0') { transitive = false }
    implementation('com.seedfinding:mc_seed:1.171.1') { transitive = false }
    implementation('com.seedfinding:mc_core:1.210.0') { transitive = false }
    implementation('com.seedfinding:mc_noise:1.171.1') { transitive = false }
    implementation('com.seedfinding:mc_reversal:1.171.1') { transitive = false }
    implementation('com.seedfinding:mc_biome:1.171.1') { transitive = false }
    implementation('com.seedfinding:mc_terrain:1.171.1') { transitive = false }
    implementation('com.seedfinding:mc_feature:1.171.9') { transitive = false }
	
    implementation('com.github.kludwisz:MineshaftLoot:1.3')
}
```
