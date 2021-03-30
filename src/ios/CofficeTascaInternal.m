//
//  CofficeTascaInternal.m
//  Tasca Test
//
//  Created by Adriano Tornatore on 28/08/17.
//  Copyright © 2017 Coffice srl. All rights reserved.
//

#import "CofficeTascaInternal.h"

@implementation CofficeTascaInternal




+ (CofficeTascaInternal*)sharedInstance
{
    static dispatch_once_t predicate = 0;
    __strong static id sharedObject = nil;
    //static id sharedObject = nil;  //if you're not using ARC
    dispatch_once(&predicate, ^{
        sharedObject = [[self alloc] init];
        //sharedObject = [[[self alloc] init] retain]; // if you're not using ARC
        [[TascaDBHelper sharedInstance] checkDB];
        
    });
    return sharedObject;
}


- (void)dealloc {
    // Should never be called, but just here for clarity really.
}



#pragma mark - Utility

- (NSDictionary *)getEstateByID:(long)estateID forLanguage:(NSString *)lang{

    return [[TascaDBHelper sharedInstance] getEstateByID:estateID
                                             forLanguage:lang];

}

- (NSDictionary *)getTasteByID:(long)tasteID forLanguage:(NSString *)lang{

    return [[TascaDBHelper sharedInstance] getTasteByID:tasteID
                                            forLanguage:lang];

}

- (NSDictionary *)getWineByID:(long)wineID forLanguage:(NSString *)lang{

    return [[TascaDBHelper sharedInstance] getWineByID:wineID
                                           forLanguage:lang];

}

- (NSArray *)getEstatesTypeAhead:(NSString *)term forLanguage:(NSString *)lang{

    return [[TascaDBHelper sharedInstance] getEstatesTypeAhead:term
                                                   forLanguage:lang];

}

- (NSArray *)getTastesTypeAhead:(NSString *)term{

    return [[TascaDBHelper sharedInstance] getTastesTypeAhead:term];

}

- (NSArray *)getWinesTypeAhead:(NSString *)term forLanguage:(NSString *)lang{

    return [[TascaDBHelper sharedInstance] getWinesTypeAhead:term
                                                 forLanguage:lang];

}

- (NSArray *)getEstates:(NSString *)term
            forLanguage:(NSString *)lang
                  limit:(int)limit
                 offset:(int)offset
                   sort:(NSString *)sort
          sortDirection:(BOOL)sortDirection{


    return [[TascaDBHelper sharedInstance] getEstates:term
                                          forLanguage:lang
                                                limit:limit
                                               offset:offset
                                                 sort:sort
                                        sortDirection:sortDirection];

}

- (NSArray *)getTastes:(NSString *)term
                 limit:(int)limit
                offset:(int)offset
                  sort:(NSString *)sort
         sortDirection:(BOOL)sortDirection{


    return [[TascaDBHelper sharedInstance] getTastes:term
                                               limit:limit
                                              offset:offset
                                                sort:sort
                                       sortDirection:sortDirection];

}

- (NSArray *)getWines:(NSString *)term
          forLanguage:(NSString *)lang
                limit:(int)limit
               offset:(int)offset
                 sort:(NSString *)sort
        sortDirection:(BOOL)sortDirection{


    return [[TascaDBHelper sharedInstance] getWines:term
                                        forLanguage:lang
                                              limit:limit
                                             offset:offset
                                               sort:sort
                                      sortDirection:sortDirection];

}

- (long)insertEstate:(NSDictionary *)estateDictionary{

    [[TascaDBHelper sharedInstance] insertEstate:estateDictionary];

    return 0;
}

- (long)insertTaste:(NSDictionary *)tasteDictionary{

    [[TascaDBHelper sharedInstance] insertTaste:tasteDictionary];

    return 0;
}

- (long)insertWine:(NSDictionary *)wineDictionary{

    [[TascaDBHelper sharedInstance] insertWine:wineDictionary];

    return 0;
}

