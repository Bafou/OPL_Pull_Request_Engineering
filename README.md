# OPL_Pull_Request_Engineering


Utilisation : 
- 1ere etape : lancement du .jar (site web à terme ?) avec identifiant du projet github.
- Un contributeur crée une PR sur le projet github.
- L'outil récupère les infos de la PR (code...)
- L'outil controle de style.
- L'outil ajout un commentaire à la PR (checkstyle OK, KO et pourquoi).


Mise en place :
- Java
- Fichier de configuration des règles (xml, json) ajouter dans le projet Github


Outils externes:
- Checkstyle
- PMD

Fonctionnalité principale :
- Execution du controle de style seulement sur le code modifié par la PR.

Evaluation :

