package th.or.nectec.wqal;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CalibrateFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CalibrateFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CalibrateFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private Spinner mPhRefSpinner;
    private Spinner mEcRefSpinner;
    private Spinner mDoRefSpinner;

    private Button mDoCalBtn;
    private Button mEcCalBtn;
    private Button mPhCalBtn;

    private TextView mVDoTv;
    private TextView mVEcTv;
    private TextView mVPhTv;

    private TextView mCalStatusTv;

    private WqmStation mWqmStation = WqmStation.getInstance();

    public CalibrateFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CalibrateFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CalibrateFragment newInstance(String param1, String param2) {
        CalibrateFragment fragment = new CalibrateFragment();
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
        View v;
        v = inflater.inflate(R.layout.fragment_calibrate, container, false);


        mEcRefSpinner = v.findViewById(R.id.spEcRef);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter_ec = ArrayAdapter.createFromResource(getActivity(),
                R.array.ec_ref_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter_ec.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mEcRefSpinner.setAdapter(adapter_ec);

        mDoRefSpinner = v.findViewById(R.id.spDoRef);
        ArrayAdapter<CharSequence> adapter_do = ArrayAdapter.createFromResource(getActivity(),
                R.array.do_ref_array, android.R.layout.simple_spinner_item);
        adapter_do.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mDoRefSpinner.setAdapter(adapter_do);

        mPhRefSpinner = (Spinner) v.findViewById(R.id.spPhRef);
        ArrayAdapter<CharSequence> adapter_ph = ArrayAdapter.createFromResource(getActivity(),
                R.array.ph_ref_array, android.R.layout.simple_spinner_item);
        adapter_ph.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mPhRefSpinner.setAdapter(adapter_ph);



        mDoCalBtn = v.findViewById(R.id.btDoCal);
        mEcCalBtn = v.findViewById(R.id.btEcCal);
        mPhCalBtn = v.findViewById(R.id.btPhCal);

        mVDoTv = v.findViewById(R.id.tvMvDo);
        mVEcTv = v.findViewById(R.id.tvMvEc);
        mVPhTv = v.findViewById(R.id.tvMvPh);

        mCalStatusTv = v.findViewById(R.id.tvCalStatus);


        mWqmStation.setOnWqmStationCmdResponseListener(new WqmStation.OnWqmStationCmdResponseListener() {
            @Override
            public void onWqmStationCmdReponse() {

                showVDo(mWqmStation.getVDo());
                showVEc(mWqmStation.getVEc());
                showVPh(mWqmStation.getVPh());

            }
        });


        startCalibratingHandler();
        // Inflate the layout for this fragment
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

    private  void showVDo(float v_do) {
        mVDoTv.setText(String.valueOf(v_do));
    }

    private  void showVEc(float v_ec) {
        mVEcTv.setText(String.valueOf(v_ec));
    }

    private  void showVPh(float v_ph) {
        mVPhTv.setText(String.valueOf(v_ph));
    }


    private enum CalcState {
        IDLE,
        C_PH,
        C_DO,
        C_EC,
        DONE,
    }
    private void setCalcState(CalcState state) {
        mCalcState = state;
    }

    private CalcState mCalcState = CalcState.IDLE;

    private Handler mCalibratingHandler;
    private final int mUpdateInterval = 1000;
    private int mRound=0;

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {

            mWqmStation.addCommand(WqmStation.CMD_GET_VDO);
            mWqmStation.addCommand(WqmStation.CMD_GET_VEC);
            mWqmStation.addCommand(WqmStation.CMD_GET_VPH);
            
      /* do what you need to do */
            switch (mCalcState) {
                case IDLE:
                    break;
                case C_DO:
                    break;
                case C_PH:
                    break;
                case C_EC:
                    break;
                default:
                    break;
            }
            mCalibratingHandler.postDelayed(this, mUpdateInterval);
            mCalStatusTv.setText(String.valueOf(mRound++));
        }
    };

    private void startCalibratingHandler(){

        if (mCalibratingHandler==null) {
            mCalibratingHandler = new Handler();
        }
        mCalibratingHandler.postDelayed(runnable,mUpdateInterval);

    }

    void stopCalibratingHandler() {
        mCalibratingHandler.removeCallbacks(runnable);
    }


}
