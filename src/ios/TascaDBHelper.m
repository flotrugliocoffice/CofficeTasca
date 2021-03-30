//
//  TascaDBHelper.m
//  Tasca Test
//
//  Created by Adriano Tornatore on 28/08/17.
//  Copyright © 2017 Coffice srl. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "TascaDBHelper.h"
#import "TascaCommon.h"

static TascaDBHelper *sharedDBServer = nil;

@implementation TascaDBHelper
dispatch_queue_t databaseQueue = nil;
+ (TascaDBHelper*)sharedInstance
{
    static dispatch_once_t predicate = 0;
    __strong static id sharedObject = nil;

    //static id sharedObject = nil;  //if you're not using ARC
    dispatch_once(&predicate, ^{
        sharedObject = [[self alloc] init];


        //sharedObject = [[[self alloc] init] retain]; // if you're not using ARC


    });
    return sharedObject;
}


- (void)dealloc {
    // Should never be called, but just here for clarity really.
    if(sqlManager) {
        [sqlManager closeDatabase];
    }
}

- (void)checkDB{
    //PRepare database queue;
    databaseQueue = dispatch_queue_create("tasca.databasemanager", 0);

    NSString *libraryPath = [NSSearchPathForDirectoriesInDomains(NSLibraryDirectory, NSUserDomainMask, YES) lastObject];
    NSString *targetPath = [libraryPath stringByAppendingPathComponent:[NSString stringWithFormat:@"%@.sqlite",[TascaCommon getDatabaseName]]];


    if (![[NSFileManager defaultManager] fileExistsAtPath:targetPath]) {
        // database doesn't exist in your library path... copy it from the bundle
        NSString *sourcePath = [[NSBundle mainBundle] pathForResource:[TascaCommon getDatabaseName] ofType:@"db"];
        NSError *error = nil;

        if (![[NSFileManager defaultManager] copyItemAtPath:sourcePath toPath:targetPath error:&error]) {
            TascaLog(@"Error: %@", error);
        }


    }
    NSLog(@"DB path: %@",targetPath);
    dataBasePath = targetPath;

    sqlManager = [[SQLiteManager alloc] initWithDatabaseNamed:targetPath];

}

- (NSString*)getDatabasePath{
    return dataBasePath;
}
- (NSArray *)getRowsForQuery:(NSString *)query{
    return [sqlManager getRowsForQuery:query];
}

