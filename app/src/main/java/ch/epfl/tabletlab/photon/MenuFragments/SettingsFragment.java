package ch.epfl.tabletlab.photon.MenuFragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.maps.GoogleMap;
import com.parse.ParseUser;

import ch.epfl.tabletlab.photon.MenuActivity;
import ch.epfl.tabletlab.photon.PhotonApplication;
import ch.epfl.tabletlab.photon.R;
import ch.epfl.tabletlab.photon.ResideMenu.ResideMenu;

/**
 * User: special
 * Date: 13-12-22
 * Time: 下午3:28
 * Mail: specialcyci@gmail.com
 */
public class SettingsFragment extends Fragment {
    private SeekBar seekBarNumber;
    private View parentView;
    private ResideMenu resideMenu;

    private boolean mapNormalMode = true;
    private Switch mapSwithch;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        parentView =  inflater.inflate(R.layout.settings, container, false);
        setUpSwitchMap();
        setSeekBar();
        return parentView;
    }

    private void setUpSwitchMap() {
        mapSwithch = (Switch) parentView.findViewById(R.id.switchMapType);
        if (PhotonApplication.MAP_TYPE == GoogleMap.MAP_TYPE_SATELLITE){
            mapSwithch.setChecked(true);
        }
        else{
            mapSwithch.setChecked(false);
        }

        mapSwithch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                if(mapNormalMode){
                    mapNormalMode = false;
                    PhotonApplication.MAP_TYPE = GoogleMap.MAP_TYPE_SATELLITE;
                }
                else{
                    mapNormalMode = true;
                    PhotonApplication.MAP_TYPE = GoogleMap.MAP_TYPE_NORMAL;
                }

            }
        });
    }


    private void setSeekBar() {
        seekBarNumber = (SeekBar)  parentView.findViewById(R.id.seekBarRestaurantDistance);

        //ignore view for swipping
        MenuActivity parentActivity = (MenuActivity) getActivity();
        resideMenu = parentActivity.getResideMenu();
        resideMenu.addIgnoredView(seekBarNumber);

        final TextView seekBarValue = (TextView) parentView.findViewById(R.id.value_distance_restaurant);
        final ParseUser currentUser = DataManager.getUser();
        if (currentUser == null) return;
        int seekbarValueInit = currentUser.getInt("numberDisplayed");
        if(0 != seekbarValueInit){
            seekBarNumber.setProgress(seekbarValueInit);
            seekBarValue.setText(String.valueOf(seekbarValueInit)+ " displayed");
        }

        final int[] seekvalue = {0};

        seekBarNumber.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                seekBarValue.setText(String.valueOf(progress) + " displayed");
                seekvalue[0] = progress;
                PhotonApplication.MAX_POST_SEARCH_RESULTS = seekvalue[0];
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                currentUser.put("numberDisplayed", seekvalue[0]);
                currentUser.saveInBackground();
                PhotonApplication.MAX_POST_SEARCH_RESULTS = seekvalue[0];
            }
        });

    }


}
