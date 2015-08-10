package com.intel.bugkoops;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

public class MenuActivity extends ActionBarActivity {
    final String LOG_TAG = getClass().getSimpleName();

    protected Menu mMenu;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.action_reports:
                startActivity(new Intent(this, ReportActivity.class));
                break;
            case R.id.action_about:
                Utility.showAbout(this);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            mMenu.performIdentifierAction(R.id.action_options, 0);
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }
}
