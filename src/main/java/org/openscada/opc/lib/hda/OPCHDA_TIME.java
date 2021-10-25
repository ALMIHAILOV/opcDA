package org.openscada.opc.lib.hda;

import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.core.JIFlags;
import org.jinterop.dcom.core.JIPointer;
import org.jinterop.dcom.core.JIString;
import org.jinterop.dcom.core.JIStruct;
import org.openscada.opc.dcom.common.FILETIME;

import java.util.Calendar;

/**
 * wang-tao.wt@siemens.com
 * Created by wangtao on 2016/7/8.
 */
public class OPCHDA_TIME {
    private final JIStruct struct;

    public OPCHDA_TIME(boolean string, String timeStr, FILETIME time) throws JIException
    {
        struct = new JIStruct();
        struct.addMember(string);
        struct.addMember(new JIPointer(new JIString(timeStr, JIFlags.FLAG_REPRESENTATION_STRING_LPWSTR)));//szTime
        struct.addMember(time.toStruct());
    }

    public JIStruct toStruct(){
        return struct;
    }

    public OPCHDA_TIME(JIStruct struct)
    {
        this.struct = struct;
    }

    public boolean isString()
    {
        return (Boolean) struct.getMember(0);
    }

    public String getTimeString()
    {
        return ((JIString)((JIPointer) struct.getMember(1)).getReferent()).getString();
    }

    public Calendar getTime()
    {
        return new FILETIME((JIStruct) struct.getMember(2)).asCalendar();
    }

    public static JIStruct getStruct() throws JIException
    {
        JIStruct jstruct = new JIStruct();
        jstruct.addMember(Boolean.class);
        jstruct.addMember(new JIPointer(new JIString(JIFlags.FLAG_REPRESENTATION_STRING_LPWSTR)));
        jstruct.addMember(new FILETIME().getStruct());
        return jstruct;
    }
}
