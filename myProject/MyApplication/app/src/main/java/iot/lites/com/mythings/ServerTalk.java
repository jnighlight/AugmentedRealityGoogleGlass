package iot.lites.com.mythings;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by Owner on 11/10/2014.
 */
class ServerTalk extends AsyncTask<Object, Void, String>
{
    protected String doInBackground(Object...serverData)
    {
        System.out.println("THIS IS IN THE BACKGROUND");
        String fromServer = "-1";
        LiveCardMenuActivity lcma = (LiveCardMenuActivity)serverData[2];
        try {
            Socket kkSocket = new Socket((String)serverData[0], Integer.parseInt((String)serverData[1]));
            PrintWriter out = new PrintWriter(kkSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(kkSocket.getInputStream()));
            BufferedReader stdIn =
                    new BufferedReader(new InputStreamReader(System.in));
            String fromUser;

            out.println("connected");
            while ((fromServer = in.readLine()) != null) {
                System.out.println("Server: " + fromServer);
                break;
                //if (fromServer.equals("Bye."))
                    //break;

                /*fromUser = stdIn.readLine();
                if (fromUser != null) {
                    System.out.println("Client: " + fromUser);
                    out.println(fromUser);
                }*/
            }
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + serverData[0]);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                    serverData[0] + ", port #" + serverData[1]);
           // System.exit(1);
        }
        lcma.canFinish(fromServer);
        return "done";
    }

    protected void onPostExecute(String result)
    {
        System.out.println("THIS IS ON THE POST EXECUTE");
        System.out.println("Result: " + result);
    }
}
