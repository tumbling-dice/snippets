package com.example.activity;

import java.util.HashMap;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.adapter.DynamicFragmentPagerAdapter;
import com.example.fragment.FugaFragment;
import com.example.fragment.HogeFragment;
import com.example.fragment.PiyoFragment;

public class MainActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ViewPager pager = (ViewPager) findViewById(R.id.pager);
		PagerTabStrip strip = (PagerTabStrip) findViewById(R.id.strip);
		strip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		strip.setTextColor(0xff9acd32);
		strip.setTextSpacing(50);
		strip.setNonPrimaryAlpha(0.3f);
		strip.setDrawFullUnderline(true);
		strip.setTabIndicatorColor(0xff9acd32);


		HashMap<String, Fragment> fragments = new HashMap<String, Fragment>();
		fragments.put("Fuga", new FugaFragment());
		fragments.put("Hoge", new HogeFragment());

		DynamicFragmentPagerAdapter adapter = new DynamicFragmentPagerAdapter(getSupportFragmentManager(), fragments);
		adapter.add("Piyo", new PiyoFragment());

		pager.setAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		ViewPager pager = (ViewPager) findViewById(R.id.pager);
		DynamicFragmentPagerAdapter adapter = (DynamicFragmentPagerAdapter) pager.getAdapter();
		Bundle arg = new Bundle();

		switch (item.getItemId()) {
			case R.id.itemAdd:
				FugaFragment fugaFragment = new FugaFragment();
				arg.putString("text", "addしたFugaだよ");
				fugaFragment.setArguments(arg);
				adapter.add("FugaFuga", fugaFragment);
				break;
			case R.id.itemReplace:
				HogeFragment hogeFragment = new HogeFragment();
				arg.putString("text", "PiyoをreplaceしてHogeにしたよ");
				hogeFragment.setArguments(arg);
				adapter.replace(2, "HogeHoge", hogeFragment);
				break;
			case R.id.itemInsert:
				PiyoFragment piyoFragment = new PiyoFragment();
				arg.putString("text", "HogeとFugaの間にPiyoを挿入したよ");
				piyoFragment.setArguments(arg);
				adapter.insert(1, "PiyoPiyo", piyoFragment);
				break;
			case R.id.itemDelete:
				adapter.remove(0);
				Toast.makeText(getApplicationContext(), "Hogeを削除したよ", Toast.LENGTH_SHORT).show();
				break;
			default:
				return false;
		}

		adapter.notifyDataSetChanged();
		return true;
	}

}
