package ru.lpi_studio.lpitestapp.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import ru.lpi_studio.lpitestapp.POJOs.News;
import ru.lpi_studio.lpitestapp.R;

/**
 * Created by Titerin E.M. on 03.12.2014.
 */
public class NewsListAdapter extends ArrayAdapter {

    private static final String TAG = "NewsListAdapter";
    private LayoutInflater layoutInflater;
    private Context context;
    private DisplayImageOptions options;
    public ImageLoader imageLoader;

    public NewsListAdapter(Context context, int resource, List<News> items) {

        super(context, resource, items);
        Log.d(TAG, "Constructor");
        this.context=context;

        imageLoader=ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder()
                .cacheOnDisk(true) // Картинки кешируются в ПЗУ для оффлайн доступа...
                .cacheInMemory(true) //...и в память, для плавной прокрутки списка
                .build();
    }

    @Override
    public View getView (int position, View convertView, ViewGroup parent) {

        View view = convertView;

        if (view == null) {
            layoutInflater = LayoutInflater.from(context);
            view = layoutInflater.inflate(R.layout.list_row, parent, false);
        };

        //News news = getItem(position);
        // Что-то странное происходит, пришлось кастануть вручную
        News news = (News)getItem(position);

        if(news != null) {
            // Заполняем layout данными
            TextView txtNewsTitle = (TextView) view.findViewById(R.id.newsTitle);
            txtNewsTitle.setText(news.getTitle());

            TextView txtImageUrl = (TextView) view.findViewById(R.id.imageUrl);
            txtImageUrl.setText(news.getUrl());

            ImageView imageNews = (ImageView) view.findViewById(R.id.imageNews);
            imageLoader.displayImage(news.getUrl(),imageNews,options);
        }
        Log.d(TAG, "View on position " + Integer.toString(position) + " created.");
        return view;
    }
}
