package com.ust.qspace

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

import kotlinx.android.synthetic.main.activity_info.*

class InfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)
        setSupportActionBar(toolbar)

        tv_info_intro.text = getString(R.string.info_intro, uName)
        tv_info_misvis.text = getString(R.string.info_mission) + "\n\n" + getString(R.string.info_vision)+
                "\n\n" + getString(R.string.info_how)
        tv_info_faq.text = getString(R.string.info_how_to)

    }

}
