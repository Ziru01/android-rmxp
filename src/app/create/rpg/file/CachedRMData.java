package app.create.rpg.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.os.FileObserver;
import android.os.Parcel;
import app.create.rpg.ActivityProject;

import com.jinoh.ruby.marshal.Marshal;

public class CachedRMData extends CachedFile {

	public CachedRMData(File file, ActivityProject main, String name) {
		super(file, main, name);
	}
	
	public CachedRMData(Parcel source) {
		super(source);
		// TODO: Could this be the best way to store and load state?
		try {
			mData = Marshal.loadAs (source.createByteArray(), List.class);
		} catch (IOException e) {
			
			try {
				load();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<Object> getData() { return (List<Object>) mData; }

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		// TODO: Could this be the best way to store and load state?
		try {
			dest.writeByteArray(Marshal.dump(mData));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void load() throws IOException {
		InputStream is = new FileInputStream(mFile);
		try {
			Object loadData = Marshal.load(is);
			is.close(); is = null;
			if (loadData.getClass().isArray()) {
				Object[] arr = (Object[]) loadData;
				List<Object> list = new ArrayList<Object>(arr.length);
				for (int i = 1; i < arr.length; ++i) {
					if (arr[i] != null)
						list.add(arr[i]);
				}
				mData = list;
			} else throw new IOException ("Broken file");
		} catch (FileNotFoundException e) {
			onEvent(FileObserver.DELETE_SELF, mFile.getAbsolutePath());
		} finally {
			System.gc();
			if (is != null) is.close();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void save() throws IOException {
		if (mData != null) {
			List<Object> list = (List<Object>) mData;
			Object[] data = new Object[list.size()+1];
			int i = 0;
			data[0] = null;
			for (Object o : list) {
				data[++i] = o;
			}
			OutputStream os = new FileOutputStream(mFile);
			Marshal.dump(os, data);
			os.close();
		}
	}
	
	public static Creator<CachedRMData> CREATOR = new Creator<CachedRMData>() {
		public CachedRMData[] newArray(int size) { return new CachedRMData[size]; }
		public CachedRMData createFromParcel(Parcel source) { return new CachedRMData(source); }
	};

}
