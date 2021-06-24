# What is this
A Repo that has the following snippets
- Allocate X amount of data to each user - pkg: `allocateData`
- Understand `exitBlockOnFail` with `forever` loop - pkg: `foreverExitBlock`


## Run
```bash
mvn clean gatling:test -Dgatling.simulationClass={pckgName}.{simulationName}
```