- (BOOL)editEstate:(long)itemID data:(NSDictionary *)estateDictionary{

    return [[TascaDBHelper sharedInstance] updateEstate:itemID data:estateDictionary];

}

- (BOOL)editTaste:(long)itemID data:(NSDictionary *)tasteDictionary{

    return [[TascaDBHelper sharedInstance] updateTaste:itemID data:tasteDictionary];

}

- (BOOL)editWine:(long)itemID data:(NSDictionary *)wineDictionary{

    return [[TascaDBHelper sharedInstance] updateWine:itemID data:wineDictionary];

}

- (BOOL)deleteEstate:(long)itemID{

    return [[TascaDBHelper sharedInstance] deleteEstate:itemID];

}

- (BOOL)deleteTaste:(long)itemID{

    return [[TascaDBHelper sharedInstance] deleteTaste:itemID];

}

- (BOOL)deleteWine:(long)itemID{

    return [[TascaDBHelper sharedInstance] deleteWine:itemID];

}


- (int)clearEstates{

    [[TascaDBHelper sharedInstance] clearTableEstates];

    return 0;
}

- (int)clearTastes{

    [[TascaDBHelper sharedInstance] clearTableTastes];

    return 0;
}

- (int)clearWines{

    [[TascaDBHelper sharedInstance] clearTableWines];

    return 0;

}

- (int)clearImages{

    [[TascaDBHelper sharedInstance] clearTableImages];

    return 0;

}

- (int)clearCache{

    [[TascaDBHelper sharedInstance] clearDownloads];

    return 0;
}

- (int)clearAll{

    [[TascaDBHelper sharedInstance] clearAllData];

    return 0;
}

- (void)downloadAll:(NSString *)lang
     downloadImages:(BOOL)downloadImages
  stepProgressBlock:(ProgressBlock)stepProgress
currentProgressBlock:(ProgressBlock)currentProgress
        finishBlock:(CompletionBlock)finishBlock{
    if (stepProgress == nil) {
        stepProgress = ^(float progress, int current, int total, int step) {
            NSLog(@"progress: %f, %d, %d", progress,current,total);
        };
    }
    if (currentProgress == nil) {
        currentProgress  = ^(float progress, int current, int total, int step) {
            NSLog(@"progress: %f, %d, %d", progress,current,total);
        };
    }
    [[TascaRequestManager sharedInstance] downloadAll:lang
                                       downloadImages:downloadImages
                                    stepProgressBlock:stepProgress
                                 currentProgressBlock:currentProgress
                                          finishBlock:finishBlock];

}
- (void)downloadAllWithMedia:(NSString *)lang
           stepProgressBlock:(ProgressBlock)stepProgress
        currentProgressBlock:(ProgressBlock)currentProgress
                 finishBlock:(CompletionBlock)finishBlock{

    if (stepProgress == nil) {
        stepProgress = ^(float progress, int current, int total,int step) {
            NSLog(@"progress: %f, %d, %d", progress,current,total);
        };
    }
    if (currentProgress == nil) {
        currentProgress  = ^(float progress, int current, int total,int step) {
            NSLog(@"progress: %f, %d, %d", progress,current,total);
        };
    }
    [[TascaRequestManager sharedInstance] downloadAll:lang
                                       downloadImages:YES
                                    stepProgressBlock:stepProgress
                                 currentProgressBlock:currentProgress
                                          finishBlock:finishBlock];

}

- (void)downloadAllMedia:(NSArray *)array
    currentProgressBlock:(ProgressBlock)progressBlock
             finishBlock:(CompletionBlock)finishBlock{

    if (progressBlock == nil) {
        progressBlock  = ^(float progress, int current, int total,int step) {
            NSLog(@"progress: %f, %d, %d", progress,current,total);
        };
    }
    [[TascaRequestManager sharedInstance] downloadAllMedia:array
                                      currentProgressBlock:progressBlock
                                               finishBlock:finishBlock];

}

