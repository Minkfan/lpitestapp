package ru.lpi_studio.lpitestapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import ru.lpi_studio.lpitestapp.POJOs.News;

/**
 * Created by Titerin E.M. on 03.12.2014.
 * Объектно-реляционное отображение (Object-Relational Mapper)
 * базы новостей
 */
public class NewsORM {

    private static final String TAG = "NewsORM";

    private static final String TABLE_NAME = "news";

    private static final String COMMA_SEP = ", ";

    private static final String COLUMN_NID_TYPE = "INTEGER PRIMARY KEY";
    private static final String COLUMN_NID = "nid";

    private static final String COLUMN_TITLE_TYPE = "TEXT";
    private static final String COLUMN_TITLE = "title";

    private static final String COLUMN_URL_TYPE = "TEXT";
    private static final String COLUMN_URL = "url";



    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_NID  + " " + COLUMN_NID_TYPE + COMMA_SEP +
                    COLUMN_TITLE  + " " + COLUMN_TITLE_TYPE + COMMA_SEP +
                    COLUMN_URL + " " + COLUMN_URL_TYPE +
                    ")";

    public static final String SQL_DROP_TABLE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    /**
     * Добавляет объект News в базу
     * @param context
     * @param news
     */
    public static void insertNews(Context context, News news) {
        DatabaseWrapper databaseWrapper = new DatabaseWrapper(context);
        SQLiteDatabase database = databaseWrapper.getWritableDatabase();

        ContentValues values = postToContentValues(news);
        long newsId = database.insert(NewsORM.TABLE_NAME, "null", values);
        Log.d(TAG, "Inserted new News with ID: " + newsId);

        database.close();
    }

    /**
     * Обертываем объект News в ContentValues map для использования с SQL inserts.
     */
    private static ContentValues postToContentValues(News post) {
        ContentValues values = new ContentValues();
        values.put(NewsORM.COLUMN_NID, post.getNid());
        values.put(NewsORM.COLUMN_TITLE, post.getTitle());
        values.put(NewsORM.COLUMN_URL, post.getUrl());

        return values;
    }

    /**
     * Возвращает весь список новостей из базы
     * @param context
     * @return
     */
    public static List<News> getNews(Context context) {
        DatabaseWrapper databaseWrapper = new DatabaseWrapper(context);
        SQLiteDatabase database = databaseWrapper.getReadableDatabase();

        Cursor cursor = database.rawQuery("SELECT * FROM " + NewsORM.TABLE_NAME, null);

        Log.d(TAG, "Loaded " + cursor.getCount() + " News...");
        List<News> newsList = new ArrayList<News>();

        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                News news = cursorToNews(cursor);
                newsList.add(news);
                cursor.moveToNext();
            }
            Log.d(TAG, "News loaded successfully.");
        }

        database.close();

        return newsList;
    }

    /**
     * Создает объект News из Cursor
     * @param cursor
     * @return
     */
    private static News cursorToNews(Cursor cursor) {
        News news = new News(
                             cursor.getInt(cursor.getColumnIndex(COLUMN_NID)),
                             cursor.getString(cursor.getColumnIndex(COLUMN_TITLE)),
                             cursor.getString(cursor.getColumnIndex(COLUMN_URL))
                            );

        return news;
    }
}
