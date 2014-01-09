package app.create.rpg.task;

import java.io.PrintWriter;
import java.io.StringWriter;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.NotificationCompat;
import app.create.rpg.R;
import app.create.rpg.ServiceFileTask;

public abstract class Task implements Runnable, Parcelable {
	
	protected ServiceFileTask mService;
	protected int mTaskId, mQueueId;
	protected String mMessage;
	protected float mProgress;
	private boolean mUpdated = false;

	public float getProgress () { return mProgress; }
	public String getMessage () { return mMessage; }
	public boolean onNotified () { boolean k = mUpdated; mUpdated = true; return k; }
	
	public Task () {
		mTaskId = 0;
		mQueueId = 0;
		mMessage = "";
		mProgress= 0.0f;
	}
	
	public Task (Parcel source) {
		mTaskId = source.readInt();
		mQueueId = source.readInt();
		mMessage = source.readString();
		mProgress = source.readFloat();
	}
	
	public void printException (Exception e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		mMessage = sw.toString();
		mService.onTaskUpdate();
		NotificationManager nm = (NotificationManager) mService.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify(ServiceFileTask.class.getName(), R.string.task_complete + mQueueId,
				new NotificationCompat.Builder(mService)
					.setAutoCancel(true).setOngoing(false)
					.setTicker(mService.getText(R.string.app_name))
					.setContentTitle("Error : " + e.getClass().getSimpleName())
					.setContentText(e.toString())
					.setContentIntent(mService.getTaskDialog())
					.setSmallIcon(R.drawable.ic_launcher).build());
		try {
			while (true)
				Thread.sleep(Long.MAX_VALUE);
		} catch (InterruptedException e1) {
		}
	}
	
	@Override
	public void writeToParcel (Parcel dest, int flags) {
		dest.writeInt(mTaskId);
		dest.writeInt(mQueueId);
		dest.writeString(mMessage);
		dest.writeFloat(mProgress);
	}
	
	public int getTaskId () {
		return mTaskId;
	}
	
	public int getQueueId () {
		return mQueueId;
	}
	
	public void execute (Context context, int queueid) {
		Intent intent = new Intent(context, ServiceFileTask.class);
		intent.setAction(ServiceFileTask.ACTION_PUSH_TASK);
		intent.putExtra("queueid", queueid);
		intent.putExtra("task", (Parcelable) this);
		context.startService(intent);
	}
	
	public void setService (ServiceFileTask service, int id, int queue) {
		mService = service;
		mTaskId = id;
		mQueueId = queue;
	}
	
	public static final Creator<Task> CREATOR = null;

}
