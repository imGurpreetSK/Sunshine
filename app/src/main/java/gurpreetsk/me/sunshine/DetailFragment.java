package gurpreetsk.me.sunshine;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ShareActionProvider;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class DetailFragment extends Fragment {


    public DetailFragment() {
        // Required empty public constructor
    }

    String mForecast;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_detail, container, false);
//        tv = (TextView) v.findViewById(R.id.detailTextView);

        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)){
            mForecast = intent.getStringExtra(Intent.EXTRA_TEXT);
            ((TextView) v.findViewById(R.id.detailTextView)).setText(mForecast);
        }

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem menuItem = menu.findItem(R.id.shareActionProv);
        android.support.v7.widget.ShareActionProvider sap = (android.support.v7.widget.ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        if(sap!=null){
            sap.setShareIntent(createShareForecastIntent());
        }else{
            Log.e("DetailFragment", "SAP null");
        }
    }

    private Intent createShareForecastIntent(){
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mForecast+" #SunshineApp");
        return shareIntent;
    }


}
