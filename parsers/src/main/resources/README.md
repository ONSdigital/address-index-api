The shared objects are built from C code in the ONSdigital repository:

##Build Lib LBFGS:
```
git clone https://github.com/chokkan/liblbfgs.git
cd liblbfgs
./autogen.sh
./configure --prefix=$HOME/local
make
make install
```
##Build CRFSuite:
```
git clone https://github.com/chokkan/crfsuite.git
cd crfsuite
./autogen.sh
./configure --prefix=$HOME/local --with-liblbfgs=$HOME/local
make
make install
```
##Build Shim:
```
git clone https://github.com/ONSdigital/address-index-api.git
cd tagger/crftagger
make clean
make
```
