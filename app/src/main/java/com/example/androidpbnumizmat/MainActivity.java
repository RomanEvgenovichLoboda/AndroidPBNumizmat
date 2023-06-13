package com.example.androidpbnumizmat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    Animation animation;
    public boolean isDark = false;
    boolean check = false;
    SQLiteDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        animation = AnimationUtils.loadAnimation(this, R.anim.animate);
        loadApplication();

        db = getBaseContext().openOrCreateDatabase("app.db", MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS themes ( theme TEXT)");

        Cursor query = db.rawQuery("SELECT * FROM themes;", null);
        String txt="";
        while(query.moveToNext()){
            txt=query.getString(0);
        }
        if(txt.equals("MODE_NIGHT_YES")){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            isDark=true;
        }
        else if(txt.equals("MODE_NIGHT_NO")){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            isDark=false;
        }
        query.close();
        db.close();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //ContentValues values=new ContentValues();
        //TextView headerView = findViewById(R.id.selectedMenuItem);
        switch(id){
            case R.id.action_settings :
                //headerView.setText("Настройки");
                Toast.makeText(getApplicationContext(),"Settings", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.open_settings:
                //headerView.setText("Открыть");
                Toast.makeText(getApplicationContext(),"Open", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.save_settings:
                //headerView.setText("Сохранить");
                Toast.makeText(getApplicationContext(),"Save", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.change_theme:
//                if(isDark) {
//                    //setTheme(R.style.Theme_AndroidThemes);
//                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
//                    isDark=false;
//                    values.put("theme","MODE_NIGHT_NO");
//                    db = getBaseContext().openOrCreateDatabase("app.db", MODE_PRIVATE, null);
//                    db.insert("themes",null,values);
//                    db.close();
//                }
//                else {
//                    //setTheme(com.google.android.material.R.style.Base_Theme_Material3_Dark);
//                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
//                    isDark=true;
//                    values.put("theme","MODE_NIGHT_YES");
//                    db = getBaseContext().openOrCreateDatabase("app.db", MODE_PRIVATE, null);
//                    db.insert("themes",null,values);
//                    db.close();
//                }
                changeTheme();
                return true;
        }
        //headerView.setText(item.getTitle());
        return super.onOptionsItemSelected(item);
    }

    private ArrayList<Coin> parseHtml() throws IOException {
        ArrayList<Coin> coins = new ArrayList<>();
        Document doc = Jsoup.connect("https://privatbank.ua/premium-banking/coins").get();

        Element content = doc.getElementsByClass("coins-container").get(0);
        Elements items = content.getElementsByClass("lazy-coin");
        for (Element item : items) {
            String Text = item.getElementsByClass("bold-text").text();
            String Price = item.getElementsByClass("coin-name").text();
            Coin coin = new Coin();
            coin.Text = Text;
            coin.Price = Price;
            coins.add(coin);
        }

        int i = 0;
        Elements links = content.getElementsByClass("images-coins");
        for (Element link : links) {
            Elements images = link.getElementsByTag("img");
            String front = images.get(0).attr("data-src");
            String back = images.get(1).attr("data-src");
            coins.get(i).Front = front;
            coins.get(i).Back = back;

            i++;
        }
        return coins;
    }

    private void loadApplication(){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    ArrayList<Coin> coins = parseHtml();
                    for (Coin coin: coins) {
                        coin.PictureFront = getBitMap(coin.Front);
                        coin.PictureBack = getBitMap(coin.Back);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                CardsAdapter adapter = new CardsAdapter( getBaseContext().getApplicationContext(), coins, animation);
                                ListView listView = (ListView) findViewById(R.id.listView);
                                listView.setAdapter(adapter);

                            }catch (Exception ex){
                                System.out.println(ex.toString());
                            }

                        }
                    });

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private Bitmap getBitMap(String url) throws IOException {
        URL imgUrl = new URL(url);
        return BitmapFactory.decodeStream(imgUrl.openConnection().getInputStream());
    }

    private void setAnimate(String url_1, String url_2) throws IOException {

        Bitmap bitMap1 = getBitMap(url_1);
        Bitmap bitMap2 = getBitMap(url_2);

        // create background 1
        AnimationDrawable animationDrawable_1 = new AnimationDrawable();
        animationDrawable_1.setOneShot(true);
        animationDrawable_1.addFrame(new BitmapDrawable(bitMap1), 1000);
        animationDrawable_1.addFrame(new BitmapDrawable(bitMap2), 1000);

        // create background 2
        AnimationDrawable animationDrawable_2 = new AnimationDrawable();
        animationDrawable_2.setOneShot(true);
        animationDrawable_2.addFrame(new BitmapDrawable(bitMap2), 1000);
        animationDrawable_2.addFrame(new BitmapDrawable(bitMap1), 1000);

        setImage(animationDrawable_1, animationDrawable_2);

    }

    private void setImage(AnimationDrawable animationDrawable_1, AnimationDrawable animationDrawable_2){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try{
                    ImageView imageView = (ImageView) findViewById(R.id.imageView);
                    // set background 1
                    imageView.setBackground(animationDrawable_1);

                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(!check){
                                check = true;
                                imageView.setBackground(animationDrawable_1);
                                animationDrawable_1.start();
                                imageView.startAnimation(animation);
                            }
                            else{
                                check = false;
                                imageView.setBackground(animationDrawable_2);
                                animationDrawable_2.start();
                                imageView.startAnimation(animation);
                            }
                        }
                    });
                }catch (Exception ex){
                    System.out.println(ex.toString());
                }

            }
        });
    }



    public void changeTheme() {

        ContentValues values=new ContentValues();
        if(isDark) {
            //setTheme(R.style.Theme_AndroidThemes);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            isDark=false;
            values.put("theme","MODE_NIGHT_NO");
            db = getBaseContext().openOrCreateDatabase("app.db", MODE_PRIVATE, null);
            db.insert("themes",null,values);
            db.close();
        }
        else {
            //setTheme(com.google.android.material.R.style.Base_Theme_Material3_Dark);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            isDark=true;
            values.put("theme","MODE_NIGHT_YES");
            db = getBaseContext().openOrCreateDatabase("app.db", MODE_PRIVATE, null);
            db.insert("themes",null,values);
            db.close();
        }
    }
}