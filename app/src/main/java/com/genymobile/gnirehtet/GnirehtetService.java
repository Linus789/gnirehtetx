/*
 * Copyright (C) 2017 Genymobile
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.genymobile.gnirehtet;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.VpnService;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Toast;

import com.genymobile.gnirehtet.settings.PreferencesManagerKt;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

import kotlinx.coroutines.flow.MutableStateFlow;
import kotlinx.coroutines.flow.StateFlow;
import kotlinx.coroutines.flow.StateFlowKt;

public class GnirehtetService extends VpnService {

    public static final boolean VERBOSE = false;

    private static final String ACTION_START_VPN = "com.genymobile.gnirehtet.START_VPN";
    private static final String ACTION_CLOSE_VPN = "com.genymobile.gnirehtet.CLOSE_VPN";
    private static final String EXTRA_VPN_CONFIGURATION = "vpnConfiguration";

    private static final String TAG = GnirehtetService.class.getSimpleName();

    private static final InetAddress VPN_ADDRESS = Net.toInetAddress(new byte[] {10, 0, 0, 2});
    // magic value: higher (like 0x8000 or 0xffff) or lower (like 1500) values show poorer performances
    private static final int MTU = 0x4000;

    private final Notifier notifier = new Notifier(this);
    private final RelayTunnelConnectionStateHandler handler = new RelayTunnelConnectionStateHandler(this);

    private ParcelFileDescriptor vpnInterface;
    private Forwarder forwarder;
    private static final MutableStateFlow<Boolean> isRunning = StateFlowKt.MutableStateFlow(false);
    private static final MutableStateFlow<Boolean> isConnected = StateFlowKt.MutableStateFlow(false);
    private static VpnConfiguration lastConfiguration;

    public static void start(Context context, VpnConfiguration config) {
        Intent intent = new Intent(context, GnirehtetService.class);
        intent.setAction(ACTION_START_VPN);
        intent.putExtra(GnirehtetService.EXTRA_VPN_CONFIGURATION, config);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public static void stop(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(createStopIntent(context));
        } else {
            context.startService(createStopIntent(context));
        }
    }

    static Intent createStopIntent(Context context) {
        Intent intent = new Intent(context, GnirehtetService.class);
        intent.setAction(ACTION_CLOSE_VPN);
        return intent;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        Log.d(TAG, "Received request " + action);
        if (ACTION_START_VPN.equals(action)) {
            if (isRunning().getValue()) {
                Log.d(TAG, "VPN already running, ignore START request");
            } else {
                VpnConfiguration config = intent.getParcelableExtra(EXTRA_VPN_CONFIGURATION);
                if (config == null) {
                    config = new VpnConfiguration();
                }
                lastConfiguration = config;
                startVpn(config);
            }
        } else if (ACTION_CLOSE_VPN.equals(action)) {
            close();
        }
        return START_NOT_STICKY;
    }

    public static StateFlow<Boolean> isRunning() {
        return isRunning;
    }

    public static StateFlow<Boolean> isConnected() {
        return isConnected;
    }

    public static VpnConfiguration getLastConfiguration() {
        return lastConfiguration;
    }

    private void startVpn(VpnConfiguration config) {
        if (setupVpn(config)) {
            notifier.start();
            startForwarding();
        } else {
            Toast.makeText(this, "Failed to start Gnirehtet", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    private boolean setupVpn(VpnConfiguration config) {
        Builder builder = new Builder();
        builder.addAddress(VPN_ADDRESS, 32);
        builder.setSession(getString(R.string.app_name));

        CIDR[] routes = config.getRoutes();
        if (routes.length == 0) {
            // no routes defined, redirect the whole network traffic
            builder.addRoute("0.0.0.0", 0);
        } else {
            for (CIDR route : routes) {
                builder.addRoute(route.getAddress(), route.getPrefixLength());
            }
        }

        InetAddress[] dnsServers = config.getDnsServers();
        if (dnsServers.length == 0) {
            // no DNS server defined, use Google DNS
            builder.addDnsServer("8.8.8.8");
        } else {
            for (InetAddress dnsServer : dnsServers) {
                builder.addDnsServer(dnsServer);
            }
        }

        for (String blockedApp : config.getBlockedPackageNames()) {
            try {
                builder.addDisallowedApplication(blockedApp);
            } catch (PackageManager.NameNotFoundException ignored) {}
        }

        // non-blocking by default, but FileChannel is not selectable, that's stupid!
        // so switch to synchronous I/O to avoid polling
        builder.setBlocking(true);
        builder.setMtu(MTU);

        vpnInterface = builder.establish();
        isRunning.setValue(vpnInterface != null);
        if (vpnInterface == null) {
            Log.w(TAG, "VPN starting failed, please retry");
            // establish() may return null if the application is not prepared or is revoked
            return false;
        }

        setAsUndernlyingNetwork();
        return true;
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    private void setAsUndernlyingNetwork() {
        if (Build.VERSION.SDK_INT >= 22) {
            Network vpnNetwork = findVpnNetwork();
            if (vpnNetwork != null) {
                // so that applications knows that network is available
                setUnderlyingNetworks(new Network[] {vpnNetwork});
            }
        } else {
            Log.w(TAG, "Cannot set underlying network, API version " + Build.VERSION.SDK_INT + " < 22");
        }
    }

    private Network findVpnNetwork() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        Network[] networks = cm.getAllNetworks();
        for (Network network : networks) {
            LinkProperties linkProperties = cm.getLinkProperties(network);
            List<LinkAddress> addresses = linkProperties.getLinkAddresses();
            for (LinkAddress addr : addresses) {
                if (addr.getAddress().equals(VPN_ADDRESS)) {
                    return network;
                }
            }
        }
        return null;
    }

    private void startForwarding() {
        forwarder = new Forwarder(this, vpnInterface.getFileDescriptor(), new RelayTunnelListener(handler));
        forwarder.forward();
    }

    private void close() {
        if (!isRunning().getValue()) {
            // already closed
            return;
        }

        notifier.stop();

        try {
            forwarder.stop();
            forwarder = null;
            vpnInterface.close();
            vpnInterface = null;
            isRunning.setValue(false);
            isConnected.setValue(false);
        } catch (IOException e) {
            Log.w(TAG, "Cannot close VPN file descriptor", e);
        }
    }

    private static final class RelayTunnelConnectionStateHandler extends Handler {

        private final GnirehtetService vpnService;

        private RelayTunnelConnectionStateHandler(GnirehtetService vpnService) {
            super(Looper.myLooper());
            this.vpnService = vpnService;
        }

        @Override
        public void handleMessage(Message message) {
            if (!isRunning().getValue()) {
                // if the VPN is not running anymore, ignore obsolete events
                return;
            }
            switch (message.what) {
                case RelayTunnelListener.MSG_RELAY_TUNNEL_CONNECTED:
                    Log.d(TAG, "Relay tunnel connected");
                    vpnService.notifier.setFailure(false);

                    if (!isConnected.getValue() && PreferencesManagerKt.getPreferences().getGnirehtetShowToastOnConnect().getValue()) {
                        Toast.makeText(vpnService, "Gnirehtet connection established", Toast.LENGTH_SHORT).show();
                    }

                    isConnected.setValue(true);
                    break;
                case RelayTunnelListener.MSG_RELAY_TUNNEL_DISCONNECTED:
                    Log.d(TAG, "Relay tunnel disconnected");
                    if (lastConfiguration.stopOnDisconnect()) {
                        stop(vpnService);
                    } else {
                        vpnService.notifier.setFailure(true);
                    }

                    if (isConnected.getValue() && PreferencesManagerKt.getPreferences().getGnirehtetShowToastOnDisconnect().getValue()) {
                        String toastText;

                        if (lastConfiguration.stopOnDisconnect()) {
                            toastText = "Gnirehtet stopped due to no connection";
                        } else {
                            toastText = "Gnirehtet connection failed";
                        }

                        Toast.makeText(vpnService, toastText, Toast.LENGTH_SHORT).show();
                    }

                    isConnected.setValue(false);
                    break;
                default:
            }
        }

    }

}
