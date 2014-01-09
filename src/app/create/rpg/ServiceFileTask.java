package app.create.rpg;

import java.util.Iterator;
import java.util.LinkedList;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.util.SparseArray;
import app.create.rpg.task.Task;

public class ServiceFileTask extends Service {

	public static final String ACTION_SET_RECEIVER = "app.create.rpg.ACTION_SET_RECEIVER",
	ACTION_PUSH_TASK = "app.create.rpg.ACTION_PUSH_TASK",
	ACTION_CANCEL_TASK = "app.create.rpg.ACTION_CANCEL_TASK",
	ACTION_STOP_QUEUE = "app.create.rpg.ACTION_STOP_QUEUE";

	public static class MyQueue extends Thread implements Parcelable {
		LinkedList<Task> mTaskQueue;
		Task mCurrent;
		int mNextId, mQueueId;
		ServiceFileTask mParent;
		NotificationCompat.Builder mBuilder;

		public MyQueue() {
			mTaskQueue = new LinkedList<Task>();
			mCurrent = null;
			mNextId = 0;
			mQueueId = 0;
		}

		MyQueue(ServiceFileTask parent, int queueId) {
			this();
			mQueueId = queueId;
			mParent = parent;
		}

		public MyQueue(Parcel parcel) {
			final ClassLoader cl = getClass().getClassLoader();
			parcel.readList(mTaskQueue = new LinkedList<Task>(), cl);
			mCurrent = parcel.readParcelable(cl);
			mNextId = parcel.readInt();
			mQueueId = parcel.readInt();
		}

		public synchronized String toString() {
			if (mCurrent == null)
				return mTaskQueue.size() > 0 ? "[pending]" : "[No task]";
			return mCurrent.getMessage();
		}

		public synchronized float getProgress() {
			if (mCurrent == null)
				return 0.0f;
			return mCurrent.getProgress();
		}

		public synchronized void pushTask(Task task) {
			task.setService(mParent, mNextId++, mQueueId);
			mTaskQueue.add(task);
		}

		public synchronized void stopTask(int id) {
			if (id == -1 || mCurrent.getTaskId() == id)
				interrupt();
			else {
				for (Iterator<Task> it = mTaskQueue.iterator(); it.hasNext();) {
					Task t = it.next();
					if (t.getTaskId() == id) {
						it.remove();
						break;
					}
				}
			}
		}

		public void destroyQueue() {
			mTaskQueue = null;
			mCurrent = null;
			interrupt();
		}

		public void run() {
			try {
				Log.d("CreateRPG", "Start Queue " + mQueueId);
				while (!(mTaskQueue == null || mParent.mStop)) {
					try {
						synchronized (this) {
							mCurrent = mTaskQueue.poll();
						}
						if (mCurrent == null || mParent.mStop) {
							Log.d("CreateRPG", "Queue " + mQueueId + ", Out of task queue");
							break;
						}
						if (mParent.mStop) break;
						if (interrupted()) continue;
						Log.d("CreateRPG", "Queue " + mQueueId + ", Start task " + mCurrent);
						mParent.onTaskUpdate();
						mCurrent.run();
						mParent.onTaskUpdate();
						Log.d("CreateRPG", "Queue " + mQueueId + ", End task " + mCurrent);
						synchronized (this) {
							mCurrent = null;
						}
						Thread.sleep(100);
					} catch (InterruptedException e) {
					}
				}
			} finally {
				synchronized (mParent.mTasks) {
					SparseArray<MyQueue> list = mParent.mTasks;
					list.delete(list.indexOfValue(this));
				}
				mParent.deleteQueue(this);
				Log.d("CreateRPG", "End Queue " + mQueueId);
			}
		}

		@Override
		public int describeContents() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeList(mTaskQueue);
			dest.writeParcelable(mCurrent, 0);
			dest.writeInt(mNextId);
			dest.writeInt(mQueueId);
		}

