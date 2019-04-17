package com.kyberswap.android.data.repository.datasource.local;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.db.SupportSQLiteOpenHelper;
import android.arch.persistence.db.SupportSQLiteOpenHelper.Callback;
import android.arch.persistence.db.SupportSQLiteOpenHelper.Configuration;
import android.arch.persistence.room.DatabaseConfiguration;
import android.arch.persistence.room.InvalidationTracker;
import android.arch.persistence.room.RoomOpenHelper;
import android.arch.persistence.room.RoomOpenHelper.Delegate;
import android.arch.persistence.room.util.TableInfo;
import android.arch.persistence.room.util.TableInfo.Column;
import android.arch.persistence.room.util.TableInfo.ForeignKey;
import android.arch.persistence.room.util.TableInfo.Index;
import java.lang.IllegalStateException;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.HashMap;
import java.util.HashSet;

@SuppressWarnings("unchecked")
public class AppDatabase_Impl extends AppDatabase {
  private volatile HeaderDao _headerDao;

  @Override
  protected SupportSQLiteOpenHelper createOpenHelper(DatabaseConfiguration configuration) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(configuration, new RoomOpenHelper.Delegate(5) {
      @Override
      public void createAllTables(SupportSQLiteDatabase _db) {
        _db.execSQL("CREATE TABLE IF NOT EXISTS `HeaderEntity` (`articleFeatures` TEXT NOT NULL, `reviewCount` INTEGER NOT NULL, `likeCount` INTEGER NOT NULL, `type` TEXT NOT NULL, `uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL)");
        _db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        _db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, \"84a26c765e03ca673bdd1ddcfd4867ae\")");
      }

      @Override
      public void dropAllTables(SupportSQLiteDatabase _db) {
        _db.execSQL("DROP TABLE IF EXISTS `HeaderEntity`");
      }

      @Override
      protected void onCreate(SupportSQLiteDatabase _db) {
        if (mCallbacks != null) {
          for (int _i = 0, _size = mCallbacks.size(); _i < _size; _i++) {
            mCallbacks.get(_i).onCreate(_db);
  

      }

      @Override
      public void onOpen(SupportSQLiteDatabase _db) {
        mDatabase = _db;
        internalInitInvalidationTracker(_db);
        if (mCallbacks != null) {
          for (int _i = 0, _size = mCallbacks.size(); _i < _size; _i++) {
            mCallbacks.get(_i).onOpen(_db);
  

      }

      @Override
      protected void validateMigration(SupportSQLiteDatabase _db) {
        final HashMap<String, TableInfo.Column> _columnsHeaderEntity = new HashMap<String, TableInfo.Column>(5);
        _columnsHeaderEntity.put("articleFeatures", new TableInfo.Column("articleFeatures", "TEXT", true, 0));
        _columnsHeaderEntity.put("reviewCount", new TableInfo.Column("reviewCount", "INTEGER", true, 0));
        _columnsHeaderEntity.put("likeCount", new TableInfo.Column("likeCount", "INTEGER", true, 0));
        _columnsHeaderEntity.put("type", new TableInfo.Column("type", "TEXT", true, 0));
        _columnsHeaderEntity.put("uid", new TableInfo.Column("uid", "INTEGER", true, 1));
        final HashSet<TableInfo.ForeignKey> _foreignKeysHeaderEntity = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesHeaderEntity = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoHeaderEntity = new TableInfo("HeaderEntity", _columnsHeaderEntity, _foreignKeysHeaderEntity, _indicesHeaderEntity);
        final TableInfo _existingHeaderEntity = TableInfo.read(_db, "HeaderEntity");
        if (! _infoHeaderEntity.equals(_existingHeaderEntity)) {
          throw new IllegalStateException("Migration didn't properly handle HeaderEntity(com.kyberswap.android.data.api.home.entity.HeaderEntity).\n"
                  + " Expected:\n" + _infoHeaderEntity + "\n"
                  + " Found:\n" + _existingHeaderEntity);

      }
    }, "84a26c765e03ca673bdd1ddcfd4867ae", "0c0f750aec97ca328920019d6dc2488a");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(configuration.context)
        .name(configuration.name)
        .callback(_openCallback)
        .build();
    final SupportSQLiteOpenHelper _helper = configuration.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  protected InvalidationTracker createInvalidationTracker() {
    return new InvalidationTracker(this, "HeaderEntity");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `HeaderEntity`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  public HeaderDao articleFeatureDao() {
    if (_headerDao != null) {
      return _headerDao;
    } else {
      synchronized(this) {
        if(_headerDao == null) {
          _headerDao = new HeaderDao_Impl(this);

        return _headerDao;
      }
    }
  }
}
