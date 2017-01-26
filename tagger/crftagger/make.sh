mkdir -p .deps

JNI_INCLUDE=/System/Library/Frameworks/JavaVM.framework/Versions/Current/Headers
FRONTEND=../crfsuite/frontend

gcc -DHAVE_CONFIG_H -I$FRONTEND -mfpmath=sse -msse2 -DUSE_SSE -O3 -fomit-frame-pointer -ffast-math -Winline -std=c99 -MT crftagger.o -MD -MP -MF .deps/crftagger.Tpo -c -o crftagger.o `test -f 'crftagger.c' || echo './'`crftagger.c
gcc -DHAVE_CONFIG_H -I$JNI_INCLUDE -mfpmath=sse -msse2 -DUSE_SSE -O3 -fomit-frame-pointer -ffast-math -Winline -std=c99 -MT crfjni.o -MD -MP -MF .deps/crfjni.Tpo -c -o crfjni.o `test -f 'crfjni.c' || echo './'`crfjni.c
gcc -DHAVE_CONFIG_H -mfpmath=sse -msse2 -DUSE_SSE -O3 -fomit-frame-pointer -ffast-math -Winline -std=c99 -MT fmemopen.o -MD -MP -MF .deps/fmemopen.Tpo -c -o fmemopen.o `test -f 'fmemopen.c' || echo './'`fmemopen.c
gcc -DHAVE_CONFIG_H -mfpmath=sse -msse2 -DUSE_SSE -O3 -fomit-frame-pointer -ffast-math -Winline -std=c99 -MT iwa.o -MD -MP -MF .deps/iwa.Tpo -c -o iwa.o `test -f '$FRONTEND/iwa.c' || echo './'`$FRONTEND/iwa.c

gcc -shared -o libcrftagger.so crftagger.o crfjni.o fmemopen.o iwa.o -L/usr/local/lib -lcrfsuite -llbfgs -lcqdb
