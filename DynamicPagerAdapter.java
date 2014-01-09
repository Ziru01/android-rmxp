/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.create.rpg;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBar.TabListener;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

/**
 * Implementation of {@link android.support.v4.view.PagerAdapter} that
 * represents each page as a {@link Fragment} that is persistently kept in the
 * fragment manager as long as the user can return to the page.
 */
public class DynamicPagerAdapter extends PagerAdapter implements TabListener,
		OnPageChangeListener {
	private static final String TAG = "DynamicPagerAdapter";
	private static final boolean DEBUG = false;

	private FragmentManager mFragmentManager;
	private FragmentTransaction mCurTransaction = null;
	private Fragment mCurrentPrimaryItem = null;
	private List<Fragment.SavedState> mSavedState = new ArrayList<Fragment.SavedState>();
	private List<Fragment> mFragments = new ArrayList<Fragment>();
	private ActionBar mActionBar;
	private ViewPager mViewPager;
	private View mEmptyView;
	private boolean mShowEmptyView;

	public DynamicPagerAdapter(ActionBarActivity activity) {
		mFragmentManager = activity.getSupportFragmentManager();
		mActionBar = activity.getSupportActionBar();
		mViewPager = (ViewPager) activity.findViewById(R.id.viewPager);
		mViewPager.setAdapter(this);
		mViewPager.setOnPageChangeListener(this);
	}

	/**
	 * Return the Fragment associated with a specified position.
	 */
	// public Fragment getItem(int position) { return
	// mFragmentManager.findFragmentByTag(makeFragmentName(; }
	public int getCount() {
		int sz = mFragments.size();
		return sz < 1 ? 1 : sz;
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	@Override
	public void onPageSelected(int position) {
		Fragment frag = mFragments.get(position);
		if (frag == null)
			return;
		int i = position;
		if (mActionBar.getTabAt(i) == null)
			return;
		if (mActionBar.getTabAt(i).getTag().equals(frag)) {
			mActionBar.setSelectedNavigationItem(i);
			return;
		}
		for (i = 0; i < mActionBar.getTabCount(); ++i) {
			if (mActionBar.getTabAt(i).getTag().equals(frag))
				mActionBar.setSelectedNavigationItem(i);
		}
	}

	@Override
	public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
	}

	@Override
	public synchronized void onTabSelected(Tab tab, FragmentTransaction ft) {
		int i = mFragments.indexOf(tab.getTag());
		if (i != -1)
			mViewPager.setCurrentItem(i);
	}

	@Override
	public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
	}

	@Override
	public void startUpdate(View container) {
	}

	public synchronized void insertFragment(Fragment frag) {
		insertFragment(mActionBar.newTab(), frag, -1);
	}

	public synchronized void insertFragment(Fragment frag, int before) {
		insertFragment(mActionBar.newTab(), frag, before);
	}

	public synchronized void insertFragment(ActionBar.Tab tab, Fragment frag,
			int before) {
		mFragments.add(frag);
		if (before >= mFragments.size() || before == -1) {
			before = -1;
			mFragments.add(frag);
			mSavedState.add(null);
		} else {
			mFragments.add(before, frag);
			mSavedState.add(before, null);
		}
		if (frag instanceof FragmentPageBase) {
			tab.setText(((FragmentPageBase) frag).getName());
		}
		tab.setTag(frag);
		tab.setTabListener(this);
		if (before != -1)
			mActionBar.addTab(tab);
		else
			mActionBar.addTab(tab, before);
		notifyDataSetChanged();
	}

	public synchronized void removeFragment(Fragment frag) {
		int i = mFragments.indexOf(frag);
		if (i == -1)
			return;
		mFragments.remove(i);
		mSavedState.remove(i);
		ActionBar.Tab tab = mActionBar.getTabAt(i);
		if (tab != null && tab.equals(frag)) {
			mActionBar.removeTabAt(i);
		} else {
			for (i = 0; i < mActionBar.getTabCount(); ++i) {
				if (mActionBar.getTabAt(i).equals(frag)) {
					mActionBar.removeTabAt(i);
				}
			}
		}
		notifyDataSetChanged();
	}

	public synchronized void replaceFragment(Fragment frag, int index) {
		replaceFragment(null, frag, index);
	}

	public synchronized void replaceFragment(ActionBar.Tab tab, Fragment frag,
			int index) {
		ActionBar.Tab tab2;
		if (tab != null) {
			tab2 = mActionBar.getTabAt(index);
			tab2.setTag(frag);
			tab2.setText(tab.getText());
			tab2.setIcon(tab.getIcon());
			tab2.setCustomView(tab.getCustomView());
			tab2.setContentDescription(tab.getContentDescription());
		}
		mFragments.set(index, frag);
		mSavedState.set(index, null);
		notifyDataSetChanged();
	}

	public synchronized void setFragmentTitle(Fragment frag, CharSequence title) {
		for (int i = 0; i < mActionBar.getTabCount(); ++i) {
			ActionBar.Tab tab = mActionBar.getTabAt(i);
			if (tab.getTag().equals(frag)) {
				tab.setText(title);
			}
		}
	}

	public synchronized void setFragmentTitle(Fragment frag, int title) {
		String name = frag.getTag();
		for (int i = 0; i < mActionBar.getTabCount(); ++i) {
			ActionBar.Tab tab = mActionBar.getTabAt(i);
			if (tab.getTag().equals(name)) {
				tab.setText(title);
			}
		}
	}

	@Override
	public Object instantiateItem(View container, int position) {
		if (mFragments.isEmpty()) {
			mShowEmptyView = true;
			if (mEmptyView == null)
				mEmptyView = View.inflate(container.getContext(),
						R.layout.page_empty, null);
			return mEmptyView;
		}
		if (mCurTransaction == null) {
			mCurTransaction = mFragmentManager.beginTransaction();
		}
		Fragment fragment = mFragments.get(position);
		if (mFragmentManager.getFragments().contains(fragment))
			return fragment;
		if (DEBUG)
			Log.v(TAG, "Adding item #" + position + ": f=" + fragment);
		Fragment.SavedState fss = mSavedState.get(position);
		if (fss != null) {
			fragment.setInitialSavedState(fss);
			mSavedState.set(position, null);
		}
		mCurTransaction.add(container.getId(), fragment);
		if (fragment != mCurrentPrimaryItem) {
			fragment.setMenuVisibility(false);
		}
		int n = position - mActionBar.getTabCount();
		
		return fragment;
	}

	@Override
	public void destroyItem(View container, int position, Object object) {
		if (mFragments.isEmpty()) {
			mShowEmptyView = false;
			return;
		}
		if (mCurTransaction == null) {
			mCurTransaction = mFragmentManager.beginTransaction();
		}
		Fragment fragment = (Fragment) object;
		if (DEBUG)
			Log.v(TAG, "Removing item #" + position + ": f=" + object + " v="
					+ ((Fragment) object).getView());
		mSavedState.set(position,
				mFragmentManager.saveFragmentInstanceState(fragment));
		mCurTransaction.remove((Fragment) object);
	}

	@Override
	public void setPrimaryItem(View container, int position, Object object) {
		if (object instanceof View)
			return;
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

	@Override
	public void finishUpdate(View container) {
		if (mFragments.isEmpty() && mShowEmptyView) {
			if (mEmptyView.getParent() == null)
				((ViewGroup) container).addView(mEmptyView);
		} else if (mEmptyView.getParent() != null) {
			((ViewGroup) container).removeView(mEmptyView);
		}
		if (mCurTransaction != null) {
			mCurTransaction.commitAllowingStateLoss();
			mCurTransaction = null;
			mFragmentManager.executePendingTransactions();
		}
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object || ((Fragment) object).getView() == view;
	}

	@Override
	public int getItemPosition(Object object) {
		return mFragments.indexOf(object);
	}

	@Override
	public CharSequence getPageTitle(int position) {
		Fragment frag = mFragments.get(position);
		if (frag instanceof FragmentPageBase) {
			return ((FragmentPageBase) frag).getName();
		}
		return super.getPageTitle(position);
	}

	@Override
	public Parcelable saveState() {
		Bundle state = null;
		if (mSavedState.size() > 0) {
			state = new Bundle();
			Fragment.SavedState[] fss = new Fragment.SavedState[mSavedState
					.size()];
			mSavedState.toArray(fss);
			state.putParcelableArray("states", fss);
		}
		for (int i = 0; i < mFragments.size(); i++) {
			Fragment f = mFragments.get(i);
			if (f != null) {
				if (state == null) {
					state = new Bundle();
				}
				String key = "f" + i;
				mFragmentManager.putFragment(state, key, f);
			}
		}
		return state;
	}

	@Override
	public void restoreState(Parcelable state, ClassLoader loader) {
		if (state != null) {
			Bundle bundle = (Bundle) state;
			bundle.setClassLoader(loader);
			Parcelable[] fss = bundle.getParcelableArray("states");
			mSavedState.clear();
			mFragments.clear();
			if (fss != null) {
				for (int i = 0; i < fss.length; i++) {
					mSavedState.add((Fragment.SavedState) fss[i]);
				}
			}
			Iterable<String> keys = bundle.keySet();
			for (String key : keys) {
				if (key.startsWith("f")) {
					int index = Integer.parseInt(key.substring(1));
					Fragment f = mFragmentManager.getFragment(bundle, key);
					if (f != null) {
						while (mFragments.size() <= index) {
							mFragments.add(null);
						}
						f.setMenuVisibility(false);
						mFragments.set(index, f);
					} else {
						Log.w(TAG, "Bad fragment at key " + key);
					}
				}
			}
		}
	}
}
