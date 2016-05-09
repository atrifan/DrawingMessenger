Etapa 2
Trifan Alexandru
342C3

Am implementat in mod complet UI-ul aplicatiei.
Aplicatia este sparta in mai multe componente de 2 tipuri:
- componente simple
- componente complexe

Componente simple = JButton, JTextPanel, JTree, etc
Componente complexe - care extind JFrame - si agrega in interiorul lor componente simple
- login
- toolbar
- userPanel
- groupPanel
- chat

Comunicarea intre componente se face prin enventuri.
Componenta principala "App" comunica cu celelalte componente si viceversa.
Desenarea se face in mod dinamic, la mouse click se incepe desenul iar la mouse release se finalizeaza desenul,
in tot acest timp figura este randadata pe ecran in functie de miscarea mouse-ului.

Sunt suportate toate culorile din Color pentru chat (luate prin reflection din Color - fieldurile statice). Iar
font size-urile sunt hardcodate.

Se poate observa posibilitatea de adaugare de useri in grupuri si de join(nu contine validari).

Tehnologii:
-----------
Java 1.7
Java swing + awt
Maven - packaging/build tool

Impachetare proiect pentru jar:
-------------------------------
mvn package --> jarul se va regasi in folderul target ( de aici se poate rula in terminal java -jar <nume_artefact>.jar
