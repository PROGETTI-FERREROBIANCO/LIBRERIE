/*
standard library of Gabriele Ferrero
*/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>
#include <math.h>


#define and &&
#define or ||
#define is ==
#define not !=
#define reverse =!



int ctoi(char *carattere){      // funzione per convertire una stringa contenente un numero intero

    int numero_finale=0;
    int numero_volte_carattere_meno_inserito=0;
    int numero_cifre_numero=0;
    int i=0;

    for(numero_cifre_numero=0; (*(carattere+numero_cifre_numero))!='\0'; numero_cifre_numero++){}

    for(i=0; (((*(carattere+i))=='-') || ((*(carattere+i))=='0') || ((*(carattere+i))=='1') || ((*(carattere+i))=='2') || ((*(carattere+i))=='3') || ((*(carattere+i))=='4') || ((*(carattere+i))=='5') || ((*(carattere+i))=='6') || ((*(carattere+i))=='7') || ((*(carattere+i))=='8') || ((*(carattere+i))=='9')) && ((*(carattere+i))!='\0'); i++){

        numero_finale=numero_finale*10;

        switch (*(carattere+i)){
        case '-':
        numero_volte_carattere_meno_inserito++;
        break;
        case '0':
        numero_finale=numero_finale+0;
        break;
        case '1':
        numero_finale=numero_finale+1;
        break;
        case '2':
        numero_finale=numero_finale+2;
        break;
        case '3':
        numero_finale=numero_finale+3;
        break;
        case '4':
        numero_finale=numero_finale+4;
        break;
        case '5':
        numero_finale=numero_finale+5;
        break;
        case '6':
        numero_finale=numero_finale+6;
        break;
        case '7':
        numero_finale=numero_finale+7;
        break;
        case '8':
        numero_finale=numero_finale+8;
        break;
        case '9':
        numero_finale=numero_finale+9;
        break;
        default:
        break;
        }
    }

    if(numero_volte_carattere_meno_inserito==1){
        return numero_finale*(-1);
    }else if(numero_volte_carattere_meno_inserito==0){
        return numero_finale;
    }else{
        
    }

}



#define max_caratteri_per_stringa 3000

int scanfInt(char stringa[max_caratteri_per_stringa]){      //funzione per il la stampa di una stringa e l'acquisizione di un int
    int numero;
    printf("%s", stringa);
    scanf("%d", &numero);
    return numero;
}

char scanfChar(char stringa[max_caratteri_per_stringa]){      //funzione per il la stampa di una stringa e l'acquisizione di un char
    char carattere;
    printf("%s", stringa);
    scanf("%*c");
    scanf("%c", &carattere);
    return carattere;
}

float scanfFloat(char stringa[max_caratteri_per_stringa]){      //funzione per il la stampa di una stringa e l'acquisizione di un float
    float numero_con_virgola;   
    printf("%s", stringa);
    scanf("%f", &numero_con_virgola);
    return numero_con_virgola;
}

char* scanfString(char stringa[max_caratteri_per_stringa]){      //funzione per il la stampa di una stringa e l'acquisizione di una stringa
    char ch;
    int i=0;
    char* string=(char*)malloc(10000*sizeof(char));
    printf("%s", stringa);
    for(i=0; (ch=getchar())!='\n' ; i++){
        (*(string+i))=ch;
    }
    (*(string+i))='\0';

    return string;
}
