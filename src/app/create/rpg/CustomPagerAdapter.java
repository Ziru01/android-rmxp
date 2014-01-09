package app.create.rpg;

import java.util.ArrayList;
import java.util.Iterator;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.Fragment.SavedState;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBar.TabListener;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.ViewGroup;

public class CustomPagerAdapter extends PagerAdapter implements OnPageChangeListener, TabListener {

	public abstract static class FragmentCreator implements Parcelable {
		long id;
		
		public FragmentCreator() { }
		public FragmentCreator(Parcel source) { }
		public abstract Fragment newInstance (Context context);
		public abstract String getTitle (Context context);
		public abstract Class<?> getFragmentClass ();
		public boolean isFromThis (Fragment frag) { return getFragmentClass().equals(frag.getClass()); } // No subclass, must match exactly
		@Override public int describeContents() { return 0; }
		@Override public void writeToParcel(Parcel dest, int flags) { }
		
		public static final Creator<FragmentCreator> CREATOR = null;
	}
	
	private final FragmentManager mFragmentManager;
	private final ViewPager mViewPager;
	private final ActionBar mActionBar;
	private final FragmentEmpty mEmptyPage;
	private FragmentTransaction mCurTransaction = null;
	private ArrayList<Object> mCache = new ArrayList<Object>();
	private ArrayList<FragmentCreator> mCreators = new ArrayList<FragmentCreator>();
	private Fragment mCurrentPrimaryItem = null;
	
	public CustomPagerAdapter(ActionBarActivity act) {
		mFragmentManager = act.getSupportFragmentManager();
		mActionBar = act.getSupportActionBar();
		mEmptyPage = new FragmentEmpty();
		mViewPager = (ViewPager) act.findViewById(R.id.viewPager);
		mViewPager.setOnPageChangeListener(this);
		mViewPager.setAdapter(this);
	}
	
	public void addPage (final FragmentCreator info, boolean bSelect) {
		int i = 0;
		for (Iterator<FragmentCreator> it = mCreators.iterator(); it.hasNext(); ++i) {
			FragmentCreator item = it.next();
			if (item.equals(info)) {
				if (!bSelect) return;
				mViewPager.setCurrentItem(i);
				if (mActionBar.getSelectedNavigationIndex() != i)
					mActionBar.setSelectedNavigationItem(i);
				return;
			}
		}
		mCreators.add(info);
		mCache.add(null);
		notifyDataSetChanged();
		mActionBar.addTab(mActionBar.newTab()
				.setTag(info)
				.setTabListener(CustomPagerAdapter.this)
				.setText(info.getTitle(mViewPager.getContext())), bSelect);
	}
	
	public void removePage (final FragmentCreator info) {
		int idx = mCreators.indexOf(info);
		if (idx == -1) return;
		mCreators.remove(idx);
		mCache.remove(idx);
		notifyDataSetChanged();
		mActionBar.removeTabAt(idx);
	}
	
	public void removePage (final Fragment frag) {
		int idx = mCache.indexOf(frag);
		if (idx == -1) return;
		mCreators.remove(idx);
		mCache.remove(idx);
		notifyDataSetChanged();
		mActionBar.removeTabAt(idx);
	}
	
	public Fragment getFragmentByCreator (FragmentCreator creator) {
		int i = mCreators.indexOf(creator);
		if (i < 0) return null;
		Object o = mCache.get(i);
		if (o instanceof Fragment) return (Fragment) o;
		return null;
	}

	public Fragment getItem(int position) {
		if (mCreators.isEmpty())
			return mEmptyPage;
		return mCreators.get(position).newInstance(mViewPager.getContext());
	}

	@Override
	public int getCount() {
		return mCreators.isEmpty() ? 1 : mCreators.size();
	}

	@Override
	public int getItemPosition(Object object) {
		if (mCreators.isEmpty() && !object.equals(mEmptyPage))
			return POSITION_NONE;
		int i = 0;
		for (Iterator<FragmentCreator> it = mCreators.iterator(); it.hasNext(); ++i) {
			if (it.next().getFragmentClass().equals(object.getClass()))
				return i;
		}
		return POSITION_NONE;
	}

