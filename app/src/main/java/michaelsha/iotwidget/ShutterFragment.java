package michaelsha.iotwidget;

import android.app.Fragment;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by michael on 12/14/15.
 */
public class ShutterFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    private static final String TAG = ShutterFragment.class.getSimpleName();

    public static Fragment newInstance(int sectionNumber) {
        ShutterFragment fragment = new ShutterFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.shutter_control_fragment, container, false);

        final Button btnUp = (Button) rootView.findViewById(R.id.btnUp);
        final Button btnDown = (Button) rootView.findViewById(R.id.btnDown);

        btnUp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                activateShutter("up");
            }
        });

        btnDown.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                activateShutter("down");
            }
        });

        return rootView;
    }

    private void activateShutter(String direction)
    {
        // Gets the data repository in reading mode
        mDNSDBHelper mDbHelper = new mDNSDBHelper(getContext());
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Get the IP and Port of the Shutter
        final String[] projection = {
                mDNSDBHelper.mDNSEntry.COLUMN_NAME_IP,
                mDNSDBHelper.mDNSEntry.COLUMN_NAME_PORT,
        };

        final String[] whereArgs = {IoTDeviceType.Shutter};

        try {

            Cursor c = db.query(
                    mDNSDBHelper.mDNSEntry.TABLE_NAME,
                    projection,
                    mDNSDBHelper.mDNSEntry.COLUMN_NAME_TYPE + "=?",
                    whereArgs,
                    null, null, null);

            if (!c.moveToFirst()) {
                Toast.makeText(getActivity(), "Failed to find Shutter records in the database", Toast.LENGTH_LONG).show();
                return;
            }

            String ipStr = c.getString(c.getColumnIndexOrThrow(mDNSDBHelper.mDNSEntry.COLUMN_NAME_IP));
            int port = c.getInt(c.getColumnIndexOrThrow(mDNSDBHelper.mDNSEntry.COLUMN_NAME_PORT));


            // invoke URL open command on the shutter
            String urlAddress = "http://" + ipStr + ":" + port + "/" + direction;
            Log.i(TAG, "Calling " + direction + " on Shutter at " + urlAddress);

            new AsyncTask<String, Void, Integer>()
            {

                @Override
                protected Integer doInBackground(String... params) {
                    try {
                        URL url = new URL(params[0]);
                        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                        int code = urlConnection.getResponseCode();
                        String response = urlConnection.getResponseMessage();
                        Log.i(TAG, "received response from server [code] " + code + " [message] " + response);
                        urlConnection.disconnect();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return 0;
                }

            }.execute(urlAddress);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Toast.makeText(getActivity(), "Failed to issue command to shutter: " + ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