#pragma mark - Generic methods
- (NSString*)insertItemQuery:(NSDictionary *)itemDictionary toTable:(NSString *)tableName {
    NSMutableString *columns = [NSMutableString new];
    NSMutableString *values = [NSMutableString new];
    BOOL hasError = NO;
    for (NSString* key in itemDictionary.allKeys) {
        if([itemDictionary[key] isKindOfClass:[NSNull class]]){
            hasError = YES;
            break;
        }else if ([itemDictionary[key] isKindOfClass:[NSString class]]){
            [values appendFormat:@",'%@'",[itemDictionary[key] stringByReplacingOccurrencesOfString:@"'" withString:@"''"]];
        }else if([itemDictionary[key] isKindOfClass:[NSNumber class]]) {
            [values appendFormat:@",'%@'",itemDictionary[key]];

        }else if([itemDictionary[key] isKindOfClass:[NSArray class]] || [itemDictionary[key] isKindOfClass:[NSDictionary class]]){
            [values appendFormat:@",'%@'",[[TascaCommon jsonFromObject:itemDictionary[key]] stringByReplacingOccurrencesOfString:@"'" withString:@"''"]];
        }else{
            continue;
        }
        [columns appendFormat:@",%@",key];

    }
    if(hasError){
        return @"";
    }
    return [NSString stringWithFormat:@"INSERT INTO [%@] (%@) VALUES (%@);", tableName, [columns substringFromIndex:1], [values substringFromIndex:1]];
}
- (NSString*)updateItemQuery:(NSDictionary *)itemDictionary fromTable:(NSString *)tableName withID:(NSString *)itemID {
    NSMutableString *values = [NSMutableString new];

    BOOL hasError = NO;
    for (NSString* key in itemDictionary.allKeys) {
        if([itemDictionary[key] isKindOfClass:[NSNull class]]){
            hasError = YES;
            break;
        }

        [values appendFormat:@", %@ = ",key];

        if ([itemDictionary[key] isKindOfClass:[NSString class]]){
            [values appendFormat:@"'%@'",[itemDictionary[key] stringByReplacingOccurrencesOfString:@"'" withString:@"''"]];
        }else if([itemDictionary[key] isKindOfClass:[NSNumber class]]) {
            [values appendFormat:@"'%@'",itemDictionary[key]];

        }else if([itemDictionary[key] isKindOfClass:[NSArray class]] || [itemDictionary[key] isKindOfClass:[NSDictionary class]]){
            [values appendFormat:@"'%@'",[[TascaCommon jsonFromObject:itemDictionary[key]] stringByReplacingOccurrencesOfString:@"'" withString:@"''"]];
        }else{
            continue;
        }


    }
    if(hasError){
        return @"";
    }
    return [NSString stringWithFormat:@"UPDATE [%@] SET %@ WHERE ID = '%@';", tableName, [values substringFromIndex:1], itemID];
}
- (NSString*)updateItemQuery:(NSDictionary *)itemDictionary fromTable:(NSString *)tableName withURL:(NSString *)itemURL {
    NSMutableString *values = [NSMutableString new];

    BOOL hasError = NO;
    for (NSString* key in itemDictionary.allKeys) {
        if([itemDictionary[key] isKindOfClass:[NSNull class]]){
            hasError = YES;
            break;
        }

        [values appendFormat:@", %@ = ",key];

        if ([itemDictionary[key] isKindOfClass:[NSString class]]){
            [values appendFormat:@"'%@'",[itemDictionary[key] stringByReplacingOccurrencesOfString:@"'" withString:@"''"]];
        }else if([itemDictionary[key] isKindOfClass:[NSNumber class]]) {
            [values appendFormat:@"'%@'",itemDictionary[key]];

        }else if([itemDictionary[key] isKindOfClass:[NSArray class]] || [itemDictionary[key] isKindOfClass:[NSDictionary class]]){
            [values appendFormat:@"'%@'",[[TascaCommon jsonFromObject:itemDictionary[key]] stringByReplacingOccurrencesOfString:@"'" withString:@"''"]];
        }else{
            continue;
        }


    }
    if(hasError){
        return @"";
    }
    return [NSString stringWithFormat:@"UPDATE [%@] SET %@ WHERE url = '%@';", tableName, [values substringFromIndex:1], itemURL];
}
- (NSString*)deleteItemQuery:(NSString*)itemKey keyField:(NSString*)key fromTable:(NSString*)table {
    return [NSString stringWithFormat:@"delete from [%@] where %@ = '%@';", table, key, itemKey];
}
- (NSString*)deleteItemByIDQuery:(NSString*)itemKey fromTable:(NSString*)table {
    return [self deleteItemQuery:itemKey keyField:@"ID" fromTable:table];
}
- (NSString*)deleteItemByURLQuery:(NSString*)itemKey fromTable:(NSString*)table {
    return [self deleteItemQuery:itemKey keyField:@"url" fromTable:table];
}

- (long)insertItem:(NSDictionary *)itemDictionary toTable:(NSString *)tableName{
    NSString *query = [self insertItemQuery:itemDictionary toTable:tableName];
    if([query length]==0) {
        return -1;
    }
    //NSLog(@"total query: %@",query);
    NSError *error = [sqlManager doQuery:query];
    if (error) {

        TascaLog(@"errore query: %@",[error description]);
        return -1;
    }

    return [sqlManager getLastInsertRowID];
}

