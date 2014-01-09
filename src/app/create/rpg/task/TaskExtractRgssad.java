package app.create.rpg.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import android.os.Parcel;
import app.create.rpg.R;
import app.create.rpg.ServiceFileTask;
import app.create.rpg.rgssad.RgssEntry;
import app.create.rpg.rgssad.RgssInputStream;

public class TaskExtractRgssad extends Task {
	
	private String mPath;

	public TaskExtractRgssad(String path) { super (); mPath = path; }
	public TaskExtractRgssad(Parcel source) { super (source); mPath = source.readString(); }
	@Override public int describeContents () { return 0; }
	@Override public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeString(mPath);
	}

	@Override
	public void setService(ServiceFileTask service, int id, int queue) {
		super.setService(service, id, queue);
		mMessage = service.getString(R.string.task_decompressing);
	}

	@Override
	public void run() {
		try {
			File file = new File(mPath);
			File folder = file.getParentFile();
			RgssInputStream zis = new RgssInputStream(new FileInputStream(file), 0xDEADCAFE);
			OutputStream os = null;
			RgssEntry ze;
			int len, counter;
			long total, read;
			String strMsgPrefix = mService.getString(R.string.task_decompressing);
			try {
				byte[] buffer = new byte[1024];
				while ((ze = zis.getNextEntry()) != null) {
					if (Thread.interrupted()) return;
					mMessage = new StringBuilder(strMsgPrefix).append(ze.getName()).toString();
					mProgress = 0.0f;
					file = new File(folder, ze.getName().replace('\\', '/'));
					if (ze.isDirectory()) {
						file.mkdirs();
						continue;
					} else file.getParentFile().mkdirs();
					os = new FileOutputStream(file);
					total = ze.getSize(); read = 0; counter = 0;
					mService.onTaskUpdate(this);
					while ((len = zis.read(buffer)) > 0) {
						read += len;
						os.write(buffer, 0, len);
						if (++counter > 10) { mService.onTaskUpdate(this); counter = 0; }
						mProgress = (float) (((double) read) / total);
					}
					os.close();
				}
			} finally {
				try { os.close(); } catch (Exception e) { } // This should catch every exception including NullPointerException :P
				zis.close();
			} // end extract
		} catch (Exception e) {
			StringWriter sw = new StringWriter ();
			e.printStackTrace(new PrintWriter(sw));
			mMessage = sw.toString();
			try { Thread.sleep(999999); } catch (Throwable e1) { }
		}
	}
	
	public static final TaskExtractRgssad EMPTY = new TaskExtractRgssad("");
	public static final Creator<TaskExtractRgssad> CREATOR = new Creator<TaskExtractRgssad>() {
		@Override public TaskExtractRgssad[] newArray(int size) { return new TaskExtractRgssad[size]; }
		@Override public TaskExtractRgssad createFromParcel(Parcel source) { return new TaskExtractRgssad(source); }
	};

}
