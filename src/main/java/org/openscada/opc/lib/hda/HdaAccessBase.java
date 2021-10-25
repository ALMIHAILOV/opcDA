package org.openscada.opc.lib.hda;

import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.core.IJIComObject;
import org.openscada.opc.lib.common.AlreadyConnectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * wang-tao.wt@siemens.com
 * Created by wangtao on 2016/7/11.
 * Changed mihailov
 */
public abstract class HdaAccessBase {

    private static final Logger logger = LoggerFactory.getLogger ( HdaAccessBase.class );

    protected HdaServer hdaServer;

    protected IJIComObject readObject;

    private final String IID;

    private final Set<String> itemSetCache = new HashSet<>();

    protected boolean active = false;

    public HdaAccessBase(HdaServer hdaServer, String IID) {
        this.hdaServer = hdaServer;
        this.IID = IID;
    }

    public synchronized void addItem(String itemName) throws JIException{
        if(isActive()){
            this.hdaServer.addItem(itemName);
        }else{
            this.itemSetCache.add(itemName);
        }
    }

    public synchronized void addItems(Set<String> itemNames) throws JIException{
        if(isActive()){
            this.hdaServer.addItems(itemNames);
        }else{
            this.itemSetCache.addAll(itemNames);
        }
    }

    public synchronized void connect() throws JIException, AlreadyConnectedException, UnknownHostException {
        this.hdaServer.connect();

        this.readObject = hdaServer.queryInterface(this.IID);

        if(itemSetCache.isEmpty()){
            return;
        }

        this.hdaServer.addItems(this.itemSetCache);
        itemSetCache.clear();

        this.active = true;
        logger.info("HdaAccessBase start to work");
    }

    public boolean isActive(){
        return this.active;
    }

    protected IJIComObject getObject(){
        return readObject;
    }

    public abstract OPCHDA_ITEM readRaw(String itemName, Date begin, Date end) throws JIException;
    public abstract List<OPCHDA_ITEM> readRaws(List<String> itemName, Date begin, Date end) throws JIException;
}
