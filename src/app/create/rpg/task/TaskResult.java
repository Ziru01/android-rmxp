package app.create.rpg.task;
import android.os.Parcel;
import android.os.ResultReceiver;

public class TaskResult extends Task {

	private ResultReceiver mReceiver;
	
	private TaskResult () { mReceiver = null; }
	
	public TaskResult (ResultReceiver rr) {
		mReceiver = rr;
	}
	
	public TaskResult (Parcel source, ClassLoader cl) {
		super (source);
		mReceiver = source.readParcelable(cl);
	}
	
	public int describeContents() {
		// TODO: Implement this method
		return 0;
	}
	
	public void writeToParcel (Parcel dest, int flags) {
		super.writeToParcel (dest, flags);
		dest.writeParcelable(mReceiver, 0);
	}

	public void run() {
		mReceiver.send(0, null);
	}

	public static final TaskResult EMPTY = new TaskResult();
	public static final ClassLoaderCreator<TaskResult> CREATOR = new ClassLoaderCreator<TaskResult>() {
		@Override public TaskResult[] newArray(int size) { return new TaskResult[size]; }
		@Override public TaskResult createFromParcel(Parcel source) { return new TaskResult(source, getClass().getClassLoader()); }
		@Override public TaskResult createFromParcel(Parcel source, ClassLoader cl) { return new TaskResult(source, cl); }
	};

}
