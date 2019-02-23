package paul.sidekick;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class MainActivity extends AppCompatActivity {
    private boolean twelve = true;
    Background mService;
    boolean mBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("SideKick");

    }

    @Override
    protected void onStart() {
        super.onStart();
        Thread t = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateClock();
                            }
                        });
                        Thread.sleep(500);
                    }
                } catch (InterruptedException e) {
                }
            }
        };
        t.start();
        // Bind to LocalService
        Intent intent = new Intent(this, Background.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }
    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }
    
    protected void updateClock() {
        Calendar c = Calendar.getInstance();
        String formattedDate = "";
        if (twelve == true) {
            SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy h:mm:ss a");
            formattedDate = df.format(c.getTime());
        }
        if (twelve == false) {
            SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            formattedDate = df.format(c.getTime());
        }
        // Now we display formattedDate value in TextView
        TextView txtView = (TextView) findViewById(R.id.textView3);
        txtView.setText(formattedDate);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.bt_diag) {
            Intent i = new Intent(MainActivity.this, DeviceList.class);
            startActivity(i);

        }

        return super.onOptionsItemSelected(item);
    }

    public void ClockFormatSelect(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.twelve:
                if (checked) {
                    twelve = true;
                    if (mService.btSocket != null) {
                        try {
                            mService.btSocket.getOutputStream().write("0".getBytes());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
            case R.id.twentyfour:
                if (checked) {
                    twelve = false;
                    if (mService.btSocket != null) {
                        try {
                            mService.btSocket.getOutputStream().write("1".getBytes());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
        }
    }

    public void TempFormat(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.celcius:
                if (checked) {
                    if (mService.btSocket != null) {
                        try {
                            mService.btSocket.getOutputStream().write("3".getBytes());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
            case R.id.farenheit:
                if (checked) {
                    if (mService.btSocket != null) {
                        try {
                            mService.btSocket.getOutputStream().write("4".getBytes());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
        }
    }

    public void update(View view) {
            if (mService.btSocket != null) {
            Calendar c = Calendar.getInstance();
            String formattedDate = "";
            SimpleDateFormat df = new SimpleDateFormat("HHmmssddMMyyyy");
            formattedDate = df.format(c.getTime());
            try {
                mService.btSocket.getOutputStream().write(("T" + formattedDate).getBytes());
            } catch (IOException e) {
                e.printStackTrace();
                msg("Error");
            }
        }
    }

    // fast way to call Toast
    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }




    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        Thread.currentThread().interrupt();
        finish();
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            Background.LocalBinder binder = (Background.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

}