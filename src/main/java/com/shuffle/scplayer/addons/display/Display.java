package com.shuffle.scplayer.addons.display;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.pi4j.component.lcd.impl.GpioLcdDisplay;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.system.SystemInfo;
import com.shuffle.scplayer.core.SpotifyConnectPlayerImpl;
import com.shuffle.scplayer.core.Track;

public class Display implements Runnable {

	private static final transient Log log = LogFactory.getLog(SpotifyConnectPlayerImpl.class);

	private static int LCD_ROWS = 2;
	private static int LCD_COLUMNS = 16;
	private static int LCD_ROW_1 = 0;
	private static int LCD_ROW_2 = 1;
	
	private static GpioController gpio;
	private static GpioLcdDisplay lcd;
	private static GpioPinDigitalOutput backlight;

	private int overlayTTL = 0;
	private String overlayText1 = "";
	private String overlayText2 = "";
	private Track currentTrack;
	private int scrollPos = 0;
	private boolean active = false;
	private boolean playing = false;
	
	Thread thread;
	Display()
	{
		// create gpio controller
		gpio = GpioFactory.getInstance();

		// provision gpio pin 27 as an output pin and turn on
		backlight = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02, PinState.HIGH);
		backlight.setShutdownOptions(true, PinState.LOW);

		// initialize LCD
		lcd = new GpioLcdDisplay(
			LCD_ROWS,          // number of row supported by LCD
			LCD_COLUMNS,       // number of columns supported by LCD
			RaspiPin.GPIO_11,  // LCD RS pin
			RaspiPin.GPIO_10,  // LCD strobe pin
			RaspiPin.GPIO_06,  // LCD data bit 1
			RaspiPin.GPIO_05,  // LCD data bit 2
			RaspiPin.GPIO_04,  // LCD data bit 3
			RaspiPin.GPIO_00   // LCD data bit 4
		);
		
		// clear LCD
		lcd.clear();
		
		// start this display as its own thread
		thread = new Thread(this, "Display thread");
		thread.start();
	}

	@Override
	public void run() {
		try
		{
			while (!Thread.interrupted())
			{
				if(scrollPos == 1) Thread.sleep(800);
				showOverlay();
				showTrackInfo();
				showStatus();
				Thread.yield();
				Thread.sleep(200);
			}
		}
		catch(Exception e)
		{
			log.error("Display thread interrupted", e);
		}

		// destroy gpio object
        gpio.shutdown();
	}
	
	public void setActive(boolean active) {
		this.active = active;
	}

	public void setPlaying(boolean playing) {
		this.playing = playing;
	}

	public void setBacklight(boolean b) {
		backlight.setState(PinState.HIGH);
	}

	public void disableBacklight() {
		backlight.setState(PinState.LOW);
	}
	
	public void setTrack(Track track) {
		scrollPos = 0;
		currentTrack = track;		
	}
	
	private void showOverlay() {
		if(overlayTTL <= 0) return;
		overlayTTL--;
		lcd.write(LCD_ROW_1, formatRow(overlayText1));
		lcd.write(LCD_ROW_2, formatRow(overlayText2));		
	}
	
	private void showTrackInfo() {
		if(overlayTTL > 0) return;
		if(currentTrack != null) {
			showTitle();
			showArtist();
		}
	}
	
	private void showStatus() {
		if(active && playing) lcd.write(LCD_ROW_2, 15, (byte)0x0);
		else if(active && !playing) lcd.write(LCD_ROW_2, 15, (byte)0x1);
		else lcd.write(LCD_ROW_2, 15, (byte)0x2);
	}
	
	private void showTitle()
	{
    	if(currentTrack.getName() != null && !currentTrack.getName().isEmpty()) {
    		String title = currentTrack.getName() + " - " + currentTrack.getAlbum();
    		lcd.write(LCD_ROW_1, formatRow(title, scrollPos));
    		if(scrollPos < title.length() && title.length() > 16) scrollPos++;
    		else scrollPos = 0;
    	}
	}

	private void showArtist() {
		String artist = currentTrack.getArtist();
		if(artist.length() > 14) {
			lcd.write(LCD_ROW_2, formatRow(artist, 0, 14));
			lcd.write(LCD_ROW_2, 14, (byte)0x3);
		} else {
			lcd.write(LCD_ROW_2, formatRow(artist));
		}
	}

	public void showVolume(short level) {
		int tmp = level < 0 ? level + 65536 : level;
		int percentage = tmp * 100 / 65535;
		float temp = getCpuTemperature();
		overlayTTL = 20;
		overlayText1 = "Volume: " + percentage + "%";
		overlayText2 = "   Cpu: " + temp + "C";
	}
	
	private String formatRow(String text)
	{
		return formatRow(text, 0, 16);
	}
	
	private String formatRow(String text, int startPos)
	{
		return formatRow(text, startPos, 16);
	}
	
	private String formatRow(String text, int startPos, int maxLength)
	{
		int endPos = Math.min(text.length(), startPos + maxLength);
		text = text.substring(startPos, endPos);
		text = String.format("%-" + maxLength + "s", text);
		return text;
	}
	
	private float getCpuTemperature() {
		try {
			return SystemInfo.getCpuTemperature();
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		return 0;
	}
}
