package paul.sidekick;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;
import java.io.IOException;
import java.util.UUID;

public class Background extends Service {

    private String address = null;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    //SPP UUID. Look for it
    private static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        Background getService() {
            // Return this instance of LocalService so clients can call public methods
            return Background.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    @Override
    public int onStartCommand(Intent i, int flags, int startId) {
        address = i.getStringExtra("address");
        if (address != null) {
          new ConnectBT().execute(); //Call the class to connect
        }
        return START_NOT_STICKY;
    }

        public void onDestroy(){
        super.onDestroy();
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
     private boolean ConnectSuccess = true; //if it's here, it's almost connected

     @Override
     protected void onPreExecute() {

    }

    @Override
    protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
    {
        try {
            if (btSocket == null || !isBtConnected) {
                myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                BluetoothDevice deviceToConnect = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                btSocket = deviceToConnect.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                btSocket.connect();

            }
        } catch (IOException e) {
            ConnectSuccess = false;//if the try failed, you can check the exception here
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
    {
        super.onPostExecute(result);

        if (!ConnectSuccess) {
            msg("Connection Failed.\nSelect a SPP Bluetooth device \nor check if the device is On");
        } else {
            msg("Connected.");
            isBtConnected = true;
        }

    }
    }

    protected void interceptcall(int register) {
        TelephonyManager myTelManager =
                (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        MyPhoneStateListener myListner = new MyPhoneStateListener();
        if(register==1)
            myTelManager.listen(myListner, PhoneStateListener.LISTEN_CALL_STATE);
        else if(register==0)
            myTelManager.listen(myListner, PhoneStateListener.LISTEN_NONE);
    }



    class MyPhoneStateListener extends PhoneStateListener {
        public void onCallStateChanged(int state, String incomingNumber) {
            if (state == TelephonyManager.CALL_STATE_RINGING) {
                //gets the id of phone number of this incoming call
                String name = getContactName(incomingNumber);
                if (name == null || name.equals("")) {
                    name = incomingNumber;
                }
                if (btSocket != null && isBtConnected) {
                    try {
                        btSocket.getOutputStream().write(("R" + name).getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (state == TelephonyManager.CALL_STATE_IDLE) {
                if (btSocket != null && isBtConnected) {
                    try {
                        btSocket.getOutputStream().write("I".getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();

                    }
                }
            }
        }

        public String getContactName(String phoneNumber) {
            ContentResolver cr = getApplicationContext().getContentResolver();
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
            Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
            if (cursor == null) {
                return null;
            }
            String contactName = null;
            if (cursor.moveToFirst()) {
                contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            }

            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }

            return contactName;
        }
    }


    public void closesocket() {
        if(isBtConnected)
            try {
                btSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }
        // fast way to call Toast
        private void msg(String s) {
            Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
        }
}