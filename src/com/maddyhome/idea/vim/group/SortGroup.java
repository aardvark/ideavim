package com.maddyhome.idea.vim.group;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.maddyhome.idea.vim.common.TextRange;
import com.maddyhome.idea.vim.ex.LineRange;
import com.maddyhome.idea.vim.ex.Ranges;
import com.maddyhome.idea.vim.helper.EditorHelper;
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

  public boolean sort(Editor editor, DataContext context, LineRange range,
                      TextRange tr, String command, String argument,
                      Ranges ranges) {
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
    String[] text;
    if (isRangeEmpty(ranges)) {
      text = editor.getDocument().getText().split("\n");
    } else {
      text = EditorHelper.getText(editor, tr.getStartOffset(), tr.getEndOffset()).split("\n");
    }

    List<String> textCol = new ArrayList<>(Arrays.asList(text));

    if (isUnique(flags)){
      textCol = Lists.newArrayList(Sets.newHashSet(textCol));
    }

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

    if (isRangeEmpty(ranges)){
      editor.getDocument().setText(outText);
    } else {
      editor.getDocument().replaceString(tr.getStartOffset(), tr.getEndOffset(), outText);
    }

    return res;
  }

  private boolean isRangeEmpty(Ranges ranges) {
    return ranges.size() == 0;
  }

  private boolean isIgnoreCase(int flags) {
    return checkFlagSet(flags, IGNORE_CASE);
  }

  private boolean isReverse(int flags){
    return checkFlagSet(flags, REVERSE);
  }

  private boolean isUnique(int flags){
    return checkFlagSet(flags, UNIQUE);
  }

  private boolean checkFlagSet(int flags, int flag) {
    return ((flags & flag) == flag);
  }
}
