package ru.lpi_studio.lpitestapp;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import java.net.URL;
import java.net.URLConnection;

import java.util.ArrayList;
import java.util.List;

import ru.lpi_studio.lpitestapp.POJOs.News;
import ru.lpi_studio.lpitestapp.adapters.NewsListAdapter;
import ru.lpi_studio.lpitestapp.database.NewsORM;

/**
 * Created by Titerin E.M. on 03.12.2014.
 */
public class MainActivity extends ListActivity {

    private static final String loadURL = "http://api.innogest.ru/api/v3/amobile/news";
    private static final String TAG = "MainActivity";

    Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Constructor");
        setContentView(R.layout.activity_main);
        context = this;
        // Конфигурируем менеджер картинок
        configImageLoader();


        // Получаем список записей из БД
        List<News> newsListFromDB = NewsORM.getNews(this);

        if(newsListFromDB.isEmpty()/*doesDatabaseExist(this,DatabaseWrapper.getName())*/) { // ...если БД пуста
            // ...скачиваем новости и заполняем БД (см. onPostExecute)
            new RetrieveNews().execute(loadURL);
        } else{ // ...если в БД были записи, выводим их
            setListAdapter(new NewsListAdapter(this, R.layout.list_row, newsListFromDB));
        }

    }


    private static boolean doesDatabaseExist(ContextWrapper context, String dbName) {
        File dbFile = context.getDatabasePath(dbName);
        return dbFile.exists();
    }

    /**
     * Конфигуратор менеджера картинок UIL
     */
    private void configImageLoader(){

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .threadPoolSize(5)
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new WeakMemoryCache())
                .discCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .build();
        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config);
        ImageLoader.getInstance().handleSlowNetwork(true);

    }

    /**
     * Asynctask, выполняющий запрос к серверу, парсящий ответ и заполняющий БД
     */
    private class RetrieveNews  extends AsyncTask<String, Void, String> {

        private final List<News> newsList = new ArrayList<News>();
        private String sError = null;
        private ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        String data ="";
        String TAG = "RetrieveNews";

        protected void onPreExecute() {
            // Включаем крутилку загрузки JSONа
            // дабы юзер ощущал загрузку :)
            progressDialog.setMessage("Загружаю новости...");
            progressDialog.show();
            Log.d(TAG, "onPreExecute");
        }

        protected String doInBackground(String... urls) {

            BufferedReader bufferedReader=null; // Дабы был доступен в секции finally
            StringBuilder stringBuilder = new StringBuilder();
            Log.d(TAG, "doInBackGround " + "begins...");

            try
            {
                // Забираем адрес запроса из параметров...
                URL url = new URL(urls[0]);

                // Посылаем POST запрос
                URLConnection urlConnection = url.openConnection();
                urlConnection.setDoOutput(true);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(urlConnection.getOutputStream());
                outputStreamWriter.write( data );
                outputStreamWriter.flush();

                // Читаем ответ сервера...
                bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                //stringBuilder = new StringBuilder();
                String line;

                // Поскольку строк может быть очень много - используем StringBuilder
                // для увеличения скорости обработки ответа
                while((line = bufferedReader.readLine()) != null)
                {
                    stringBuilder.append(line);
                }
            }
            catch(Exception e)
            {
                sError = e.getMessage();
                e.printStackTrace();
            }
            finally
            {
                try
                {
                    bufferedReader.close();
                }

                catch(Exception e) {
                    e.printStackTrace();
                }
            }
            Log.d(TAG, "doInBackGround " + "ends...");
            // Передаем результирующую строку с ответом сервера в onPostExecute
            return stringBuilder.toString();
        }

        protected void onPostExecute(String response) {
            Log.d(TAG, "onPostExecute " + "begins...");
            // Прекращаем отображать загрузку
            progressDialog.dismiss();

            if (sError != null) {

                // TODO: как-то обработать ошибку (напр. toast)

            } else {

                // Если всё успешно загрузилось, можно парсить полученный JSON
                /**
                 * По-хорошему, если список новостей предполагается достаточно большим,
                 * его нужно парсить в отдельном таске. В данном случае не делаю из-за
                 * ограниченности времени задания, в реальном проекте будет учтено.
                 */

                try {
                    // Создаем JSON массив из ответа сервера
                    JSONArray jsonArray = new JSONArray(response);
                    int jsonArrayLength = jsonArray.length();
                    // Парсим новостные записи...
                    for(int i = 0; i < jsonArrayLength; i++){
                        JSONObject tempObj = jsonArray.getJSONObject(i);
                        int nId = tempObj.getInt("nid");
                        String newsTitle = tempObj.getString("title");
                        String imgURL = tempObj.getString("img_url");
                        // Добавляем очередную запись в список новостей
                        newsList.add(new News(nId,newsTitle,imgURL));
                    };


                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }

            // Отображаем спарсенное
            setListAdapter(new NewsListAdapter(context, R.layout.list_row, newsList));
            // ...наконец, заполняем БД
            for(News news : newsList) {
                NewsORM.insertNews(context, news);
            }
            //listViewNews.setAdapter(new NewsListAdapter(MainActivity.this, R.layout.list_row, newsList));
            //setListAdapter(new NewsListAdapter(context, R.layout.list_row, newsList));
            //listNews = newsList;
            Log.d(TAG, "onPostExecute " + "ends...");
        }

    }

}
