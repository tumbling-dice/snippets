package com.example.androidwithscala

import android.app.Activity
import android.os.Bundle

class ScalaActivity extends Activity {

  override def onCreate(savedInstanceState : Bundle) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
  }
}