package com.trkaynak.travelbook;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    static SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(this);//haritayı bağlamak için

       locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,15));

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String []{Manifest.permission.ACCESS_FINE_LOCATION},1);//eğer kullanıcı izin vermedi ise izin iste
        } else{
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
            //Mainden gelen bilgileri karşılama
            Intent intent = getIntent();
            String info = intent.getStringExtra("info");

            if(info.equalsIgnoreCase("new")){//eğer yeni bir yer ise
                mMap.clear();
                Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15));

            }else{//eski bir yere bakılacaksa listviewden
                mMap.clear();
               int position = intent.getIntExtra("position",0);
                LatLng location = new LatLng(MainActivity.location.get(position).latitude,MainActivity.location.get(position).longitude);
                mMap.addMarker(new MarkerOptions().position(location).title(MainActivity.names.get(position)));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location,15));

            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults.length > 0){
            if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);//ilk defa izin verildi ise yeri öğretniliyor.

                //kullanıcı ilk defe izin veriyor ise de Mainden geleni karşılanıyor
                Intent intent = getIntent();
                String info = intent.getStringExtra("info");

                if(info.equalsIgnoreCase("new")){//eğer yeni bir yer ise
                    mMap.clear();
                    Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15));

                }else{//eski bir yere bakılacaksa listviewden
                    mMap.clear();
                    int position = intent.getIntExtra("position",0);
                    LatLng location = new LatLng(MainActivity.location.get(position).latitude,MainActivity.location.get(position).longitude);
                    mMap.addMarker(new MarkerOptions().position(location).title(MainActivity.names.get(position)));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location,15));

                }
            }
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        mMap.clear();
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        String address="";

        try {
            List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude,1);
            if (addressList != null && addressList.size() >0 ){
                if(addressList.get(0).getThoroughfare() != null){
                    address += addressList.get(0).getThoroughfare();
                }
                if(addressList.get(0).getSubThoroughfare() != null){
                    address += addressList.get(0).getSubThoroughfare();
                }
            }else {
                address = "Not know place";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMap.addMarker(new MarkerOptions().position(latLng).title(address));

        //haritadan ekleme yapıldığından listenin güncellenmesi manuel olarak
        MainActivity.names.add(address);
        MainActivity.location.add(latLng);
        MainActivity.arrayAdapter.notifyDataSetChanged();

        Toast.makeText(getApplicationContext(),"New Place Create",Toast.LENGTH_LONG).show();

        try {
            //koordinatlar alınıyor
            Double l1 = latLng.latitude;
            Double l2 = latLng.longitude;
            //koordinatları saklamak için stringe dönüştürülüyor
            String coord1 = l1.toString();
            String coord2 = l2.toString();
            //database oluşturuluyor. adı Place
            database = this.openOrCreateDatabase("Places",MODE_PRIVATE,null);
            //veritabanı için tablolar oluşturuluyor.
            database.execSQL("CREATE TABLE IF NOT EXISTS places (name VARCHAR, latitude VARCHAR, longitude VARCHAR) ");
            //Veri kayıt etmek için. ne kayıt edileceği bilinmediği için ? işareti kullanılıyor
            String toCompile = "INSERT INTO places (name, latitude, longitude) VALUES (?,?,?)";
            //sql kodunu işleme almak için
            SQLiteStatement sqLiteStatement = database.compileStatement(toCompile);
            //Kayıt edilecek veriler
            sqLiteStatement.bindString(1,address);//1. adress
            sqLiteStatement.bindString(2,coord1);
            sqLiteStatement.bindString(3,coord2);

            //sql kod çalıtırmak için
            sqLiteStatement.execute();


        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
