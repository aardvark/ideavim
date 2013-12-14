package com.maddyhome.idea.vim.group;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.maddyhome.idea.vim.ex.LineRange;
import com.maddyhome.idea.vim.regexp.CharPointer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by aardvark on 12/14/13.
 */
public class SortGroup extends AbstractActionGroup {
  public static final int REVERSE = 1;
  public static final int IGNORE_CASE = 2;
  public static final int SORT_DECIMAL = 4;
  public static final int SORT_HEX = 8;
  public static final int SORT_OCTAL = 16;
  public static final int UNIQUE = 32;

  public boolean sort(Editor editor, DataContext context, LineRange range, String command, String argument) {
    int flags = 0;
    boolean res = true;
    CharPointer arg = new CharPointer(new StringBuffer(argument));
    while (!arg.isNul()){
      switch(arg.charAtInc()){
        case '!':
          flags += REVERSE;
          break;
        case 'i':
          flags += IGNORE_CASE;
          break;
        case 'u':
          flags += UNIQUE;
          break;
        default:
          return false;
      }
    }
    String[] text = editor.getDocument().getText().split("\n");
    List<String> textCol = new ArrayList<>(Arrays.asList(text));
    String outText = "";
    if (isIgnoreCase(flags)){
      Collections.sort(textCol, String.CASE_INSENSITIVE_ORDER);
    } else {
      Collections.sort(textCol);
    }

    if (isReverse(flags)){
      Collections.reverse(textCol);
    }

    for (String s : textCol) {
      outText += s + "\n";
    }

    editor.getDocument().setText(outText);
    return res;
  }

  private boolean isIgnoreCase(int flags) {
    return ((flags & IGNORE_CASE) == IGNORE_CASE);
  }

  private boolean isReverse(int flags){
    return ((flags & REVERSE) == REVERSE);
  }
}
