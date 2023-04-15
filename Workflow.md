Workflow
==============
Installation de l'environement de développement
-------------------
1. Installer Eclipse et Eclipe JEE
2. Installer PostgreSQL
2. Télécharger OpenXava ; la version 5.4 (https://sourceforge.net/projects/openxava/files/openxava/5.4/) est recommandée mais la version 5.5.1 a l'air de fonctionner. Extraire OpenXava dans un dossier.
3. Ouvrir Eclipse sur le workspace OpenXava.
4. Afficher la perspective Git, cloner le repository https://github.com/florianhof/SCIS.git en spécifiant , au 3ème écran, la destination comme le workspace OpenXava et la branche initiale (pour le moment nommée "develop").
5. Créer une nouvelle branche, checkout cette branche
6. Importer le clone come projet dans Eclipse.
7. Repasser dans la perspective Java, ouvrir la vue Ant (Window -> Show view -> Ant). La vue Ant apparaît sur la droite, y glisser le fichier SCIS\build.xml. Ouvrir SCIS dans la vue Ant et double-cliquer sur generatePortlets
8. (Obtenir et) copier les fichierx jxl.jar et postgresql-xxx.jar dans SCIS\web\WEB-INF\lib
9. Refresh (F5) du projet SCIS
10. Passer en perspective JEE, dans la vue servers créer un nouveau server Tomcat 7 depuis le dossier Tomcat fourni avec OpenXava
11. Dans le dossier servers du Project Explorer sur la gauche, ouvrir Tomcat v7.0.../context.xml et y ajouter comme ressource :
name="jdbc/SCISDS" 
          auth="Container" 
          type="javax.sql.DataSource"
          driverClassName="org.postgresql.Driver"
          org.hibernate.dialect="org.hibernate.dialect.PostgreSQLDialect"
          maxActive="20" maxIdle="5" maxWait="10000"
          url="jdbc:postgresql://localhost/sgharchiv"
          username="sgharchiv_dev_scis" password="Merci5mitmachen" 
12. Ouvrir pgAdmin, exécuter un-à-un les scripts du dossier persistence/scripts (sauf la première partie de generateSchema avant "create table COMMUNE") et 
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO sgharchiv_dev_scis;