- (long)updateItem:(NSDictionary *)itemDictionary fromTable:(NSString *)tableName withID:(NSString *)itemID{
    NSString* query = [self updateItemQuery:itemDictionary fromTable:tableName withID:itemID];
    if([query length]==0) {
        return -1;
    }

    //NSLog(@"total query: %@",query);
    NSError *error = [sqlManager doQuery:query];
    if (error) {

        TascaLog(@"errore query: %@",[error description]);
        return -1;
    }

    return [itemID longLongValue];
}

- (NSString *)updateItem:(NSDictionary *)itemDictionary fromTable:(NSString *)tableName withURL:(NSString *)itemURL{

    NSString* query = [self updateItemQuery:itemDictionary fromTable:tableName withURL:itemURL];
    if([query length]==0) {
        return @"";
    }
    //NSLog(@"total query: %@",query);
    NSError *error = [sqlManager doQuery:query];
    if (error) {

        TascaLog(@"errore query: %@",[error description]);
        return @"";
    }

    return itemURL;
}

- (BOOL)deleteItemWithID:(NSString *)itemID fromTable:(NSString *)tableName{
    NSString *query = [self deleteItemQuery:itemID keyField:@"ID" fromTable:tableName];
    return [sqlManager doQuery:query];
}

- (BOOL)deleteItemWithUrl:(NSString *)itemURL fromTable:(NSString *)tableName{
    NSString *query = [self deleteItemQuery:itemURL keyField:@"url" fromTable:tableName];
    return [sqlManager doQuery:query];
}


-(BOOL)executeQueryTransaction:(NSArray*)queries {
    NSError* error = [sqlManager doTransaction:queries];
    if(error) {
        NSLog(@"Errore db %@",error);
    }
    return error!=nil;
}

- (BOOL)clearTable:(NSString *)tableName{

    NSString *query = [NSString stringWithFormat:@"delete from [%@]", tableName];
    return [sqlManager doQuery:query];

}

#pragma mark - Estates

- (NSDictionary *)getEstateByID:(long)itemID forLanguage:(NSString *)lang{

    NSString *query = [NSString stringWithFormat:@"SELECT json_raw FROM [%@] WHERE ID = '%@' and language = '%@'", ESTATES_TABLE, [NSString stringWithFormat:@"%ld",itemID], lang];
    NSArray *results = [self convertJsonRawArray:[sqlManager getRowsForQuery:query]];

    if(results && results.count>0){
        return results[0];
    }
    return nil;

}
- (NSArray *)getEstatesTypeAhead:(NSString *)term forLanguage:(NSString *)lang{



    /*
     date_gmt
     modified_gmt
     id
     title.rendered
     acf.localita
     acf.territorio_sottotitolo
     */

    lang = [TascaCommon getLanguage:lang];

    NSString *whereSql = @"";
    if(term && term.length>0){
        whereSql = @"AND (date_gmt = '[TERM]' OR modified_gmt = '[TERM]' OR id = '[TERM]' OR title LIKE '[TERM]%%' OR acf_locality = '[TERM]%%' OR acf_territorial_subtitles LIKE '[TERM]%%')";
        whereSql = [whereSql stringByReplacingOccurrencesOfString:@"[TERM]" withString:term];
    }


    NSString* query = [NSString stringWithFormat:@"SELECT id, title FROM [%@] WHERE language = '%@' %@ LIMIT 10", ESTATES_TABLE, lang, whereSql];



    return [self convertJsonRawArray:[sqlManager getRowsForQuery:query]];

}
- (NSArray *)getEstates:(NSString *)term forLanguage:(NSString *)lang limit:(int)limit offset:(int)offset sort:(NSString *)sort sortDirection:(BOOL)sortDirection{

    NSString *whereSql = @"";
    if (term && term.length>0){

        whereSql = [NSString stringWithFormat:@"AND (id = '%@' OR title LIKE '%%%@%%')", term, term];

    }

    NSMutableString *limitSql = [NSMutableString stringWithString:@""];
    if (limit>0) {
        [limitSql appendFormat:@"LIMIT %i",limit];

        if(offset>0){
            [limitSql appendFormat:@", %i",offset];
        }

    }

    NSString *sortSql = @"";
    if (sort && sort.length>0) {

        sortSql = [NSString stringWithFormat:@"SORT BY %@ ORDER %@", sort, (sortDirection?@"ASC":@"DESC")];

    }

    NSString *query = [NSString stringWithFormat:@"SELECT * FROM [%@] WHERE language = '%@' %@ %@ %@", ESTATES_TABLE, lang, whereSql, limitSql, sortSql];


    return [self convertJsonRawArray:[sqlManager getRowsForQuery:query]];

}

