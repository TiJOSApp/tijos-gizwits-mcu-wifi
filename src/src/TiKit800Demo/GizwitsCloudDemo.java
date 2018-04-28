package TiKit800Demo;

import java.io.IOException;
import java.util.Date;

import gizwits.device.GizwitsDevice;
import gizwits.device.IGizwitsEventListener;
import gizwits.protocol.DataPoint;
import gizwits.protocol.DataPointManager;
import gizwits.protocol.WiFiMode;
import gizwits.protocol.WifiEventType;
import gizwits.uart.TiUartInputStream;
import gizwits.uart.TiUartOutputStream;
import gizwits.protocol.DataPoint.DataGroup;
import gizwits.protocol.DataPoint.DataType;

import tijos.framework.devicecenter.TiUART;
import tijos.framework.platform.peripheral.ITiKeyboardListener;
import tijos.framework.sensor.button.ITiButtonEventListener;
import tijos.framework.sensor.button.TiButton;
import tijos.framework.util.Delay;
import tijos.framework.util.Formatter;

/**
 * 机智云事件
 * @author TiJOS
 *
 */
class GizwitsEventHandler implements IGizwitsEventListener {

	Object context;

	public GizwitsEventHandler(Object context) {
		this.context = context;
	}

	/**
	 * WIFI 事件
	 */
	@Override
	public void onWifiEvent(WifiEventType eventType, int value) {
		TikitT800 t800 = (TikitT800) context;

		System.out.println("wifi event " + eventType.toString() + " value " + value);
		if (eventType == WifiEventType.WIFI_STATION) {
			try {
				t800.oledPrint(0, 3, "WIFI Connected  ");
			} catch (IOException e) {

				e.printStackTrace();
			}
		}
	}

	/**
	 * 收到机智云透传数据
	 */
	@Override
	public void onTransparentDataReceived(byte[] transparentData, int start, int len) {
	 
		System.out.println("onTransparentDataReceived  len " + len);
	}

	/**
	 * 点控制事件
	 */
	@Override
	public void onPointControl(DataPoint pt, double newValue) {

		System.out.println("Point Control: " + pt.toString() + " new value " + newValue);

		if (context == null) {
			System.out.println("context is null");
			return;
		}

		try {
			TikitT800 t800 = (TikitT800) context;
			if (pt.name.equals("RelayControl")) {
				int val = (int) newValue;
				System.out.println("Turn relay " + val);
				t800.turnRelay(val == 1 ? true : false);
			} else if (pt.name.equals("LEDControl")) {
				int val = (int) newValue;
				System.out.println("Turn LED " + val);
				t800.turnLED(val == 1 ? true : false);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
}

class TouchListener implements ITiButtonEventListener {

	Object context;

	public TouchListener(Object context) {
		this.context = context;
	}

	@Override
	public void onPressed(TiButton button) {

		try {
			TikitT800 t800 = (TikitT800) context;
			t800.oledPrint(0, 3, "touch:onPressed ");
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("touch:onPressed");
	}

	@Override
	public void onReleased(TiButton button) {

		try {
			TikitT800 t800 = (TikitT800) context;
			t800.oledPrint(0, 3, "touch:onReleased");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

class KeyboardListener implements ITiKeyboardListener {

	GizwitsDevice giz;
	TikitT800 t800;

	public KeyboardListener(GizwitsDevice giz, TikitT800 t800) {
		this.giz = giz;
		this.t800 = t800;
	}

	@Override
	public void onPressed(int arg0, long arg1) {

	}

	@Override
	public void onReleased(int arg0, long arg1) {

		try {
			giz.setWifiMode(WiFiMode.AIRLINK);
			t800.oledPrint(0, 3, "Air Link        ");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

public class GizwitsCloudDemo {

	public static void main(String[] args) {
		
		//从机智云产品信息中获得
		String productKey = "e042b9f38c14498bbd8c9bbcead15d2f";
		String productSecret = "ccd1f2d169e9498db0f6878d14e3034b";

		System.out.println("Gizwits Demo Start ...");

		//定义机智云设备
		GizwitsDevice giz = new GizwitsDevice(productKey, productSecret, 4/*数据点个数*/);

		try {
			
			//数据管理器
			DataPointManager dpm = giz.getDPM();

			//根据数据点定义设置每一个数据点的相应属性
			dpm.setDataPoint(0, new DataPoint("RelayControl", DataType.typeBool, DataGroup.readWrite, 1));

			dpm.setDataPoint(1, new DataPoint("Temperature", DataType.typeUint8, DataGroup.readOnly, 1, 0));

			dpm.setDataPoint(2, new DataPoint("Huminity", DataType.typeUint8, DataGroup.readOnly, 1, 0));

			dpm.setDataPoint(3, new DataPoint("LEDControl", DataType.typeBool, DataGroup.readWrite, 1));

			//打开连接机智云模块的UART
			TiUART uart = TiUART.open(2);
			uart.setWorkParameters(8, 1, TiUART.PARITY_NONE, 9600);

			//传感器相关设置
			TikitT800 t800 = new TikitT800();
			t800.initialize();
			t800.setButtonEventListener(new TouchListener(t800));
			t800.setKeyboardEventListener(new KeyboardListener(giz, t800));

			TiUartOutputStream out = new TiUartOutputStream(uart);
			TiUartInputStream in = new TiUartInputStream(uart);

			//当前请求从机智云下发时，相关事件会触发
			giz.setEventListener(new GizwitsEventHandler(t800));
			giz.initialize(in, out);

			Thread.sleep(5000);

			Date time = giz.getNtp();

			if (time != null) {
				System.out.println("ntp time " + time);
				t800.oledPrint(0, 3, time.toString());
			}

			while (true) {
				try {
					Delay.msDelay(1000);

					t800.measure();
						
					/**
					 * 设置相应的点的值 
					 */
					dpm.setPointValue("Temperature", t800.getTemperature());
					dpm.setPointValue("Huminity", t800.getHumidity());

					//如果点的值有变化会上传至机智云
					giz.deviceStatusUpdate();

					System.out.println("Temp " + t800.getTemperature() + " Hum " + t800.getHumidity());

				} catch (Exception e) {
				 
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
