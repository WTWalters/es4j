/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.hlc;

import com.google.common.util.concurrent.AbstractScheduledService;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeStamp;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * NTPServerTimeProvider is a physical time provider that uses external NTP servers to fetch timestamp
 * periodically (currently hardcoded as 1 minute).
 * <p>
 * By default, NTP servers are:
 * <p>
 * "0.pool.ntp.org", "1.pool.ntp.org", "2.pool.ntp.org", "3.pool.ntp.org", "localhost"
 * <p>
 * NTPServerTimeProvider is an EventReducer Service and needs to be started prior
 * to using it as a PhysicalTimeProvider.
 */
@Component(property = "ntp.servers=localhost,0.pool.ntp.org,1.pool.ntp.org,2.pool.ntp.org,3.pool.ntp.org")
public class NTPServerTimeProvider extends AbstractScheduledService implements PhysicalTimeProvider {


    public static final int SO_TIMEOUT = 2000;
    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);

    private static final String[] DEFAULT_NTP_SERVERS =
            {"localhost", "0.pool.ntp.org", "1.pool.ntp.org", "2.pool.ntp.org", "3.pool.ntp.org"};

    private NTPUDPClient client;
    private List<InetAddress> servers;

    private TimeStamp timestamp;
    private long nano;

    @Setter
    @Accessors(fluent = true)
    private long delay = 30;
    @Setter @Accessors(fluent = true)
    private TimeUnit delayUnits = TimeUnit.MINUTES;


    @Activate
    protected void activate(ComponentContext ctx) throws SocketException {
        client = new NTPUDPClient();
        String serversList = (String) ctx.getProperties().get("ntp.servers");
        setServers(serversList.split(","));
        setSocketTimeout();
    }

    private void setSocketTimeout() throws SocketException {
        client.open();
        client.setSoTimeout(SO_TIMEOUT);
    }

    /**
     * Creates NTPServerTimeProvider with default NTP servers
     *
     * @throws UnknownHostException Throws UnknownHostException for the first unresolved host, if no hosts were resolvable
     */
    public NTPServerTimeProvider() throws UnknownHostException, SocketException {
        this(DEFAULT_NTP_SERVERS);
    }

    @Override
    protected void startUp() throws Exception {
        update();
    }

    @Override
    protected void runOneIteration() throws Exception {
        update();
    }

    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedDelaySchedule(0, delay, delayUnits);
    }

    /**
     * Creates NTPServerTimeProvider with a custom list of NTP server addresses
     *
     * @param ntpServers Array of custom NTP server addresses
     * @throws UnknownHostException Throws UnknownHostException for the first unresolved host, if no hosts were resolvable
     */
    public NTPServerTimeProvider(String[] ntpServers) throws UnknownHostException, SocketException {
        client = new NTPUDPClient();
        setServers(ntpServers);
        if (servers.isEmpty()) {
            throw new UnknownHostException(ntpServers[0]);
        }
        setSocketTimeout();
    }

    protected void setServers(String[] ntpServers) {
        servers = Arrays.asList(ntpServers).stream().map(server -> {
            try {
                return InetAddress.getByName(server);
            } catch (UnknownHostException e) {
                return null;
            }
        }).filter(address -> address != null).collect(Collectors.toList());
    }

    synchronized private void update() {
        InetAddress server = servers.remove(0);
        try {
            timestamp = client.getTime(server).getMessage().getTransmitTimeStamp();
            nano = System.nanoTime();
            servers.add(0, server); // add back to the beginning
        } catch (IOException e) {
            servers.add(server); // add to the end of the list
        }
    }

    TimeStamp getTimestamp() throws TimeoutException {
        if (timestamp == null) {
            throw new TimeoutException();
        }
        TimeStamp ts = new TimeStamp(timestamp.ntpValue());
        long fraction = ts.getFraction();
        long seconds = ts.getSeconds();
        long nanoTime = System.nanoTime();
        long l = (nanoTime - nano) / 1_000_000_000;
        double v = (nanoTime - nano) / 1_000_000_000.0 - l;
        long i = (long) (v * 1_000_000_000);
        long fraction_ = fraction + i;
        if (fraction_ >= 1_000_000_000) {
            fraction_ -= 1_000_000_000;
            l++;
        }
        return new TimeStamp((seconds + l) << 32 | fraction_);
    }


    @Override
    @SneakyThrows
    public long getPhysicalTime()  {
        return getTimestamp().ntpValue();
    }

}