- (long)insertEstate:(NSDictionary *)itemDictionary{
    return [self insertEstate:itemDictionary forLanguage:[TascaCommon getLanguage]];
}

- (long)insertEstate:(NSDictionary *)itemDictionary forLanguage:(NSString *)lang{

    NSMutableDictionary *dbDictionary = [NSMutableDictionary new];

    [dbDictionary setObject:itemDictionary[@"id"] forKey:@"id"];
    [dbDictionary setObject:lang forKey:@"language"];
    [dbDictionary setObject:itemDictionary forKey:@"json_raw"];
    [dbDictionary setObject:itemDictionary[@"date"] forKey:@"date_gmt"];
    [dbDictionary setObject:itemDictionary[@"modified"] forKey:@"modified_gmt"];
    [dbDictionary setObject:itemDictionary[@"title"] forKey:@"title"];

    NSDictionary *acf = itemDictionary[ACF_FIELD];

    NSString *localita = acf[@"localita"];
    if(localita){
        [dbDictionary setObject:localita forKey:@"acf_locality"];
    }
    NSString *territorio_sottotitolo = acf[@"territorio_sottotitolo"];
    if(territorio_sottotitolo){
        [dbDictionary setObject:territorio_sottotitolo forKey:@"acf_territorial_subtitles"];
    }


    return [self insertItem:dbDictionary toTable:ESTATES_TABLE];

}

- (BOOL)updateEstate:(long)itemID data:(NSDictionary *)itemDictionary{

    return [self updateItem:itemDictionary fromTable:ESTATES_TABLE withID:[NSString stringWithFormat:@"%ld",itemID]];

}

- (BOOL)deleteEstate:(long)itemID{

    return [self deleteItemWithID:[NSString stringWithFormat:@"%ld",itemID] fromTable:ESTATES_TABLE];

}

- (BOOL)clearTableEstates{

    return [self clearTable:ESTATES_TABLE];

}


#pragma mark - Tastes

