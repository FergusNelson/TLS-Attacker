/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2016 Ruhr University Bochum / Hackmanit GmbH
 *
 * Licensed under Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package tlsattacker.fuzzer.agent;

import tlsattacker.fuzzer.agent.Agent;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import tlsattacker.fuzzer.graphs.BranchTrace;
import tlsattacker.fuzzer.graphs.Edge;
import tlsattacker.fuzzer.helper.LogFileIDManager;
import tlsattacker.fuzzer.result.AgentResult;
import tlsattacker.fuzzer.server.TLSServer;
import tlsattacker.fuzzer.certificate.ServerCertificateStructure;
import tlsattacker.fuzzer.testvector.TestVector;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * An Agent implemented with the modified Binary Instrumentation used by
 * American Fuzzy Lop
 * 
 * @author Robert Merget - robert.merget@rub.de
 */
public class AFLAgent extends Agent {

    /**
     * Logger
     */
    private static final Logger LOG = Logger.getLogger(AFLAgent.class.getName());

    /**
     * The name of the Agent when referred by command line
     */
    public static final String optionName = "AFL";

    /**
     * Parses a file into a BranchTrace object
     * 
     * @param file
     *            File to parse
     * @return Newly generated BranchTrace object
     */
    private static BranchTrace getBranchTrace(File file) {
        BufferedReader br = null;
        Set<Long> verticesSet = new HashSet<>();
        Map<Edge, Edge> edgeMap = new HashMap<>();
        try {
            br = new BufferedReader(new FileReader(file));
            long previousNumber = Long.MIN_VALUE;
            String line = null;
            while ((line = br.readLine()) != null) {
                // Check if the Line can be parsed
                long parsedNumber;
                try {
                    parsedNumber = Long.parseLong(line, 16);
                    verticesSet.add(parsedNumber);
                } catch (NumberFormatException e) {
                    if (line.contains("CRASH") || line.contains("TIMEOUT")) {
                        continue;
                    }
                    throw new NumberFormatException("BranchTrace contains unparsable Lines: " + line);
                }
                if (previousNumber != Long.MIN_VALUE) {
                    Edge e = edgeMap.get(new Edge(previousNumber, parsedNumber));
                    if (e == null) {
                        e = new Edge(previousNumber, parsedNumber);
                        edgeMap.put(e, e);
                    }
                    e.addCounter(1l);

                }
                previousNumber = parsedNumber;
            }
            return new BranchTrace(verticesSet, edgeMap);
        } catch (IOException ex) {
            Logger.getLogger(AFLAgent.class.getName()).log(Level.SEVERE,
                    "Could not read BranchTrace from file, using Empty BranchTrace instead", ex);
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(AFLAgent.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new BranchTrace();
    }

    /**
     * The prefix that has to be set in front of the actual server command
     */
    private final String prefix = "AFL/afl-showmap -m none -o [output]/[id] ";

    /**
     * Default Constructor
     * 
     * @param keypair
     *            The key certificate pair the server should be started with
     */
    public AFLAgent(ServerCertificateStructure keypair, TLSServer server) {
        super(keypair, server);
        timeout = false;
        crash = false;
    }

    @Override
    public void applicationStart() {
        if (running) {
            throw new IllegalStateException("Cannot start a running AFL Agent");
        }
        startTime = System.currentTimeMillis();
        running = true;
        server.start(prefix, keypair.getCertificateFile(), keypair.getKeyFile());
    }

    @Override
    public void applicationStop() {
        if (!running) {
            throw new IllegalStateException("Cannot stop a stopped AFL Agent");
        }
        stopTime = System.currentTimeMillis();
        running = false;
        server.stop();
    }

    @Override
    public AgentResult collectResults(File branchTrace, TestVector vector) {
        if (running) {
            throw new IllegalStateException("Can't collect Results, Agent still running!");
        }
        if (branchTrace.exists()) {
            String tail = tail(branchTrace);
            switch (tail) {
                case "CRASH":
                    LOG.log(Level.INFO, "Found a Crash!");
                    crash = true;
                    break;
                case "TIMEOUT":
                    LOG.log(Level.INFO, "Found a Timeout!");
                    timeout = true;
                    break;
            }
            BranchTrace t = getBranchTrace(branchTrace);

            AgentResult result = new AgentResult(crash, timeout, startTime, stopTime, t, vector, LogFileIDManager
                    .getInstance().getFilename(), server);

            return result;
        } else {
            LOG.log(Level.FINE, "Failed to collect instrumentation output");
            return new AgentResult(crash, timeout, startTime, startTime, new BranchTrace(), vector, LogFileIDManager
                    .getInstance().getFilename(), server);
        }
    }

    /**
     * Returns the last line in a File
     * 
     * @param file
     *            File to search in
     * @return Last line in the File
     */
    private String tail(File file) {
        RandomAccessFile fileHandler = null;
        try {
            fileHandler = new RandomAccessFile(file, "r");
            long fileLength = fileHandler.length() - 1;
            StringBuilder sb = new StringBuilder();

            for (long filePointer = fileLength; filePointer != -1; filePointer--) {
                fileHandler.seek(filePointer);
                int readByte = fileHandler.readByte();

                if (readByte == 0xA) {
                    if (filePointer == fileLength) {
                        continue;
                    }
                    break;

                } else if (readByte == 0xD) {
                    if (filePointer == fileLength - 1) {
                        continue;
                    }
                    break;
                }

                sb.append((char) readByte);
            }

            String lastLine = sb.reverse().toString();
            return lastLine;
        } catch (java.io.FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (fileHandler != null) {
                try {
                    fileHandler.close();
                } catch (IOException e) {
                    /* ignore */
                }
            }
        }
    }

}