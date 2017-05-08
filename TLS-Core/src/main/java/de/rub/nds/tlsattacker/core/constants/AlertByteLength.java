/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2017 Ruhr University Bochum / Hackmanit GmbH
 *
 * Licensed under Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.tlsattacker.core.constants;

/**
 *
 * @author Robert Merget - robert.merget@rub.de
 */
public class AlertByteLength {

    private AlertByteLength() {
    }

    /**
     * certificate length field
     */
    public static final int LEVEL_LENGTH = 1;

    /**
     * version field length
     */
    public static final int DESCRIPTION_LENGTH = 1;
}