- (NSDictionary *)getTasteByID:(long)itemID{

    NSString *query = [NSString stringWithFormat:@"SELECT json_raw FROM [%@] WHERE ID = '%@'", TASTES_TABLE, [NSString stringWithFormat:@"%ld",itemID]];
    NSArray *results = [sqlManager getRowsForQuery:query];

    if(results && results.count>0){
        return results[0];
    }
    return nil;

}
- (NSArray *)getTastesTypeAhead:(NSString *)term{


    NSString *whereSql = @"";
    if(term != nil && term.length>0){
        whereSql = @"WHERE date_gmt = '[TERM]' OR modified_gmt = '[TERM]' OR id = '[TERM]' OR name LIKE '[TERM]%%' OR year = '[TERM]' OR wine LIKE '[TERM]%%' OR vote = '[TERM]' OR estate LIKE '[TERM]%%' OR wine_id = '[TERM]' OR estate_id = '[TERM]'";
        whereSql = [whereSql stringByReplacingOccurrencesOfString:@"[TERM]" withString:term];
    }


    NSString* query = [NSString stringWithFormat:@"SELECT id, name FROM [%@] %@ LIMIT 10", TASTES_TABLE, whereSql];



    return [sqlManager getRowsForQuery:query];

}
- (NSArray *)getTastes:(NSString *)term limit:(int)limit offset:(int)offset sort:(NSString *)sort sortDirection:(BOOL)sortDirection{

    NSString *whereSql = @"";
    if (term && term.length>0){

        whereSql = [NSString stringWithFormat:@"WHERE (id = '%@' OR wine LIKE '%%%@%%')", term, term];

    }

    NSMutableString *limitSql = [NSMutableString stringWithString:@""];
    if (limit>0) {
        [limitSql appendFormat:@"LIMIT %i",limit];

        if(offset>0){
            [limitSql appendFormat:@", %i",offset];
        }

    }

    NSString *sortSql = @"";
    if (sort && sort.length>0) {

        sortSql = [NSString stringWithFormat:@"SORT BY %@ ORDER %@", sort, (sortDirection?@"ASC":@"DESC")];

    }

    NSString *query = [NSString stringWithFormat:@"SELECT * FROM [%@] %@ %@ %@", TASTES_TABLE, whereSql, limitSql, sortSql];


    return [sqlManager getRowsForQuery:query];

}
- (long)insertTaste:(NSDictionary *)itemDictionary{

    NSMutableDictionary *dbDictionary = [NSMutableDictionary new];

    [dbDictionary setObject:itemDictionary[@"id"] forKey:@"id"];
    [dbDictionary setObject:itemDictionary forKey:@"json_raw"];
    [dbDictionary setObject:itemDictionary[@"date"] forKey:@"date_gmt"];
    [dbDictionary setObject:itemDictionary[@"modified"] forKey:@"modified_gmt"];


    NSNumber *year = itemDictionary[@"year"];
    if(year){
        [dbDictionary setObject:year forKey:@"year"];
    }else{
        [dbDictionary setObject:@-1 forKey:@"year"];
    }
    NSString *name = itemDictionary[@"name"];
    if(name){
        [dbDictionary setObject:name forKey:@"name"];
    }
    NSString *wine = itemDictionary[@"wine"];
    if(name){
        [dbDictionary setObject:wine forKey:@"wine"];
    }
    NSString *vote = itemDictionary[@"vote"];
    if(name){
        [dbDictionary setObject:vote forKey:@"vote"];
    }
    NSString *estate = itemDictionary[@"estate"];
    if(name){
        [dbDictionary setObject:estate forKey:@"estate"];
    }
    NSNumber *wineID = itemDictionary[@"wineId"];
    if(name){
        [dbDictionary setObject:wineID forKey:@"wine_id"];
    }
    NSNumber *estateID = itemDictionary[@"estateId"];
    if(name){
        [dbDictionary setObject:estateID forKey:@"estate_id"];
    }

    return [self insertItem:dbDictionary toTable:TASTES_TABLE];

}
- (BOOL)updateTaste:(long)itemID data:(NSDictionary *)itemDictionary{

    return [self updateItem:itemDictionary fromTable:TASTES_TABLE withID:[NSString stringWithFormat:@"%ld",itemID]];

}

- (BOOL)deleteTaste:(long)itemID{

    return [self deleteItemWithID:[NSString stringWithFormat:@"%ld",itemID] fromTable:TASTES_TABLE];

}

- (BOOL)clearTableTastes{

    return [self clearTable:TASTES_TABLE];;

}


#pragma mark - Wines

