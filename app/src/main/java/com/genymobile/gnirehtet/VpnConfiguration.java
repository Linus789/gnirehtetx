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

import android.os.Parcel;
import android.os.Parcelable;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class VpnConfiguration implements Parcelable {

    private final InetAddress[] dnsServers;
    private final CIDR[] routes;
    private final String[] blockedPackageNames;
    private boolean stopOnDisconnect;
    private final boolean startedByServer;

    public VpnConfiguration() {
        this.dnsServers = new InetAddress[0];
        this.routes = new CIDR[0];
        this.blockedPackageNames = new String[0];
        this.stopOnDisconnect = false;
        this.startedByServer = true;
    }

    public VpnConfiguration(InetAddress[] dnsServers, CIDR[] routes, String[] blockedPackageNames, boolean stopOnDisconnect, boolean startedByServer) {
        this.dnsServers = dnsServers;
        this.routes = routes;
        this.blockedPackageNames = blockedPackageNames;
        this.stopOnDisconnect = stopOnDisconnect;
        this.startedByServer = startedByServer;
    }

    private VpnConfiguration(Parcel source) {
        int dnsCount = source.readInt();
        dnsServers = new InetAddress[dnsCount];
        try {
            for (int i = 0; i < dnsCount; ++i) {
                dnsServers[i] = InetAddress.getByAddress(source.createByteArray());
            }
        } catch (UnknownHostException e) {
            throw new AssertionError("Invalid address", e);
        }
        routes = source.createTypedArray(CIDR.CREATOR);
        blockedPackageNames = source.createStringArray();
        stopOnDisconnect = source.readByte() == 1;
        startedByServer = source.readByte() == 1;
    }

    public InetAddress[] getDnsServers() {
        return dnsServers;
    }

    public CIDR[] getRoutes() {
        return routes;
    }

    public String[] getBlockedPackageNames() {
        return blockedPackageNames;
    }

    public boolean stopOnDisconnect() {
        return stopOnDisconnect;
    }

    public void setStopOnDisconnect(boolean stopOnDisconnect) {
        this.stopOnDisconnect = stopOnDisconnect;
    }

    public boolean isStartedByServer() {
        return startedByServer;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(dnsServers.length);
        for (InetAddress addr : dnsServers) {
            dest.writeByteArray(addr.getAddress());
        }
        dest.writeTypedArray(routes, 0);
        dest.writeStringArray(blockedPackageNames);
        dest.writeByte(stopOnDisconnect ? (byte) 1 : (byte) 0);
        dest.writeByte(startedByServer ? (byte) 1 : (byte) 0);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<VpnConfiguration> CREATOR = new Creator<VpnConfiguration>() {
        @Override
        public VpnConfiguration createFromParcel(Parcel source) {
            return new VpnConfiguration(source);
        }

        @Override
        public VpnConfiguration[] newArray(int size) {
            return new VpnConfiguration[size];
        }
    };
}
