import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Captcha {

	// CAPTCHA settings
	String char_set = "2345678abcdefhijkmnpqrstuvwxyzABCDEFGHJKLMNPQRTUVWXY";	// Character setting
	String font_family = "Fixedsys";	// Font family
	int font_size = 30;					// Font size
	Color font_color = null;			// Font color
	Color bg_color = null;				// Background color
	int img_width = 128;				// Image width
	int img_height = 49;				// Image height

	// Interference settings
	boolean enable_noise = true;		// Enable noise
	boolean enable_curve = true;		// Enable confusion curve
	int curve_aberration = 30;			// Color deviation of the confusion curve from the CAPTCHA, 0 is no deviation

	// English CAPTCHA
	String[] font_family_en = { "3x5", "13_Misa", "AddCityboy", "Airbus Special", "Yahoo", "Stencil Four" }; // Built-in font family

	// Chinese CAPTCHA
	boolean enable_zh = false;			// Enable Chinese CAPTCHA
	String font_family_zh_set = "";		// Chinese Font Family
	String[] font_family_zh = { "宋体", "楷体", "黑体", "幼圆" };	// Built-in font family

	/**
	 * Generate CAPTCHA
	 *
	 * @param String str	CAPTCHA
	 * @throws IOException
	 */
	public void generate(String str) throws IOException {
		var file = new File("temp.jpg");

		// Create an image
		BufferedImage image = new BufferedImage(img_width, img_height, BufferedImage.TYPE_INT_RGB);

		// Random background color
		if (bg_color == null) bg_color = new Color(random(15, 240), random(15, 240), random(15, 240));

		// Set CAPTCHA image background
		var	g2 = (Graphics2D) image.getGraphics();
		g2.setBackground(bg_color);
		g2.clearRect(0, 0, img_width, img_height);

		// Random font color
		if (font_color == null) font_color = new Color(random(150, 50), random(150, 50), random(150, 50));

		// Drawing interfering characters
		if (enable_noise) drawNoise(g2);

		// Randomized fonts
		if ("".equals(font_family)) font_family = font_family_en[random(font_family_en.length, 0)];
		
		// Randomized fonts for Chinese CAPTCHA
		if (!enable_zh && "".equals(font_family_zh_set)) font_family = font_family_zh[random(font_family_zh.length, 0)];

		// Setting the font family
		Font font = new Font(font_family, Font.BOLD, font_size);
		g2.setFont(font);

		// Set CAPTCHA font color
		g2.setPaint(font_color);

		// Drawing CAPTCHA into an image
		drawString(g2, str);

		// Drawing interference line
		if (enable_curve) drawCurve(g2);
		
		ImageIO.write(image, "jpg", file);
	}

	/**
	 * Draw a random sinusoidal function consisting of two connected curves for the interference line
	 *
	 * Analytic equation of a sine-type function: y=Asin(ωx+φ)+b The effect of each constant value on the image of the function
	 * A：The peak value, which is the multiple of longitudinal stretching and compression
	 * b：The distance the waveform moves on the y-axis
	 * φ：The distance the waveform moves on the x-axis
	 * ω：Period, the minimum positive period is T=2π/∣ω∣
	 */
	private void drawCurve(Graphics2D g2) {
		var px = 0;
		var py = 0;

		// Set the color of the curve to a color similar to the CAPTCHA
		g2.setPaint(new Color(
			font_color.getRed() + curve_aberration,
			font_color.getGreen() + curve_aberration,
			font_color.getBlue() + curve_aberration
		));

		// The first half of the curve
		var A = img_height / 3d;	// Amplitude of vibration
		var b = 0d;					// Y-axis offset

		var f = Math.random() * img_height / 2d - img_height / 4d;		// X-axis offset
		var T = (Math.random() * 0.5 + 0.4) * img_width * 1.5;			// Period
		var w = 2d * Math.PI / T;

		var px1 = 0;	// Starting point of the horizontal coordinate of the curve
		var px2 = (int) (Math.random() * img_width * 1.3 - img_width * 0.5);	// The end point of the horizontal coordinate of the curve

		int _px;		// Offset x-axis coordinate
		int _py;		// Offset y-axis coordinate

		for (px = px1; px <= px2; px++) {
			if (w != 0) {
				py = (int) (A * Math.sin(w * px + f) + b + img_height / 2);	// y=Asin(ωx+φ)+b
				var i = (int) (font_size / 5);
				while (i > 0) {
					_px = px + i;
					_py = py + i;
					g2.drawLine(_px, _py, _px, _py);
					i--;
				}
			}
		}

		// The second half of the curve
		f = Math.random() * img_height / 2 - img_height / 4;	// X-axis offset
		T = (Math.random() * 0.8 + 0.3) * img_width;			// Period
		w = 2 * Math.PI / T;
		px1 = px2;
		px2 = img_width;

		for (px = px1; px <= px2; px++) {
			if (w != 0) {
				py = (int) (A * Math.sin(w * px + f) + b + img_height / 2);	// y=Asin(ωx+φ)+b
				var i = (int) (font_size / 5);
				while (i > 0) {
					_px = px + i;
					_py = py + i;
					g2.drawLine(_px, _py, _px, _py);
					i--;
				}
			}
		}
	}

	/**
	 * Draw noise and write different color letters or numbers on the captcha image
	 */
	private void drawNoise(Graphics2D g2) {
		for (var i = 0; i < 3; i++) {
			g2.setFont(new Font(font_family, Font.BOLD, (int) (font_size * 0.7)));
			// Noise Color
			var noiseColor = new Color(random(75, 150), random(75, 150), random(75, 150));
			var char_setChr = char_set.toCharArray();
			for (var j = 0; j < 6; j++) {
				// Draw Noise
				g2.setPaint(noiseColor);
				g2.drawChars(char_setChr, 
					random(char_set.length(), 0), 1, 
					random(img_width, 10), 
					random(img_height, 10)
				);
			}
		}
	}

	/**
	 * Write a randomly positioned, randomly sized, randomly rotated string to the CAPTCHA image
	 * 
	 * @param str	CAPTCHA that writes a string to an image
	 */
	private void drawString(Graphics2D g2, String str) {
		var x = (int) (img_width / str.length());
		var y = img_height - font_size;
		var baseX = 0;
		var rfont_size = 0;
		var strArr = str.toCharArray();

		var move_x = 0;		// Translate the distance in horizontal coordinate
		var move_y = 0;		// Translate the distance in vertical coordinate
		var radians = 0d;	// Angle of rotation

		for (var i = 0; i < str.length(); i++) {
			rfont_size = (int) (font_size + Math.random() * 6 - 3);
			var font = new Font("", Font.BOLD, rfont_size);
			g2.setFont(font);

			move_x = random(x - rfont_size, baseX);
			move_y = random(y, font_size);
			radians = Math.random() * Math.toRadians(60) - Math.toRadians(30);

			g2.translate(move_x, move_y);
			g2.rotate(radians);
			g2.drawChars(strArr, i, 1, 0, 0);

			g2.rotate(-radians);
			g2.translate(-move_x, -move_y);
			baseX += x;
		}
	}

	/**
	 * Generate integer random number
	 * 
	 * @param int	a is the range of values (0-a)
	 * @param int	b is the offset
	 * @return int	Integer random number
	 */
	private int random(int a, int b) {
		return (int) (Math.random() * a + b);
	}
}