- (NSDictionary *)getWineByID:(long)itemID forLanguage:(NSString *)lang{

    NSString *query = [NSString stringWithFormat:@"SELECT json_raw FROM [%@] WHERE ID = '%@' and language = '%@'", WINES_TABLE, [NSString stringWithFormat:@"%ld",itemID], lang];
    NSArray *results = [self convertJsonRawArray:[sqlManager getRowsForQuery:query]];

    if(results && results.count>0){
        return results[0];
    }

    return nil;

}
- (NSArray *)getWinesTypeAhead:(NSString *)term forLanguage:(NSString *)lang{

    /*
     date_gmt
     modified_gmt
     id
     title.rendered
     tenuta.id
     acf.denominazione
     slug
     language
     */

    lang = [TascaCommon getLanguage:lang];

    NSString *whereSql = @"";
    if(term && term.length>0){
        whereSql = @"AND (date_gmt = '[TERM]' OR modified_gmt = '[TERM]' OR id = '[TERM]' OR title LIKE '[TERM]%%' OR estate_id = '[TERM]' OR acf_denomination LIKE '[TERM]%%' OR slug LIKE '[TERM]%%' OR language = '[TERM]') ";
        whereSql = [whereSql stringByReplacingOccurrencesOfString:@"[TERM]" withString:term];
    }
    NSString* query = [NSString stringWithFormat:@"SELECT id, title FROM [%@] WHERE language = '%@' %@ LIMIT 10", WINES_TABLE, lang, whereSql];



    return [self convertJsonRawArray:[sqlManager getRowsForQuery:query]];

}
- (NSArray *)getWines:(NSString *)term forLanguage:(NSString *)lang limit:(int)limit offset:(int)offset sort:(NSString *)sort sortDirection:(BOOL)sortDirection{


    NSString *whereSql = @"";
    if (term && term.length>0){

        whereSql = [NSString stringWithFormat:@"AND (id = '%@' OR title LIKE '%%%@%%')", term, term];

    }

    NSMutableString *limitSql = [NSMutableString stringWithString:@""];
    if (limit>0) {
        [limitSql appendFormat:@"LIMIT %i",limit];

        if(offset>0){
            [limitSql appendFormat:@", %i",offset];
        }

    }

    NSString *sortSql = @"";
    if (sort && sort.length>0) {

        sortSql = [NSString stringWithFormat:@"SORT BY %@ ORDER %@", sort, (sortDirection?@"ASC":@"DESC")];

    }

    NSString *query = [NSString stringWithFormat:@"SELECT * FROM [%@] WHERE language = '%@' %@ %@ %@", WINES_TABLE, lang, whereSql, limitSql, sortSql];
    return [self convertJsonRawArray:[sqlManager getRowsForQuery:query]];
}
- (long)insertWine:(NSDictionary *)itemDictionary{
    return [self insertWine:itemDictionary forLanguage:[TascaCommon getLanguage]];
}
- (long)insertWine:(NSDictionary *)itemDictionary forLanguage:(NSString *)lang{


    NSMutableDictionary *dbDictionary = [NSMutableDictionary new];

    [dbDictionary setObject:itemDictionary[@"id"] forKey:@"id"];
    [dbDictionary setObject:lang forKey:@"language"];
    [dbDictionary setObject:itemDictionary forKey:@"json_raw"];
    [dbDictionary setObject:itemDictionary[@"date"] forKey:@"date_gmt"];
    [dbDictionary setObject:itemDictionary[@"modified"] forKey:@"modified_gmt"];
    [dbDictionary setObject:itemDictionary[@"title"] forKey:@"title"];
    [dbDictionary setObject:itemDictionary[@"slug"] forKey:@"slug"];
    NSDictionary *acf = itemDictionary[ACF_FIELD];

    NSDictionary *estate = acf[@"tenuta"];
    if(estate){
        [dbDictionary setObject:estate[@"ID"] forKey:@"estate_id"];
    }
    NSString *denomination = acf[@"denominazione"];
    if(denomination){
        [dbDictionary setObject:denomination forKey:@"acf_denomination"];
    }


    return [self insertItem:dbDictionary toTable:WINES_TABLE];



}
- (BOOL)updateWine:(long)itemID data:(NSDictionary *)itemDictionary{

    return [self updateItem:itemDictionary fromTable:WINES_TABLE withID:[NSString stringWithFormat:@"%ld",itemID]];

}
- (BOOL)deleteWine:(long)itemID{

    return [self deleteItemWithID:[NSString stringWithFormat:@"%ld",itemID] fromTable:WINES_TABLE];

}
- (BOOL)clearTableWines{

    return [self clearTable:WINES_TABLE];;

}

