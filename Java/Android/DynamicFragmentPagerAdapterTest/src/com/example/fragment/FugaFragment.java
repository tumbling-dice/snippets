package com.example.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.activity.R;

public class FugaFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_fuga, null);

		Bundle arg = getArguments();
		if(arg != null) {
			((TextView)v.findViewById(R.id.textView1)).setText(arg.getString("text"));
		}

		return v;
	}



}
