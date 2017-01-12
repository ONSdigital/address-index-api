#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "iwa.h"
#include "crftagger.h"

static int format_result(
    char *buffer,
    crfsuite_tagger_t *tagger,
    const crfsuite_instance_t *inst,
    int *output,
    crfsuite_dictionary_t *labels)
{
    int p = 0;
    int i;
    
    for (i = 0; i < inst->num_items; ++i) {
        const char *label = NULL;
        floatval_t prob;

        labels->to_string(labels, output[i], &label);
        tagger->marginal_point(tagger, output[i], i, &prob);

        p += sprintf(buffer + p, "%s: %f\n", label, prob);

        labels->free(labels, label);
    }

    return p;
}

int tag(const char *items, crfsuite_model_t* model, char *buffer)
{
    int N = 0, L = 0, ret = 0, lid = -1;

    int p = 0;

    crfsuite_instance_t inst;
    crfsuite_item_t item;
    crfsuite_attribute_t cont;

    char *comment = NULL;

    iwa_t* iwa = NULL;
    const iwa_token_t* token = NULL;

    crfsuite_tagger_t *tagger = NULL;
    crfsuite_dictionary_t *attrs = NULL, *labels = NULL;

    /* Obtain the dictionary interface representing the labels in the model */
    ret = model->get_labels(model, &labels);

    if (ret) {
        goto force_exit;
    }

    /* Obtain the dictionary interface representing the attributes in the model */
    ret = model->get_attrs(model, &attrs);
    
    if (ret) {
        goto force_exit;
    }

    /* Obtain the tagger interface */
    ret = model->get_tagger(model, &tagger);

    if (ret) {
        goto force_exit;
    }

    /* Initialize the objects for instance and evaluation */
    L = labels->num(labels);

    crfsuite_instance_init(&inst);

    /* Create an IWA reader */
    iwa = iwa_string_reader(items);

    if (iwa == NULL) {
        ret = 2;
        goto force_exit;
    }

    /* Read the input data and assign labels */
    while (token = iwa_read(iwa), token != NULL) {
        switch (token->type) {
        case IWA_BOI:
            /* Initialize an item */
            lid = -1;
            crfsuite_item_init(&item);
            
            free(comment);
            comment = NULL;
            break;

        case IWA_EOI:
            /* Append the item to the instance */
            crfsuite_instance_append(&inst, &item, lid);
            crfsuite_item_finish(&item);
            break;

        case IWA_ITEM:
            if (lid == -1) {
                /* The first field in a line presents a label */
                lid = labels->to_id(labels, token->attr);

                if (lid < 0) {
                    lid = L;    /* #L stands for a unknown label */
                }                
            } else {
                /* Fields after the first field present attributes */
                int aid = attrs->to_id(attrs, token->attr);

                /* Ignore attributes 'unknown' to the model */
                if (0 <= aid) {
                    /* Associate the attribute with the current item */
                    if (token->value && *token->value) {
                        crfsuite_attribute_set(&cont, aid, atof(token->value));
                    } else {
                        crfsuite_attribute_set(&cont, aid, 1.0);
                    }

                    crfsuite_item_append_attribute(&item, &cont);
                }
            }

            break;

        case IWA_NONE:
        case IWA_EOF:
            if (!crfsuite_instance_empty(&inst)) {
                /* Initialize the object to receive the tagging result */
                floatval_t score = 0;

                int *output = calloc(sizeof(int), inst.num_items);

                /* Set the instance to the tagger */
                if ((ret = tagger->set(tagger, &inst))) {
                    goto force_exit;
                }

                /* Obtain the viterbi label sequence */
                if ((ret = tagger->viterbi(tagger, output, &score))) {
                    goto force_exit;
                }

                ++N;

                p += format_result(buffer + p, tagger, &inst, output, labels);

                free(output);
                output = NULL;

                crfsuite_instance_finish(&inst);
            }
            break;
        }
    }

force_exit:
    /* Close the IWA parser */
    iwa_delete(iwa);
    iwa = NULL;

    free(comment);
    comment = NULL;

    crfsuite_instance_finish(&inst);

    SAFE_RELEASE(tagger);
    SAFE_RELEASE(attrs);
    SAFE_RELEASE(labels);

    return ret;
}
