package app.create.rpg.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import android.os.Parcel;

public class TaskCompressFolder extends Task {

	private String mDir, mTo;

	public TaskCompressFolder(String dir, String to) {
		mDir = dir; mTo = to;
	}

	public TaskCompressFolder(Parcel source) {
		super(source);
		mDir = source.readString();
		mTo = source.readString();
	}
	
	private void compress (String path, ZipOutputStream zos) throws IOException {
		File f = new File(path);
		ZipEntry ze = new ZipEntry(f.isDirectory() ? path + "/" : path);
		ze.setSize(f.length());
		ze.setTime(f.lastModified());
		zos.putNextEntry(ze);
		if (f.isFile()) {
			FileInputStream fis = new FileInputStream(f);
			byte[] buf = new byte[4096];
			int read;
			// long size = ze.getSize(), total;
			mMessage = mService.getString(0);
			while ((read = fis.read(buf)) > 0) {
				zos.write(buf, 0, read);
			}
			fis.close();
		}
		zos.closeEntry();
	}

	@Override
	public void run() {
		try {
			ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(mTo));
			compress ("", zos);
			zos.close();
		} catch (Exception e) {
			printException(e);
		}
	}

	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeString(mDir);
		dest.writeString(mTo);
	}

	public static final TaskCompressFolder EMPTY = new TaskCompressFolder(null, null);
	public static final Creator<TaskCompressFolder> CREATOR = new Creator<TaskCompressFolder>() {
		public TaskCompressFolder[] newArray(int size) { return new TaskCompressFolder[size]; }
		public TaskCompressFolder createFromParcel(Parcel source) { return new TaskCompressFolder(source); }
	};

}
