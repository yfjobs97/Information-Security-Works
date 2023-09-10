#include <stdio.h>
#include <stdlib.h>
#include <math.h>


const unsigned long N = 60529591;

int PrimeCheck(unsigned long);

void Q6A(unsigned long* , unsigned long* , unsigned long*);
unsigned long invMod(unsigned long, unsigned long);
unsigned long decryptRSA(unsigned long, unsigned long);
unsigned long firstDivisor(unsigned long);


unsigned long decryptHelp(unsigned long, unsigned long);

int PrimeCheck(unsigned long val){
    if(val == 2){
        return 1;//Is prime
    }
    else{
        for(unsigned long i= 2; i * i <= val; i++){
            if(val % i == 0){
                return 0;//Not prime
            }
        }
        //printf("PrimeCheck: value passed in (%lu) is a prime.\n",val);
        return 1;
    }


}

void Q6A(unsigned long* p_ptr, unsigned long* q_ptr, unsigned long* phiN_ptr){
    unsigned long p;
    unsigned long q;
    unsigned long phiN;

    for(q = 2; q <= N; q++){//Find the first pair of prime numbers divisible by N
        if( PrimeCheck(q) ){//If q is prime
            if( N % q == 0 && PrimeCheck(N / q) ){//and that N mod q is 0 and N/q is a prime
                p = N / q;
                phiN = (p-1)*(q-1);
                printf("p = %lu, q = %lu, phiN = %lu \n",p, q, phiN);
                *p_ptr = p;
                *q_ptr = q;
                *phiN_ptr = phiN;
                return;
            }
        }
    }
    printf("Error encountered during p, q generation.\n");

}

//Assuming ax mod m for Q6B
unsigned long invMod(unsigned long a, unsigned long m)
{
    for (unsigned long x = 1; x < m; x++){
        if (((a%m) * (x%m)) % m == 1){
            return x;
        }
    }
}

unsigned long decryptRSA(unsigned long ciphertext, unsigned long d){
    unsigned long plaintext;
    plaintext = decryptHelp(ciphertext, d);
    return plaintext;
}
unsigned long decryptHelp(unsigned long M, unsigned long d){
    if(d == 0){
        return (1 % N)* M % N;
    }
    else{
        return decryptHelp(M, d-1)* M % N;
    }
}

int main()
{

    unsigned long p = 0;
    unsigned long q = 0;
    unsigned long phiN = 0;

    Q6A(&p, &q, &phiN);
    printf("p = %lu, q = %lu, phiN = %lu \n",p, q, phiN);
    /*Q6B*/
    unsigned long e = 31;
    unsigned long d = invMod(e, phiN);
    printf("e = %lu, d = %lu \n",e, d);
    /*Q6D, not successful*/
    unsigned long ciphertext[] = {10000, 20000, 30000, 40000, 50000, 60000};
    for(int i = 0; i < 6; i++){
        long double plaintext = decryptRSA(ciphertext, d);
        printf("For ciphertext = %lu, plaintext = %lu  \n",ciphertext[i], plaintext);
    }
    return 0;
}
