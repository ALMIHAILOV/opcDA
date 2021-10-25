package hda;

import org.jinterop.dcom.common.JIException;
import org.openscada.opc.lib.common.AlreadyConnectedException;
import org.openscada.opc.lib.common.ConnectionInformation;
import org.openscada.opc.lib.da.ItemState;
import org.openscada.opc.lib.hda.HdaAccessBase;
import org.openscada.opc.lib.hda.HdaServer;
import org.openscada.opc.lib.hda.HdaSyncAccess;
import org.openscada.opc.lib.hda.OPCHDA_ITEM;

import java.net.UnknownHostException;
import java.util.*;

/**
 * wang-tao.wt@siemens.com
 * Created by wangtao on 2016/7/11.
 */
public class hdaTest1 {
    public static void main(String[] args) throws JIException{
        final ConnectionInformation ci = new ConnectionInformation ();
        ci.setHost ( "192.168.0.100" );
        ci.setDomain ( "" );
        ci.setUser ( "user" );
        ci.setPassword ( "password" );
        ci.setClsid ( "F8582CF2-88FB-11D0-B850-00C0F0104305" );

        HdaServer server = new HdaServer(ci);

        HdaAccessBase access = null;
        try {
            access = new HdaSyncAccess(server);
        } catch (JIException e) {
            e.printStackTrace();
        }

        Set<String> items = new HashSet<>();
        items.add("Bucket Brigade.Int1");
        items.add("Bucket Brigade.Real4");
        try {
            access.addItems(items);
        } catch (JIException e) {
            e.printStackTrace();
        }

        try {
            access.connect();
        } catch (JIException e) {
            e.printStackTrace();
        } catch (AlreadyConnectedException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        Calendar c = Calendar.getInstance();
        Date ss = new Date();
        c.setTime(ss);
        c.add(Calendar.MINUTE, -100);

        OPCHDA_ITEM hdaItem;
        try {
            hdaItem = access.readRaw("Bucket Brigade.Real4", c.getTime(), new Date());
        } catch (JIException e) {
            e.printStackTrace();
            return;
        }
        List<ItemState> itemStates = hdaItem.getItemStates();
        for (ItemState itemState:itemStates){
            System.out.println(itemState.getValue().getObject());
            System.out.println(itemState.getTimestamp().getTime());
            System.out.println(itemState.getQuality());
        }

        server.disconnect();

    }
}
