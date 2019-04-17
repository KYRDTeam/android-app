package com.kyberswap.android.data.repository.datasource.local;

import android.arch.persistence.db.SupportSQLiteStatement;
import android.arch.persistence.room.EntityDeletionOrUpdateAdapter;
import android.arch.persistence.room.EntityInsertionAdapter;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.RoomSQLiteQuery;
import android.arch.persistence.room.RxRoom;
import android.arch.persistence.room.SharedSQLiteStatement;
import android.database.Cursor;
import com.kyberswap.android.data.api.home.entity.ArticleFeatureEntity;
import com.kyberswap.android.data.api.home.entity.HeaderEntity;
import io.reactivex.Flowable;
import java.lang.Exception;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.List;
import java.util.concurrent.Callable;

@SuppressWarnings("unchecked")
public class HeaderDao_Impl implements HeaderDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter __insertionAdapterOfHeaderEntity;

  private final HeaderTypeConverter __headerTypeConverter = new HeaderTypeConverter();

  private final EntityDeletionOrUpdateAdapter __deletionAdapterOfHeaderEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  public HeaderDao_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfHeaderEntity = new EntityInsertionAdapter<HeaderEntity>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR REPLACE INTO `HeaderEntity`(`articleFeatures`,`reviewCount`,`likeCount`,`type`,`uid`) VALUES (?,?,?,?,nullif(?, 0))";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, HeaderEntity value) {
        final String _tmp;
        _tmp = __headerTypeConverter.listToString(value.getArticleFeatures());
        if (_tmp == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, _tmp);
        }
        stmt.bindLong(2, value.getReviewCount());
        stmt.bindLong(3, value.getLikeCount());
        if (value.getType() == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindString(4, value.getType());
        }
        stmt.bindLong(5, value.getUid());
      }
    };
    this.__deletionAdapterOfHeaderEntity = new EntityDeletionOrUpdateAdapter<HeaderEntity>(__db) {
      @Override
      public String createQuery() {
        return "DELETE FROM `HeaderEntity` WHERE `uid` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, HeaderEntity value) {
        stmt.bindLong(1, value.getUid());
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      public String createQuery() {
        final String _query = "DELETE FROM HeaderEntity";
        return _query;
      }
    };
  }

  @Override
  public void insert(HeaderEntity headerEntity) {
    __db.beginTransaction();
    try {
      __insertionAdapterOfHeaderEntity.insert(headerEntity);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void delete(HeaderEntity headerEntity) {
    __db.beginTransaction();
    try {
      __deletionAdapterOfHeaderEntity.handle(headerEntity);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void updateData(HeaderEntity headerEntity) {
    __db.beginTransaction();
    try {
      HeaderDao.DefaultImpls.updateData(this, headerEntity);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deleteAll() {
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAll.acquire();
    __db.beginTransaction();
    try {
      _stmt.executeUpdateDelete();
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
      __preparedStmtOfDeleteAll.release(_stmt);
    }
  }

  @Override
  public Flowable<HeaderEntity> getAll() {
    final String _sql = "SELECT * FROM HeaderEntity";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return RxRoom.createFlowable(__db, new String[]{"HeaderEntity"}, new Callable<HeaderEntity>() {
      @Override
      public HeaderEntity call() throws Exception {
        final Cursor _cursor = __db.query(_statement);
        try {
          final int _cursorIndexOfArticleFeatures = _cursor.getColumnIndexOrThrow("articleFeatures");
          final int _cursorIndexOfReviewCount = _cursor.getColumnIndexOrThrow("reviewCount");
          final int _cursorIndexOfLikeCount = _cursor.getColumnIndexOrThrow("likeCount");
          final int _cursorIndexOfType = _cursor.getColumnIndexOrThrow("type");
          final int _cursorIndexOfUid = _cursor.getColumnIndexOrThrow("uid");
          final HeaderEntity _result;
          if(_cursor.moveToFirst()) {
            _result = new HeaderEntity();
            final List<ArticleFeatureEntity> _tmpArticleFeatures;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfArticleFeatures);
            _tmpArticleFeatures = __headerTypeConverter.stringToList(_tmp);
            _result.setArticleFeatures(_tmpArticleFeatures);
            final long _tmpReviewCount;
            _tmpReviewCount = _cursor.getLong(_cursorIndexOfReviewCount);
            _result.setReviewCount(_tmpReviewCount);
            final long _tmpLikeCount;
            _tmpLikeCount = _cursor.getLong(_cursorIndexOfLikeCount);
            _result.setLikeCount(_tmpLikeCount);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            _result.setType(_tmpType);
            final long _tmpUid;
            _tmpUid = _cursor.getLong(_cursorIndexOfUid);
            _result.setUid(_tmpUid);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }
}
