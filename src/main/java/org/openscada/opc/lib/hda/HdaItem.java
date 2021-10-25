package org.openscada.opc.lib.hda;

/**
 * wang-tao.wt@siemens.com
 * Created by wangtao on 2016/7/11.
 * Changed by mihailov
 */
public class HdaItem {
    private final int serverHandle;
    private final int clientHandle;

    HdaItem (final int serverHandle, final int clientHandle) {
        super ();
        this.serverHandle = serverHandle;
        this.clientHandle = clientHandle;
    }

    public int getServerHandle ()
    {
        return this.serverHandle;
    }
    public int getClientHandle () {return this.clientHandle;}
}
