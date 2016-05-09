package ro.atrifan.client.worker;

import org.apache.log4j.Logger;
import ro.atrifan.client.http.util.HTTPMethod;
import ro.atrifan.client.http.util.HTTPParser;
import ro.atrifan.client.http.util.HTTPResponse;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Created by alexandru.trifan on 02.05.2016.
 */
public class NIOTask implements Runnable {

    private static final Logger log = Logger.getLogger(NIOWorker.class);
    private static final int MAX = 200;

    private volatile boolean running = false;
    private ByteBuffer inBound = ByteBuffer.allocate(4096);
    private CharsetDecoder decoder = Charset.forName("ISO-8859-1").newDecoder();
    private Selector selector;
    ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(20,
            new ThreadFactory() {
                public Thread newThread(Runnable r) {
                    Thread t = Executors.defaultThreadFactory().newThread(r);
                    t.setDaemon(true);
                    return t;
                }
            });

    public NIOTask(HTTPMethod method, long timeout, CallBack callBack) throws IOException {
        log.info("Started Nio Worker");
        selector = Selector.open();
        // Start ourselves up
        Work work = new Work();
        work.method = method;

        boolean added = false;

        Set keys = selector.keys();

        SocketChannel channel = null;

        try {
            URL url = work.method.getURL();
            int port = url.getPort() > 0 ? url.getPort() : 80;
            InetAddress ia = InetAddress.getByName(url.getHost());
            InetSocketAddress isa = new InetSocketAddress(ia, port);

            channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.connect(isa);

            work.outBound = ByteBuffer.wrap(work.method.toHTTP().getBytes());
            work.callBack = callBack;
            WorkState state = new WorkState(timeout, work);
            channel.register(selector, SelectionKey.OP_CONNECT, state);
        } catch (IOException ioe) {
            log.error("Problem adding work to NIOWorker thread", ioe);

            if (channel != null) {
                SelectionKey key = channel.keyFor(selector);
                if (key != null) {
                    key.cancel();
                }

                try {
                    channel.close();
                } catch (IOException ioe2) {
                    // Smother
                    log.error("Error closing channel", ioe2);
                }
            }
        }

    }

    public void run() {
        running = true;

        // Run until stopped
        while(running) {
            try {
                int num = selector.selectNow();
                if (num > 0) {
                    processKeys();
                } else {
                    Thread.yield();
                }

                // If there is no work, wait until these is something to do
                Set keys = selector.keys();
                if (keys.size() == 0) {
                    keys.wait();
                } else {
                    Iterator iter = keys.iterator();
                    while (iter.hasNext()) {
                        SelectionKey key = (SelectionKey) iter.next();
                        WorkState state = (WorkState) key.attachment();
                        if (state.isTimedOut()) {
                            SocketChannel channel = (SocketChannel) key.channel();
                            finished(channel, key, state);
                        }
                    }
                }
            } catch (IOException ioe) {
                log.error(ioe);
            } catch (InterruptedException ie) {
                log.error(ie);
            }
        }
        cleanup();
    }

    private void cleanup() {
        Set keys = selector.keys();
        for (Iterator iter = keys.iterator(); iter.hasNext();) {
            SelectionKey key = (SelectionKey) iter.next();
            SocketChannel channel = (SocketChannel) key.channel();

            key.cancel();

            try {
                channel.close();
            } catch (IOException ioe) {
                log.fatal("Unable to shutdown an NIO socket channel", ioe);
            }
        }

        try {
            selector.close();
        } catch (IOException ioe) {
            log.fatal("Unable to shutdown NIO", ioe);
        }
    }

    public void shutdown() {
        running = false;

        Set keys = selector.keys();
    }

