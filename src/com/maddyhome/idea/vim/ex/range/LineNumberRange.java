package com.maddyhome.idea.vim.ex.range;

/*
 * IdeaVim - A Vim emulator plugin for IntelliJ Idea
 * Copyright (C) 2003-2005 Rick Maddy
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

import com.intellij.openapi.editor.Editor;
import com.maddyhome.idea.vim.helper.DataPackage;
import com.maddyhome.idea.vim.helper.EditorHelper;

/**
 * Represents a specific line, the current line, or the last line of a file
 */
public class LineNumberRange extends AbstractRange
{
    public static final int CURRENT_LINE = -99999999;
    public static final int LAST_LINE =    -99999998;

    /**
     * Create a range for the current line
     * @param offset The range offset
     * @param move True if cursor should be moved
     */
    public LineNumberRange(int offset, boolean move)
    {
        super(offset, move);

        this.line = CURRENT_LINE;
    }

    /**
     * Create a range for the given line
     * @param offset The range offset
     * @param move True if cursor should be moved
     */
    public LineNumberRange(int line, int offset, boolean move)
    {
        super(offset, move);

        this.line = line;
    }

    /**
     * Gets the line number specified by this range without regard to any offset.
     * @param editor The editor to get the line for
     * @param context The data context
     * @param lastZero True if last line was set to start of file
     * @return The zero based line number, -1 for start of file
     */
    protected int getRangeLine(Editor editor, DataPackage context, boolean lastZero)
    {
        if (line == CURRENT_LINE)
        {
            line = editor.getCaretModel().getLogicalPosition().line;
        }
        else if (line == LAST_LINE)
        {
            line = EditorHelper.getLineCount(editor) - 1;
        }

        return line;
    }

    public String toString()
    {
        StringBuffer res = new StringBuffer();
        res.append("LineNumberRange[");
        res.append("line=").append(line);
        res.append(", ");
        res.append(super.toString());
        res.append("]");

        return res.toString();
    }

    private int line;
}
