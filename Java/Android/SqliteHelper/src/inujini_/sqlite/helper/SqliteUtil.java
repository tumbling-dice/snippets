package inujini_.sqlite.helper;

import static inujini_.linq.Linq.*;
import inujini_.function.Function.Action1;
import inujini_.function.Function.Func1;
import inujini_.function.Function.Predicate;
import inujini_.sqlite.meta.ISqlite;
import inujini_.sqlite.meta.annotation.SqliteField;
import inujini_.sqlite.meta.annotation.SqliteTable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SqliteUtil {

	/**
	 * DBから要素を取得
	 * @param query Select文
	 * @param context
	 * @param helper
	 * @param f CursorからTを抽出するFunc
	 * @return 要素が見つからない場合はNull
	 */
	public static <T> T get(String query, Context cont, SQLiteOpenHelper helper, Func1<Cursor, T> f) {

		SQLiteDatabase db = helper.getReadableDatabase();

		if(!isMainThread(cont)) db.acquireReference();

		Cursor c = null;

		try {
			c = db.rawQuery(query, null);
		} catch(RuntimeException e) {
			db.close();
			throw e;
		}

		if(!c.moveToFirst()) {
			c.close();
			helper.close();
			return null;
		}

		T obj;

		try {
			obj = f.call(c);
		} finally {
			c.close();
			helper.close();
		}

		return obj;
	}

	/**
	 * DBからすべての要素を取得
	 * @param query Select文
	 * @param context
	 * @param helper
	 * @param f CursorからTを抽出するFunc
	 * @return 要素が見つからない場合は空のリスト
	 */
	public static <T> List<T> getList(String query, Context cont, SQLiteOpenHelper helper, Func1<Cursor, T> f) {

		List<T> dataList = new ArrayList<T>();

		SQLiteDatabase db = helper.getReadableDatabase();
		if(!isMainThread(cont)) db.acquireReference();

		Cursor c = null;
		try {
			c = db.rawQuery(query, null);
		} catch(RuntimeException e) {
			helper.close();
			throw e;
		}

		if(!c.moveToFirst()) {
			c.close();
			helper.close();
			return dataList;
		}

		try {
			do {
				dataList.add(f.call(c));
			} while (c.moveToNext());
		} finally {
			c.close();
			helper.close();
		}

		return dataList;
	}

	/**
	 * トランザクション処理
	 * @param helper
	 * @param context
	 * @param act 実行内容
	 */
	public static void transaction(SQLiteOpenHelper helper, Context cont, Action1<SQLiteDatabase> act) {
		SQLiteDatabase db = helper.getWritableDatabase();

		if(!isMainThread(cont)) db.acquireReference();

		db.beginTransaction();
		try {
			act.call(db);
			if(db.isOpen()) db.setTransactionSuccessful();
		} finally {
			if(db.isOpen()) {
				db.endTransaction();
				helper.close();
			}
		}
	}

	/**
	 * トランザクション処理
	 * @param helper
	 * @param context
	 * @param act 実行内容
	 * @param finallyAct finally節で実行する内容(endTransactionは必ず呼ばれる)
	 */
	public void transaction(SQLiteOpenHelper helper, Context cont
			, Action1<SQLiteDatabase> act, Action1<SQLiteDatabase> finallyAct) {

		SQLiteDatabase db = helper.getWritableDatabase();
		if(!isMainThread(cont)) db.acquireReference();

		db.beginTransaction();
		try {
			act.call(db);
			if(db.isOpen()) db.setTransactionSuccessful();
		} finally {
			finallyAct.call(db);
			if(db.isOpen()) {
				db.endTransaction();
				helper.close();
			}
		}
	}

	public static String getTableName(Class<? extends ISqlite> clazz) {
		return clazz.getAnnotation(SqliteTable.class).value();
	}

	public static String getDropTableQuery(Class<? extends ISqlite> clazz) {
		return "DROP TABLE " + getTableName(clazz) + ";";
	}

	public static String getCreateTableQuery(Class<? extends ISqlite> clazz) {
		final StringBuilder tblBuilder = new StringBuilder();
		final StringBuilder idxBuilder = new StringBuilder();
		final List<String> primaryKeyList = new ArrayList<String>();
		final Map<String, ArrayList<String>> indexMap = new HashMap<String, ArrayList<String>>();

		tblBuilder.append("CREATE TABLE IF NOT EXISTS ");

		//テーブル名
		SqliteTable tblAttribute = clazz.getAnnotation(SqliteTable.class);

		final String tblName = tblAttribute.value();
		tblBuilder.append(tblName).append(" (");

		//_idのプライマリキー有無
		if(tblAttribute.hasPrimaryId()) tblBuilder.append("_id INTEGER PRIMARY KEY AUTOINCREMENT,");

		//カラム名
		linq(clazz.getDeclaredFields())
		.where(new Predicate<Field>() {
			@Override
			public Boolean call(Field x) {
				//SqliteFieldのあるフィールドだけをフィルタする
				Annotation[] annotations = x.getDeclaredAnnotations();
				if(annotations.length == 0) return false;

				return linq(annotations)
						.where(new Predicate<Annotation>() {
							@Override
							public Boolean call(Annotation x) {
								return x instanceof SqliteField;
							}
						}).any();
			}
		})
		.forEach(new Action1<Field>() {
			@Override
			public void call(Field x) {

				//SqliteFieldを取得する
				SqliteField fieldAttribute = linq(x.getDeclaredAnnotations())
											.where(new Predicate<Annotation>() {
												@Override
												public Boolean call(Annotation x) {
													return x instanceof SqliteField;
												}
											}).select(new Func1<Annotation, SqliteField>() {
												@Override
												public SqliteField call(Annotation x) {
													return (SqliteField) x;
												}
											}).first();
				//カラム名
				String columnName = fieldAttribute.name();

				//カラム名をセット
				tblBuilder.append(columnName).append(" ");

				//データ型をセット
				switch(fieldAttribute.type()){
					case ISqlite.FIELD_TEXT:
						tblBuilder.append("TEXT");
						break;
					case ISqlite.FIELD_INTEGER:
						tblBuilder.append("INTEGER");
						break;
					case ISqlite.FIELD_REAL:
						tblBuilder.append("REAL");
						break;
					case ISqlite.FIELD_BLOB:
						tblBuilder.append("BLOB");
					case ISqlite.FIELD_NULL:
						tblBuilder.append("NULL");
				}

				//プライマリキー
				if(fieldAttribute.primary()) primaryKeyList.add(columnName);

				//not null有無
				if(fieldAttribute.notNull()) tblBuilder.append(" NOT NULL");

				//一意制約
				if(fieldAttribute.unique()) tblBuilder.append(" UNIQUE");

				//デフォルト値
				if(!"".equals(fieldAttribute.defaultValue())) {
					tblBuilder.append(" DEFAULT ");
					//TEXTの場合はシングルクォーテーションを付与する
					if(fieldAttribute.type() == ISqlite.FIELD_TEXT) {
						tblBuilder.append("'").append(fieldAttribute.defaultValue()).append("'");
					} else {
						tblBuilder.append(fieldAttribute.defaultValue());
					}
				}

				tblBuilder.append(",");

				//Index
				if(!"".equals(fieldAttribute.indexName())) {
					if(!indexMap.containsKey(fieldAttribute.indexName()))
						indexMap.put(fieldAttribute.indexName(), new ArrayList<String>());

					indexMap.get(fieldAttribute.indexName()).add(columnName);

					idxBuilder.append("CREATE INDEX IF NOT EXISTS ")
								.append(fieldAttribute.indexName())
								.append(" ON ").append(tblName)
								.append("(").append(columnName).append(");");
				}
			}
		});

		//最後についた,を削除する
		tblBuilder.deleteCharAt(tblBuilder.length() - 1);

		//PrimaryKeyがある場合は追加する
		if(!primaryKeyList.isEmpty()) {
			tblBuilder.append(" ,PRIMARY KEY(");
			for (String primaryKey : primaryKeyList) {
				tblBuilder.append(primaryKey).append(",");
			}
			tblBuilder.deleteCharAt(tblBuilder.length() - 1);
			tblBuilder.append(")");
		}

		tblBuilder.append(");");

		//Indexがある場合は追加する
		if(!indexMap.isEmpty()) {
			for (Entry<String, ArrayList<String>> index : indexMap.entrySet()) {
				tblBuilder.append("CREATE INDEX IF NOT EXISTS ")
						  .append(index.getKey())
						  .append(" ON ").append(tblName)
						  .append("(");

				for (String indexColumn : index.getValue()) {
					tblBuilder.append(indexColumn).append(",");
				}

				tblBuilder.deleteCharAt(tblBuilder.length() - 1);
				tblBuilder.append(");");
			}
		}

		return tblBuilder.toString();
	}

	public static String getDropCreateQuery(Class<? extends ISqlite> clazz) {
		return getDropTableQuery(clazz) + getCreateTableQuery(clazz);
	}

	private static boolean isMainThread(Context cont) {
		return Thread.currentThread().equals(cont.getMainLooper().getThread());
	}
}