    protected boolean add(Work work, long timeout, CallBack callBack) {
        boolean added = false;

        Set keys = selector.keys();
        if (keys.size() >= MAX) {
            log.error("NIOWorker is full");
            return false;
        }

        SocketChannel channel = null;

        try {
            URL url = work.method.getURL();
            int port = url.getPort() > 0 ? url.getPort() : 80;
            InetAddress ia = InetAddress.getByName(url.getHost());
            InetSocketAddress isa = new InetSocketAddress(ia, port);

            channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.connect(isa);

            work.outBound = ByteBuffer.wrap(work.method.toHTTP().getBytes());
            work.callBack = callBack;
            WorkState state = new WorkState(timeout, work);
            channel.register(selector, SelectionKey.OP_CONNECT, state);
            added = true;
        } catch (IOException ioe) {
            log.error("Problem adding work to NIOWorker thread", ioe);

            if (channel != null) {
                SelectionKey key = channel.keyFor(selector);
                if (key != null) {
                    key.cancel();
                }

                try {
                    channel.close();
                } catch (IOException ioe2) {
                    // Smother
                    log.error("Error closing channel", ioe2);
                }
            }
        }
        selector.wakeup();

        return added;
    }

    protected void processKeys() {
        Set keys = selector.selectedKeys();
        for (Iterator iter = keys.iterator(); iter.hasNext();) {
            SelectionKey key = (SelectionKey) iter.next();
            iter.remove();

            WorkState state = (WorkState) key.attachment();
            SocketChannel channel = (SocketChannel) key.channel();

            if (state.isTimedOut()) {
                finished(channel, key, state);
                continue;
            }

            try {
                if (key.isConnectable()) {
                    if (channel.finishConnect()) {
                        channel.register(selector, SelectionKey.OP_WRITE, state);
                    } else {
                        channel.register(selector, SelectionKey.OP_CONNECT, state);
                    }
                } else if (key.isWritable()) {
                    if (doWrite(channel, state)) {
                        channel.register(selector, SelectionKey.OP_READ, state);
                    } else {
                        channel.register(selector, SelectionKey.OP_WRITE, state);
                    }
                } else if (key.isReadable()) {
                    if (doRead(channel, state)) {
                        finished(channel, key, state);
                    } else {
                        channel.register(selector, SelectionKey.OP_READ, state);
                    }
                } else {
                    throw new IOException("INVALID NIO SelectionKey STATE!");
                }
            } catch (IOException ioe) {
                log.error("Error encountered while processing keys", ioe);
                finished(channel, key, state);
            }
        }
    }

    protected boolean doWrite(SocketChannel channel, WorkState state)
            throws IOException {
        Work work = state.work;
        int rem = work.outBound.remaining();
        int num = channel.write(work.outBound);
        return (num == rem);
    }

    protected boolean doRead(SocketChannel channel, WorkState state)
            throws IOException {
        inBound.clear();
        decoder.reset();

        boolean done = false;
        int num = channel.read(inBound);
        if (num == -1) {
            done = true;
            state.success = true;
        } else if (num > 0) {
            inBound.flip();
            CharBuffer buf = decoder.decode(inBound);
            state.parser.feed(buf.toString());
        }

        return done;
    }

    protected void finished(SocketChannel channel, SelectionKey key, WorkState state) {
        // First, cancel the key
        key.cancel();

        // Failed to register, close the channel and wake up thread
        try {
            channel.close();
        } catch (IOException ioe) {
            // smother, can't close the channel
            log.error(ioe);
        } finally {
            Work work = state.work;

            if (state.isTimedOut()) {
                log.fatal("NIO work timed out before it was completed");
            } else {
                if (state.success) {
                    work.method.setResponse(state.parser.getResponse());
                }

                HTTPResponse response = work.method.getResponse();
                if (response != null) {
                    response.setRunningTime(System.currentTimeMillis() - state.startTime);
                    work.callBack.setResponse(response);
                    threadPoolExecutor.submit(work.callBack);
                }

            }
        }
        running = false;
    }

    public static class WorkState {
        public final StringBuffer buffer = new StringBuffer();
        public final long startTime;
        public final long timeoutPoint;
        public boolean success = false;
        public Work work;
        public final HTTPParser parser;

        public WorkState(long timeout, Work work) {
            this.startTime = System.currentTimeMillis();
            this.timeoutPoint = startTime + timeout;
            this.work = work;
            this.parser = new HTTPParser(work.method);
        }

        public boolean isTimedOut() {
            long now = System.currentTimeMillis();
            return (now > timeoutPoint);
        }
    }

    public static class Work {
        public HTTPMethod method;
        public ByteBuffer outBound;
        public CallBack callBack;
    }
}