		public static final MyQueue EMPTY = new MyQueue();
		public static final Creator<MyQueue> CREATOR = new Creator<ServiceFileTask.MyQueue>() {
			public MyQueue[] newArray(int size) { return new MyQueue[size]; }
			public MyQueue createFromParcel(Parcel source) { return new MyQueue(source); }
		};
	}


	public class MyBinder extends Binder {

		public SparseArray<MyQueue> getTasks() {
			return mTasks;
		}

	}

	private SparseArray<MyQueue> mTasks;
	private final IBinder mBinder = new MyBinder();
	private boolean mStop;
	private ResultReceiver mReceiver;
	private NotificationCompat.Builder mBuilder, mNotifyComplete;
	private PendingIntent mPending;
	private NotificationManager mNM;

	public void onTaskUpdate() {
		if (mReceiver != null)
			mReceiver.send(0, null);
	}

	public PendingIntent getTaskDialog() {
		return mPending;
	}

	public void onTaskUpdate(Task task) {
		onTaskUpdate();
		float pro = task.getProgress();
		if (pro < 0f || pro > 1f)
			mBuilder.setProgress(0, 0, true);
		else
			mBuilder.setProgress(10000, Math.round(task.getProgress() * 10000f), false);
		mNM.notify(getClass().getName(), R.string.task_complete + task.getQueueId(), mBuilder
				   .setContentTitle(new StringBuilder().append(Math.round(task.getProgress() * 100f)).append("% done"))
				   .setContentText(task.getMessage()).build());
	}

	public void deleteQueue(MyQueue queue) {
		onTaskUpdate();
		mNM.cancel(getClass().getName(), R.string.task_complete + queue.mQueueId);
		mNM.notify(getClass().getName(), R.string.task_complete + queue.mQueueId, mNotifyComplete
				   .setTicker(getText(R.string.task_complete))
				   .setContentTitle(getText(R.string.task_complete)).build());
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mStop = false;
		mTasks = new SparseArray<MyQueue>();
		mReceiver = null;
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mPending = PendingIntent.getActivity(this, 0, new Intent(this, ActivityTaskDialog.class), 0);
		CharSequence ticker = getText(R.string.app_name);
		mBuilder = new NotificationCompat.Builder(this)
			.setAutoCancel(false).setOngoing(true).setOnlyAlertOnce(true)
			.setTicker(ticker)
			.setContentIntent(mPending)
			.setSmallIcon(R.drawable.ic_launcher);
		mNotifyComplete = new NotificationCompat.Builder(this)
			.setAutoCancel(true).setOngoing(false)
			.setContentIntent(mPending)
			.setSmallIcon(R.drawable.ic_launcher);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mStop = true;
	}

	public void handleCommand(Intent intent) {
		if (intent == null) {
			return;
		}
		Log.d("CreateRPG", "ServiceFileTask Command : " + intent.getAction());
		long time = System.currentTimeMillis();
		intent.getExtras().setClassLoader(getClass().getClassLoader());
		try {
			String action = intent.getAction();
			if (action.equals(ACTION_SET_RECEIVER)) {
				mReceiver = intent.getParcelableExtra("receiver");
				return;
			}
			int queueid = intent.getIntExtra("queueid", -1);
			MyQueue queue;
			if (queueid == -1) {
				if (action.equals(ACTION_STOP_QUEUE)) {
					synchronized (mTasks) {
						for (int i = 0; i < mTasks.size(); ++i) {
							mTasks.valueAt(i).destroyQueue();
						}
						mTasks.clear();
					}
					return;
				}
				queueid = 0;
				// look for a empty queue index
				synchronized (mTasks) {
					for (int i = 0; i < mTasks.size(); ++i) {
						if (mTasks.keyAt(i) > queueid) break;
						queueid = mTasks.keyAt(i) + 1;
					}
					queue = mTasks.get(queueid);
				}
				if (queueid < 0) { // overflow
					return;
				}
			} else queue = mTasks.get(queueid);
			if (action.equals(ACTION_STOP_QUEUE)) {
				if (queue != null) {
					synchronized (mTasks) {
						mTasks.delete(queueid);
					}
					mNM.cancel(getClass().getName(), R.string.task_complete + queueid);
					queue.destroyQueue();
				}
				return;
			}
			if (queue == null) {
				synchronized (mTasks) {
					mTasks.put(queueid, queue = new MyQueue(this, queueid));
				}
			}
			if (action.equals(ACTION_PUSH_TASK)) {
				Parcelable[] arr = intent.getParcelableArrayExtra("tasks");
				if (arr != null) {
					Log.d("CreateRPG", "Size : " + arr.length);
					for (Parcelable t : arr) {
						Log.d("CreateRPG", t == null ? "null" : t.toString());
						if (t != null)
							queue.pushTask((Task) t);
					}
				} else queue.pushTask((Task) intent.getParcelableExtra("task"));
				if (!queue.isAlive())
					queue.start();
				startActivity(new Intent(this, ActivityTaskDialog.class).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
			} else if (action.equals(ACTION_CANCEL_TASK)) {
				queue.stopTask(intent.getIntExtra("taskid", -1));
			}
		} finally {
			Log.d("CreateRPG", "ServiceFileTask Command : " + intent.getAction() + ", Finished in : " + (System.currentTimeMillis() - time) / 1000.0f + " sec");
			onTaskUpdate();
		}
	}

	@Override
	public void onStart(Intent intent, int startId) {
	    handleCommand(intent);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	    handleCommand(intent);
	    return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

}
