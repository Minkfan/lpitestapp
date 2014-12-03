package ru.lpi_studio.lpitestapp.POJOs;

/**
 * Created by Titerin E.M. on 03.12.2014.
 */
public class News {
    // Используется для индексирования в БД
    private final int nid;

    private final String title;
    private final String url;


    public News(int nid, String title, String url)  {
        super();
        this.nid = nid;
        this.title = title;
        this.url = url;
    }

    public int getNid() {
        return nid;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }
}
