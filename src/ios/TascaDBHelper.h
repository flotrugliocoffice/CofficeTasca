//
//  TascaDBHelper.h
//  Tasca Test
//
//  Created by Adriano Tornatore on 28/08/17.
//  Copyright © 2017 Coffice srl. All rights reserved.
//


#import <Foundation/Foundation.h>
#import "SQLiteManager.h"

@interface TascaDBHelper : NSObject{
    SQLiteManager *sqlManager;
    NSString *dataBaseName;
    NSString *dataBasePath;
}

+ (id)sharedInstance;

- (void)checkDB;
- (NSArray*)getRowsForQuery:(NSString*)query;

/* estates */
- (NSDictionary *)getEstateByID:(long)itemID forLanguage:(NSString *)lang;
- (NSArray *)getEstatesTypeAhead:(NSString *)term forLanguage:(NSString *)lang;
- (NSArray *)getEstates:(NSString *)term forLanguage:(NSString *)lang limit:(int)limit offset:(int)offset sort:(NSString *)sort sortDirection:(BOOL)sortDirection;
- (long)insertEstate:(NSDictionary *)itemDictionary;
 - (long)insertEstate:(NSDictionary *)itemDictionary forLanguage:(NSString *)lang;
- (BOOL)updateEstate:(long)itemID data:(NSDictionary *)itemDictionary;
- (BOOL)deleteEstate:(long)itemID;
- (BOOL)clearTableEstates;

/* tastes */
- (NSDictionary *)getTasteByID:(long)itemID;
- (NSArray *)getTastesTypeAhead:(NSString *)term;
- (NSArray *)getTastes:(NSString *)term limit:(int)limit offset:(int)offset sort:(NSString *)sort sortDirection:(BOOL)sortDirection;
- (long)insertTaste:(NSDictionary *)itemDictionary;
- (BOOL)updateTaste:(long)itemID data:(NSDictionary *)itemDictionary;
- (BOOL)deleteTaste:(long)itemID;
- (BOOL)clearTableTastes;

/* wines */
- (NSDictionary *)getWineByID:(long)itemID forLanguage:(NSString *)lang;
- (NSArray *)getWinesTypeAhead:(NSString *)term forLanguage:(NSString *)lang;
- (NSArray *)getWines:(NSString *)term forLanguage:(NSString *)lang limit:(int)limit offset:(int)offset sort:(NSString *)sort sortDirection:(BOOL)sortDirection;
- (long)insertWine:(NSDictionary *)itemDictionary;
- (long)insertWine:(NSDictionary *)itemDictionary forLanguage:(NSString *)lang;
- (BOOL)updateWine:(long)itemID data:(NSDictionary *)itemDictionary;
- (BOOL)deleteWine:(long)itemID;
- (BOOL)clearTableWines;

/* images */
- (NSDictionary *)getImageByUrl:(NSString*)url;
- (NSArray *)getImages;
- (NSArray *)getImagesNotDownloaded;
- (long)insertImage:(NSDictionary *)itemDictionary;
- (BOOL)updateImage:(NSString*)url data:(NSDictionary *)itemDictionary;
- (BOOL)deleteImage:(NSString*)url;
- (BOOL)replaceImage:(NSString*)url data:(NSDictionary *)itemDictionary;
- (BOOL)clearTableImages;


/* other */

- (NSComparisonResult)compareLastUpdate:(NSString *)dateString lang:(NSString *)lang table:(NSString *)table;
- (BOOL)clearDownloads;
- (BOOL)clearAllData;

- (void)resetContents;

- (NSArray*)getAllTables;
- (void)clearAllTables;


@end
