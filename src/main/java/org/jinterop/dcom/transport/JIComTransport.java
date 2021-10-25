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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.Properties;
import java.util.logging.Level;

/**
 * Borrowed all from ncacn_ip_tcp.RpcTransport from jarapac, modified attach api
 * to include SocketChannel.
 *
 * @exclude
 * @since 1.0
 *
 */
final class JIComTransport implements Transport {

    public static final String PROTOCOL = "ncacn_ip_tcp";

    private static final String LOCALHOST;

    private Properties properties;

    private String host;

    private int port;

    private Socket socket;

    private OutputStream output;

    private InputStream input;

    private boolean attached;

    private SocketChannel channel = null;

    static {
        String localhost = null;
        try {
            localhost = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ex) {
        }
        LOCALHOST = localhost;
    }

    JIComTransport(String address, Properties properties)
            throws ProviderException {
        this.properties = properties;
        parse(address);
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
        try {
            if (JISystem.getLogger().isLoggable(Level.FINEST)) {
                JISystem.getLogger().log(Level.FINEST, "Opening socket on {0}", new InetSocketAddress(InetAddress.getByName(host), port));
            }
            channel = SocketChannel.open();
            socket = channel.socket();

            int timeout = 0;
            try {
                timeout = Integer.parseInt(this.properties.getProperty("rpc.socketTimeout", "0"));
            } catch (NumberFormatException ex) {
                JISystem.getLogger().log(Level.WARNING, "Invalid timeout value " + this.properties.getProperty("rpc.socketTimeout"), ex);
            }
            socket.setSoTimeout(timeout);
            socket.connect(new InetSocketAddress(InetAddress.getByName(host), port), timeout);
            output = null;
            input = null;
            attached = true;
            socket.setKeepAlive(true);//backup for not providing a timeout.
            return new JIComEndpoint(this, syntax);
        } catch (IOException ex) {
            try {
                close();
            } catch (Exception ignore) {
            }
            throw ex;
        }
    }

    @Override
    public void close() throws IOException {
        try {
            if (socket != null) {
//            	input.close();
//            	output.close();
                socket.shutdownInput();
                socket.shutdownOutput();
                socket.close();
                channel.close();
                if (JISystem.getLogger().isLoggable(Level.FINEST)) {
                    JISystem.getLogger().log(Level.FINEST, "Socket closed... {0} host {1} , port {2}", new Object[]{socket, host, port});
                }
            }
        } finally {
            attached = false;
            socket = null;
            output = null;
            input = null;
            channel = null;
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
        channel.configureBlocking(true);
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

    void parse(String address) throws ProviderException {
        if (address == null) {
            throw new ProviderException("Null address.");
        }
        if (!address.startsWith("ncacn_ip_tcp:")) {
            throw new ProviderException("Not an ncacn_ip_tcp address.");
        }
        address = address.substring(13);
        int index = address.indexOf('[');
        if (index == -1) {
            throw new ProviderException("No port specifier present.");
        }
        String server = address.substring(0, index);
        address = address.substring(index + 1);
        index = address.indexOf(']');
        if (index == -1) {
            throw new ProviderException("Port specifier not terminated.");
        }
        address = address.substring(0, index);
        if ("".equals(server)) {
            server = LOCALHOST;
        }
        try {
            port = Integer.parseInt(address);
        } catch (Exception ex) {
            throw new ProviderException("Invalid port specifier.");
        }
        host = server;
    }

}
