package pt.ipleiria.estg.es2.byinvitationonly.CustomComponents;

import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;

import pt.ipleiria.estg.es2.byinvitationonly.Controllers.SharedPreferenceController;
import pt.ipleiria.estg.es2.byinvitationonly.Drawer.SectionFragments.ConferenceScheduleFragment;
import pt.ipleiria.estg.es2.byinvitationonly.MainActivity;


public class FilterSpinnerInteractionListener implements AdapterView.OnItemSelectedListener, View.OnTouchListener {

    private boolean userSelect = false;
    private MainActivity mainActivity;
    private ConferenceScheduleFragment conferenceFrag;

    /**
     * Listener criado com o intuito de prevenir eventos indesejados despoletados
     * automaticamente na atribuição dos listener sobre um spinner.
     *
     * @param mainActivity   - Main Activity
     * @param conferenceFrag - Conference Fragment
     */
    public FilterSpinnerInteractionListener(MainActivity mainActivity, ConferenceScheduleFragment conferenceFrag) {
        this.mainActivity = mainActivity;
        this.conferenceFrag = conferenceFrag;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        userSelect = true;
        return false;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        if (userSelect) {
            // user code goes here
            switch (pos) {
                case 0:
                    conferenceFrag.changeToOriginalSessionList();
                    SharedPreferenceController.disableFilterState(mainActivity);
                    break;
                case 1:
                    mainActivity.openFilterDialog(conferenceFrag.getTrackList());
                    SharedPreferenceController.enableFilterState(mainActivity);
                    break;
                case 2:
                    mainActivity.filterByOnAgenda();
                    SharedPreferenceController.enableFilterState(mainActivity);
                    break;
            }
            // user code ends
            userSelect = false;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

}