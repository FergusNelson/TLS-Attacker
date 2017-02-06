/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2016 Ruhr University Bochum / Hackmanit GmbH
 *
 * Licensed under Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.tlsattacker.tls.config.delegate;

import com.beust.jcommander.JCommander;
import de.rub.nds.tlsattacker.tls.workflow.TlsConfig;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Robert Merget - robert.merget@rub.de
 */
public class ClientAuthenticationDelegateTest {

    private ClientAuthenticationDelegate delegate;
    private JCommander jcommander;
    private String[] args;

    public ClientAuthenticationDelegateTest() {
    }

    @Before
    public void setUp() {
        delegate = new ClientAuthenticationDelegate();
        jcommander = new JCommander(delegate);
    }

    /**
     * Test of isClientAuthentication method, of class
     * ClientAuthenticationDelegate.
     */
    @Test
    public void testIsClientAuthentication() {
        args = new String[1];
        args[0] = "-client_authentication";
        assertFalse(delegate.isClientAuthentication());
        jcommander.parse(args);
        assertTrue(delegate.isClientAuthentication());
    }

    /**
     * Test of setClientAuthentication method, of class
     * ClientAuthenticationDelegate.
     */
    @Test
    public void testSetClientAuthentication() {
        assertFalse(delegate.isClientAuthentication());
        delegate.setClientAuthentication(true);
        assertTrue(delegate.isClientAuthentication());
    }

    /**
     * Test of applyDelegate method, of class ClientAuthenticationDelegate.
     */
    @Test
    public void testApplyDelegate() {
        TlsConfig config = new TlsConfig();
        config.setClientAuthentication(false);
        args = new String[1];
        args[0] = "-client_authentication";
        jcommander.parse(args);
        delegate.applyDelegate(config);
        assertTrue(config.isClientAuthentication());

    }

}