package woolcock.kyle.rcCarRemote;

import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;

public class RCControlSelector extends Activity {

	int REQUEST_ENABLE_BT = 1;

	ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<String>(this,
			android.R.layout.select_dialog_singlechoice);

	// Will listen for broadcast when a new bluetooth device is found
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();
	        // When discovery finds a device
	        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
	            // Get the BluetoothDevice object from the Intent
	            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	            // Add the name and address to an array adapter to show in a ListView
	            mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
	        }
	    }
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rccontrol_selector);
	}
	
	protected void onDestroy() {
		unregisterReceiver(mReceiver);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.rccontrol_selector, menu);
		return true;
	}

	public void connectToDevice(View view) {
		final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
				.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			// Device does not support Bluetooth
	        AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        builder.setMessage(R.string.bt_not_sup)
	               .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
	                   public void onClick(DialogInterface dialog, int id) {
	                	   // Do nothing and halt the search
	                       return;
	                   }
	               });
	        builder.create().show();
		}
		if (!mBluetoothAdapter.isEnabled()) {
			final ProgressDialog progDailog = ProgressDialog.show(this, "Waiting for Bluetooth Adapter to Power on",
		            "Please Wait....", true);
		    new Thread() {
		        public void run() {
		            try {
		    			Intent enableBtIntent = new Intent(
		    					BluetoothAdapter.ACTION_REQUEST_ENABLE);
		            	startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		            	while(mBluetoothAdapter.getState() != BluetoothAdapter.STATE_ON) {
		            		sleep(100);
		            	}
		            } catch (Exception e) {
		            }
		            progDailog.dismiss();
		        }
		    }.start();			
		}
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		// If there are paired devices
		if (pairedDevices.size() > 0) {
			// Loop through paired devices
			for (BluetoothDevice device : pairedDevices) {
				// Add the name and address to an array adapter after getting current info
				device = mBluetoothAdapter.getRemoteDevice(device.getAddress());
				mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
			}
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.pick_device).setAdapter(mArrayAdapter, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// The 'which' argument contains the index position
				// of the selected item
				//TODO Pair with selected device
			}
		}).setPositiveButton(R.string.search_for_device, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				mBluetoothAdapter.startDiscovery();
				// Register for notifications when a new device is discovered
				IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
				registerReceiver(mReceiver, filter);
				// TODO Progress display for device search.
			}
		});
		builder.create().show();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_ENABLE_BT) {
			if (resultCode == RESULT_OK) {

			} else if (resultCode == RESULT_CANCELED) {

			} else {

			}
		}
	}
}
