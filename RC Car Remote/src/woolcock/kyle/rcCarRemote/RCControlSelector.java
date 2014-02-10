package woolcock.kyle.rcCarRemote;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Looper;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;

public class RCControlSelector extends Activity {

	private int REQUEST_ENABLE_BT = 1;
	private ArrayAdapter<String> mArrayAdapter;
	private BluetoothSocket socket;
	private boolean btSearchComplete = false;

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
			if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				btSearchComplete = true;
			}
		}
	};

	class CustomListener implements OnClickListener {
		private BluetoothAdapter mBluetoothAdapter;
		
		@Override
		public void onClick(View view) {		
			final ProgressDialog progDailog = ProgressDialog.show(RCControlSelector.this,
					"Waiting for scan results", "Please Wait....", true);
			btSearchComplete = false;
			// Create a new thread to search on
			new Thread() {
				public void run() {
					Looper.prepare();
					try {
						mBluetoothAdapter.startDiscovery();
						// Register for notifications when a new device is discovered and when the search completes
						IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
						IntentFilter filter2 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
						registerReceiver(mReceiver, filter);
						registerReceiver(mReceiver,filter2);
						// check for when the search completes
						while (!btSearchComplete) {
							sleep(100);
						}
					} catch (Exception e) {
					}
					progDailog.dismiss();
				}
			}.start();
		}

		private CustomListener(BluetoothAdapter bluetoothadapter) {
			super();
			mBluetoothAdapter = bluetoothadapter;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rccontrol_selector);

	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(mReceiver);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.rccontrol_selector, menu);
		return true;
	}
	
	public void goToAutoPilotActivity(View view) {
		Intent intent = new Intent(this, AutopilotActivity.class);
		startActivity(intent);
	}

	public void connectToDevice(View view) {
		final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice);
		if (mBluetoothAdapter == null) {
			// Device does not support Bluetooth
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.bt_not_sup).setPositiveButton(R.string.confirm,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							// Do nothing and halt the search
							return;
						}
					});
			builder.create().show();
		}
		// Check if bluetooth is enabled, enable it if it is not
		if (!mBluetoothAdapter.isEnabled()) {
			new Thread() {
				@Override
				public void run() {
					Looper.prepare();
					try {
						Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
						startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
					} catch (Exception e) {
						// TODO Failure to start bluetooth notification?
						return;
					}
				}
			}.start();
		}
		// Wait for bluetooth adapter to power up before continuing
		while (mBluetoothAdapter.getState() != BluetoothAdapter.STATE_ON) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// Ignore
			}
		}
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		// If there are paired devices
		if (pairedDevices.size() > 0) {
			// Loop through paired devices
			for (BluetoothDevice device : pairedDevices) {
				// Add the name and address to an array adapter after getting
				// current info
				device = mBluetoothAdapter.getRemoteDevice(device.getAddress());
				mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
			}
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.pick_device)
				.setAdapter(mArrayAdapter, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// The 'which' argument contains the index position of the selected item
						// TODO Pair with selected device
						mBluetoothAdapter.cancelDiscovery();
						String device = mArrayAdapter.getItem(which);
						try {
							socket = mBluetoothAdapter
									.getRemoteDevice(device.substring(device.indexOf('\n') + 1))
									.createRfcommSocketToServiceRecord(
											UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
							socket.connect();
						} catch (IOException e) {
							// TODO could not connect notification
						}
					}
				})
				.setPositiveButton(R.string.search_for_device,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// Do nothing, will override
							}
						});
		final AlertDialog dialog = builder.create();
		dialog.show();
		// Override onClickListener
		dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new CustomListener(mBluetoothAdapter));
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
