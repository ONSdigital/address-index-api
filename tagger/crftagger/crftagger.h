#ifndef _CRFTAGGER_H
#define _CRFTAGGER_H

#include <crfsuite.h>

#define SAFE_RELEASE(obj) if ((obj) != NULL) { (obj)->release(obj); (obj) = NULL; }

int tag(const char *items, crfsuite_model_t* model, char *buffer);

#endif
