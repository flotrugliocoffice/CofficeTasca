//
//  SQLiteManager.h
//  Tasca Test
//
//  Created by Adriano Tornatore on 29/08/17.
//  Copyright © 2017 App&Map Srls. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "sqlite3.h"


enum errorCodes {
    kDBNotExists,
    kDBFailAtOpen,
    kDBFailAtCreate,
    kDBErrorQuery,
    kDBFailAtClose
};

@interface SQLiteManager : NSObject {

    sqlite3 *db; // The SQLite db reference
    NSString *databaseName; // The database name
}

- (id)initWithDatabaseNamed:(NSString *)name;

// SQLite Operations
- (NSError *) openDatabase;
- (NSError *) doQuery:(NSString *)sql;
- (NSError *) doTransaction:(NSArray*)queries;
- (NSError *)doUpdateQuery:(NSString *)sql withParams:(NSArray *)params;
- (NSArray *) getRowsForQuery:(NSString *)sql;
- (NSError *) closeDatabase;
- (NSInteger)getLastInsertRowID;

- (NSString *)getDatabaseDump;

static void distanceFunc(sqlite3_context *context, int argc, sqlite3_value **argv);

@end
