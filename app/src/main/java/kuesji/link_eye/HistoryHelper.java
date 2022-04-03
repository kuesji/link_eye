package kuesji.link_eye;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class HistoryHelper {

	public class HistoryModel {
		public int id = 0;
		public int epoch = 0;
		public String content = "";

		public HistoryModel(){}
		public HistoryModel(int id, int epoch, String content){
			this.id = id;
			this.epoch = epoch;
			this.content = content;
		}
	}

	private Context context;
	public SQLiteDatabase database;

	public HistoryHelper(Context context){
		this.context = context;

		database = context.openOrCreateDatabase("app.db",Context.MODE_PRIVATE,null);
		database.execSQL("create table if not exists history ( id integer primary key, date integer not null, content text not null );");
	}

	public void close(){
		database.close();
	}

	public void insert(String content){
		ContentValues values = new ContentValues();
		values.put("date",System.currentTimeMillis()/1000);
		values.put("content",content);
		database.insert("history",null,values);
	}

	public List<HistoryModel> list(){
		List<HistoryModel> result = new ArrayList<>();

		Cursor cursor = database.rawQuery("select id,date,content from history order by date desc",null);

		if( cursor.moveToFirst() ) {
			do {
				result.add(new HistoryModel(cursor.getInt(0),cursor.getInt(1),cursor.getString(2)));
			} while (cursor.moveToNext());
		}

		return result;
	}

	public List<HistoryModel> search(String query){
		List<HistoryModel> result = new ArrayList<>();

		Cursor cursor = database.rawQuery("select id,date,content from history where content like ? order by date desc",new String[]{"%"+query+"%"});

		if( cursor.moveToFirst() ) {
			do {
				result.add(new HistoryModel(cursor.getInt(0),cursor.getInt(1),cursor.getString(2)));
			} while (cursor.moveToNext());
		}

		return result;
	}

	public void delete(int id){
		database.delete("history","id=?",new String[]{String.valueOf(id)});
	}

	public void clear(){
		database.execSQL("delete from history");
	}
}
