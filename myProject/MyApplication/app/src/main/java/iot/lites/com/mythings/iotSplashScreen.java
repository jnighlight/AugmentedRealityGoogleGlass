package iot.lites.com.mythings;

import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.LiveCard.PublishMode;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.util.Base64;
import android.widget.RemoteViews;

/**
 * A {@link Service} that publishes a {@link LiveCard} in the timeline.
 */
public class iotSplashScreen extends Service {

    private static final String LIVE_CARD_TAG = "iotSplashScreen";
    private LiveCard mLiveCard;
    private RemoteViews mLiveCardView;

    public class iotBinder extends Binder {
        /**
         * Read the current heading aloud using the text-to-speech engine.
         */
        public void readHeadingAloud(String change) {
            System.out.println("Got to changing station. About to attempt change to: " + change);
            /*mLiveCardView.setTextViewText(R.id.qr_val_id,
                    String.valueOf(change));
            mLiveCard.setViews(mLiveCardView);*/
            try {
                byte[] bob = Base64.decode(change, Base64.DEFAULT);
                System.out.println(bob[0] + ", " + bob[1]);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bob, 0, bob.length);
                if(bitmap != null) {
                    mLiveCardView.setImageViewBitmap(R.id.image_view_id, bitmap);
                    mLiveCard.setViews(mLiveCardView);
                }
                else
                {
                    System.out.println("Daaang, dat bitmap was null doe");
                }
            }
            catch (IllegalArgumentException e)
            {
                System.out.println("Base64 had an issues: " + e);
            }
            catch (NullPointerException e)
            {
                System.out.println("Null Pointer: " + e);
            }
        }
    }

    private final iotBinder mBinder = new iotBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mLiveCard == null) {
            mLiveCard = new LiveCard(this, LIVE_CARD_TAG);


            mLiveCardView = new RemoteViews(getPackageName(), R.layout.iot_splash_screen);
            mLiveCard.setViews(mLiveCardView);

            // Display the options menu when the live card is tapped.
            Intent menuIntent = new Intent(this, LiveCardMenuActivity.class);
            mLiveCard.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0));
            mLiveCard.publish(PublishMode.REVEAL);
        } else {
            mLiveCard.navigate();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mLiveCard != null && mLiveCard.isPublished()) {
            mLiveCard.unpublish();
            mLiveCard = null;
        }
        super.onDestroy();
    }
}