#pragma mark Image
- (NSDictionary *)getImageByUrl:(NSString*)url {
    NSString *query = [NSString stringWithFormat:@"SELECT * FROM [%@] WHERE url = '%@' ", IMAGES_TABLE, url];
    NSArray *results = [sqlManager getRowsForQuery:query];
    if(results && results.count>0){
        return results[0];
    }
    return nil;
}
- (NSArray *)getImages {
    NSString *query = [NSString stringWithFormat:@"SELECT * FROM [%@] ", IMAGES_TABLE];
    return [sqlManager getRowsForQuery:query];
}
- (NSArray *)getImagesNotDownloaded {
    NSString *query = [NSString stringWithFormat:@"SELECT * FROM [%@] WHERE local is null or length(local)=0 ", IMAGES_TABLE];
    return [sqlManager getRowsForQuery:query];
}
- (long)insertImage:(NSDictionary *)itemDictionary {
    //__block long res = -1;
    //dispatch_sync(databaseQueue, ^{
    BOOL res = [self insertItem:itemDictionary toTable:IMAGES_TABLE];
    //});
    return res;

}
- (BOOL)updateImage:(NSString*)url data:(NSDictionary *)itemDictionary {
    //__block BOOL res = NO;
    //dispatch_sync(databaseQueue, ^{
    BOOL res = [self updateItem:itemDictionary fromTable:IMAGES_TABLE withURL:url].length>0?YES:NO;
    //});
    return res;
}
- (BOOL)deleteImage:(NSString*)url {
    return [self deleteItemWithUrl:url fromTable:IMAGES_TABLE];
}
- (BOOL)replaceImage:(NSString*)url data:(NSDictionary *)itemDictionary {
    //Delete and insert:
    //__block BOOL res = NO;
    //dispatch_sync(databaseQueue, ^{
        NSMutableArray*queries = [NSMutableArray new];
        [queries addObject:[self deleteItemByURLQuery:url fromTable:IMAGES_TABLE]];
        [queries addObject:[self insertItemQuery:itemDictionary toTable:IMAGES_TABLE]];
    BOOL res = [self executeQueryTransaction:queries];
    //});
    return res;
}
- (BOOL)clearTableImages {
    return [self clearTable:IMAGES_TABLE];;
}

/* other */

- (NSComparisonResult)compareLastUpdate:(NSString *)dateString lang:(NSString *)lang table:(NSString *)table{
    NSString *query = [NSString stringWithFormat:@"SELECT modified_gmt from [%@] WHERE language = '%@' ORDER BY modified_gmt DESC LIMIT 1", table, lang];

    NSArray *results = [sqlManager getRowsForQuery:query];

    if(!results || results.count<1){
        return NSOrderedDescending;
    }

    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setDateFormat:@"yyyy-MM-dd HH:mm:ss"];

    NSDate *jsonDate = [dateFormatter dateFromString:dateString];

    NSString *lastDateString = results[0][@"modified_gmt"];
    NSDate *dbDate = [dateFormatter dateFromString:lastDateString];


    NSLog(@"Table %@ - lang %@ - From %@ - Last: %@ ", table, lang, dateString,lastDateString);
    return [jsonDate compare:dbDate];



}

- (BOOL)clearDownloads{

    return NO;

}

- (BOOL)clearAllData{



    return NO;

}

- (void)resetContents{



}


- (NSArray*)getAllTables{

    return nil;

}

- (void)clearAllTables{



}

- (NSArray*)convertJsonRawArray:(NSArray*)source {
    NSMutableArray*array = [[NSMutableArray alloc] initWithCapacity:source.count];
    [source enumerateObjectsUsingBlock:^(id  _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
        if(obj[@"json_raw"]) {
            id tmp =[TascaCommon jsonObjectFromString:obj[@"json_raw"]];
            if(tmp) {
                array[idx] = tmp;
                return;
            }
        }
        array[idx] = obj;
    }];
    return array;
}

@end
