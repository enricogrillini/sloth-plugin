# Sloth plugin

## Goal: `form`

Genera le classi nei package:
 - controller
 - controllerBaseLogic
 - form

Parametri:
 - `formDirectory` 
   - Default: `${project.basedir}/form`
   - Directory in cui sono presenti i file form.xml
 - `outputJavaDirectory`
   - Default: `${project.build.directory}/generated-sources/sloth`
   - Directory in cui sono generati i sorgenti
 - `genPackage`
   - Example: `it.itdistribuzione.sloth.gen`
   - Nome del package di generazione
   
## Goal: `spring`


## Goal: `bean`
Genera le classi nei package:
 - bean - Bean per l'acceso base al DB

Parametri:
 - `dbSchema` 
   - Default: `db/dbSchema.xml`
   - File in cui sono salvate le specifiche DB
 - `outputJavaDirectory`
   - Default: `${project.build.directory}/generated-sources/sloth`
   - Directory in cui sono generati i sorgenti
 - `genPackage`
   - Example: `it.itdistribuzione.sloth.gen`
   - Nome del package di generazione

## Goal: `refreshdb`
Parametri:
 - `dbSchema` 
   - Default: `db/dbSchema.xml`
   - File in cui sono salvate le specifiche DB

## Changelog
### Versione 2.0.0
 - refreshdb - Aggiornato dbSchema.xsd per gestione connessione DB