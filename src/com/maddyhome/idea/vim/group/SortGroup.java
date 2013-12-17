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
        case 'x':
          flags += SORT_HEX;
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

    List<String> stringsToSort = new ArrayList<>(Arrays.asList(text));

    if (isUnique(flags)){
      stringsToSort = Lists.newArrayList(Sets.newHashSet(stringsToSort));
    }

    String sortedStrings = "";
    if (isIgnoreCase(flags)){
      //TODO replace sort with collation key sort
      Collections.sort(stringsToSort, String.CASE_INSENSITIVE_ORDER);
    } else {
      Collections.sort(stringsToSort);
    }

    if (checkFlagSet(flags, SORT_NUMERIC)) {
      final Pattern pat = Pattern.compile("\\D*(\\d+).*");
      sortNumerics(pat, 10, stringsToSort);
    }

    if (checkFlagSet(flags, SORT_HEX)){
      final Pattern pat = Pattern.compile("\\D*0x(\\p{XDigit}+).*");
      sortNumerics(pat, 16, stringsToSort);
    }

    if (checkFlagSet(flags, SORT_OCTAL)){
      final Pattern pat = Pattern.compile("\\D*0([0-7]+).*");
      sortNumerics(pat, 8, stringsToSort);
    }

    if (isReverse(flags)){
      Collections.reverse(stringsToSort);
    }

    for (String s : stringsToSort) {
      sortedStrings += s + "\n";
    }

    if (isRangeEmpty(ranges)){
      editor.getDocument().setText(sortedStrings);
    } else {
      editor.getDocument().replaceString(tr.getStartOffset(), tr.getEndOffset(), sortedStrings);
    }

    return res;
  }

  private Comparator<String> getComparator(final Pattern pat, final int radix) {
    return new Comparator<String>() {
      @Override
      public int compare(String o1, String o2) {
        Matcher matcher = pat.matcher(o1);
        matcher.matches();
        int d1 = Integer.parseInt(matcher.group(1), radix);
        matcher = pat.matcher(o2);
        matcher.matches();
        int d2 = Integer.parseInt(matcher.group(1), radix);
        return d1 - d2;
      }
    };
  }

  private void sortNumerics(Pattern pat, int radix, List<String> textCol){
    List<String> numList= new ArrayList<>();
    for (String s : textCol) {
      if (pat.matcher(s).matches()){
        numList.add(s);
      }
    }
    textCol.removeAll(numList);
    Collections.sort(numList, getComparator(pat, radix));
    textCol.addAll(numList);
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
