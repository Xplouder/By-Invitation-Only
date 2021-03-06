package pt.ipleiria.estg.es2.byinvitationonly;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.facebook.FacebookSdk;
import com.firebase.client.Firebase;

import pt.ipleiria.estg.es2.byinvitationonly.Controllers.FirebaseController;


public class SplashScreenActivity extends Activity {

    private Thread timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        timer = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(3000);
                    Intent i = new Intent(SplashScreenActivity.this, MainActivity.class);
                    startActivity(i);
                    finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        ImageView myImageView = (ImageView) findViewById(R.id.imageViewLogo);
        Animation myFadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.tween);
        myImageView.startAnimation(myFadeInAnimation);
        timer.start();
        setFirebaseConfigs();
        FacebookSdk.sdkInitialize(getApplicationContext());
    }

    private void setFirebaseConfigs() {
        Firebase.setAndroidContext(getApplicationContext());
        if (!Firebase.getDefaultConfig().isPersistenceEnabled()) {
            Firebase.getDefaultConfig().setPersistenceEnabled(true);
        }
        FirebaseController.fbSessionsNode.keepSynced(true);
        FirebaseController.fbConferenceNode.keepSynced(true);
    }
}
