package app.create.rpg.task;

import java.io.File;

import org.ini4j.Config;
import org.ini4j.Ini;

import android.os.Parcel;
import android.util.Log;
import app.create.rpg.R;

public class TaskProject extends TaskDecompressZip {
	
	boolean mCreateFirst;
	String mGameName;
	String mRTP[];
	
	protected TaskProject() { super(); }

	public TaskProject(boolean bCreateFirst, String name, String[] rtp, String folder) {
		super (folder, R.raw.project_template);
		mCreateFirst = bCreateFirst;
		mGameName = name;
		mRTP = rtp;
	}
	
	public TaskProject(Parcel source) {
		super (source);
		mCreateFirst = source.readByte() != 0;
		mGameName = source.readString();
		mRTP = source.createStringArray();
	}

	@Override public int describeContents() { return 0; }

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel (dest, flags);
		dest.writeByte((byte) (mCreateFirst ? 1 : 0));
		dest.writeString(mGameName);
		dest.writeStringArray(mRTP);
	}
	
	public static final TaskProject EMPTY = new TaskProject();
	public static final Creator<TaskProject> CREATOR = new Creator<TaskProject>() {
		@Override public TaskProject[] newArray(int size) { return new TaskProject[size]; }
		@Override public TaskProject createFromParcel(Parcel source) { return new TaskProject (source); }
	};

	@Override
	public void run() {
		try {
			Log.d("CreateRPG", "TaskProject Start running");
			File folder = new File(mFolder), file;
			if (mCreateFirst) {
				Log.d("CreateRPG", "TaskProject Unzipping");
				super.run();
				Log.d("CreateRPG", "TaskProject complete");
			} // end unzip
			mMessage = mService.getString(R.string.task_editing);
			mService.onTaskUpdate(this);
			file = new File(folder, "Game.ini");
			Config cfg = Config.getGlobal();
			cfg.setEscape(false);
			cfg.setStrictOperator(true);
			Ini ini = new Ini();
			ini.load(file);
			ini.put("Game", "Title", mGameName);
			int len = mRTP.length > 2 ? 3 : mRTP.length;
			for (int i = 0; i < len; ++i) {
				ini.put("Game", "RTP"+(i+1), mRTP[i]);
			}
			ini.store(file);
			Log.d("CreateRPG", "TaskProject okay");
		} catch (Exception e) {
			Log.e("CreateRPG", "TaskProject error", e);
			printException(e);
		} finally {
			Log.d("CreateRPG", "TaskProject finish");
		}
	}

}
