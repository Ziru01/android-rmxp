package app.create.rpg.task;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.os.Parcel;
import android.util.Log;

public class TaskIntent extends Task {

	PendingIntent mPending;
	public TaskIntent() { super(); }
	
	public TaskIntent(PendingIntent pendingIntent) {
		super();
		mPending = pendingIntent;
	}

	public TaskIntent(Parcel source) {
		super(source);
		mPending = (PendingIntent) source.readParcelable(getClass().getClassLoader());
	}

	@Override
	public void run() {
		try {
			Log.d("CreateRPG", "Start TaskIntent");
			mPending.send();
			Log.d("CreateRPG", "Success TaskIntent");
		} catch (CanceledException e) {
			Log.e("CreateRPG", "Error TaskIntent", e);
			printException(e);
		} finally {
			Log.d("CreateRPG", "Finish TaskIntent");
		}
	}
	
	@Override
	public void writeToParcel (Parcel dest, int flags) {
		super.writeToParcel (dest, flags);
		dest.writeParcelable(mPending, 0);
	}

	@Override
	public int describeContents() {
		return 0;
	}
	
	public static final TaskIntent EMPTY = new TaskIntent();
	
	public static final Creator<TaskIntent> CREATOR = new Creator<TaskIntent>() {
		@Override public TaskIntent[] newArray(int size) { return new TaskIntent[size]; }
		@Override public TaskIntent createFromParcel(Parcel source) { return new TaskIntent(source); }
	};

}
