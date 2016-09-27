# OPL_Pull_Request_Engineering

Utilisation : 
1ere etape :
- lancement du .jar (site web à terme ?) avec identifiant du projet github.
- (création du fichier de config checkstyle dans le projet github)
- (création du webhook sur les pull_request du projet github)

2eme etape : 
- Un contributeur crée une PR sur le projet github.

3eme etape :
- L'outil récupère les infos de la PR (code...)
- L'outil controle de style **sur le code modifié par la PR**.
- L'outil ajout un commentaire à la PR (checkstyle OK, KO et pourquoi).

Mise en place :
- Langage vérifié = Java

Outils externes:
- Checkstyle

Evaluation :
Faire un clone d'un gros projet et tester l'execution de l'outil avec différentes PR sur celui-ci.
Comparer le temps d'execution de checkstyle sur le projet en entier et notre solution.
