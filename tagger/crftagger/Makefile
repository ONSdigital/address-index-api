CC=gcc
ECHO=@echo
RMF=rm -f

LIB=$(HOME)/local/lib
CCFLAGS=-fPIC -DHAVE_CONFIG_H -mfpmath=sse -msse2 -DUSE_SSE -O3 -fomit-frame-pointer -ffast-math -Winline -std=c99 -MT crftagger.o -MD -MP -c

ifeq ($(OS),Windows)
	CC=x86_64-w64-mingw32-gcc
	TARGET=libcrftagger.dll
	LIB=$(HOME)/local/win/lib

	export CPATH=$(HOME)/local/win/include:/usr/java/latest/include:/usr/java/latest/include/linux
else
	UNAME=$(shell uname -s)
	
	# NOTE: on OSX / Linux, dynamic CRFSuite libs can get in the way so we remove them.
	RMF_DYNAMIC_LIBS= $(RMF) \
		$(LIB)/libcqdb*.dylib $(LIB)/libcqdb*.la \
		$(LIB)/libcrfsuite*.dylib $(LIB)/libcrfsuite*.la \
		$(LIB)/liblbfgs*.dylib $(LIB)/liblbfgs*.la

	OBJ=crftagger.o crfjni.o iwa.o

	ifeq ($(UNAME),Darwin)
		TARGET=libcrftagger-osx.so
		LIB_OPTIONS=-lcrfsuite -lcqdb -llbfgs

		export CPATH=$(HOME)/local/include:/System/Library/Frameworks/JavaVM.framework/Versions/Current/Headers
	else
		TARGET=libcrftagger.so
		LIB_OPTIONS=-l:libcrfsuite.a -l:libcqdb.a -l:liblbfgs.a

		export CPATH=$(HOME)/local/include:/usr/java/latest/include:/usr/java/latest/include/linux
	endif
endif

%.o: %.c
	$(CC) $(CCFLAGS) $< -o $@

$(TARGET): $(OBJ)
	$(RM_DYNAMIC_LIBS)
	$(CC) $^ -shared -o $@ -L$(LIB) $(LIB_OPTIONS)

all: $(TARGET)

clean:
	$(RMF) *.so *.dll *.o *.d
