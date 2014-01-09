package app.create.rpg.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.os.Parcel;
import android.util.Log;
import app.create.rpg.R;

public class TaskDecompressZip extends Task {
	
	protected String mFolder, mFile;
	protected int mResId;
	
	public TaskDecompressZip() { super(); }

	public TaskDecompressZip(String folder, int resId) {
		super ();
		mFolder = folder;
		mFile = null;
		mResId = resId;
	}

	public TaskDecompressZip(File folder, int resId) {
		super ();
		mFolder = folder.getAbsolutePath();
		mFile = null;
		mResId = resId;
	}

	public TaskDecompressZip(String folder, String file) {
		super ();
		mFolder = folder;
		mFile = file;
		mResId = 0;
	}

	public TaskDecompressZip(File folder, String file) {
		super ();
		mFolder = folder.getAbsolutePath();
		mFile = file;
		mResId = 0;
	}

	public TaskDecompressZip(String folder, File file) {
		super ();
		mFolder = folder;
		mFile = file.getAbsolutePath();
		mResId = 0;
	}

	public TaskDecompressZip(File folder, File file) {
		super ();
		mFolder = folder.getAbsolutePath();
		mFile = file.getAbsolutePath();
		mResId = 0;
	}

	public TaskDecompressZip(Parcel source) {
		super(source);
		mFolder = source.readString();
		mFile = source.readString();
		mResId = source.readInt();
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeString(mFolder);
		dest.writeString(mFile);
		dest.writeInt(mResId);
	}

	@Override
	public void run() {
		try {
			Log.d("CreateRPG", "TaskDecompressZip start");
			File folder = new File(mFolder), file;
			folder.mkdirs();
			if (!folder.isDirectory())
				throw new IOException ("Directory creation failure : " + folder.getAbsolutePath());
			Log.d("CreateRPG", "TaskDecompressZip open zis");
			ZipInputStream zis = new ZipInputStream(mFile != null ? new FileInputStream(mFile) : mService.getResources().openRawResource(mResId));
			OutputStream os = null;
			ZipEntry ze;
			int len, counter;
			long total, read;
			String strMsgPrefix = mService.getString(R.string.task_decompressing);
			try {
				byte[] buffer = new byte[8192];
				Log.d("CreateRPG", "TaskDecompressZip start reading zip entries");
				int cnt = 0;
				while ((ze = zis.getNextEntry()) != null) {
					if (Thread.interrupted()) return;
					mMessage = new StringBuilder(strMsgPrefix).append(ze.getName()).toString();
					mProgress = 0.0f;
					file = new File(folder, ze.getName());
					if (ze.isDirectory()) {
						file.mkdirs();
						continue;
					} else file.getParentFile().mkdirs();
					os = new FileOutputStream(file);
					total = ze.getSize(); read = 0; counter = 0;
					while ((len = zis.read(buffer)) > 0) {
						read += len;
						os.write(buffer, 0, len);
						if (++counter > 10) { mService.onTaskUpdate(this); counter = 0; }
						if (total == -1) continue;
						mProgress = (float) (((double) read) / total);
					}
					os.close();
					zis.closeEntry();
					++cnt;
				}
				Log.d("CreateRPG", "TaskDecompressZip end reading zip entries : " + cnt);
			} finally {
				try { os.close(); } catch (Exception e) { } // This should catch every exception including NullPointerException :P
				zis.close();
			}
		} catch (Exception e) {
			Log.d("CreateRPG", "TaskDecompressZip error - " + (mFolder == null ? "null" : mFolder) + " / " + mResId, e);
			printException (e);
		} finally {
			Log.d("CreateRPG", "TaskDecompressZip finish");
		}
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public static final TaskDecompressZip EMPTY = new TaskDecompressZip();
	public static final Creator<TaskDecompressZip> CREATOR = new Creator<TaskDecompressZip>() {
		public TaskDecompressZip createFromParcel(Parcel source) {
			return new TaskDecompressZip(source);
		}
		public TaskDecompressZip[] newArray(int size) {
			return new TaskDecompressZip[size];
		}
		
	};

}
