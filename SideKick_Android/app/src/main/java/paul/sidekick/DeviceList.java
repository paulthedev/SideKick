package paul.sidekick;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.Set;


public class DeviceList extends AppCompatActivity
{

    //widgets
    ListView devicelist;
    //Bluetooth
    private BluetoothAdapter myBluetooth = null;
    private Set<BluetoothDevice> pairedDevices;
    public static String EXTRA_ADDRESS = "device_address";
    private SwipeRefreshLayout swipeContainer;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Choose Paired Device");

        //Calling widgets
        devicelist = (ListView)findViewById(R.id.listView);

        //if the device has bluetooth
        myBluetooth = BluetoothAdapter.getDefaultAdapter();

        if(myBluetooth == null)
        {
            //Show a mensag. that the device has no bluetooth adapter
            Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();

            //finish apk
            finish();
        }
        else if(!myBluetooth.isEnabled())
        {
            //Ask to the user turn the bluetooth on
            Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnBTon,1);
        }

        // Lookup the swipe container view
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pairedDevicesList();
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_red_light);

    }

    @Override
    protected void onStart() {
        super.onStart();
        pairedDevicesList();
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
    protected void pairedDevicesList()
    {
        pairedDevices = myBluetooth.getBondedDevices();
        ArrayList list = new ArrayList();

        if (pairedDevices.size()>0)
        {
            list.clear();
            for(BluetoothDevice bt : pairedDevices)
            {

                list.add(bt.getName() + "\n" + bt.getAddress()); //Get the device's name and the address

                final ArrayAdapter adapter = new ArrayAdapter(this,R.layout.custom_layout, list);
                devicelist.setAdapter(adapter);
                devicelist.setOnItemClickListener(myListClickListener); //Method called when the device from the list is clicked
            }
        }
        else
        {
            list.clear();
            list.add("No Paired Bluetooth Devices Found.\nPair a device from bluetooth settings first.");

            final ArrayAdapter adapter = new ArrayAdapter(this,R.layout.custom_layout, list);
            devicelist.setAdapter(adapter);
        }
        swipeContainer.setRefreshing(false);

    }

    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener()
    {
        public void onItemClick (AdapterView<?> av, View v, int arg2, long arg3)
        {
            // Get the device MAC address, the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            // Make an intent to start next activity.
            Intent i = new Intent(getBaseContext(), Background.class);

            //Change the activity.
            i.putExtra("address", address);
            startService(i);
            Intent myIntent = new Intent(getBaseContext(),MainActivity.class);
            startActivity(myIntent);
            finish();
        }
    };

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        Intent myIntent = new Intent(getBaseContext(),MainActivity.class);
        startActivity(myIntent);
        finish();
    }
}


