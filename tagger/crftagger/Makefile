CC=gcc
ECHO=@echo
RM=rm

LIB=$(HOME)/local/lib
CCFLAGS=-fPIC -DHAVE_CONFIG_H -mfpmath=sse -msse2 -DUSE_SSE -O3 -fomit-frame-pointer -ffast-math -Winline -std=c99 -MT crftagger.o -MD -MP -c

ifeq ($(OS),Windows_NT)
	OSNAME=Windows
	TARGET=libcrftagger.dll

	LIB=$(HOME)/local/win/lib

	export CPATH=$(HOME)/local/win/include:/usr/java/latest/include:/usr/lib/jvm/java-1.8.0/include:/usr/lib/jvm/java-1.8.0/include/linux:/usr/java/latest/include/linux

else
	UNAME=$(shell uname -s)

	ifeq ($(UNAME),Darwin)
		OSNAME=MacOS
		TARGET=libcrftagger-osx.so
		OBJ=crftagger.o crfjni.o fmemopen.o iwa.o
		LIB_OPTIONS=-lcrfsuite -lcqdb -llbfgs

		export CPATH=$(HOME)/local/include:/System/Library/Frameworks/JavaVM.framework/Versions/Current/Headers
	else
		OSNAME=Linux
		TARGET=libcrftagger-linux.so
		OBJ=crftagger.o crfjni.o iwa.o
		LIB_OPTIONS=-l:libcrfsuite.a -l:libcqdb.a -l:liblbfgs.a

		export CPATH=$(HOME)/local/include:/usr/java/latest/include:/usr/lib/jvm/java-1.8.0/include:/usr/lib/jvm/java-1.8.0/include/linux:/usr/java/latest/include/linux
	endif
endif

%.o: %.c
	$(CC) $(CCFLAGS) $< -o $@

$(TARGET): $(OBJ)
	@rm -f $(LIB)/libcqdb*.dylib $(LIB)/libcqdb*.la
	@rm -f $(LIB)/libcrfsuite*.dylib $(LIB)/libcrfsuite*.la
	@rm -f $(LIB)/liblbfgs*.dylib $(LIB)/liblbfgs*.la

	$(CC) $^ -shared -o $@ -L$(LIB) $(LIB_OPTIONS)

all: $(TARGET)

clean:
	$(RM) -f *.so *.o *.d
