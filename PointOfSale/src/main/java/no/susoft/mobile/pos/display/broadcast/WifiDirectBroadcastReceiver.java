package no.susoft.mobile.pos.display.broadcast;

import java.net.InetAddress;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.widget.Toast;

import no.susoft.mobile.pos.ui.activity.MainActivity;

/*
 * Created by Sami on 10/26/2016.
 */

public class WifiDirectBroadcastReceiver extends BroadcastReceiver{
	public static final int IS_OWNER = 1;
	public static final int IS_CLIENT = 2;
	private static final String TAG = "WifiDirectBroadcastReceiver";
	
	private WifiP2pManager mManager;
	private Channel mChannel;
	private Activity mActivity;
	private int isGroupeOwner;
	private InetAddress ownerAddr;
	
	private static WifiDirectBroadcastReceiver instance;
	
	private WifiDirectBroadcastReceiver(){
		super();
	}
	
	public static WifiDirectBroadcastReceiver createInstance(){
		if(instance == null){
			instance = new WifiDirectBroadcastReceiver();
		}
		return instance;
	}

	public int isGroupeOwner() { return isGroupeOwner; }
	public InetAddress getOwnerAddr() { return ownerAddr; }
	public void setmManager(WifiP2pManager mManager) { this.mManager = mManager; }
	public void setmChannel(Channel mChannel) { this.mChannel = mChannel; }
	public void setmActivity(Activity mActivity) { this.mActivity = mActivity; }

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		
		/**********************************
		 Wifi P2P is enabled or disabled 
		**********************************/
		if(action.equals(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)){ 
			//Log.v(TAG, "WIFI_P2P_STATE_CHANGED_ACTION");
			
			//check if Wifi P2P is supported
			int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
			if(state != WifiP2pManager.WIFI_P2P_STATE_ENABLED){
				Toast.makeText(mActivity, "Wifi P2P is not supported by this device", Toast.LENGTH_SHORT).show();
			}
		}
		
		/**********************************
		 Available peer list has changed
		**********************************/
		else if(action.equals(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)){
		}
		
		/***************************************
		 This device's wifi state has changed 
		***************************************/
		else if(action.equals(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)){
			
		}
		
		/******************************************************************
		 State of connectivity has changed (new connection/disconnection) 
		******************************************************************/
		else if(action.equals(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)){
			
			if(mManager == null){
				return;
			}
			NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
			if(networkInfo.isConnected()){

				mManager.requestConnectionInfo(mChannel, new ConnectionInfoListener() {
					
					@Override
					public void onConnectionInfoAvailable(WifiP2pInfo info) {
						InetAddress groupOwnerAddress = info.groupOwnerAddress;
						ownerAddr= groupOwnerAddress;
						/******************************************************************
						 The GO : create a server thread and accept incoming connections 
						******************************************************************/
						if (info.groupFormed && info.isGroupOwner) { 
							isGroupeOwner = IS_OWNER;
						}
						/******************************************************************
						 The client : create a client thread that connects to the group owner 
						******************************************************************/
						else if (info.groupFormed) { 
							isGroupeOwner = IS_CLIENT;
						}

						if (mActivity.getClass() == MainActivity.class) {
							((MainActivity)mActivity).initServerAndClientThread();
							Toast.makeText(mActivity, "Connected with a display", Toast.LENGTH_SHORT).show();
						}
					}
				});				
			} else {
				Toast.makeText(mActivity, "Disconnected with a display", Toast.LENGTH_SHORT).show();
			}
		}
	}

}