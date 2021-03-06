/*
 * 練習問題 20.5 p.463
 * 指定されたファイルを読み込んで、指定された単語を検索するプログラムを作成しなさい。
 * 単語が発見された全ての行を、行の前に行番号を付けて表示しなさい
 */

package ch20.ex20_05;

import java.io.*;

public class FindWord
{
    public static void main(String[] args) throws IOException
    {
        if (args.length != 2)
        {
            throw new IllegalArgumentException("need word and file");
        }

        String match = args[0];
        FileReader fileIn = new FileReader(args[1]);
        LineNumberReader in = new LineNumberReader(fileIn);
        int ch;
        String line = "";
        int count = 0;
        int previousLine = 0;
        boolean findFlag = false;
        while ((ch = in.read()) != -1)
        {
            if (in.getLineNumber() != previousLine)
            {
                if (findFlag == true)
                {
                    System.out.println(previousLine + ": " + line);
                }
                line = "";
                findFlag = false;
            }
            previousLine = in.getLineNumber();
            if (ch != '\n')
            {
                line = line + String.valueOf((char) ch);
            }
            if (ch == match.charAt(count))
            {
                count++;
            }
            else
            {
                count = 0;
            }
            if (match.length() == count)
            {
                findFlag = true;
                count = 0;
            }
        }
        if (findFlag == true)
        {
            System.out.println(previousLine + ": " + line);
        }
        line = "";
        findFlag = false;

    }

}
