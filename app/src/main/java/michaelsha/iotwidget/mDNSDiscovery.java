package michaelsha.iotwidget;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;
import android.widget.BaseAdapter;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by michael on 12/12/15.
 * Helper class to locate mDNS (Zero-Configuration) services on the network
 */
public class mDNSDiscovery {

    public interface INotifier {
        void onNewNetworkDevicesAvailable();
        void onNetworkServiceResolved(NsdServiceInfo serviceInfo);
    }

    private static final String TAG = mDNSDiscovery.class.getSimpleName();
    private INotifier mNotifier;
    private NsdManager.DiscoveryListener mDiscoveryListener;
    private NsdManager.ResolveListener mResolveListener;
    private NsdManager mNsdManager;

    /***
     *  Wrapper class for the NsdServiceInfo
     */
    public static class NsdServiceInfoWrapper
    {
        NsdServiceInfo mNsdServiceInfo;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            NsdServiceInfoWrapper that = (NsdServiceInfoWrapper) o;

            return mNsdServiceInfo.toString().equals(that.mNsdServiceInfo.toString());
        }

        @Override
        public int hashCode() {
            return mNsdServiceInfo.hashCode();
        }

        public NsdServiceInfoWrapper(NsdServiceInfo mNsdServiceInfo) {
            this.mNsdServiceInfo = mNsdServiceInfo;
        }

        public NsdServiceInfo getmNsdServiceInfo() {
            return mNsdServiceInfo;
        }

        @Override
        public String toString() {
                return mNsdServiceInfo.getServiceName();
        }
    }

    private LinkedList<NsdServiceInfoWrapper> mServiceList = new LinkedList<>();


    public final static String SERVICE_TYPE_HTTP_TCP = "_http._tcp";

    public LinkedList<NsdServiceInfoWrapper> getServiceList() {
        return mServiceList;
    }

    /***
     * DNS Discovery constructor
     * @param ctx   reference to a valid context object
     */
    public mDNSDiscovery(Context ctx)
    {
        initializeDiscoveryListener();
        initializeResolveListener();
        mNsdManager =  (NsdManager)ctx.getSystemService(Context.NSD_SERVICE);
    }

    /***
     * Starts the mDNS discovery process
     * @param serviceType   service type to search for
     * @param notifier      interface to notify when data is changed during scan
     */
    public void startDiscovery(String serviceType, INotifier notifier)
    {
        mNotifier = notifier;
        mNsdManager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
    }

    private void initializeDiscoveryListener() {

        // Instantiate a new DiscoveryListener
        mDiscoveryListener = new NsdManager.DiscoveryListener() {

            //  Called as soon as service discovery begins.
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started...");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                // A service was found!  Do something with it.
                Log.d(TAG, "Service discovery success" + service);


                NsdServiceInfoWrapper serviceInfo = new NsdServiceInfoWrapper(service);
                // add service lists that are new
                if(!mServiceList.contains(serviceInfo)) {
                    mServiceList.add(serviceInfo);

                    if (mNotifier != null)
                        mNotifier.onNewNetworkDevicesAvailable();
                }

                /*if(service.getServiceName().equals("triss")) {
                    mNsdManager.resolveService(service, mResolveListener);
                }*/
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                // When the network service is no longer available.
                // Internal bookkeeping code goes here.
                Log.e(TAG, "service lost" + service);
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }
        };
    }


    public void ResolveService(NsdServiceInfoWrapper infoWrapper) {
        mNsdManager.resolveService(infoWrapper.getmNsdServiceInfo(),mResolveListener);
    }


    public void initializeResolveListener() {
        mResolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Called when the resolve fails.  Use the error code to debug.
                Log.e(TAG, "Resolve failed" + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.e(TAG, "Resolve Succeeded. " + serviceInfo);
                mNotifier.onNetworkServiceResolved(serviceInfo);
            }
        };
    }

}
