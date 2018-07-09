import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Verify {

	// 验证码属性
	protected static String codeSet = "2345678abcdefhijkmnpqrstuvwxyzABCDEFGHJKLMNPQRTUVWXY";// 验证码字符集合
	protected static String fontType = "Fixedsys"; // 验证码使用的字体
	protected static int fontSize = 30; // 验证码字体大小
	protected static int imageH = 49; // 验证码图片高度
	protected static int imageW = 128; // 验证码图片宽度
	protected int[] bg = null; // 背景颜色
	protected static Color fontColor = null; // 验证码字体颜色

	// 验证码的实例对象
	private static BufferedImage image = null; // 验证码图片实例
	private static Font font = null; // 验证码字体实例
	private static Graphics2D g2 = null; // 验证码绘图对象

	// 干扰设置
	protected static boolean useCurve = true; // 是否画混淆曲线
	protected static boolean useNoise = true; // 是否添加杂点
	protected static int CurveAberration = 30; // 混淆曲线与验证码的的颜色偏差，0表示不偏差

	// 英文验证码
	private static String[] fontTypeEn = { "3x5", "13_Misa", "AddCityboy", "Airbus Special", "Yahoo", "Stencil Four" }; // 随机字体数组

	// 中文验证码
	protected static boolean useZh = false; // 使用中文验证码
	protected static String zhSet = ""; // 中文验证码字体类型
	private static String[] fontTypeZh = { "宋体", "楷体", "黑体", "幼圆" }; // 内置字体类型

	/**
	 * 生成验证码
	 *
	 * @access public
	 * @param String
	 *            str 要生成验证码的标识
	 * @throws IOException
	 */
	public void entry(String str) throws IOException {
		File file = new File("temp.jpg");

		// 建立一幅 this.imageW x this.imageH 的图像
		image = new BufferedImage(imageW, imageH, BufferedImage.TYPE_INT_RGB);

		// 随机背景色
		if (bg == null) {
			bg = new int[3];
			bg[0] = random(15, 240);
			bg[1] = random(15, 240);
			bg[2] = random(15, 240);
		}

		// 设置背景
		g2 = (Graphics2D) image.getGraphics();
		g2.setBackground(new Color(bg[0], bg[1], bg[2]));
		g2.clearRect(0, 0, imageW, imageH);

		// 验证码随机字体颜色
		if (fontColor == null) {
			fontColor = new Color(random(150, 50), random(150, 50), random(150, 50));
		}

		// 绘制干扰字符
		if (useNoise) {
			// 绘杂点
			_writeNoise();
		}

		// 随机字体
		if ("".equals(fontType)) {
			fontType = fontTypeEn[random(fontTypeEn.length, 0)];
		}
		// 中文验证码随机字体
		if (!useZh && "".equals(zhSet)) {
			fontType = fontTypeZh[random(fontTypeZh.length, 0)];
		}

		// 设置字体
		font = new Font(fontType, Font.BOLD, fontSize);
		g2.setFont(font);

		// 设置验证码字体颜色
		g2.setPaint(fontColor);

		// 将验证码写入图片
		_writeString(str);

		// 绘制干扰线
		if (useCurve) {
			_writeCurve();
		}
		ImageIO.write(image, "jpg", file);
	}

	/**
	 * 画一条由两条连在一起构成的随机正弦函数曲线作干扰线(你可以改成更帅的曲线函数)
	 *
	 * 高中的数学公式咋都忘了涅，写出来 正弦型函数解析式： y=Asin(ωx+φ)+b 各常数值对函数图像的影响：
	 * A：决定峰值（即纵向拉伸压缩的倍数） b：表示波形在Y轴的位置关系或纵向移动距离（上加下减） φ：决定波形与X轴位置关系或横向移动距离（左加右减）
	 * ω：决定周期（最小正周期T=2π/∣ω∣）
	 *
	 */
	private static void _writeCurve() {
		int px = 0, py = 0;

		// 设置干扰曲线颜色与验证码相近的颜色
		g2.setPaint(new Color(fontColor.getRed() + CurveAberration, fontColor.getGreen() + CurveAberration,
				fontColor.getBlue() + CurveAberration));

		// 曲线前部分
		double A = imageH / 3; // 振幅
		double b = 0; // Y轴方向偏移量

		double f = Math.random() * imageH / 2 - imageH / 4; // X轴方向偏移量
		double T = (Math.random() * 0.5 + 0.4) * imageW * 1.5; // 周期
		double w = 2 * Math.PI / T;

		int px1 = 0; // 曲线横坐标起始位置
		int px2 = (int) (Math.random() * imageW * 1.3 - imageW * 0.5); // 曲线横坐标结束位置

		int _px; // 偏移后的x轴坐标
		int _py; // 偏移后的y轴坐标

		for (px = px1; px <= px2; px++) {
			if (w != 0) {
				py = (int) (A * Math.sin(w * px + f) + b + imageH / 2); // y=Asin(ωx+φ)+b
				int i = (int) (fontSize / 5);
				while (i > 0) {
					_px = px + i;
					_py = py + i;
					g2.drawLine(_px, _py, _px, _py); // 这里(while)循环画像素点用字体大小一次画出（不用这while循环）性能要好很多
					i--;
				}
			}
		}

		// 曲线后部分
		f = Math.random() * imageH / 2 - imageH / 4; // X轴方向偏移量
		T = (Math.random() * 0.8 + 0.3) * imageW; // 周期
		w = 2 * Math.PI / T;
		px1 = px2;
		px2 = imageW;

		for (px = px1; px <= px2; px++) {
			if (w != 0) {
				py = (int) (A * Math.sin(w * px + f) + b + imageH / 2); // y=Asin(ωx+φ)+b
				int i = (int) (fontSize / 5);
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
	 * 画杂点 往图片上写不同颜色的字母或数字
	 */
	private static void _writeNoise() {
		for (int i = 0; i < 3; i++) {
			g2.setFont(new Font(fontType, Font.BOLD, (int) (fontSize * 0.7)));
			// 杂点颜色
			Color noiseColor = new Color(random(75, 150), random(75, 150), random(75, 150));
			char[] codeSetChr = codeSet.toCharArray();
			for (int j = 0; j < 6; j++) {
				// 绘杂点
				g2.setPaint(noiseColor);
				g2.drawChars(codeSetChr, random(codeSet.length(), 0), 1, random(imageW, 10), random(imageH, 10));
			}
		}
	}

	/**
	 * 写入验证码 往图片上随机位置、随机大小、随机旋转的写入验证码字符串
	 *
	 * @param str
	 *            写入图片的验证码字符串
	 */
	private static void _writeString(String str) {
		int x = (int) (imageW / str.length());
		int y = imageH - fontSize;
		int baseX = 0;
		int rFontSize = 0;
		char[] strArr = str.toCharArray();

		int move_x = 0; // 平移横向坐标的距离
		int move_y = 0; // 平移纵向坐标的距离
		double radians = 0; // 旋转的角度

		for (int i = 0; i < str.length(); i++) {
			rFontSize = (int) (fontSize + Math.random() * 6 - 3);
			font = new Font("", Font.BOLD, rFontSize);
			g2.setFont(font);

			move_x = random(x - rFontSize, baseX);
			move_y = random(y, fontSize);
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
	 * 生成整型随机数
	 *
	 * @param int
	 *            a 范围（0-a）
	 * @param int
	 *            b 范围偏移量
	 * @return int 整型随机数
	 */
	private static int random(int a, int b) {
		return (int) (Math.random() * a + b);
	}
}
