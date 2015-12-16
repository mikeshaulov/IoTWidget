package michaelsha.iotwidget;

import android.app.Fragment;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 * A placeholder fragment containing a simple view.
 */
public class mDNSDiscoveryFragment extends Fragment implements mDNSDiscovery.INotifier {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    private mDNSDiscovery mmDNSDiscovery;
    private ListView mDNSListView;
    private ArrayAdapter<mDNSDiscovery.NsdServiceInfoWrapper> mDNSListAdapter;
    private static final String TAG = mDNSDiscoveryFragment.class.getSimpleName();

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static mDNSDiscoveryFragment newInstance(int sectionNumber) {
        mDNSDiscoveryFragment fragment = new mDNSDiscoveryFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public mDNSDiscoveryFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mmDNSDiscovery = new mDNSDiscovery(getActivity());
        mDNSListView = (ListView) rootView.findViewById(R.id.mDNSListView);
        mDNSListAdapter = new ArrayAdapter(getContext(),android.R.layout.simple_list_item_1,mmDNSDiscovery.getServiceList());
        mDNSListView.setAdapter(mDNSListAdapter);

        mDNSListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // resolve the service for the selected item
                mDNSDiscovery.NsdServiceInfoWrapper infoWrapper = mDNSListAdapter.getItem(position);
                if(infoWrapper != null)
                {
                    mmDNSDiscovery.ResolveService(infoWrapper);
                }

            }
        });

                // start discovery process
                mmDNSDiscovery.startDiscovery(mDNSDiscovery.SERVICE_TYPE_HTTP_TCP, this);

        return rootView;
    }

    @Override
    public void onNewNetworkDevicesAvailable() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDNSListAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onNetworkServiceResolved(final NsdServiceInfo serviceInfo) {
        // Gets the data repository in write mode
        mDNSDBHelper mDbHelper = new mDNSDBHelper(getContext());
        try {
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            // Create a new map of values, where column names are the keys
            ContentValues values = new ContentValues();
            values.put(mDNSDBHelper.mDNSEntry.COLUMN_NAME_MDNS, serviceInfo.getServiceName());
            values.put(mDNSDBHelper.mDNSEntry.COLUMN_NAME_TYPE, IoTDeviceType.Shutter);
            values.put(mDNSDBHelper.mDNSEntry.COLUMN_NAME_IP, serviceInfo.getHost().getHostAddress());
            values.put(mDNSDBHelper.mDNSEntry.COLUMN_NAME_PORT, serviceInfo.getPort());


            // Insert the new row, returning the primary key value of the new row
            db.insertWithOnConflict(
                    mDNSDBHelper.mDNSEntry.TABLE_NAME,
                    mDNSDBHelper.mDNSEntry.COLUMN_NAME_MDNS,
                    values,
                    SQLiteDatabase.CONFLICT_REPLACE);
        }
        catch(Exception e)
        {
            e.printStackTrace();

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(),"Failed to save routing for service " + serviceInfo.getServiceName() + " added at IP " + serviceInfo.getHost().getHostAddress(),Toast.LENGTH_LONG).show();
                }
            });

        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(),"Saved routing for service " + serviceInfo.getServiceName() + " added at IP " + serviceInfo.getHost().getHostAddress(),Toast.LENGTH_LONG).show();
            }
        });
    }
}