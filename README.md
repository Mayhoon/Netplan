# Netzplan rechner
Ein Rechner der den kritischen Pfad fuer ein gegebenes Netz errechnet.

Es gibt zwei Arten dem Rechner die Prozesse zu uebergeben,
manuell oder in Form einer Tabelle:

A	3	C,D,E
B	1	G
C	7	B
E	4	F
D	5	B
F	2	G
G	10	-

Diese kann der Rechner mit der import Funktion loesen.
Dafuer den gesamten Dateipfad ohne die .xls Endung angeben.
Nur alte Excel Dateien im .xls Format werden verarbeitet.

Wichtig fuer beide Arten ist die Angabe ob zu jedem Prozess der vorherige oder der nachfolgende Prozess bekannt ist.
Je nach dem muss dann auch die Tabelle oder manuell der Input angegeben werden.

Ist ein nachfolgender oder vorheriger Prozess nicht bekannt muss NONE angegeben werden.
