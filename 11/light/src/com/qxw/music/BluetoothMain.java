package com.qxw.music;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
//import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class BluetoothMain extends Activity {
	static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
	
	Button btnSearch, btnDis, btnExit;
	ToggleButton tbtnSwitch;
	ListView lvBTDevices;
	ArrayAdapter<String> adtDevices;
	List<String> lstDevices = new ArrayList<String>();
	
	BluetoothAdapter btAdapt;
	public static BluetoothSocket btSocket;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bluetooth);
		// Button ����
		btnSearch = (Button) this.findViewById(R.id.btnSearch);
		btnSearch.setOnClickListener(new ClickEvent());
		btnExit = (Button) this.findViewById(R.id.btnExit);
		btnExit.setOnClickListener(new ClickEvent());
		btnDis = (Button) this.findViewById(R.id.btnDis);
		btnDis.setOnClickListener(new ClickEvent());

		// ToogleButton����
		tbtnSwitch = (ToggleButton) this.findViewById(R.id.tbtnSwitch);
		tbtnSwitch.setOnClickListener(new ClickEvent());

		// ListView��������Դ ������
		lvBTDevices = (ListView) this.findViewById(R.id.lvDevices);
		adtDevices = new ArrayAdapter<String>(BluetoothMain.this,
				android.R.layout.simple_list_item_1, lstDevices);
		lvBTDevices.setAdapter(adtDevices);
		//lvBTDevices.setOnItemClickListener(new ItemClickEvent());
		
		// ��ʼ��������������                // ��ȡ����״̬����ʾ
		btAdapt = BluetoothAdapter.getDefaultAdapter();
        if (btAdapt.getState() == BluetoothAdapter.STATE_OFF)
			tbtnSwitch.setChecked(false);
		else if (btAdapt.getState() == BluetoothAdapter.STATE_ON)
			tbtnSwitch.setChecked(true);

		// ע��Receiver����ȡ�����豸��صĽ��
		IntentFilter intent = new IntentFilter();
		intent.addAction(BluetoothDevice.ACTION_FOUND);// ��BroadcastReceiver��ȡ���������
		intent.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		intent.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
		intent.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		registerReceiver(searchDevices, intent);
		
		if (btAdapt.getState() == BluetoothAdapter.STATE_OFF) {// ���������û����
			Toast.makeText(BluetoothMain.this, "Bluetooth is openning��Just a minute , please", 1000).show();
			btAdapt.enable();
			tbtnSwitch.setChecked(true);
		}
		setTitle("The Bluetooth address��" + btAdapt.getAddress());
		lstDevices.clear();
		btAdapt.startDiscovery();
		
	}
	
	

	private BroadcastReceiver searchDevices = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Bundle b = intent.getExtras();
			Object[] lstName = b.keySet().toArray();

			// ��ʾ�����յ�����Ϣ����ϸ��
			for (int i = 0; i < lstName.length; i++) {
				String keyName = lstName[i].toString();
				Log.e(keyName, String.valueOf(b.get(keyName)));
			}
			
			//�����豸ʱ��ȡ���豸��MAC��ַ
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				String str= device.getName() + "|" + device.getAddress();
				String str1 = device.getAddress();
			//if(str1.equals("00:11:08:01:06:78"))  //�����ж��Ƿ�Ϊ��������Ҫ������
			//{	
				
				if (lstDevices.indexOf(str) == -1)// ��ֹ�ظ�����
					lstDevices.add(str); // ��ȡ�豸���ƺ�mac��ַ
				adtDevices.notifyDataSetChanged();
				/*  �����жϣ����Ϊ����Ի����Ѵ�mac��ַ���豸
				 *  �Զ��������ӣ�����ת����һ��Activity
				 * */
				if(true){   //δ��������
					btAdapt.cancelDiscovery(); 
					//String str = lstDevices.get();
					//String[] values = str.split("\\|");
					//String address=values[1];
					//Log.e("address",values[1]);
					UUID uuid = UUID.fromString(SPP_UUID);
					BluetoothDevice btDev = btAdapt.getRemoteDevice(device.getAddress());
					try {
						btSocket = btDev
								.createRfcommSocketToServiceRecord(uuid);
						btSocket.connect();
						
						Intent intent1 = new Intent();
						intent1.setClass(BluetoothMain.this, Main.class);
						startActivity(intent1);
						overridePendingTransition(R.anim.zoomin,R.anim.zoomout);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			    }
				
				
			//}
		
			
			
			
			}
		}
	};

	@Override
	protected void onDestroy() {
	    this.unregisterReceiver(searchDevices);
		super.onDestroy();
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	/*class ItemClickEvent implements AdapterView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			btAdapt.cancelDiscovery(); 
			String str = lstDevices.get(arg2);
			String[] values = str.split("\\|");
			String address=values[1];
			Log.e("address",values[1]);
			UUID uuid = UUID.fromString(SPP_UUID);
			BluetoothDevice btDev = btAdapt.getRemoteDevice(address);
			try {
				btSocket = btDev
						.createRfcommSocketToServiceRecord(uuid);
				btSocket.connect();
				
				Intent intent = new Intent();
				intent.setClass(BluetoothMain.this, KEY.class);
				startActivity(intent);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}*/
	
	
	class ClickEvent implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			if (v == btnSearch)// ���������豸����BroadcastReceiver��ʾ���
			{
				
				if (btAdapt.getState() == BluetoothAdapter.STATE_OFF) {// ���������û����
					Toast.makeText(BluetoothMain.this, "���ȴ�����", 1000).show();
					return;
				}
				
				setTitle("����������ַ��" + btAdapt.getAddress());
				lstDevices.clear();
				btAdapt.startDiscovery();
			} else if (v == tbtnSwitch) {// ������������/�ر�
				if (btAdapt.getState() == BluetoothAdapter.STATE_OFF){
					btAdapt.enable();
					tbtnSwitch.setChecked(true);
				}
				
				else if (btAdapt.getState() == BluetoothAdapter.STATE_ON){
					btAdapt.disable();
					tbtnSwitch.setChecked(false);
				}
			} else if (v == btnDis)// �������Ա�����
			{
				Intent discoverableIntent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
				discoverableIntent.putExtra(
						BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
				startActivity(discoverableIntent);
			} else if (v == btnExit) {
				try {
					if (btSocket != null)
						btSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
     		 if (btAdapt.getState() == BluetoothAdapter.STATE_ON){
					btAdapt.disable();
     		 }
				BluetoothMain.this.finish();
			}
		}

	}
	
	/*Handler handler  = new Handler();
	Runnable runnable =  new Runnable(){

		  @Override
		  public void run() {
			  searchDevices.onReceive(getBaseContext(), intent1);
			  }
	} ; */
	
}