	@Override
	public void onTabReselected(final Tab tab, FragmentTransaction ft) {
		DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (which == DialogInterface.BUTTON_POSITIVE) {
					removePage((FragmentCreator) tab.getTag());
					notifyDataSetChanged();
				}
			}
		};
		new AlertDialog.Builder(mViewPager.getContext())
			.setMessage(R.string.str_ask_close_page)
			.setNegativeButton(android.R.string.no, listener)
			.setPositiveButton(android.R.string.yes, listener)
			.show();
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		if (mViewPager.getCurrentItem() != tab.getPosition())
			mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) { }

	@Override
	public void onPageScrollStateChanged(int arg0) { }

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) { }

	@Override
	public void onPageSelected(int arg0) {
		if (mActionBar.getSelectedNavigationIndex() != arg0)
			mActionBar.setSelectedNavigationItem(arg0);
	}

	@Override
	public void restoreState(Parcelable arg0, ClassLoader arg1) {
		Bundle bundle = (Bundle) arg0;
		bundle.setClassLoader(arg1);
		int i = 0, s = mViewPager.getCurrentItem();
		mCreators = bundle.getParcelableArrayList("creators");
		mCache.clear();
		mCache.addAll(bundle.getParcelableArrayList("states"));
		notifyDataSetChanged();
		mActionBar.removeAllTabs();
		for (FragmentCreator item : mCreators) {
			mActionBar.addTab(mActionBar.newTab()
				.setTag(item).setTabListener(this).setText(item.getTitle(mViewPager.getContext()))
				, i++ == s);
		}
	}

	@Override
	public Parcelable saveState() {
		Bundle bundle = new Bundle();
		bundle.putParcelableArrayList("creators", mCreators);
		int c = mCache.size();
		ArrayList<Parcelable> state = new ArrayList<Parcelable>(c);
		for (int i = 0; i < c; ++i) {
			Object o = mCache.get(i);
			if (o instanceof Fragment)
				o = mFragmentManager.saveFragmentInstanceState((Fragment) o);
			state.add((Parcelable) o);
		}
		bundle.putParcelableArrayList("states", state);
		return bundle;
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return ((Fragment) arg1).getView() == arg0;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		if (mCurTransaction == null)
			mCurTransaction = mFragmentManager.beginTransaction();
		if (object.equals(mEmptyPage)) {
			// Yeah, that's the empty indicator
			mCurTransaction.remove(mEmptyPage);
			return;
		}
		
		// Convert my fragment into just a state xD
		if (mCreators.size() > position && mCreators.get(position).getFragmentClass().equals(object))
			mCache.set(position, mFragmentManager.saveFragmentInstanceState((Fragment) object));
		
		// Remove it
		mCurTransaction.remove((Fragment) object);
		
	}

	@Override
	public void finishUpdate(ViewGroup container) {
		if (mCurTransaction != null) {
			mCurTransaction.commitAllowingStateLoss();
			mCurTransaction = null;
			mFragmentManager.executePendingTransactions();
		}
	}
	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		if (mCreators.isEmpty()) {
			// This is empty so just add a page
			if (!mEmptyPage.isAdded()) {
				if (mCurTransaction == null)
					mCurTransaction = mFragmentManager.beginTransaction();
				mCurTransaction.add(container.getId(), mEmptyPage);
			}
			return mEmptyPage;
		}
		Object cache = null;
		if (mCache.size() > position) {
			cache = mCache.get(position);
			if (cache instanceof Fragment) {
				// We already have instantiated this fragment, so just return it
				return cache;
			}
		}
		if (mCurTransaction == null)
			mCurTransaction = mFragmentManager.beginTransaction();
		Fragment frag = getItem(position); // New fragment!
		if (cache instanceof SavedState) { // Let's restore its state!
			frag.setInitialSavedState((SavedState) cache);
		}
		
		// Now save the frag into the cache!
		while (mCache.size() <= position) mCache.add(null);
		frag.setMenuVisibility(false);
		mCurTransaction.add(container.getId(), frag);
		mCache.set(position, frag);
		
		return frag;
	}

	@Override
	public void setPrimaryItem(ViewGroup container, int position, Object object) {
		Fragment fragment = (Fragment) object;
		if (fragment != mCurrentPrimaryItem) {
			if (mCurrentPrimaryItem != null) {
				mCurrentPrimaryItem.setMenuVisibility(false);
			}
			if (fragment != null) {
				fragment.setMenuVisibility(true);
			}
			mCurrentPrimaryItem = fragment;
		}
	}
	
	

}
