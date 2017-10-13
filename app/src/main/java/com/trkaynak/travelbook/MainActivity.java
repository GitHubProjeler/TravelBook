package com.trkaynak.travelbook;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.function.DoubleUnaryOperator;

public class MainActivity extends AppCompatActivity {

   static ArrayList<String> names = new ArrayList<String>();
   static ArrayList<LatLng> location = new ArrayList<LatLng>();
    ArrayAdapter arrayAdapter;

    @Override//menü tanımlaması yapılıyor
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.add_place,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override//menü seçilince ne olacak
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add_place){

            //intent
            Intent intent=new Intent(getApplicationContext(),MapsActivity.class);
            intent.putExtra("info","new");
            startActivity(intent);

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView listView = (ListView) findViewById(R.id.listView);

        //database den veriler çekmek için
        try{
            //veritabanına ulaşılıyor
            MapsActivity.database = this.openOrCreateDatabase("Places",MODE_PRIVATE,null);
            //veriler çekiliyor
            Cursor cursor =  MapsActivity.database.rawQuery("SELECT * FROM Places",null);
            //indexlere ulaşılması için
            int nameIx = cursor.getColumnIndex("name");//name adındaki sütuna ulaşılıyor.
            int latitudeIx = cursor.getColumnIndex("latitude");//latitude sütuna ulaşılıyor
            int longitudeIx = cursor.getColumnIndex("longitude");//longitude ulaşılıyor.

            //ilk kayıta gidiliyor
            cursor.moveToFirst();
            //
            while (cursor != null){//imleç boş bir yerde değilse döngüye giriyor.
                String nameFromDatabase = cursor.getString(nameIx);//veritabanından gelen name aktarılıyor
                String latitudeFromDatabase = cursor.getColumnName(latitudeIx);
                String longitudeFromDatabase = cursor.getColumnName(longitudeIx);

                //Arraylara ekleniyor.
                names.add(nameFromDatabase);
                //koordinatlar double çevriliyor
                Double l1 = Double.parseDouble(latitudeFromDatabase);
                Double l2 = Double.parseDouble(longitudeFromDatabase);
                //koordinatlar listeye ekleniyor
                LatLng locationFromDatabase = new LatLng(l1,l2);
                location.add(locationFromDatabase);
                //başka data varsa sonrakine geç
                cursor.moveToNext();
            }


        }catch (Exception e){
            e.printStackTrace();
        }
        //Arrayadapter ile listviewde  gösterme
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, names);
        listView.setAdapter(arrayAdapter);

        //listeye tıklayınca ilgili yere ulaşmak
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                intent.putExtra("info", "old");

                //diğer aktivity ye koordinat gönderiliyor
                intent.putExtra("position",position);
                startActivity(intent);
            }
        });
    }
}
