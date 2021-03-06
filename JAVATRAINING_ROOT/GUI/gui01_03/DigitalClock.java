/*
 * 課題1-3
 * 課題1-2のデジタル時計を、次のように修正してください。
 * ・FrameではなくWindowクラスを使用して、フレーム枠のないデジタル時計にする。
 * ・課題1-2のダイアログで指定できた属性は、マウスの右クリックでポップアップメニューを表示して、
 * 　カスケード形式で選択できるようにする（ダイアログは開かない）。
 * 時計内をマウスの左ボタンでクリックしたまま、デスクトップ上でウィンドウを移動させることができるようにする。
 */

/*
 * 課題1-2
 * デジタル時計に次の機能追加を行ってください。
 * ・メニューを付けて、プロパティダイアログを開ける。
 * ・プロパティダイアログでは、以下の項目を設定できる。
 * 　1. フォントの指定
 * 　2. フォントサイズの指定
 * 　3. 文字色の指定
 * 　4. 時計の背景色の指定
 * ・描画に際して、ちらつきをなくすようにダブルバッファリングする。
 * ・フォントとフォントサイズを変更すると、時計を表示すべきフレームの大きさを適切に自動変更して、正しく表示されるようにする。
 */

/*
 * 課題1-1
 * AWTのFrameを使用して、時間を表示するデジタル時計（アナログ時計は不可）を作成してください。
 * ・java.awt.Frameを使用する。
 * ・Windowsの普通のアプリケーションと同様にタイトルバーの「×」ボタンをクリックすると終了する。
 * ・デジタル時計の描画は、paintメソッド内でGraphicsを使用して行う。テキストラベルによる単なる表示は、不可。
 */

