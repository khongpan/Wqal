package th.or.nectec.wqal;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

public class MainActivity extends AppCompatActivity
        implements MonitorFragment.OnFragmentInteractionListener
        , DebugFragment.OnFragmentInteractionListener
        , CalibrateFragment.OnFragmentInteractionListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private String arg1, arg2;

    BluetoothSPP mBt;

    private MonitorFragment mMonitorFragment;
    private DebugFragment mDebugFragment;
    private CalibrateFragment mCalibrateFragment;

    private WqmStation mWqmStation = WqmStation.getInstance();

    private ChatData mChatData = ChatData.getInstance();


    private Handler mAutoUpdateHandler;
    private final int mUpdateInterval = 5000;

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
      /* do what you need to do */
            //update();
      /* and here comes the "trick" */
            mAutoUpdateHandler.postDelayed(this, mUpdateInterval);
        }
    };


    private void update() {
        //mBt.send("$ wqalarm get 0",  true);
        mWqmStation.update();
        Log.i("auto_update", "Update Screen");
        if (mMonitorFragment != null) {
            mMonitorFragment.showEc(mWqmStation.getEc());
            mMonitorFragment.showDo(mWqmStation.getDo());
            mMonitorFragment.showPh(mWqmStation.getPh());
            mMonitorFragment.showTm(mWqmStation.getTemp());
        }
    }

    private void startAutoUpdate() {
        mAutoUpdateHandler = new Handler();
        mAutoUpdateHandler.postDelayed(runnable, mUpdateInterval);
    }

    private void stopAutoUpdate() {
        mAutoUpdateHandler.removeCallbacks(runnable);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        setupBluetooth();
        setupWqmStation();

    }


    private void setupBluetooth() {
        mBt = new BluetoothSPP(this);
        if (!mBt.isBluetoothAvailable()) {
            Toast.makeText(getApplicationContext()
                    , "Bluetooth is not available"
                    , Toast.LENGTH_LONG).show();
            finish();
        }


//        mBt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
//            public void onDataReceived(byte[] data, String message) {
//                //textRead.append(message + "\n");
//
//                if(null != mMonitorFragment  /*&& mMonitorFragment.isInLayout()*/)
//                {
//                    mMonitorFragment.receivedData(message + "\n");
//                }
//                if (null != mDebugFragment) {
//                    mDebugFragment.receivedData(message+"\n");
//                }
//
//                if (null != mChatData) {
//                    mChatData.append(message+"\n");
//                }
//                String[] splited = message.split("\\s+");
//            }
//        });


        mBt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            public void onDeviceDisconnected() {
                //textStatus.setText("Status : Not connect");
                //menu.clear();
                //getMenuInflater().inflate(R.menu.menu_connection, menu);
                stopAutoUpdate();
                Toast.makeText(MainActivity.this, "BT: Disconnect", Toast.LENGTH_LONG).show();
            }

            public void onDeviceConnectionFailed() {
                //textStatus.setText("Status : Connection failed");
                Toast.makeText(MainActivity.this, "BT: fail to connect", Toast.LENGTH_LONG).show();

            }

            public void onDeviceConnected(String name, String address) {
                //textStatus.setText("Status : Connected to " + name);
                //menu.clear();
                //getMenuInflater().inflate(R.menu.menu_disconnection, menu);
                Toast.makeText(MainActivity.this, "BT: Connect", Toast.LENGTH_LONG).show();
                startAutoUpdate();
            }
        });
    }

    private void setupWqmStation() {
        mWqmStation.setBt(mBt);
        mWqmStation.setOnWqmStationDataReceivedListener(new WqmStation.OnWqmStationDataReceivedListener() {
            public void onWqmStationDataReceived(byte[] data, String message) {
                //textRead.append(message + "\n");

                if (null != mMonitorFragment  /*&& mMonitorFragment.isInLayout()*/) {
                    mMonitorFragment.receivedData(message + "\n");
                }
                if (null != mDebugFragment) {
                    mDebugFragment.receivedData(message + "\n");
                }

                if (null != mChatData) {
                    mChatData.append(message + "\n");
                }
                String[] splited = message.split("\\s+");
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (!mBt.isBluetoothEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            if (!mBt.isServiceAvailable()) {
                mBt.setupService();
                mBt.startService(BluetoothState.DEVICE_ANDROID);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBt.stopService();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.actionConnectBt) {

            if (mBt.getServiceState() == BluetoothState.STATE_CONNECTED)
                mBt.disconnect();

            mBt.setDeviceTarget(BluetoothState.DEVICE_OTHER);
            Intent intent = new Intent(getApplicationContext(), DeviceList.class);
            startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);

        }

        if (id == R.id.actionDisconnectBt) {
            if (mBt.getServiceState() == BluetoothState.STATE_CONNECTED)
                mBt.disconnect();
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).

            switch (position) {
                case 0:
                    mMonitorFragment = new MonitorFragment();
                    return mMonitorFragment;
                case 1:
                    mCalibrateFragment = new CalibrateFragment();
                    return mCalibrateFragment;
                case 2:
                    mDebugFragment = new DebugFragment();
                    return mDebugFragment;
                default:
                    return PlaceholderFragment.newInstance(position + 1);
            }

        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK)
                mBt.connect(data);
        } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                mBt.setupService();
                mBt.startService(BluetoothState.DEVICE_ANDROID);
                //setup();
            } else {
                Toast.makeText(getApplicationContext()
                        , "Bluetooth was not enabled."
                        , Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }


}
