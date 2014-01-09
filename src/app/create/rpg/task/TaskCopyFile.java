package app.create.rpg.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.os.Parcel;
import app.create.rpg.R;
import app.create.rpg.ServiceFileTask;

public class TaskCopyFile extends Task {
	
	private String mFrom, mTo;
	private boolean mMove;

	public TaskCopyFile(String from, String to) {
		mFrom = from; mTo = to; mMove = false;
	}

	public TaskCopyFile(String from, String to, boolean move) {
		mFrom = from; mTo = to; mMove = move;
	}

	public TaskCopyFile(Parcel source) {
		super(source);
		mFrom = source.readString();
		mTo = source.readString();
		mMove = source.readByte() != 0;
	}

	@Override
	public void run() {
		InputStream is = null;
		OutputStream os = null;
		try {
			if (mMove) {
				new File(mTo).delete();
				if (!(new File(mFrom).renameTo(new File(mTo))))
					throw new IOException ("Rename failure");
				return;
			}
			is = new FileInputStream(mFrom);
			os = new FileOutputStream(mTo);
			byte[] buf = new byte[2048];
			long size = new File(mFrom).length(), total = 0;
			int len;
			mService.onTaskUpdate(this);
			while ((len = is.read(buf)) > 0) {
				os.write(buf, 0, len);
				mProgress = (float)(total += len) / size;
				mService.onTaskUpdate(this);
			}
		} catch (IOException e) {
			printException (e);
		} finally {
			if (is != null)
				try { is.close(); } catch (IOException e) { }
			if (os != null)
				try { os.close(); } catch (IOException e) { }
		}
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void setService(ServiceFileTask service, int id, int queue) {
		super.setService(service, id, queue);
		mMessage = service.getString(R.string.task_transferring);
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeString(mFrom);
		dest.writeString(mTo);
		dest.writeByte((byte) (mMove ? 1 : 0));
	}

	public static final TaskCopyFile EMPTY = new TaskCopyFile(null, null);
	public static final Creator<TaskCopyFile> CREATOR = new Creator<TaskCopyFile>() {
		@Override public TaskCopyFile[] newArray(int size) { return new TaskCopyFile[size]; }
		@Override public TaskCopyFile createFromParcel(Parcel source) { return new TaskCopyFile(source); }
	};

}
