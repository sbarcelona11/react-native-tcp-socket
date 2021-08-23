package com.asterinet.react.tcpsocket;

import android.util.Base64;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpEventListener {

    private final DeviceEventManagerModule.RCTDeviceEventEmitter rctEvtEmitter;

    public TcpEventListener(final ReactContext reactContext) {
        rctEvtEmitter = reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class);
    }

    public void onConnection(Integer serverId, Integer clientId, Socket socket) {
        WritableMap eventParams = Arguments.createMap();
        eventParams.putInt("id", serverId);

        WritableMap infoParams = Arguments.createMap();
        infoParams.putInt("id", clientId);

        WritableMap connectionParams = Arguments.createMap();
        final InetSocketAddress remoteAddress = (InetSocketAddress) socket.getRemoteSocketAddress();

        connectionParams.putString("localAddress", socket.getLocalAddress().getHostAddress());
        connectionParams.putInt("localPort", socket.getLocalPort());
        connectionParams.putString("remoteAddress", remoteAddress.getAddress().getHostAddress());
        connectionParams.putInt("remotePort", socket.getPort());
        connectionParams.putString("remoteFamily", remoteAddress.getAddress() instanceof Inet6Address ? "IPv6" : "IPv4");

        infoParams.putMap("connection", connectionParams);
        eventParams.putMap("info", infoParams);

        sendEvent("connection", eventParams);
    }

    public void onConnect(Integer id, TcpSocketClient client) {
        WritableMap eventParams = Arguments.createMap();
        eventParams.putInt("id", id);
        WritableMap connectionParams = Arguments.createMap();
        final Socket socket = client.getSocket();
        final InetSocketAddress remoteAddress = (InetSocketAddress) socket.getRemoteSocketAddress();

        connectionParams.putString("localAddress", socket.getLocalAddress().getHostAddress());
        connectionParams.putInt("localPort", socket.getLocalPort());
        connectionParams.putString("remoteAddress", remoteAddress.getAddress().getHostAddress());
        connectionParams.putInt("remotePort", socket.getPort());
        connectionParams.putString("remoteFamily", remoteAddress.getAddress() instanceof Inet6Address ? "IPv6" : "IPv4");
        eventParams.putMap("connection", connectionParams);
        sendEvent("connect", eventParams);
    }

    public void onListen(Integer id, TcpSocketServer server) {
        WritableMap eventParams = Arguments.createMap();
        eventParams.putInt("id", id);
        WritableMap connectionParams = Arguments.createMap();
        final ServerSocket serverSocket = server.getServerSocket();
        final InetAddress address = serverSocket.getInetAddress();

        connectionParams.putString("localAddress", serverSocket.getInetAddress().getHostAddress());
        connectionParams.putInt("localPort", serverSocket.getLocalPort());
        connectionParams.putString("localFamily", address instanceof Inet6Address ? "IPv6" : "IPv4");
        eventParams.putMap("connection", connectionParams);
        sendEvent("listening", eventParams);
    }

    public void onData(Integer id, byte[] data) {
        WritableMap eventParams = Arguments.createMap();
        eventParams.putInt("id", id);
        eventParams.putString("data", Base64.encodeToString(data, Base64.NO_WRAP));

        sendEvent("data", eventParams);
    }

    public void onClose(Integer id, String error) {
        if (error != null) {
            onError(id, error);
        }
        WritableMap eventParams = Arguments.createMap();
        eventParams.putInt("id", id);
        eventParams.putBoolean("hadError", error != null);

        sendEvent("close", eventParams);
    }

    public void onError(Integer id, String error) {
        WritableMap eventParams = Arguments.createMap();
        eventParams.putInt("id", id);
        eventParams.putString("error", error);

        sendEvent("error", eventParams);
    }

    private void sendEvent(String eventName, WritableMap params) {
        rctEvtEmitter.emit(eventName, params);
    }
}
