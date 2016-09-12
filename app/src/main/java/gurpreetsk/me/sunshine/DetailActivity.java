package gurpreetsk.me.sunshine;

import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.settings) {
//            return true;
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.show_location) {
            openLocationInMap();
        }

        return super.onOptionsItemSelected(item);
    }

    private void openLocationInMap() {

        String location = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getString(getString(R.string.pref_loc_key), getString(R.string.pref_default_value));

        //build location string to be searched
        Uri geoLocation = Uri.parse("geo:0,0?").buildUpon()
                .appendQueryParameter("q", location)
                .build();

        Intent map = new Intent(Intent.ACTION_VIEW);
        map.setData(geoLocation);

        if (map.resolveActivity(getPackageManager()) != null)
            startActivity(map);
        else
            Log.e("DetailActivity", "Maps app not found");

    }

}
