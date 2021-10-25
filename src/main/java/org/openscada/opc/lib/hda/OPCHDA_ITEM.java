package org.openscada.opc.lib.hda;

import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.core.JIArray;
import org.jinterop.dcom.core.JIPointer;
import org.jinterop.dcom.core.JIStruct;
import org.jinterop.dcom.core.JIVariant;
import org.openscada.opc.dcom.common.FILETIME;
import org.openscada.opc.lib.da.ItemState;

import java.util.ArrayList;
import java.util.List;

/**
 * wang-tao.wt@siemens.com
 * Created by wangtao on 2016/7/8.
 */
public class OPCHDA_ITEM {
    private final JIStruct struct;

    private final int errorCode;

    public OPCHDA_ITEM(JIStruct struct, int errorCode)
    {
        this.struct = struct;
        this.errorCode = errorCode;
    }

    public static JIStruct getStruct() throws JIException
    {
        JIStruct jstruct = new JIStruct();
        jstruct.addMember(Integer.class); //hClient
        jstruct.addMember(Integer.class); //haAggregate
        jstruct.addMember(Integer.class); //dwCount
        jstruct.addMember(new JIPointer(new JIArray(FILETIME.getStruct(), null, 1, true)));
        jstruct.addMember(new JIPointer(new JIArray(Integer.class, null, 1, true))); //pdwQualities
        jstruct.addMember(new JIPointer(new JIArray(JIVariant.class, null, 1, true))); //pvDataValues
        return jstruct;
    }

    public List<ItemState> getItemStates(){
        if(struct == null || getCount() == 0){
            return null;
        }
        final JIStruct[] times = (JIStruct[]) ((JIArray) ((JIPointer) struct.getMember(3)).getReferent()).getArrayInstance();
        final Integer[] quality = (Integer[]) ((JIArray) ((JIPointer) struct.getMember(4)).getReferent()).getArrayInstance();
        final JIVariant[] values = (JIVariant[]) ((JIArray) ((JIPointer) struct.getMember(5)).getReferent()).getArrayInstance();

        List<ItemState>  itemStates = new ArrayList<>();
        for (int i=0;i< times.length;i++){
            itemStates.add(new ItemState(0, values[i], new FILETIME(times[i]).asCalendar(), quality[i].shortValue()));
        }

        return itemStates;
    }

    public int getClient()
    {
        return (Integer) struct.getMember(0);
    }

    public int getAggregate()
    {
        return (Integer) struct.getMember(1);
    }

    public int getCount()
    {
        return (Integer) struct.getMember(2);
    }
}
