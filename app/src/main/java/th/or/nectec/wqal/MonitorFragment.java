package th.or.nectec.wqal;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MonitorFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MonitorFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MonitorFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private TextView mBtSerialTextview;

    private TextView mEcTextview;
    private TextView mDoTextview;
    private TextView mPhTextview;
    private TextView mTmTextview;

    private WqmStation mWqmStation = WqmStation.getInstance();

    public MonitorFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MonitorFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MonitorFragment newInstance(String param1, String param2) {
        MonitorFragment fragment = new MonitorFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_monitor, container, false);

        mBtSerialTextview = v.findViewById(R.id.tvBtSerial);

        mEcTextview = v.findViewById(R.id.tvEcVal);
        mDoTextview = v.findViewById(R.id.tvDoVal);
        mPhTextview = v.findViewById(R.id.tvPhVal);
        mTmTextview = v.findViewById(R.id.tvTempVal);

        mWqmStation.setOnWqmStationCmdResponseListener(new WqmStation.OnWqmStationCmdResponseListener() {
            @Override
            public void onWqmStationCmdReponse() {
                showEc(mWqmStation.getEc());
                showDo(mWqmStation.getDo());
                showPh(mWqmStation.getPh());
                showTm(mWqmStation.getTemp());
            }
        });

        startAutoUpdate();

        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    void receivedData(String msg) {
        mBtSerialTextview.append(msg);
    }

    void showDo(float do_) {
        mDoTextview.setText(String.valueOf(do_));
    }

    void showPh(float ph) {
        mPhTextview.setText(String.valueOf(ph));
    }

    void showEc(float ec) {
        mEcTextview.setText(String.valueOf(ec));
    }

    void showTm(float tm) { mTmTextview.setText(String.valueOf(tm)); }

    private Handler mAutoUpdateHandler;
    private final int mUpdateInterval = 5000;

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
      /* do what you need to do */
            update();
      /* and here comes the "trick" */
            mAutoUpdateHandler.postDelayed(this, mUpdateInterval);
        }
    };


    private void update() {
        //mBt.send("$ wqalarm get 0",  true);
        mWqmStation.update();
        Log.i("auto_update", "Update Screen");

//        showEc(mWqmStation.getEc());
//        showDo(mWqmStation.getDo());
//        showPh(mWqmStation.getPh());
//        showTm(mWqmStation.getTemp());

    }

    private void startAutoUpdate() {
        mAutoUpdateHandler = new Handler();
        mAutoUpdateHandler.postDelayed(runnable, mUpdateInterval);
    }

    private void stopAutoUpdate() {
        mAutoUpdateHandler.removeCallbacks(runnable);
    }
}
