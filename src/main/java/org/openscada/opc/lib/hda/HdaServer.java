package org.openscada.opc.lib.hda;

import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.core.*;
import org.openscada.opc.dcom.common.impl.Helper;
import org.openscada.opc.lib.common.AlreadyConnectedException;
import org.openscada.opc.lib.common.ConnectionInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;
import java.util.*;

/**
 * wang-tao.wt@siemens.com
 * Created by wangtao on 2016/7/11.
 * Changed by mihailov
 */
public class HdaServer {
    private static final Logger logger = LoggerFactory.getLogger ( HdaServer.class );

    private final ConnectionInformation connectionInformation;

    private JISession session = null;

    private JIComServer comServer;

    private IJIComObject serverInstance;

    private IJIComObject hdaServer;

    private Integer clientHander = 0;

    private final Map<String, HdaItem> itemMap = new HashMap<>();

    public HdaServer ( final ConnectionInformation connectionInformation)
    {
        super ();
        this.connectionInformation = connectionInformation;
    }

    protected IJIComObject queryInterface(String iid) throws JIException {
        return this.serverInstance.queryInterface(iid);
    }

    public synchronized void connect() throws AlreadyConnectedException , UnknownHostException, JIException {
        if (isConnected()){
            throw new AlreadyConnectedException();
        }

        this.session = JISession.createSession ( this.connectionInformation.getDomain (), this.connectionInformation.getUser (), this.connectionInformation.getPassword () );
        this.comServer = new JIComServer ( JIClsid.valueOf ( this.connectionInformation.getClsid () ), this.connectionInformation.getHost (), this.session );
        this.serverInstance = this.comServer.createInstance();
        this.hdaServer  = this.serverInstance.queryInterface(HDAIID.IOPCHDA_Server);
        logger.info("Hda server connected");
    }

    public synchronized  void disconnect() throws JIException{
        if(isConnected()){
            JISession.destroySession(this.session);
            this.session = null;
        }
        logger.info("Hda server disconnected");
    }

    public boolean isConnected(){
        return session != null;
    }

    private void getItemHandles(JIString[] items, Integer[] phClients) throws JIException{
        JICallBuilder callObject = new JICallBuilder ( true );
        callObject.setOpnum (3);

        callObject.addInParamAsInt ( items.length, JIFlags.FLAG_NULL );
        callObject.addInParamAsArray ( new JIArray(items, true), JIFlags.FLAG_NULL );
        callObject.addInParamAsArray ( new JIArray(phClients, true), JIFlags.FLAG_NULL );
        callObject.addOutParamAsObject ( new JIPointer ( new JIArray ( Integer.class, null, 1, true ) ), JIFlags.FLAG_NULL );
        callObject.addOutParamAsObject ( new JIPointer ( new JIArray ( Integer.class, null, 1, true ) ), JIFlags.FLAG_NULL );

        final Object[] result = Helper.callRespectSFALSE ( this.hdaServer, callObject );

        assert ((JIPointer) result[0]).getReferent() != null;
        Integer[] serverhandles = (Integer[]) ( (JIArray) ( (JIPointer)result[0] ).getReferent () ).getArrayInstance ();
        assert ((JIPointer) result[1]).getReferent() != null;
        Integer[] errorCodes = (Integer[]) ( (JIArray) ( (JIPointer)result[1] ).getReferent () ).getArrayInstance ();

        for (int i = 0; i < items.length; i++){
            //// TODO: 2016/7/11 TO handle errorCodes, judge whether the number of add is equal to the returned server, and throw the wrong itemNames
            if (errorCodes[i] == 0) {
                this.itemMap.put(items[i].getString(), new HdaItem(serverhandles[i], phClients[i]));
            }else{
                logger.error("Wrong items: " + items[i] + " . Error code: " + errorCodes[i]);

            }
        }
    }

    protected synchronized void addItem(String itemName) throws JIException {
        if(this.isAddedItem(itemName)){
            return;
        }
        JIString[] strings = new JIString[1];
        strings[0] = new JIString(itemName, JIFlags.FLAG_REPRESENTATION_STRING_LPWSTR);
        final Integer[] phClients = new Integer[1];
        phClients[0] = getClientHandle();
        this.getItemHandles(strings, phClients);
    }

    protected synchronized void addItems(Set<String> itemNames) throws JIException{
        List<JIString> strings = new ArrayList<>();
        List<Integer> clients = new ArrayList<>();
        for (String s : itemNames) {
            if(this.isAddedItem(s)){
                continue;
            }
            strings.add(new JIString(s, JIFlags.FLAG_REPRESENTATION_STRING_LPWSTR));
            clients.add(getClientHandle());
        }
        this.getItemHandles(strings.toArray(new JIString[0]), clients.toArray(new Integer[0]) );
    }

    protected int getServerHandle(String itemName){
        HdaItem hdaItem = itemMap.get(itemName);
        if (hdaItem == null){
            return -1;
        }
        return hdaItem.getServerHandle();
    }

    protected synchronized int getClientHandle(){
        return clientHander++;
    }

    protected boolean isAddedItem(String itemName){
        return itemMap.containsKey(itemName);
    }
}
