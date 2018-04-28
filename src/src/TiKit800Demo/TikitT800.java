package TiKit800Demo;

import java.io.IOException;

import tijos.framework.devicecenter.TiGPIO;
import tijos.framework.devicecenter.TiI2CMaster;
import tijos.framework.platform.peripheral.ITiKeyboardListener;
import tijos.framework.platform.peripheral.TiKeyboard;
import tijos.framework.sensor.button.ITiButtonEventListener;
import tijos.framework.sensor.button.TiButton;
import tijos.framework.sensor.dht.TiDHT;
import tijos.framework.transducer.led.TiLED;
import tijos.framework.transducer.oled.TiOLED_UG2864;
import tijos.framework.transducer.relay.TiRelay1CH;

public class TikitT800 {

	TiGPIO gpio2 = TiGPIO.open(2, 1, 3);
	TiGPIO gpio3 = TiGPIO.open(3, 6, 7);
	TiI2CMaster i2cm0 = TiI2CMaster.open(0);
	
	TiRelay1CH relay = new TiRelay1CH(gpio2, 1);
	TiDHT dht11 = new TiDHT(gpio2, 3);
	TiLED led = new TiLED(gpio3, 6);
	TiButton touch = new TiButton(gpio3, 7, true);
	TiOLED_UG2864 oled = new TiOLED_UG2864(i2cm0, 0x3c);
	
	long lastMeasureTime = 0; 
	
	double temperature = 0;
	double humidity = 0;
	
	public TikitT800() throws IOException {
		
		
	}
	
	public void oledPrint(int x,int y, String msg) throws IOException{
		oled.print(y, x, msg);
	}
	
	public void setButtonEventListener(ITiButtonEventListener listner) throws IOException {
		touch.setEventListener(listner);
	}
	
	public void setKeyboardEventListener(ITiKeyboardListener listener) throws IOException {
		TiKeyboard.getInstance().setEventListener(listener);
	}
	
	public void initialize() throws IOException{
		oled.turnOn();
		oled.clear();
		
		oled.print(0, 0, "TiKit-T800 Demo");
	}
	
	public void turnRelay(boolean state) throws IOException {
		if(state)
			this.relay.turnOn();
		else
			this.relay.turnOff();
	}
	
	public void turnLED(boolean state) throws IOException {
		if(state)
			this.led.turnOn();
		else
			this.led.turnOff();
	}
	
	public void measure() throws IOException {
		//at least 3 seconds
		if(( System.currentTimeMillis() - lastMeasureTime) > 3000){
			lastMeasureTime =  System.currentTimeMillis();
			dht11.measure();
			
			temperature = dht11.getTemperature();
			humidity = dht11.getHumidity();
			
			oled.print(1, 0, "Temp:" + temperature + " C");
			oled.print(2, 0, "Humi:" + humidity + " RH");
		}
	}
	
	public double getTemperature() {
		return this.temperature;
	}
	
	public double getHumidity() {
		return this.humidity;
	}
	
}
