/** j-Interop (Pure Java implementation of DCOM protocol)
 * Copyright (C) 2006  Vikram Roopchand
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * Though a sincere effort has been made to deliver a professional,
 * quality product,the library itself is distributed WITHOUT ANY WARRANTY;
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110, USA
 */
package org.jinterop.dcom.transport;

import ndr.NdrBuffer;
import org.jinterop.dcom.common.JISystem;
import rpc.Endpoint;
import rpc.ProviderException;
import rpc.RpcException;
import rpc.Transport;
import rpc.core.PresentationSyntax;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Properties;

/**
 * @exclude @since 1.0
 *
 */
final class JIComRuntimeTransport implements Transport {

    public static final String PROTOCOL = "ncacn_ip_tcp";

    private Properties properties;

    private Socket socket;

    private OutputStream output;

    private InputStream input;

    private boolean attached;

    JIComRuntimeTransport(String address, Properties properties)
            throws ProviderException {
        this.properties = properties;
        //address is ignored
    }

    @Override
    public String getProtocol() {
        return PROTOCOL;
    }

    @Override
    public Properties getProperties() {
        return properties;
    }

    @Override
    public Endpoint attach(PresentationSyntax syntax) throws IOException {
        if (attached) {
            throw new RpcException("Transport already attached.");
        }

        Endpoint endPoint = null;
        try {
            socket = (Socket) JISystem.internal_getSocket();
            output = null;
            input = null;
            attached = true;
            endPoint = new JIComRuntimeEndpoint(this, syntax);
        } catch (Exception ex) {
            try {
                close();
            } catch (Exception ignore) {
            }
        }
        return endPoint;
    }

    @Override
    public void close() throws IOException {
        try {
            if (socket != null) {
                socket.close();
            }
        } finally {
            attached = false;
            socket = null;
            output = null;
            input = null;
        }
    }

    @Override
    public void send(NdrBuffer buffer) throws IOException {
        if (!attached) {
            throw new RpcException("Transport not attached.");
        }
        if (output == null) {
            output = socket.getOutputStream();
        }
        output.write(buffer.getBuffer(), 0, buffer.getLength());
        output.flush();
    }

    @Override
    public void receive(NdrBuffer buffer) throws IOException {
        if (!attached) {
            throw new RpcException("Transport not attached.");
        }
        if (input == null) {
            input = socket.getInputStream();
        }
        buffer.length = (input.read(buffer.getBuffer(), 0,
                buffer.getCapacity()));
    }

}
