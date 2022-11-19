package com.example.mymusic;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    TextView noMusicTextView;
    // Создадим список аудиомоделей
    ArrayList<AudioModel> songsList = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // RecyclerView равен просмотру поиска по индетификатору
        recyclerView = findViewById(R.id.recycler_view);
        // Просмотр текста (списка) без музыки равен поиску по индетификатору
        noMusicTextView = findViewById(R.id.no_songs_text);


        // Метод проверки разрешения
        if(checkPermission() == false){
            // У нас нет разрешения, тогда мы его запросим и и вернемся на стадию запроса разрешения
            requestPermission();
            return;
        }

        // Cписок музыки
        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA, // путь
                MediaStore.Audio.Media.DURATION // Продолжительность
        };

        // Создодим строку выбора музыки
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != @"; // Объеденим это с услувием, которое не будет равно нулю

        // Если у нас есть разрешение, то мы получим весь список музыки из хранилища и получим к нему доступ
        // курсор равен получению запроса контента, мы запросим базу данных, где мы будем хранить внешний URI контента,
        // так с помощью этого URI адрессы мы будем запрашивать все музыкальные файлы
        //Все данные музыкального файла будут сохранены в cursor
        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, null);
        while (cursor.moveToNext()){
            // Получим все песни в songDate
            AudioModel songData = new AudioModel(cursor.getString(1), cursor.getString(0), cursor.getString(2));
            // Проверим существует ли песня
            if(new File(songData.getPath()).exists())
                songsList.add(songData);
        }

        // Проверим пус ли список музыки
        if (songsList.size() == 0){
            noMusicTextView.setVisibility(View.VISIBLE);
        }else{
            // Иначе покажем список песен в recyclerView
            // recyclerView                   LinearLayoutManager - менеджер линейной компоновки
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            // Установим адаптер
            recyclerView.setAdapter(new MusicListAdapter(songsList, getApplicationContext()));
        }


    }

    /** Чтобы получить все музыкальные файлы из хранилища нам нужно разрешение пользователя
     Пользователь должен дать согласие, тогда появится мписок музыкальных файлов */

    // Создадим метод проверки разрешения пользователя

    boolean checkPermission(){
        // если у нас еще нет разешения, мы его запросим
        int result = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if(result == PackageManager.PERMISSION_GRANTED){
            return true;
        } else {
            return false;
        }
    }

    // запрос разрешения
    void requestPermission(){
        //Случай когда пользователь отказал в разрешении, мы выведем текс, что пользователь не дал разрешения
        // и попросим разрешить доступ к списку музыки
        if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)){
            //Выведем что требуется разрешение на чтение
            Toast.makeText(MainActivity.this, "Read permission is requierd!", Toast.LENGTH_SHORT).show();

        } else
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 123);
    }

}
//проверка