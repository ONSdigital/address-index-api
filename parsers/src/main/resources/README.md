The shared objects are built from C code in the ONSdigital repository:

## Build Lib LBFGS:
```
git clone https://github.com/ONSdigital/liblbfgs.git
cd liblbfgs
./autogen.sh
./configure --prefix=$HOME/local
make
make install
```
## Build CRFSuite:
```
git clone https://github.com/ONSdigital/crfsuite.git
cd crfsuite
./autogen.sh
./configure --prefix=$HOME/local --with-liblbfgs=$HOME/local
make
make install
```
## Build Shim for OSX or Linux:
```
git clone https://github.com/ONSdigital/crftagger.git
cd crftagger
make clean
make
```

## Build Shim for Windows:
```
git clone https://github.com/ONSdigital/crftagger.git
cd crftagger
make clean
OS="Windows" make
```
