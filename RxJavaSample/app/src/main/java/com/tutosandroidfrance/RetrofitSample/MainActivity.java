package com.tutosandroidfrance.RetrofitSample;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;

import java.util.List;

import retrofit.RestAdapter;
import retrofit.android.AndroidLog;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;


public class MainActivity extends ActionBarActivity {

    GithubService githubService;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textView);

        githubService = new RestAdapter.Builder()
                .setEndpoint(GithubService.ENDPOINT)
                .setLog(new AndroidLog("retrofit"))
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build()
                .create(GithubService.class);

        getListRepos();
    }

    private void getListRepos() {
        githubService.listRepos("florent37")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())

                //on commence par couper notre observable de List<Repo> en observable de Repo
                .flatMap(new Func1<List<Repo>, Observable<Repo>>() {
                    @Override
                    public Observable<Repo> call(List<Repo> repos) {return Observable.from(repos);}
                })
                //afin de récupérer le nom de chaque repo, nous aurons donc des Observable<String>
                .map(new Func1<Repo, String>() {
                    @Override
                    public String call(Repo repo) {
                        return repo.getName();
                    }
                })

                //on trie nos Observable<String>
                .toSortedList(new Func2<String, String, Integer>() {
                    @Override
                    public Integer call(String s, String s2) { return s.compareTo(s2); }
                })

                //on coupe l'observable de List<String> en Observable<String>
                .flatMap(new Func1<List<String>, Observable<String>>() {
                    @Override
                    public Observable<String> call(List<String> strings) { return Observable.from(strings);
                    }
                })
                //afin d'accéder à la méthode reduce, permettant de réduire nos noms, de la forme nom1 \n nom2
                .reduce(new Func2<String, String, String>() {
                    @Override
                    public String call(String s, String s2) {
                        return s + "\n" + s2;
                    }
                })

                //on obtient à la fin un String contenant la liste de nos noms de repo, que l'on va afficher
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        textView.setText(s);
                    }
                });
    }
}
