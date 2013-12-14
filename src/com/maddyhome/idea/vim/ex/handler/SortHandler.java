package com.maddyhome.idea.vim.ex.handler;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.maddyhome.idea.vim.ex.*;
import com.maddyhome.idea.vim.group.CommandGroups;


/**
 * Created by aardvark on 12/14/13.
 */
public class SortHandler extends CommandHandler {
  public SortHandler() {
    super(new CommandName[]{
      new CommandName("sor", "t")
    }, RANGE_OPTIONAL | ARGUMENT_OPTIONAL | WRITABLE);
  }

  @Override
  public boolean execute(Editor editor, DataContext context, ExCommand cmd) throws ExException {
    LineRange range = cmd.getLineRange(editor, context, false);
    return CommandGroups.getInstance().getSort().sort(editor, context, range, cmd.getCommand(),
                                                                    cmd.getArgument());

  }
}
