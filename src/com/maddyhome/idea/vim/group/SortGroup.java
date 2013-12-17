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

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by aardvark on 12/14/13.
 */
public class SortGroup extends AbstractActionGroup {
  public static final int REVERSE = 1;
  public static final int IGNORE_CASE = 2;
  public static final int SORT_NUMERIC= 4;
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
        case 'n':
          flags += SORT_NUMERIC;
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
      //TODO replace sort with collation key sort
      Collections.sort(textCol, String.CASE_INSENSITIVE_ORDER);
    } else {
      Collections.sort(textCol);
    }

    /*
     * what numeric sort actually do
     * 1. filter all strings in collection that contains numerics
     * 2. sort numerics
     * 3. dont sort remaining collection
     * 4. join sorted numerics and collection
     * 5. return results
     */
    if (checkFlagSet(flags, SORT_NUMERIC)) {
      final Pattern pat = Pattern.compile("\\D*(\\d+).*");
      List<String> numQueue = new ArrayList<>();
      for (String s : textCol) {
        if (pat.matcher(s).matches()){
          numQueue.add(s);
        }
      }
      textCol.removeAll(numQueue);

      Collections.sort(numQueue, new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
          Matcher matcher = pat.matcher(o1);
          matcher.matches();
          int d1 = Integer.parseInt(matcher.group(1));
          matcher = pat.matcher(o2);
          matcher.matches();
          int d2 = Integer.parseInt(matcher.group(1));
          return d1 - d2;
        }
      });
      textCol.addAll(numQueue);
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
