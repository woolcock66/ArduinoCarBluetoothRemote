package woolcock.kyle.rcCarRemote;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
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
import android.widget.ArrayAdapter;

public class RCControlSelector extends Activity {

	int REQUEST_ENABLE_BT = 1;

	ArrayAdapter<String> mArrayAdapter;

	// Will listen for broadcast when a new bluetooth device is found
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// Add the name and address to an array adapter to show in a
				// ListView
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
					ProgressDialog progDailog = ProgressDialog.show(RCControlSelector.this,
							"Waiting for Bluetooth Adapter to Power on", "Please Wait....", true);
					try {
						Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
						startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
						while (mBluetoothAdapter.getState() != BluetoothAdapter.STATE_ON) {
							sleep(100);
						}
					} catch (Exception e) {
					}
					progDailog.dismiss();
				}
			}.start();
		}
		// TODO wait for bluetooth to power up
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
						// The 'which' argument contains the index position of
						// the selected item
						// TODO Pair with selected device
						String device = mArrayAdapter.getItem(which);
						new ConnectThread(mBluetoothAdapter.getRemoteDevice(device.substring(device
								.indexOf('\n') + 1)));
					}
				})
				.setPositiveButton(R.string.search_for_device,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// Do nothing, will override
							}
						});
		final AlertDialog dialog = builder.create();
		dialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface dialog) {
				new Thread() {
					public void run() {
						ProgressDialog progDailog = ProgressDialog.show(
								RCControlSelector.this, "Waiting for scan results",
								"Please Wait....", true);
						try {
							mBluetoothAdapter.startDiscovery();
							// Register for notifications when a new device is
							// discovered
							IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
							registerReceiver(mReceiver, filter);
							while (mBluetoothAdapter.isDiscovering()) {
								sleep(100);
							}
						} catch (Exception e) {
						}
						progDailog.dismiss();
					}
				}.start();
			}
		});
		dialog.show();

		// TODO Progress display for device search.
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_ENABLE_BT) {
			if (resultCode == RESULT_OK) {

			} else if (resultCode == RESULT_CANCELED) {

			} else {

			}
		}
	}

	private class ConnectThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;

		public ConnectThread(BluetoothDevice device) {
			// Use a temporary object that is later assigned to mmSocket,
			// because mmSocket is final
			BluetoothSocket tmp = null;
			mmDevice = device;

			// Get a BluetoothSocket to connect with the given BluetoothDevice
			try {
				// MY_UUID is the app's UUID string, also used by the server
				// code
				tmp = device.createRfcommSocketToServiceRecord(UUID
						.fromString("00001101-0000-1000-8000-00805F9B34FB"));
			} catch (IOException e) {
			}
			mmSocket = tmp;
		}

		public void run() {
			// Cancel discovery because it will slow down the connection
			BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

			try {
				// Connect the device through the socket. This will block
				// until it succeeds or throws an exception
				mmSocket.connect();
			} catch (IOException connectException) {
				// Unable to connect; close the socket and get out
				try {
					mmSocket.close();
				} catch (IOException closeException) {
				}
				return;
			}

			// Do work to manage the connection (in a separate thread)
			// manageConnectedSocket(mmSocket);
		}

		/** Will cancel an in-progress connection, and close the socket */
		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
			}
		}
	}
}
