package iot.lites.com.mythings;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.glass.timeline.LiveCard;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Set;

/**
 * A transparent {@link Activity} displaying a "Stop" options menu to remove the {@link LiveCard}.
 */
public class LiveCardMenuActivity extends Activity {

    private static final int QR_REQUEST= 77;
    private iotSplashScreen.iotBinder mBinderService;

    // Requested actions.
    private boolean mDoStop;
    private boolean mDoGo;
    private boolean mDoServer;
    private boolean mDoFinish;
    private boolean mDoFinishWait;


    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service instanceof iotSplashScreen.iotBinder) {
                mBinderService = (iotSplashScreen.iotBinder) service;
                System.out.println("CONNECTED");
                performActionsIfConnected();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            System.out.println("UNCONNECTED");
            // Do nothing.
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindService(new Intent(this, iotSplashScreen.class), mConnection, 0);
    }


    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        // Open the options menu right away.
        openOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.iot_splash_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_stop:
                mDoStop = true;
                return true;
            case R.id.action_go:
                //this.scanForQR();
                mDoGo = true;
                return true;
            /*case R.id.action_server:
                mDoServer = true;
                return true;*/
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        super.onOptionsMenuClosed(menu);
        // Nothing else to do, finish the Activity.
        mDoFinish = true;
        performActionsIfConnected();
    }

    private void performActionsIfConnected() {
        System.out.println("Here to Perform Actions (if connected)");
        System.out.println("is mBinderService null? : " + mBinderService==null? "Yes, do nothing" : "No, go into menus");
        if ( mBinderService != null) {
            if (mDoGo) {
                mDoGo = false;
                mDoFinishWait = true;
                System.out.println("performing action scan for QR");
                this.scanForQR();
                //this.contactServer();
                //mDoFinishWait = false;
            }
            if (mDoStop){
                mDoStop = false;
                System.out.println("performing action to end the app");
                // Stop the service which will unpublish the live card.
                stopService(new Intent(this, iotSplashScreen.class));
            }
            if (mDoServer) {
                mDoServer = false;
                mDoFinishWait = true;
                System.out.println("performing action contact server");
                this.contactServer();
            }
            //DoFinish ends the app, unless we're waiting on a response from the QR Scanner
            //If we are, then we'll change the doFinishWait variable and recall this method when
            //we get a response back
            if (mDoFinish && !mDoFinishWait)
            {
                System.out.println("performing action to finish the menu (nono unless already got data back)");
                mBinderService = null;
                unbindService(mConnection);
                finish();
            }
            System.out.println("End of menus/performing actions");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( requestCode == QR_REQUEST && resultCode == RESULT_OK) {
            String result = data.getStringExtra("SCAN_RESULT");
            System.out.println("SCAN_RESULT:" + result);
            if(mBinderService != null)
            {
                //mBinderService.readHeadingAloud(result);
                if(result.equals("1"))
                {
                    this.contactServer();
                }
                else
                {
                    System.out.println("Unexpected QR code scanned");
                }
            }
            else
                {System.out.println("CRITICAL ERROR: BINDER SERVICE WAS NULL");}
            //mDoFinish = true;
            //mDoFinishWait = false;
            //performActionsIfConnected();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void scanForQR()
    {
        System.out.println("Requesting QR scan by intent");
        Intent objIntent = new Intent("com.google.zxing.client.android.SCAN");
        objIntent.putExtra("SCAN_MODE", "QR_CODE_MODE");
        startActivityForResult(objIntent, QR_REQUEST);
    }

    public void canFinish(String message)
    {
        if(mBinderService != null)
        {
            if(message == null)
                {//mBinderService.readHeadingAloud("The server is not up right now");
                 System.out.println("In CanFinish, message was NULL");}
            else
                {mBinderService.readHeadingAloud(message);}
        }
        else
        {System.out.println("CRITICAL ERROR: BINDER SERVICE WAS NULL");}
        mDoFinishWait = false;
        performActionsIfConnected();
    }

    private void contactServer()
    {
        System.out.println("Gonna connect to that sweet, sweet server");
        String hostName = "galactus.stetson.edu";
        String portNumber = "80";
        new ServerTalk().execute((Object) hostName, (Object) portNumber, (Object) this);
        /*try {
            Socket kkSocket = new Socket(hostName, portNumber);
            PrintWriter out = new PrintWriter(kkSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(kkSocket.getInputStream()));
            BufferedReader stdIn =
                    new BufferedReader(new InputStreamReader(System.in));
            String fromServer;
            String fromUser;

            out.println("connected");
            while ((fromServer = in.readLine()) != null) {
                System.out.println("Server: " + fromServer);
                if (fromServer.equals("Bye."))
                    break;

                /*fromUser = stdIn.readLine();
                if (fromUser != null) {
                    System.out.println("Client: " + fromUser);
                    out.println(fromUser);
                }
            //}
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            //System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                    hostName);
            //System.exit(1);
        }
        */
        System.out.println("FINISHED SERVER TALK");
    }
}


