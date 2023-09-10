#include <stdio.h>
#include <stdlib.h>
#include <math.h>
const int P = 96737;

int directGCD(int a, int b){
    if(a == 0){
		return b;
	}
	else if(b == 0){
		return a;
	}
    else{
        int biggerValue, smallerValue, remainder;
        if(a >= b){ //Find out larger one
			biggerValue = a;
			smallerValue = b;
		}else{//a < b
			biggerValue = b;
			smallerValue = a;
		}
        do{
            remainder = biggerValue % smallerValue; // Find remainder for current round

            /* Prepare for next round.*/
            biggerValue = smallerValue; // In next round, biggerValue is the smallerValue in the previous round
            smallerValue = remainder;    //smallerValue is the remainder of the previous round.

        }while(remainder != 0); // Stop looping until the remainder reaches 0
        return biggerValue;
    }


}
int PrimeCheck(unsigned long val){
    if(val == 0){//Not prime
        return 0;
    }
    else if(val == 2 || val == 1){
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
int createZPrimeP(int* ZPrimeP){
    int indexZPrimeP = 0;
    for(int i = 0; i < P; i++){
        if(directGCD(i,P) == 1){
            ZPrimeP[indexZPrimeP] = i;
            indexZPrimeP++;
        }
    }
    return indexZPrimeP;
}
int findGenerator(int* ZPrimeP){
    int generator = 2;
    double currentGtoJ;
    int currentModResult = 0;
    while(currentModResult != 1 && generator < P){
        //currentGtoJ = pow((double)generator,(double)(P - 1));
        currentModResult = modHelp(generator, P-1);
        printf("Trying generator %d. Current G^j is %Lf. Current ModResult is %d. \n",generator, currentGtoJ, currentModResult);
        generator++;
    }
    return generator;
}
int modHelp(int x, int j){
    if(j == 0){
        return (1 % P)*x%P;
    }
    else{
        return modHelp(x, j-1)* x % P;
    }
}
int invMod(int a, int m)
{
    for (unsigned long x = 1; x < m; x++){
        if (((a%m) * (x%m)) % m == 1){
            return x;
        }
    }
}
/*Q7C*/
int decryptElGamal(int c1, int c2){
    int x = fmod(pow(c1, a), P);
    int z = inv(x,p);
    int plaintext = c2 * z % P
}
int main()
{
    int ZPrimeP[P];
    int actualSizeZPrimeP = 0;
    actualSizeZPrimeP = createZPrimeP(ZPrimeP);
    /*for(int i = 0; i < actualSizeZPrimeP; i++){

        printf("ZPrimeP[%d] = %d. \n",i, ZPrimeP[i]);

    }*/
    int generator = findGenerator(ZPrimeP);
    printf("Generator found is %d. \n",generator);
    int decryptResult[6];
    decryptResult[0] = decryptElGamal(5000,5001);
    decryptResult[1] = decryptElGamal(10000,20000);
    decryptResult[2] = decryptElGamal(30000,40000);
    decryptResult[3] = decryptElGamal(50000,60000);
    decryptResult[4] = decryptElGamal(70000,80001);
    decryptResult[5] = decryptElGamal(90000,100000);
     for(int i = 0; i < 6; i++){

        printf("decryptResult[%d] = %d. \n",i, decryptResult[i]);

    }
    return 0;
}
