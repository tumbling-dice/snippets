package com.example.adapter;

import java.util.Map;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

public class SubFragmentPagerAdapter extends DynamicFragmentPagerAdapter{

	public SubFragmentPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	public SubFragmentPagerAdapter(FragmentManager fm, Map<CharSequence, Fragment> fragments) {
		super(fm, fragments);
	}

	@Override
	public void remove(int position) {
		FragmentTransactionProxy ftp =  super.getFragmentTransactionProxy();
		ftp.remove(super.getFragmentInfo(position).getFragment());
		super.getFragmentInfoes().remove(position);
		super.needAllChange();
	}

}