package gui01_03;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DigitalClock extends Window implements Runnable, ActionListener
{
    private static final long serialVersionUID = 1L;

    private Date currentDate;
    private Date capturedDate;
    private SimpleDateFormat simpleDataFormat = new SimpleDateFormat("HH:mm:ss");

    private String stringCurrentTime = " Current: --:--:--";
    private String stringErapsedTime = " Erapsed: --:--:--";
    private String stringCapturedTime = "Captured: --:--:--";

    private Thread th;
    private Image imageBuffer;
    private Graphics graphicBuffer;

    private String fontType = "Broadway BT";
    private int fontStyle = Font.PLAIN;
    private Integer fontSize = 60;
    private Color fontColor = Color.black;
    private Color backgroundColor = Color.white;

    private int windowSizeX = 60 * 8 + 50;
    private int windowSizeY = 60 + 50;

    private boolean captureFlag = false;

    public PopupMenu popup = new PopupMenu();

    private Menu property = new Menu("Property");
    private MenuItem menuItemExit = new MenuItem("Exit");
    private MenuItem menuItemCapture = new MenuItem("Capture!");

    private Menu menuFontType = new Menu("Font Type");
    private Menu menuFontStyle = new Menu("Font Style");
    private Menu menuFontSize = new Menu("Font Size");
    private Menu menuFontColor = new Menu("Font Color");
    private Menu menuBackgroundColor = new Menu("BackgroundColor");

    static private String fonts[] = GraphicsEnvironment
            .getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

    static private String stringFontStyle[] =
    { "Plain", "Bold", "Italic", "Bold and Italic" };
    static private int intFontStyle[] =
    { Font.PLAIN, Font.BOLD, Font.ITALIC, Font.BOLD | Font.ITALIC };

    static private String stringColor[] =
    { "Black", "White", "Red", "Green", "Blue", "Cyan", "Magenta", "Yellow",
            "Orange" };
    static private Color colorColor[] =
    { Color.black, Color.white, Color.red, Color.green, Color.blue, Color.cyan,
            Color.magenta, Color.yellow, Color.orange };

    private RightClickMenu mouse;

    // フォントのデフォルトの設定
    private Font fontSetting = new Font(fontType, fontStyle, fontSize);

    // コンストラクタ
    public DigitalClock()
    {
        super(null);

        // 時刻用変数の初期化
        currentDate = new Date();
        capturedDate = null;

        property.add(menuFontType);
        {
            for (int i = 0; i < fonts.length; i++)
            {
                menuFontType.add(fonts[i]);
            }
        }
        property.add(menuFontStyle);
        {
            for (int i = 0; i < stringFontStyle.length; i++)
            {
                menuFontStyle.add(stringFontStyle[i]);
            }
        }
        property.add(menuFontSize);
        {
            for (Integer i = 10; i < 300; i += 30)
            {
                menuFontSize.add(i.toString());
            }
        }
        property.add(menuFontColor);
        {
            for (int i = 0; i < stringColor.length; i++)
            {
                menuFontColor.add(new MenuItem(stringColor[i]));
            }
        }
        property.add(menuBackgroundColor);
        {
            for (int i = 0; i < stringColor.length; i++)
            {
                menuBackgroundColor.add(new MenuItem(stringColor[i]));
            }
        }
        popup.add(property);
        popup.add(menuItemCapture);
        popup.add(menuItemExit);

        menuFontType.addActionListener(this);
        menuFontStyle.addActionListener(this);
        menuFontSize.addActionListener(this);
        menuFontColor.addActionListener(this);
        menuBackgroundColor.addActionListener(this);
        menuItemCapture.addActionListener(this);
        menuItemExit.addActionListener(this);

        add(popup);

        mouse = new RightClickMenu(this);

        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    public void paint(Graphics g)
    {
        // 現在時刻の更新
        currentDate = new Date();

        // Capture!が押されたらキャプチャ時刻を取得する
        if (true == captureFlag)
        {
            capturedDate = new Date();
            captureFlag = false;
        }

        // 時刻用文字列の生成
        stringCurrentTime = " Current: " + simpleDataFormat.format(currentDate);
        // キャプチャ時刻がnullでなければキャプチャ時刻・経過時間用文字列を生成する
        if (capturedDate != null)
        {
            stringCapturedTime = "Captured: "
                    + simpleDataFormat.format(capturedDate);
            // capturedDateがnullでない場合は、経過時間を取得する
            stringErapsedTime = " Erapsed: "
                    + calculateErapsedTime(capturedDate, currentDate);
        }

        if (null != graphicBuffer)
        {
            // ウィンドウサイズの計算
            windowSizeX = Math.max(
                    graphicBuffer.getFontMetrics().stringWidth(
                            stringCurrentTime), Math.max(
                            graphicBuffer.getFontMetrics().stringWidth(
                                    stringCapturedTime),
                            graphicBuffer.getFontMetrics().stringWidth(
                                    stringErapsedTime)));

            windowSizeX += getInsets().left;
            windowSizeX += getInsets().right;

            windowSizeY = graphicBuffer.getFontMetrics().getAscent();
            windowSizeY += graphicBuffer.getFontMetrics().getDescent();
            windowSizeY += graphicBuffer.getFontMetrics().getLeading();
            windowSizeY *= 3; // 経過時間、キャプチャした時刻用
            windowSizeY += getInsets().top;
        }
        setSize(windowSizeX, windowSizeY);

        imageBuffer = createImage(windowSizeX, windowSizeY);
        graphicBuffer = imageBuffer.getGraphics();

        // 背景を色つきで塗りつぶす
        graphicBuffer.setColor(backgroundColor);
        graphicBuffer.fillRect(0, 0, windowSizeX, windowSizeY);

        // 時刻の描画
        fontSetting = new Font(fontType, fontStyle, fontSize);
        graphicBuffer.setFont(fontSetting); // フォントの設定
        graphicBuffer.setColor(fontColor); // 文字色の設定

        graphicBuffer.drawString(stringCurrentTime, 0, graphicBuffer
                .getFontMetrics().getAscent()
                + getInsets().top
                - getInsets().bottom);

        // 経過時間の描画
        graphicBuffer.drawString(stringErapsedTime, 0,
                (graphicBuffer.getFontMetrics().getAscent()) * 2
                        + getInsets().top - getInsets().bottom);

        // キャプチャした時刻の描画
        graphicBuffer.drawString(stringCapturedTime, 0,
                (graphicBuffer.getFontMetrics().getAscent()) * 3
                        + getInsets().top - getInsets().bottom);

        // バッファのコピー
        g.drawImage(imageBuffer, 0, 0, this);
    }

    public int getFontStyle()
    {
        return fontStyle;
    }

    public void setFontStyle(int fontStyle)
    {
        this.fontStyle = fontStyle;
    }

    @Override
    public void update(Graphics g)
    {
        // ちらつき防止のため、updateメソッドからそのままpaintメソッドにつなぐ
        // (画面がクリアされないようにする)
        paint(g);
    }

    @Override
    public void run()
    {
        while (true)
        {
            // 再描画
            repaint();

            try
            {
                Thread.sleep(10); // スリープ1秒
            }
            catch (InterruptedException e)
            {
                ; // 何もしない
            }
        }

    }

    public String getFontType()
    {
        return fontType;
    }

    public void setFontType(String fontType)
    {
        this.fontType = fontType;
    }

    public Integer getFontSize()
    {
        return fontSize;
    }

    public void setFontSize(int fontSize)
    {
        this.fontSize = fontSize;
    }

    public Color getFontColor()
    {
        return fontColor;
    }

    public void setFontColor(Color fontColor)
    {
        this.fontColor = fontColor;
    }

    public Color getBackgroundColor()
    {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor)
    {
        this.backgroundColor = backgroundColor;
    }

    public Font getFontSetting()
    {
        return fontSetting;
    }

    public void setFontSetting(Font fontSetting)
    {
        this.fontSetting = fontSetting;
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        DigitalClock window = new DigitalClock();

        window.th = new Thread(window);

        window.setSize(220, 150);
        window.setLocation(100, 100);
        window.setVisible(true);

        window.imageBuffer = window.createImage(220, 150);
        window.graphicBuffer = window.imageBuffer.getGraphics();
        window.setAlwaysOnTop(true);

        window.th.start(); // スレッドスタート

    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (menuFontType == e.getSource())
        {
            setFontType(e.getActionCommand());
        }
        else if (menuFontStyle == e.getSource())
        {
            for (int i = 0; i < stringFontStyle.length; i++)
            {
                if (e.getActionCommand() == stringFontStyle[i])
                {
                    setFontStyle(intFontStyle[i]);
                }
            }
        }
        else if (menuFontSize == e.getSource())
        {
            setFontSize(Integer.valueOf(e.getActionCommand()));
        }
        else if (menuFontColor == e.getSource())
        {
            setFontColor(changeStringToColor(e.getActionCommand()));
        }
        else if (menuBackgroundColor == e.getSource())
        {
            setBackgroundColor(changeStringToColor(e.getActionCommand()));
        }
        else if ("Capture!" == e.getActionCommand())
        {
            captureFlag = true;
        }
        else if ("Exit" == e.getActionCommand())
        {
            System.exit(0);
        }
        else
        {
            System.out.println(e.getSource());
            System.out.println("This action is not implemented!");
        }
    }

    public Color changeStringToColor(String colorString)
    {
        for (int i = 0; i < stringColor.length; i++)
        {
            if (colorString == stringColor[i])
            {
                return colorColor[i];
            }
        }
        return Color.black;
    }

    public void setWindowLocation(int x, int y)
    {
        setLocation(x, y);
    }

    public String calculateErapsedTime(Date start, Date end)
    {
        Long diffAsSecond = (end.getTime() - start.getTime()) / 1000;
        Long diffHour = diffAsSecond / (60 * 60);
        Long diffMinute = (diffAsSecond - diffHour * 60 * 60) / 60;
        Long diffSecond = (diffAsSecond - diffHour * 60 * 60 - diffMinute * 60);

        String stringHour;
        String stringMinute;
        String stringSecond;

        if (diffHour < 10)
        {
            stringHour = "0" + diffHour;
        }
        else if (diffHour >= 100)
        {
            stringHour = "00";
        }
        else
        {
            stringHour = diffHour.toString();
        }

        if (diffMinute < 10)
        {
            stringMinute = "0" + diffMinute;
        }
        else
        {
            stringMinute = diffMinute.toString();
        }

        if (diffSecond < 10)
        {
            stringSecond = "0" + diffSecond;
        }
        else
        {
            stringSecond = diffSecond.toString();
        }

        return stringHour + ":" + stringMinute + ":" + stringSecond;
    }
}
