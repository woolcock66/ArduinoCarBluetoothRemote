package woolcock.kyle.rcCarRemote;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

public class AutopilotActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_autopilot);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.autopilot, menu);
		return true;
	}
	
	public void spiralAutopilot(View view) {
		
	}
	
	public void cautiousAutopilot(View view) {
		
	}
	
	public void autopilot(View view) {
		
	}
	
	public void stop(View view) {
		
	}

}
