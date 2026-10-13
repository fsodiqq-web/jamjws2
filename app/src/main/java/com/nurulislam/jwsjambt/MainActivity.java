package com.nurulislam.jwsjambt;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends Activity {
    private final UUID SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter adapter;
    private BluetoothSocket socket;
    private OutputStream output;
    private TextView status;
    private Spinner devices;
    private final ArrayList<BluetoothDevice> paired = new ArrayList<>();
    private EditText n1,n2,n3,day,month,year,hour,minute,second,weekday,brightDay,brightNight,speed,jam;
    private CheckBox bus;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        adapter = BluetoothAdapter.getDefaultAdapter();
        buildUi();
        if (adapter == null) { status.setText("Perangkat ini tidak mendukung Bluetooth."); return; }
        if (Build.VERSION.SDK_INT >= 31 && checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 101);
        else refreshDevices();
    }

    private int dp(float v){ return (int)(v*getResources().getDisplayMetrics().density+0.5f); }
    private TextView text(String s, int size, boolean bold) { TextView t=new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(Color.rgb(25,42,58)); if(bold)t.setTypeface(null,Typeface.BOLD); t.setPadding(0,dp(7),0,dp(5)); return t; }
    private EditText edit(String hint, String val) { EditText e=new EditText(this); e.setSingleLine(true); e.setText(val); e.setHint(hint); e.setTextSize(16); e.setInputType( android.text.InputType.TYPE_CLASS_TEXT); return e; }
    private LinearLayout vertical(){ LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); l.setPadding(dp(16),dp(10),dp(16),dp(20)); return l; }
    private void add(LinearLayout root, View v){ root.addView(v,new LinearLayout.LayoutParams(-1,-2)); }
    private Button button(String label, Runnable action){ Button b=new Button(this); b.setText(label); b.setOnClickListener(v->action.run()); return b; }
    private void buildUi(){
        ScrollView scroll=new ScrollView(this); LinearLayout root=vertical(); root.setBackgroundColor(Color.rgb(245,248,251)); scroll.addView(root); setContentView(scroll);
        TextView title=text("JWS JAM BLUETOOTH",22,true); title.setTextColor(Color.rgb(13,43,69)); add(root,title);
        add(root,text("Pengendali jam P10 Arduino • Bluetooth serial HC-05/HC-06",13,false));
        status=text("Status: belum terhubung",14,true); status.setTextColor(Color.rgb(21,101,192)); add(root,status);
        devices=new Spinner(this); add(root,devices);
        LinearLayout pair=new LinearLayout(this); pair.setOrientation(LinearLayout.HORIZONTAL);
        Button refresh=button("Muat ulang",this::refreshDevices); Button connect=button("Hubungkan",this::connect);
        pair.addView(refresh,new LinearLayout.LayoutParams(0,-2,1)); pair.addView(connect,new LinearLayout.LayoutParams(0,-2,1)); add(root,pair);
        add(root,text("Nama yang tampil",18,true));
        n1=edit("Nama 1",""); n2=edit("Nama 2",""); n3=edit("Nama 3","");
        add(root,n1); add(root,button("Simpan Nama 1",()->send("N1"+n1.getText().toString()))); add(root,n2); add(root,button("Simpan Nama 2",()->send("N2"+n2.getText().toString()))); add(root,n3); add(root,button("Simpan Nama 3",()->send("N3"+n3.getText().toString())));
        add(root,text("Atur waktu RTC",18,true));
        LinearLayout dates=row(); day=edit("Tgl","1"); month=edit("Bln","1"); year=edit("Thn","26"); hour=edit("Jam","0"); minute=edit("Mnt","0"); second=edit("Dtk","0"); weekday=edit("Hari 1-7","1");
        dates.addView(day,weight()); dates.addView(month,weight()); dates.addView(year,weight()); add(root,dates);
        LinearLayout times=row(); times.addView(hour,weight()); times.addView(minute,weight()); times.addView(second,weight()); times.addView(weekday,weight()); add(root,times);
        add(root,text("Hari: 1=Minggu, 2=Senin, ... 7=Sabtu",12,false));
        add(root,button("Isi dari jam HP",this::fillNow));
        add(root,button("Kirim Waktu",()->send("SDT"+two(day)+two(month)+two(year)+two(hour)+two(minute)+two(second)+one(weekday))));
        add(root,text("Kecerahan & tampilan",18,true));
        brightDay=edit("Kecerahan siang 0-1023","150"); brightNight=edit("Kecerahan malam 0-1023","30"); speed=edit("Kecepatan teks 10-200 ms","45"); jam=edit("Durasi jam besar 3-60 detik","10");
        add(root,brightDay); add(root,button("Simpan Kecerahan Siang",()->send("BS"+brightDay.getText().toString())));
        add(root,brightNight); add(root,button("Simpan Kecerahan Malam",()->send("BM"+brightNight.getText().toString())));
        add(root,speed); add(root,button("Simpan Kecepatan Teks",()->send("SP"+speed.getText().toString())));
        add(root,jam); add(root,button("Simpan Durasi Jam",()->send("JM"+jam.getText().toString())));
        bus=new CheckBox(this); bus.setText("Aktifkan animasi bus"); bus.setChecked(true); add(root,bus); add(root,button("Simpan Status Bus",()->send(bus.isChecked()?"BUS1":"BUS0")));
        add(root,text("Informasi & reset",18,true)); add(root,button("Baca Pengaturan (?)",()->send("?"))); add(root,button("Reset ke Pengaturan Awal",()->new android.app.AlertDialog.Builder(this).setTitle("Reset pengaturan?").setMessage("Semua pengaturan akan kembali ke nilai bawaan di Arduino.").setNegativeButton("Batal",null).setPositiveButton("Reset",(d,w)->send("RESET")).show()));
        fillNow();
        add(root,text("Catatan: pasangkan modul Bluetooth dari Pengaturan Android terlebih dahulu. Aplikasi ini mengirim perintah ke sketch JWS_Jam_BT.ino melalui Bluetooth serial.",12,false));
    }
    private void fillNow(){
        java.util.Calendar c=java.util.Calendar.getInstance();
        day.setText(String.valueOf(c.get(java.util.Calendar.DAY_OF_MONTH)));
        month.setText(String.valueOf(c.get(java.util.Calendar.MONTH)+1));
        year.setText(String.valueOf(c.get(java.util.Calendar.YEAR)%100));
        hour.setText(String.valueOf(c.get(java.util.Calendar.HOUR_OF_DAY)));
        minute.setText(String.valueOf(c.get(java.util.Calendar.MINUTE)));
        second.setText(String.valueOf(c.get(java.util.Calendar.SECOND)));
        weekday.setText(String.valueOf(c.get(java.util.Calendar.DAY_OF_WEEK)));
    }
    private LinearLayout row(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.HORIZONTAL);return l;}
    private LinearLayout.LayoutParams weight(){return new LinearLayout.LayoutParams(0,-2,1);}
    private String digits(EditText e,int count){String s=e.getText().toString().trim(); if(s.isEmpty())s="0"; try{s=String.format(java.util.Locale.US,"%0"+count+"d",Integer.parseInt(s));}catch(Exception ex){s="";for(int i=0;i<count;i++)s+="0";} return s.length()>count?s.substring(s.length()-count):s;}
    private String two(EditText e){return digits(e,2);} private String one(EditText e){return digits(e,1);}
    private void refreshDevices(){
        if(adapter==null)return;
        try { if(Build.VERSION.SDK_INT>=31 && checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT},101);return;}
            paired.clear(); ArrayList<String> labels=new ArrayList<>(); Set<BluetoothDevice> set=adapter.getBondedDevices();
            for(BluetoothDevice d:set){paired.add(d); labels.add(d.getName()+"\n"+d.getAddress());}
            if(labels.isEmpty())labels.add("Tidak ada perangkat terpasang / paired");
            devices.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,labels)); status.setText("Perangkat tersimpan: "+paired.size());
        }catch(SecurityException e){status.setText("Izin Bluetooth belum diberikan.");}
    }
    private void connect(){
        if(adapter==null)return;
        if(Build.VERSION.SDK_INT>=31 && checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT},101);return;}
        int pos=devices.getSelectedItemPosition(); if(pos<0||pos>=paired.size()){status.setText("Pilih perangkat Bluetooth yang sudah dipasangkan.");return;}
        final BluetoothDevice d=paired.get(pos); status.setText("Menghubungkan ke "+d.getName()+"...");
        new Thread(()->{try{ if(socket!=null)socket.close(); BluetoothSocket s=d.createRfcommSocketToServiceRecord(SPP); s.connect(); OutputStream o=s.getOutputStream(); runOnUiThread(()->{socket=s;output=o;status.setText("Terhubung: "+d.getName());}); }catch(Exception e){runOnUiThread(()->status.setText("Gagal terhubung. Pastikan modul aktif dan sudah paired."));}}).start();
    }
    private void send(String command){
        if(output==null){status.setText("Belum terhubung ke Bluetooth.");return;}
        final OutputStream out=output;
        new Thread(()->{try{out.write((command+"\n").getBytes(StandardCharsets.UTF_8));out.flush();runOnUiThread(()->status.setText("Perintah terkirim: "+command));}catch(Exception e){runOnUiThread(()->status.setText("Gagal mengirim perintah."));}}).start();
    }
    @Override public void onRequestPermissionsResult(int r,String[] p,int[] g){super.onRequestPermissionsResult(r,p,g);if(r==101){if(g.length>0&&g[0]==PackageManager.PERMISSION_GRANTED)refreshDevices();else status.setText("Izin Bluetooth diperlukan untuk menyambungkan perangkat.");}}
    @Override protected void onDestroy(){super.onDestroy();try{if(socket!=null)socket.close();}catch(Exception ignored){}}
}
