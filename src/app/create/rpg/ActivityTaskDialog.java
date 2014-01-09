package app.create.rpg;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.view.View;
import android.view.Window;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import app.create.rpg.ServiceFileTask.MyBinder;
import app.create.rpg.ServiceFileTask.MyQueue;
import app.create.rpg.task.TaskListAdapter;

public class ActivityTaskDialog extends Activity implements ServiceConnection, View.OnClickListener, OnItemLongClickListener {

	Thread updaterThread;
	ServiceFileTask.MyBinder binder;
	TaskListAdapter adapter;
	
	private class MyResultReceiver extends ResultReceiver {

		public MyResultReceiver(Handler handler) {
			super(handler);
		}

		@Override
		protected void onReceiveResult(int resultCode, Bundle resultData) {
			if (binder == null) return;
			adapter.update(binder);
		}
	}

	@Override
	public void onCreate (Bundle state) {
		super.onCreate(state);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_task);
		
		final ListView lv = (ListView) findViewById(R.id.list);
		adapter = new TaskListAdapter(this);
		lv.setAdapter(adapter);
		lv.setLongClickable(true);
		lv.setOnItemLongClickListener(this);
		
		findViewById(android.R.id.button2).setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		finish();
	}

	@Override
	public void onResume () {
		super.onResume();
		bindService(new Intent(this, ServiceFileTask.class), this, BIND_AUTO_CREATE);
	}
	
	@Override
	public void onPause () {
		super.onPause();
		unbindService(this);
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		binder = (MyBinder) service;
		startService(new Intent(ServiceFileTask.ACTION_SET_RECEIVER)
					 .putExtra("receiver", new MyResultReceiver(new Handler(getMainLooper()))));
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		binder = null;
		((ArrayAdapter<?>) ((ListView) findViewById(R.id.list)).getAdapter()).clear();
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapterView,
			View itemView, int position, long id) {
		final MyQueue obj = (MyQueue) ((Adapter) adapterView.getAdapter()).getItem(position);
		DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (which == DialogInterface.BUTTON_POSITIVE) {
					startService(new Intent(ServiceFileTask.ACTION_STOP_QUEUE)
						.putExtra("queueid", obj.mQueueId));
				}
			}
		};
		new AlertDialog.Builder(this)
			.setMessage(R.string.str_ask_terminate)
			.setNegativeButton(android.R.string.no, listener)
			.setPositiveButton(android.R.string.yes, listener).show();
		return true;
	}

}