- (void)checkWinesUpdate:(NSString *)lang finishBlock:(CompletionBlock)finishBlock{

    [[TascaRequestManager sharedInstance] checkWinesLastUpdate:lang
                                                   finishBlock:finishBlock];

}

- (void)downloadWines:(NSString *)lang
       downloadImages:(BOOL)downloadImages
 currentProgressBlock:(ProgressBlock)progressBlock
          finishBlock:(CompletionBlock)finishBlock{
    if (progressBlock == nil) {
        progressBlock  = ^(float progress, int current, int total,int step) {
            NSLog(@"progress: %f, %d, %d", progress,current,total);
        };
    }
    [[TascaRequestManager sharedInstance] downloadWines:lang
                                         downloadImages:downloadImages
                                   currentProgressBlock:progressBlock
                                            finishBlock:finishBlock];

}

- (void)checkEstatesUpdate:(NSString *)lang finishBlock:(CompletionBlock)finishBlock{

    [[TascaRequestManager sharedInstance] checkEstatesLastUpdate:lang
                                                     finishBlock:finishBlock];

}
- (void)downloadEstates:(NSString *)lang
         downloadImages:(BOOL)downloadImages
   currentProgressBlock:(ProgressBlock)progressBlock
            finishBlock:(CompletionBlock)finishBlock{
    if (progressBlock == nil) {
        progressBlock  = ^(float progress, int current, int total,int step) {
            NSLog(@"progress: %f, %d, %d", progress,current,total);
        };
    }
    [[TascaRequestManager sharedInstance] downloadEstates:lang
                                            downloadImage:downloadImages
                                     currentProgressBlock:progressBlock
                                              finishBlock:finishBlock];
    
}

- (NSString *)getImageByUrl:(NSString *)imageUrl finishBlock:(CompletionBlock)finishBlock{
    return [self getImageByUrl:imageUrl overrideCache:NO finishBlock:finishBlock];
}
- (NSString *)getImageByUrl:(NSString *)imageUrl overrideCache:(BOOL)overrideCache finishBlock:(CompletionBlock)finishBlock{
    if (finishBlock == nil) {
        finishBlock  = ^(id obje) {
            NSLog(@"Finis: %@", obje);
        };
    }
    [[TascaRequestManager sharedInstance] downloadMedia:imageUrl disableCache:overrideCache finishBlock:finishBlock];
    return imageUrl;
}

- (NSArray*)getMediaMap {
    return [[TascaDBHelper sharedInstance] getImages];
}



/*
 
 getWineByID si commenta da sola ritorna l'intero oggetto json
 getEstateByID si commenta da sola ritorna l'intero oggetto json
 clearWines svuota la tabella vini
 clearTenute svuota la tabella tenute
 clearDegustazioni svuota la tabella degustazioni
 clearCache svuota la cartella dove vengono scaricate le immagini
 clearAll(clearImages: true|false) : richiama in sequenza i metodi clear* se clearImages è true chiama anche clearCache, altrimenti elimina solo i dati testuali.
 downloadAll: funzione che avvia il retrieve di tutti i vini e di tutte le tenute, deve avere 2 listener (progress e finished) - vedi singole gestione per il download di vini e tenute,
 scarica tutti i vini ivi incluse le immagini (non i video)
 scarica tutte le tenute ivi incluse le immagini (non i video)
 espone 2 listener PROGRESS e FINISHED (possibilmente inserire un 3 listener per aggiornamento titolo del progresso)
 IMPORTANTE: deve lavorare in background, e deve avere una booleana in ingresso per indicare "scarica immagini" TRUE|FALSE (se false non scarica le immagini di vini/tenute)
 
 Importante tutti i metodi GET devono sempre avere il parametro "LANG" in ingresso ed opzionale (anche la downloadAll) => se passato forza il dato nella lingua richiesta, se non passato utilizza la lingua di sistema del device in uso.
 
 */



@end
